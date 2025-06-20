package com.example.restservice.service;

import com.example.restservice.model.File;
import com.example.restservice.repository.FileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import jakarta.annotation.PostConstruct;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Service
public class OCRService {
    private static final Logger log = LoggerFactory.getLogger(OCRService.class);
    private static final String OCR_DIR = "ocr/";
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".bmp", ".gif");
    private RestClient restClient;
    private final FileRepository fileRepository;
    private final FileService fileService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${ocr.api-base-url}")
    private String ocrApiBaseUrl;
    @Value("${ocr.api-key}")
    private String apiKey;

    public OCRService(FileRepository fileRepository, FileService fileService, RabbitTemplate rabbitTemplate) {
        this.fileRepository = fileRepository;
        this.fileService = fileService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
                .baseUrl(ocrApiBaseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        // Ensure ocr directory exists
        try {
            Files.createDirectories(Paths.get(OCR_DIR));
        } catch (Exception e) {
            log.error("Failed to create OCR directory: {}", e.getMessage(), e);
        }
    }

//    public void sendFileToOcr(Long fileId) {
//        rabbitTemplate.convertAndSend("OCRFileIds", fileId);
//    }

//    @RabbitListener(queues = "OCRFileIds")
    public void sendFileToOcr(Long fileId) {
        try {
            // Validate configuration
            if (ocrApiBaseUrl.isBlank() || apiKey.isBlank()) {
                log.error("OCR API configuration is incomplete");
                return;
            }

            // Retrieve file metadata
            Optional<File> fileOptional = fileRepository.findById(fileId);
            if (fileOptional.isEmpty()) {
                log.error("File ID {} not found, unable to send to OCR server", fileId);
                return;
            }

            File file = fileOptional.get();
            // Retrieve file content from OSS using FileService
            byte[] fileContent = fileService.getBytesArrayByFileName(file.getFileName());
            if (fileContent == null) {
                log.error("Failed to retrieve file content from OSS for file: {}", file.getFileName());
                return;
            }

            // Prepare multipart form data
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return file.getOriginalName();
                }
            });
            body.add("purpose", "ocr");

            // Upload file to OCR API
            Map<String, Object> uploadResponse = restClient.post()
                    .uri("/files")
                    .contentType(MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (uploadResponse == null || !uploadResponse.containsKey("id")) {
                log.error("Failed to upload file ID {} to OCR API", fileId);
                return;
            }

            String uploadedFileId = (String) uploadResponse.get("id");

            // Retrieve signed URL
            Map<String, Object> signedUrlResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/files/{file_id}/url")
                            .queryParam("expiry", 24)
                            .build(uploadedFileId))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (signedUrlResponse == null || !signedUrlResponse.containsKey("url")) {
                log.error("Failed to retrieve signed URL for file ID {}", fileId);
                return;
            }

            String signedUrl = (String) signedUrlResponse.get("url");

            // Prepare OCR processing payload with signed URL
            Map<String, Object> payload = prepareOCRPayload(signedUrl, "mistral-ocr-latest", null, null, null, null, file.getOriginalName());
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(payload);

            // Process OCR
            Map<String, Object> ocrResponse = restClient.post()
                    .uri("/ocr")
                    .contentType(APPLICATION_JSON)
                    .body(jsonString)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            // Extract markdown from all pages
            List<Map<String, Object>> pages = (List<Map<String, Object>>) ocrResponse.getOrDefault("pages", Collections.emptyList());
            if (pages.isEmpty()) {
                log.error("No pages returned in OCR response for file ID {}", fileId);
                return;
            }

            StringBuilder markdownContent = new StringBuilder();
            for (Map<String, Object> page : pages) {
                String markdown = (String) page.getOrDefault("markdown", "");
                if (!markdown.isEmpty()) {
                    markdownContent.append(markdown).append("\n\n");
                }
            }

            if (markdownContent.length() == 0) {
                log.error("No markdown content extracted from OCR for file ID {}", fileId);
                return;
            }

            // Convert markdown to PDF and save to /ocr directory
            String pdfFileName = file.getFileName().replaceFirst("\\.[^.]+$", ".pdf");
            Path pdfPath = Paths.get(OCR_DIR, pdfFileName);
            convertTextToPdf(markdownContent.toString(), pdfPath);

            log.info("File {} (ID: {}) successfully sent, processed, and saved as PDF", file.getOriginalName(), fileId);
        } catch (Exception e) {
            log.error("Failed to send/process file ID {} to OCR server: {}", fileId, e.getMessage(), e);
        }
    }

    public ResponseEntity<Resource> downloadFileFromOcr(Long fileId) {
        try {
            // Validate configuration
            if (ocrApiBaseUrl.isBlank() || apiKey.isBlank()) {
                log.error("OCR API configuration is incomplete");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(null);
            }

            // Retrieve file metadata
            Optional<File> fileOptional = fileRepository.findById(fileId);
            if (fileOptional.isEmpty()) {
                log.error("File ID {} not found, unable to download from OCR directory", fileId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            File file = fileOptional.get();
            String pdfFileName = file.getFileName().replaceFirst("\\.[^.]+$", ".pdf");
            Path pdfPath = Paths.get(OCR_DIR, pdfFileName);

            if (!Files.exists(pdfPath)) {
                log.error("PDF file does not exist for ID {} at path: {}", fileId, pdfPath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(null);
            }

            // Read PDF file content
            byte[] fileContent = Files.readAllBytes(pdfPath);
            ByteArrayResource resource = new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return pdfFileName;
                }
            };

            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment().filename(pdfFileName).build());
            headers.setContentLength(fileContent.length);

            log.info("File {} (ID: {}) successfully retrieved from OCR directory", pdfFileName, fileId);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            log.error("Failed to download file ID {} from OCR directory: {}", fileId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    private Map<String, Object> prepareOCRPayload(String documentUrl, String model, int[] pages,
                                                  Boolean includeImageBase64, Integer imageLimit, Integer imageMinSize,
                                                  String originalFileName) {
        Map<String, Object> payload = new HashMap<>();

        // Required fields
        payload.put("model", model);
        Map<String, String> document = new HashMap<>();

        // Determine if the file is an image based on extension
        String extension = originalFileName.substring(originalFileName.lastIndexOf(".")).toLowerCase();
        if (IMAGE_EXTENSIONS.contains(extension)) {
            document.put("image_url", documentUrl); // Use image_url for image files
        } else {
            document.put("document_url", documentUrl); // Use document_url for non-image files
        }
        payload.put("document", document);

        // Optional fields
        if (pages != null && pages.length > 0) {
            payload.put("pages", pages);
        }
        if (includeImageBase64 != null) {
            payload.put("include_image_base64", includeImageBase64);
        }
        if (imageLimit != null) {
            payload.put("image_limit", imageLimit);
        }
        if (imageMinSize != null) {
            payload.put("image_min_size", imageMinSize);
        }

        return payload;
    }

    private void convertTextToPdf(String text, Path pdfPath) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(pdfPath.toFile());
             PdfWriter writer = new PdfWriter(fos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document pdfDocument = new Document(pdfDoc)) {

            for (String line : text.split("\n")) {
                if (!line.trim().isEmpty()) {
                    pdfDocument.add(new Paragraph(line));
                }
            }
        } catch (Exception e) {
            log.error("Failed to convert text to PDF at {}: {}", pdfPath, e.getMessage(), e);
            throw e;
        }
    }

    public String getOcrApiBaseUrl() {
        return ocrApiBaseUrl;
    }

    public void setOcrApiBaseUrl(String ocrApiBaseUrl) {
        this.ocrApiBaseUrl = ocrApiBaseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
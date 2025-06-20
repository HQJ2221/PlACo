package com.example.restservice.controller;

import com.example.restservice.model.ApiResponse;
import com.example.restservice.model.File;
import com.example.restservice.repository.FileRepository;
import com.example.restservice.model.PostReturnData;
import com.example.restservice.service.FileConvertService;
import com.example.restservice.service.FileService;
import com.example.restservice.service.LogService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.restservice.service.OCRService; // 新增导入

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileRepository fileRepository;
    // 文件存储目录
    private static final String UPLOAD_DIR = "uploads/";
    private static final String PDF_DIR = "pdf/";
    private final OCRService ocrService; // 新增字段
    private final FileConvertService fileConvertService;
    private final FileService fileService;
    private final LogService logService;

    public FileController(FileRepository fileRepository,
                          OCRService ocrService,
                          FileConvertService fileConvertService,
                          FileService fileService,
                          LogService logService) {
        this.fileRepository = fileRepository;
        this.ocrService = ocrService;
        this.fileConvertService = fileConvertService;
        this.logService=logService;

        // 在构造函数中创建上传目录（如果不存在）
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录: " + UPLOAD_DIR, e);
        }
        this.fileService = fileService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createFile(@RequestParam("file") MultipartFile multipartFile) {
        try {
            // 验证文件是否有效
            if (multipartFile == null || multipartFile.isEmpty()) {
                return ApiResponse.internalServerError("上传文件不能为空");
            }

            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                return ApiResponse.internalServerError("文件名无效");
            }

            // 清理文件名，防止非法字符
            String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
            if (safeFilename.isEmpty()) {
                safeFilename = "unnamed_file";
            }

            // 创建 File 实体
            File file = new File();
            file.setOriginalName(originalFilename);

            // 生成唯一文件名
            String uniqueFileName = UUID.randomUUID() + "_" + safeFilename;
            file.setFileName(uniqueFileName);

            // 保存文件到本地目录
            Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);
            // 确保目标文件路径的父目录存在
            Files.createDirectories(filePath.getParent());
            // 使用 Files.copy 替代 transferTo，增加可靠性
            Files.copy(multipartFile.getInputStream(), filePath);

            // 保存文件元数据到数据库
            File savedFile = fileRepository.save(file);
            PostReturnData postReturnData = new PostReturnData();
            postReturnData.setId(savedFile.getId());

            //将文件存储到阿里云oss
            fileService.uploadFile(uniqueFileName, multipartFile.getBytes());

            logService.createLog("Create file: "+uniqueFileName);
            return ApiResponse.ok(postReturnData);
        } catch (IOException e) {
            // 记录详细错误
            e.printStackTrace();
            return ApiResponse.internalServerError("文件上传失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.internalServerError("服务器错误: " + e.getMessage());
        }
    }

//    @GetMapping
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<?> getAllFiles(@RequestParam(value = "download", defaultValue = "false") boolean download) {
//        List<File> files = fileRepository.findAll();
//        if (!download) {
//            // 默认返回元信息列表
//            return ApiResponse.ok(files);
//        }
//
//        // 返回所有文件作为 ZIP 压缩包
//        try {
//            // 创建临时文件用于存储 ZIP
//            Path tempZip = Files.createTempFile("files_", ".zip");
//            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(tempZip))) {
//                for (File file : files) {
//                    Path filePath = Paths.get(UPLOAD_DIR, file.getFileName());
//                    if (Files.exists(filePath)) {
//                        ZipEntry zipEntry = new ZipEntry(file.getOriginalName());
//                        zipOut.putNextEntry(zipEntry);
//                        Files.copy(filePath, zipOut);
//                        zipOut.closeEntry();
//                    }
//                }
//            }
//
//            Resource resource = new FileSystemResource(tempZip.toFile());
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//            headers.setContentDisposition(ContentDisposition.attachment().filename("files.zip").build());
//            headers.setContentLength(Files.size(tempZip));
//
//            logService.createLog("Download files: "+tempZip.toFile().getAbsolutePath());
//            // 设置响应，在请求完成后删除临时文件
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(resource);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ApiResponse.internalServerError("无法创建 ZIP 文件: " + e.getMessage());
//        }
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getFileById(@PathVariable Long id,
//                                         @RequestParam(value = "metadata", defaultValue = "false") boolean metadata) {
//        Optional<File> fileOptional = fileRepository.findById(id);
//        if (fileOptional.isEmpty()) {
//            return ApiResponse.internalServerError("文件未找到");
//        }
//
//        File file = fileOptional.get();
//        if (metadata) {
//            // 返回元信息
//            return ApiResponse.ok(file);
//        }
//
//        // 返回文件内容
//        Path filePath = Paths.get(UPLOAD_DIR, file.getFileName());
//        if (!Files.exists(filePath)) {
//            return ApiResponse.internalServerError("文件不存在于服务器");
//        }
//
//        try {
//            Resource resource = new FileSystemResource(filePath.toFile());
//            String contentType = Files.probeContentType(filePath);
//            if (contentType == null) {
//                contentType = "application/octet-stream";
//            }
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.parseMediaType(contentType));
//            headers.setContentDisposition(ContentDisposition.attachment().filename(file.getOriginalName()).build());
//            headers.setContentLength(Files.size(filePath));
//
//            logService.createLog("Get File: "+file.getOriginalName());
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(resource);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ApiResponse.internalServerError("无法读取文件: " + e.getMessage());
//        }
//    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllFiles(@RequestParam(value = "download", defaultValue = "false") boolean download) {
        List<File> files = fileRepository.findAll();
        if (!download) {
            // 默认返回元信息列表
            return ApiResponse.ok(files);
        }

        // 返回所有文件作为 ZIP 压缩包
        try {
            // 创建临时 ZIP 文件内容
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
                for (File file : files) {
                    byte[] fileContent = fileService.getBytesArrayByFileName(file.getFileName());
                    if (fileContent != null) {
                        ZipEntry zipEntry = new ZipEntry(file.getOriginalName());
                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(fileContent);
                        zipOut.closeEntry();
                    }
                }
            }

            Resource resource = new ByteArrayResource(baos.toByteArray());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename("files.zip").build());
            headers.setContentLength(baos.size());

            logService.createLog("Download files as ZIP");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.internalServerError("无法创建 ZIP 文件: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFileById(@PathVariable Long id,
                                         @RequestParam(value = "metadata", defaultValue = "false") boolean metadata) {
        Optional<File> fileOptional = fileRepository.findById(id);
        if (fileOptional.isEmpty()) {
            return ApiResponse.internalServerError("文件未找到");
        }

        File file = fileOptional.get();
        if (metadata) {
            // 返回元信息
            return ApiResponse.ok(file);
        }

        // 返回文件内容
        byte[] fileContent = fileService.getBytesArrayByFileName(file.getFileName());
        if (fileContent == null) {
            return ApiResponse.internalServerError("文件不存在于服务器");
        }

        try {
            Resource resource = new ByteArrayResource(fileContent);
            String contentType = "application/octet-stream"; // 简单设置，实际可根据文件类型推断
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDisposition(ContentDisposition.attachment().filename(file.getOriginalName()).build());
            headers.setContentLength(fileContent.length);

            logService.createLog("Get File: " + file.getOriginalName());
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.internalServerError("无法读取文件: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/ocr")
    public ResponseEntity<?> getOCRFile(@PathVariable Long id) {
        // 获取远端已经OCR的pdf文件

        logService.createLog("Get OCR File: "+id);
        return ocrService.downloadFileFromOcr(id);
    }

    // AI-generated-content
    // tool: ChatGPT
    // version: latest
    // usage: generate and manual debug
    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> getPDFFile(@PathVariable Long id) {
        Optional<File> fileOptional = fileRepository.findById(id);
        if (fileOptional.isEmpty()) {
            return ApiResponse.internalServerError();
        }
        File file = fileOptional.get();
        String filename = file.getFileName();
        String inputPath = Paths.get(UPLOAD_DIR, filename).toString();
        String pdfFilename = filename.substring(0, filename.lastIndexOf('.')) + ".pdf";
        String outputPath = Paths.get(PDF_DIR, pdfFilename).toString();
        String originalFilename = file.getOriginalName();
        String originalPdfFileName = originalFilename.substring(0, originalFilename.lastIndexOf('.')) + ".pdf";

        // Check if the original file is already a PDF
        if (filename.toLowerCase().endsWith(".pdf")) {
            Resource resource = new FileSystemResource(inputPath);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                    .body(resource);
        }

        try {
            // Check if PDF already exists in pdf_dir
            Path pdfPath = Paths.get(outputPath);
            if (Files.exists(pdfPath)) {
                // Return existing PDF
                Resource resource = new FileSystemResource(pdfPath);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalPdfFileName + "\"")
                        .body(resource);
            }

            Files.createDirectories(pdfPath.getParent());

            // Attempt to convert the file to PDF
            boolean conversionSuccess = fileConvertService.convertToPdf(inputPath, outputPath);
            if (!conversionSuccess) {
                return ApiResponse.badRequest("Unsupported file format for conversion.");
            }

            Resource resource = new FileSystemResource(pdfPath);

            logService.createLog("Create PDF: "+pdfFilename);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalPdfFileName + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ApiResponse.internalServerError("Error converting file to PDF: " + e.getMessage());
        }
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
//        if (!fileRepository.existsById(id)) {
//            return ApiResponse.internalServerError();
//        }
//        Optional<File> fileOptional = fileRepository.findById(id);
//        if (fileOptional.isEmpty()) {
//            return ApiResponse.internalServerError("文件未找到");
//        }
//        File file = fileOptional.get();
//        fileRepository.deleteById(id);
//        // 需要删除文件
//        try {
//            // 删除本地文件
//            Path filePath = Paths.get(UPLOAD_DIR, file.getFileName());
//            Files.deleteIfExists(filePath);
//            // 删除数据库记录
//            fileRepository.deleteById(id);
//
//            logService.createLog("File deleted: "+id);
//            return ApiResponse.ok();
//        } catch (IOException e) {
//            return ApiResponse.internalServerError("删除文件失败: " + e.getMessage());
//        }
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        Optional<File> fileOptional = fileRepository.findById(id);
        if (fileOptional.isEmpty()) {
            return ApiResponse.internalServerError("文件未找到");
        }

        File file = fileOptional.get();
        try {
            // 删除 OSS 中的文件
            fileService.deleteFile(file.getFileName());
            // 删除数据库记录
            fileRepository.deleteById(id);

            logService.createLog("File deleted: " + id);
            return ApiResponse.ok();
        } catch (IOException e) {
            return ApiResponse.internalServerError("删除文件失败: " + e.getMessage());
        }
    }
}
package com.example.restservice.service;

import com.example.restservice.model.File;
import com.example.restservice.repository.FileRepository;
import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Transactional
public class OCRServiceTest {
    @Autowired
    OCRService ocrService;

    @Autowired
    FileService fileService;

    @Autowired
    FileRepository fileRepository;

    @Test
    public void testOCR() {
        Assumptions.assumeThat(ocrService.getOcrApiBaseUrl()).isNotBlank();
        Assumptions.assumeThat(ocrService.getApiKey()).isNotBlank();
        assertDoesNotThrow(() -> {
            java.io.File tempFile = java.io.File.createTempFile("test_ocr", ".txt", new java.io.File(fileService.getUploadDir()));
            tempFile.deleteOnExit();
            String testContent = "Sample OCR Text";
            try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(tempFile))) {
                writer.write(testContent.getBytes(StandardCharsets.UTF_8));
            }

            // Create and persist File entity
            File file = new File();
            file.setOriginalName(tempFile.getName());
            file.setFileName(tempFile.getName());
            fileRepository.save(file);

            ocrService.sendFileToOcr(file.getId());
            ocrService.downloadFileFromOcr(file.getId());
        });
    }
}
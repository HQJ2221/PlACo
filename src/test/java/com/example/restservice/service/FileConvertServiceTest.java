package com.example.restservice.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextBox;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

// AI-generated-content
// tool: ChatGPT
// version: latest
// usage: generate and manual debug
@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class FileConvertServiceTest {

    @Autowired
    private FileConvertService fileConvertService;

    @TempDir
    File tempDir;

    @Test
    public void testConvertToPdf() throws IOException {
        // Test 1: Convert .txt file to PDF
        File txtFile = new File(tempDir, "test.txt");
        String txtContent = "Sample text content";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
            writer.write(txtContent);
        }

        File txtPdfOutput = new File(tempDir, "test_txt_output.pdf");
        boolean txtResult = fileConvertService.convertToPdf(txtFile.getAbsolutePath(), txtPdfOutput.getAbsolutePath());
        assertTrue(txtResult, "Text file conversion should succeed");
        assertTrue(txtPdfOutput.exists(), "Text PDF output file should exist");

        // Verify PDF content
        try (PdfReader reader = new PdfReader(txtPdfOutput); PdfDocument pdfDoc = new PdfDocument(reader)) {
            String extractedText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(1), new SimpleTextExtractionStrategy());
            assertTrue(extractedText.contains("Sample text"), "PDF should contain the input text");
        }

        // Test 2: Convert .jpg file to PDF
        File jpgFile = new File(tempDir, "test.jpg");
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ImageIO.write(image, "jpg", jpgFile);

        File jpgPdfOutput = new File(tempDir, "test_jpg_output.pdf");
        boolean jpgResult = fileConvertService.convertToPdf(jpgFile.getAbsolutePath(), jpgPdfOutput.getAbsolutePath());
        assertTrue(jpgResult, "Image file conversion should succeed");
        assertTrue(jpgPdfOutput.exists(), "Image PDF output file should exist");

        // Verify PDF has at least one page (basic check for image presence)
        try (PdfReader reader = new PdfReader(jpgPdfOutput); PdfDocument pdfDoc = new PdfDocument(reader)) {
            assertEquals(1, pdfDoc.getNumberOfPages(), "Image PDF should have one page");
        }
    }

    @Test
    public void testConvertDocxToPdf() throws IOException {
        // Create .docx file
        File docxFile = new File(tempDir, "test.docx");
        String docxContent = "Test DOCX Content";
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFParagraph paragraph = doc.createParagraph();
            paragraph.createRun().setText(docxContent);
            try (FileOutputStream out = new FileOutputStream(docxFile)) {
                doc.write(out);
            }
        }

        File pdfOutput = new File(tempDir, "test_docx_output.pdf");
        boolean result = fileConvertService.convertToPdf(docxFile.getAbsolutePath(), pdfOutput.getAbsolutePath());
        assertTrue(result, "DOCX conversion should succeed");
        assertTrue(pdfOutput.exists(), "DOCX PDF output file should exist");

        // Verify PDF content
        try (PdfReader reader = new PdfReader(pdfOutput); PdfDocument pdfDoc = new PdfDocument(reader)) {
            String extractedText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(1), new SimpleTextExtractionStrategy());
            assertTrue(extractedText.contains("Test DOCX"), "PDF should contain DOCX text");
        }
    }



    @Test
    public void testConvertPptxToPdf() throws IOException {
        // Create .pptx file
        File pptxFile = new File(tempDir, "test.pptx");
        String pptxContent = "Test PPTX Content";
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTextBox textBox = slide.createTextBox();
            textBox.setText(pptxContent);
            try (FileOutputStream out = new FileOutputStream(pptxFile)) {
                ppt.write(out);
            }
        }

        File pdfOutput = new File(tempDir, "test_pptx_output.pdf");
        boolean result = fileConvertService.convertToPdf(pptxFile.getAbsolutePath(), pdfOutput.getAbsolutePath());
        assertTrue(result, "PPTX conversion should succeed");
        assertTrue(pdfOutput.exists(), "PPTX PDF output file should exist");

        // Verify PDF content
//        try (PdfReader reader = new PdfReader(pdfOutput); PdfDocument pdfDoc = new PdfDocument(reader)) {
//            String extractedText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(1), new SimpleTextExtractionStrategy());
//            assertTrue(extractedText.contains("Test PPTX"), "PDF should contain PPTX text");
//        }
    }

    @Test
    public void testConvertPptToPdf() throws IOException {
        // Create .ppt file
        File pptFile = new File(tempDir, "test.ppt");
        String pptContent = "Test PPT Content";
        try (HSLFSlideShow ppt = new HSLFSlideShow()) {
            HSLFSlide slide = ppt.createSlide();
            HSLFTextBox textBox = slide.createTextBox();
            textBox.setText(pptContent);
            try (FileOutputStream out = new FileOutputStream(pptFile)) {
                ppt.write(out);
            }
        }

        File pdfOutput = new File(tempDir, "test_ppt_output.pdf");
        boolean result = fileConvertService.convertToPdf(pptFile.getAbsolutePath(), pdfOutput.getAbsolutePath());
        assertTrue(result, "PPT conversion should succeed");
        assertTrue(pdfOutput.exists(), "PPT PDF output file should exist");

        // Verify PDF content
        try (PdfReader reader = new PdfReader(pdfOutput); PdfDocument pdfDoc = new PdfDocument(reader)) {
            String extractedText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(1), new SimpleTextExtractionStrategy());
            assertTrue(extractedText.contains("Test PPT"), "PDF should contain PPT text");
        }
    }

    @Test
    public void testConvertXlsxToPdf() throws IOException {
        // Create .xlsx file
        File xlsxFile = new File(tempDir, "test.xlsx");
        String xlsxContent = "Test XLSX Content";
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("TestSheet");
            XSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue(xlsxContent);
            try (FileOutputStream out = new FileOutputStream(xlsxFile)) {
                workbook.write(out);
            }
        }

        File pdfOutput = new File(tempDir, "test_xlsx_output.pdf");
        boolean result = fileConvertService.convertToPdf(xlsxFile.getAbsolutePath(), pdfOutput.getAbsolutePath());
        assertTrue(result, "XLSX conversion should succeed");
        assertTrue(pdfOutput.exists(), "XLSX PDF output file should exist");

        // Verify PDF content
        try (PdfReader reader = new PdfReader(pdfOutput); PdfDocument pdfDoc = new PdfDocument(reader)) {
            String extractedText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(1), new SimpleTextExtractionStrategy());
            assertTrue(extractedText.contains("Test XLSX"), "PDF should contain XLSX text");
            assertTrue(extractedText.contains("Sheet: TestSheet"), "PDF should contain sheet name");
        }
    }

    @Test
    public void testConvertXlsToPdf() throws IOException {
        // Create .xls file
        File xlsFile = new File(tempDir, "test.xls");
        String xlsContent = "Test XLS Content";
        try (org.apache.poi.hssf.usermodel.HSSFWorkbook workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook()) {
            org.apache.poi.hssf.usermodel.HSSFSheet sheet = workbook.createSheet("TestSheet");
            org.apache.poi.hssf.usermodel.HSSFRow row = sheet.createRow(0);
            row.createCell(0).setCellValue(xlsContent);
            try (FileOutputStream out = new FileOutputStream(xlsFile)) {
                workbook.write(out);
            }
        }

        File pdfOutput = new File(tempDir, "test_xls_output.pdf");
        boolean result = fileConvertService.convertToPdf(xlsFile.getAbsolutePath(), pdfOutput.getAbsolutePath());
        assertTrue(result, "XLS conversion should succeed");
        assertTrue(pdfOutput.exists(), "XLS PDF output file should exist");

        // Verify PDF content
        try (PdfReader reader = new PdfReader(pdfOutput); PdfDocument pdfDoc = new PdfDocument(reader)) {
            String extractedText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(1), new SimpleTextExtractionStrategy());
            assertTrue(extractedText.contains("Test XLS"), "PDF should contain XLS text");
            assertTrue(extractedText.contains("Sheet: TestSheet"), "PDF should contain sheet name");
        }
    }
}
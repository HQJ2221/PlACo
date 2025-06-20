package com.example.restservice.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

// AI-generated-content
// tool: ChatGPT
// version: latest
// usage: generate and manual debug
@Service
public class FileConvertService {
    public boolean convertToPdf(String inputPath, String outputPath) throws IOException {
        String extension = getFileExtension(inputPath).toLowerCase();

        switch (extension) {
            case "docx":
                convertDocxToPdf(inputPath, outputPath);
                break;
            case "doc":
                convertDocToPdf(inputPath, outputPath);
                break;
            case "pptx":
                convertPptxToPdf(inputPath, outputPath);
                break;
            case "ppt":
                convertPptToPdf(inputPath, outputPath);
                break;
            case "xlsx":
                convertXlsxToPdf(inputPath, outputPath);
                break;
            case "xls":
                convertXlsToPdf(inputPath, outputPath);
                break;
            case "txt":
            case "py":
            case "java":
            case "js":
                convertTxtToPdf(inputPath, outputPath);
                break;
            case "jpg":
            case "png":
            case "jpeg":
            case "gif":
                convertImageToPdf(inputPath, outputPath);
                break;
            default:
                return false;
        }
        return true;
    }

    private String getFileExtension(String filePath) {
        int lastIndex = filePath.lastIndexOf('.');
        if (lastIndex > 0 && lastIndex < filePath.length() - 1) {
            return filePath.substring(lastIndex + 1);
        }
        return "";
    }

    public void convertDocxToPdf(String docxPath, String pdfPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(docxPath);
             XWPFDocument document = new XWPFDocument(fis)) {
            try (FileOutputStream fos = new FileOutputStream(pdfPath);
                 PdfWriter writer = new PdfWriter(fos);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document pdfDocument = new Document(pdfDoc)) {
                for (XWPFParagraph paragraph : document.getParagraphs()) {
                    StringBuilder paragraphText = new StringBuilder();
                    for (XWPFRun run : paragraph.getRuns()) {
                        paragraphText.append(run.getText(0));
                    }
                    if (!paragraphText.isEmpty()) {
                        pdfDocument.add(new Paragraph(paragraphText.toString()));
                    }
                }
            }
        }
    }

    public void convertDocToPdf(String docPath, String pdfPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(docPath);
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {

            try (FileOutputStream fos = new FileOutputStream(pdfPath);
                 PdfWriter writer = new PdfWriter(fos);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document pdfDocument = new Document(pdfDoc)) {

                String[] paragraphs = extractor.getParagraphText();

                for (String paragraph : paragraphs) {
                    if (paragraph != null && !paragraph.trim().isEmpty()) {
                        pdfDocument.add(new Paragraph(paragraph));
                    }
                }
            }
        }
    }

    public void convertPptxToPdf(String pptxPath, String pdfPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(pptxPath);
             XMLSlideShow slideShow = new XMLSlideShow(fis)) {

            Dimension pgsize = slideShow.getPageSize();
            int widthPt = (int) pgsize.getWidth();
            int heightPt = (int) pgsize.getHeight();

            try (FileOutputStream fos = new FileOutputStream(pdfPath);
                 PdfWriter writer = new PdfWriter(fos);
                 PdfDocument pdfDoc = new PdfDocument(writer)) {

                pdfDoc.setDefaultPageSize(new com.itextpdf.kernel.geom.PageSize(widthPt, heightPt));

                try (Document document = new Document(pdfDoc)) {
                    document.setMargins(0, 0, 0, 0);

                    for (XSLFSlide slide : slideShow.getSlides()) {
                        BufferedImage img = new BufferedImage(widthPt, heightPt, BufferedImage.TYPE_INT_RGB);
                        Graphics2D graphics = img.createGraphics();
                        graphics.setPaint(Color.white);
                        graphics.fill(new Rectangle(0, 0, widthPt, heightPt));
                        slide.draw(graphics);
                        graphics.dispose();

                        Image pdfImg = new Image(ImageDataFactory.create(img, null));
                        document.add(pdfImg);
                    }
                }
            }
        }
    }

    public void convertPptToPdf(String pptPath, String pdfPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(pptPath);
             HSLFSlideShow ppt = new HSLFSlideShow(fis)) {

            try (FileOutputStream fos = new FileOutputStream(pdfPath);
                 PdfWriter writer = new PdfWriter(fos);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document pdfDocument = new Document(pdfDoc)) {

                for (HSLFSlide slide : ppt.getSlides()) {
                    for (HSLFShape shape : slide.getShapes()) {
                        if (shape instanceof HSLFTextShape textShape) {
                            String text = textShape.getText();
                            if (text != null && !text.trim().isEmpty()) {
                                pdfDocument.add(new Paragraph(text));
                            }
                        }
                    }
                    pdfDocument.add(new Paragraph("--- Slide ---"));
                }
            }
        }
    }

    public void convertXlsxToPdf(String xlsxPath, String pdfPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(xlsxPath);
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

            try (FileOutputStream fos = new FileOutputStream(pdfPath);
                 PdfWriter writer = new PdfWriter(fos);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document pdfDocument = new Document(pdfDoc)) {

                PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    XSSFSheet sheet = workbook.getSheetAt(i);
                    String sheetName = sheet.getSheetName();

                    Paragraph sheetParagraph = new Paragraph("Sheet: " + sheetName);
                    sheetParagraph.setFont(boldFont);
                    pdfDocument.add(sheetParagraph);

                    for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                        XSSFRow row = sheet.getRow(rowIndex);
                        if (row != null) {
                            StringBuilder rowText = new StringBuilder();
                            for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                                XSSFCell cell = row.getCell(cellIndex);
                                String cellValue = (cell == null) ? "" : cell.toString();
                                rowText.append(cellValue).append("\t");
                            }
                            if (!rowText.isEmpty()) {
                                pdfDocument.add(new Paragraph("Row " + (rowIndex + 1) + ": " + rowText.toString()));
                            }
                        }
                    }
                    pdfDocument.add(new Paragraph("--- End of Sheet ---"));
                }
            }
        }
    }

    public void convertXlsToPdf(String xlsPath, String pdfPath) throws IOException {
        try (FileInputStream fis = new FileInputStream(xlsPath);
             HSSFWorkbook workbook = new HSSFWorkbook(fis)) {

            try (FileOutputStream fos = new FileOutputStream(pdfPath);
                 PdfWriter writer = new PdfWriter(fos);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document pdfDocument = new Document(pdfDoc)) {

                PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    HSSFSheet sheet = workbook.getSheetAt(i);
                    String sheetName = sheet.getSheetName();

                    Paragraph sheetParagraph = new Paragraph("Sheet: " + sheetName);
                    sheetParagraph.setFont(boldFont);
                    pdfDocument.add(sheetParagraph);

                    for (int rowIndex = 0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                        HSSFRow row = sheet.getRow(rowIndex);
                        if (row != null) {
                            StringBuilder rowText = new StringBuilder();
                            for (int cellIndex = 0; cellIndex < row.getLastCellNum(); cellIndex++) {
                                HSSFCell cell = row.getCell(cellIndex);
                                String cellValue = (cell == null) ? "" : cell.toString();
                                rowText.append(cellValue).append("\t");
                            }
                            if (!rowText.isEmpty()) {
                                pdfDocument.add(new Paragraph("Row " + (rowIndex + 1) + ": " + rowText.toString()));
                            }
                        }
                    }
                    pdfDocument.add(new Paragraph("--- End of Sheet ---"));
                }
            }
        }
    }

    public void convertTxtToPdf(String txtPath, String pdfPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(txtPath))) {
            try (FileOutputStream fos = new FileOutputStream(pdfPath);
                 PdfWriter writer = new PdfWriter(fos);
                 PdfDocument pdfDoc = new PdfDocument(writer);
                 Document pdfDocument = new Document(pdfDoc)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        pdfDocument.add(new Paragraph(line));
                    }
                }
            }
        }
    }

    public void convertImageToPdf(String imagePath, String pdfPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(pdfPath);
             PdfWriter writer = new PdfWriter(fos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document pdfDocument = new Document(pdfDoc)) {

            Image image = new Image(ImageDataFactory.create(imagePath));

            float pageWidth = pdfDoc.getDefaultPageSize().getWidth() - pdfDocument.getLeftMargin() - pdfDocument.getRightMargin();
            float pageHeight = pdfDoc.getDefaultPageSize().getHeight() - pdfDocument.getTopMargin() - pdfDocument.getBottomMargin();
            image.scaleToFit(pageWidth, pageHeight);

            pdfDocument.add(image);
        }
    }
}

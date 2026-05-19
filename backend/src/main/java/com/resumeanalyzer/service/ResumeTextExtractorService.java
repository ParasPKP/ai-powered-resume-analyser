package com.resumeanalyzer.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class ResumeTextExtractorService {

    /**
     * Extract text from uploaded resume file (PDF or DOCX)
     */
    public String extractText(MultipartFile file) throws IOException {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new IllegalArgumentException("File content type is unknown");
        }

        return switch (contentType) {
            case "application/pdf" -> extractPdfText(file.getInputStream());
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
                    extractDocxText(file.getInputStream());
            case "application/msword" ->
                    throw new IllegalArgumentException("Legacy .doc format is not supported. Please upload a .docx file.");
            default ->
                    throw new IllegalArgumentException("Unsupported file type: " + contentType + ". Only PDF and DOCX files are supported.");
        };
    }

    /**
     * Extract text from PDF using Apache PDFBox
     */
    private String extractPdfText(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return cleanText(text);
        }
    }

    /**
     * Extract text from DOCX using Apache POI
     */
    private String extractDocxText(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            StringBuilder text = new StringBuilder();

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String paraText = paragraph.getText();
                if (paraText != null && !paraText.isBlank()) {
                    text.append(paraText).append("\n");
                }
            }

            // Also extract text from tables
            document.getTables().forEach(table ->
                    table.getRows().forEach(row ->
                            row.getTableCells().forEach(cell -> {
                                String cellText = cell.getText();
                                if (cellText != null && !cellText.isBlank()) {
                                    text.append(cellText).append(" ");
                                }
                            })
                    )
            );

            return cleanText(text.toString());
        }
    }

    /**
     * Clean extracted text: normalize whitespace, trim, and limit length
     */
    private String cleanText(String text) {
        if (text == null) return "";

        // Remove excessive whitespace, normalize spacing
        text = text.replaceAll("\\s+", " ")
                .replaceAll("\\n+", "\n")
                .trim();

        // Limit text length to prevent token overflow (keep first 6000 chars)
        if (text.length() > 6000) {
            text = text.substring(0, 6000);
        }

        return text;
    }
}

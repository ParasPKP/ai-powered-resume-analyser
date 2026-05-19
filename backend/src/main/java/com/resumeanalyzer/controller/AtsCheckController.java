package com.resumeanalyzer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.resumeanalyzer.service.GroqAiService;
import com.resumeanalyzer.service.ResumeTextExtractorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class AtsCheckController {

    private static final Logger log = LoggerFactory.getLogger(AtsCheckController.class);

    private final ResumeTextExtractorService textExtractor;
    private final GroqAiService groqAiService;
    private final ObjectMapper objectMapper;

    public AtsCheckController(ResumeTextExtractorService textExtractor,
                               GroqAiService groqAiService,
                               ObjectMapper objectMapper) {
        this.textExtractor = textExtractor;
        this.groqAiService = groqAiService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/atsCheck")
    public ResponseEntity<?> atsCheck(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(errorJson("No file provided"));
            }

            // Extract text from resume
            String resumeText = textExtractor.extractText(file);

            if (resumeText == null || resumeText.trim().length() < 20) {
                return ResponseEntity.badRequest().body(
                        errorJson("Could not extract meaningful text from resume")
                );
            }

            // Build AI prompt (exact same as JS version)
            String atsPrompt = """
                You are an expert ATS (Applicant Tracking System) specialist with years of experience in resume screening. Analyze the following resume for ATS compatibility.
                
                ATS SCORING CRITERIA:
                1. Formatting (0-100):
                   - Clean, simple layout: +25 points
                   - No images/graphics/tables: +25 points
                   - Standard fonts and sections: +25 points
                   - Proper spacing and structure: +25 points
                
                2. Keywords (0-100):
                   - Relevant technical skills: +30 points
                   - Industry-specific terms: +30 points
                   - Action verbs: +20 points
                   - Quantifiable achievements: +20 points
                
                3. Sections (0-100):
                   - Contact Information: +20 points
                   - Professional Summary: +15 points
                   - Work Experience: +25 points
                   - Education: +20 points
                   - Skills: +20 points
                
                4. Overall ATS Score (0-100): Weighted average of formatting, keywords, and sections
                5. Passability Score (0-100): Likelihood of passing automated ATS screening
                
                Resume Text:
                %s
                
                Return ONLY valid JSON (no markdown, no code blocks):
                {
                  "atsScore": number (0-100),
                  "passabilityScore": number (0-100),
                  "overallRating": "Excellent" or "Good" or "Needs Improvement",
                  "strengths": ["strength1", "strength2", "strength3"],
                  "weaknesses": ["weakness1", "weakness2", "weakness3"],
                  "formatting": {
                    "score": number (0-100),
                    "issues": ["issue1", "issue2"]
                  },
                  "keywords": {
                    "score": number (0-100),
                    "analysis": "detailed keyword analysis paragraph"
                  },
                  "sections": {
                    "score": number (0-100),
                    "analysis": "detailed section analysis paragraph"
                  },
                  "recommendations": "comprehensive paragraph with actionable improvements"
                }
                """.formatted(resumeText);

            // Call Groq AI
            JsonNode analysis = groqAiService.chat(atsPrompt, 0.1, 2500);

            log.info("ATS check completed. Score: {}", analysis.path("atsScore").asInt());

            // Validate required fields
            String[] requiredFields = {
                    "atsScore", "passabilityScore", "overallRating",
                    "strengths", "weaknesses", "formatting",
                    "keywords", "sections", "recommendations"
            };
            for (String field : requiredFields) {
                if (analysis.path(field).isMissingNode()) {
                    log.warn("ATS analysis missing field: {}", field);
                }
            }

            return ResponseEntity.ok(analysis);

        } catch (GroqAiService.GroqApiException e) {
            log.error("Groq API Error in ATS: {}", e.getMessage());
            if (e.getStatusCode() == 400) {
                return ResponseEntity.badRequest().body(
                        errorJson("ATS analysis failed. Please ensure your resume is text-based, not image-based.")
                );
            }
            return ResponseEntity.internalServerError().body(
                    errorJson("ATS check failed: " + e.getMessage())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorJson(e.getMessage()));
        } catch (Exception e) {
            log.error("ATS check error: ", e);
            return ResponseEntity.internalServerError().body(
                    errorJson("Failed to check ATS compatibility: " + e.getMessage())
            );
        }
    }

    private ObjectNode errorJson(String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("error", message);
        return error;
    }
}

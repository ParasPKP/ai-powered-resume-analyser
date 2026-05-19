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
public class MatchJobController {

    private static final Logger log = LoggerFactory.getLogger(MatchJobController.class);

    private final ResumeTextExtractorService textExtractor;
    private final GroqAiService groqAiService;
    private final ObjectMapper objectMapper;

    public MatchJobController(ResumeTextExtractorService textExtractor,
                               GroqAiService groqAiService,
                               ObjectMapper objectMapper) {
        this.textExtractor = textExtractor;
        this.groqAiService = groqAiService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/matchJob")
    public ResponseEntity<?> matchJob(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobDescription") String jobDescription) {
        try {
            if (file.isEmpty() || jobDescription == null || jobDescription.isBlank()) {
                return ResponseEntity.badRequest().body(
                        errorJson("File and job description are required")
                );
            }

            // Extract text from resume
            String resumeText = textExtractor.extractText(file);

            if (resumeText == null || resumeText.length() < 20) {
                return ResponseEntity.badRequest().body(
                        errorJson("Could not extract text from resume")
                );
            }

            // Build AI prompt (exact same as JS version)
            String matchPrompt = """
                You are an expert job-matching specialist and recruitment consultant. Compare the candidate's resume with the job description and provide a detailed compatibility analysis.
                
                SCORING CRITERIA:
                1. Match Percentage (0-100): Overall fit for the position
                   - Technical skills alignment: 35%%
                   - Experience level match: 30%%
                   - Domain expertise: 20%%
                   - Education requirements: 15%%
                
                2. Experience Match (0-100): Years and type of experience relevance
                   - Directly relevant experience: +50 points
                   - Related/transferable experience: +30 points
                   - Leadership/management if required: +20 points
                
                3. Role Alignment (0-100): How well the candidate fits the specific role
                   - Core responsibilities match: +40 points
                   - Required certifications/qualifications: +30 points
                   - Cultural/soft skills fit: +30 points
                
                Resume Content:
                %s
                
                Job Description:
                %s
                
                Analyze thoroughly and return ONLY valid JSON (no markdown, no code blocks):
                {
                  "matchPercentage": number (0-100),
                  "experienceMatch": number (0-100),
                  "roleAlignment": number (0-100),
                  "matchedKeywords": ["keyword1", "keyword2", "..."],
                  "missingKeywords": ["missing1", "missing2", "..."],
                  "skillGaps": ["gap1 with improvement suggestion", "gap2 with improvement suggestion"],
                  "recommendations": "detailed paragraph with specific actions the candidate should take to improve their fit",
                  "jobTitle": "extracted job title from job description"
                }
                """.formatted(resumeText, jobDescription);

            // Call Groq AI
            JsonNode analysis = groqAiService.chat(matchPrompt, 0.2, 2000);

            log.info("Job match completed. Match: {}%", analysis.path("matchPercentage").asInt());

            // Validate response
            if (analysis.path("matchPercentage").isMissingNode() ||
                    analysis.path("experienceMatch").isMissingNode()) {
                throw new RuntimeException("Invalid AI response structure");
            }

            return ResponseEntity.ok(analysis);

        } catch (GroqAiService.GroqApiException e) {
            log.error("Groq API Error in Job Match: {}", e.getMessage());
            if (e.getStatusCode() == 400) {
                return ResponseEntity.badRequest().body(
                        errorJson("Job matching failed. Please check your resume and job description.")
                );
            }
            return ResponseEntity.internalServerError().body(
                    errorJson("Job matching failed: " + e.getMessage())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorJson(e.getMessage()));
        } catch (Exception e) {
            log.error("Job matching error: ", e);
            return ResponseEntity.internalServerError().body(
                    errorJson("Failed to analyze job match: " + e.getMessage())
            );
        }
    }

    private ObjectNode errorJson(String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("error", message);
        return error;
    }
}

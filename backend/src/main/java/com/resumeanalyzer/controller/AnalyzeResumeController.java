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
public class AnalyzeResumeController {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeResumeController.class);

    private final ResumeTextExtractorService textExtractor;
    private final GroqAiService groqAiService;
    private final ObjectMapper objectMapper;

    public AnalyzeResumeController(ResumeTextExtractorService textExtractor,
                                    GroqAiService groqAiService,
                                    ObjectMapper objectMapper) {
        this.textExtractor = textExtractor;
        this.groqAiService = groqAiService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/analyzeResume")
    public ResponseEntity<?> analyzeResume(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        errorJson("No file uploaded")
                );
            }

            // Extract text from resume
            String extractedText = textExtractor.extractText(file);

            if (extractedText == null || extractedText.trim().length() < 20) {
                return ResponseEntity.badRequest().body(
                        errorJson("Could not extract text from resume")
                );
            }

            // Build AI prompt (exact same prompt as JS version)
            String prompt = """
                You are an expert ATS (Applicant Tracking System) resume analyzer. Analyze the following resume comprehensively and provide HIGH SCORES for strong resumes.
                
                SCORING CRITERIA (Be GENEROUS for strong candidates):
                
                1. ATS Score (0-100): Based on format, keywords, sections, readability
                   - Proper sections (Education, Experience, Skills, Projects): +25 points
                   - Contact information present (email, phone, LinkedIn, GitHub): +10 points
                   - Clear formatting and structure: +15 points
                   - Relevant keywords and technical skills: +20 points
                   - Quantifiable achievements with metrics: +15 points
                   - Professional language and certifications: +15 points
                   
                   BONUS SCORING:
                   - LeetCode/competitive coding (250+ problems): +5 points
                   - Cloud certifications (Azure, AWS, GCP): +5 points
                   - Multiple internships/work experience: +5 points
                   - Production projects with real users: +5 points
                   - Strong CGPA (8.5+/10): +3 points
                   - GitHub contributions (200+): +3 points
                
                2. Skill Match (0-100%%): Based on technical skills, experience level, and domain expertise
                   - Technical skills clearly listed and diverse: +30%%
                   - Relevant experience/internships: +25%%
                   - Projects with real-world impact: +20%%
                   - Domain expertise and certifications: +15%%
                   - Problem-solving proof (LeetCode, GitHub): +10%%
                
                IMPORTANT:
                - A resume with LeetCode 250+, internships, certifications, and strong CGPA should score 90-95/100
                - Be realistic but FAIR - don't underscore strong candidates
                - Students with multiple internships, projects, and skills deserve 85-95 range
                - Basic resumes with limited experience should score 60-75 range
                
                ANALYSIS REQUIREMENTS:
                - Provide constructive feedback
                - List 3-5 specific strengths
                - List 3-5 specific weaknesses with improvement suggestions
                - Suggest 5-10 missing keywords relevant to the candidate's field
                - Give actionable suggestions for improvement
                
                Resume Content:
                %s
                
                Return ONLY valid JSON (no markdown, no code blocks, no extra text):
                {
                  "atsScore": number (0-100),
                  "skillMatch": number (0-100),
                  "missingKeywords": ["keyword1", "keyword2", "..."],
                  "summary": "2-3 sentence overview of the candidate's profile",
                  "suggestions": "Detailed paragraph with actionable improvement suggestions",
                  "strengths": ["strength1", "strength2", "strength3"],
                  "weaknesses": ["weakness1 with fix suggestion", "weakness2 with fix suggestion", "weakness3 with fix suggestion"]
                }
                """.formatted(extractedText);

            // Call Groq AI
            JsonNode analysis = groqAiService.chat(prompt, 0.2, 2000);

            log.info("Resume analysis completed. ATS Score: {}", analysis.path("atsScore").asInt());

            // Build response
            ObjectNode response = objectMapper.createObjectNode();
            response.put("success", true);
            response.set("analysis", analysis);

            return ResponseEntity.ok(response);

        } catch (GroqAiService.GroqApiException e) {
            log.error("Groq API Error: {}", e.getMessage());
            if (e.getStatusCode() == 400) {
                return ResponseEntity.badRequest().body(
                        errorJson("AI analysis failed. Please ensure your resume has clear text content and is not image-based.")
                );
            }
            return ResponseEntity.internalServerError().body(
                    errorJson("Analysis failed: " + e.getMessage())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorJson(e.getMessage()));
        } catch (Exception e) {
            log.error("Analysis Error: ", e);
            return ResponseEntity.internalServerError().body(
                    errorJson("Analysis failed: " + e.getMessage())
            );
        }
    }

    private ObjectNode errorJson(String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("error", message);
        return error;
    }
}

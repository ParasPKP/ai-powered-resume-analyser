package com.resumeanalyzer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.resumeanalyzer.service.GroqAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class GenerateQuestionsController {

    private static final Logger log = LoggerFactory.getLogger(GenerateQuestionsController.class);

    private final GroqAiService groqAiService;
    private final ObjectMapper objectMapper;

    public GenerateQuestionsController(GroqAiService groqAiService, ObjectMapper objectMapper) {
        this.groqAiService = groqAiService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/generateQuestions")
    public ResponseEntity<?> generateQuestions(@RequestBody Map<String, String> request) {
        try {
            String jobRole = request.get("jobRole");

            if (jobRole == null || jobRole.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(errorJson("Job role is required"));
            }

            String prompt = """
                You are an expert technical interviewer and HR specialist. Generate 6-7 diverse interview questions for a "%s" position.
                
                QUESTION CATEGORIES (mix these):
                1. Technical/Domain-specific: 2-3 questions testing core skills
                2. Behavioral/STAR: 2 questions about past experiences
                3. Situational: 1-2 questions about hypothetical scenarios
                4. Problem-solving: 1 question testing analytical thinking
                
                REQUIREMENTS:
                - Questions should be specific to the %s role
                - Mix difficulty levels (1-2 basic, 3-4 intermediate, 1-2 advanced)
                - Include both open-ended and scenario-based questions
                - Make questions practical and relevant to actual job responsibilities
                
                Return ONLY valid JSON (no markdown, no code blocks):
                {
                  "questions": [
                    "Question 1 text here",
                    "Question 2 text here",
                    "Question 3 text here",
                    "Question 4 text here",
                    "Question 5 text here",
                    "Question 6 text here"
                  ]
                }
                """.formatted(jobRole, jobRole);

            JsonNode result = groqAiService.chat(prompt, 0.7, 1500);

            log.info("Generated questions for role: {}", jobRole);

            // Validate questions array
            JsonNode questions = result.path("questions");
            if (questions.isMissingNode() || !questions.isArray() || questions.size() < 5) {
                throw new RuntimeException("Invalid questions format");
            }

            return ResponseEntity.ok(result);

        } catch (GroqAiService.GroqApiException e) {
            log.error("Groq API Error in Questions: {}", e.getMessage());
            if (e.getStatusCode() == 400) {
                return ResponseEntity.badRequest().body(
                        errorJson("Question generation failed. Please try again.")
                );
            }
            return ResponseEntity.internalServerError().body(
                    errorJson("Question generation failed: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("Question generation error: ", e);
            return ResponseEntity.internalServerError().body(
                    errorJson("Failed to generate questions: " + e.getMessage())
            );
        }
    }

    private ObjectNode errorJson(String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("error", message);
        return error;
    }
}

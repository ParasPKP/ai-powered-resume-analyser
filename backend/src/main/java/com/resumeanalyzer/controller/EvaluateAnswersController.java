package com.resumeanalyzer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.resumeanalyzer.service.GroqAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EvaluateAnswersController {

    private static final Logger log = LoggerFactory.getLogger(EvaluateAnswersController.class);

    private final GroqAiService groqAiService;
    private final ObjectMapper objectMapper;

    public EvaluateAnswersController(GroqAiService groqAiService, ObjectMapper objectMapper) {
        this.groqAiService = groqAiService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/evaluateAnswers")
    public ResponseEntity<?> evaluateAnswers(@RequestBody Map<String, Object> request) {
        try {
            String jobRole = (String) request.get("jobRole");
            @SuppressWarnings("unchecked")
            List<String> questions = (List<String>) request.get("questions");
            @SuppressWarnings("unchecked")
            List<String> answers = (List<String>) request.get("answers");

            if (jobRole == null || questions == null || answers == null) {
                return ResponseEntity.badRequest().body(
                        errorJson("Job role, questions, and answers are required")
                );
            }

            if (questions.size() != answers.size()) {
                return ResponseEntity.badRequest().body(
                        errorJson("Number of questions and answers must match")
                );
            }

            // Prepare combined Q/A text
            StringBuilder qaText = new StringBuilder();
            for (int i = 0; i < questions.size(); i++) {
                qaText.append(String.format("Question %d: %s\nAnswer: %s\n\n",
                        i + 1, questions.get(i), answers.get(i)));
            }

            String prompt = """
                You are an expert interviewer, career coach, and technical evaluator. Evaluate the following interview responses for a %s position.
                
                EVALUATION CRITERIA:
                
                1. Overall Score (0-100): Weighted composite of all categories
                   - Consider answer quality, depth, and relevance
                
                2. Category Scores (0-100 each):
                   - Communication: Clarity, structure, articulation
                   - Technical: Domain knowledge, technical accuracy, depth
                   - Problem Solving: Analytical thinking, creativity, approach
                   - Leadership: Initiative, teamwork, decision-making
                
                3. Feedback Requirements:
                   - Provide specific, actionable feedback
                   - Highlight 2-3 key strengths
                   - Identify 2-3 areas for improvement with suggestions
                   - Give 2-3 specific recommendations for preparation
                
                Interview Responses:
                %s
                
                Return ONLY valid JSON (no markdown, no code blocks):
                {
                  "overallScore": number (0-100),
                  "categoryScores": {
                    "communication": number (0-100),
                    "technical": number (0-100),
                    "problemSolving": number (0-100),
                    "leadership": number (0-100)
                  },
                  "feedback": "comprehensive paragraph summarizing overall performance",
                  "detailedFeedback": [
                    "feedback point 1 about specific answer",
                    "feedback point 2 about specific answer",
                    "feedback point 3 about specific answer"
                  ],
                  "strengths": [
                    "strength 1 with example",
                    "strength 2 with example"
                  ],
                  "improvements": [
                    "improvement area 1 with actionable suggestion",
                    "improvement area 2 with actionable suggestion"
                  ],
                  "recommendations": "detailed paragraph with specific preparation strategies"
                }
                """.formatted(jobRole, qaText.toString());

            JsonNode result = groqAiService.chat(prompt, 0.3, 2500);

            log.info("Answer evaluation completed. Score: {}", result.path("overallScore").asInt());

            // Validate response structure
            if (result.path("overallScore").isMissingNode() ||
                    result.path("categoryScores").isMissingNode()) {
                throw new RuntimeException("Invalid evaluation response structure");
            }

            return ResponseEntity.ok(result);

        } catch (GroqAiService.GroqApiException e) {
            log.error("Groq API Error in Evaluation: {}", e.getMessage());
            if (e.getStatusCode() == 400) {
                return ResponseEntity.badRequest().body(
                        errorJson("Answer evaluation failed. Please try again.")
                );
            }
            return ResponseEntity.internalServerError().body(
                    errorJson("Evaluation failed: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("Answer evaluation error: ", e);
            return ResponseEntity.internalServerError().body(
                    errorJson("Failed to evaluate answers: " + e.getMessage())
            );
        }
    }

    private ObjectNode errorJson(String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("error", message);
        return error;
    }
}

# AI Resume Analyzer - Java Backend

Spring Boot backend for the AI-powered Resume Analyzer application.

## Tech Stack

- **Java 17** + Spring Boot 3.2
- **Apache PDFBox** - PDF text extraction
- **Apache POI** - DOCX text extraction
- **OkHttp** - HTTP client for Groq AI API calls
- **Jackson** - JSON processing

## Prerequisites

- Java 17 or higher (JDK)
- Maven 3.8+ (or use the Maven wrapper)

## Setup

### 1. Install Java JDK 17

Download from: https://adoptium.net/temurin/releases/?version=17

After installation, verify:
```bash
java -version
```

### 2. Install Maven

Download from: https://maven.apache.org/download.cgi

After installation, verify:
```bash
mvn -version
```

### 3. Configure API Keys

Edit `src/main/resources/application.properties` and set your Groq API keys:
```properties
groq.api.keys=YOUR_KEY_1,YOUR_KEY_2,YOUR_KEY_3
```

Get your API keys from: https://console.groq.com/keys

### 4. Build & Run

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The backend starts on **http://localhost:8080**

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/analyzeResume` | Analyze resume (multipart file) |
| POST | `/api/atsCheck` | ATS compatibility check (multipart file) |
| POST | `/api/matchJob` | Job matching (file + job description) |
| POST | `/api/generateQuestions` | Generate interview questions (JSON) |
| POST | `/api/evaluateAnswers` | Evaluate interview answers (JSON) |

## Project Structure

```
backend/
├── pom.xml
├── src/main/java/com/resumeanalyzer/
│   ├── ResumeAnalyzerApplication.java     # Main entry point
│   ├── config/
│   │   ├── CorsConfig.java                # CORS configuration
│   │   └── GroqConfig.java                # API key management
│   ├── controller/
│   │   ├── AnalyzeResumeController.java   # Resume analysis
│   │   ├── AtsCheckController.java        # ATS compatibility
│   │   ├── MatchJobController.java        # Job matching
│   │   ├── GenerateQuestionsController.java # Mock interview questions
│   │   └── EvaluateAnswersController.java # Answer evaluation
│   └── service/
│       ├── ResumeTextExtractorService.java # PDF/DOCX text extraction
│       └── GroqAiService.java             # Groq AI API client
└── src/main/resources/
    └── application.properties              # Configuration
```

## Running with Frontend

1. Start the Java backend: `mvn spring-boot:run` (port 8080)
2. Start the React frontend: `npm run dev` (port 4000)
3. The Vite dev server proxies `/api/*` requests to the Java backend

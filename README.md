
<h1 align="center">🤖 AI-Powered Resume Analyzer</h1>

<p align="center">
  <strong>Analyze, Optimize & Ace Your Resume with AI</strong>
</p>

<p align="center">
  <a href="https://github.com/ParasPKP/ai-powered-resume-analyser"><img src="https://img.shields.io/badge/GitHub-Repository-181717?style=for-the-badge&logo=github" alt="GitHub Repo" /></a>
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React" />
  <img src="https://img.shields.io/badge/Groq_AI-LLaMA_3.3-FF6B35?style=for-the-badge&logo=meta&logoColor=white" alt="Groq AI" />
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#%EF%B8%8F-tech-stack">Tech Stack</a> •
  <a href="#-getting-started">Getting Started</a> •
  <a href="#-api-endpoints">API Endpoints</a> •
  <a href="#-project-structure">Project Structure</a> •
  <a href="#-developer">Developer</a>
</p>

---

## 📖 About

**AI-Powered Resume Analyzer** is a full-stack web application that helps job seekers optimize their resumes using artificial intelligence. Upload your resume (PDF/DOCX) and get instant feedback on ATS compatibility, keyword optimization, job matching, and interview preparation.

Built with a **React** frontend and a **Java Spring Boot** backend, powered by **Groq AI (LLaMA 3.3 70B)** for intelligent resume analysis.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 📄 **Resume Analyzer** | Upload PDF/DOCX and get ATS score, skill match, strengths, weaknesses, and AI recommendations |
| ✅ **ATS Checker** | Check format, keyword usage, and section quality for ATS compatibility |
| 🎯 **Job Matching** | Compare your resume against a job description — see match %, missing keywords, and skill gaps |
| 🎤 **Mock Interview** | AI-generated interview questions tailored to your target role with detailed scoring |
| 📊 **Visual Reports** | Interactive charts (Bar, Pie, Radar) for score visualization |
| 📥 **PDF Export** | Save analysis reports as PDF |
| 🌙 **Dark Mode** | Full dark/light theme support |
| 💾 **Local History** | Recent results saved in browser localStorage |

---

## 🛠️ Tech Stack

### Frontend
| Technology | Purpose |
|-----------|---------|
| React 18 | UI Components |
| React Router 7 | File-based routing |
| Vite | Build tool & dev server |
| Tailwind CSS | Styling |
| Recharts | Data visualization (charts) |
| Lucide React | Icons |

### Backend
| Technology | Purpose |
|-----------|---------|
| Java 17 | Programming language |
| Spring Boot 3.2 | REST API framework |
| Apache PDFBox 3.0 | PDF text extraction |
| Apache POI 5.2 | DOCX text extraction |
| OkHttp 4.12 | HTTP client for AI API calls |
| Groq AI (LLaMA 3.3 70B) | AI-powered analysis |

---

## 🚀 Getting Started

### Prerequisites

- **Java 17 JDK** — [Download from Adoptium](https://adoptium.net/temurin/releases/?version=17)
- **Maven 3.8+** — [Download from Maven](https://maven.apache.org/download.cgi)
- **Node.js 18+** — [Download from Node.js](https://nodejs.org/)
- **Groq API Key** — [Get from Groq Console](https://console.groq.com/keys)

### Installation

#### 1. Clone the repository
```bash
git clone https://github.com/ParasPKP/ai-powered-resume-analyser.git
cd ai-powered-resume-analyser
```

#### 2. Set up the Backend

```bash
cd backend
```

Create a `.env` file (use `.env.example` as reference):
```bash
cp .env.example .env
```

Add your Groq API keys in `backend/.env`:
```env
GROQ_API_KEY01=your_groq_api_key_1
```

Start the backend server:
```bash
mvn spring-boot:run
```
> ✅ Backend starts on **http://localhost:8080**

#### 3. Set up the Frontend

Open a **new terminal** in the project root:
```bash
npm install        # first time only
npm run dev
```
> ✅ Frontend starts on **http://localhost:4000**

#### 4. Open the App

Navigate to **http://localhost:4000** in your browser and start analyzing resumes!

---

## 📡 API Endpoints

All API endpoints are served by the Java Spring Boot backend on port `8080`. The frontend dev server automatically proxies `/api/*` requests.

| Method | Endpoint | Input | Description |
|--------|----------|-------|-------------|
| `POST` | `/api/analyzeResume` | Multipart (PDF/DOCX file) | Full resume analysis with ATS scoring |
| `POST` | `/api/atsCheck` | Multipart (PDF/DOCX file) | ATS compatibility check |
| `POST` | `/api/matchJob` | Multipart (file + job description) | Resume vs job description matching |
| `POST` | `/api/generateQuestions` | JSON `{ "jobRole": "..." }` | Generate mock interview questions |
| `POST` | `/api/evaluateAnswers` | JSON `{ "jobRole", "questions", "answers" }` | Evaluate interview answers |

---


## 🤝 Contributing

Contributions are welcome! Here's how:

1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/amazing-feature`
3. **Commit** your changes: `git commit -m 'Add amazing feature'`
4. **Push** to branch: `git push origin feature/amazing-feature`
5. **Open** a Pull Request

---

## 👨‍💻 Developer

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/ParasPKP">
        <img src="https://github.com/ParasPKP.png" width="120px;" alt="Paras Parshuramkar" style="border-radius:50%"/>
        <br />
        <strong>Paras Parshuramkar</strong>
      </a>
      <br />
      <a href="https://github.com/ParasPKP" title="GitHub">
        <img src="https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white" alt="GitHub" />
      </a>
      <a href="https://www.linkedin.com/in/paras-parshuramkar-b8237b315/" title="LinkedIn">
        <img src="https://img.shields.io/badge/LinkedIn-0A66C2?style=flat-square&logo=linkedin&logoColor=white" alt="LinkedIn" />
      </a>
    </td>
  </tr>
</table>

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/ParasPKP">Paras Parshuramkar</a>
</p>

<p align="center">
  <a href="https://github.com/ParasPKP/ai-powered-resume-analyser">⭐ Star this repo if you found it helpful!</a>
</p>

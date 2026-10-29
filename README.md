# TrustCheck 🛡️

> **"Fake internships? Fake certifications? No more after us."**  
> *Verify Before You Trust.*

TrustCheck is a student-first web application designed to detect potentially fraudulent internship offers, recruitment scams, and fake credentials.

---

## 🎨 Clean White & Black Design

- **Minimalist Aesthetic**: Professional, human-crafted black & white interface with clean typography, sharp borders, and subtle card elevation (no AI gradients, glowing animations, or hackathon tags).
- **Deterministic Color Codes**:
  - `VERIFIED`: Clean Emerald Green badge (`#166534`)
  - `SUSPICIOUS`: Clean Crimson Red badge (`#991B1B`)
  - `UNVERIFIED`: Clean Amber badge (`#92400E`)

---

## 🚀 Instant Deployment to Vercel & GitHub

TrustCheck is pre-configured with `vercel.json`, root `index.html`, and a `.gitignore` file, making it 100% plug-and-play for GitHub and Vercel.

### Step 1: Push to GitHub
In your project directory (`C:\Users\Ashwath Ramanathan\OneDrive\Desktop\TC\trustcheck`):
```bash
git init
git add .
git commit -m "Initial commit: TrustCheck web application"
git branch -M main
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/trustcheck.git
git push -u origin main
```

### Step 2: Deploy on Vercel
1. Go to [vercel.com](https://vercel.com/) and log in.
2. Click **"Add New..."** &rarr; **"Project"**.
3. Import your `trustcheck` GitHub repository.
4. Click **"Deploy"** *(Zero configuration needed — Vercel detects root `index.html` and `vercel.json` immediately)*.
5. Your live URL is generated in ~10 seconds!

---

## 💻 Local Execution via Java

You can also run TrustCheck locally using the built-in pure Java SE server:

### Windows (PowerShell or Command Prompt):
```powershell
.\run.bat
```
*(or run `.\run.ps1`)*

### Linux / macOS:
```bash
chmod +x run.sh test.sh
./run.sh
```

Then visit: **http://localhost:8080/**

---

## 🧪 Automated Tests
Run the 15 automated test cases:
```bash
.\test.bat
```
Or directly:
```bash
java -cp bin com.trustcheck.test.TrustCheckTest
```

---

## 📁 Repository Structure

```text
trustcheck/
├── index.html                  # Root frontend for instant Vercel deployment
├── vercel.json                 # Vercel configuration
├── .gitignore                  # Git ignore rules for Java build artifacts
├── README.md                   # Documentation and deployment guide
├── run.bat                     # Windows batch runner
├── run.ps1                     # PowerShell runner
├── run.sh                      # Linux/macOS runner
├── test.bat                    # Test suite runner
├── data/
│   └── community_reports.json # Persistent JSON data store
├── src/com/trustcheck/         # Modular Java standard library backend
│   ├── model/                  # Domain entities and status enums
│   ├── repository/             # Starter database and report persistence
│   ├── service/                # 3-layer verification and keyword heuristics
│   ├── server/                 # Built-in HttpServerApp
│   ├── util/                   # Zero-dependency JSON serializer and XSS escaper
│   └── test/                   # Comprehensive test suite
└── web/
    └── index.html              # Synchronized frontend copy
```

# TrustCheck PowerShell Runner
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "  TrustCheck - Fake Internship & Certificate Verification Engine  " -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

# Locate javac and java
$javac = (Get-Command javac -ErrorAction SilentlyContinue).Source
$java = (Get-Command java -ErrorAction SilentlyContinue).Source

if (!$javac -and $env:JAVA_HOME) {
    if (Test-Path "$env:JAVA_HOME\bin\javac.exe") {
        $javac = "$env:JAVA_HOME\bin\javac.exe"
        $java = "$env:JAVA_HOME\bin\java.exe"
    }
}

if (!$javac) {
    $jdk = Get-ChildItem "C:\Program Files\Java\jdk*" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($jdk -and (Test-Path "$($jdk.FullName)\bin\javac.exe")) {
        $javac = "$($jdk.FullName)\bin\javac.exe"
        $java = "$($jdk.FullName)\bin\java.exe"
    }
}

if (!$javac) {
    $javac = "javac"
    $java = "java"
}

if (!(Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" -Force | Out-Null
}

Write-Host "[1/2] Compiling TrustCheck Java source files..." -ForegroundColor Gray
$javaFiles = Get-ChildItem -Path "src" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
& $javac -encoding UTF-8 -d bin $javaFiles

if (!(Test-Path "bin\com\trustcheck\server\HttpServerApp.class")) {
    Write-Host "[ERROR] Compilation failed. Please ensure JDK 11+ is installed." -ForegroundColor Red
    exit 1
}

Write-Host "[✓] Compilation verified." -ForegroundColor Green
Write-Host ""
Write-Host "[2/2] Starting TrustCheck HTTP server on port 8080..." -ForegroundColor Cyan
Write-Host "Open your browser at: http://localhost:8080/" -ForegroundColor Green
Write-Host "Press Ctrl+C to stop the server." -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan

& $java -cp bin com.trustcheck.server.HttpServerApp 8080

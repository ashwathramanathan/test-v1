#!/usr/bin/env bash
set -e

echo "=================================================================="
echo "  TrustCheck - Fake Internship & Certificate Verification Engine  "
echo "=================================================================="

if ! command -v java &> /dev/null; then
    echo "[ERROR] Java is not installed or not in PATH."
    exit 1
fi

mkdir -p bin

echo "[1/2] Compiling Java source files..."
javac -encoding UTF-8 -d bin $(find src -name "*.java")

echo "[✓] Compilation successful!"
echo ""
echo "[2/2] Starting TrustCheck HTTP server on port 8080..."
echo "Open your browser at: http://localhost:8080/"
echo "Press Ctrl+C to terminate."
echo "=================================================================="

java -cp bin com.trustcheck.server.HttpServerApp 8080

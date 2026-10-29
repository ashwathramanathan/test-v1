@echo off
setlocal enabledelayedexpansion

echo ==================================================================
echo   TrustCheck - Fake Internship ^& Certificate Verification
echo ==================================================================

:: Detect JDK
set "JAVAC_BIN=javac"
set "JAVA_BIN=java"

where javac >nul 2>&1
if %errorlevel% neq 0 (
    if defined JAVA_HOME (
        if exist "%JAVA_HOME%\bin\javac.exe" (
            set "JAVAC_BIN=%JAVA_HOME%\bin\javac.exe"
            set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
        )
    )
)

where "%JAVAC_BIN%" >nul 2>&1
if %errorlevel% neq 0 (
    if not exist "%JAVAC_BIN%" (
        for /d %%i in ("C:\Program Files\Java\jdk*") do (
            if exist "%%i\bin\javac.exe" (
                set "JAVAC_BIN=%%i\bin\javac.exe"
                set "JAVA_BIN=%%i\bin\java.exe"
            )
        )
    )
)

where "%JAVAC_BIN%" >nul 2>&1
if %errorlevel% neq 0 (
    if not exist "%JAVAC_BIN%" (
        for /d %%i in ("C:\Program Files\Java\latest") do (
            if exist "%%i\bin\javac.exe" (
                set "JAVAC_BIN=%%i\bin\javac.exe"
                set "JAVA_BIN=%%i\bin\java.exe"
            )
        )
    )
)

if not exist "bin" mkdir bin

echo [1/2] Compiling TrustCheck Java source files...
"%JAVAC_BIN%" -encoding UTF-8 -d bin src\com\trustcheck\model\*.java src\com\trustcheck\util\*.java src\com\trustcheck\repository\*.java src\com\trustcheck\service\*.java src\com\trustcheck\server\*.java src\com\trustcheck\test\*.java >nul 2>&1

if not exist "bin\com\trustcheck\server\HttpServerApp.class" (
    echo [ERROR] Could not compile source files. Please ensure JDK 11+ is installed.
    pause
    exit /b 1
)

echo [✓] Compilation verified.
echo.
echo [2/2] Starting TrustCheck HTTP server on port 8080...
echo Open your browser at: http://localhost:8080/
echo Press Ctrl+C to stop the server.
echo ==================================================================

"%JAVA_BIN%" -cp bin com.trustcheck.server.HttpServerApp 8080
if %errorlevel% neq 0 (
    pause
)

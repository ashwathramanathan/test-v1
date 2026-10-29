@echo off
setlocal enabledelayedexpansion

echo ==================================================================
echo   TrustCheck - Running Automated Test Suite
echo ==================================================================

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

if not exist "bin" mkdir bin

echo Compiling test suite...
"%JAVAC_BIN%" -encoding UTF-8 -d bin src\com\trustcheck\model\*.java src\com\trustcheck\util\*.java src\com\trustcheck\repository\*.java src\com\trustcheck\service\*.java src\com\trustcheck\server\*.java src\com\trustcheck\test\*.java >nul 2>&1

echo Executing TrustCheckTest...
"%JAVA_BIN%" -cp bin com.trustcheck.test.TrustCheckTest
pause

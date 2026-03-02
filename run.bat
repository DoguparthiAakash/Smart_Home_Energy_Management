@echo off
setlocal enabledelayedexpansion

:: Change to script directory
cd /d "%~dp0"

echo ======================================================
echo   Smart Home Energy Management - Windows Startup
echo ======================================================

echo [1/4] Checking environment...
java -version >nul 2>&1
if !ERRORLEVEL! neq 0 (
    echo Error: Java is not installed or not in PATH.
    pause
    exit /b 1
)

:: Only build if jar doesn't exist to speed up subsequent runs
if not exist "backend\target\backend-0.0.1-SNAPSHOT.jar" (
    echo [2/4] Building project, this may take a minute...
    mvn -version >nul 2>&1
    if !ERRORLEVEL! neq 0 (
        echo Error: Maven is not installed or not in PATH.
        pause
        exit /b 1
    )
    set MAVEN_OPTS=-XX:+UseSerialGC -Xmx1024m
    call mvn -f backend\pom.xml clean package -DskipTests
    if !ERRORLEVEL! neq 0 (
        echo.
        echo BUILD FAILED. Please check the logs above.
        pause
        exit /b 1
    )
) else (
    echo [2/4] Found existing build, skipping Maven compilation...
)

echo [3/4] Cleaning up port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    echo Killing existing process on 8080, PID: %%a...
    taskkill /F /PID %%a >nul 2>&1
)

echo [4/4] Launching Application...
echo.
echo The Dashboard will open automatically once the server is ready.
echo.

:: Automatically open browser
start "" http://localhost:8080

java -XX:+UseSerialGC -Xmx1024m -jar backend\target\backend-0.0.1-SNAPSHOT.jar

if !ERRORLEVEL! equ 130 (
    echo.
    echo Server stopped gracefully.
    exit /b 0
)

if !ERRORLEVEL! equ 2 (
    echo.
    echo Server stopped gracefully.
    exit /b 0
)

if !ERRORLEVEL! neq 0 (
    echo.
    echo Application exited with error code !ERRORLEVEL!
    pause
)

endlocal

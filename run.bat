@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ======================================================
echo   Smart Home Energy Management - Startup (Verbose)
echo ======================================================

echo [1/4] Checking environment...
java -version
if !ERRORLEVEL! neq 0 (
    echo [ERROR] Java not found! Please install JDK 17+ and add to PATH.
    pause & exit /b 1
)

mvn -version
if !ERRORLEVEL! neq 0 (
    echo [ERROR] Maven (mvn) not found! Please install Maven and add to PATH.
    echo Current PATH: %PATH%
    pause & exit /b 1
)

echo [2/4] Building project (this can take ~30-60 seconds)...
call mvn -f backend\pom.xml clean package -DskipTests
if !ERRORLEVEL! neq 0 (
    echo [ERROR] BUILD FAILED!
    pause & exit /b 1
)

echo [3/4] Verify JAR exists...
if not exist "backend\target\backend-0.0.1-SNAPSHOT.jar" (
    echo [ERROR] JAR file not found even though Build reported SUCCESS!
    pause & exit /b 1
)

echo [4/4] Launching...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    taskkill /F /PID %%a >nul 2>&1
)

start "" http://localhost:8080
java -XX:+UseSerialGC -Xmx1024m -jar backend\target\backend-0.0.1-SNAPSHOT.jar

if !ERRORLEVEL! neq 0 (
    echo [ERROR] Application crashed or stopped with code !ERRORLEVEL!.
    pause
)

endlocal

@echo off
setlocal enabledelayedexpansion

echo ======================================================
echo   Smart Home Energy Management - Windows Startup
echo ======================================================

echo [1/4] Checking environment...
java -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: Java is not installed or not in PATH.
    pause
    exit /b 1
)

mvn -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: Maven is not installed or not in PATH.
    pause
    exit /b 1
)

echo [2/4] Cleaning up port 8080...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    echo Killing existing process on 8080 (PID: %%a)...
    taskkill /F /PID %%a >nul 2>&1
)

echo [3/4] Building project (this may take a minute)...
set MAVEN_OPTS=-XX:+UseSerialGC -Xmx1024m
call mvn -f backend/pom.xml clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo.
    echo BUILD FAILED. Please check the logs above.
    pause
    exit /b %ERRORLEVEL%
)

echo [4/4] Launching Application...
echo.
echo Dashboard will be available at: http://localhost:8080
echo.

java -XX:+UseSerialGC -Xmx1024m -jar backend/target/backend-0.0.1-SNAPSHOT.jar

if %ERRORLEVEL% neq 0 (
    echo.
    echo Application exited with error code %ERRORLEVEL%
    pause
)

endlocal

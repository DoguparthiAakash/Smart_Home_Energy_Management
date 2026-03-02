# Smart Home Energy Management - PowerShell Startup
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "  Smart Home Energy Management - Startup (PS)" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan

# 1. Check Environment
Write-Host "[1/4] Checking environment..." -ForegroundColor Yellow
if (!(Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Error "Java not found! Please install JDK 17+."
    return
}

# 2. Build Project
Write-Host "[2/4] Building project (this takes ~30-60s)..." -ForegroundColor Yellow
$mvn = Get-Command mvn -ErrorAction SilentlyContinue
if (!$mvn) {
    Write-Error "Maven (mvn) not found in PATH!"
    return
}

& mvn -f backend/pom.xml clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Error "Build Failed!"
    return
}

# 3. Verify JAR
$jarPath = "backend/target/backend-0.0.1-SNAPSHOT.jar"
if (!(Test-Path $jarPath)) {
    Write-Error "JAR file not found after build!"
    return
}

# 4. Cleanup & Launch
Write-Host "[3/4] Checking port 8080..." -ForegroundColor Yellow
$portCheck = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($portCheck) {
    Write-Host "Cleaning up port 8080 (PID: $($portCheck.OwningProcess))..."
    Stop-Process -Id $portCheck.OwningProcess -Force
}

Write-Host "[4/4] Launching Application..." -ForegroundColor Green
Start-Process "http://localhost:8080"

java -XX:+UseSerialGC -Xmx1024m -jar $jarPath

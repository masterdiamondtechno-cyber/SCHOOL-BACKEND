@echo off
echo ========================================================
echo       Starting School Management Microservices
echo ========================================================

echo Cleaning up any previously running services on ports 8761, 8080, 8081, 8082, 8083...
powershell -ExecutionPolicy Bypass -File .\cleanup-ports.ps1
echo Ports check complete!
echo.

echo 1. Starting Eureka Server (Port 8761)...
start "Eureka Server [8761]" cmd /k "cd eureka-server && ..\mvnw.cmd spring-boot:run"

echo Waiting 12 seconds for Eureka Server to register...
powershell -NoProfile -Command "Start-Sleep -Seconds 12"

echo 2. Starting Auth Service (Port 8081)...
start "Auth Service [8081]" cmd /k "cd auth-service && ..\mvnw.cmd spring-boot:run"

echo 3. Starting School Service (Port 8083)...
start "School Service [8083]" cmd /k "cd school-service && ..\mvnw.cmd spring-boot:run"

echo 4. Starting Subscription Service (Port 8082)...
start "Subscription Service [8082]" cmd /k "cd subscription-service && ..\mvnw.cmd spring-boot:run"

echo Waiting 10 seconds before starting API Gateway...
powershell -NoProfile -Command "Start-Sleep -Seconds 10"

echo 5. Starting API Gateway (Port 8080)...
start "API Gateway [8080]" cmd /k "cd api-gateway && ..\mvnw.cmd spring-boot:run"

echo ========================================================
echo  All services have been launched in separate windows!
echo  Check Eureka dashboard at: http://localhost:8761
echo ========================================================
pause

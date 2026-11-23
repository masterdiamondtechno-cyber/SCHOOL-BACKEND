@echo off
echo ========================================================
echo       Stopping All School Backend Microservices
echo ========================================================

powershell -ExecutionPolicy Bypass -File .\cleanup-ports.ps1

echo All microservices stopped successfully!
pause

@echo off
setlocal

set "PROJECT_ROOT=%~dp0"
set "BACKEND_DIR=%PROJECT_ROOT%backend"
set "FRONTEND_DIR=%PROJECT_ROOT%frontend"

if not exist "%BACKEND_DIR%\pom.xml" (
    echo [ERROR] Backend project not found: "%BACKEND_DIR%"
    pause
    exit /b 1
)

if not exist "%FRONTEND_DIR%\package.json" (
    echo [ERROR] Frontend project not found: "%FRONTEND_DIR%"
    pause
    exit /b 1
)

where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven was not found in PATH.
    echo Install Maven or add it to PATH, then run this script again.
    pause
    exit /b 1
)

where npm.cmd >nul 2>&1
if errorlevel 1 (
    echo [ERROR] npm was not found in PATH.
    echo Install Node.js or add it to PATH, then run this script again.
    pause
    exit /b 1
)

start "Hospital Backend - 8080" cmd /k "cd /d "%BACKEND_DIR%" && mvn spring-boot:run"
start "Hospital Frontend - 3000" cmd /k "cd /d "%FRONTEND_DIR%" && npm.cmd run dev"

echo Backend:  http://localhost:8080
echo Frontend: http://localhost:3000
echo Two terminal windows have been opened. Close those windows to stop the services.

endlocal

@echo off
setlocal

cd /d "%~dp0"
set "PROJECT_ROOT=%CD%"

if not exist "%PROJECT_ROOT%\backend\pom.xml" (
    echo [ERROR] backend\pom.xml was not found.
    pause
    exit /b 1
)

if not exist "%PROJECT_ROOT%\frontend\package.json" (
    echo [ERROR] frontend\package.json was not found.
    pause
    exit /b 1
)

where java.exe >nul 2>&1 || (
    echo [ERROR] Java was not found in PATH.
    pause
    exit /b 1
)

where mvn.cmd >nul 2>&1 || (
    echo [ERROR] Maven was not found in PATH.
    pause
    exit /b 1
)

where node.exe >nul 2>&1 || (
    echo [ERROR] Node.js was not found in PATH.
    pause
    exit /b 1
)

where npm.cmd >nul 2>&1 || (
    echo [ERROR] npm was not found in PATH.
    pause
    exit /b 1
)

echo Starting backend at http://127.0.0.1:8080 ...
start "Hospital Backend" /D "%PROJECT_ROOT%\backend" cmd.exe /k "mvn.cmd spring-boot:run"

echo Starting frontend at http://127.0.0.1:3000 ...
start "Hospital Frontend" /D "%PROJECT_ROOT%\frontend" cmd.exe /k "if not exist node_modules npm.cmd install && npm.cmd run dev -- --host 127.0.0.1 --port 3000"

echo.
echo Frontend: http://127.0.0.1:3000
echo Backend : http://127.0.0.1:8080
echo Close the two service windows to stop the project.
exit /b 0

@echo off
chcp 65001 >nul
title 医院预约挂号系统 - 一键启动

echo.
echo  ╔══════════════════════════════════════╗
echo  ║   医院在线预约挂号系统              ║
echo  ║   后端: Spring Boot 3.3  :8080/api   ║
echo  ║   前端: Vue 3 + Vite    :3000       ║
echo  ╚══════════════════════════════════════╝
echo.

REM 检查 MySQL
echo [1/3] 检查 MySQL...
sc query MySQL | find "RUNNING" >nul
if %errorlevel% neq 0 (
    echo   MySQL 未运行，正在启动...
    net start MySQL
    if %errorlevel% neq 0 (
        echo   [错误] 无法启动 MySQL，请手动启动
        pause
        exit /b 1
    )
)
echo   MySQL 运行正常
echo.

REM 启动后端（新窗口）
echo [2/3] 启动后端 (端口 8080)...
start "hospital-backend" cmd /c "cd /d C:\Users\AA\Desktop\dev\prj\backend && mvn spring-boot:run"
echo   等待后端启动...
timeout /t 10 /nobreak >nul

REM 启动前端（新窗口）
echo [3/3] 启动前端 (端口 3000)...
start "hospital-frontend" cmd /c "cd /d C:\Users\AA\Desktop\dev\prj\frontend && npx vite"
echo   等待前端启动...
timeout /t 5 /nobreak >nul

REM 打开浏览器
echo.
echo ═══════════════════════════════════════
echo   服务已启动，正在打开浏览器...
echo ═══════════════════════════════════════
start http://localhost:3000

echo.
echo   后端接口文档: http://localhost:8080/api/doc.html
echo   关闭本窗口不会影响服务运行
echo.
pause

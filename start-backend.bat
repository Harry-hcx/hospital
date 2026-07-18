@echo off
chcp 65001 >nul
title 医院预约系统 - 后端

echo ========================================
echo   启动后端 Spring Boot (端口 8080)
echo ========================================
echo.
cd /d "C:\Users\AA\Desktop\dev\prj\backend"
call mvn spring-boot:run
pause

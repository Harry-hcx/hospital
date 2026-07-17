@echo off
chcp 65001 >nul
title 医院预约系统 - 前端

echo ========================================
echo   启动前端 Vue 3 (端口 3000)
echo ========================================
echo.
cd /d "C:\Users\AA\Desktop\dev\prj\frontend"
call npx vite
pause

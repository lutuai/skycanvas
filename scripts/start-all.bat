@echo off
chcp 65001
echo ====================================
echo SkyCanvas 一键启动脚本 (Windows)
echo ====================================
echo.
echo 此脚本将同时启动前端和后端服务
echo.
echo ⚠️  注意：
echo   1. 请确保已初始化数据库 (运行 init-database.bat)
echo   2. 请确保MySQL和Redis服务已启动
echo   3. 两个终端窗口将被打开
echo.
pause

echo.
echo 启动后端服务...
start "SkyCanvas Backend" cmd /k "cd /d %~dp0 && start-backend.bat"

echo 等待3秒...
timeout /t 3 /nobreak >nul

echo 启动前端服务...
start "SkyCanvas Frontend" cmd /k "cd /d %~dp0 && start-frontend.bat"

echo.
echo ✅ 启动完成！
echo.
echo 后端服务将在独立窗口运行
echo 前端服务将在独立窗口运行
echo.
echo 访问地址: http://localhost:3000
echo.

pause


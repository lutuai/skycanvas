@echo off
chcp 65001 >nul 2>&1

REM SkyCanvas 快捷启动脚本 (Windows)
REM 详细脚本请查看 scripts 目录

REM 切换到脚本所在目录（项目根目录）
cd /d "%~dp0"

echo.
echo ====================================
echo   SkyCanvas 项目启动菜单
echo ====================================
echo.
echo 请选择操作：
echo.
echo   1. 一键启动（前端+后端）
echo   2. 仅启动后端
echo   3. 仅启动前端
echo   4. 初始化数据库
echo   5. 退出
echo.
set /p choice=请输入选择 (1-5): 

if "%choice%"=="1" (
    call scripts\start-all.bat
) else if "%choice%"=="2" (
    call scripts\start-backend.bat
) else if "%choice%"=="3" (
    call scripts\start-frontend.bat
) else if "%choice%"=="4" (
    call scripts\init-database.bat
) else if "%choice%"=="5" (
    exit /b 0
) else (
    echo 无效选择！
    pause
    exit /b 1
)


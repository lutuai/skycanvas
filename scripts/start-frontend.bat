@echo off
chcp 65001
echo ====================================
echo SkyCanvas 前端启动脚本 (Windows)
echo ====================================
echo.

echo [1/3] 检查环境...
echo.

REM 检查Node.js
node -v >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js未安装或未配置环境变量
    echo 请安装Node.js 14或更高版本
    pause
    exit /b 1
)
echo ✅ Node.js环境检测成功

echo.
echo [2/3] 安装依赖...
cd ..\frontend
if not exist "node_modules" (
    echo 正在安装依赖，请稍候...
    call npm install
    if errorlevel 1 (
        echo ❌ 依赖安装失败
        echo 尝试使用国内镜像...
        call npm install --registry=https://registry.npmmirror.com
    )
)

echo.
echo [3/3] 选择启动方式...
echo.
echo 请选择启动方式：
echo   1. H5 (推荐，浏览器访问)
echo   2. 微信小程序 (需要微信开发者工具)
echo.
set /p choice=请输入选择 (1 或 2): 

if "%choice%"=="1" (
    echo.
    echo 启动H5版本...
    echo 启动后访问: http://localhost:3000
    echo.
    call npm run dev:h5
) else if "%choice%"=="2" (
    echo.
    echo 启动微信小程序版本...
    echo 编译完成后，请使用微信开发者工具导入:
    echo 目录: %cd%\dist\dev\mp-weixin
    echo.
    call npm run dev:mp-weixin
    pause
) else (
    echo 无效选择，默认启动H5...
    call npm run dev:h5
)

pause

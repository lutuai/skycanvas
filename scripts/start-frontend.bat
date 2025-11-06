@echo off
chcp 65001 >nul 2>&1
echo ====================================
echo SkyCanvas 前端启动脚本 (Windows)
echo ====================================
echo.

REM 切换到脚本所在目录
cd /d "%~dp0"

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

REM 切换到frontend目录
cd /d "%~dp0\..\frontend"
if not exist "package.json" (
    echo ❌ 错误：未找到frontend目录或package.json文件
    echo 当前目录: %CD%
    pause
    exit /b 1
)

echo 前端目录: %CD%
echo.

if not exist "node_modules" (
    echo 正在安装依赖，请稍候...
    call npm install
    if errorlevel 1 (
        echo ❌ 依赖安装失败
        echo 尝试使用国内镜像...
        call npm install --registry=https://registry.npmmirror.com
    )
) else (
    echo ✅ 依赖已安装
)

echo.
echo [3/3] 选择启动方式...
echo.
echo 请选择启动方式：
echo   1. H5 (推荐，浏览器访问)
echo   2. 微信小程序 (需要微信开发者工具)
echo   3. 同时启动 H5 和小程序 (推荐开发调试)
echo.
set /p choice=请输入选择 (1, 2 或 3): 

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
) else if "%choice%"=="3" (
    echo.
    echo ====================================
    echo 同时启动 H5 和小程序版本
    echo ====================================
    echo.
    echo [H5] 将在新窗口启动，访问地址: http://localhost:3000
    echo [小程序] 将在当前窗口编译，编译完成后用微信开发者工具打开
    echo 目录: %cd%\dist\dev\mp-weixin
    echo.
    echo 提示: 关闭此窗口不会关闭 H5 服务，请手动关闭 H5 窗口
    echo.
    pause
    
    REM 在新窗口启动 H5
    start "SkyCanvas H5 服务" cmd /k "echo 正在启动 H5 服务... && echo 访问地址: http://localhost:3000 && echo. && npm run dev:h5"
    
    REM 等待1秒让H5先启动
    timeout /t 1 /nobreak >nul
    
    REM 在当前窗口启动小程序编译
    echo.
    echo 正在编译小程序版本...
    echo.
    call npm run dev:mp-weixin
    
) else (
    echo 无效选择，默认启动H5...
    call npm run dev:h5
)

pause

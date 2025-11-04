@echo off
chcp 65001 >nul 2>&1
echo ====================================
echo SkyCanvas 后端启动脚本 (Windows)
echo ====================================
echo.

REM 切换到脚本所在目录
cd /d "%~dp0"
echo 当前目录: %CD%
echo.

echo [1/4] 检查环境...
echo.

REM 检查Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java未安装或未配置环境变量
    echo 请安装JDK 17或更高版本
    pause
    exit /b 1
)
echo ✅ Java环境检测成功
echo.

echo 正在检查Maven...
REM 检查Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven未安装或未配置环境变量
    echo 请安装Maven 3.6或更高版本
    pause
    exit /b 1
)
echo ✅ Maven环境检测成功

echo.
echo [2/4] 检查数据库配置...
echo ⚠️  请确保已经：
echo   1. 启动了MySQL服务
echo   2. 导入了数据库: mysql -u root -p ^< database/schema.sql
echo   3. 启动了Redis服务
echo.
pause

echo.
echo [3/4] 检查配置文件...
if not exist "..\backend\src\main\resources\application-dev.yml" (
    echo ⚠️  未找到application-dev.yml配置文件
    echo 正在复制示例配置...
    copy "..\backend\src\main\resources\application-dev.yml.example" "..\backend\src\main\resources\application-dev.yml"
    echo ✅ 配置文件已创建
    echo.
    echo ⚠️  请编辑 backend\src\main\resources\application-dev.yml 填写配置
    echo   - 数据库密码
    echo   - Redis密码（如果有）
    echo.
    pause
)

echo.
echo [4/4] 启动后端服务...
echo.

REM 切换到backend目录
cd /d "%~dp0\..\backend"
if not exist "pom.xml" (
    echo ❌ 错误：未找到backend目录或pom.xml文件
    echo 当前目录: %CD%
    pause
    exit /b 1
)

echo 后端目录: %CD%
echo.
echo 正在启动Spring Boot应用...
echo 提示：首次启动可能需要下载依赖，请耐心等待...
echo.

call mvn spring-boot:run

echo.
echo 后端服务已停止
pause

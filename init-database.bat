@echo off
chcp 65001
echo ====================================
echo SkyCanvas 数据库初始化脚本
echo ====================================
echo.

echo 请确保MySQL已启动
echo.

set /p db_user=请输入MySQL用户名 (默认: root): 
if "%db_user%"=="" set db_user=root

set /p db_pass=请输入MySQL密码: 

echo.
echo 正在导入数据库...
mysql -u %db_user% -p%db_pass% < database\schema.sql

if errorlevel 1 (
    echo ❌ 数据库导入失败
    echo 请检查：
    echo   1. MySQL是否启动
    echo   2. 用户名密码是否正确
    echo   3. database\schema.sql文件是否存在
    pause
    exit /b 1
)

echo.
echo ✅ 数据库初始化成功！
echo.
echo 数据库名: skycanvas
echo 已创建表:
echo   - tb_user (用户表)
echo   - tb_credit_log (积分记录表)
echo   - tb_video_task (视频任务表)
echo   - tb_work (作品表)
echo   - tb_order (订单表)
echo   - tb_credit_package (积分套餐表)
echo   - tb_post (社区帖子表)
echo   - tb_comment (评论表)
echo   - tb_like (点赞表)
echo.

pause


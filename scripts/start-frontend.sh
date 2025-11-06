#!/bin/bash

echo "===================================="
echo "SkyCanvas 前端启动脚本 (Linux/Mac)"
echo "===================================="
echo

echo "[1/3] 检查环境..."
echo

# 检查Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Node.js未安装或未配置环境变量"
    echo "请安装Node.js 14或更高版本"
    exit 1
fi
echo "✅ Node.js环境检测成功"

echo
echo "[2/3] 安装依赖..."
cd ../frontend
if [ ! -d "node_modules" ]; then
    echo "正在安装依赖，请稍候..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ 依赖安装失败"
        echo "尝试使用国内镜像..."
        npm install --registry=https://registry.npmmirror.com
    fi
fi

echo
echo "[3/3] 选择启动方式..."
echo
echo "请选择启动方式："
echo "  1. H5 (推荐，浏览器访问)"
echo "  2. 微信小程序 (需要微信开发者工具)"
echo "  3. 同时启动 H5 和小程序 (推荐开发调试)"
echo
read -p "请输入选择 (1, 2 或 3): " choice

if [ "$choice" = "1" ]; then
    echo
    echo "启动H5版本..."
    echo "启动后访问: http://localhost:3000"
    echo
    npm run dev:h5
elif [ "$choice" = "2" ]; then
    echo
    echo "启动微信小程序版本..."
    echo "编译完成后，请使用微信开发者工具导入:"
    echo "目录: $(pwd)/dist/dev/mp-weixin"
    echo
    npm run dev:mp-weixin
elif [ "$choice" = "3" ]; then
    echo
    echo "===================================="
    echo "同时启动 H5 和小程序版本"
    echo "===================================="
    echo
    echo "[H5] 后台启动中，访问地址: http://localhost:3000"
    echo "[小程序] 将在前台编译，编译完成后用微信开发者工具打开"
    echo "目录: $(pwd)/dist/dev/mp-weixin"
    echo
    
    # 创建日志目录
    mkdir -p logs
    
    # 后台启动 H5
    echo "正在后台启动 H5 服务..."
    nohup npm run dev:h5 > logs/h5.log 2>&1 &
    H5_PID=$!
    echo "✅ H5 服务已启动 (PID: $H5_PID)"
    echo "日志文件: $(pwd)/logs/h5.log"
    
    # 等待1秒让H5先启动
    sleep 1
    
    # 前台启动小程序编译
    echo
    echo "正在编译小程序版本..."
    echo "提示: 按 Ctrl+C 停止小程序编译，H5 服务会继续运行"
    echo "停止 H5 服务命令: kill $H5_PID"
    echo
    
    # 保存 PID 到文件
    echo $H5_PID > logs/h5.pid
    
    npm run dev:mp-weixin
    
else
    echo "无效选择，默认启动H5..."
    npm run dev:h5
fi


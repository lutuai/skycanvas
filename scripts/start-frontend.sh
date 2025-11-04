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
echo
read -p "请输入选择 (1 或 2): " choice

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
else
    echo "无效选择，默认启动H5..."
    npm run dev:h5
fi


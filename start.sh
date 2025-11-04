#!/bin/bash
# SkyCanvas 快捷启动脚本 (Linux/Mac)
# 详细脚本请查看 scripts 目录

echo
echo "===================================="
echo "  SkyCanvas 项目启动菜单"
echo "===================================="
echo
echo "请选择操作："
echo
echo "  1. 启动后端"
echo "  2. 启动前端"
echo "  3. 初始化数据库"
echo "  4. 退出"
echo
read -p "请输入选择 (1-4): " choice

case $choice in
    1)
        ./scripts/start-backend.sh
        ;;
    2)
        ./scripts/start-frontend.sh
        ;;
    3)
        ./scripts/init-database.sh
        ;;
    4)
        exit 0
        ;;
    *)
        echo "无效选择！"
        exit 1
        ;;
esac


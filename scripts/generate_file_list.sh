#!/bin/bash

# 输出文件名
OUTPUT_FILE="file_list.txt"

# 定义需要忽略的目录 (正则格式)
# 包含: .git, .idea(IDEA配置), target(Java构建), node_modules, dist, __pycache__(Python), venv
IGNORE_PATTERN="\.git/|\.idea/|\.vscode/|target/|node_modules/|dist/|__pycache__/|\.DS_Store|venv/|\.mvn/"

echo "正在生成文件列表..."

# 检查系统是否有 tree 命令 (tree 生成的结构 AI 最容易看懂)
if command -v tree &> /dev/null; then
    # 使用 tree 命令，-I 忽略指定模式，--dirsfirst 目录在前
    # 注意：这里 -I 的格式和 grep 不太一样，用 | 分割
    tree -a -I ".git|.idea|.vscode|target|node_modules|dist|__pycache__|venv|.mvn|.DS_Store" --dirsfirst > "$OUTPUT_FILE"
    echo "✅ 已使用 'tree' 命令生成结构树。"
else
    # 如果没有 tree，使用 find + grep 过滤
    # 查找当前目录(.)，排除忽略的路径
    find . -type f | grep -v -E "$IGNORE_PATTERN" | sed 's|^./||' | sort > "$OUTPUT_FILE"
    echo "⚠️ 未检测到 'tree' 命令，已使用 'find' 生成平铺列表。"
    echo "💡 建议安装 tree (Mac: brew install tree / Ubuntu: apt install tree) 以获得更好的可读性。"
fi

echo "📄 文件列表已保存至: $OUTPUT_FILE"
echo "------------------------------------------------"
# 打印前20行预览
head -n 20 "$OUTPUT_FILE"
echo "..."
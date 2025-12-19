#!/bin/bash

# Lattice Python Engine 本地启动脚本 (增强版 - 健壮性修复)

ENV_NAME="lattice-env"
PORT=8000
PIP_MIRROR="https://pypi.tuna.tsinghua.edu.cn/simple"

echo "🚀 正在检查环境并启动 Lattice Python Engine..."

# 1. 定位 Conda
if [ -z "$CONDA_EXE" ]; then
    CONDA_EXE=$(command -v conda)
fi
if [ -z "$CONDA_EXE" ]; then
    echo "❌ 错误: 未检测到 conda 命令。"
    exit 1
fi
CONDA_BASE=$($CONDA_EXE info --base)
source "$CONDA_BASE/etc/profile.d/conda.sh"

# 2. 检查环境
ENV_PATH=$($CONDA_EXE env list | grep -E "^$ENV_NAME\s" | awk '{print $2}')

if [ -z "$ENV_PATH" ]; then
    echo "📦 环境 '$ENV_NAME' 未找到，正在创建..."
    grep -v "prefix:" environment.yml > temp_env.yml
    $CONDA_EXE env create -f temp_env.yml
    rm temp_env.yml
    ENV_PATH=$($CONDA_EXE env list | grep -E "^$ENV_NAME\s" | awk '{print $2}')
else
    echo "✅ 环境 '$ENV_NAME' 已存在于 $ENV_PATH"
fi

# 3. 激活并强制检查依赖
conda activate "$ENV_NAME"

# 验证 uvicorn 是否真的在当前环境中
if ! python -c "import uvicorn" &> /dev/null; then
    echo "⚠️ 环境依赖不完整，正在从 requirements.txt 补充安装..."
    python -m pip install -i $PIP_MIRROR -r requirements.txt
fi

# 4. 运行服务
echo "🔥 启动 Uvicorn (端口: $PORT)..."
export APP_NAME="Lattice-Python-Engine-Local"
export DEBUG="true"
export PYTHONPATH=$PYTHONPATH:$(pwd)

# 显式使用环境中的 python
python -m uvicorn app.main:app --host 0.0.0.0 --port $PORT --reload
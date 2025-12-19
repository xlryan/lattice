#!/bin/bash

# 探测 Python 引擎连通性脚本

PYTHON_URL="http://localhost:8000"
TEST_FILE="test_ingest.txt"

echo "Testing connection to Python Engine at $PYTHON_URL..."

# 1. 检查健康状态
HEALTH=$(curl -s -o /dev/null -w "%{http_code}" $PYTHON_URL/health)
if [ "$HEALTH" == "200" ]; then
    echo "✅ Python Engine is alive!"
else
    echo "❌ Python Engine is not reachable (HTTP $HEALTH). Make sure it's running on port 8000."
    exit 1
fi

# 2. 模拟 Ingestion 调用
echo "Testing /engine/analyze endpoint..."
echo "Hello Lattice, this is a test file for Python Engine." > $TEST_FILE

RESPONSE=$(curl -s -X POST -F "file=@$TEST_FILE" $PYTHON_URL/engine/analyze)

if [[ $RESPONSE == *"content"* ]]; then
    echo "✅ Analysis successful! Response received:"
    echo $RESPONSE | python3 -m json.tool 2>/dev/null || echo $RESPONSE
else
    echo "❌ Analysis failed or returned unexpected response."
    echo "Debug Response: $RESPONSE"
fi

rm $TEST_FILE

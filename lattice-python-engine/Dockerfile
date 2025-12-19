FROM python:3.11-slim

WORKDIR /app

# 使用阿里云 Debian 镜像源加速 apt
# 注意：Debian Bookworm (Python 3.11-slim) 使用 debian.sources，这里逻辑没问题
RUN sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list.d/debian.sources 2>/dev/null || \
    sed -i 's/deb.debian.org/mirrors.aliyun.com/g' /etc/apt/sources.list

# 安装必要的系统依赖
# 新增：libgomp1 (Paddle 必须)
# 修改：libgl1-mesa-glx (OpenCV 更稳妥的依赖)
# 移除：tesseract-ocr (除非你非常确定需要它，否则建议移除以减小体积)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    libgl1-mesa-glx \
    libglib2.0-0 \
    libsm6 \
    libxrender1 \
    libxext6 \
    libgomp1 \
    poppler-utils \
    && rm -rf /var/lib/apt/lists/*

# 预安装基础依赖以利用缓存
COPY requirements.txt .

# 增加 --default-timeout 防止网络波动导致大包(如 paddle)下载失败
RUN pip install --no-cache-dir --default-timeout=100 -i https://pypi.tuna.tsinghua.edu.cn/simple -r requirements.txt

# 复制源码
COPY . .

# 设置环境变量
ENV PYTHONPATH=/app
ENV PYTHONUNBUFFERED=1

EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
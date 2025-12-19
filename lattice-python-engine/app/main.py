import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI, UploadFile, File
from app.config import settings
from app.core.model_loader import model_engine
from app.core.processor import DocumentProcessor
from app.schemas.response import AnalysisResult

# 配置日志格式
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger("lattice.api")

# 生命周期管理 (Lifespan)
@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动前：加载模型
    logger.info("🚀 System startup: Loading models...")
    model_engine.load_models()
    yield
    # 关闭后：清理资源 (如有)
    logger.info("🛑 System shutdown.")

app = FastAPI(
    title=settings.APP_NAME,
    lifespan=lifespan
)

@app.get("/health")
async def health_check():
    """健康检查接口 (Docker/K8s 使用)"""
    return {"status": "healthy", "config": settings.APP_NAME}

@app.post("/engine/analyze", response_model=AnalysisResult)
async def analyze_document(file: UploadFile = File(...)):
    """
    核心接口：接收文件 -> 解析 -> AI分析 -> 返回JSON
    """
    logger.info(f"Receive upload request: {file.filename}")
    result = await DocumentProcessor.process(file)
    return result

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=settings.DEBUG)
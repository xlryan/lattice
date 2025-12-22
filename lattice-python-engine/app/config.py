import os
from pydantic_settings import BaseSettings
from pathlib import Path



class Settings(BaseSettings):
    # 服务基础配置
    APP_NAME: str = "Lattice AI Engine"
    API_PREFIX: str = "/api/v1"
    DEBUG: bool = False

    # 视觉模型配置
    MODELS_DIR: str = str(Path(__file__).parent.parent / 'models')
    DATA_DIR: str = str(Path(__file__).parent.parent / 'data')

    # 模型配置
    NLP_MODEL_NAME: str = "paraphrase-multilingual-MiniLM-L12-v2"
    USE_OCR: bool = True  # 是否启用 OCR (耗资源，可开关)

    # 路径配置
    TEMP_DIR: str = "/tmp/lattice_upload"

    class Config:
        env_file = ".env"


settings = Settings()

# 确保目录存在
os.makedirs(settings.TEMP_DIR, exist_ok=True)
os.makedirs(settings.MODELS_DIR, exist_ok=True)

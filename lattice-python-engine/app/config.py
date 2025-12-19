import os
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # 服务基础配置
    APP_NAME: str = "Lattice AI Engine"
    API_PREFIX: str = "/api/v1"
    DEBUG: bool = False

    # 模型配置
    NLP_MODEL_NAME: str = "paraphrase-multilingual-MiniLM-L12-v2"
    USE_OCR: bool = True  # 是否启用 OCR (耗资源，可开关)

    # 路径配置
    TEMP_DIR: str = "/tmp/lattice_upload"

    class Config:
        env_file = ".env"


settings = Settings()

# 确保临时目录存在
os.makedirs(settings.TEMP_DIR, exist_ok=True)
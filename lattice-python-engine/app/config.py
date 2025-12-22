import os
from typing import List

from pydantic_settings import BaseSettings
from pathlib import Path



class Settings(BaseSettings):
    # 服务基础配置
    APP_NAME: str = "Lattice AI Engine"
    API_PREFIX: str = "/api/v1"
    DEBUG: bool = False

    # --- 模型默认配置 ---
    # 视觉模型默认值
    DEFAULT_VISION_MODEL: str = "ViT-B-32"
    DEFAULT_VISION_PRETRAINED: str = "laion2b_s34b_b79k"
    VISION_BATCH_SIZE: int = 128  # 预计算特征时的批大小

    # NLP 模型配置
    NLP_MODEL_NAME: str = "paraphrase-multilingual-MiniLM-L12-v2"
    NLP_BATCH_SIZE: int = 32  # 文本向量化时的批大小

    # OCR 配置
    OCR_LANGUAGES: List[str] = ['ch_sim', 'en']
    USE_OCR: bool = True

    # 视觉置信度阀值
    VISION_CONFIDENCE_THRESHOLD: float = 0.1

    # 关键词黑名单（支持从环境变量以逗号分隔读取）
    KEYWORD_BLACKLIST: set = {
        'background', 'wall', 'floor', 'ceiling', 'curtain', 'plastic bag',
        'fabric', 'textile', 'art', 'pattern', 'design'
    }

    # 文本关键词过滤规则
    TEXT_KW_MIN_LEN: int = 2
    TEXT_KW_MAX_LEN: int = 20
    TEXT_KW_EXCLUDE_DIGITS: bool = True

    # 默认使用的 Whisper 模型标识符 (tiny, base, small, medium, large)
    DEFAULT_AUDIO_MODEL: str = "base"

    # 是否在系统启动时启用音频模块预加载
    USE_AUDIO: bool = True

    # 语音转录是否使用半精度推理（建议 GPU 开启，CPU 关闭）
    AUDIO_FP16: bool = True

    # Path configuration
    TEMP_DIR: str = "/tmp/lattice_upload"
    MODELS_DIR: str = os.getenv("MODELS_DIR", str(Path(__file__).parent.parent.parent / "models"))

    class Config:
        env_file = ".env"
        env_nested_delimiter = "__"


settings = Settings()

# 确保目录存在
os.makedirs(settings.TEMP_DIR, exist_ok=True)
os.makedirs(settings.MODELS_DIR, exist_ok=True)

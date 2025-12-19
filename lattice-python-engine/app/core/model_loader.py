import logging
from sentence_transformers import SentenceTransformer
from paddleocr import PaddleOCR
from app.config import settings
import easyocr

logger = logging.getLogger("lattice.core")


class ModelManager:
    _instance = None
    nlp_model = None
    ocr_model = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(ModelManager, cls).__new__(cls)
        return cls._instance

    def load_models(self):
        """应用启动时调用，预加载所有模型"""
        logger.info("⚡ 正在初始化 AI 模型引擎...")

        # 1. 加载 NLP 模型
        if not self.nlp_model:
            logger.info(f"Loading NLP Model: {settings.NLP_MODEL_NAME}...")
            self.nlp_model = SentenceTransformer(settings.NLP_MODEL_NAME)
            logger.info("✅ NLP Model loaded.")

        # 2. 加载 OCR 模型 (可选)
        if settings.USE_OCR and not self.ocr_model:
            logger.info("Loading EasyOCR Reader (ch_sim, en)...")
            self.ocr_model = easyocr.Reader(['ch_sim', 'en'], gpu=True)
            logger.info("✅ EasyOCR Engine loaded.")

    def get_nlp_model(self):
        if not self.nlp_model:
            self.load_models()
        return self.nlp_model

    def get_ocr_model(self):
        if settings.USE_OCR and not self.ocr_model:
            self.load_models()
        return self.ocr_model


# 全局单例
model_engine = ModelManager()
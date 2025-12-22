import whisper
import torch
from logging import getLogger

logger = getLogger(__name__)


class AudioModel:
    def __init__(self, model_name: str = "base", download_root: str = None):
        self.model_name = model_name
        # 确保模型从你指定的 models/audio 目录加载
        self.download_root = download_root
        self.device = "cuda" if torch.cuda.is_available() else "cpu"
        self.model = None
        self.load_model()

    def load_model(self):
        logger.info(f"Loading audio model '{self.model_name}' from {self.download_root} on {self.device}")
        try:
            # 使用 download_root 指定本地缓存目录
            self.model = whisper.load_model(
                self.model_name,
                device=self.device,
                download_root=self.download_root
            )
            logger.info(f"Audio model '{self.model_name}' loaded successfully.")
        except Exception as e:
            logger.error(f"Failed to load audio model: {e}")
            raise

    def transcribe(self, audio_path: str) -> str:
        if not self.model:
            raise RuntimeError("Audio model is not loaded.")

        try:
            # 自动处理 FP16 推理
            result = self.model.transcribe(audio_path, fp16=(self.device == "cuda"))
            return result.get("text", "").strip()
        except Exception as e:
            logger.error(f"Transcription error: {e}")
            return ""

    def reload_model(self, model_name: str):
        """实现热加载，释放旧模型显存"""
        logger.info(f"Hot-reloading audio model to: {model_name}")
        self.model_name = model_name

        # 显式清理显存防止 OOM
        if self.model is not None:
            del self.model
            if torch.cuda.is_available():
                torch.cuda.empty_cache()

        self.load_model()

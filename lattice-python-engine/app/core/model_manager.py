import os
import logging
import threading
from typing import Optional, Dict, List
from sentence_transformers import SentenceTransformer
import easyocr
import torch
import open_clip
from pathlib import Path
from datetime import datetime
from .vision_model import ProjectVisionModel
from ..config import settings

logger = logging.getLogger("lattice.model_manager")

class ModelManager:
    """
    全能模型管理器：支持按分类进行模型发现、动态切换和状态监控。
    """

    def __init__(self):
        self._lock = threading.Lock()
        self._active_models: Dict[str, any] = {"vision": None, "nlp": None, "ocr": None}
        self._active_configs: Dict[str, dict] = {"vision": {}, "nlp": {}, "ocr": {}}

        self._category_paths = {
            "vision": Path(settings.MODELS_DIR) / "vision",
            "nlp": Path(settings.MODELS_DIR) / "nlp",
            "ocr": Path(settings.MODELS_DIR) / "ocr"
        }
        for path in self._category_paths.values():
            path.mkdir(parents=True, exist_ok=True)

    def get_available_models(self) -> Dict[str, List[str]]:
        """扫描所有分类目录下的可用模型"""
        available = {}
        for category, path in self._category_paths.items():
            if path.is_dir():
                available[category] = [p.name for p in path.iterdir() if not p.name.startswith('.')]
        return available

    def get_vision_model(self) -> Optional[ProjectVisionModel]:
        with self._lock:
            if self._active_models["vision"] is None:
                self._load_default_model("vision")
            return self._active_models["vision"]

    def get_nlp_model(self) -> Optional[SentenceTransformer]:
        with self._lock:
            if self._active_models["nlp"] is None:
                self._load_default_model("nlp")
            return self._active_models["nlp"]

    def get_ocr_model(self) -> Optional[easyocr.Reader]:
        if not settings.USE_OCR: return None
        with self._lock:
            if self._active_models["ocr"] is None:
                self._load_default_model("ocr")
            return self._active_models["ocr"]

    def _download_default_model(self, category: str) -> bool:
        """
        自动下载默认模型到指定目录
        """
        logger.info(f"⚡ Downloading default model for category: {category}...")
        save_dir = self._category_paths[category]

        try:
            if category == "vision":
                # 下载 ViT-B-32
                logger.info("Downloading OpenCLIP ViT-B-32...")
                open_clip.create_model_and_transforms(
                    'ViT-B-32',
                    pretrained='laion2b_s34b_b79k',
                    cache_dir=str(save_dir)
                )

            elif category == "nlp":
                # 下载 SentenceTransformer
                model_name = 'paraphrase-multilingual-MiniLM-L12-v2'
                logger.info(f"Downloading NLP model {model_name}...")
                model = SentenceTransformer(model_name)
                # 确保保存到指定的 nlp 目录下
                model.save(str(save_dir / model_name))

            elif category == "ocr":
                # 下载 EasyOCR
                logger.info("Downloading EasyOCR models...")
                easyocr.Reader(
                    ['ch_sim', 'en'],
                    gpu=torch.cuda.is_available(),
                    model_storage_directory=str(save_dir)
                )

            logger.info(f"✅ Successfully downloaded default {category} model.")
            return True

        except Exception as e:
            logger.error(f"❌ Failed to download default {category} model: {e}", exc_info=True)
            return False

    def _load_default_model(self, category: str):
        """加载一个分类下的默认（第一个）模型，如果不存在则尝试下载"""
        logger.info(f"Attempting to load default model for category: {category}")

        # 1. 获取现有模型
        available = self.get_available_models().get(category)

        # 2. 如果没有模型，触发自动下载
        if not available:
            logger.warning(f"No models found locally for '{category}'. triggering auto-download...")
            success = self._download_default_model(category)
            if success:
                # 下载完成后重新扫描
                available = self.get_available_models().get(category)

        if not available:
            logger.error(f"❌ Still no models available for category '{category}' after download attempt.")
            return

        # 3. 加载（通常加载列表中的第一个作为默认）
        # 对于 OCR，available 可能是具体的模型文件，但 EasyOCR 初始化只需要目录，
        # 所以这里的 default_model_name 对 OCR 来说可能只是为了通过 _switch_logic 的检查
        default_model_name = available[0]

        # 特殊处理：如果是 OCR，EasyOCR 需要的是目录，而不是具体文件切换
        # 但为了保持逻辑统一，我们还是传递文件名，_switch_logic 内部对 OCR 做了特殊处理
        try:
            self._switch_logic(category, default_model_name)
        except Exception as e:
            logger.error(f"Failed to load default model for {category}: {e}", exc_info=True)

    def switch_model(self, category: str, model_name: str) -> dict:
        """API 入口：切换指定分类的模型"""
        try:
            with self._lock:
                self._switch_logic(category, model_name)
            return {"status": "success", "message": f"{category.capitalize()} model switched to {model_name}"}
        except FileNotFoundError as e:
            logger.warning(f"Model switch failed: {e}")
            return {"status": "error", "message": str(e)}
        except Exception as e:
            logger.error(f"Model switch failed with an unexpected error: {e}", exc_info=True)
            return {"status": "error", "message": f"An unexpected error occurred: {e}"}

    def _switch_logic(self, category: str, model_name: str):
        """内部切换实现"""
        full_path = self._category_paths[category] / model_name
        actual_model_path = str(full_path)
        if full_path.is_dir():
            logger.info(f"Directory detected, searching recursively: {full_path}")
            # 使用 rglob 进行递归搜索 (* 代表当前目录，** 代表所有子目录)
            # 这样才能找到 snapshots 文件夹里的权重文件
            weight_files = list(full_path.rglob("*.bin")) + \
                           list(full_path.rglob("*.safetensors")) + \
                           list(full_path.rglob("*.pt"))

            if weight_files:
                # 排序一下，优先选最大的文件（通常是权重文件）
                weight_files.sort(key=lambda x: x.stat().st_size, reverse=True)
                actual_model_path = str(weight_files[0])
                logger.info(f"✅ Found weight file at: {actual_model_path}")
            else:
                raise FileNotFoundError(f"❌ No weight files found in any subfolder of {full_path}")

        if category == "vision":
            if self._active_models["vision"] is None:
                # 确保传递的是搜寻到的具体文件路径 actual_model_path
                self._active_models["vision"] = ProjectVisionModel(
                    model_name='ViT-B-32',
                    model_path=actual_model_path
                )
            else:
                self._active_models["vision"].reload_model(actual_model_path)

        elif category == "nlp":
            self._active_models["nlp"] = SentenceTransformer(str(full_path))

        elif category == "ocr":
            # OCR 模型加载实际上是指定一个目录，EasyOCR 会在该目录下查找并使用模型文件
            self._active_models["ocr"] = easyocr.Reader(['ch_sim', 'en'], gpu=torch.cuda.is_available(), model_storage_directory=str(self._category_paths["ocr"]))

        self._active_configs[category] = {"filename": model_name, "load_time": datetime.now().isoformat()}
        logger.info(f"Switch successful for '{category}' to '{model_name}'")

    def get_current_models(self) -> Dict[str, Optional[str]]:
        """获取当前激活的模型文件名"""
        return {key: val.get("filename") for key, val in self._active_configs.items()}


model_manager = ModelManager()
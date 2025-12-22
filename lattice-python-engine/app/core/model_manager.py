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
from .audio_model import AudioModel
from ..config import settings

logger = logging.getLogger("lattice.model_manager")

class ModelManager:
    """
    All-in-one model manager: supports model discovery, dynamic switching, and status monitoring by category.
    """

    def __init__(self):
        self._lock = threading.Lock()
        self._active_models: Dict[str, any] = {"vision": None, "nlp": None, "ocr": None, "audio": None}
        self._active_configs: Dict[str, dict] = {"vision": {}, "nlp": {}, "ocr": {}, "audio": {}}

        self._category_paths = {
            "vision": Path(settings.MODELS_DIR) / "vision",
            "nlp": Path(settings.MODELS_DIR) / "nlp",
            "ocr": Path(settings.MODELS_DIR) / "ocr",
            "audio": Path(settings.MODELS_DIR) / "audio",
        }
        for path in self._category_paths.values():
            path.mkdir(parents=True, exist_ok=True)

    def get_available_models(self) -> Dict[str, List[str]]:
        """Scans all available models in the category directories"""
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

    def get_audio_model(self) -> Optional[AudioModel]:
        if not settings.USE_AUDIO:
            return None
        with self._lock:
            if self._active_models["audio"] is None:
                self._load_default_model("audio")
            return self._active_models["audio"]

    def _download_default_model(self, category: str) -> bool:
        """
        Automatically downloads the default model to the specified directory.
        """
        logger.info(f"⚡ Downloading default model for category: {category}...")
        save_dir = self._category_paths[category]

        try:
            if category == "vision":
                logger.info(f"Downloading {settings.DEFAULT_VISION_MODEL}...")
                open_clip.create_model_and_transforms(
                    settings.DEFAULT_VISION_MODEL,
                    pretrained=settings.DEFAULT_VISION_PRETRAINED,
                    cache_dir=str(save_dir)
                )
            elif category == "nlp":
                logger.info(f"Downloading NLP model {settings.NLP_MODEL_NAME}...")
                model = SentenceTransformer(settings.NLP_MODEL_NAME)
                model.save(str(save_dir / settings.NLP_MODEL_NAME))
            elif category == "ocr":
                logger.info("Downloading EasyOCR models...")
                easyocr.Reader(
                    settings.OCR_LANGUAGES,
                    gpu=torch.cuda.is_available(),
                    model_storage_directory=str(save_dir)
                )
            elif category == "audio":
                logger.info(f"Downloading Audio model {settings.DEFAULT_AUDIO_MODEL}...")
                from whisper import load_model
                load_model(settings.DEFAULT_AUDIO_MODEL, download_root=str(save_dir))

            logger.info(f"✅ Successfully downloaded default {category} model.")
            return True

        except Exception as e:
            logger.error(f"❌ Failed to download default {category} model: {e}", exc_info=True)
            return False

    def _load_default_model(self, category: str):
        """Loads the default (first) model in a category, or tries to download it if it doesn't exist."""
        logger.info(f"Attempting to load default model for category: {category}")

        # 1. Get existing models
        available = self.get_available_models().get(category)

        # 2. If there are no models, trigger auto-download
        if not available:
            logger.warning(f"No models found locally for '{category}'. triggering auto-download...")
            success = self._download_default_model(category)
            if success:
                # Rescan after download
                available = self.get_available_models().get(category)

        if not available:
            logger.error(f"❌ Still no models available for category '{category}' after download attempt.")
            return

        # 3. Load (usually the first in the list is the default)
        # For OCR, available may be specific model files, but EasyOCR initialization only needs a directory,
        # so default_model_name here may just be to pass the _switch_logic check for OCR
        default_model_name = available[0]

        # Special handling: if it's OCR, EasyOCR needs a directory, not a specific file switch
        # But to keep the logic consistent, we still pass the filename, and _switch_logic handles OCR internally
        try:
            self._switch_logic(category, default_model_name)
        except Exception as e:
            logger.error(f"Failed to load default model for {category}: {e}", exc_info=True)

    def switch_model(self, category: str, model_name: str) -> dict:
        """API entry point: switch the model for the specified category"""
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
        """Internal switch implementation"""
        full_path = self._category_paths[category] / model_name
        actual_model_path = str(full_path)
        if full_path.is_dir():
            logger.info(f"Directory detected, searching recursively: {full_path}")
            # Use rglob for recursive search (* represents the current directory, ** represents all subdirectories)
            # This is to find the weight files in the snapshots folder
            weight_files = list(full_path.rglob("*.bin")) + \
                           list(full_path.rglob("*.safetensors")) + \
                           list(full_path.rglob("*.pt"))

            if weight_files:
                # Sort them, prioritizing the largest file (usually the weight file)
                weight_files.sort(key=lambda x: x.stat().st_size, reverse=True)
                actual_model_path = str(weight_files[0])
                logger.info(f"✅ Found weight file at: {actual_model_path}")
            else:
                raise FileNotFoundError(f"❌ No weight files found in any subfolder of {full_path}")

        if category == "vision":
            if self._active_models["vision"] is None:
                # Use settings.DEFAULT_VISION_MODEL instead of hardcoding
                self._active_models["vision"] = ProjectVisionModel(
                    model_name=settings.DEFAULT_VISION_MODEL,
                    model_path=actual_model_path
                )
            else:
                self._active_models["vision"].reload_model(actual_model_path)

        elif category == "nlp":
            self._active_models["nlp"] = SentenceTransformer(str(full_path))

        elif category == "ocr":
            # OCR model loading is actually specifying a directory, and EasyOCR will find and use the model files in that directory
            self._active_models["ocr"] = easyocr.Reader(['ch_sim', 'en'], gpu=torch.cuda.is_available(), model_storage_directory=str(self._category_paths["ocr"]))

        elif category == "audio":
            audio_dir = str(self._category_paths["audio"])
            model_id = Path(model_name).stem

            if self._active_models["audio"] is None:
                # 初始加载
                self._active_models["audio"] = AudioModel(
                    model_name=model_id,
                    download_root=audio_dir
                )
            else:
                self._active_models["audio"].reload_model(model_id)

        self._active_configs[category] = {"filename": model_name, "load_time": datetime.now().isoformat()}
        logger.info(f"Switch successful for '{category}' to '{model_name}'")

    def get_current_models(self) -> Dict[str, Optional[str]]:
        """Gets the currently active model filenames"""
        return {key: val.get("filename") for key, val in self._active_configs.items()}




model_manager = ModelManager()
import torch
import torch.nn as nn
import open_clip
from PIL import Image
import torch.nn.functional as F
from typing import Tuple, List, Union
from ..config import settings
from pathlib import Path
import logging

logger = logging.getLogger("lattice.vision_model")



class ProjectVisionModel(nn.Module):
    """
    视觉模型：采用 open_clip (ViT-B-32)，支持本地化和动态重载。
    """

    def __init__(self, model_name: str, model_path: str):
        super().__init__()
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        self.model_name = model_name
        self.model_path = model_path

        # 1. 优先加载模型
        self._load_model()

        # 2. 改进路径处理：建议从 settings 获取或保持逻辑一致
        # 假设 data 文件夹在项目根目录
        self.data_dir = Path(__file__).parents[2] / 'data'
        self.labels_path = self.data_dir / 'labels.txt'

        model_stem = Path(self.model_path).stem
        self.cache_path = self.data_dir / f'text_features_{model_stem}.pt'

        self.labels = self._load_labels()
        self.text_features = self._precompute_text_features()

    def _load_model(self):
        """合并后的单一加载方法"""
        logger.info(f"Loading local weights from: {self.model_path}")
        if not Path(self.model_path).exists():
            raise FileNotFoundError(f"Model file not found: {self.model_path}")

        try:
            self.model, _, self.preprocess = open_clip.create_model_and_transforms(
                self.model_name,
                pretrained=self.model_path,
                device=self.device,
                precision='fp16' if self.device.type == 'cuda' else 'fp32',
                jit=False
            )
            self.model.eval()
            self.tokenizer = open_clip.get_tokenizer(self.model_name)
            logger.info(f"Successfully loaded model '{self.model_name}'.")
        except Exception as e:
            logger.error(f"Failed to load model: {e}", exc_info=True)
            raise ValueError(f"Invalid model file: {e}")

    def _load_labels(self) -> List[str]:
        """补全缺失的标签加载逻辑"""
        if not self.labels_path.exists():
            logger.warning(f"⚠️ Labels file not found at {self.labels_path}. Vision keywords will be disabled.")
            return []

        try:
            with open(self.labels_path, 'r', encoding='utf-8') as f:
                labels = [line.strip() for line in f if line.strip()]
            logger.info(f"Loaded {len(labels)} labels for vision recognition.")
            return labels
        except Exception as e:
            logger.error(f"Failed to read labels: {e}")
            return []

    def reload_model(self, new_path: str):
        """热重载模型"""
        logger.info(f"Attempting to hot-reload model from new path: {new_path}")
        self.model_path = new_path

        # 核心修改：重新计算当前模型的动态缓存路径
        model_stem = Path(new_path).stem
        self.cache_path = self.data_dir / f'text_features_cache_{model_stem}.pt'

        # 释放显存
        del self.model
        if torch.cuda.is_available():
            torch.cuda.empty_cache()

        self._load_model()
        self.text_features = self._precompute_text_features()


    def _precompute_text_features(self) -> torch.Tensor:
        """
        预计算或加载缓存的文本特征。
        增加 OOM 防护和缓存有效性检查。
        """
        if self.cache_path.exists():
            cached_data = torch.load(self.cache_path, map_location=self.device)
            if cached_data.shape[0] == len(self.labels):
                logger.info(f"Loading cached text features from {self.cache_path}")
                return cached_data
            else:
                logger.warning("Label count mismatch, recomputing features.")

        if not self.labels:
            return torch.empty(0, self.model.text_projection.shape[1], device=self.device)

        logger.info(f"Precomputing text features for {len(self.labels)} labels...")

        all_features = []
        batch_size = settings.VISION_BATCH_SIZE

        with torch.inference_mode(), torch.cuda.amp.autocast(enabled=self.device.type == 'cuda'):
            for i in range(0, len(self.labels), batch_size):
                batch_labels = self.labels[i:i + batch_size]
                text_tokens = self.tokenizer(batch_labels).to(self.device)
                batch_features = self.model.encode_text(text_tokens)
                batch_features = F.normalize(batch_features, p=2, dim=-1)
                all_features.append(batch_features.cpu()) # Move to CPU to save GPU memory

        text_features = torch.cat(all_features).to(self.device)
        torch.save(text_features, self.cache_path)
        logger.info(f"Saved text features to {self.cache_path}")
        return text_features

    @torch.inference_mode()
    def get_embedding(self, img_tensor: torch.Tensor) -> torch.Tensor:
        """提取 L2 归一化的图像特征向量"""
        with torch.cuda.amp.autocast(enabled=self.device.type == 'cuda'):
            img_tensor = img_tensor.to(self.device)
            image_features = self.model.encode_image(img_tensor)
            return F.normalize(image_features, p=2, dim=-1)

    @torch.inference_mode()
    def get_keywords(self, img_tensor: torch.Tensor, top_k: int = 5) -> List[Tuple[str, float]]:
        """识别关键词及置信度"""
        if self.text_features.numel() == 0:
            return []

        with torch.cuda.amp.autocast(enabled=self.device.type == 'cuda'):
            image_features = self.get_embedding(img_tensor)

            logit_scale = self.model.logit_scale.exp()
            similarity = (logit_scale * image_features @ self.text_features.T).softmax(dim=-1)

            probs, indices = torch.topk(similarity, top_k, dim=-1)

            return [(self.labels[idx], prob.item()) for idx, prob in zip(indices[0], probs[0])]

    def process_image(self, image: Image.Image) -> torch.Tensor:
        """应用模型匹配的预处理，确保 RGB 模式"""
        if image.mode != 'RGB':
            image = image.convert('RGB')
        return self.preprocess(image).unsqueeze(0)

def calculate_similarity(feat1: torch.Tensor, feat2: torch.Tensor) -> float:
    """计算余弦相似度"""
    return F.cosine_similarity(feat1, feat2, dim=-1).item()

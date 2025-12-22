# --- 修改后的 app/core/quality_gate.py ---
import logging
from typing import List, Tuple
from app.schemas.response import AnalysisResult
from app.config import settings  # 导入动态配置

logger = logging.getLogger("lattice.quality_gate")


class QualityGate:
    """
    后处理模块：所有过滤规则现在都通过 settings 动态获取。
    """

    @staticmethod
    def apply(result: AnalysisResult, raw_vision_keywords: List[Tuple[str, float]] = None) -> AnalysisResult:
        """统一应用入口。"""
        if result.file_type == 'image' and raw_vision_keywords:
            result = QualityGate._filter_vision_keywords(result, raw_vision_keywords)

        text_types = {'pdf', 'word', 'table', 'text', 'audio'}
        if result.file_type in text_types and result.keywords:
            result = QualityGate._clean_text_keywords(result)

        return result

    @staticmethod
    def _filter_vision_keywords(result: AnalysisResult, raw_keywords: List[Tuple[str, float]]) -> AnalysisResult:
        """使用 settings.VISION_CONFIDENCE_THRESHOLD 进行过滤。"""
        final_keywords = []
        for keyword, confidence in raw_keywords:
            # 动态阈值检查
            if confidence < settings.VISION_CONFIDENCE_THRESHOLD:
                continue

            # 动态黑名单检查
            if keyword.lower() in settings.KEYWORD_BLACKLIST:
                continue

            final_keywords.append(keyword)

        result.keywords = final_keywords
        return result

    @staticmethod
    def _clean_text_keywords(result: AnalysisResult) -> AnalysisResult:
        """使用 settings 中的规则清洗文本关键词。"""
        cleaned_keywords = []
        for kw in result.keywords:
            kw = kw.strip().lower()

            # 1. 动态长度过滤
            if len(kw) < settings.TEXT_KW_MIN_LEN or len(kw) > settings.TEXT_KW_MAX_LEN:
                continue

            # 2. 动态数字过滤
            if settings.TEXT_KW_EXCLUDE_DIGITS and kw.isdigit():
                continue

            # 3. 动态黑名单过滤
            if kw in settings.KEYWORD_BLACKLIST:
                continue

            cleaned_keywords.append(kw)

        result.keywords = list(dict.fromkeys(cleaned_keywords))
        return result
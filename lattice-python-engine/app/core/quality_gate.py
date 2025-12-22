import logging
from typing import List, Tuple
from app.schemas.response import AnalysisResult

logger = logging.getLogger("lattice.quality_gate")


class QualityGate:
    """
    A post-processing module to filter and enhance the results from AI models
    before they are returned by the API.
    """

    # --- Configuration ---
    # Confidence threshold for object recognition. Results below this will be discarded.
    VISION_CONFIDENCE_THRESHOLD = 0.1  # Corresponds to 10% confidence

    # Blacklist of common, low-value keywords that often add noise.
    # These will be removed regardless of their confidence score.
    KEYWORD_BLACKLIST = {
        'background', 'wall', 'floor', 'ceiling', 'curtain', 'plastic bag',
        'fabric', 'textile', 'art', 'pattern', 'design'
    }

    @staticmethod
    def apply(result: AnalysisResult, raw_vision_keywords: List[Tuple[str, float]] = None) -> AnalysisResult:
        """
        Applies a series of quality checks and filters to the analysis result.

        Args:
            result: The original AnalysisResult from the processor.
            raw_vision_keywords: The raw output from the vision model, including confidence scores.

        Returns:
            A cleaned and filtered AnalysisResult.
        """
        if result.file_type == 'image' and raw_vision_keywords:
            result = QualityGate._filter_vision_keywords(result, raw_vision_keywords)
        
        # You can add more filtering rules for other file types here.
        # For example:
        # if result.file_type == 'text':
        #     result = QualityGate._clean_text_keywords(result)

        logger.debug(f"Quality gate applied. Final keywords: {result.keywords}")
        return result

    @staticmethod
    def _filter_vision_keywords(result: AnalysisResult, raw_keywords: List[Tuple[str, float]]) -> AnalysisResult:
        """
        Filters keywords from vision models based on confidence and a blacklist.
        """
        final_keywords = []
        for keyword, confidence in raw_keywords:
            # 1. Check against confidence threshold
            if confidence < QualityGate.VISION_CONFIDENCE_THRESHOLD:
                logger.debug(f"Dropping keyword '{keyword}' due to low confidence ({confidence:.2f})")
                continue

            # 2. Check against blacklist
            if keyword.lower() in QualityGate.KEYWORD_BLACKLIST:
                logger.debug(f"Dropping blacklisted keyword '{keyword}'")
                continue
            
            final_keywords.append(keyword)

        result.keywords = final_keywords
        return result

    # Example for a text-based filter (can be implemented later)
    # @staticmethod
    # def _clean_text_keywords(result: AnalysisResult) -> AnalysisResult:
    #     # ... logic to clean keywords from NLP text analysis ...
    #     return result


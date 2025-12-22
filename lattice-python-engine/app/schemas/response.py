from pydantic import BaseModel
from typing import List, Optional


class AnalysisResult(BaseModel):
    """
    Represents the result of a document analysis.

    This model holds all the information generated during the document
    processing pipeline, including file metadata, processing status,
    and the extracted AI-powered features.
    """
    filename: str  # Original name of the uploaded file.
    file_type: str  # Detected file type (e.g., 'pdf', 'image', 'text').
    status: str  # Processing status: 'success', 'warning', or 'error'.
    message: Optional[str] = None  # Optional message, e.g., for errors or warnings.

    # Core data extracted from the document
    char_count: int = 0  # Total number of characters extracted.
    preview_text: Optional[str] = None  # A short preview of the extracted text.
    keywords: List[str] = []  # List of top keywords identified in the text.
    vector: List[float] = []  # High-dimensional semantic vector representing the document's content.
    vector_dim: int = 0  # The dimension of the semantic vector.


class SimilarityResponse(BaseModel):
    """
    Represents the result of a similarity comparison between two items.
    """
    similarity_score: float  # The calculated cosine similarity score (between -1.0 and 1.0).


class ObjectKeywordsResponse(BaseModel):
    """
    Represents the result of an object recognition task.
    """
    keywords: List[str]  # A list of identified keywords from the image.

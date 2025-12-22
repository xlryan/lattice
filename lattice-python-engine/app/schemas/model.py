from pydantic import BaseModel
from typing import List, Optional, Dict

class ModelSwitchRequest(BaseModel):
    category: str  # "vision", "nlp", or "ocr"
    model_name: str # The filename or directory name of the model to activate

class ModelListResponse(BaseModel):
    available_models: Dict[str, List[str]]
    current_models: Dict[str, Optional[str]]

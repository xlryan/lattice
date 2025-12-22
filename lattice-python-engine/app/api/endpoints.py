import os
import shutil
from pathlib import Path
from app.config import settings
from app.core.model_manager import model_manager
from pydantic import BaseModel
from fastapi import APIRouter, UploadFile, File, HTTPException, Body, Form
from app.core.processor import DocumentProcessor
from app.schemas.response import AnalysisResult, SimilarityResponse, ObjectKeywordsResponse, KeywordDetail
from app.core.vision_model import calculate_similarity
from PIL import Image
import io
import logging

# --- 修改后的部分 ---

# 1. 定义业务接口 Router (用于 /engine/xxx)
router = APIRouter()

# 2. 定义模型管理 Router (用于 /models/xxx)
model_router = APIRouter()

logger = logging.getLogger("lattice.api.endpoints")

# 接下来是你的导入和逻辑
from app.schemas.model import ModelSwitchRequest, ModelListResponse
import aiofiles

@router.on_event("startup")
async def startup_event():
    """
    Application startup event: Pre-loads the default model for each category.
    """
    logger.info("🚀 Application startup: Pre-loading default AI models for each category...")

# --- Model Management API for Java Backend ---

@model_router.post("/models/upload", tags=["Model Management"])
async def upload_model(category: str = Form(...), file: UploadFile = File(...)):
    """
    Uploads a model file (or a .zip for NLP models) to the specified category.
    """
    if category not in model_manager._category_paths:
        raise HTTPException(status_code=400, detail=f"Invalid category '{category}'. Must be one of {list(model_manager._category_paths.keys())}")

    upload_dir = model_manager._category_paths[category]
    file_path = upload_dir / file.filename

    # Simple security check to prevent path traversal
    if ".." in file.filename:
        raise HTTPException(status_code=400, detail="Invalid filename.")

    try:
        async with aiofiles.open(file_path, 'wb') as f:
            while content := await file.read(1024 * 1024):
                await f.write(content)
        logger.info(f"Successfully uploaded '{file.filename}' to '{category}' directory.")
        return {"status": "success", "message": f"Model '{file.filename}' uploaded to '{category}'."}
    except Exception as e:
        logger.error(f"Failed to upload model file: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail="Failed to save uploaded file.")

@model_router.get("/models/list", response_model=ModelListResponse, tags=["Model Management"])
async def list_models():
    """
    Lists all available models and the currently active model for each category.
    """
    return {
        "available_models": model_manager.get_available_models(),
        "current_models": model_manager.get_current_models()
    }

@model_router.post("/models/switch", tags=["Model Management"])
async def switch_active_model(request: ModelSwitchRequest):
    """
    Switches the active model for a given category to the specified model name.
    """
    result = model_manager.switch_model(request.category, request.model_name)
    if result.get("status") == "error":
        raise HTTPException(status_code=400, detail=result["message"])
    return result


# --- Analysis Endpoints ---

@router.post("/engine/analyze", response_model=AnalysisResult, tags=["Analysis"])
async def analyze_document(file: UploadFile = File(...)):
    """
    Core endpoint: receives a file, parses it, performs AI analysis, and returns the JSON result.
    """
    logger.info(f"Received upload request: {file.filename}")
    result = await DocumentProcessor.process(file)
    return result

@router.post("/engine/compare-images", response_model=SimilarityResponse, tags=["Analysis"])
async def compare_images(file1: UploadFile = File(...), file2: UploadFile = File(...)):
    """
    Receives two image files, calculates, and returns their semantic similarity.
    """
    logger.info(f"Received request to compare '{file1.filename}' and '{file2.filename}'")
    
    for file in [file1, file2]:
        if not file.filename.lower().endswith((".jpg", ".png", ".jpeg")):
            raise HTTPException(
                status_code=400,
                detail=f"Unsupported file type for '{file.filename}'. Please upload JPG or PNG images."
            )

    vision_model = model_manager.get_vision_model()
    if not vision_model:
        raise HTTPException(
            status_code=503,
            detail="Vision model is not available. The service might be initializing."
        )

    try:
        content1 = await file1.read()
        content2 = await file2.read()

        image1 = Image.open(io.BytesIO(content1)).convert("RGB")
        image2 = Image.open(io.BytesIO(content2)).convert("RGB")
        
        tensor1 = vision_model.process_image(image1)
        tensor2 = vision_model.process_image(image2)

        embedding1 = vision_model.get_embedding(tensor1)
        embedding2 = vision_model.get_embedding(tensor2)

        similarity_score = calculate_similarity(embedding1, embedding2)
        
        logger.info(f"Successfully compared '{file1.filename}' and '{file2.filename}'. "
                    f"Similarity score: {similarity_score:.4f}")

        return SimilarityResponse(similarity_score=similarity_score)

    except Exception as e:
        logger.error(f"Failed during image comparison: {e}", exc_info=True)
        raise HTTPException(
            status_code=500,
            detail=f"An internal error occurred while comparing images: {str(e)}"
        )

@router.post("/engine/recognize-objects", response_model=ObjectKeywordsResponse, tags=["Analysis"])
async def recognize_objects(file: UploadFile = File(...)):
    """
    Receives a single image file and returns a list of identified object keywords.
    """
    logger.info(f"Received request to recognize objects in '{file.filename}'")

    if not file.filename.lower().endswith((".jpg", ".png", ".jpeg")):
        raise HTTPException(
            status_code=400,
            detail=f"Unsupported file type for '{file.filename}'. Please upload JPG or PNG images."
        )

    vision_model = model_manager.get_vision_model()
    if not vision_model:
        raise HTTPException(
            status_code=503,
            detail="Vision model is not available. The service might be initializing."
        )

    try:
        content = await file.read()
        image = Image.open(io.BytesIO(content)).convert("RGB")
        tensor = vision_model.process_image(image)

        # 获取原始关键词和置信度元组列表
        # raw_keywords 格式为: [("keyword1", 0.85), ("keyword2", 0.12), ...]
        raw_keywords = vision_model.get_keywords(tensor, top_k=5)

        if not raw_keywords:
            return ObjectKeywordsResponse(keywords_detail=[], keywords=[], top_keyword=None)

        # 1. 转换为详细的模型列表
        details = [
            KeywordDetail(keyword=k, confidence=round(c, 4))
            for k, c in raw_keywords
        ]

        # 2. 提取所有关键词文本
        all_keywords = [k for k, c in raw_keywords]

        # 3. 选择最高置信度的关键词
        # vision_model.get_keywords 内部已经按置信度从高到低排序
        # 所以列表第一个元素就是置信度最高的
        highest_confidence_keyword = raw_keywords[0][0]

        logger.info(f"Recognition success. Top result: {highest_confidence_keyword}")

        return ObjectKeywordsResponse(
            keywords_detail=details,
            keywords=all_keywords,
            top_keyword=highest_confidence_keyword
        )

    except Exception as e:
        logger.error(f"Failed during object recognition: {e}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))


@router.post("/engine/transcribe", tags=["Analysis"])
async def transcribe_audio(file: UploadFile = File(...)):
    """
    专门的语音转录接口：接收音频文件，返回纯文本转录结果。
    """
    if not file.filename.lower().endswith(('.mp3', '.wav', '.flac', '.m4a')):
        raise HTTPException(status_code=400, detail="Unsupported audio format.")

    # 直接调用已有的音频处理逻辑
    content = await file.read()
    text = await DocumentProcessor._extract_audio(content)

    if not text:
        raise HTTPException(status_code=500, detail="Transcription failed.")

    return {"filename": file.filename, "transcription": text}
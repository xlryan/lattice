import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI, UploadFile, File
from app.config import settings
from app.api.endpoints import router as api_router, model_router
from app.core.model_manager import model_manager

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)
logger = logging.getLogger("lattice.main")

@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Handles application startup events by pre-loading all default AI models.
    """
    logger.info("🚀 System startup: Pre-loading all default AI models...")
    try:
        model_manager.get_vision_model()
        model_manager.get_nlp_model()
        model_manager.get_ocr_model()
        logger.info("✅ All default models loaded successfully. System is ready.")
    except Exception as e:
        logger.error(f"❌ Critical error during model pre-loading: {e}", exc_info=True)

    yield

    logger.info("🛑 System shutdown.")

# Create the main FastAPI application instance
app = FastAPI(
    title=settings.APP_NAME,
    lifespan=lifespan
)

# --- Mount the API routers ---
app.include_router(api_router, prefix="/api/v1")
app.include_router(model_router, prefix="/api/v1")

# --- Main execution block for running with uvicorn ---
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)

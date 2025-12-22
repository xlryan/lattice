import os
import shutil
from sentence_transformers import SentenceTransformer
import open_clip
import easyocr
import torch

# Define the models directory
BASE_DIR = os.path.dirname(__file__)
MODELS_DIR = os.path.join(BASE_DIR, 'models')

DIRS = {
    "vision": os.path.join(MODELS_DIR, "vision"),
    "nlp": os.path.join(MODELS_DIR, "nlp"),
    "ocr": os.path.join(MODELS_DIR, "ocr"),
    "audio": os.path.join(MODELS_DIR, "audio")
}

for d in DIRS.values():
    os.makedirs(d, exist_ok=True)

print(f"--- Downloading default models to {MODELS_DIR} ---")

# 1. Download Vision Model (OpenCLIP ViT-B-32)
print("\n[1/4] Downloading Vision Model...")
try:
    open_clip.create_model(
        'ViT-B-32',
        pretrained='laion2b_s34b_b79k',
        cache_dir=DIRS["vision"]
    )
    print("✅ Vision Model downloaded successfully.")
except Exception as e:
    print(f"❌ Error downloading Vision Model: {e}")

# 2. Download NLP Model (SentenceTransformer)
print("\n[2/4] Downloading NLP Model...")
try:
    nlp_model_name = 'paraphrase-multilingual-MiniLM-L12-v2'
    nlp_save_path = os.path.join(DIRS["nlp"], nlp_model_name)
    if not os.path.exists(nlp_save_path):
        model = SentenceTransformer(nlp_model_name)
        model.save(nlp_save_path)
        print(f"✅ NLP Model saved to {nlp_save_path}")
    else:
        print("ℹ️ NLP Model already exists.")
except Exception as e:
    print(f"❌ Error downloading NLP Model: {e}")

# 3. Download OCR Model (EasyOCR)
print("\n[3/4] Downloading OCR Model...")
try:
    easyocr.Reader(['ch_sim', 'en'], gpu=torch.cuda.is_available(), model_storage_directory=DIRS["ocr"])
    print("✅ OCR Model downloaded successfully.")
except Exception as e:
    print(f"❌ Error downloading OCR Model: {e}")

print("\n--- All default models are ready. ---")

# 4. Download Audio Model (Whisper)
print("\n[4/4] Downloading Audio Model (Whisper)...")
try:
    import whisper
    audio_dir = DIRS["audio"]
    # 显式下载到指定目录
    whisper.load_model("base", download_root=audio_dir)
    print(f"✅ Audio Model (base) downloaded to {audio_dir}")
except Exception as e:
    print(f"❌ Error downloading Audio Model: {e}")
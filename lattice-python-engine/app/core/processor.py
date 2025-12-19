import io
import pdfplumber
from pdf2image import convert_from_bytes
import jieba.analyse
import logging
from fastapi import UploadFile
from app.core.model_loader import model_engine
from app.schemas.response import AnalysisResult
import cv2
import numpy as np
import docx
import pandas as pd

logger = logging.getLogger("lattice.processor")


class DocumentProcessor:

    @staticmethod
    async def process(file: UploadFile) -> AnalysisResult:
        filename = file.filename
        content = await file.read()
        file_size = len(content)

        logger.info(f"Processing file: {filename} ({file_size} bytes)")

        text_content = ""
        file_type = "unknown"

        # 1. 解析文本
        try:
            if filename.lower().endswith(".pdf"):
                file_type = "pdf"
                text_content = DocumentProcessor._extract_pdf(content)
            elif filename.lower().endswith((".jpg", ".png", ".jpeg")):
                file_type = "image"
                text_content = DocumentProcessor._extract_image(content)
            elif filename.lower().endswith(('.doc', '.docx')):
                file_type = "word"
                text_content = DocumentProcessor._extract_word(content)
            elif filename.lower().endswith(('.xls', '.xlsx')):
                file_type = "table"
                text_content = DocumentProcessor._extract_table(content)
            elif filename.lower().endswith(('.txt', '.md', '.py', '.json', '.xml', '.html', '.css', '.js', '.ts', '.java', '.c', '.cpp', '.go', '.rs', '.php', '.rb', '.sh', '.log', '.csv')):
                file_type = "text"
                text_content = DocumentProcessor._extract_text(content)
            else:
                return AnalysisResult(
                    filename=filename, file_type="unsupported", status="error",
                    message="Unsupported file format"
                )
        except Exception as e:
            logger.error(f"Error parsing file: {e}", exc_info=True)
            return AnalysisResult(
                filename=filename, file_type=file_type, status="error",
                message=f"Parse error: {str(e)}"
            )

        # 2. 空内容检查
        if not text_content or len(text_content.strip()) == 0:
            return AnalysisResult(
                filename=filename, file_type=file_type, status="warning",
                message="No text extracted (Scanned PDF without OCR?)"
            )

        # 3. AI 特征工程
        try:
            # A. 关键词提取 (Top 10)
            keywords = jieba.analyse.extract_tags(text_content, topK=10)

            # B. 语义向量化
            nlp = model_engine.get_nlp_model()
            embedding = nlp.encode(text_content).tolist()

            return AnalysisResult(
                filename=filename,
                file_type=file_type,
                status="success",
                char_count=len(text_content),
                preview_text=text_content[:200].replace("\n", " ") + "...",
                keywords=keywords,
                vector=embedding,
                vector_dim=len(embedding)
            )
        except Exception as e:
            logger.error(f"AI Inference error: {e}", exc_info=True)
            return AnalysisResult(
                filename=filename, file_type=file_type, status="error",
                message=f"AI processing failed: {str(e)}"
            )

    @staticmethod
    def _extract_pdf(file_bytes: bytes) -> str:
        text = ""
        with pdfplumber.open(io.BytesIO(file_bytes)) as pdf:
            for page in pdf.pages:
                page_text = page.extract_text()
                if page_text:
                    text += page_text + "\n"

        # 如果 text 为空，这里可以引入 pdf2image 将页面转为图片，再调 _extract_image
        if not text.strip():
            logger.warning(
                "No text layer found in PDF, attempting OCR..."
            )
            all_text = []
            try:
                images = convert_from_bytes(file_bytes)
                for i, image in enumerate(images):
                    logger.debug(f"Processing PDF page {i+1} via OCR")
                    # Convert PIL image to bytes for _extract_image
                    img_byte_arr = io.BytesIO()
                    image.save(img_byte_arr, format='PNG')
                    img_bytes = img_byte_arr.getvalue()
                    all_text.append(DocumentProcessor._extract_image(img_bytes))
                text = "\n".join(all_text)
            except Exception as e:
                logger.error(f"PDF to Image OCR failed: {e}", exc_info=True)

        return text

    @staticmethod
    def _run_ocr_and_extract(ocr_engine, image_np: np.ndarray) -> list[str]:
        """
        使用 EasyOCR 运行识别并提取文本。
        """
        # EasyOCR 的 readtext 直接接受 numpy 数组
        # detail=1 返回详细信息（含置信度），detail=0 只返回文本列表
        results = ocr_engine.readtext(image_np)

        extracted_lines = []
        if not results:
            return extracted_lines

        for res in results:
            # EasyOCR 格式: ( [bbox], text, confidence )
            if len(res) == 3:
                bbox, text, score = res
                if score > 0.3:  # EasyOCR 置信度通常比较稳，0.3 即可
                    extracted_lines.append(text)

        return extracted_lines

    @staticmethod
    def _extract_image(file_bytes: bytes) -> str:
        ocr = model_engine.get_ocr_model()
        if not ocr:
            return ""

        try:
            # 1. 解码图像
            nparr = np.frombuffer(file_bytes, np.uint8)
            img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
            if img is None:
                return ""

            # Ensure image array is np.uint8 to avoid numpy incompatibility issues
            img = img.astype(np.uint8)
            texts = DocumentProcessor._run_ocr_and_extract(ocr, img)

            if not texts:
                gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY).astype(np.uint8)
                texts = DocumentProcessor._run_ocr_and_extract(ocr, gray)

            return "\n".join(texts)

        except Exception as e:
            logger.error(f"OCR 识别出错: {e}", exc_info=True)
            return ""


    @staticmethod
    def _extract_word(file_bytes: bytes) -> str:
        """Extract text from a Word document (.docx)."""
        text = ""
        try:
            doc = docx.Document(io.BytesIO(file_bytes))
            for para in doc.paragraphs:
                text += para.text + "\n"
        except Exception as e:
            logger.error(f"Error extracting from Word file: {e}", exc_info=True)
        return text

    @staticmethod
    def _extract_table(file_bytes: bytes) -> str:
        """Extract text from a table-based file (.xls, .xlsx)."""
        text = ""
        try:
            df_dict = pd.read_excel(io.BytesIO(file_bytes), sheet_name=None)
            for sheet_name, df in df_dict.items():
                text += f"--- Sheet: {sheet_name} ---\n"
                text += df.to_string(index=False) + "\n\n"
        except Exception as e:
            logger.error(f"Error extracting from table file: {e}", exc_info=True)
        return text


    @staticmethod
    def _extract_text(file_bytes: bytes) -> str:
        """从纯文本字节流中提取文本，支持多种编码格式"""
        for encoding in ["utf-8", "gbk", "gb18030", "utf-16"]:
            try:
                return file_bytes.decode(encoding)
            except (UnicodeDecodeError, LookupError):
                continue
        # 兜底方案：强制以 utf-8 解码并忽略错误字符
        return file_bytes.decode("utf-8", errors="ignore")



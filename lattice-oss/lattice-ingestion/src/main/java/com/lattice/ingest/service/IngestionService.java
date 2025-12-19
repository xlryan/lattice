package com.lattice.ingest.service;

import com.lattice.ingest.service.dto.IngestionResponse;
import com.lattice.ingest.service.dto.TextIngestionRequest;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文本/文件 -> 元数据 -> Node 的编排接口。
 */
public interface IngestionService {

    IngestionResponse ingest(TextIngestionRequest request);

    IngestionResponse ingestFile(MultipartFile file, String titleHint, java.util.List<String> tags);
}


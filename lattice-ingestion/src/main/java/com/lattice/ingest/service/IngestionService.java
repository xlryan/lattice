package com.lattice.ingest.service;

import com.lattice.ingest.service.dto.IngestionResponse;
import com.lattice.ingest.service.dto.TextIngestionRequest;

/**
 * 文本 -> 元数据 -> Node 的编排接口。
 */
public interface IngestionService {

    IngestionResponse ingest(TextIngestionRequest request);
}

package com.lattice.ingest.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 入库请求 DTO，原始文本 + 可选标签。
 */
public record TextIngestionRequest(
        @NotBlank(message = "rawText 不能为空")
        String rawText,
        @Size(max = 120, message = "标题不能超过 120 字")
        String titleHint,
        List<String> tags
) {
}

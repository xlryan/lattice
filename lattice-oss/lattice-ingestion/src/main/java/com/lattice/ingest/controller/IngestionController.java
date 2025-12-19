package com.lattice.ingest.controller;

import com.lattice.ingest.service.IngestionService;
import com.lattice.ingest.service.dto.IngestionResponse;
import com.lattice.ingest.service.dto.TextIngestionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * 对外开放的 Ingestion API，支持文本和文件入库。
 */
@RestController("latticeIngestionController")
@RequestMapping("/api/ingest")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping
    public ResponseEntity<IngestionResponse> ingest(@Valid @RequestBody TextIngestionRequest request) {
        return ResponseEntity.ok(ingestionService.ingest(request));
    }

    @PostMapping("/file")
    public ResponseEntity<IngestionResponse> ingestFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "tags", required = false) List<String> tags) {
        return ResponseEntity.ok(ingestionService.ingestFile(file, title, tags));
    }
}


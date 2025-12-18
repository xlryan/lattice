package com.lattice.ingest.controller;

import com.lattice.ingest.service.IngestionService;
import com.lattice.ingest.service.dto.IngestionResponse;
import com.lattice.ingest.service.dto.TextIngestionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对外开放的 Ingestion API，单 endpoint 就能完成自动分类入库。
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
}

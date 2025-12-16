package com.lattice.retrieval.controller;

import com.lattice.retrieval.api.SearchRequest;
import com.lattice.retrieval.api.SearchResponse;
import com.lattice.retrieval.service.RetrievalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 混合检索 Endpoint，可被前端或 Agent 直接调用。
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final RetrievalService retrievalService;

    @PostMapping
    public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        return ResponseEntity.ok(retrievalService.search(request));
    }
}

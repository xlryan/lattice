package com.lattice.retrieval.service;

import com.lattice.retrieval.api.SearchRequest;
import com.lattice.retrieval.api.SearchResponse;

public interface RetrievalService {
    SearchResponse search(SearchRequest request);
}

package com.lattice.retrieval.api;

import java.util.List;

public record SearchResponse(List<SearchResultItem> results, long tookMillis) {
}

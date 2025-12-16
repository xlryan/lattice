package com.lattice.retrieval.api;

import java.util.UUID;

public record SearchResultItem(
        UUID id,
        String title,
        String snippet,
        double score
) {
}

package com.lattice.core.repository.career;

import com.lattice.core.domain.career.CareerNode;

import java.util.List;

public interface CareerRepositoryCustom {

    List<CareerNode> semanticSearch(List<Double> queryEmbedding, int limit);
}

package com.lattice.core.repository.wealth;

import com.lattice.core.domain.wealth.WealthEntry;

import java.util.List;

public interface WealthRepositoryCustom {

    List<WealthEntry> semanticSearch(List<Double> embedding, int limit);
}

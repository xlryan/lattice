package com.lattice.core.repository.intel;

import com.lattice.core.domain.intel.IntelSignal;

import java.util.List;

public interface IntelSignalRepositoryCustom {

    List<IntelSignal> semanticSearch(List<Double> embedding, int limit);
}

package com.lattice.core.repository;

import com.lattice.core.repository.query.HybridSearchCriteria;
import com.lattice.core.repository.query.LatticeNodeSearchResult;

import java.util.List;

public interface LatticeNodeQueryRepository {

    /**
     * 混合检索封装，先过滤后向量算分。
     */
    List<LatticeNodeSearchResult> hybridSearch(HybridSearchCriteria criteria);
}

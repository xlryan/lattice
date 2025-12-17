package com.lattice.core.repository.build;

import com.lattice.core.domain.build.BuildArtifact;

import java.util.List;

public interface BuildArtifactRepositoryCustom {

    List<BuildArtifact> semanticSearch(List<Double> embedding, int limit);
}

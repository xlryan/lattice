package com.lattice.core.repository.build;

import com.lattice.core.domain.build.BuildArtifact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BuildArtifactRepository extends JpaRepository<BuildArtifact, UUID>, BuildArtifactRepositoryCustom {
}

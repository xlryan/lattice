package com.lattice.core.repository.intel;

import com.lattice.core.domain.intel.IntelSignal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IntelSignalRepository extends JpaRepository<IntelSignal, UUID>, IntelSignalRepositoryCustom {
}

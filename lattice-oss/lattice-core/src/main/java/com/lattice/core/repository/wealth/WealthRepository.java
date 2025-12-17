package com.lattice.core.repository.wealth;

import com.lattice.core.domain.wealth.WealthEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WealthRepository extends JpaRepository<WealthEntry, UUID>, WealthRepositoryCustom {
}

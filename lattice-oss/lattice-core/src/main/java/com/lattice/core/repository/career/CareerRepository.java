package com.lattice.core.repository.career;

import com.lattice.core.domain.career.CareerNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CareerRepository extends JpaRepository<CareerNode, UUID>, CareerRepositoryCustom {
}

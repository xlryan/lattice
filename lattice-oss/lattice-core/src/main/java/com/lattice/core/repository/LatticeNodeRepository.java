package com.lattice.core.repository;

import com.lattice.core.domain.model.LatticeNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Node 的基础 CRUD 仓储，复杂检索交给自定义接口。
 */
public interface LatticeNodeRepository extends JpaRepository<LatticeNode, UUID>, LatticeNodeQueryRepository {
}

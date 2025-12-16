package com.lattice.core.domain;

/**
 * Lattice 的四大核心领域。通过显式枚举可以约束 JSONB 中的 schema 变体。
 */
public enum DomainType {
    CAREER,
    DIY,
    MUSIC,
    LIFE,
    FINANCE
}

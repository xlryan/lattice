package com.lattice.core.repository.support;

import com.pgvector.PGvector;

import java.util.List;

/**
 * Utility to convert embedding payloads into JDBC parameters understood by pgvector.
 */
public final class PgvectorParameter {

    private PgvectorParameter() {
    }

    public static PGvector from(List<Double> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("查询向量不能为空");
        }
        return new PGvector(values);
    }
}

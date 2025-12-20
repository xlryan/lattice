package com.lattice.core.domain.support;

import java.util.List;

/**
 * Helper for converting embedding payloads from external services into pgvector-friendly arrays.
 */
public final class VectorUtils {

    private VectorUtils() {
    }

    public static float[] toFloatArray(List<Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        float[] vector = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            Double value = values.get(i);
            if (value == null) {
                throw new IllegalArgumentException("Vector element cannot be null at index " + i);
            }
            vector[i] = value.floatValue();
        }
        return vector;
    }
}

package com.lattice.core.domain.support;

import com.pgvector.PGvector;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts List<Double> to/from PostgreSQL pgvector columns.
 */
@Converter(autoApply = false)
public class DoubleVectorConverter implements AttributeConverter<List<Double>, PGvector> {

    @Override
    public PGvector convertToDatabaseColumn(List<Double> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        float[] values = new float[attribute.size()];
        for (int i = 0; i < attribute.size(); i++) {
            values[i] = attribute.get(i).floatValue();
        }
        return new PGvector(values);
    }

    @Override
    public List<Double> convertToEntityAttribute(PGvector dbData) {
        if (dbData == null || dbData.toArray() == null) {
            return null;
        }
        float[] floats = dbData.toArray();
        List<Double> results = new ArrayList<>(floats.length);
        for (float value : floats) {
            results.add((double) value);
        }
        return results;
    }
}

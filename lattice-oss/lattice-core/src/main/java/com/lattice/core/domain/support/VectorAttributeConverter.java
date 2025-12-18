package com.lattice.core.domain.support;

import com.pgvector.PGvector;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts float[] vectors to PostgreSQL pgvector.
 */
@Converter(autoApply = false)
public class VectorAttributeConverter implements AttributeConverter<float[], PGvector> {

    @Override
    public PGvector convertToDatabaseColumn(float[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return null;
        }
        return new PGvector(attribute);
    }

    @Override
    public float[] convertToEntityAttribute(PGvector dbData) {
        if (dbData == null || dbData.toArray() == null) {
            return new float[0];
        }
        return dbData.toArray();
    }
}

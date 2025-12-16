package com.lattice.core.domain.support;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Locale;

/**
 * 将 float[] 与 PostgreSQL pgvector 列互转，保证语义向量可以透明持久化。
 */
@Converter(autoApply = false)
public class VectorAttributeConverter implements AttributeConverter<float[], PGobject> {

    @Override
    public PGobject convertToDatabaseColumn(float[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return null;
        }
        try {
            PGobject object = new PGobject();
            object.setType("vector");
            object.setValue(toLiteral(attribute));
            return object;
        } catch (SQLException e) {
            throw new IllegalStateException("无法写入 pgvector 列", e);
        }
    }

    @Override
    public float[] convertToEntityAttribute(PGobject dbData) {
        if (dbData == null || dbData.getValue() == null) {
            return new float[0];
        }
        return parseLiteral(dbData.getValue());
    }

    private String toLiteral(float[] vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            builder.append(String.format(Locale.US, "%s", vector[i]));
            if (i < vector.length - 1) {
                builder.append(',');
            }
        }
        return builder.append(']').toString();
    }

    private float[] parseLiteral(String literal) {
        if (literal.length() < 2) {
            return new float[0];
        }
        String body = literal.substring(1, literal.length() - 1).trim();
        if (body.isEmpty()) {
            return new float[0];
        }
        String[] tokens = body.split(",");
        float[] result = new float[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            result[i] = Float.parseFloat(tokens[i].trim());
        }
        return result;
    }
}

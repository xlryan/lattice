package com.lattice.core.domain.support;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 将 List<Double> 持久化为 PostgreSQL pgvector 列。
 */
@Converter(autoApply = false)
public class DoubleVectorConverter implements AttributeConverter<List<Double>, PGobject> {

    @Override
    public PGobject convertToDatabaseColumn(List<Double> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        try {
            PGobject object = new PGobject();
            object.setType("vector");
            object.setValue(toLiteral(attribute));
            return object;
        } catch (SQLException ex) {
            throw new IllegalStateException("无法写入 pgvector", ex);
        }
    }

    @Override
    public List<Double> convertToEntityAttribute(PGobject dbData) {
        if (dbData == null || dbData.getValue() == null) {
            return Collections.emptyList();
        }
        String literal = dbData.getValue();
        String body = literal.substring(1, literal.length() - 1).trim();
        if (body.isEmpty()) {
            return Collections.emptyList();
        }
        return java.util.Arrays.stream(body.split(","))
                .map(token -> Double.parseDouble(token.trim()))
                .collect(Collectors.toList());
    }

    private String toLiteral(List<Double> values) {
        return values.stream()
                .map(v -> String.format(Locale.US, "%f", v))
                .collect(Collectors.joining(",", "[", "]"));
    }
}

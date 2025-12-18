package com.lattice.core.infrastructure.persistence;

import com.pgvector.PGvector;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Custom Hibernate type mapping List<Double> to PostgreSQL pgvector columns.
 */
public class VectorType implements UserType<List<Double>> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<List<Double>> returnedClass() {
        return (Class<List<Double>>) (Class<?>) List.class;
    }

    @Override
    public boolean equals(List<Double> x, List<Double> y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(List<Double> x) {
        return Objects.hashCode(x);
    }

    @Override
    public List<Double> nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        Object object = rs.getObject(position);
        if (object == null) {
            return null;
        }
        if (object instanceof PGvector pgVector) {
            float[] floats = pgVector.toArray();
            List<Double> list = new ArrayList<>(floats.length);
            for (float value : floats) {
                list.add((double) value);
            }
            return list;
        }
        throw new SQLException("Unknown pgvector type: " + object.getClass());
    }

    @Override
    public void nullSafeSet(PreparedStatement st, List<Double> value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            float[] floats = new float[value.size()];
            for (int i = 0; i < value.size(); i++) {
                floats[i] = value.get(i).floatValue();
            }
            st.setObject(index, new PGvector(floats));
        }
    }

    @Override
    public List<Double> deepCopy(List<Double> value) {
        return value == null ? null : new ArrayList<>(value);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(List<Double> value) {
        return (Serializable) deepCopy(value);
    }

    @Override
    public List<Double> assemble(Serializable cached, Object owner) {
        return deepCopy((List<Double>) cached);
    }
}

package com.lattice.retrieval.filter;

import java.util.LinkedHashMap;
import java.util.Map;

public record FilterRange(String path, Double min, Double max) implements FilterExpression {

    @Override
    public SqlFragment toSqlFragment(String paramPrefix) {
        String accessor = "(" + JsonbPathHelper.accessor(path) + ")::numeric";
        Map<String, Object> params = new LinkedHashMap<>();
        StringBuilder expression = new StringBuilder();
        if (min != null) {
            String param = paramPrefix + "_min";
            expression.append(accessor).append(" >= :").append(param);
            params.put(param, min);
        }
        if (max != null) {
            if (!params.isEmpty()) {
                expression.append(" and ");
            }
            String param = paramPrefix + "_max";
            expression.append(accessor).append(" <= :").append(param);
            params.put(param, max);
        }
        return new SqlFragment(expression.toString(), params);
    }
}

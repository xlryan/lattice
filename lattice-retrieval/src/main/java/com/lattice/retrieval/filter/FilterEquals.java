package com.lattice.retrieval.filter;

import java.util.Map;

public record FilterEquals(String path, Object value) implements FilterExpression {

    @Override
    public SqlFragment toSqlFragment(String paramPrefix) {
        String paramName = paramPrefix + "_eq";
        String expression = JsonbPathHelper.accessor(path) + " = :" + paramName;
        return new SqlFragment(expression, Map.of(paramName, value));
    }
}

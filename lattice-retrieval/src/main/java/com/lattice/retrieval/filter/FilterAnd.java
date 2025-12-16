package com.lattice.retrieval.filter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record FilterAnd(List<FilterExpression> expressions) implements FilterExpression {

    @Override
    public SqlFragment toSqlFragment(String paramPrefix) {
        if (expressions == null || expressions.isEmpty()) {
            return new SqlFragment("1=1", Map.of());
        }
        Map<String, Object> params = new LinkedHashMap<>();
        String sql = expressions.stream()
                .map(expr -> expr.toSqlFragment(paramPrefix + params.size()))
                .map(fragment -> {
                    params.putAll(fragment.parameters());
                    return '(' + fragment.expression() + ')';
                })
                .collect(Collectors.joining(" and "));
        return new SqlFragment(sql, params);
    }
}

package com.lattice.retrieval.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * JSONB 过滤表达式抽象，封装 SQL 拼接与参数绑定。
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FilterEquals.class, name = "eq"),
        @JsonSubTypes.Type(value = FilterRange.class, name = "range"),
        @JsonSubTypes.Type(value = FilterAnd.class, name = "and"),
        @JsonSubTypes.Type(value = FilterOr.class, name = "or")
})
public interface FilterExpression {

    SqlFragment toSqlFragment(String paramPrefix);
}

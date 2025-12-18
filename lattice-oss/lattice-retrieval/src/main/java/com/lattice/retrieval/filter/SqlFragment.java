package com.lattice.retrieval.filter;

import java.util.Map;

public record SqlFragment(String expression, Map<String, Object> parameters) {
}

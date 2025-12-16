package com.lattice.retrieval.filter;

/**
 * 将 dot 表达式转换为 jsonb 访问器（properties -> 'key' ->> 'leaf'）。
 */
final class JsonbPathHelper {

    private JsonbPathHelper() {
    }

    static String accessor(String path) {
        String[] segments = path.split("\\.");
        StringBuilder builder = new StringBuilder("properties");
        for (int i = 0; i < segments.length; i++) {
            boolean last = i == segments.length - 1;
            builder.append(last ? "->>" : "->")
                    .append("'").append(segments[i]).append("'");
        }
        return builder.toString();
    }
}

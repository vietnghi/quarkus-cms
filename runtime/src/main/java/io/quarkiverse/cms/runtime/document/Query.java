package io.quarkiverse.cms.runtime.document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Strapi-compatible query options for list operations.
 */
public record Query(
        Map<String, List<Filter>> filters,
        List<SortField> sort,
        int page,
        int pageSize,
        List<String> fields,
        List<String> populate,
        String locale,
        String status
) {
    public static Query defaults() {
        return new Query(Map.of(), List.of(), 1, 25, List.of(), List.of(), null, null);
    }

    /** Filter operator/value pair. */
    public record Filter(String operator, String value) {}
    /** Sort field and direction. */
    public record SortField(String field, String dir) {}

    private static final ObjectMapper SIMPLE = new ObjectMapper();

    /** Parse Strapi-style filters JSON: {"field":{"$op":"val"}} */
    public static Map<String, List<Filter>> parseFilters(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            Map<String, Map<String, String>> raw = SIMPLE.readValue(json,
                    new TypeReference<Map<String, Map<String, String>>>() {});
            Map<String, List<Filter>> result = new HashMap<>();
            raw.forEach((field, ops) -> {
                List<Filter> list = new ArrayList<>();
                ops.forEach((op, val) -> list.add(new Filter(op, val)));
                result.put(field, list);
            });
            return result;
        } catch (Exception e) {
            return Map.of();
        }
    }

    /** Parse Strapi-style sort: "field:asc,field2:desc" */
    public static List<SortField> parseSort(String param) {
        if (param == null || param.isBlank()) return List.of();
        List<SortField> result = new ArrayList<>();
        for (String part : param.split(",")) {
            String[] pair = part.split(":");
            String field = pair[0].trim();
            String dir = pair.length > 1 ? pair[1].trim() : "asc";
            if (!field.isEmpty()) result.add(new SortField(field, dir));
        }
        return result;
    }
}

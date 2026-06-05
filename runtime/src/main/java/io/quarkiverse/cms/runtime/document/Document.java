package io.quarkiverse.cms.runtime.document;

import java.time.Instant;
import java.util.Map;

/**
 * A single content entry as the API sees it. The {@code data} map holds field
 * values; the dynamic persistence layer stores these (JSONB by default).
 */
public record Document(
        String id,
        String contentType,
        String status,        // "draft" | "published"
        String locale,
        Instant createdAt,
        Instant updatedAt,
        Map<String, Object> data
) {}

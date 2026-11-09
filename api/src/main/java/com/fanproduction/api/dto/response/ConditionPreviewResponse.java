package com.fanproduction.api.dto.response;

/**
 * Результат предпросмотра условия CONDITION.
 *
 * @param matched {@code true} — условие истинно (THEN), {@code false} — ложно (ELSE)
 */
public record ConditionPreviewResponse(boolean matched) {
}

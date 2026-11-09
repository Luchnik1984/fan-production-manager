package com.fanproduction.gui.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Результат предпросмотра условия CONDITION (US9.9.d).
 *
 * @param matched {@code true} — условие истинно (THEN), {@code false} — ложно (ELSE)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConditionPreviewResponse(boolean matched) {
}

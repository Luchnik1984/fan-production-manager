package com.fanproduction.gui.dto.response.preview;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Результат предпросмотра формулы DSL.
 * <p>
 * Соответствует серверному {@code com.fanproduction.api.dto.response.FormulaPreviewResponse}.
 *
 * @param result вычисленное значение (строка); может быть {@code null}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FormulaPreviewResponse(String result) {
}

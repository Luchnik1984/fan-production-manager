package com.fanproduction.api.dto.response;

/**
 * Результат предпросмотра формулы DSL (US9.9.c).
 *
 * @param result вычисленное значение (строка); может быть {@code null}
 */
public record FormulaPreviewResponse(String result) {
}
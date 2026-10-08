package com.fanproduction.template.model;

import java.util.List;

/**
 * Правило маркировки — последовательность элементов.
 * <p>
 * Сериализуется в JSONB как:
 * <pre>
 * {
 *   "sequence": [ ... ]
 * }
 * </pre>
 */
public record MarkingRule(List<MarkingRuleElement> sequence) {

    /**
     * Пустое правило.
     */
    public static MarkingRule empty() {
        return new MarkingRule(List.of());
    }
}

package com.fanproduction.template.model;

/**
 * Side-effect, выполняемый при выборе значения в SELECTABLE-поле.
 * <p>
 * Пример:
 * <pre>
 * {
 *   "action": "ADD_TO_PRODUCT",
 *   "role": "Ступица",
 *   "quantity": 1.0
 * }
 * </pre>
 */
public record OnSelectAction(
        String action,
        String role,
        Double quantity
) {
}

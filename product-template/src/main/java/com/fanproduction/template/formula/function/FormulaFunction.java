package com.fanproduction.template.formula.function;

import java.util.List;

/**
 * Функциональный интерфейс формулы.
 * <p>
 * Принимает список уже вычисленных аргументов, возвращает результат.
 * Аргументы могут быть {@code null} — конкретная функция решает,
 * как обрабатывать отсутствие значения.
 */
@FunctionalInterface
public interface FormulaFunction {

    /**
     * Вычислить значение функции.
     *
     * @param arguments вычисленные аргументы (могут быть {@code null})
     * @return результат или {@code null}
     */
    Object apply(List<Object> arguments);
}

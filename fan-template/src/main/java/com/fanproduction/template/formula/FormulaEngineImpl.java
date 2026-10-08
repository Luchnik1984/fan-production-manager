package com.fanproduction.template.formula;

import com.fanproduction.template.formula.ast.Expression;
import com.fanproduction.template.formula.ast.FieldExpression;
import com.fanproduction.template.formula.ast.FunctionExpression;
import com.fanproduction.template.formula.ast.LiteralExpression;
import com.fanproduction.template.formula.ast.MapExpression;
import com.fanproduction.template.formula.exception.FormulaException;
import com.fanproduction.template.formula.function.FormulaFunctions;
import com.fanproduction.template.formula.parser.FormulaParser;
import com.fanproduction.template.service.ComputedFieldsLibrary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация движка исполнения формул DSL.
 * <p>
 * Обходит AST рекурсивно. Для {@link FieldExpression} сначала ищет
 * значение в переданной карте, потом обращается к {@link ComputedFieldsLibrary}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FormulaEngineImpl implements FormulaEngine {

    private final FormulaFunctions formulaFunctions;
    private final ComputedFieldsLibrary computedFieldsLibrary;

    // ==========================================================
    // PARSE
    // ==========================================================

    @Override
    public Expression parse(String formula) {
        return new FormulaParser(formula).parse();
    }

    // ==========================================================
    // EVALUATE
    // ==========================================================

    @Override
    public Object evaluate(String formula, Map<String, Object> values) {
        Expression expression = parse(formula);
        return evaluate(expression, values);
    }

    @Override
    public Object evaluate(Expression expression, Map<String, Object> values) {
        if (expression == null) return null;

        Map<String, Object> safeValues = values != null ? values : Map.of();

        if (expression instanceof LiteralExpression lit) {
            return lit.value();
        }

        if (expression instanceof FieldExpression field) {
            return resolveField(field.fieldName(), safeValues);
        }

        if (expression instanceof MapExpression map) {
            Map<Object, Object> result = new LinkedHashMap<>();
            for (Map.Entry<Object, Expression> entry : map.entries().entrySet()) {
                result.put(entry.getKey(), evaluate(entry.getValue(), safeValues));
            }
            return result;
        }

        if (expression instanceof FunctionExpression fn) {
            List<Object> args = fn.arguments().stream()
                    .map(arg -> evaluate(arg, safeValues))
                    .toList();
            return formulaFunctions.call(fn.name(), args);
        }

        throw new FormulaException(
                "Неизвестный тип выражения: " + expression.getClass().getName());
    }

    @Override
    public String evaluateToString(String formula, Map<String, Object> values) {
        Object result = evaluate(formula, values);
        if (result == null) return null;
        return FormulaFunctions.toStringValue(result);
    }

    // ==========================================================
    // РАЗРЕШЕНИЕ ПОЛЕЙ
    // ==========================================================

    /**
     * Разрешить значение поля:
     * <ol>
     *   <li>Сначала ищем прямое значение в {@code values}.</li>
     *   <li>Если нет — пытаемся через {@link ComputedFieldsLibrary}.</li>
     *   <li>Если и там нет — возвращаем {@code null}.</li>
     * </ol>
     */
    private Object resolveField(String fieldName, Map<String, Object> values) {
        if (fieldName == null) return null;

        Object direct = values.get(fieldName);
        if (direct != null) return direct;

        // Проверяем, зарегистрировано ли поле как вычисляемое
        if (computedFieldsLibrary.isRegistered(fieldName)) {
            try {
                return computedFieldsLibrary.evaluate(fieldName, values);
            } catch (Exception e) {
                log.warn("Ошибка вычисления системного поля '{}': {}", fieldName, e.getMessage());
                return null;
            }
        }

        return null;
    }
}

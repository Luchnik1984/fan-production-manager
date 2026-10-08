package com.fanproduction.template.formula.function;

import com.fanproduction.template.formula.exception.FormulaException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр функций DSL.
 * <p>
 * Содержит 25 встроенных функций (математические, строковые,
 * логические, проверки, MAP).
 * <p>
 * Регистрация через {@code @PostConstruct}. Расширение — через
 * {@link #register(String, FormulaFunction)}.
 */
@Slf4j
@Component
public class FormulaFunctions {

    private final Map<String, FormulaFunction> functions = new ConcurrentHashMap<>();

    // ==========================================================
    // РЕГИСТРАЦИЯ
    // ==========================================================

    @PostConstruct
    public void init() {
        // ==================== Математические (7) ====================

        register("DIVIDE", args -> {
            Double a = toNumber(requireArg(args, 0, "DIVIDE"));
            Double b = toNumber(requireArg(args, 1, "DIVIDE"));
            if (a == null || b == null) return null;
            if (b == 0) throw new FormulaException("Деление на ноль");
            return a / b;
        });

        register("MULTIPLY", args -> {
            Double a = toNumber(requireArg(args, 0, "MULTIPLY"));
            Double b = toNumber(requireArg(args, 1, "MULTIPLY"));
            if (a == null || b == null) return null;
            return a * b;
        });

        register("ADD", args -> {
            Double a = toNumber(requireArg(args, 0, "ADD"));
            Double b = toNumber(requireArg(args, 1, "ADD"));
            if (a == null || b == null) return null;
            return a + b;
        });

        register("SUBTRACT", args -> {
            Double a = toNumber(requireArg(args, 0, "SUBTRACT"));
            Double b = toNumber(requireArg(args, 1, "SUBTRACT"));
            if (a == null || b == null) return null;
            return a - b;
        });

        register("FLOOR", args -> {
            Double x = toNumber(requireArg(args, 0, "FLOOR"));
            if (x == null) return null;
            return Math.floor(x);
        });

        register("CEIL", args -> {
            Double x = toNumber(requireArg(args, 0, "CEIL"));
            if (x == null) return null;
            return Math.ceil(x);
        });

        register("ROUND", args -> {
            Double x = toNumber(requireArg(args, 0, "ROUND"));
            if (x == null) return null;
            return (double) Math.round(x);
        });

        // ==================== Строковые (5) ====================

        register("CONCAT", args -> {
            StringBuilder sb = new StringBuilder();
            for (Object arg : args) {
                String s = toStringValue(arg);
                sb.append(s != null ? s : "");
            }
            return sb.toString();
        });

        register("UPPER", args -> {
            String s = toStringValue(requireArg(args, 0, "UPPER"));
            return s != null ? s.toUpperCase() : null;
        });

        register("LOWER", args -> {
            String s = toStringValue(requireArg(args, 0, "LOWER"));
            return s != null ? s.toLowerCase() : null;
        });

        register("TRIM", args -> {
            String s = toStringValue(requireArg(args, 0, "TRIM"));
            return s != null ? s.trim() : null;
        });

        register("FORMAT_NUMBER", args -> {
            Double x = toNumber(requireArg(args, 0, "FORMAT_NUMBER"));
            if (x == null) return null;
            return formatNumber(x);
        });

        // ==================== Логические (8) ====================

        register("IF", args -> {
            if (args.size() != 3) {
                throw new FormulaException("IF требует 3 аргумента");
            }
            Boolean cond = toBoolean(args.get(0));
            return Boolean.TRUE.equals(cond) ? args.get(1) : args.get(2);
        });

        register("EQUALS", args -> {
            Object a = requireArg(args, 0, "EQUALS");
            Object b = requireArg(args, 1, "EQUALS");
            return valuesEqual(a, b);
        });

        register("NOT_EQUALS", args -> {
            Object a = requireArg(args, 0, "NOT_EQUALS");
            Object b = requireArg(args, 1, "NOT_EQUALS");
            return !valuesEqual(a, b);
        });

        register("GREATER", args -> {
            Double a = toNumber(requireArg(args, 0, "GREATER"));
            Double b = toNumber(requireArg(args, 1, "GREATER"));
            if (a == null || b == null) return false;
            return a > b;
        });

        register("LESS", args -> {
            Double a = toNumber(requireArg(args, 0, "LESS"));
            Double b = toNumber(requireArg(args, 1, "LESS"));
            if (a == null || b == null) return false;
            return a < b;
        });

        register("AND", args -> {
            for (Object arg : args) {
                if (!Boolean.TRUE.equals(toBoolean(arg))) {
                    return false;
                }
            }
            return true;
        });

        register("OR", args -> {
            for (Object arg : args) {
                if (Boolean.TRUE.equals(toBoolean(arg))) {
                    return true;
                }
            }
            return false;
        });

        register("NOT", args -> {
            Object arg = requireArg(args, 0, "NOT");
            return !Boolean.TRUE.equals(toBoolean(arg));
        });

        // ==================== Проверки (4) ====================

        register("EMPTY", args -> {
            Object arg = args.isEmpty() ? null : args.get(0);
            return isEmpty(arg);
        });

        register("NOT_EMPTY", args -> {
            Object arg = args.isEmpty() ? null : args.get(0);
            return !isEmpty(arg);
        });

        register("DEFAULT", args -> {
            if (args.size() != 2) {
                throw new FormulaException("DEFAULT требует 2 аргумента");
            }
            Object value = args.get(0);
            return isEmpty(value) ? args.get(1) : value;
        });

        register("COALESCE", args -> {
            for (Object arg : args) {
                if (!isEmpty(arg)) {
                    return arg;
                }
            }
            return null;
        });

        // ==================== MAP (1) ====================

        register("MAP", args -> {
            if (args.size() != 2) {
                throw new FormulaException("MAP требует 2 аргумента: значение и словарь");
            }
            Object value = args.get(0);
            Object mapObj = args.get(1);

            if (!(mapObj instanceof Map<?, ?> map)) {
                throw new FormulaException("Второй аргумент MAP должен быть словарём");
            }

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (keysEqual(entry.getKey(), value)) {
                    return entry.getValue();
                }
            }
            return null;
        });

        log.info("FormulaFunctions инициализирован: {} функций", functions.size());
    }

    // ==========================================================
    // ПУБЛИЧНЫЕ МЕТОДЫ
    // ==========================================================

    /**
     * Вызвать функцию по имени.
     *
     * @throws FormulaException если функция не зарегистрирована
     */
    public Object call(String name, List<Object> arguments) {
        FormulaFunction fn = functions.get(name);
        if (fn == null) {
            throw new FormulaException("Неизвестная функция: '" + name + "'");
        }

        try {
            return fn.apply(arguments);
        } catch (FormulaException e) {
            throw e;
        } catch (Exception e) {
            throw new FormulaException(
                    "Ошибка выполнения функции '" + name + "': " + e.getMessage(), e);
        }
    }

    public boolean isRegistered(String name) {
        return functions.containsKey(name);
    }

    public Set<String> getRegisteredNames() {
        return Collections.unmodifiableSet(functions.keySet());
    }

    public void register(String name, FormulaFunction fn) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя функции не может быть пустым");
        }
        if (fn == null) {
            throw new IllegalArgumentException("Функция не может быть null");
        }
        functions.put(name, fn);
        log.debug("Зарегистрирована функция: {}", name);
    }

    public boolean unregister(String name) {
        return functions.remove(name) != null;
    }

    // ==========================================================
    // УТИЛИТЫ (приведение типов)
    // ==========================================================

    private static Object requireArg(List<Object> args, int index, String functionName) {
        if (index >= args.size()) {
            throw new FormulaException(
                    "Функция " + functionName + " требует минимум "
                            + (index + 1) + " аргументов");
        }
        return args.get(index);
    }

    /**
     * Привести значение к {@link Double}. Возвращает {@code null} при неудаче.
     */
    static Double toNumber(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof String s) {
            String trimmed = s.trim().replace(',', '.');
            if (trimmed.isEmpty()) return null;
            try {
                return Double.parseDouble(trimmed);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Привести значение к {@link Boolean}. {@code null} → {@code false}.
     */
    static Boolean toBoolean(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.doubleValue() != 0;
        if (value instanceof String s) return Boolean.parseBoolean(s.trim());
        return false;
    }

    /**
     * Привести значение к {@link String}. {@code null} → {@code null}.
     */
    public static String toStringValue(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) {
            double d = n.doubleValue();
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d);
            }
            return String.valueOf(d);
        }
        return value.toString();
    }

    /**
     * Проверка на «пустое» значение: {@code null}, пустая строка, пустая коллекция.
     */
    static boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String s) return s.isEmpty();
        if (value instanceof java.util.Collection<?> c) return c.isEmpty();
        return false;
    }

    /**
     * Сравнить два значения с учётом числового/строкового представления.
     */
    static boolean valuesEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        // Числовое сравнение, если оба — числа
        Double na = toNumber(a);
        Double nb = toNumber(b);
        if (na != null && nb != null) {
            return Double.compare(na, nb) == 0;
        }

        // Строковое сравнение
        return a.toString().equals(b.toString());
    }

    /**
     * Сравнить ключи MAP: числа — как числа, строки — как строки.
     */
    static boolean keysEqual(Object key, Object value) {
        return valuesEqual(key, value);
    }

    /**
     * Форматировать число: целое — без дроби, дробное — с запятой.
     */
    static String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        String s = String.valueOf(value);
        return s.replace('.', ',');
    }
}

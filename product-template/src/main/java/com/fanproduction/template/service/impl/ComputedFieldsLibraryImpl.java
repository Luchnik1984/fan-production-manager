package com.fanproduction.template.service.impl;

import com.fanproduction.template.service.ComputedField;
import com.fanproduction.template.service.ComputedFieldsLibrary;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация библиотеки вычисляемых полей.
 * <p>
 * 14 системных полей регистрируются в {@link #init()} через {@code @PostConstruct}.
 * Библиотека расширяема: новые поля добавляются через {@link #register(String, ComputedField)}.
 * <p>
 * Все вычисляемые поля возвращают {@code String}. Если поле не может быть
 * вычислено (нет входных данных), возвращается {@code null} или пустая строка.
 */
@Slf4j
@Service
public class ComputedFieldsLibraryImpl implements ComputedFieldsLibrary {

    private final Map<String, ComputedField> fields = new ConcurrentHashMap<>();

    // ==========================================================
    // РЕГИСТРАЦИЯ СИСТЕМНЫХ ПОЛЕЙ
    // ==========================================================

    @PostConstruct
    public void init() {
        // ===== Маркировка размера колеса =====
        register("wheelSizeMarking", values -> {
            Double wheelSize = toDouble(values.get("wheelSize"));
            if (wheelSize == null) return null;
            return String.valueOf((int) Math.floor(wheelSize / 10));
        });

        // ===== Код напряжения =====
        register("voltageCode", values -> {
            Integer voltage = toInteger(values.get("voltage"));
            if (voltage == null) return null;
            return switch (voltage) {
                case 220 -> "E";
                case 380 -> "D";
                default -> null;
            };
        });

        // ===== Маркировка исполнения =====
        register("executionMarking", values -> {
            Boolean generalPurpose = toBoolean(values.get("generalPurpose"));
            Boolean fireproof = toBoolean(values.get("fireproof"));
            Boolean explosionProof = toBoolean(values.get("explosionProof"));

            if (Boolean.TRUE.equals(generalPurpose)) return "C";
            if (Boolean.TRUE.equals(fireproof)) {
                String marking = toStringValue(values.get("fireproofMarking"));
                return (marking != null && !marking.isEmpty()) ? marking : "F-2/400";
            }
            if (Boolean.TRUE.equals(explosionProof)) {
                String marking = toStringValue(values.get("explosionMarking"));
                return (marking != null && !marking.isEmpty()) ? marking : "1Ex d IIC T4 Gb";
            }
            return "";
        });

        // ===== Форматирование размера =====
        register("sizeMarking", values -> {
            Double size = toDouble(values.get("size"));
            if (size == null) return null;
            return formatNumber(size);
        });

        // ===== Форматирование мощности =====
        register("powerMarking", values -> {
            Double power = toDouble(values.get("powerKw"));
            if (power == null) return null;
            return formatNumber(power);
        });

        // ===== Маркировка лопаток =====
        register("bladeMarking", values -> {
            Integer bladeCount = toInteger(values.get("bladeCount"));
            Integer bladeSlots = toInteger(values.get("bladeSlots"));
            if (bladeCount == null || bladeSlots == null) return null;

            StringBuilder sb = new StringBuilder();
            sb.append(bladeCount).append("/").append(bladeSlots);

            Boolean hasHa = toBoolean(values.get("hasHa"));
            Boolean hasCa = toBoolean(values.get("hasCa"));
            if (Boolean.TRUE.equals(hasHa)) sb.append("HA");
            if (Boolean.TRUE.equals(hasCa)) sb.append("CA");

            return sb.toString();
        });

        // ===== Диаметр колеса (осевое) =====
        register("wheelDiameter", values -> {
            Double size = toDouble(values.get("size"));
            Double trim = toDouble(values.get("trimCoefficient"));
            if (size == null) return null;
            double trimValue = trim != null ? trim : 0.0;
            long diameter = Math.round(size * (100 - trimValue));
            return String.valueOf(diameter);
        });

        // ===== Формула осевого колеса =====
        register("wheelFormulaAxial", values -> {
            String diameter = toStringValue(values.get("wheelDiameter"));
            String bladeCount = toStringValue(values.get("bladeCount"));
            String maxBladeCount = toStringValue(values.get("maxBladeCount"));
            String bladeName = toStringValue(values.get("bladeName"));
            String bladeAngle = toStringValue(values.get("bladeAngle"));
            String bladeMaterial = toStringValue(values.get("bladeMaterial"));

            if (isEmpty(diameter) || isEmpty(bladeCount) || isEmpty(maxBladeCount)
                    || isEmpty(bladeName) || isEmpty(bladeAngle) || isEmpty(bladeMaterial)) {
                return null;
            }

            return diameter + "/" + bladeCount + "-" + maxBladeCount
                    + "/" + bladeName + "/" + bladeAngle + "/" + bladeMaterial;
        });

        // ===== Формула радиального колеса =====
        register("wheelFormulaRadial", values -> {
            String bladeType = toStringValue(values.get("bladeType"));
            String wheelCode = toStringValue(values.get("wheelCode"));
            String frontDiskMod = toStringValue(values.get("frontDiskMod"));
            String wheelWidth = toStringValue(values.get("wheelWidth"));
            String bladeCount = toStringValue(values.get("bladeCount"));
            String bladeLengthCoeff = toStringValue(values.get("bladeLengthCoeff"));

            if (isEmpty(bladeType) || isEmpty(wheelCode) || isEmpty(frontDiskMod)
                    || isEmpty(wheelWidth) || isEmpty(bladeCount) || isEmpty(bladeLengthCoeff)) {
                return null;
            }

            // Формат: N.14/B.027/6/1.03
            return bladeType + "." + wheelCode
                    + "/" + frontDiskMod + "." + wheelWidth
                    + "/" + bladeCount + "/" + bladeLengthCoeff;
        });

        // ===== Маркировка огнестойкости =====
        register("fireproofMarking", values -> {
            Boolean fireproof = toBoolean(values.get("fireproof"));
            if (!Boolean.TRUE.equals(fireproof)) return null;

            String time = toStringValue(values.get("fireproofTime"));
            String temp = toStringValue(values.get("maxTemperature"));
            if (isEmpty(temp)) temp = "400";

            StringBuilder sb = new StringBuilder("F");
            if (!isEmpty(time)) sb.append("-").append(time);
            sb.append("/").append(temp);
            return sb.toString();
        });

        // ===== Маркировка взрывозащиты =====
        register("explosionMarking", values -> {
            Boolean explosionProof = toBoolean(values.get("explosionProof"));
            if (!Boolean.TRUE.equals(explosionProof)) return null;

            String marking = toStringValue(values.get("explosionMarking"));
            return (isEmpty(marking)) ? "1Ex d IIC T4 Gb" : marking;
        });

        // ===== Климатическое исполнение =====
        register("climateTypeMarking", values -> {
            String climate = toStringValue(values.get("climateType"));
            return isEmpty(climate) ? null : climate;
        });

        // ===== Положение вентилятора =====
        register("positionMarking", values -> {
            String position = toStringValue(values.get("position"));
            return isEmpty(position) ? null : position;
        });

        // ===== Маркировка направляющего/спрямляющего аппарата =====
        register("connectionTypeMarking", values -> {
            Boolean hasHa = toBoolean(values.get("hasHa"));
            Boolean hasCa = toBoolean(values.get("hasCa"));

            if (!Boolean.TRUE.equals(hasHa) && !Boolean.TRUE.equals(hasCa)) return null;

            StringBuilder sb = new StringBuilder();
            if (Boolean.TRUE.equals(hasHa)) sb.append("HA");
            if (Boolean.TRUE.equals(hasCa)) sb.append("CA");
            return sb.toString();
        });

        log.info("ComputedFieldsLibrary инициализирована: {} полей", fields.size());
    }

    // ==========================================================
    // ПУБЛИЧНЫЕ МЕТОДЫ
    // ==========================================================

    @Override
    public String evaluate(String name, Map<String, Object> values) {
        if (name == null || name.isBlank()) return null;

        ComputedField field = fields.get(name);
        if (field == null) {
            log.debug("Вычисляемое поле не зарегистрировано: {}", name);
            return null;
        }

        try {
            return field.compute(values != null ? values : Map.of());
        } catch (Exception e) {
            log.warn("Ошибка вычисления поля '{}': {}", name, e.getMessage());
            return null;
        }
    }

    @Override
    public boolean isRegistered(String name) {
        return name != null && fields.containsKey(name);
    }

    @Override
    public Set<String> getRegisteredNames() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    @Override
    public void register(String name, ComputedField field) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя вычисляемого поля не может быть пустым");
        }
        if (field == null) {
            throw new IllegalArgumentException("Функция вычисления не может быть null");
        }
        if (fields.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Вычисляемое поле '" + name + "' уже зарегистрировано");
        }
        fields.put(name, field);
        log.debug("Зарегистрировано вычисляемое поле: {}", name);
    }

    @Override
    public boolean unregister(String name) {
        if (name == null) return false;
        boolean removed = fields.remove(name) != null;
        if (removed) {
            log.debug("Удалено вычисляемое поле: {}", name);
        }
        return removed;
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ (приведение типов)
    // ==========================================================

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s.replace(',', '.'));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        if (value instanceof String s) {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Boolean toBoolean(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        if (value instanceof String s) return Boolean.parseBoolean(s);
        return null;
    }

    private String toStringValue(Object value) {
        return value != null ? value.toString().trim() : null;
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private String formatNumber(double value) {
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value).replace('.', ',');
    }
}

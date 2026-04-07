package com.fanproduction.gui.service;

import com.fanproduction.gui.dto.FieldMetadataDto;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Сервис для получения метаданных полей для разных типов карточек.
 */
@Service
public class FieldMetadataService {

    private final Map<String, List<FieldMetadataDto>> metadataMap = new HashMap<>();

    public FieldMetadataService() {
        initMetadata();
    }

    private void initMetadata() {
        // ========== Электродвигатель (MOTOR) ==========
        metadataMap.put("MOTOR", Arrays.asList(
                createField("motorType", "Тип двигателя", "text", true, "100L2", null, null, "Например: 100L, 132M"),
                createField("poles", "Количество полюсов", "number", true, "4", null, null, "2, 4, 6, 8"),
                createField("powerKw", "Мощность (КВт)", "double", true, null, null, null, "Например: 5,5"),
                createField("ratedSpeedRpm", "Номинальная скорость (об/мин)", "number", false, null, null, null, "6000 / полюсов"),
                createField("actualSpeedRpm", "Фактическая скорость (об/мин)", "number", false, null, null, null),
                createField("shaftSize", "Размер вала (мм)", "number", true, null, null, null),
                createField("mountingType", "Исполнение по монтажу", "text", true, null, null, null, "IM1081, IM3081, IM B14"),
                createField("climateType", "Климатическое исполнение", "combobox", true, "У1", new String[]{"У1", "У2", "УХЛ", "У3"}, null),
                createField("voltage", "Напряжение (В)", "number", true, "380", null, null, "220, 380, 660"),
                createField("operationMode", "Режим работы", "combobox", false, "S1", new String[]{"S1", "S2", "S3", "S4", "S5"}, null),
                createField("weightKg", "Масса (кг)", "double", false, null, null, null)
        ));

        // ========== Мотор-колесо (MOTOR_WHEEL) ==========
        metadataMap.put("MOTOR_WHEEL", Arrays.asList(
                createField("manufacturer", "Производитель", "text", false, null, null, null),
                createField("bladeType", "Тип лопаток", "combobox", true, "RO", new String[]{"RO", "RE"}, null, "RO - впередзагнутые, RE - назадзагнутые"),
                createField("size", "Размер", "number", true, null, null, null, "Например: 310"),
                createField("poles", "Количество полюсов", "number", true, "4", null, null),
                createField("voltageCode", "Код напряжения", "combobox", true, "D", new String[]{"E", "D"}, null, "E - 220В, D - 380В"),
                createField("powerKw", "Мощность (КВт)", "double", true, null, null, null),
                createField("ratedSpeedRpm", "Номинальная скорость", "number", false, null, null, null),
                createField("actualSpeedRpm", "Фактическая скорость", "number", false, null, null, null),
                createField("voltage", "Напряжение (В)", "number", true, "380", null, null),
                createField("weightKg", "Масса (кг)", "double", false, null, null, null)
        ));

        // ========== Радиальное колесо (RADIAL_WHEEL) ==========
        metadataMap.put("RADIAL_WHEEL", Arrays.asList(
                createField("manufacturer", "Производитель", "text", false, null, null, null),
                createField("marking", "Маркировка", "text", true, null, null, null, "КЦ-220 C1"),
                createField("bladeType", "Тип лопаток", "combobox", true, "назадзагнутые", new String[]{"впередзагнутые", "назадзагнутые"}, null),
                createField("size", "Размер колеса", "double", true, null, null, null),
                createField("hubType", "Ступица", "text", false, null, null, null, "SM 1610, BF 2012"),
                createField("maxSpeedRpm", "Макс. скорость (об/мин)", "number", false, null, null, null),
                createField("weightKg", "Масса (кг)", "double", false, null, null, null),
                createField("wheelFormula", "Формула колеса", "text", false, null, null, null),
                createField("bladeCount", "Количество лопаток", "number", false, null, null, null)
        ));

        // ========== Осевой вентилятор (AXIAL_FAN) ==========
        metadataMap.put("AXIAL_FAN", Arrays.asList(
                createField("size", "Типоразмер", "double", true, null, null, null, "5,6"),
                createField("seriesName", "Наименование серии", "text", true, "VO-PatAIR", null, null),
                createField("execution", "Исполнение", "text", false, null, null, null, "C, Ex, F"),
                createField("position", "Положение", "combobox", true, "Г", new String[]{"Г", "В", "С"}, null),
                createField("climateType", "Климатическое исполнение", "combobox", true, "У1", new String[]{"У1", "У2", "УХЛ"}, null),
                createField("motorId", "Электродвигатель", "reference", true, null, null, "MOTOR", "Выберите из базы"),
                createField("hubType", "Ступица", "text", false, null, null, null),
                createField("bladeCount", "Количество лопаток", "number", true, "9", null, null),
                createField("bladeSlots", "Посадочных мест", "number", true, "9", null, null),
                createField("bladeShape", "Форма лопатки", "combobox", true, "4Z", new String[]{"4Z", "5Z", "109_50", "76_14"}, null),
                createField("bladeAngle", "Угол установки", "number", true, "27", null, null),
                createField("cableSpec", "Кабель подключения", "text", false, null, null, null),
                createField("fanClass", "Класс", "combobox", true, "ОБЩЕОБМЕННЫЙ", new String[]{"ОБЩЕОБМЕННЫЙ", "ДЫМОУДАЛЕНИЕ"}, null)
        ));
    }

    private FieldMetadataDto createField(String name, String label, String type, boolean required,
                                         String defaultValue, String[] options, String referenceType, String hint) {
        FieldMetadataDto field = new FieldMetadataDto();
        field.setName(name);
        field.setLabel(label);
        field.setType(type);
        field.setRequired(required);
        field.setDefaultValue(defaultValue);
        field.setOptions(options);
        field.setReferenceType(referenceType);
        field.setHint(hint);
        return field;
    }

    private FieldMetadataDto createField(String name, String label, String type, boolean required,
                                         String defaultValue, String[] options, String referenceType) {
        return createField(name, label, type, required, defaultValue, options, referenceType, null);
    }

    /**
     * Получить метаданные для типа карточки
     */
    public List<FieldMetadataDto> getFieldsForType(String cardType) {
        return metadataMap.getOrDefault(cardType, Collections.emptyList());
    }

    /**
     * Получить все доступные типы карточек
     */
    public List<String> getAvailableCardTypes() {
        return new ArrayList<>(metadataMap.keySet());
    }
}
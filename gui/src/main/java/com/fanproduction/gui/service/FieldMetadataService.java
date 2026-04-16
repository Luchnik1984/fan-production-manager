package com.fanproduction.gui.service;

import com.fanproduction.gui.dto.metadata.FieldMetadataDto;
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
        List<FieldMetadataDto> motorFields = new ArrayList<>();

        // Серия (АИР, 5АИ, ВАО и т.д.)
        motorFields.add(createField("series", "Серия", "text", true, null, null, null, "АИР, 5АИ, ВАО..."));

        // Тип двигателя (100L, 112M и т.д.)
        motorFields.add(createField("motorType", "Тип двигателя", "text", true, null, null, null, "100L, 112M, 132S..."));

        // Количество полюсов (выпадающий список)
        motorFields.add(createField("poles", "Количество полюсов", "combobox", true, "4",
                new String[]{"2", "4", "6", "8", "10", "12"}, null, "2, 4, 6, 8, 10, 12"));

        // Мощность (КВт)
        motorFields.add(createField("powerKw", "Мощность (КВт)", "double", true, null, null, null, "5,5"));

        // Номинальная скорость (рассчитывается автоматически, но можно редактировать)
        motorFields.add(createField("ratedSpeedRpm", "Номинальная скорость (об/мин)", "number", false, null, null, null, "6000 / полюсов"));

        // Фактическая скорость
        motorFields.add(createField("actualSpeedRpm", "Фактическая скорость (об/мин)", "number", false, null, null, null));

        // Размер вала
        motorFields.add(createField("shaftSize", "Размер вала (мм)", "number", true, null, null, null, "28, 38, 48..."));

        // Исполнение по монтажу
        motorFields.add(createField("mountingType", "Исполнение по монтажу", "text", true, null, null, null, "IM1081, IM3081, IM B14..."));

        // Климатическое исполнение (просто поле ввода)
        motorFields.add(createField("climateType", "Климатическое исполнение и категория размещения", "text", true, "У1", null, null, "У1, У2, УХЛ1, Т2, О2, М1..."));

        // Напряжение (выпадающий список)
        motorFields.add(createField("voltage", "Напряжение (В)", "combobox", true, "380", new String[]{"220", "380", "660"}, null, "220, 380, 660"));

        // Режим работы (выпадающий список S1-S9)
        motorFields.add(createField("operationMode", "Режим работы", "combobox", false, "S1",
                new String[]{"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9"}, null, "S1-S9"));

        // Масса
        motorFields.add(createField("weightKg", "Масса (кг)", "double", false, null, null, null));

        // Галочка "Общего применения"
        motorFields.add(createField("generalPurpose", "Общего применения", "boolean", false, "true", null, null));

        // Галочка "Огнестойкость"
        motorFields.add(createField("fireproof", "Огнестойкость", "boolean", false, "false", null, null));

        // Предельная температура (появляется при огнестойком)
//        motorFields.add(createField("maxTemperature", "Предельная температура (°C)", "number", false, null, null, null, "появляется при выборе Огнестойкость", false));

        motorFields.add(createField("fireproofMarking", "Маркировка огнестойкости", "text", false, "FR400", null, null, "FR400", false));

        // Галочка "Взрывозащита"
        motorFields.add(createField("explosionProof", "Взрывозащита", "boolean", false, "false", null, null));

        // Маркировка взрывозащиты (появляется при взрывозащищённом)
        motorFields.add(createField("explosionMarking", "Маркировка взрывозащиты", "text", false, "1Ex db IIC T4 Gb", null, null, "1Ex db IIC T4 Gb", false));

        // Полная маркировка (редактируемое поле, формируется автоматически)
        motorFields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически, можно редактировать"));

        metadataMap.put("MOTOR", motorFields);

        // ========== Мотор-колесо (MOTOR_WHEEL) ==========
        List<FieldMetadataDto> motorWheelFields = new ArrayList<>();

        // Производитель
        motorWheelFields.add(createField("manufacturer", "Производитель", "text", false, null, null, null, "Например: Siemens, ABB"));

        // Тип лопаток (выпадающий список)
        motorWheelFields.add(createField("bladeType", "Тип лопаток", "combobox", true, "впередзагнутые",
                new String[]{"впередзагнутые", "назадзагнутые"}, null, "впередзагнутые / назадзагнутые"));

        // Размер
        motorWheelFields.add(createField("size", "Размер", "number", true, null, null, null, "Размер в мм: 310, 400..."));

        // Количество полюсов
        motorWheelFields.add(createField("poles", "Количество полюсов", "combobox", true, "4",
                new String[]{"2", "4", "6", "8", "10", "12"}, null, "2, 4, 6, 8, 10, 12"));

        // Код напряжения (выпадающий список)
        motorWheelFields.add(createField("voltageCode", "Код напряжения", "combobox", true, "D",
                new String[]{"E", "D"}, null, "E - 220В, D - 380В"));

        // Напряжение (заполняется автоматически)
        motorWheelFields.add(createField("voltage", "Напряжение (В)", "number", true, null, null, null, "заполняется автоматически из кода"));

        // Мощность
        motorWheelFields.add(createField("powerKw", "Мощность (КВт)", "double", true, null, null, null, "5,5"));

        // Номинальная скорость
        motorWheelFields.add(createField("ratedSpeedRpm", "Номинальная скорость (об/мин)", "number", false, null, null, null, "6000 / полюсов"));

        // Фактическая скорость
        motorWheelFields.add(createField("actualSpeedRpm", "Фактическая скорость (об/мин)", "number", false, null, null, null));

        // Масса
        motorWheelFields.add(createField("weightKg", "Масса (кг)", "double", false, null, null, null));

        // Код двигателя
        motorWheelFields.add(createField("motorCode", "Код двигателя", "text", false, null, null, null, "AC0E, QW1a"));

        // Полная маркировка (формируется автоматически)
        motorWheelFields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически, можно редактировать"));

        metadataMap.put("MOTOR_WHEEL", motorWheelFields);


        // ========== Колесо радиальное (RADIAL_WHEEL) ==========
        List<FieldMetadataDto> radialWheelFields = new ArrayList<>();

        // Производитель
        radialWheelFields.add(createField("manufacturer", "Производитель", "text", false, null, null, null, "Например: Siemens, ABB"));

        // Маркировка колеса
        radialWheelFields.add(createField("marking", "Маркировка колеса", "text", true, null, null, null, "КЦ-220"));

        // Тип лопаток (выпадающий список)
        radialWheelFields.add(createField("bladeType", "Тип лопаток", "combobox", true, "назадзагнутые",
                new String[]{"впередзагнутые", "назадзагнутые"}, null, "впередзагнутые / назадзагнутые"));

        // Модификация лопатки (новое поле)
        radialWheelFields.add(createField("bladeMod", "Модификация лопатки", "text", false, null, null, null, "1.05, 14, 12U"));

        // Размер колеса
        radialWheelFields.add(createField("size", "Размер колеса", "double", true, null, null, null, "5,6"));

        // Формула колеса
        radialWheelFields.add(createField("wheelFormula", "Формула колеса", "text", false, null, null, null, "5,6_B14"));

        // Количество лопаток
        radialWheelFields.add(createField("bladeCount", "Количество лопаток", "number", false, null, null, null, "6, 7, 9"));

        // Ступица
        radialWheelFields.add(createField("hubType", "Ступица", "text", false, null, null, null, "SM1610, BF2012"));

        // Максимальная скорость вращения
        radialWheelFields.add(createField("maxSpeedRpm", "Макс. скорость (об/мин)", "number", false, null, null, null));

        // Масса
        radialWheelFields.add(createField("weightKg", "Масса (кг)", "double", false, null, null, null));

        // Галочка "Общего применения"
        radialWheelFields.add(createField("generalPurpose", "Общего применения", "boolean", false, "true", null, null));

        // Галочка "Огнестойкость"
        radialWheelFields.add(createField("fireproof", "Огнестойкость", "boolean", false, "false", null, null));

        // Маркировка огнестойкости (скрыто по умолчанию)
        radialWheelFields.add(createField("fireproofMarking", "Маркировка огнестойкости", "text", false, "F400", null, null, "F400"));

        // Галочка "Взрывозащита"
        radialWheelFields.add(createField("explosionProof", "Взрывозащита", "boolean", false, "false", null, null));

        // Поле маркировки взрывозащиты (скрыто по умолчанию)
        radialWheelFields.add(createField("explosionMarking", "Маркировка взрывозащиты", "text", false, "1Ex db IIC T4 Gb", null, null, "1Ex db IIC T4 Gb"));

        // Полная маркировка (формируется автоматически)
        radialWheelFields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически, можно редактировать"));

        metadataMap.put("RADIAL_WHEEL", radialWheelFields);

        // ========== Осевое колесо (AXIAL_WHEEL) ==========
        List<FieldMetadataDto> axialWheelFields = new ArrayList<>();

        axialWheelFields.add(createField("manufacturer", "Производитель", "text", false, null, null, null));
        axialWheelFields.add(createField("marking", "Маркировка колеса", "text", false, null, null, null, "Заполняется только для покупного колеса"));

        axialWheelFields.add(createField("bladeType", "Тип лопаток", "text", true, null, null, null, "4Z, 5Z, 109_50, 76_14"));
        axialWheelFields.add(createField("size", "Типоразмер", "double", true, null, null, null, "6,3"));
        axialWheelFields.add(createField("trimCoefficient", "Коэффициент подрезки (%)", "double", false, "0", null, null, "1,00"));

        axialWheelFields.add(createField("hubType", "Ступица", "text", false, null, null, null, "SM1610, BF2012"));

        axialWheelFields.add(createField("bladeCount", "Количество лопаток", "number", true, null, null, null));
        axialWheelFields.add(createField("bladeSlots", "Посадочных мест", "number", true, null, null, null));
        axialWheelFields.add(createField("bladeAngle", "Угол установки", "number", true, null, null, null, "27"));
        axialWheelFields.add(createField("bladeMaterial", "Материал лопатки", "combobox", true, "PAG", new String[]{"PAG", "ST"}, null));

        axialWheelFields.add(createField("wheelDiameter", "Диаметр колеса (мм)", "number", false, null, null, null, "рассчитывается автоматически"));
        axialWheelFields.add(createField("wheelFormula", "Формула колеса", "text", false, null, null, null, "формируется автоматически"));

        // Специальные поля
        axialWheelFields.add(createField("generalPurpose", "Общего применения", "boolean", false, "true", null, null));
        axialWheelFields.add(createField("fireproof", "Огнестойкость", "boolean", false, "false", null, null));
        axialWheelFields.add(createField("fireproofMarking", "Маркировка огнестойкости", "text", false, "F400", null, null, "F400"));
        axialWheelFields.add(createField("explosionProof", "Взрывозащита", "boolean", false, "false", null, null));
        axialWheelFields.add(createField("explosionMarking", "Маркировка взрывозащиты", "text", false, "1Ex db IIC T4 Gb", null, null, "1Ex db IIC T4 Gb"));
//        axialWheelFields.add(createField("maxTemperature", "Предельная температура (°C)", "number", false, null, null, null, "появляется при выборе Огнестойкость", false));
        axialWheelFields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически, можно редактировать"));

        metadataMap.put("AXIAL_WHEEL", axialWheelFields);


        // ========== Вентилятор осевой (AXIAL_FAN) ==========
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
                                         String defaultValue, String[] options, String referenceType,
                                         String hint, boolean visible) {
        FieldMetadataDto field = new FieldMetadataDto();
        field.setName(name);
        field.setLabel(label);
        field.setType(type);
        field.setRequired(required);
        field.setDefaultValue(defaultValue);
        field.setOptions(options);
        field.setReferenceType(referenceType);
        field.setHint(hint);
        field.setVisible(visible);
        return field;
    }

    private FieldMetadataDto createField(String name, String label, String type, boolean required,
                                         String defaultValue, String[] options, String referenceType, String hint) {
        return createField(name, label, type, required, defaultValue, options, referenceType, hint, true);
    }

    private FieldMetadataDto createField(String name, String label, String type, boolean required,
                                         String defaultValue, String[] options, String referenceType) {
        return createField(name, label, type, required, defaultValue, options, referenceType, null, true);
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
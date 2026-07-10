package com.fanproduction.gui.service;

import com.fanproduction.gui.dto.metadata.FieldMetadataDto;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Сервис для получения метаданных полей для разных типов карточек.
 * Используется для динамического построения форм в UI.
 */
@Service
public class FieldMetadataService {

    private final Map<String, List<FieldMetadataDto>> metadataMap = new HashMap<>();

    public FieldMetadataService() {
        initMetadata();
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ СОЗДАНИЯ ПОЛЕЙ
    // ==========================================================

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

    private FieldMetadataDto createSeparator(String title) {
        FieldMetadataDto field = new FieldMetadataDto();
        field.setName("separator_" + System.currentTimeMillis());
        field.setLabel(title);
        field.setType("separator");
        field.setRequired(false);
        field.setVisible(true);
        return field;
    }

    private FieldMetadataDto createHiddenSeparator(String title) {
        FieldMetadataDto field = createSeparator(title);
        field.setVisible(false);
        return field;
    }

    private FieldMetadataDto createReadOnlyField(String name, String label, String type,
                                                 String defaultValue, String hint) {
        FieldMetadataDto field = createField(name, label, type, false, defaultValue, null, null, hint);
        field.setReadOnly(true);
        return field;
    }

    private FieldMetadataDto createHiddenField(String name, String label, String type, boolean required,
                                               String defaultValue, String[] options, String referenceType,
                                               String hint) {
        FieldMetadataDto field = createField(name, label, type, required, defaultValue, options, referenceType, hint);
        field.setVisible(false);
        return field;
    }

    private FieldMetadataDto createHiddenReadOnlyField(String name, String label, String type,
                                                       String defaultValue, String hint) {
        FieldMetadataDto field = createReadOnlyField(name, label, type, defaultValue, hint);
        field.setVisible(false);
        return field;
    }

    private FieldMetadataDto createHiddenField(String name, String label, String type, boolean required,
                                               String defaultValue, String[] options, String referenceType) {
        return createHiddenField(name, label, type, required, defaultValue, options, referenceType, null);
    }

    /**
     * Создаёт поля для секции "Исполнение" (общего применения, огнестойкость, взрывозащита)
     */
    private List<FieldMetadataDto> createExecutionFields() {
        List<FieldMetadataDto> fields = new ArrayList<>();
        fields.add(createSeparator("Исполнение"));
        fields.add(createField("generalPurpose", "Общего применения", "boolean", false, "true", null, null));
        fields.add(createField("fireproof", "Огнестойкость", "boolean", false, "false", null, null));
        fields.add(createHiddenField("fireproofTime", "Время огнестойкости (часы)", "number", false, null, null, null, "Укажите кол-во часов (необязательное поле)"));
        fields.add(createHiddenField("maxTemperature", "Предельная температура (°C)", "number", false, "400", null, null, "Введите температуру"));
        fields.add(createHiddenField("fireproofMarking", "Маркировка огнестойкости", "text", false, null, null, null, "формируется автоматически"));
        fields.add(createField("explosionProof", "Взрывозащита", "boolean", false, "false", null, null));
        fields.add(createHiddenField("explosionMarking", "Маркировка взрывозащиты", "text", false, "1Ex d IIC T4 Gb", null, null, "можно редактировать"));
        return fields;
    }

    /**
     * Создаёт общие поля (масса, макс. скорость)
     */
    private List<FieldMetadataDto> createCommonFields() {
        List<FieldMetadataDto> fields = new ArrayList<>();
        fields.add(createField("maxSpeedRpm", "Максимальная скорость вращения (об/мин)", "number", false, null, null, null, "Введите число"));
        fields.add(createField("weightKg", "Масса (кг)", "double", false, null, null, null, "Введите число"));
        return fields;
    }

    /**
     * Создаёт поля для полной маркировки
     */
    private List<FieldMetadataDto> createFullMarkingField() {
        List<FieldMetadataDto> fields = new ArrayList<>();
        fields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически, можно редактировать"));
        return fields;
    }

    // ==========================================================
    // ИНИЦИАЛИЗАЦИЯ МЕТАДАННЫХ
    // ==========================================================

    private void initMetadata() {
        initMotorMetadata();
        initMotorWheelMetadata();
        initRadialWheelMetadata();
        initAxialWheelMetadata();
        initDuctFanMetadata();
        initRoofLowProfileFanMetadata();
        initRoofRadialFanMetadata();
        initAxialFanMetadata();
        // Остальные карточки (CUP, ACCESSORY, ROOF_AXIAL_FAN) можно добавить позже
    }

    // ==========================================================
    // МЕТОДЫ ИНИЦИАЛИЗАЦИИ ДЛЯ КАЖДОГО ТИПА
    // ==========================================================

    private void initMotorMetadata() {
        List<FieldMetadataDto> fields = new ArrayList<>();

        fields.add(createField("series", "Серия", "text", true, null, null, null, "АИР, 5АИ, ВАО..."));
        fields.add(createField("motorType", "Тип двигателя", "text", true, null, null, null, "100L, 112M, 132S..."));
        fields.add(createField("poles", "Количество полюсов", "combobox", true, "4",
                new String[]{"2", "4", "6", "8", "10", "12"}, null, "2, 4, 6, 8, 10, 12"));
        fields.add(createField("powerKw", "Мощность (КВт)", "double", true, null, null, null, "5,5"));
        fields.add(createField("ratedSpeedRpm", "Номинальная скорость (об/мин)", "number", false, null, null, null, "6000 / полюсов"));
        fields.add(createField("actualSpeedRpm", "Фактическая скорость (об/мин)", "number", false, null, null, null));
        fields.add(createField("shaftSize", "Размер вала (мм)", "number", true, null, null, null, "28, 38, 48..."));
        fields.add(createField("mountingType", "Исполнение по монтажу", "text", true, null, null, null, "IM1081, IM3081, IM B14..."));
        fields.add(createField("climateType", "Климатическое исполнение", "text", true, "У1", null, null, "У1, У2, УХЛ1, Т2, О2, М1..."));
        fields.add(createField("voltage", "Напряжение (В)", "combobox", true, "380", new String[]{"220", "380", "660"}, null, "220, 380, 660"));
        fields.add(createField("operationMode", "Режим работы", "combobox", false, "S1",
                new String[]{"S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9"}, null, "S1-S9"));
        fields.add(createField("weightKg", "Масса (кг)", "double", false, null, null, null));
        fields.add(createField("generalPurpose", "Общего применения", "boolean", false, "true", null, null));
        fields.add(createField("fireproof", "Огнестойкость", "boolean", false, "false", null, null));
        fields.add(createHiddenField("fireproofMarking", "Маркировка огнестойкости", "text", false, "FR400", null, null, "FR400"));
        fields.add(createField("explosionProof", "Взрывозащита", "boolean", false, "false", null, null));
        fields.add(createHiddenField("explosionMarking", "Маркировка взрывозащиты", "text", false, "1Ex d IIC T4 Gb", null, null, "1Ex d IIC T4 Gb"));
        fields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически, можно редактировать"));

        metadataMap.put("MOTOR", fields);
    }

    private void initMotorWheelMetadata() {
        List<FieldMetadataDto> fields = new ArrayList<>();

        fields.add(createField("manufacturer", "Производитель", "text", false, null, null, null, "Например: Siemens, ABB"));
        fields.add(createField("manufacturerMarking", "Маркировка производителя", "text", true, null, null, null, "Например: RE280F-4D-AC0E или DYF4D-280-QW1a"));
        fields.add(createField("bladeType", "Тип лопаток", "combobox", true, "впередзагнутые",
                new String[]{"впередзагнутые", "назадзагнутые"}, null, "впередзагнутые / назадзагнутые"));
        fields.add(createField("size", "Размер", "number", true, null, null, null, "Размер в мм: 310, 400..."));
        fields.add(createField("poles", "Количество полюсов", "combobox", true, "4",
                new String[]{"2", "4", "6", "8", "10", "12"}, null, "2, 4, 6, 8, 10, 12"));
        fields.add(createField("voltageCode", "Код напряжения", "combobox", true, "D",
                new String[]{"E", "D"}, null, "E - 220В, D - 380В"));
        fields.add(createField("voltage", "Напряжение (В)", "number", true, null, null, null, "заполняется автоматически из кода"));
        fields.add(createField("powerKw", "Мощность (КВт)", "double", true, null, null, null, "5,5"));
        fields.add(createField("ratedSpeedRpm", "Номинальная скорость (об/мин)", "number", false, null, null, null, "6000 / полюсов"));
        fields.add(createField("actualSpeedRpm", "Фактическая скорость (об/мин)", "number", false, null, null, null));
        fields.add(createField("weightKg", "Масса (кг)", "double", false, null, null, null));
        fields.add(createField("motorCode", "Код двигателя", "text", false, null, null, null, "AC0E, QW1a"));
        fields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически, можно редактировать"));

        metadataMap.put("MOTOR_WHEEL", fields);
    }

    private void initRadialWheelMetadata() {
        List<FieldMetadataDto> fields = new ArrayList<>();

        fields.add(createField("manufacturer", "Производитель", "text", false, null, null, null));
        fields.add(createField("size", "Размер колеса", "double", true, null, null, null, "Введите число, например 280"));
        fields.add(createField("isPartnerWheel", "Партнёрское рабочее колесо", "boolean", false, "false", null, null));
        fields.add(createHiddenField("marking", "Маркировка производителя", "text", true, null, null, null, "Например: КЦ-280-1610х28"));
        fields.add(createField("isOwnProduction", "Фирменное рабочее колесо", "boolean", false, "false", null, null));

        // Фирменное колесо (скрыто)
        fields.add(createHiddenField("series", "Серия колеса", "text", true, null, null, null, "Введите серию, например КЦ, РК"));
        fields.add(createHiddenField("bladeType", "Тип лопаток", "combobox", true, null,
                new String[]{"V", "N", "RO"}, null, "V - впередзагнутые / N - назадзагнутые / RO - радиальнооканчивающиеся"));
        fields.add(createHiddenField("hubComponentId", "Ступица", "selectable", false,
                null, null, "COMPONENT", "Выберите ступицу из базы компонентов"));
        fields.add(createHiddenReadOnlyField("hubName", "", "text", "", ""));
        fields.add(createHiddenField("bladeMod", "Модификация лопатки", "text", false, null, null, null, "Например 14; 12U"));
        fields.add(createHiddenField("frontDiskMod", "Модификация переднего диска", "text", false, null, null, null, "Например А; В"));
        fields.add(createHiddenField("wheelWidth", "Ширина колеса", "double", false, null, null, null, "Введите коэффициент, например 0.27"));
        fields.add(createHiddenField("bladeCount", "Количество лопаток", "number", false, null, null, null, "Введите число лопаток, например 6"));
        fields.add(createHiddenField("bladeLengthCoeff", "Коэффициент длины лопатки", "double", false, null, null, null, "Введите коэффициент, например 1.05"));
        fields.add(createHiddenField("wheelCode", "Код колеса", "text", false, null, null, null, "формируется автоматически из модификации лопатки"));
        fields.add(createHiddenField("wheelFormula", "Формула колеса", "text", false, null, null, null, "формируется автоматически, можно редактировать"));

        // Общие поля
        fields.addAll(createCommonFields());

        // Исполнение
        fields.addAll(createExecutionFields());

        // Полная маркировка
        fields.addAll(createFullMarkingField());

        metadataMap.put("RADIAL_WHEEL", fields);
    }

    private void initAxialWheelMetadata() {
        List<FieldMetadataDto> fields = new ArrayList<>();

        // Основная информация
        fields.add(createField("name", "Наименование", "text", true, "Колесо осевое", null, null, "Можно изменить при необходимости"));
        fields.add(createField("manufacturer", "Производитель", "text", false, null, null, null, "Например: Промпат, FogStream"));
        fields.add(createField("size", "Типоразмер", "double", true, null, null, null, "Например: 5,6; 6,3"));
        fields.add(createField("trimCoefficient", "Коэффициент подрезки (%)", "double", false, null, null, null, "Например: 1,00"));
        fields.add(createReadOnlyField("wheelDiameter", "Диаметр колеса (мм)", "number", null, "Рассчитывается автоматически: Типоразмер × (100 - Коэф. подрезки)"));

        // Тип колеса
        fields.add(createSeparator("Тип рабочего колеса"));
        fields.add(createField("isPartnerWheel", "Партнёрское рабочее колесо", "boolean", false, "false", null, null));
        fields.add(createHiddenField("marking", "Маркировка производителя", "text", true, null, null, null, "Например: PAG.400.6-3.P3HR.30.30.41-3"));
        fields.add(createField("isOwnProduction", "Фирменное рабочее колесо", "boolean", false, "false", null, null));

        // Фирменное колесо (скрыто)
        fields.add(createHiddenField("series", "Серия колеса", "text", true, null, null, null, "Например: AW, AWR"));
        fields.add(createHiddenSeparator("Тип изготовления"));
        fields.add(createHiddenField("isAssembledFromComponents", "Колесо сборное из компонентов", "boolean", false, "false", null, null));
        fields.add(createHiddenField("isWeldedFromMaterials", "Колесо сварное из материалов", "boolean", false, "false", null, null));

        // Хаб (сборный)
        fields.add(createHiddenField("wheelHubComponentId", "Ступица (Хаб) рабочего колеса", "selectable", true, null, null, "COMPONENT", "Выберите хаб из базы компонентов"));
        fields.add(createHiddenReadOnlyField("wheelHubName", "", "text", "", ""));

        // Хаб (сварной)
        fields.add(createHiddenField("wheelHubType", "Ступица (Хаб) рабочего колеса", "text", true, null, null, null, "введите тип хаба, например 109_50/6-6"));

        // Макс. количество лопаток
        fields.add(createHiddenField("maxBladeCount", "Максимально возможное количество лопаток для данного Хаба", "number", true, null, null, null, "Например: 9, 12"));

        // Лопатка (сборный)
        fields.add(createHiddenField("bladeComponentId", "Лопатка рабочего колеса", "selectable", true, null, null, "COMPONENT", "Выберите лопатку из базы компонентов"));
        fields.add(createHiddenReadOnlyField("bladeName", "", "text", "", ""));

        // Лопатка (сварной)
        fields.add(createHiddenField("bladeType", "Лопатка рабочего колеса", "text", true, null, null, null, "введите тип лопатки, например 109_50"));

        // Материал лопатки
        fields.add(createHiddenField("bladeMaterial", "Материал лопатки", "text", true, null, null, null, "Укажите условный материал лопатки, например: St или AISI"));

        // Количество лопаток
        fields.add(createHiddenField("bladeCount", "Количество установленных лопаток", "number", true, null, null, null, "Должно быть больше 1 и не превышать максимальное количество для хаба"));

        // Угол установки
        fields.add(createHiddenField("bladeAngle", "Угол установки лопаток", "number", true, null, null, null, "Например: 27, 30, 43"));

        // Установочная ступица
        fields.add(createHiddenField("hubComponentId", "Установочная ступица", "selectable", true, null, null, "COMPONENT", "Выберите ступицу из базы компонентов"));
        fields.add(createHiddenReadOnlyField("hubName", "", "text", "", ""));

        // Формула колеса
        fields.add(createHiddenField("wheelFormula", "Формула колеса", "text", true, null, null, null, "формируется автоматически, можно редактировать"));

        // Общие поля
        fields.addAll(createCommonFields());

        // Исполнение
        fields.addAll(createExecutionFields());

        // Полная маркировка
        fields.addAll(createFullMarkingField());

        metadataMap.put("AXIAL_WHEEL", fields);
    }

    private void initDuctFanMetadata() {
        List<FieldMetadataDto> fields = new ArrayList<>();

        fields.add(createField("seriesName", "Наименование серии", "text", true, "VRK-PatAIR", null, null, "VRK-PatAIR"));
        fields.add(createField("ductSize", "Типоразмер", "text", true, null, null, null, "40-20, 60-30"));
        fields.add(createField("executionType", "Исполнение", "combobox", true, "P",
                new String[]{"P", "PS", "PKV", "PRV"}, null, "P, PS, PKV, PRV"));
        fields.add(createField("ductFanType", "Тип колеса", "combobox", true, "MOTOR_WHEEL",
                new String[]{"MOTOR_WHEEL", "RADIAL_WHEEL"}, null, "Мотор-колесо / Радиальное колесо"));

        // Скрытые поля для выбора компонентов
        fields.add(createHiddenField("motorWheelId", "Мотор-колесо", "selectable", false, null, null, "MOTOR_WHEEL", "Выберите мотор-колесо"));
        fields.add(createHiddenField("radialWheelId", "Радиальное колесо", "selectable", false, null, null, "RADIAL_WHEEL", "Выберите радиальное колесо"));
        fields.add(createHiddenField("motorId", "Электродвигатель", "selectable", false, null, null, "MOTOR", "Выберите электродвигатель"));
        fields.add(createHiddenField("wheelSize", "Размер колеса", "number", false, null, null, null, "заполняется автоматически"));

        // Электрические параметры (скрыты до выбора)
        fields.add(createHiddenField("poles", "Количество полюсов", "number", false, null, null, null, "заполняется автоматически"));
        fields.add(createHiddenField("voltage", "Напряжение (В)", "number", false, null, null, null, "заполняется автоматически"));
        fields.add(createHiddenField("voltageCode", "Код напряжения", "text", false, null, null, null, "заполняется автоматически"));
        fields.add(createHiddenField("ratedSpeedRpm", "Номинальная скорость (об/мин)", "number", false, null, null, null, "заполняется автоматически"));
        fields.add(createHiddenField("actualSpeedRpm", "Фактическая скорость (об/мин)", "number", false, null, null, null, "можно изменить"));

        // Добавляем специальные поля (исполнение, маркировка)
        fields.addAll(getFanSpecialFields());

        metadataMap.put("DUCT_FAN", fields);
    }

    private void initRoofLowProfileFanMetadata() {
        List<FieldMetadataDto> fields = new ArrayList<>();

        fields.add(createField("seriesName", "Наименование серии", "text", true, "VR-PatAIR", null, null, "VR-PatAIR"));
        fields.add(createField("executionType", "Исполнение", "combobox", true, "KpM",
                new String[]{"KpM", "KpMS"}, null, "KpM, KpMS"));
        fields.add(createField("roofSize", "Типоразмер", "text", true, null, null, null, "40/31"));
        fields.add(createField("climateType", "Климатическое исполнение", "text", true, "У1", null, null, "У1, У2, УХЛ1"));
        fields.add(createField("motorWheelId", "Мотор-колесо", "reference", true, null, null, "MOTOR_WHEEL", "Выберите мотор-колесо"));
        fields.add(createField("poles", "Количество полюсов", "number", false, null, null, null, "заполняется автоматически"));
        fields.add(createField("voltage", "Напряжение (В)", "number", false, null, null, null, "заполняется автоматически"));

        // Специальные поля
        fields.addAll(getFanSpecialFields());

        metadataMap.put("ROOF_LOW_PROFILE_FAN", fields);
    }

    private void initRoofRadialFanMetadata() {
        List<FieldMetadataDto> fields = new ArrayList<>();

        fields.add(createField("seriesName", "Наименование серии", "text", true, "VR-PatAIR", null, null, "VR-PatAIR"));
        fields.add(createField("executionType", "Исполнение", "combobox", true, "KpR",
                new String[]{"KpR", "KpRS"}, null, "KpR, KpRS"));
        fields.add(createField("roofSize", "Типоразмер", "text", true, null, null, null, "40, 50"));
        fields.add(createField("climateType", "Климатическое исполнение", "text", true, "У1", null, null, "У1, У2, УХЛ1"));
        fields.add(createField("radialWheelId", "Радиальное колесо", "reference", true, null, null, "RADIAL_WHEEL", "Выберите радиальное колесо"));
        fields.add(createField("motorId", "Электродвигатель", "reference", true, null, null, "MOTOR", "Выберите электродвигатель"));
        fields.add(createField("poles", "Количество полюсов", "number", false, null, null, null, "заполняется автоматически"));
        fields.add(createField("voltage", "Напряжение (В)", "number", false, null, null, null, "заполняется автоматически"));
        fields.add(createField("voltageCode", "Код напряжения", "text", false, null, null, null, "заполняется автоматически"));
        fields.add(createField("ratedSpeedRpm", "Номинальная скорость (об/мин)", "number", false, null, null, null, "заполняется автоматически"));
        fields.add(createField("actualSpeedRpm", "Фактическая скорость (об/мин)", "number", false, null, null, null, "можно изменить"));

        // Специальные поля
        fields.addAll(getFanSpecialFields());

        metadataMap.put("ROOF_RADIAL_FAN", fields);
    }

    private void initAxialFanMetadata() {
        List<FieldMetadataDto> fields = new ArrayList<>();

        fields.add(createField("size", "Типоразмер", "double", true, null, null, null, "5,6"));
        fields.add(createField("seriesName", "Наименование серии", "text", true, "VO-PatAIR", null, null));
        fields.add(createField("execution", "Исполнение", "text", false, null, null, null, "C, Ex, F"));
        fields.add(createField("position", "Положение", "combobox", true, "Г", new String[]{"Г", "В", "С"}, null));
        fields.add(createField("climateType", "Климатическое исполнение", "combobox", true, "У1", new String[]{"У1", "У2", "УХЛ"}, null));
        fields.add(createField("motorId", "Электродвигатель", "reference", true, null, null, "MOTOR", "Выберите из базы"));
        fields.add(createField("hubType", "Ступица", "text", false, null, null, null));
        fields.add(createField("bladeCount", "Количество лопаток", "number", true, "9", null, null));
        fields.add(createField("bladeSlots", "Посадочных мест", "number", true, "9", null, null));
        fields.add(createField("bladeShape", "Форма лопатки", "combobox", true, "4Z", new String[]{"4Z", "5Z", "109_50", "76_14"}, null));
        fields.add(createField("bladeAngle", "Угол установки", "number", true, "27", null, null));
        fields.add(createField("cableSpec", "Кабель подключения", "text", false, null, null, null));
        fields.add(createField("fanClass", "Класс", "combobox", true, "ОБЩЕОБМЕННЫЙ", new String[]{"ОБЩЕОБМЕННЫЙ", "ДЫМОУДАЛЕНИЕ"}, null));

        // Специальные поля
        fields.addAll(getFanSpecialFields());

        metadataMap.put("AXIAL_FAN", fields);
    }

    // ==========================================================
    // ОБЩИЕ ПОЛЯ ДЛЯ ВЕНТИЛЯТОРОВ (ОГНЕСТОЙКОСТЬ, ВЗРЫВОЗАЩИТА)
    // ==========================================================

    private List<FieldMetadataDto> getFanSpecialFields() {
        List<FieldMetadataDto> fields = new ArrayList<>();

        fields.add(createField("powerKw", "Мощность (КВт)", "double", false, null, null, null, "заполняется автоматически"));
        fields.add(createField("generalPurpose", "Общего применения", "boolean", false, "true", null, null));
        fields.add(createField("fireproof", "Огнестойкость", "boolean", false, "false", null, null));
        fields.add(createHiddenField("fireproofMarking", "Маркировка огнестойкости", "text", false, "F-2/400", null, null, "F-2/400"));
        fields.add(createHiddenField("maxTemperature", "Предельная температура (°C)", "number", false, null, null, null, "появляется при выборе Огнестойкость"));
        fields.add(createField("explosionProof", "Взрывозащита", "boolean", false, "false", null, null));
        fields.add(createHiddenField("explosionMarking", "Маркировка взрывозащиты", "text", false, "1Ex d IIC T4 Gb", null, null, "1Ex d IIC T4 Gb"));
        fields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически, можно редактировать"));

        // Скрытые поля для хранения маркировки компонентов
        fields.add(createHiddenField("motorWheelFullMarking", "", "hidden", false, null, null, null, null));
        fields.add(createHiddenField("radialWheelFullMarking", "", "hidden", false, null, null, null, null));
        fields.add(createHiddenField("axialWheelFullMarking", "", "hidden", false, null, null, null, null));
        fields.add(createHiddenField("motorFullMarking", "", "hidden", false, null, null, null, null));

        return fields;
    }

    // ==========================================================
    // ПУБЛИЧНЫЕ МЕТОДЫ
    // ==========================================================

    public List<FieldMetadataDto> getFieldsForType(String cardType) {
        return metadataMap.getOrDefault(cardType, Collections.emptyList());
    }

    public List<String> getAvailableCardTypes() {
        return new ArrayList<>(metadataMap.keySet());
    }
}
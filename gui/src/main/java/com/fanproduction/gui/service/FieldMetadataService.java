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

        // РАЗДЕЛ 1: ОСНОВНЫЕ ХАРАКТЕРИСТИКИ
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

        // РАЗДЕЛ 2: ИСПОЛНЕНИЕ (взаимоисключающие галочки)
        motorFields.add(createSeparator("Исполнение"));

        // Галочка "Общего применения" (по умолчанию включена)
        motorFields.add(createField("generalPurpose", "Общего применения", "boolean", false, "true", null, null));

        // Галочка "Огнестойкость"
        motorFields.add(createField("fireproof", "Огнестойкость", "boolean", false, "false", null, null));

        // ========== ПОЛЯ, ПОЯВЛЯЮЩИЕСЯ ПРИ ВЫБОРЕ "ОГНЕСТОЙКОСТЬ" ==========
        // (все скрыты по умолчанию, управляются через ExecutionMarkingHelper)

        // Время огнестойкости (необязательное поле)
        motorFields.add(createHiddenField(
                "fireproofTime",
                "Время огнестойкости (часы)",
                "number",
                false,
                null,
                null,
                null,
                "Укажите время огнестойкости (необязательно)"
        ));

        // Предельная температура (предзаполнена 400)
        motorFields.add(createHiddenField(
                "maxTemperature",
                "Предельная температура (°C)",
                "number",
                false,
                "400",
                null,
                null,
                "Введите температуру (по умолчанию 400°C)"
        ));

        // Маркировка огнестойкости (формируется автоматически)
        motorFields.add(createHiddenField(
                "fireproofMarking",
                "Маркировка огнестойкости",
                "text",
                false,
                null,
                null,
                null,
                "формируется автоматически из времени и температуры"
        ));

        // ========== ВЗРЫВОЗАЩИТА ==========
        // Галочка "Взрывозащита"
        motorFields.add(createField("explosionProof", "Взрывозащита", "boolean", false, "false", null, null));

        // Поле, появляющееся при выборе "Взрывозащита" (скрыто по умолчанию)
        motorFields.add(createHiddenField(
                "explosionMarking",
                "Маркировка взрывозащиты",
                "text",
                false,
                "1Ex d IIC T4 Gb",
                null,
                null,
                "можно редактировать (по умолчанию 1Ex d IIC T4 Gb)"
        ));

        // РАЗДЕЛ 3: ПОЛНАЯ МАРКИРОВКА
        motorFields.addAll(createFullMarkingField());
        metadataMap.put("MOTOR", motorFields);


        // ========== Мотор-колесо (MOTOR_WHEEL) ==========
        List<FieldMetadataDto> motorWheelFields = new ArrayList<>();

        // Производитель
        motorWheelFields.add(createField("manufacturer", "Производитель", "text", false, null, null, null, "Например: Siemens, ABB"));

        // Маркировка производителя
        motorWheelFields.add(createField("manufacturerMarking","Маркировка производителя","text",true,null,null,null,"Например: RE280F-4D-AC0E или DYF4D-280-QW1a"));

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
        motorWheelFields.addAll(createFullMarkingField());

        metadataMap.put("MOTOR_WHEEL", motorWheelFields);


        // ========== Колесо радиальное (RADIAL_WHEEL) ==========
        List<FieldMetadataDto> radialWheelFields = new ArrayList<>();

        // ========== ОСНОВНЫЕ ПОЛЯ ==========
        radialWheelFields.add(createField("manufacturer", "Производитель", "text", false, null, null, null));
        radialWheelFields.add(createField("size", "Размер колеса", "double", true, null, null, null, "Введите число, например 280"));

        // ========== ТИП КОЛЕСА ==========
        radialWheelFields.add(createSeparator("Тип рабочего колеса"));

        // Галочка "Партнёрское рабочее колесо"
        radialWheelFields.add(createField("isPartnerWheel", "Партнёрское рабочее колесо", "boolean", false, "false", null, null));

        // Поле "Маркировка производителя" (скрыто по умолчанию)
        radialWheelFields.add(createField("marking", "Маркировка производителя", "text", true, null, null, null, "Например: КЦ-280-1210", false));

        // Галочка "Фирменное рабочее колесо"
        radialWheelFields.add(createField("isOwnProduction", "Фирменное рабочее колесо", "boolean", false, "false", null, null));

        // ========== ПОЛЯ ДЛЯ ФИРМЕННОГО КОЛЕСА (скрыты по умолчанию) ==========
        radialWheelFields.add(createHiddenField("series", "Серия колеса", "text", true, null, null, null, "Например: КЦ, РК"));
        radialWheelFields.add(createHiddenField("bladeType", "Тип лопаток", "combobox", true, null,
                new String[]{"V", "N", "RO"}, null,
                "V - впередзагнутые / N - назадзагнутые / RO - радиальнооканчивающиеся"));

        // ========== ВЫБОР КОМПОНЕНТА "СТУПИЦА" ==========
        // ВОТ ЗДЕСЬ МЫ МЕНЯЕМ ОБЫЧНОЕ ПОЛЕ НА SELECTABLE!
        radialWheelFields.add(createSelectableField(
                "hubComponentId",
                "Ступица",
                "COMPONENT",
                "hubName",
                "Ступица колеса"
        ));

        // Поле для отображения имени ступицы (скрытое, заполняется автоматически)
        radialWheelFields.add(createHiddenReadOnlyField("hubName", "", "text", "", ""));

        // ========== ОСТАЛЬНЫЕ ПОЛЯ ==========
        radialWheelFields.add(createHiddenField("bladeMod", "Модификация лопатки", "text", false, null, null, null, "Например 14; 12U"));
        radialWheelFields.add(createHiddenField("frontDiskMod", "Модификация переднего диска", "text", false, null, null, null, "Например А; В"));
        radialWheelFields.add(createHiddenField("wheelWidth", "Ширина колеса", "double", false, null, null, null, "Введите коэффициент, например 0.27"));
        radialWheelFields.add(createHiddenField("bladeCount", "Количество лопаток", "number", false, null, null, null, "Введите число лопаток, например 6"));
        radialWheelFields.add(createHiddenField("bladeLengthCoeff", "Коэффициент длины лопатки", "double", false, null, null, null, "Введите коэффициент, например 1.05"));
        radialWheelFields.add(createHiddenField("wheelCode", "Код колеса", "text", false, null, null, null, "формируется автоматически"));
        radialWheelFields.add(createHiddenField("wheelFormula", "Формула колеса", "text", false, null, null, null, "формируется автоматически"));

        // ========== ОБЩИЕ ПОЛЯ ==========
        radialWheelFields.addAll(createCommonFields());

        // ========== ИСПОЛНЕНИЕ ==========
        radialWheelFields.addAll(createExecutionFields());

        // ========== ПОЛНАЯ МАРКИРОВКА ==========
        radialWheelFields.addAll(createFullMarkingField());

        metadataMap.put("RADIAL_WHEEL", radialWheelFields);


        // ========== Осевое колесо (AXIAL_WHEEL) ==========
        List<FieldMetadataDto> axialWheelFields = new ArrayList<>();

        axialWheelFields.add(createField("manufacturer", "Производитель", "text", false, null, null, null, "Например: Промпат, FogStream"));
        axialWheelFields.add(createField("size", "Типоразмер", "double", true, null, null, null, "Например: 5,6; 6,3"));
        axialWheelFields.add(createField("trimCoefficient", "Коэффициент подрезки (%)", "double", false, null, null, null, "Например: 1,00"));
        axialWheelFields.add(createReadOnlyField("wheelDiameter", "Диаметр колеса (мм)", "number", null, "Рассчитывается автоматически: Типоразмер × (100 - Коэф. подрезки)"));

        // 2. ТИП КОЛЕСА
        axialWheelFields.add(createSeparator("Тип рабочего колеса"));
        axialWheelFields.add(createField("isPartnerWheel", "Партнёрское рабочее колесо", "boolean", false, "false", null, null, null));
        axialWheelFields.add(createHiddenField("marking", "Маркировка производителя", "text", true, null, null, null, "Например: PAG.400.6-3.P3HR.30.30.41-3"));
        axialWheelFields.add(createField("isOwnProduction", "Фирменное рабочее колесо", "boolean", false, "false", null, null, null));

        // 3. ФИРМЕННОЕ КОЛЕСО (скрыто)
        axialWheelFields.add(createHiddenField("series", "Серия колеса", "text", true, null, null, null, "Например: AW, AWR"));

        axialWheelFields.add(createHiddenSeparator("Тип изготовления"));
        axialWheelFields.add(createHiddenField("isAssembledFromComponents", "Колесо сборное из компонентов", "boolean", false, "false", null, null, null));
        axialWheelFields.add(createHiddenField("isWeldedFromMaterials", "Колесо сварное из материалов", "boolean", false, "false", null, null, null));

        // 3.1 ХАБ (сборный)
        axialWheelFields.add(createSelectableField("wheelHubComponentId", "Ступица (Хаб) рабочего колеса", "COMPONENT", "wheelHubName", "Хаб рабочего колеса"));
        axialWheelFields.add(createHiddenReadOnlyField("wheelHubName", "", "text", "", ""));

        // 3.2 ХАБ (сварной)
        axialWheelFields.add(createHiddenField("wheelHubType", "Ступица (Хаб) рабочего колеса", "text", true, null, null, null, "введите тип хаба, например 109_50/6-6"));

        // 3.3 Максимальное количество лопаток
        axialWheelFields.add(createHiddenField("maxBladeCount", "Максимальное кол-во лопаток в данном Хабе", "number", true, null, null, null, "Например: 9, 12"));

        // 3.4 Лопатка (сборный)
        axialWheelFields.add(createSelectableField(
                "bladeComponentId",           // fieldName
                "Лопатка рабочего колеса",    // label
                "COMPONENT",                  // referenceType
                "bladeName",                  // targetFieldName
                "Лопатка рабочего колеса"     // role
        ));
        axialWheelFields.add(createHiddenReadOnlyField("bladeName", "", "text", "", ""));

        // 3.5 Лопатка (сварной)
        axialWheelFields.add(createHiddenField("bladeType", "Лопатка рабочего колеса", "text", true, null, null, null, "введите тип лопатки, например 109_50"));

        // 3.6 Материал лопатки
        axialWheelFields.add(createHiddenField("bladeMaterial", "Материал лопатки", "text", true, null, null, null, "Укажите условный материал лопатки, например: St или AISI"));

        // 3.7 Количество лопаток
        axialWheelFields.add(createHiddenField("bladeCount", "Количество установленных лопаток", "number", true, null, null, null, "Должно быть больше 1 и не превышать максимальное количество для хаба"));

        // 3.8 Угол установки
        axialWheelFields.add(createHiddenField("bladeAngle", "Угол установки лопаток", "number", true, null, null, null, "Например: 27, 30, 43"));

        // 3.9 Установочная ступица
        axialWheelFields.add(createSelectableField(
                "hubComponentId",
                "Установочная ступица",
                "COMPONENT",
                "hubName",
                "Установочная ступица"
        ));
        axialWheelFields.add(createHiddenReadOnlyField("hubName", "", "text", "", ""));
        // 3.10 Формула колеса
        axialWheelFields.add(createHiddenField("wheelFormula", "Формула колеса", "text", true, null, null, null, "формируется автоматически, можно редактировать"));

        // 4. ОБЩИЕ ПОЛЯ
        axialWheelFields.addAll(createCommonFields());

        // 5. ИСПОЛНЕНИЕ
        axialWheelFields.addAll(createExecutionFields());

        // 6. ПОЛНАЯ МАРКИРОВКА
        axialWheelFields.addAll(createFullMarkingField());
        metadataMap.put("AXIAL_WHEEL", axialWheelFields);

        // ========== Вентилятор канальный (DUCT_FAN) ==========
        List<FieldMetadataDto> ductFanFields = new ArrayList<>();

        ductFanFields.add(createField("seriesName", "Наименование серии", "text", true, "VRK-PatAIR", null, null, "VRK-PatAIR"));
        ductFanFields.add(createField("ductSize", "Типоразмер", "text", true, null, null, null, "40-20, 60-30"));
        ductFanFields.add(createField("executionType", "Исполнение", "combobox", true, "P",
                new String[]{"P", "PS", "PKV", "PRV"}, null, "P, PS, PKV, PRV"));
        ductFanFields.add(createField("ductFanType", "Тип колеса", "combobox", true, "MOTOR_WHEEL",
                new String[]{"MOTOR_WHEEL", "RADIAL_WHEEL"}, null, "Мотор-колесо / Радиальное колесо"));
        ductFanFields.add(createField("ductFanType_value", "", "hidden", false, null, null, null, null, false));

        // Поля для выбора компонентов (скрыты по умолчанию)
        ductFanFields.add(createField("motorWheelId", "Мотор-колесо", "selectable", false, null, null, "MOTOR_WHEEL", "Выберите мотор-колесо", false));
        ductFanFields.add(createField("radialWheelId", "Радиальное колесо", "selectable", false, null, null, "RADIAL_WHEEL", "Выберите радиальное колесо", false));

        ductFanFields.add(createField("motorId", "Электродвигатель", "selectable", false, null, null, "MOTOR", "Выберите электродвигатель", false));

        ductFanFields.add(createField("wheelSize", "Размер колеса", "number", false, null, null, null, "заполняется автоматически. 2.5, 3.0...",false));

        ductFanFields.add(createField("poles", "Количество полюсов", "number", false, null, null, null, "заполняется автоматически"));
        ductFanFields.add(createField("voltage", "Напряжение (В)", "number", false, null, null, null, "заполняется автоматически"));
        ductFanFields.add(createField("voltageCode", "Код напряжения", "text", false, null, null, null, "заполняется автоматически"));
        ductFanFields.add(createField("ratedSpeedRpm", "Номинальная скорость (об/мин)", "number", false, null, null, null, "заполняется автоматически"));
        ductFanFields.add(createField("actualSpeedRpm", "Фактическая скорость (об/мин)", "number", false, null, null, null, "можно изменить"));

//        ductFanFields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически, можно редактировать"));

        ductFanFields.addAll(getFanSpecialFields());
        metadataMap.put("DUCT_FAN", ductFanFields);

        // ========== Вентилятор крышный низкопрофильный (ROOF_LOW_PROFILE_FAN) ==========
        List<FieldMetadataDto> roofLowProfileFields = new ArrayList<>();

        roofLowProfileFields.add(createField("seriesName", "Наименование серии", "text", true, "VR-PatAIR", null, null, "VR-PatAIR"));
        roofLowProfileFields.add(createField("executionType", "Исполнение", "combobox", true, "KpM",
                new String[]{"KpM", "KpMS"}, null, "KpM, KpMS"));
        roofLowProfileFields.add(createField("roofSize", "Типоразмер", "text", true, null, null, null, "40/31"));
        roofLowProfileFields.add(createField("climateType", "Климатическое исполнение", "text", true, "У1", null, null, "У1, У2, УХЛ1"));
        roofLowProfileFields.add(createField("motorWheelId", "Мотор-колесо", "reference", true, null, null, "MOTOR_WHEEL", "Выберите мотор-колесо"));
        roofLowProfileFields.add(createField("poles", "Количество полюсов", "number", false, null, null, null, "заполняется автоматически"));
        roofLowProfileFields.add(createField("voltage", "Напряжение (В)", "number", false, null, null, null, "заполняется автоматически"));
        roofLowProfileFields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически"));

        metadataMap.put("ROOF_LOW_PROFILE_FAN", roofLowProfileFields);

        // ========== Вентилятор крышный радиальный (ROOF_RADIAL_FAN) ==========
        List<FieldMetadataDto> roofRadialFields = new ArrayList<>();

        roofRadialFields.add(createField("seriesName", "Наименование серии", "text", true, "VR-PatAIR", null, null, "VR-PatAIR"));
        roofRadialFields.add(createField("executionType", "Исполнение", "combobox", true, "KpR",
                new String[]{"KpR", "KpRS"}, null, "KpR, KpRS"));
        roofRadialFields.add(createField("roofSize", "Типоразмер", "text", true, null, null, null, "40, 50"));
        roofRadialFields.add(createField("climateType", "Климатическое исполнение", "text", true, "У1", null, null, "У1, У2, УХЛ1"));
        roofRadialFields.add(createField("radialWheelId", "Радиальное колесо", "reference", true, null, null, "RADIAL_WHEEL", "Выберите радиальное колесо"));
        roofRadialFields.add(createField("motorId", "Электродвигатель", "reference", true, null, null, "MOTOR", "Выберите электродвигатель"));
        roofRadialFields.add(createField("poles", "Количество полюсов", "number", false, null, null, null, "заполняется автоматически"));
        roofRadialFields.add(createField("voltage", "Напряжение (В)", "number", false, null, null, null, "заполняется автоматически"));
        roofRadialFields.add(createField("voltageCode", "Код напряжения", "text", false, null, null, null, "заполняется автоматически"));
        roofRadialFields.add(createField("ratedSpeedRpm", "Номинальная скорость (об/мин)", "number", false, null, null, null, "заполняется автоматически"));
        roofRadialFields.add(createField("actualSpeedRpm", "Фактическая скорость (об/мин)", "number", false, null, null, null, "можно изменить"));
        roofRadialFields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически"));

        metadataMap.put("ROOF_RADIAL_FAN", roofRadialFields);



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

    /**
     * Возвращает список специальных полей для вентиляторов
     * (огнестойкость, взрывозащита, полная маркировка)
     */
    private List<FieldMetadataDto> getFanSpecialFields() {
        List<FieldMetadataDto> specialFields = new ArrayList<>();

        specialFields.add(createField("powerKw", "Мощность (КВт)", "double", false, null, null, null, "заполняется автоматически"));

        // Общее применение
        specialFields.add(createField("generalPurpose", "Общего применения", "boolean", false, "true", null, null));

        // Огнестойкость
        specialFields.add(createField("fireproof", "Огнестойкость", "boolean", false, "false", null, null));
        specialFields.add(createField("fireproofMarking", "Маркировка огнестойкости", "text", false, "F-2/400", null, null, "F-2/400", false));
        specialFields.add(createField("maxTemperature", "Предельная температура (°C)", "number", false, null, null, null, "появляется при выборе Огнестойкость", false));

        // Взрывозащита
        specialFields.add(createField("explosionProof", "Взрывозащита", "boolean", false, "false", null, null));
        specialFields.add(createField("explosionMarking", "Маркировка взрывозащиты", "text", false, "1Ex d IIC T4 Gb", null, null, "1Ex d IIC T4 Gb", false));

        // Полная маркировка
        specialFields.add(createField("fullMarking", "Полная маркировка", "text", false, null, null, null, "формируется автоматически, можно редактировать"));

        // Скрытые поля для хранения маркировки компонентов
        specialFields.add(createField("motorWheelFullMarking", "", "hidden", false, null, null, null, null, false));
        specialFields.add(createField("radialWheelFullMarking", "", "hidden", false, null, null, null, null, false));
        specialFields.add(createField("axialWheelFullMarking", "", "hidden", false, null, null, null, null, false));
        specialFields.add(createField("motorFullMarking", "", "hidden", false, null, null, null, null, false));

        return specialFields;
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
        field.setAddToProduct(true);
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

    /**
     * Создаёт поле-разделитель с заголовком
     */
    private FieldMetadataDto createSeparator(String title) {
        FieldMetadataDto field = new FieldMetadataDto();
        field.setName("separator_" + System.currentTimeMillis());
        field.setLabel(title);
        field.setType("separator");
        field.setRequired(false);
        field.setVisible(true);
        return field;
    }

    /**
     * Создаёт скрытый разделитель-заголовок
     */
    private FieldMetadataDto createHiddenSeparator(String title) {
        FieldMetadataDto field = createSeparator(title);
        field.setVisible(false);
        return field;
    }

    /**
     * Создаёт поле только для чтения
     */
    private FieldMetadataDto createReadOnlyField(String name, String label, String type,
                                                 String defaultValue, String hint) {
        FieldMetadataDto field = createField(name, label, type, false, defaultValue, null, null, hint);
        field.setReadOnly(true);
        return field;
    }

    /**
     * Создаёт поле, которое по умолчанию скрыто
     */
    private FieldMetadataDto createHiddenField(String name, String label, String type, boolean required,
                                               String defaultValue, String[] options, String referenceType,
                                               String hint) {
        FieldMetadataDto field = createField(name, label, type, required, defaultValue, options, referenceType, hint);
        field.setVisible(false);
        return field;
    }

    /**
     * Создаёт поле только для чтения, которое по умолчанию скрыто
     */
    private FieldMetadataDto createHiddenReadOnlyField(String name, String label, String type,
                                                       String defaultValue, String hint) {
        FieldMetadataDto field = createReadOnlyField(name, label, type, defaultValue, hint);
        field.setVisible(false);
        return field;
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


    /**
     * Создаёт поле выбора компонента с добавлением в product_components
     */
    private FieldMetadataDto createSelectableField(String name,
                                                   String label,
                                                   String referenceType,
                                                   String targetFieldName,
                                                   String role) {
        FieldMetadataDto field = new FieldMetadataDto();
        field.setName(name);
        field.setLabel(label);
        field.setType("selectable");
        field.setReferenceType(referenceType);
        field.setTargetFieldName(targetFieldName);
        field.setRole(role);
        field.setAddToProduct(true);
        field.setVisible(false);
        return field;
    }

    /**
     * Создаёт поле выбора компонента без добавления в product_components
     */
    private FieldMetadataDto createSelectableField(String name,
                                                   String label,
                                                   String referenceType,
                                                   String targetFieldName) {
        FieldMetadataDto field = new FieldMetadataDto();
        field.setName(name);
        field.setLabel(label);
        field.setType("selectable");
        field.setReferenceType(referenceType);
        field.setTargetFieldName(targetFieldName);
        field.setAddToProduct(false);
        field.setVisible(false);
        return field;
    }


}
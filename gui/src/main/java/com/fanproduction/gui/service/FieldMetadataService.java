package com.fanproduction.gui.service;

import com.fanproduction.gui.dto.metadata.FieldMetadataDto;
import org.springframework.stereotype.Service;

import com.fanproduction.gui.client.FanTemplateVersionClient;
import com.fanproduction.gui.dto.response.DynamicFormFieldDto;
import com.fanproduction.gui.dto.response.DynamicFormMetadataDto;

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
        axialWheelFields.add(createHiddenField("maxBladeCount", "Макс. кол-во лопаток в Хабе", "number", true, null, null, null, "Например: 9, 12"));

        // 3.4 Лопатка (сборный)
        axialWheelFields.add(createSelectableField(
                "bladeComponentId",
                "Лопатка рабочего колеса",
                "COMPONENT",
                "bladeName",
                "Лопатка рабочего колеса"
        ));
        axialWheelFields.add(createHiddenReadOnlyField("bladeName", "", "text", "", ""));

        // 3.5 Лопатка (сварной)
        axialWheelFields.add(createHiddenField("bladeType", "Лопатка рабочего колеса", "text", true, null, null, null, "введите тип лопатки, например 109_50"));

        // 3.6 Материал лопатки
        axialWheelFields.add(createHiddenField("bladeMaterial", "Материал лопатки", "text", true, null, null, null, "Укажите условный материал лопатки, например: St или AISI"));

        // 3.7 Количество лопаток
        axialWheelFields.add(createHiddenField("bladeCount", "Кол-во установленных лопаток", "number", true, null, null, null, "Должно быть больше 1 и не превышать максимальное количество для хаба"));

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
        fields.add(createField("maxSpeedRpm", "Макс. скорость вращения (об/мин)", "number", false, null, null, null, "Введите число"));
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

    // ========== ОБЩИЕ МЕТОДЫ СОЗДАНИЯ ПОЛЕЙ ==========

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

    // ==========================================================
    // FAN — динамические поля из шаблона
    // ==========================================================

    /**
     * Загрузить поля FAN-карточки с сервера по версии шаблона.
     * <p>
     * Вызывает REST {@code GET /api/fan-template-versions/{id}/form},
     * конвертирует {@link DynamicFormMetadataDto} → список {@link FieldMetadataDto}.
     * <p>
     * Метод синхронный — вызывать из фонового потока (например, из
     * {@code Platform.runLater} обёртки или через {@code LoadingDialog}).
     *
     * @param templateVersionId ID версии шаблона
     * @return список полей формы, в порядке {@code displayOrder}
     * @throws RuntimeException при ошибке загрузки или конвертации
     */
    public List<FieldMetadataDto> getFieldsForFan(Long templateVersionId) {
        if (templateVersionId == null) {
            throw new IllegalArgumentException("templateVersionId не может быть null");
        }

        try {
            DynamicFormMetadataDto metadata =
                    FanTemplateVersionClient.getFormMetadata(templateVersionId);

            if (metadata.getFields() == null) {
                return List.of();
            }

            return metadata.getFields().stream()
                    .map(this::toFieldMetadata)
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Не удалось загрузить поля FAN-карточки: " + e.getMessage(), e);
        }
    }

    /**
     * Получить метаданные формы FAN с кэшем (TTL 10 минут).
     * <p>
     * Кэш нужен, чтобы не делать REST-запрос при каждом открытии
     * окна просмотра. Инвалидация — по истечении TTL или через
     * {@link #clearFanMetadataCache()}.
     */
    public DynamicFormMetadataDto getFanFormMetadata(Long templateVersionId) {
        if (templateVersionId == null) {
            throw new IllegalArgumentException("templateVersionId не может быть null");
        }

        CachedMetadata cached = fanMetadataCache.get(templateVersionId);
        if (cached != null && !cached.isExpired()) {
            return cached.data();
        }

        try {
            DynamicFormMetadataDto loaded = FanTemplateVersionClient.getFormMetadata(templateVersionId);
            fanMetadataCache.put(templateVersionId,
                    new CachedMetadata(loaded, System.currentTimeMillis() + CACHE_TTL_MS));
            return loaded;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Не удалось загрузить метаданные FAN-формы: " + e.getMessage(), e);
        }
    }

    /**
     * Очистить кэш метаданных шаблонов.
     * Вызывать после публикации новой версии шаблона или по кнопке «Обновить».
     */
    public void clearFanMetadataCache() {
        fanMetadataCache.clear();
    }

    /**
     * Конвертировать {@link DynamicFormFieldDto} → {@link FieldMetadataDto}.
     * <p>
     * Маппинг типов:
     * <ul>
     *   <li>{@code TEXT} → {@code "text"}</li>
     *   <li>{@code NUMBER} → {@code "number"}</li>
     *   <li>{@code DOUBLE} → {@code "double"}</li>
     *   <li>{@code COMBOBOX} → {@code "combobox"}</li>
     *   <li>{@code SELECTABLE} → {@code "selectable"}</li>
     *   <li>{@code BOOLEAN} → {@code "boolean"}</li>
     *   <li>{@code SEPARATOR} → {@code "separator"}</li>
     *   <li>{@code HIDDEN} → {@code "hidden"}</li>
     *   <li>{@code COMPUTED} → {@code "computed"} (в GUI — read-only text)</li>
     * </ul>
     */
    private FieldMetadataDto toFieldMetadata(DynamicFormFieldDto source) {
        FieldMetadataDto target = new FieldMetadataDto();

        target.setName(source.getKey());
        target.setLabel(source.getLabel());
        target.setType(mapFieldType(source.getType()));
        target.setRequired(source.isRequired());
        target.setDefaultValue(source.getDefaultValue());

        if (source.getOptions() != null) {
            target.setOptions(source.getOptions().toArray(new String[0]));
            target.setOptionsList(source.getOptions());
        }

        target.setHint(source.getHint());
        target.setReferenceType(source.getReferenceType());
        target.setTargetFieldName(source.getTargetFieldName());
        target.setRole(source.getRole());

        if (source.getAddToProduct() != null) {
            target.setAddToProduct(source.getAddToProduct());
        }

        target.setPullFrom(source.getPullFrom());
        target.setVisibleIf(source.getVisibleIf());
        target.setRequiredIf(source.getRequiredIf());
        target.setOnSelect(source.getOnSelect());

        // Видимость: если есть visibleIf — изначально скрыто до первого вычисления
        target.setVisible(source.getVisibleIf() == null);

        // COMPUTED-поля в GUI — read-only
        target.setReadOnly("COMPUTED".equals(source.getType()));

        return target;
    }

    /**
     * Привести тип поля сервера к строковому типу GUI.
     */
    private String mapFieldType(String serverType) {
        if (serverType == null) return "text";

        return switch (serverType) {
            case "TEXT" -> "text";
            case "NUMBER" -> "number";
            case "DOUBLE" -> "double";
            case "COMBOBOX" -> "combobox";
            case "SELECTABLE" -> "selectable";
            case "BOOLEAN" -> "boolean";
            case "SEPARATOR" -> "separator";
            case "HIDDEN" -> "hidden";
            case "COMPUTED" -> "computed";
            default -> "text";
        };
    }

    // ==========================================================
    // КЭШ МЕТАДАННЫХ ШАБЛОНОВ (TTL 10 минут)
    // ==========================================================

    private static final long CACHE_TTL_MS = 10 * 60 * 1000; // 10 минут

    private final Map<Long, CachedMetadata> fanMetadataCache = new java.util.concurrent.ConcurrentHashMap<>();

    private record CachedMetadata(DynamicFormMetadataDto data, long expiresAt) {
        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}
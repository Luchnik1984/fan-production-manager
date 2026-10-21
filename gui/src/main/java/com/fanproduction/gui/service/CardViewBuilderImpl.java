package com.fanproduction.gui.service;

import com.fanproduction.gui.client.AxialWheelClient;
import com.fanproduction.gui.client.ComponentClient;
import com.fanproduction.gui.client.MotorClient;
import com.fanproduction.gui.client.MotorWheelClient;
import com.fanproduction.gui.client.ProductMaterialClient;
import com.fanproduction.gui.client.RadialWheelClient;
import com.fanproduction.gui.dto.metadata.FieldMetadataDto;
import com.fanproduction.gui.dto.response.AxialWheelDto;
import com.fanproduction.gui.dto.response.ComponentDto;
import com.fanproduction.gui.dto.response.DynamicFormFieldDto;
import com.fanproduction.gui.dto.response.DynamicFormMetadataDto;
import com.fanproduction.gui.dto.response.MotorDto;
import com.fanproduction.gui.dto.response.MotorWheelDto;
import com.fanproduction.gui.dto.response.ProductCardDto;
import com.fanproduction.gui.dto.response.RadialWheelDto;
import com.fanproduction.gui.dto.view.CardViewField;
import com.fanproduction.gui.dto.view.CardViewLayout;
import com.fanproduction.gui.dto.view.CardViewSection;
import com.fanproduction.gui.dto.view.CardViewSubsection;
import com.fanproduction.gui.dto.view.CardViewTab;
import com.fanproduction.gui.util.NumberFormatter;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Реализация построителя разметки окна просмотра.
 */
@Slf4j
public class CardViewBuilderImpl implements CardViewBuilder {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    /** Поля, которые показываются в шапке, а не в ТХ. */
    private static final Set<String> HEADER_FIELDS = Set.of(
            "name", "code", "fullMarking", "fanType", "fanSubtype", "series"
    );

    /** Технические служебные поля — не показываем вообще. */
    private static final Set<String> TECHNICAL_FIELDS = Set.of(
            "id", "cardType", "templateVersionId", "fanTemplateId", "dynamicFields",
            "motorId", "motorWheelId", "radialWheelId", "axialWheelId",
            "isPartnerProduction", "isOwnProduction"
    );

    /**
     * Поля-ссылки на компоненты — в ТХ не включаются, они идут
     * в {@link #buildLinkedElements(ProductCardDto)} как подсекции.
     */
    private static final Set<String> COMPONENT_REF_FIELDS = Set.of(
            "hubComponentId", "bladeComponentId", "wheelHubComponentId"
    );

    private final FieldMetadataService metadataService;

    public CardViewBuilderImpl(FieldMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    // ==========================================================
    // FAN
    // ==========================================================

    @Override
    public CardViewLayout buildForFan(ProductCardDto card) {
        String title = buildFanTitle(card);

        List<CardViewField> header = buildFanHeader(card);
        CardViewSection techSpecs = buildFanTechSpecs(card);

        return buildLayout(title, header, techSpecs);
    }

    private String buildFanTitle(ProductCardDto card) {
        Map<String, Object> fields = card.getFields();
        if (fields == null) return card.getName();

        String fanType = str(fields.get("fanType"));
        String fullMarking = str(fields.get("fullMarking"));

        if (fanType != null && fullMarking != null) {
            return fanType + " " + fullMarking;
        }
        if (fanType != null) return fanType;
        if (fullMarking != null) return "Вентилятор " + fullMarking;
        return card.getName();
    }

    private List<CardViewField> buildFanHeader(ProductCardDto card) {
        List<CardViewField> header = new ArrayList<>();
        Map<String, Object> fields = card.getFields() != null ? card.getFields() : Map.of();

        addIfNotEmpty(header, "Тип карточки", "Вентилятор");
        addIfNotEmpty(header, "Тип вентилятора", str(fields.get("fanType")));
        addIfNotEmpty(header, "Вид вентилятора", str(fields.get("fanSubtype")));
        addIfNotEmpty(header, "Серия", str(fields.get("series")));
        addIfNotEmpty(header, "Полная маркировка", str(fields.get("fullMarking")));
        addIfNotEmpty(header, "Код", card.getCode());
        if (card.getCreatedAt() != null) {
            addIfNotEmpty(header, "Дата создания", card.getCreatedAt().format(DATE_FORMATTER));
        }
        addIfNotEmpty(header, "Создал", card.getCreatedBy());

        return header;
    }

    private CardViewSection buildFanTechSpecs(ProductCardDto card) {
        Map<String, Object> fields = card.getFields();
        if (fields == null) return null;

        Long templateVersionId = getLong(fields.get("templateVersionId"));
        if (templateVersionId == null) {
            log.warn("У карточки нет templateVersionId — ТХ не строятся");
            return null;
        }

        DynamicFormMetadataDto metadata;
        try {
            metadata = metadataService.getFanFormMetadata(templateVersionId);
        } catch (Exception e) {
            log.warn("Не удалось загрузить метаданные шаблона: {}", e.getMessage());
            return null;
        }

        if (metadata == null || metadata.getFields() == null) return null;

        List<CardViewField> fieldsWithoutSubsection = new ArrayList<>();
        List<CardViewSubsection> subsections = new ArrayList<>();

        List<CardViewField> currentSubsectionFields = null;
        String currentSubsectionTitle = null;

        for (DynamicFormFieldDto fieldDef : metadata.getFields()) {
            String type = fieldDef.getType();

            // SEPARATOR — заголовок подсекции
            if ("SEPARATOR".equals(type)) {
                if (currentSubsectionTitle != null && !currentSubsectionFields.isEmpty()) {
                    subsections.add(new CardViewSubsection(
                            currentSubsectionTitle, currentSubsectionFields));
                }
                currentSubsectionTitle = fieldDef.getLabel();
                currentSubsectionFields = new ArrayList<>();
                continue;
            }

            if (TECHNICAL_FIELDS.contains(fieldDef.getKey())) continue;
            if (HEADER_FIELDS.contains(fieldDef.getKey())) continue;

            Object rawValue = fields.get(fieldDef.getKey());
            String formatted = formatValue(rawValue);
            if (formatted == null) continue;

            CardViewField viewField = new CardViewField(
                    cleanLabel(fieldDef.getLabel()), formatted);

            if (currentSubsectionTitle != null) {
                currentSubsectionFields.add(viewField);
            } else {
                fieldsWithoutSubsection.add(viewField);
            }
        }

        if (currentSubsectionTitle != null && !currentSubsectionFields.isEmpty()) {
            subsections.add(new CardViewSubsection(
                    currentSubsectionTitle, currentSubsectionFields));
        }

        if (fieldsWithoutSubsection.isEmpty() && subsections.isEmpty()) {
            return null;
        }

        return new CardViewSection(
                "Технические характеристики",
                fieldsWithoutSubsection,
                subsections
        );
    }

    // ==========================================================
    // SYSTEM CARDS (MOTOR, MOTOR_WHEEL, RADIAL_WHEEL, AXIAL_WHEEL, CUP, ACCESSORY)
    // ==========================================================

    @Override
    public CardViewLayout buildForSystemCard(ProductCardDto card) {
        String title = buildSystemTitle(card);

        List<CardViewField> header = buildSystemHeader(card);
        CardViewSection techSpecs = buildSystemTechSpecs(card);

        return buildLayout(title, header, techSpecs);
    }

    /**
     * Заголовок окна для системной карточки.
     * <p>
     * Формат: «Отображаемое имя типа + полная маркировка».
     * Например: «Колесо осевое AW(6,3)-С-624/3-9/4ZL/43/PAG-SM1610».
     * <p>
     * Защита от дублирования: если {@code fullMarking} уже содержит имя типа
     * в начале (пользователь мог вручную вписать в {@code fullMarking}
     * название типа) — возвращаем только {@code fullMarking}.
     * <p>
     * Если {@code fullMarking} пуст — fallback на {@code name}, чтобы заголовок
     * не был пустым (старые карточки без маркировки).
     */
    private String buildSystemTitle(ProductCardDto card) {
        String typeDisplayName = getCardTypeDisplay(card.getCardType());
        Map<String, Object> fields = card.getFields() != null ? card.getFields() : Map.of();
        String fullMarking = str(fields.get("fullMarking"));

        if (fullMarking != null) {
            if (fullMarking.startsWith(typeDisplayName)) {
                return fullMarking;
            }
            return typeDisplayName + " " + fullMarking;
        }

        String name = card.getName();
        if (name != null && !name.isBlank()) {
            return name;
        }
        return typeDisplayName;
    }

    private List<CardViewField> buildSystemHeader(ProductCardDto card) {
        List<CardViewField> header = new ArrayList<>();
        Map<String, Object> fields = card.getFields() != null ? card.getFields() : Map.of();

        addIfNotEmpty(header, "Наименование", card.getName());
        addIfNotEmpty(header, "Код", card.getCode());
        addIfNotEmpty(header, "Тип карточки", getCardTypeDisplay(card.getCardType()));
        addIfNotEmpty(header, "Полная маркировка", str(fields.get("fullMarking")));
        if (card.getCreatedAt() != null) {
            addIfNotEmpty(header, "Дата создания", card.getCreatedAt().format(DATE_FORMATTER));
        }
        addIfNotEmpty(header, "Создал", card.getCreatedBy());

        return header;
    }

    private CardViewSection buildSystemTechSpecs(ProductCardDto card) {
        Map<String, Object> fields = card.getFields();
        if (fields == null || fields.isEmpty()) return null;

        List<FieldMetadataDto> metadata = metadataService.getFieldsForType(card.getCardType());
        if (metadata == null || metadata.isEmpty()) {
            log.debug("Нет метаданных для типа {} — ТХ не строятся", card.getCardType());
            return null;
        }

        List<CardViewField> fieldsWithoutSubsection = new ArrayList<>();
        List<CardViewSubsection> subsections = new ArrayList<>();

        List<CardViewField> currentSubsectionFields = null;
        String currentSubsectionTitle = null;

        for (FieldMetadataDto fieldDef : metadata) {
            String name = fieldDef.getName();
            String type = fieldDef.getType();

            // SEPARATOR — заголовок подсекции
            if ("separator".equals(type)) {
                if (currentSubsectionTitle != null && !currentSubsectionFields.isEmpty()) {
                    subsections.add(new CardViewSubsection(
                            currentSubsectionTitle, currentSubsectionFields));
                }
                currentSubsectionTitle = fieldDef.getLabel();
                currentSubsectionFields = new ArrayList<>();
                continue;
            }

            // Служебные / заголовочные / компонентные ссылки — пропускаем
            if (TECHNICAL_FIELDS.contains(name)) continue;
            if (HEADER_FIELDS.contains(name)) continue;
            if (COMPONENT_REF_FIELDS.contains(name)) continue;

            // Специальные правила видимости (как в старой CardViewController)
            if ("fireproofMarking".equals(name)
                    || "maxTemperature".equals(name)) {
                if (!Boolean.TRUE.equals(fields.get("fireproof"))) continue;
            }
            if ("explosionMarking".equals(name)) {
                if (!Boolean.TRUE.equals(fields.get("explosionProof"))) continue;
            }

            // Скрытые и пустые — пропускаем
            Object rawValue = fields.get(name);
            String formatted = formatValue(rawValue);
            if (formatted == null) continue;

            CardViewField viewField = new CardViewField(
                    cleanLabel(fieldDef.getLabel()), formatted);

            if (currentSubsectionTitle != null) {
                currentSubsectionFields.add(viewField);
            } else {
                fieldsWithoutSubsection.add(viewField);
            }
        }

        if (currentSubsectionTitle != null && !currentSubsectionFields.isEmpty()) {
            subsections.add(new CardViewSubsection(
                    currentSubsectionTitle, currentSubsectionFields));
        }

        if (fieldsWithoutSubsection.isEmpty() && subsections.isEmpty()) {
            return null;
        }

        return new CardViewSection(
                "Технические характеристики",
                fieldsWithoutSubsection,
                subsections
        );
    }

    // ==========================================================
    // ОБЩАЯ СБОРКА LAYOUT
    // ==========================================================

    private CardViewLayout buildLayout(String title,
                                       List<CardViewField> header,
                                       CardViewSection techSpecs) {
        CardViewTab mainTab = new CardViewTab(
                "Основные поля",
                header,
                techSpecs != null ? List.of(techSpecs) : List.of()
        );

        CardViewTab componentsTab = new CardViewTab("Компоненты", List.of(), List.of());
        CardViewTab materialsTab = new CardViewTab("Материалы", List.of(), List.of());

        return new CardViewLayout(title, List.of(mainTab, componentsTab, materialsTab));
    }

    // ==========================================================
    // СВЯЗАННЫЕ ЭЛЕМЕНТЫ (FAN + системы)
    // ==========================================================

    @Override
    public List<CardViewSubsection> buildLinkedElements(ProductCardDto card) {
        List<CardViewSubsection> result = new ArrayList<>();
        Map<String, Object> fields = card.getFields() != null ? card.getFields() : Map.of();
        String cardType = card.getCardType();

        // ========== FAN: связанные узлы (электродвигатель, колёса) ==========
        if ("FAN".equals(cardType)) {
            addFanLinkedElements(fields, result);
        }

        // ========== Системные карточки: компоненты по ссылкам ==========
        if ("RADIAL_WHEEL".equals(cardType)) {
            Long hubId = getLong(fields.get("hubComponentId"));
            if (hubId != null) {
                addComponentSection(result, hubId, "Ступица колеса");
            }
        }

        if ("AXIAL_WHEEL".equals(cardType)) {
            Long wheelHubId = getLong(fields.get("wheelHubComponentId"));
            if (wheelHubId != null) {
                addComponentSection(result, wheelHubId, "Хаб рабочего колеса");
            }
            Long bladeId = getLong(fields.get("bladeComponentId"));
            if (bladeId != null) {
                addComponentSection(result, bladeId, "Лопатка рабочего колеса");
            }
            Long setupHubId = getLong(fields.get("hubComponentId"));
            if (setupHubId != null) {
                addComponentSection(result, setupHubId, "Установочная ступица");
            }
        }

        return result;
    }

    private void addFanLinkedElements(Map<String, Object> fields,
                                      List<CardViewSubsection> result) {
        Long motorId = getLong(fields.get("motorId"));
        if (motorId != null) {
            CardViewSubsection section = buildMotorSection(motorId);
            if (section != null) result.add(section);
        }

        Long axialWheelId = getLong(fields.get("axialWheelId"));
        if (axialWheelId != null) {
            CardViewSubsection section = buildAxialWheelSection(axialWheelId);
            if (section != null) result.add(section);
        }

        Long radialWheelId = getLong(fields.get("radialWheelId"));
        if (radialWheelId != null) {
            CardViewSubsection section = buildRadialWheelSection(radialWheelId);
            if (section != null) result.add(section);
        }

        Long motorWheelId = getLong(fields.get("motorWheelId"));
        if (motorWheelId != null) {
            CardViewSubsection section = buildMotorWheelSection(motorWheelId);
            if (section != null) result.add(section);
        }
    }

    private void addComponentSection(List<CardViewSubsection> result,
                                     Long componentId,
                                     String title) {
        CardViewSubsection section = buildComponentSection(componentId, title);
        if (section != null) result.add(section);
    }

    // ==========================================================
    // СЕКЦИИ СВЯЗАННЫХ ЭЛЕМЕНТОВ
    // ==========================================================

    private CardViewSubsection buildMotorSection(Long motorId) {
        try {
            MotorDto motor = MotorClient.getById(motorId);
            if (motor == null) return null;

            List<CardViewField> fields = new ArrayList<>();
            addIfNotEmpty(fields, "Маркировка", motor.getFullMarking());
            addIfNotEmpty(fields, "Мощность (КВт)", formatNumber(motor.getPowerKw()));
            addIfNotEmpty(fields, "Количество полюсов", formatInt(motor.getPoles()));
            addIfNotEmpty(fields, "Номинальная скорость (об/мин)", formatInt(motor.getRatedSpeedRpm()));
            addIfNotEmpty(fields, "Фактическая скорость (об/мин)", formatInt(motor.getActualSpeedRpm()));
            addIfNotEmpty(fields, "Напряжение (В)", formatInt(motor.getVoltage()));
            addIfNotEmpty(fields, "Режим работы", motor.getOperationMode());
            addIfNotEmpty(fields, "Исполнение по монтажу", motor.getMountingType());
            addIfNotEmpty(fields, "Климатическое исполнение", motor.getClimateType());

            return fields.isEmpty() ? null : new CardViewSubsection("Электродвигатель", fields);
        } catch (Exception e) {
            log.warn("Не удалось загрузить MOTOR id={}: {}", motorId, e.getMessage());
            return null;
        }
    }

    private CardViewSubsection buildAxialWheelSection(Long id) {
        try {
            AxialWheelDto wheel = AxialWheelClient.getById(id);
            if (wheel == null) return null;

            List<CardViewField> fields = new ArrayList<>();
            addIfNotEmpty(fields, "Маркировка", wheel.getFullMarking());
            addIfNotEmpty(fields, "Типоразмер", formatDouble(wheel.getSize()));
            addIfNotEmpty(fields, "Диаметр колеса (мм)", formatInt(wheel.getWheelDiameter()));
            addIfNotEmpty(fields, "Количество лопаток", formatInt(wheel.getBladeCount()));
            addIfNotEmpty(fields, "Угол установки", formatInt(wheel.getBladeAngle()));
            addIfNotEmpty(fields, "Материал лопатки", wheel.getBladeMaterial());

            return fields.isEmpty() ? null : new CardViewSubsection("Колесо осевое", fields);
        } catch (Exception e) {
            log.warn("Не удалось загрузить AXIAL_WHEEL id={}: {}", id, e.getMessage());
            return null;
        }
    }

    private CardViewSubsection buildRadialWheelSection(Long id) {
        try {
            RadialWheelDto wheel = RadialWheelClient.getById(id);
            if (wheel == null) return null;

            List<CardViewField> fields = new ArrayList<>();
            addIfNotEmpty(fields, "Маркировка", wheel.getFullMarking());
            addIfNotEmpty(fields, "Размер", formatDouble(wheel.getSize()));
            addIfNotEmpty(fields, "Тип лопаток", wheel.getBladeType());
            addIfNotEmpty(fields, "Ступица", wheel.getHubType());

            return fields.isEmpty() ? null : new CardViewSubsection("Колесо радиальное", fields);
        } catch (Exception e) {
            log.warn("Не удалось загрузить RADIAL_WHEEL id={}: {}", id, e.getMessage());
            return null;
        }
    }

    private CardViewSubsection buildMotorWheelSection(Long id) {
        try {
            MotorWheelDto mw = MotorWheelClient.getById(id);
            if (mw == null) return null;

            List<CardViewField> fields = new ArrayList<>();
            addIfNotEmpty(fields, "Маркировка", mw.getFullMarking());
            addIfNotEmpty(fields, "Размер", formatInt(mw.getSize()));
            addIfNotEmpty(fields, "Тип лопаток", mw.getBladeType());
            addIfNotEmpty(fields, "Количество полюсов", formatInt(mw.getPoles()));
            addIfNotEmpty(fields, "Напряжение (В)", formatInt(mw.getVoltage()));
            addIfNotEmpty(fields, "Мощность (КВт)", formatNumber(mw.getPowerKw()));

            return fields.isEmpty() ? null : new CardViewSubsection("Мотор-колесо", fields);
        } catch (Exception e) {
            log.warn("Не удалось загрузить MOTOR_WHEEL id={}: {}", id, e.getMessage());
            return null;
        }
    }

    private CardViewSubsection buildComponentSection(Long componentId, String title) {
        try {
            ComponentDto component = ComponentClient.getComponentById(componentId);
            if (component == null) return null;

            List<CardViewField> fields = new ArrayList<>();
            addIfNotEmpty(fields, "Наименование", component.getName());
            addIfNotEmpty(fields, "Обозначение", component.getDesignation());
            addIfNotEmpty(fields, "Артикул", component.getVendorCode());
            addIfNotEmpty(fields, "Класс", component.getClassName());
            addIfNotEmpty(fields, "Материал", component.getMaterial());
            addIfNotEmpty(fields, "Ед. изм.", component.getUnitCode());
            if (component.getWeightKg() != null) {
                addIfNotEmpty(fields, "Масса (кг)", NumberFormatter.formatNumber(component.getWeightKg()));
            }
            addIfNotEmpty(fields, "Описание", component.getDescription());

            return fields.isEmpty() ? null : new CardViewSubsection(title, fields);
        } catch (Exception e) {
            log.warn("Не удалось загрузить COMPONENT id={}: {}", componentId, e.getMessage());
            return null;
        }
    }

    // ==========================================================
    // КОМПОНЕНТЫ / МАТЕРИАЛЫ (без изменений)
    // ==========================================================

    @Override
    public CardViewSection buildComponentsSection(Long cardId) {
        if (cardId == null) {
            return new CardViewSection("Компоненты", List.of(), List.of());
        }

        try {
            var items = ComponentClient.getProductComponents(cardId);
            if (items == null || items.isEmpty()) {
                return new CardViewSection("Компоненты", List.of(), List.of());
            }

            List<CardViewSubsection> subsections = new ArrayList<>();
            int idx = 1;
            for (Map<String, Object> item : items) {
                List<CardViewField> fields = new ArrayList<>();
                addIfNotEmpty(fields, "Наименование", str(item.get("componentName")));
                addIfNotEmpty(fields, "Класс", str(item.get("componentClass")));
                addIfNotEmpty(fields, "Артикул", str(item.get("vendorCode")));
                String quantity = formatNumberOrInt(item.get("quantity"));
                String unit = str(item.get("unitCode"));
                if (quantity != null) {
                    addIfNotEmpty(fields, "Количество",
                            unit != null ? quantity + " " + unit : quantity);
                }
                addIfNotEmpty(fields, "Место установки", str(item.get("position")));
                addIfNotEmpty(fields, "Примечание", str(item.get("note")));

                if (!fields.isEmpty()) {
                    subsections.add(new CardViewSubsection("Компонент " + idx, fields));
                    idx++;
                }
            }

            return new CardViewSection("Компоненты", List.of(), subsections);
        } catch (Exception e) {
            log.warn("Не удалось загрузить компоненты карточки id={}: {}", cardId, e.getMessage());
            return null;
        }
    }

    @Override
    public CardViewSection buildMaterialsSection(Long cardId) {
        if (cardId == null) {
            return new CardViewSection("Материалы", List.of(), List.of());
        }

        try {
            var items = ProductMaterialClient.getProductMaterials(cardId);
            if (items == null || items.isEmpty()) {
                return new CardViewSection("Материалы", List.of(), List.of());
            }

            List<CardViewSubsection> subsections = new ArrayList<>();
            int idx = 1;
            for (Map<String, Object> item : items) {
                List<CardViewField> fields = new ArrayList<>();
                addIfNotEmpty(fields, "Наименование", str(item.get("materialName")));
                addIfNotEmpty(fields, "Класс", str(item.get("materialClass")));
                String quantity = formatNumberOrInt(item.get("quantityPerUnit"));
                String unit = str(item.get("unitCode"));
                if (quantity != null) {
                    addIfNotEmpty(fields, "Количество",
                            unit != null ? quantity + " " + unit : quantity);
                }
                addIfNotEmpty(fields, "Примечание", str(item.get("note")));

                if (!fields.isEmpty()) {
                    subsections.add(new CardViewSubsection("Материал " + idx, fields));
                    idx++;
                }
            }

            return new CardViewSection("Материалы", List.of(), subsections);
        } catch (Exception e) {
            log.warn("Не удалось загрузить материалы карточки id={}: {}", cardId, e.getMessage());
            return null;
        }
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    private void addIfNotEmpty(List<CardViewField> list, String label, String value) {
        if (value != null && !value.isBlank()) {
            list.add(new CardViewField(label, value));
        }
    }

    private String str(Object value) {
        if (value == null) return null;
        String s = value.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private Long getLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatValue(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Boolean b) return b ? "Да" : "Нет";
        if (raw instanceof Number n) return NumberFormatter.formatNumber(n);
        String s = raw.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private String formatNumber(Double value) {
        return value != null ? NumberFormatter.formatNumber(value) : null;
    }

    private String formatDouble(Double value) {
        return value != null ? NumberFormatter.formatNumber(value) : null;
    }

    private String formatInt(Integer value) {
        return value != null ? String.valueOf(value) : null;
    }

    private String formatNumberOrInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return NumberFormatter.formatNumber(n);
        return value.toString();
    }

    /**
     * Убрать trailing ':' и '*' из label.
     */
    private String cleanLabel(String label) {
        if (label == null) return "";
        String s = label.trim();
        if (s.endsWith(":")) s = s.substring(0, s.length() - 1).trim();
        if (s.endsWith("*")) s = s.substring(0, s.length() - 1).trim();
        return s;
    }

    /**
     * Русское название типа карточки для шапки.
     */
    private String getCardTypeDisplay(String cardType) {
        try {
            return com.fanproduction.core.enums.CardTemplateType
                    .valueOf(cardType).getDisplayName();
        } catch (IllegalArgumentException e) {
            return cardType;
        }
    }
}

package com.fanproduction.gui.dto.fan;

import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.config.OnSelectActionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Определение поля в шаблоне карточки вентилятора.
 * <p>
 * Дублирует серверный {@code com.fanproduction.template.model.FieldDefinition}
 * на клиентской стороне. Используется:
 * <ul>
 *   <li>в {@link com.fanproduction.gui.dto.response.FanTemplateVersionDto}
 *       для полей DRAFT-версии;</li>
 *   <li>в Request-DTO при сохранении шаблона ({@code fieldsJson}).</li>
 * </ul>
 * <p>
 * Поля {@link ConditionDto} и {@link OnSelectActionDto} переиспользуются
 * из {@code gui.dto.config} (там же живут серверные аналоги).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldDefinitionDto {

    /** Уникальный ключ поля в шаблоне. */
    private String key;

    /** Отображаемое название. */
    private String label;

    /** Тип поля. */
    private FieldType type;

    /** Обязательность. */
    private boolean required;

    /** Значение по умолчанию. */
    private String defaultValue;

    /** Опции (для COMBOBOX). */
    private List<String> options;

    /** Подсказка под полем. */
    private String hint;

    /** Тип ссылки для SELECTABLE: MOTOR, MOTOR_WHEEL, RADIAL_WHEEL, AXIAL_WHEEL, COMPONENT. */
    private String referenceType;

    /** Куда сохранять displayName выбранного элемента (для SELECTABLE). */
    private String targetFieldName;

    /** Роль компонента (для SELECTABLE с addToProduct). */
    private String role;

    /** Добавлять ли компонент в product_components (для SELECTABLE). */
    private Boolean addToProduct;

    /** Маппинг «поле связанной сущности → поле карточки». */
    private Map<String, String> pullFrom;

    /** Условие видимости. */
    private ConditionDto visibleIf;

    /** Условие обязательности. */
    private ConditionDto requiredIf;

    /** Side-effect при выборе (для SELECTABLE). */
    private OnSelectActionDto onSelect;

    /** Порядок отображения. */
    private Integer displayOrder;

    /** Формула (для COMPUTED-полей, DSL). */
    private String formula;
}

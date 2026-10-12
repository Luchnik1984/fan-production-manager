package com.fanproduction.gui.dto.metadata;

import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.config.OnSelectActionDto;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Метаданные поля для динамической формы.
 * <p>
 * Используется как для системных карточек (жёстко прописанные поля),
 * так и для FAN-карточек (поля загружаются с сервера через
 * {@code /api/fan-template-versions/{id}/form}).
 */
@Data
public class FieldMetadataDto {

    // ========== ОСНОВНЫЕ ==========

    /** Имя поля (например: motorType, powerKw). */
    private String name;

    /** Отображаемое название. */
    private String label;

    /** Тип: text, number, double, boolean, combobox, reference,
     *  selectable, separator, hidden, computed. */
    private String type;

    /** Обязательное ли поле. */
    private boolean required;

    /** Значение по умолчанию. */
    private String defaultValue;

    /** Варианты для combobox. */
    private String[] options;

    /** Для ссылок: MOTOR, MOTOR_WHEEL, RADIAL_WHEEL, AXIAL_WHEEL, COMPONENT. */
    private String referenceType;

    /** Regex для валидации. */
    private String validation;

    /** Подсказка под полем. */
    private String hint;

    /** Видимость поля при инициализации. */
    private boolean visible = true;

    /** Флаг "только для чтения". */
    private boolean readOnly = false;

    // ========== ДЛЯ SELECTABLE ==========

    /** Куда сохранять отображаемое имя выбранного компонента. */
    private String targetFieldName;

    /** Роль компонента (место установки). */
    private String role;

    /** Добавлять ли в product_components. */
    private boolean addToProduct = true;

    // ========== ДЛЯ FAN (из шаблона) ==========

    /** Условие видимости. */
    private ConditionDto visibleIf;

    /** Условие обязательности. */
    private ConditionDto requiredIf;

    /** Маппинг "поле связанной сущности → поле карточки". */
    private Map<String, String> pullFrom;

    /** Side-effect при выборе. */
    private OnSelectActionDto onSelect;

    /** Формула (для COMPUTED-полей). */
    private String formula;

    /** Порядок отображения (для FAN — из шаблона). */
    private Integer displayOrder;

    /** Все опции для COMPUTED (не используется в GUI напрямую). */
    private List<String> optionsList;
}

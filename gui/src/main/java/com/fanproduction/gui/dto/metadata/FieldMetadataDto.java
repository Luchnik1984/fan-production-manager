package com.fanproduction.gui.dto.metadata;

import lombok.Data;

/**
 * Метаданные поля для динамической формы.
 */
@Data
public class FieldMetadataDto {
    private String name;           // имя поля (motorType, powerKw и т.д.)
    private String label;          // отображаемое название
    private String type;           // тип: text, number, double, boolean, combobox, reference
    private boolean required;      // обязательное ли поле
    private String defaultValue;   // значение по умолчанию
    private String[] options;      // варианты для combobox
    private String referenceType;  // для ссылок: MOTOR, MOTOR_WHEEL, RADIAL_WHEEL
    private String validation;     // regex для валидации
    private String hint;           // подсказка
    private boolean visible = true; // видимость поля
    private boolean readOnly = false; // флаг "только для чтения"
    private String targetFieldName;      // куда сохранять отображаемое имя
    private String role;                 // роль компонента
    private boolean addToProduct = true; // добавлять ли в product_components
}

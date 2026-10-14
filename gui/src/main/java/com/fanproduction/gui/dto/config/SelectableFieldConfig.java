package com.fanproduction.gui.dto.config;

import java.util.Map;

/**
 * Конфигурация поля выбора компонента (selectable).
 * <p>
 * Содержит всю информацию, необходимую для автозаполнения
 * при выборе компонента.
 *
 * @param fieldName         имя поля, куда сохраняется ID (например, {@code "motorId"})
 * @param referenceType     тип ссылки: {@code COMPONENT}, {@code MOTOR},
 *                          {@code MOTOR_WHEEL}, {@code RADIAL_WHEEL}, {@code AXIAL_WHEEL}
 * @param targetFieldName   имя поля, куда сохраняется displayName (например, {@code "motorFullMarking"})
 * @param role              роль компонента в изделии (для добавления в product_components)
 * @param addToProduct      добавлять ли компонент в product_components
 * @param pullFrom          маппинг «поле связанной сущности → поле текущей формы»;
 *                          {@code null} или пустой — автоподстановка не выполняется
 */
public record SelectableFieldConfig(
        String fieldName,
        String referenceType,
        String targetFieldName,
        String role,
        boolean addToProduct,
        Map<String, String> pullFrom
) {

    /** Конфиг с добавлением в product_components (без pullFrom). */
    public static SelectableFieldConfig withProduct(String fieldName,
                                                    String referenceType,
                                                    String targetFieldName,
                                                    String role) {
        return new SelectableFieldConfig(fieldName, referenceType, targetFieldName, role, true, null);
    }

    /** Конфиг без добавления в product_components (без pullFrom). */
    public static SelectableFieldConfig withoutProduct(String fieldName,
                                                       String referenceType,
                                                       String targetFieldName) {
        return new SelectableFieldConfig(fieldName, referenceType, targetFieldName, null, false, null);
    }
}
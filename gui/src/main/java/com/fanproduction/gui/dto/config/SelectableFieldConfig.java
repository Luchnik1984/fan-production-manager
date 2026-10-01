package com.fanproduction.gui.dto.config;

/**
 * Конфигурация поля выбора компонента (selectable).
 * Содержит всю информацию, необходимую для автозаполнения при выборе компонента.
 */
public record SelectableFieldConfig(
        String fieldName,          // Имя поля, куда сохраняется ID (например "wheelHubComponentId")
        String referenceType,      // Тип ссылки (COMPONENT, MOTOR_WHEEL, RADIAL_WHEEL, MOTOR, AXIAL_WHEEL)
        String targetFieldName,    // Имя поля, куда сохраняется отображаемое имя (например "wheelHubName")
        String role,               // Роль компонента в изделии (для добавления в product_components)
        boolean addToProduct       // Добавлять ли компонент в product_components
) {

    /**
     * Создаёт конфиг с добавлением в product_components
     */
    public static SelectableFieldConfig withProduct(String fieldName,
                                                    String referenceType,
                                                    String targetFieldName,
                                                    String role) {
        return new SelectableFieldConfig(fieldName, referenceType, targetFieldName, role, true);
    }

    /**
     * Создаёт конфиг без добавления в product_components (только автозаполнение поля)
     */
    public static SelectableFieldConfig withoutProduct(String fieldName,
                                                       String referenceType,
                                                       String targetFieldName) {
        return new SelectableFieldConfig(fieldName, referenceType, targetFieldName, null, false);
    }
}
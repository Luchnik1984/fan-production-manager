package com.fanproduction.gui.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Полный запрос на сохранение карточки FAN.
 * <p>
 * Соответствует серверному {@code ProductFullRequest}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFullRequestDto {
    private String cardType;
    private String name;
    private Map<String, Object> fields;
    private List<ComponentItemDto> components;
    private List<Long> componentsToRemove;
    private List<MaterialItemDto> materials;
    private List<Long> materialsToRemove;

    /**
     * Компонент в составе запроса.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComponentItemDto {
        private Long componentId;
        private Double quantity;
        private String position;
        private String note;
    }

    /**
     * Материал в составе запроса.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialItemDto {
        private Long materialId;
        private Double quantityPerUnit;
        private String note;
    }
}

package com.fanproduction.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Полный запрос на создание или обновление карточки продукции.
 * <p>
 * Содержит карточку + компоненты + материалы. Сохраняется атомарно
 * на стороне сервера (см. FR11).
 * <p>
 * Применяется для endpoints:
 * <ul>
 *   <li>{@code POST /api/products/full} — создание.</li>
 *   <li>{@code PUT /api/products/{id}/full} — обновление.</li>
 * </ul>
 */
public record ProductFullRequest(

        @NotBlank(message = "Тип карточки обязателен")
        String cardType,

        @NotBlank(message = "Наименование обязательно")
        String name,

        @NotNull(message = "Поля карточки обязательны")
        Map<String, Object> fields,

        /// Компоненты для добавления/обновления.
        List<ProductComponentItemRequest> components,

        /// ID компонентов для удаления.
        List<Long> componentsToRemove,

        /// Материалы для добавления/обновления.
        List<ProductMaterialItemRequest> materials,

        /// ID материалов для удаления.
        List<Long> materialsToRemove
) {
}

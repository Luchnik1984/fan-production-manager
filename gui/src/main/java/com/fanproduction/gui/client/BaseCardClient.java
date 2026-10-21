package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ProductCardDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Базовый клиент для карточек продукции одного типа.
 * <p>
 * Инкапсулирует два общих сценария:
 * <ul>
 *   <li>{@link #fetchAll()} — список карточек типа (GET /products?cardType=X&size=1000).</li>
 *   <li>{@link #fetchById(Long)} — карточка по ID (GET /products/{id}) с проверкой типа.</li>
 * </ul>
 * <p>
 * Наследники реализуют:
 * <ul>
 *   <li>{@link #getCardType()} — код типа: "MOTOR", "MOTOR_WHEEL", "RADIAL_WHEEL", "AXIAL_WHEEL".</li>
 *   <li>{@link #createDto()} — создание пустого DTO.</li>
 *   <li>{@link #populateFromFields(Object, Map)} — маппинг полей карточки в DTO.</li>
 * </ul>
 * <p>
 * <b>Обратная совместимость:</b> наследники сохраняют static-фасады
 * {@code getAll()}, {@code getById(Long)} — они делегируют в singleton-экземпляр.
 */
public abstract class BaseCardClient<T> {

    // ==========================================================
    // АБСТРАКТНЫЕ МЕТОДЫ
    // ==========================================================

    /** Код типа карточки: {@code "MOTOR"}, {@code "MOTOR_WHEEL"} и т.д. */
    protected abstract String getCardType();

    /** Создать пустой DTO. */
    protected abstract T createDto();

    /** Заполнить DTO из полей карточки ({@code fields} из {@link ProductCardDto}). */
    protected abstract void populateFromFields(T dto, Map<String, Object> fields);

    // ==========================================================
    // PROTECTED — API для статических фасадов наследников
    // ==========================================================

    /**
     * Загрузить все карточки данного типа.
     * <p>
     * GET {@code /products?cardType=X&size=1000} — 1000 как «все» с запасом.
     */
    @SuppressWarnings("unchecked")
    protected List<T> fetchAll() throws Exception {
        TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
        ApiResponse<Map<String, Object>> response = ApiClient.get(
                "/products?cardType=" + getCardType() + "&size=1000", typeRef);

        List<T> result = new ArrayList<>();
        if (response.isSuccess() && response.getData() != null) {
            List<Map<String, Object>> content =
                    (List<Map<String, Object>>) response.getData().get("content");
            if (content != null) {
                for (Map<String, Object> item : content) {
                    result.add(mapListItem(item));
                }
            }
        }
        return result;
    }

    /**
     * Загрузить карточку по ID.
     * <p>
     * Проверяет, что карточка действительно нужного типа — иначе возвращает {@code null}.
     */
    protected T fetchById(Long id) throws Exception {
        if (id == null) return null;

        ApiResponse<ProductCardDto> response = ProductCardClient.getCardById(id);
        if (response == null || !response.isSuccess() || response.getData() == null) {
            return null;
        }

        ProductCardDto card = response.getData();
        if (!getCardType().equals(card.getCardType())) {
            return null;
        }

        T dto = createDto();
        populateBasicFields(dto, card);
        populateFromFields(dto, card.getFields());
        return dto;
    }

    // ==========================================================
    // PROTECTED — переопределяемые при необходимости
    // ==========================================================

    /**
     * Маппинг элемента из списка {@code /products}.
     * По умолчанию — достаём {@code id}, {@code name}, {@code fields} и вызываем
     * {@link #populateFromFields(Object, Map)}.
     */
    @SuppressWarnings("unchecked")
    protected T mapListItem(Map<String, Object> item) {
        T dto = createDto();
        setDtoField(dto, "id", item.get("id") instanceof Number n ? n.longValue() : null);
        setDtoField(dto, "name", item.get("name"));
        Map<String, Object> fields = (Map<String, Object>) item.get("fields");
        populateFromFields(dto, fields);
        return dto;
    }

    /**
     * Заполнить базовые поля DTO (id, name) при загрузке по ID.
     */
    protected void populateBasicFields(T dto, ProductCardDto card) {
        setDtoField(dto, "id", card.getId());
        setDtoField(dto, "name", card.getName());
    }

    // ==========================================================
    // УТИЛИТЫ
    // ==========================================================

    /**
     * Установить поле DTO через reflection.
     * <p>
     * Позволяет избежать дублирования id/name в каждом наследнике.
     * Если поля нет — молча пропускаем.
     */
    private void setDtoField(T dto, String fieldName, Object value) {
        try {
            var field = dto.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(dto, value);
        } catch (Exception ignored) {
            // Поле отсутствует — пропускаем
        }
    }
}

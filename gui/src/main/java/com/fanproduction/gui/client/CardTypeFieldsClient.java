package com.fanproduction.gui.client;

import com.fanproduction.core.dto.FieldInfoDto;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Клиент для получения полей связанных сущностей.
 * <p>
 * Использует {@code GET /api/card-types/{type}/fields}.
 * <p>
 * Кэш в памяти (TTL = сессия): справочник полей меняется редко, повторные
 * открытия {@code FieldEditDialog} не должны делать запрос каждый раз.
 * Сброс — {@link #clearCache()} (например, после изменения сущностей
 * на сервере — маловероятно в рамках одной сессии, но API оставим).
 */
public class CardTypeFieldsClient {

    private static final String BASE_PATH = "/card-types";

    /** Кэш: referenceType → список полей. */
    private static final Map<String, List<FieldInfoDto>> CACHE = new ConcurrentHashMap<>();

    private CardTypeFieldsClient() {
    }

    /**
     * Получить список полей по коду типа.
     *
     * @param referenceType код типа: MOTOR, MOTOR_WHEEL, RADIAL_WHEEL, AXIAL_WHEEL, COMPONENT
     * @return список полей; пустой, если тип неизвестен
     * @throws RuntimeException при ошибке сети / HTTP (кроме 200)
     */
    public static List<FieldInfoDto> getFields(String referenceType) throws Exception {
        if (referenceType == null || referenceType.isBlank()) {
            return List.of();
        }

        String key = referenceType.trim().toUpperCase();

        // Кэш
        List<FieldInfoDto> cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        TypeReference<ApiResponse<List<FieldInfoDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FieldInfoDto>> response = ApiClient.get(
                BASE_PATH + "/" + key + "/fields", typeRef);

        if (response.isSuccess() && response.getData() != null) {
            List<FieldInfoDto> fields = response.getData();
            CACHE.put(key, fields);
            return fields;
        }
        return List.of();
    }

    /**
     * Сбросить кэш (например, для тестов или после изменения справочника).
     */
    public static void clearCache() {
        CACHE.clear();
    }
}

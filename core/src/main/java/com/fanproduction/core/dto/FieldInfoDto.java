package com.fanproduction.core.dto;

/**
 * Информация о поле связанной сущности (US9.9.a).
 * <p>
 * Живёт в {@code core}, потому что используется и в {@code services}
 * (реестр {@code CardFieldsRegistry}), и в {@code api} (контроллер).
 * <p>
 * @param key   ключ поля (camelCase, как в сущности)
 * @param label русское название
 * @param type  тип данных: TEXT, INTEGER, DOUBLE, BOOLEAN, REFERENCE
 */
public record FieldInfoDto(String key, String label, String type) {
}

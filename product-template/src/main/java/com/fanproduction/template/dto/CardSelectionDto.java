package com.fanproduction.template.dto;

import java.util.Map;

/**
 * Краткая карточка для селектора комплектующих (US10.7.2).
 *
 * @param id          ID карточки (dynamic_card.id)
 * @param name        наименование изделия
 * @param fullMarking полная маркировка
 * @param typeId      тип продукции
 * @param typeName    наименование типа
 * @param roleCode    код роли, по которой карточка найдена
 * @param fields      значения полей (dynamic_fields) — для pullFrom
 */
public record CardSelectionDto(
        Long id,
        String name,
        String fullMarking,
        Long typeId,
        String typeName,
        String roleCode,
        Map<String, Object> fields
) {
}

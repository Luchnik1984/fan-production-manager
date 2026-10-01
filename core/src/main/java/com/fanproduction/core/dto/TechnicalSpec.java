package com.fanproduction.core.dto;

/**
 * Модель технической характеристики для компонентов и материалов.
 * Используется для JSONB поля technical_specs.
 */
public record TechnicalSpec(
        String name,        // Наименование характеристики (например "Длина")
        String value,       // Значение (например "100")
        Long unitId,        // ID единицы измерения (опционально)
        String unitCode     // Код единицы измерения (для отображения)
) {}

package com.fanproduction.template.dto;

/**
 * Краткая информация о типе вентилятора для ComboBox.
 *
 * @param id           ID типа
 * @param displayName  отображаемое имя: «Вентилятор канальный (VRK-PatAIR)»
 * @param filterValue  значение для фильтрации: «Вентилятор канальный»
 *                     (совпадает с {@code fan_card.fan_type})
 */
public record FanTypeShortDto(
        Long id,
        String displayName,
        String filterValue
) {
}

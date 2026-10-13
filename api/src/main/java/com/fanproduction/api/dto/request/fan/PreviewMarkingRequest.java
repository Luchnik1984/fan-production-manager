package com.fanproduction.api.dto.request.fan;

import java.util.Map;

/**
 * Запрос на предпросмотр маркировки FAN-карточки.
 * <p>
 * Используется GUI для real-time обновления поля «Полная маркировка»
 * при изменении полей формы, без сохранения карточки.
 *
 * @param values значения полей формы
 */
public record PreviewMarkingRequest(Map<String, Object> values) {
}

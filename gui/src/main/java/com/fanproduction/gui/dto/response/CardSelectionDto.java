package com.fanproduction.gui.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Карточка для селектора комплектующих (US10.7.2).
 * <p>
 * Сервер возвращает все карточки, типы которых имеют нужную роль:
 * например, для AXIAL_WHEEL — и AW, и RAW, и любой будущий тип.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardSelectionDto {
    private Long id;
    private String name;
    private String fullMarking;
    private Long typeId;
    private String typeName;
    private String roleCode;

    /** Значения полей карточки — источник для pullFrom. */
    private Map<String, Object> fields;

    /** В дереве показываем маркировку, иначе — наименование. */
    @Override
    public String toString() {
        if (fullMarking != null && !fullMarking.isBlank()) {
            return fullMarking;
        }
        return name != null ? name : ("Карточка " + id);
    }
}

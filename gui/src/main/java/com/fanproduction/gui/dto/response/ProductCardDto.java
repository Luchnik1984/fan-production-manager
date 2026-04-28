package com.fanproduction.gui.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для отображения карточки продукции в GUI.
 */
@Data
public class ProductCardDto {
    private Long id;
    private String name;
    private String code;
    private String cardType;
    private String cardTypeDisplay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private Map<String, Object> fields;
}

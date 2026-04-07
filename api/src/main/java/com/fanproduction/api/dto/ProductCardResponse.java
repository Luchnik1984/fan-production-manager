package com.fanproduction.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для ответа с данными карточки продукции.
 */
@Data
@Builder
public class ProductCardResponse {

    private Long id;
    private String name;
    private String code;
    private String cardType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private Map<String, Object> fields;
}

package com.fanproduction.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * DTO для создания/обновления карточки продукции.
 */
@Data
public class ProductCardRequest {

    /**
     * Тип карточки (MOTOR, AXIAL_FAN, RADIAL_FAN, DUCT_FAN, CUP, ACCESSORY)
     */
    @NotBlank(message = "Тип карточки обязателен")
    private String cardType;

    /**
     * Наименование продукции
     */
    @NotBlank(message = "Наименование обязательно")
    private String name;

    /**
     * Поля карточки (специфичные для каждого типа)
     * Например: для электродвигателя - motorType, powerKw, poles и т.д.
     */
    @NotNull(message = "Поля карточки обязательны")
    private Map<String, Object> fields;
}

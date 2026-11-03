package com.fanproduction.api.controller;

import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.core.dto.FieldInfoDto;
import com.fanproduction.services.catalog.CardFieldsRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST-контроллер для получения полей связанных сущностей.
 * <p>
 * Используется в {@code FieldEditDialog} (GUI) для заполнения ComboBox
 * в pullFrom / targetFieldName / visibleIf / requiredIf.
 * <p>
 * Доступ: ADMIN, ENGINEER.
 */
@RestController
@RequestMapping("/api/card-types")
@RequiredArgsConstructor
public class CardTypesController extends BaseController {

    private final CardFieldsRegistry cardFieldsRegistry;

    /**
     * Получить список полей связанной сущности по коду типа.
     *
     * @param type код типа: MOTOR, MOTOR_WHEEL, RADIAL_WHEEL, AXIAL_WHEEL, COMPONENT
     * @return список полей или пустой список, если тип неизвестен
     */
    @GetMapping("/{type}/fields")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<List<FieldInfoDto>> getCardTypeFields(@PathVariable String type) {
        List<FieldInfoDto> fields = cardFieldsRegistry.getFields(type);
        return ApiResponse.success(fields);
    }
}

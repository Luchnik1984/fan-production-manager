package com.fanproduction.api.controller;

import com.fanproduction.api.dto.request.preview.FormulaPreviewRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.api.dto.response.FormulaPreviewResponse;
import com.fanproduction.template.formula.FormulaEngine;
import com.fanproduction.template.formula.exception.FormulaException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST-контроллер предпросмотра формул DSL.
 * <p>
 * Используется в {@code FieldEditDialog} для кнопки «▶ Вычислить»
 * у полей типа COMPUTED.
 * <p>
 * Доступ: ADMIN, ENGINEER.
 */
@Slf4j
@RestController
@RequestMapping("/api/formula")
@RequiredArgsConstructor
public class FormulaPreviewController extends BaseController {

    private final FormulaEngine formulaEngine;

    /**
     * Вычислить формулу DSL на тестовых значениях.
     *
     * @param request формула + values
     * @return результат (строка) или ошибка с описанием
     */
    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<FormulaPreviewResponse> previewFormula(
            @Valid @RequestBody FormulaPreviewRequest request) {
        try {
            Map<String, Object> values = request.values() != null
                    ? request.values() : Map.of();
            String result = formulaEngine.evaluateToString(request.formula(), values);
            return ApiResponse.success(new FormulaPreviewResponse(result));
        } catch (FormulaException e) {
            log.debug("Ошибка формулы: {}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.warn("Неожиданная ошибка предпросмотра формулы: {}", e.getMessage(), e);
            return ApiResponse.error("Ошибка вычисления: " + e.getMessage());
        }
    }
}

package com.fanproduction.api.controller;

import com.fanproduction.api.dto.request.preview.ConditionPreviewRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.api.dto.response.ConditionPreviewResponse;
import com.fanproduction.template.service.MarkingEngine;
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
 * REST-контроллер предпросмотра условий CONDITION (US9.9.c).
 * <p>
 * Используется в {@code ConditionEditDialog} для кнопки «▶ Проверить».
 * <p>
 * Доступ: ADMIN, ENGINEER.
 */
@Slf4j
@RestController
@RequestMapping("/api/condition")
@RequiredArgsConstructor
public class ConditionPreviewController extends BaseController {

    private final MarkingEngine markingEngine;

    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ConditionPreviewResponse> previewCondition(
            @Valid @RequestBody ConditionPreviewRequest request) {
        try {
            if (request.condition() == null) {
                return ApiResponse.error("Условие обязательно");
            }
            Map<String, Object> values = request.values() != null
                    ? request.values() : Map.of();
            boolean matched = markingEngine.evaluateCondition(request.condition(), values);
            return ApiResponse.success(new ConditionPreviewResponse(matched));
        } catch (Exception e) {
            log.warn("Ошибка предпросмотра условия: {}", e.getMessage(), e);
            return ApiResponse.error("Ошибка проверки: " + e.getMessage());
        }
    }
}

package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.request.fan.CreateFanTypeRequest;
import com.fanproduction.api.dto.request.fan.UpdateFanTypeRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.dto.FanTypeDto;
import com.fanproduction.template.service.FanHierarchyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления типами вентиляторов.
 * <p>
 * Доступ: только ADMIN.
 */
@RestController
@RequestMapping("/api/fan-types")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FanTypeController extends BaseController {

    private final FanHierarchyService fanHierarchyService;

    /**
     * Все типы (без вложенных видов и серий).
     */
    @GetMapping
    public ApiResponse<List<FanTypeDto>> getAllTypes() {
        return ApiResponse.success(fanHierarchyService.getAllTypes());
    }

    /**
     * Дерево: Тип → Вид → Серия.
     */
    @GetMapping("/tree")
    public ApiResponse<List<FanTypeDto>> getTree() {
        return ApiResponse.success(fanHierarchyService.getTree());
    }

    /**
     * Тип по ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<FanTypeDto> getTypeById(@PathVariable Long id) {
        return ApiResponse.success(fanHierarchyService.getTypeById(id));
    }

    /**
     * Создать тип.
     */
    @PostMapping
    public ApiResponse<FanTypeDto> createType(@Valid @RequestBody CreateFanTypeRequest request) {
        FanTypeDto created = fanHierarchyService.createType(
                request.name(),
                request.designation(),
                getCurrentUser()
        );
        return ApiResponse.success("Тип создан", created);
    }

    /**
     * Обновить тип.
     */
    @PutMapping("/{id}")
    public ApiResponse<FanTypeDto> updateType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFanTypeRequest request) {
        FanTypeDto updated = fanHierarchyService.updateType(
                id,
                request.name(),
                request.designation(),
                getCurrentUser()
        );
        return ApiResponse.success("Тип обновлён", updated);
    }

    /**
     * Удалить тип.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteType(@PathVariable Long id) {
        fanHierarchyService.deleteType(id, getCurrentUser());
        return ApiResponse.success("Тип удалён", null);
    }
}

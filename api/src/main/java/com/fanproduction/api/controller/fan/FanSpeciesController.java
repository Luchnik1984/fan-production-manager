package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.request.fan.CreateFanSpeciesRequest;
import com.fanproduction.api.dto.request.fan.UpdateFanSpeciesRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.dto.FanSpeciesDto;
import com.fanproduction.template.service.FanHierarchyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления видами вентиляторов.
 * <p>
 * Доступ: только ADMIN.
 */
@RestController
@RequestMapping("/api/fan-species")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FanSpeciesController extends BaseController {

    private final FanHierarchyService fanHierarchyService;

    /**
     * Виды по типу.
     * <p>
     * {@code typeId} — обязательный query-параметр.
     */
    @GetMapping
    public ApiResponse<List<FanSpeciesDto>> getSpeciesByType(@RequestParam Long typeId) {
        return ApiResponse.success(fanHierarchyService.getSpeciesByType(typeId));
    }

    /**
     * Вид по ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<FanSpeciesDto> getSpeciesById(@PathVariable Long id) {
        return ApiResponse.success(fanHierarchyService.getSpeciesById(id));
    }

    /**
     * Создать вид.
     */
    @PostMapping
    public ApiResponse<FanSpeciesDto> createSpecies(
            @Valid @RequestBody CreateFanSpeciesRequest request) {
        FanSpeciesDto created = fanHierarchyService.createSpecies(
                request.typeId(),
                request.name(),
                getCurrentUser()
        );
        return ApiResponse.success("Вид создан", created);
    }

    /**
     * Обновить вид.
     */
    @PutMapping("/{id}")
    public ApiResponse<FanSpeciesDto> updateSpecies(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFanSpeciesRequest request) {
        FanSpeciesDto updated = fanHierarchyService.updateSpecies(
                id,
                request.name(),
                getCurrentUser()
        );
        return ApiResponse.success("Вид обновлён", updated);
    }

    /**
     * Удалить вид.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSpecies(@PathVariable Long id) {
        fanHierarchyService.deleteSpecies(id, getCurrentUser());
        return ApiResponse.success("Вид удалён", null);
    }
}

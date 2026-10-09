package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.request.fan.CreateFanSeriesRequest;
import com.fanproduction.api.dto.request.fan.UpdateFanSeriesRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.dto.FanSeriesDto;
import com.fanproduction.template.service.FanHierarchyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления сериями вентиляторов.
 * <p>
 * Доступ: только ADMIN.
 */
@RestController
@RequestMapping("/api/fan-series")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FanSeriesController extends BaseController {

    private final FanHierarchyService fanHierarchyService;

    /**
     * Серии по виду.
     * <p>
     * {@code speciesId} — обязательный query-параметр.
     */
    @GetMapping
    public ApiResponse<List<FanSeriesDto>> getSeriesBySpecies(@RequestParam Long speciesId) {
        return ApiResponse.success(fanHierarchyService.getSeriesBySpecies(speciesId));
    }

    /**
     * Серия по ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<FanSeriesDto> getSeriesById(@PathVariable Long id) {
        return ApiResponse.success(fanHierarchyService.getSeriesById(id));
    }

    /**
     * Создать серию.
     */
    @PostMapping
    public ApiResponse<FanSeriesDto> createSeries(
            @Valid @RequestBody CreateFanSeriesRequest request) {
        FanSeriesDto created = fanHierarchyService.createSeries(
                request.speciesId(),
                request.name(),
                getCurrentUser()
        );
        return ApiResponse.success("Серия создана", created);
    }

    /**
     * Обновить серию.
     */
    @PutMapping("/{id}")
    public ApiResponse<FanSeriesDto> updateSeries(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFanSeriesRequest request) {
        FanSeriesDto updated = fanHierarchyService.updateSeries(
                id,
                request.name(),
                getCurrentUser()
        );
        return ApiResponse.success("Серия обновлена", updated);
    }

    /**
     * Удалить серию.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSeries(@PathVariable Long id) {
        fanHierarchyService.deleteSeries(id, getCurrentUser());
        return ApiResponse.success("Серия удалена", null);
    }
}

package com.fanproduction.api.controller;

import com.fanproduction.api.dto.ApiResponse;
import com.fanproduction.api.dto.ComponentClassDto;
import com.fanproduction.api.dto.ComponentDto;
import com.fanproduction.api.dto.ProductComponentDto;
import com.fanproduction.api.dto.UnitOfMeasureDto;
import com.fanproduction.core.entity.ComponentClassEntity;
import com.fanproduction.core.entity.ComponentEntity;
import com.fanproduction.core.entity.ProductComponentEntity;
import com.fanproduction.core.entity.UnitOfMeasureEntity;
import com.fanproduction.services.ComponentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/components")
@RequiredArgsConstructor
public class ComponentController {

    private final ComponentService componentService;

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    // ========== Unit of Measure ==========

    @GetMapping("/units")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<UnitOfMeasureDto>> getAllUnits() {
        List<UnitOfMeasureDto> units = componentService.getAllUnits().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ApiResponse.success(units);
    }

    // ========== Component Classes ==========

    @GetMapping("/classes")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ComponentClassDto>> getAllClasses() {
        List<ComponentClassDto> classes = componentService.getAllComponentClasses().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ApiResponse.success(classes);
    }

    @PostMapping("/classes")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ComponentClassDto> createClass(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String description = request.get("description");

        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("Название класса обязательно");
        }

        try {
            ComponentClassEntity entity = componentService.createComponentClass(
                    name.trim(), description, getCurrentUser());
            return ApiResponse.success(toDto(entity));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/classes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> deleteClass(@PathVariable Long id) {
        try {
            componentService.deleteComponentClass(id);
            return ApiResponse.success("Класс удалён", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ========== Components ==========

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ComponentDto>> getAllComponents(
            @RequestParam(required = false) Long classId) {
        List<ComponentEntity> components;
        if (classId != null) {
            components = componentService.getComponentsByClass(classId, null).getContent();
        } else {
            components = componentService.getAllComponents();
        }

        List<ComponentDto> dtos = components.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ComponentDto> getComponent(@PathVariable Long id) {
        return componentService.getComponentById(id)
                .map(this::toDto)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("Компонент не найден"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ComponentDto> createComponent(@RequestBody ComponentDto dto) {
        try {
            ComponentEntity entity = new ComponentEntity();
            entity.setClassId(dto.getClassId());
            entity.setName(dto.getName());
            entity.setVendorCode(dto.getVendorCode());
            entity.setUnitId(dto.getUnitId());
            entity.setQuantityPerUnit(dto.getQuantityPerUnit() != null ? dto.getQuantityPerUnit() : 1.0);
            entity.setDescription(dto.getDescription());
            entity.setCreatedBy(getCurrentUser());

            ComponentEntity saved = componentService.createComponent(entity);
            return ApiResponse.success(toDto(saved));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ComponentDto> updateComponent(@PathVariable Long id, @RequestBody ComponentDto dto) {
        try {
            ComponentEntity entity = new ComponentEntity();
            entity.setName(dto.getName());
            entity.setVendorCode(dto.getVendorCode());
            entity.setUnitId(dto.getUnitId());
            entity.setQuantityPerUnit(dto.getQuantityPerUnit());
            entity.setDescription(dto.getDescription());

            ComponentEntity updated = componentService.updateComponent(id, entity);
            return ApiResponse.success(toDto(updated));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> deleteComponent(@PathVariable Long id) {
        try {
            componentService.deleteComponent(id);
            return ApiResponse.success("Компонент удалён", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ComponentDto>> searchComponents(@RequestParam String query) {
        List<ComponentDto> results = componentService.searchComponents(query).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ApiResponse.success(results);
    }

    // ========== Product Components ==========

    @GetMapping("/product/{productCardId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ProductComponentDto>> getProductComponents(@PathVariable Long productCardId) {
        List<ProductComponentDto> result = componentService.getComponentsByProductCard(productCardId).stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    @PostMapping("/product/{productCardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ProductComponentDto> addComponentToProduct(
            @PathVariable Long productCardId,
            @RequestBody Map<String, Object> request) {
        try {
            Long componentId = ((Number) request.get("componentId")).longValue();
            Double quantity = request.get("quantity") != null ? ((Number) request.get("quantity")).doubleValue() : 1.0;
            String note = (String) request.get("note");

            ProductComponentEntity entity = componentService.addComponentToProduct(
                    productCardId, componentId, quantity, note);
            return ApiResponse.success(toProductDto(entity));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/product/{productCardId}/{componentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> updateComponentQuantity(
            @PathVariable Long productCardId,
            @PathVariable Long componentId,
            @RequestBody Map<String, Double> request) {
        try {
            Double quantity = request.get("quantity");
            componentService.updateComponentQuantity(productCardId, componentId, quantity);
            return ApiResponse.success("Количество обновлено", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/product/{productCardId}/{componentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> removeComponentFromProduct(
            @PathVariable Long productCardId,
            @PathVariable Long componentId) {
        try {
            componentService.removeComponentFromProduct(productCardId, componentId);
            return ApiResponse.success("Компонент удалён", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ========== Mappers ==========

    private UnitOfMeasureDto toDto(UnitOfMeasureEntity entity) {
        return UnitOfMeasureDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .symbol(entity.getSymbol())
                .isDefault(entity.getIsDefault())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    private ComponentClassDto toDto(ComponentClassEntity entity) {
        return ComponentClassDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    private ComponentDto toDto(ComponentEntity entity) {
        ComponentDto.ComponentDtoBuilder builder = ComponentDto.builder()
                .id(entity.getId())
                .classId(entity.getClassId())
                .name(entity.getName())
                .vendorCode(entity.getVendorCode())
                .unitId(entity.getUnitId())
                .quantityPerUnit(entity.getQuantityPerUnit())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy());

        // Денормализованные поля (можно загрузить отдельным запросом)
        componentService.getComponentClassById(entity.getClassId())
                .ifPresent(cls -> builder.className(cls.getName()));
        componentService.getUnitById(entity.getUnitId())
                .ifPresent(unit -> {
                    builder.unitCode(unit.getCode());
                    builder.unitName(unit.getName());
                });

        return builder.build();
    }

    private ProductComponentDto toProductDto(ProductComponentEntity entity) {
        ProductComponentDto.ProductComponentDtoBuilder builder = ProductComponentDto.builder()
                .id(entity.getId())
                .productCardId(entity.getProductCardId())
                .componentId(entity.getComponentId())
                .quantity(entity.getQuantity())
                .note(entity.getNote());

        componentService.getComponentById(entity.getComponentId()).ifPresent(comp -> {
            builder.componentName(comp.getName());
            componentService.getComponentClassById(comp.getClassId())
                    .ifPresent(cls -> builder.componentClass(cls.getName()));
            componentService.getUnitById(comp.getUnitId())
                    .ifPresent(unit -> builder.unitCode(unit.getCode()));
        });

        return builder.build();
    }
}
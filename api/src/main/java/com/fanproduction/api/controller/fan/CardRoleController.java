package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.entity.CardRole;
import com.fanproduction.template.service.CardRoleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Справочник ролей продукции (US10.7.2).
 * <p>
 * Чтение — всем аутентифицированным (роли нужны в диалоге типа),
 * создание — ADMIN.
 */
@RestController
@RequestMapping("/api/card-roles")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CardRoleController extends BaseController {

    private final CardRoleService cardRoleService;
    private final com.fanproduction.services.catalog.CardRoleSelectorService selectorService;

    @GetMapping
    public ApiResponse<List<CardRole>> getAll() {
        return ApiResponse.success(cardRoleService.getAll());
    }

    /**
     * Карточки всех типов с указанной ролью — для селектора комплектующих.
     */
    @GetMapping("/{code}/cards")
    public ApiResponse<List<com.fanproduction.template.dto.CardSelectionDto>> getCardsByRole(
            @PathVariable String code) {
        return ApiResponse.success(selectorService.findByRole(code));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CardRole> create(@Valid @RequestBody CreateCardRoleRequest request) {
        return ApiResponse.success("Роль создана",
                cardRoleService.create(request.code(), request.name(), getCurrentUser()));
    }

    /** Тело запроса создания роли. */
    public record CreateCardRoleRequest(@NotBlank String code, @NotBlank String name) {
    }
}

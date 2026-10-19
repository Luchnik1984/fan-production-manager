package com.fanproduction.api.controller;

import com.fanproduction.api.dto.request.ProductComponentItemRequest;
import com.fanproduction.api.dto.request.ProductFullRequest;
import com.fanproduction.api.dto.request.ProductMaterialItemRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.api.dto.request.ProductCardRequest;
import com.fanproduction.api.dto.response.ProductCardResponse;
import com.fanproduction.api.mapper.ProductCardMapper;
import com.fanproduction.core.entity.product.BaseProductCard;
import com.fanproduction.core.enums.CardTemplateType;
import com.fanproduction.services.ProductCardService;
import com.fanproduction.services.dto.ProductComponentItemData;
import com.fanproduction.services.dto.ProductMaterialItemData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductCardController extends BaseController {

    private final ProductCardService productCardService;
    private final ProductCardMapper productCardMapper;

    /**
     * Создание новой карточки продукции
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ProductCardResponse> createCard(@Valid @RequestBody ProductCardRequest request) {
        try {
            validateProductCardRequest(request);

            CardTemplateType cardType = CardTemplateType.valueOf(request.getCardType());

            // Добавляем name в fields
            request.getFields().put("name", request.getName());

            // Если это временная карточка
            if (request.getIsTemporary() != null && request.getIsTemporary()) {
                request.getFields().put("isTemporary", true);
            }

            BaseProductCard card = productCardService.createCard(
                    cardType,
                    request.getFields(),
                    getCurrentUser()
            );
            return ApiResponse.success(productCardMapper.toResponse(card));

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * Получение карточки по ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ProductCardResponse> getCard(@PathVariable Long id) {
        return productCardService.getCardById(id)
                .map(card -> ApiResponse.success(productCardMapper.toResponse(card)))
                .orElse(ApiResponse.error("Карточка не найдена"));
    }

    /**
     * Получение всех карточек (с пагинацией и фильтрацией по типу)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Page<ProductCardResponse>> getAllCards(
            @RequestParam(required = false) String cardType,
            @RequestParam(required = false) String fanType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<BaseProductCard> cardsPage;

        if ("FAN".equals(cardType) && fanType != null && !fanType.isEmpty()) {
            // Фильтр по конкретному типу вентилятора
            cardsPage = productCardService.getFanCardsByFanType(fanType, pageable);
        } else if (cardType != null && !cardType.isEmpty()) {
            CardTemplateType type = CardTemplateType.valueOf(cardType);
            cardsPage = productCardService.getCardsByType(type, pageable);
        } else {
            cardsPage = productCardService.getAllCards(pageable);
        }

        Page<ProductCardResponse> responsePage = cardsPage.map(productCardMapper::toResponse);
        return ApiResponse.success(responsePage);
    }

    /**
     * Обновление карточки
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ProductCardResponse> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody ProductCardRequest request) {
        try {
            validateProductCardRequest(request);

            // Добавляем name в fields
            request.getFields().put("name", request.getName());

            BaseProductCard card = productCardService.updateCard(id, request.getFields());
            return ApiResponse.success(productCardMapper.toResponse(card));

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * Удаление карточки
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> deleteCard(@PathVariable Long id) {
            productCardService.deleteCard(id);
            return ApiResponse.success("Карточка удалена", null);
    }

    /**
     * Поиск карточек по имени
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ProductCardResponse>> searchCards(@RequestParam String query) {
        List<BaseProductCard> cards = productCardService.searchByName(query);

        // Дополнительный поиск по специфичным полям
        List<BaseProductCard> additionalCards = productCardService.searchByFields(query);

        // Объединяем и убираем дубликаты
        Set<BaseProductCard> allCards = new LinkedHashSet<>();
        allCards.addAll(cards);
        allCards.addAll(additionalCards);

        List<ProductCardResponse> responses = allCards.stream()
                .map(productCardMapper::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    /**
     * Создать карточку продукции с компонентами и материалами атомарно.
     * <p>
     * См. FR11.
     */
    @PostMapping("/full")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ProductCardResponse> createFullCard(
            @Valid @RequestBody ProductFullRequest request) {
        try {
            CardTemplateType cardType = CardTemplateType.valueOf(request.cardType());

            List<ProductComponentItemData> components = mapComponents(request.components());
            List<ProductMaterialItemData> materials = mapMaterials(request.materials());

            BaseProductCard card = productCardService.createFullCard(
                    cardType,
                    request.name(),
                    request.fields(),
                    components,
                    materials,
                    getCurrentUser()
            );

            return ApiResponse.success(productCardMapper.toResponse(card));

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * Обновить карточку продукции с компонентами и материалами атомарно.
     */
    @PutMapping("/{id}/full")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ProductCardResponse> updateFullCard(
            @PathVariable Long id,
            @Valid @RequestBody ProductFullRequest request) {
        try {
            List<ProductComponentItemData> components = mapComponents(request.components());
            List<ProductMaterialItemData> materials = mapMaterials(request.materials());

            BaseProductCard card = productCardService.updateFullCard(
                    id,
                    request.name(),
                    request.fields(),
                    components,
                    request.componentsToRemove(),
                    materials,
                    request.materialsToRemove()
            );

            return ApiResponse.success(productCardMapper.toResponse(card));

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // ==========================================================
    // МАППИНГ DTO
    // ==========================================================

    private List<ProductComponentItemData> mapComponents(List<ProductComponentItemRequest> src) {
        if (src == null) return List.of();
        return src.stream()
                .map(c -> new ProductComponentItemData(
                        c.componentId(),
                        c.quantity(),
                        c.position(),
                        c.note()))
                .toList();
    }

    private List<ProductMaterialItemData> mapMaterials(List<ProductMaterialItemRequest> src) {
        if (src == null) return List.of();
        return src.stream()
                .map(m -> new ProductMaterialItemData(
                        m.materialId(),
                        m.quantityPerUnit(),
                        m.note()))
                .toList();
    }

    private void validateProductCardRequest(ProductCardRequest request) {
        String name = request.getName();
        String cardType = request.getCardType();

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Наименование обязательно для заполнения");
        }
        if (cardType == null || cardType.trim().isEmpty()) {
            throw new IllegalArgumentException("Тип карточки обязателен");
        }
    }
}

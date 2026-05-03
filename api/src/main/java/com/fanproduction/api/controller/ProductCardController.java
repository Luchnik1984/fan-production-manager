package com.fanproduction.api.controller;

import com.fanproduction.api.dto.ApiResponse;
import com.fanproduction.api.dto.ProductCardRequest;
import com.fanproduction.api.dto.ProductCardResponse;
import com.fanproduction.api.mapper.ProductCardMapper;
import com.fanproduction.core.entity.product.BaseProductCard;
import com.fanproduction.core.enums.CardTemplateType;
import com.fanproduction.services.ProductCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductCardController {

    private final ProductCardService productCardService;
    private final ProductCardMapper productCardMapper;

    /**
     * Получение текущего пользователя из SecurityContext
     */
    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    /**
     * Создание новой карточки продукции
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ProductCardResponse> createCard(@Valid @RequestBody ProductCardRequest request) {
        try {
            CardTemplateType cardType = CardTemplateType.valueOf(request.getCardType());

            // Добавляем name в fields
            request.getFields().put("name", request.getName());

            BaseProductCard card = productCardService.createCard(
                    cardType,
                    request.getFields(),
                    getCurrentUser()
            );

            return ApiResponse.success(productCardMapper.toResponse(card));

        } catch (IllegalArgumentException e) {
            return ApiResponse.error("Неверный тип карточки: " + request.getCardType());
        } catch (Exception e) {
            return ApiResponse.error("Ошибка создания карточки: " + e.getMessage());
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<BaseProductCard> cardsPage;

        if (cardType != null && !cardType.isEmpty()) {
            try {
                CardTemplateType type = CardTemplateType.valueOf(cardType);
                cardsPage = productCardService.getCardsByType(type, pageable);
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("Неверный тип карточки: " + cardType);
            }
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
            // Добавляем name в fields
            request.getFields().put("name", request.getName());

            BaseProductCard card = productCardService.updateCard(id, request.getFields());
            return ApiResponse.success(productCardMapper.toResponse(card));

        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Ошибка обновления карточки: " + e.getMessage());
        }
    }

    /**
     * Удаление карточки
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> deleteCard(@PathVariable Long id) {
        try {
            productCardService.deleteCard(id);
            return ApiResponse.success("Карточка удалена", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("Ошибка удаления карточки: " + e.getMessage());
        }
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
}

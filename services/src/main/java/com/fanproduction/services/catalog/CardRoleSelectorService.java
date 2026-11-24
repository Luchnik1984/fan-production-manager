package com.fanproduction.services.catalog;

import com.fanproduction.core.entity.product.DynamicCardEntity;
import com.fanproduction.repositories.product.DynamicCardRepository;
import com.fanproduction.template.dto.CardSelectionDto;
import com.fanproduction.template.entity.CardRole;
import com.fanproduction.template.entity.ProductType;
import com.fanproduction.template.repository.CardRoleRepository;
import com.fanproduction.template.repository.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Выбор карточек по роли продукции (US10.7.2).
 * <p>
 * Заменяет прежние выборки из отдельных таблиц колёс: селектор в форме
 * карточки просит «все осевые колёса», а сервис находит динамические
 * карточки всех типов с ролью {@code AXIAL_WHEEL} — AW, RAW, RAX и любые
 * будущие. Это делает состав расширяемым без правок кода.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardRoleSelectorService {

    private final CardRoleRepository cardRoleRepository;
    private final ProductTypeRepository productTypeRepository;
    private final DynamicCardRepository dynamicCardRepository;

    /**
     * @param roleCode код роли (AXIAL_WHEEL, RADIAL_WHEEL, VALVE…)
     * @return карточки всех типов с этой ролью; пусто, если роль не найдена
     */
    public List<CardSelectionDto> findByRole(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return List.of();
        }

        CardRole role = cardRoleRepository.findByCodeIgnoreCase(roleCode.trim()).orElse(null);
        if (role == null) {
            log.debug("Роль не найдена: {}", roleCode);
            return List.of();
        }

        List<ProductType> types = productTypeRepository.findByCardRoleIdOrderByDesignationAsc(role.getId());
        if (types.isEmpty()) {
            return List.of();
        }

        List<Long> typeIds = types.stream().map(ProductType::getId).toList();
        List<DynamicCardEntity> cards = dynamicCardRepository.findByProductTypeIdIn(typeIds);

        List<CardSelectionDto> result = new ArrayList<>(cards.size());
        for (DynamicCardEntity card : cards) {
            ProductType type = types.stream()
                    .filter(t -> t.getId().equals(card.getProductTypeId()))
                    .findFirst()
                    .orElse(null);
            result.add(new CardSelectionDto(
                    card.getId(),
                    card.getName(),
                    card.getFullMarking(),
                    card.getProductTypeId(),
                    type != null ? type.getName() : null,
                    role.getCode(),
                    card.getDynamicFields()
            ));
        }
        return result;
    }
}

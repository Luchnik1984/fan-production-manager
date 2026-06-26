package com.fanproduction.services.impl;

import com.fanproduction.core.entity.product.*;
import com.fanproduction.core.enums.CardTemplateType;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.enums.AssemblyUnitFieldType;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.core.security.CurrentUserProvider;
import com.fanproduction.repositories.product.*;
import com.fanproduction.services.ProductCardService;
import com.fanproduction.services.factory.ProductCardFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductCardServiceImpl implements ProductCardService {

    private final ProductCardRepository productCardRepository;
    private final MotorCardRepository motorCardRepository;
    private final MotorWheelCardRepository motorWheelCardRepository;
    private final RadialWheelCardRepository radialWheelCardRepository;
    private final AxialWheelCardRepository axialWheelCardRepository;
    private final ProductCardFactory cardFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public BaseProductCard createCard(CardTemplateType cardType, Map<String, Object> fields, String createdBy) {
        BaseProductCard card = cardFactory.createCard(cardType, fields);
        card.setCreatedBy(createdBy);

        // Генерируем уникальный код
        String code = generateCode(card);
        card.setCode(code);

        BaseProductCard savedCard = productCardRepository.save(card);

        // Публикуем событие аудита
        eventPublisher.publishEvent(new AuditEvent(
                this,
                createdBy,
                AuditAction.CREATE_CARD,
                "Создана карточка: " + cardType.getDisplayName() + " - " + card.getName()
        ));

        return savedCard;
    }

    @Override
    @Transactional
    public BaseProductCard updateCard(Long id, Map<String, Object> fields) {
        BaseProductCard existingCard = productCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена: " + id));

        // Создаём новую карточку с обновлёнными полями
        CardTemplateType cardType = CardTemplateType.valueOf(existingCard.getCardType());
        BaseProductCard updatedCard = cardFactory.createCard(cardType, fields);

        // Сохраняем ID и другие неизменяемые поля
        updatedCard.setId(id);
        updatedCard.setCode(existingCard.getCode());
        updatedCard.setCreatedBy(existingCard.getCreatedBy());
        updatedCard.setCreatedAt(existingCard.getCreatedAt());

        BaseProductCard savedCard = productCardRepository.save(updatedCard);

        // Публикуем событие аудита
        eventPublisher.publishEvent(new AuditEvent(
                this,
                getCurrentUser(),
                AuditAction.UPDATE_CARD,
                "Обновлена карточка: " + savedCard.getName()
        ));

        return savedCard;
    }

    @Override
    public Optional<BaseProductCard> getCardById(Long id) {
        return productCardRepository.findById(id);
    }

    @Override
    public List<BaseProductCard> getCardsByType(CardTemplateType cardType) {
        return productCardRepository.findByCardType(cardType.name());
    }

    @Override
    public Page<BaseProductCard> getAllCards(Pageable pageable) {
        return productCardRepository.findAll(pageable);
    }

    @Override
    public Page<BaseProductCard> getCardsByType(CardTemplateType cardType, Pageable pageable) {
        return productCardRepository.findByCardType(cardType.name(), pageable);
    }

    @Override
    public List<BaseProductCard> searchByName(String name) {
        return productCardRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional
    public void deleteCard(Long id) {
        BaseProductCard card = productCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена: " + id));

        String cardType = card.getCardType();
        if (AssemblyUnitFieldType.fromUnitType(cardType) != null) {
            if (isAssemblyUnitUsedInFanCards(id)) {
                List<String> usageList = getAssemblyUnitUsageInfo(id);
                String usageMessage = String.join("\n- ", usageList);
                throw new IllegalStateException(
                        String.format("Невозможно удалить сборочный узел '%s' — он используется в следующих карточках продукции:\n- %s",
                                card.getName(), usageMessage)
                );
            }
        }

        String cardName = card.getName();
        productCardRepository.deleteById(id);

        eventPublisher.publishEvent(new AuditEvent(
                this,
                getCurrentUser(),
                AuditAction.DELETE_CARD,
                "Удалена карточка: " + cardName
        ));
    }


    @Override
    public String generateCode(BaseProductCard card) {
        // TODO: Здесь будет более сложная логика в зависимости от типа вентилятора
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return card.getCardType().toLowerCase() + "_" + timestamp + "_" + uniqueId;
    }

    @Override
    public boolean isCodeUnique(String code) {
        return !productCardRepository.existsByCode(code);
    }

    /**
     * Получение текущего пользователя (временное решение).
     */
    private String getCurrentUser() {
        String email = currentUserProvider.getCurrentUserEmail();
        return email != null ? email : "system";
    }

    @Override
    @Transactional
    public void removeTemporaryFlag(Long id) {
        BaseProductCard card = productCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена"));
            card.setTemporary(false);
            productCardRepository.save(card);
    }


    @Override
    public List<BaseProductCard> searchByFields(String query) {
        String likePattern = "%" + query.toLowerCase() + "%";
        List<BaseProductCard> results = new ArrayList<>();

        // Поиск по электродвигателям
        List<MotorCardEntity> motors = motorCardRepository.searchByFields(likePattern);
        results.addAll(motors);

        // Поиск по мотор-колёсам
        List<MotorWheelCardEntity> motorWheels = motorWheelCardRepository.searchByFields(likePattern);
        results.addAll(motorWheels);

        // Поиск по радиальным колёсам
        List<RadialWheelCardEntity> radialWheels = radialWheelCardRepository.searchByFields(likePattern);
        results.addAll(radialWheels);

        // Поиск по осевым колёсам
        List<AxialWheelCardEntity> axialWheels = axialWheelCardRepository.searchByFields(likePattern);
        results.addAll(axialWheels);

        return results;
    }

    // ==========================================================
    // ПРОВЕРКА ИСПОЛЬЗОВАНИЯ СБОРОЧНОГО УЗЛА В ВЕНТИЛЯТОРАХ
    // ==========================================================

    @Override
    public boolean isAssemblyUnitUsedInFanCards(Long cardId) {
        BaseProductCard card = productCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена: " + cardId));

        String cardType = card.getCardType();
        AssemblyUnitFieldType fieldType = AssemblyUnitFieldType.fromUnitType(cardType);
        if (fieldType == null) {
            return false;
        }

        List<String> fanCardTypes = AssemblyUnitFieldType.getFanCardTypesForUnit(cardType);
        if (fanCardTypes.isEmpty()) {
            return false;
        }

        // Используем ProductCardRepository для поиска вентиляторов
        long count = productCardRepository.countFanCardsUsingUnit(
                cardId,
                fieldType.getFieldName(),
                fanCardTypes
        );

        return count > 0;
    }

    @Override
    public List<String> getAssemblyUnitUsageInfo(Long cardId) {
        List<String> usage = new ArrayList<>();

        BaseProductCard card = productCardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена: " + cardId));

        String cardType = card.getCardType();
        AssemblyUnitFieldType fieldType = AssemblyUnitFieldType.fromUnitType(cardType);
        if (fieldType == null) {
            return usage;
        }

        List<String> fanCardTypes = AssemblyUnitFieldType.getFanCardTypesForUnit(cardType);
        if (fanCardTypes.isEmpty()) {
            return usage;
        }

        // Используем ProductCardRepository для поиска вентиляторов
        List<BaseProductCard> fanCards = productCardRepository.findFanCardsUsingUnit(
                cardId,
                fieldType.getFieldName(),
                fanCardTypes
        );

        for (BaseProductCard fanCard : fanCards) {
            usage.add(String.format("%s (ID: %d)", fanCard.getName(), fanCard.getId()));
        }

        return usage;
    }

}

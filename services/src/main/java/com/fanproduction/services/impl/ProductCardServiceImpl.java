package com.fanproduction.services.impl;

import com.fanproduction.core.entity.product.BaseProductCard;
import com.fanproduction.core.entity.product.MotorCardEntity;
import com.fanproduction.core.entity.product.MotorWheelCardEntity;
import com.fanproduction.core.entity.product.RadialWheelCardEntity;
import com.fanproduction.core.enums.CardTemplateType;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.core.security.CurrentUserProvider;
import com.fanproduction.repositories.product.ProductCardRepository;
import com.fanproduction.repositories.product.MotorCardRepository;
import com.fanproduction.repositories.product.MotorWheelCardRepository;
import com.fanproduction.repositories.product.RadialWheelCardRepository;
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

        String cardName = card.getName();
        productCardRepository.deleteById(id);

        // Публикуем событие аудита
        eventPublisher.publishEvent(new AuditEvent(
                this,
                getCurrentUser(),
                AuditAction.DELETE_CARD,
                "Удалена карточка: " + cardName
        ));
    }

    @Override
    public String generateCode(BaseProductCard card) {
        // Базовый код: тип_времямяти_уникальныйID
        // TODO: Здесь будет более сложная логика в зависимости от типа вентилятора
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return card.getCardType().toLowerCase() + "_" + timestamp + "_" + uniqueId;
    }

    @Override
    public boolean isCodeUnique(String code) {
        return !productCardRepository.existsByCode(code);
    }

    @Override
    public Optional<MotorCardEntity> getMotorById(Long id) {
        return motorCardRepository.findById(id);
    }

    @Override
    public Optional<MotorWheelCardEntity> getMotorWheelById(Long id) {
        return motorWheelCardRepository.findById(id);
    }

    @Override
    public Optional<RadialWheelCardEntity> getRadialWheelById(Long id) {
        return radialWheelCardRepository.findById(id);
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

        // Поиск по электродвигателям
        List<MotorCardEntity> motors = motorCardRepository.searchByFields(likePattern);
        List<BaseProductCard> results = new ArrayList<>(motors);

        // Поиск по мотор-колёсам
        List<MotorWheelCardEntity> motorWheels = motorWheelCardRepository.searchByFields(likePattern);
        results.addAll(motorWheels);

        // Поиск по радиальным колёсам
        List<RadialWheelCardEntity> radialWheels = radialWheelCardRepository.searchByFields(likePattern);
        results.addAll(radialWheels);

        // TODO: добавить поиск по другим типам карточек

        return results;
    }
}

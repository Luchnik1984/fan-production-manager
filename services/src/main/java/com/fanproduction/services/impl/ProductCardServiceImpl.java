package com.fanproduction.services.impl;

import com.fanproduction.core.entity.component.ProductComponentEntity;
import com.fanproduction.core.entity.material.ProductMaterialRequirementEntity;
import com.fanproduction.core.entity.product.*;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.enums.CardTemplateType;
import com.fanproduction.core.enums.AssemblyUnitFieldType;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.core.security.CurrentUserProvider;
import com.fanproduction.repositories.product.*;
import com.fanproduction.services.ComponentService;
import com.fanproduction.services.FanCardService;
import com.fanproduction.services.MaterialService;
import com.fanproduction.services.ProductCardService;
import com.fanproduction.services.dto.ProductComponentItemData;
import com.fanproduction.services.dto.ProductMaterialItemData;
import com.fanproduction.services.factory.ProductCardFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCardServiceImpl implements ProductCardService {

    private final ProductCardRepository productCardRepository;
    private final MotorCardRepository motorCardRepository;
    private final MotorWheelCardRepository motorWheelCardRepository;
    private final RadialWheelCardRepository radialWheelCardRepository;
    private final AxialWheelCardRepository axialWheelCardRepository;
    private final ProductCardFactory cardFactory;
    private final FanCardService fanCardService;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentUserProvider currentUserProvider;
    private final FanCardRepository fanCardRepository;
    private final ComponentService componentService;
    private final MaterialService materialService;

    // ==========================================================
    // CREATE
    // ==========================================================

    @Override
    @Transactional
    public BaseProductCard createCard(CardTemplateType cardType, Map<String, Object> fields, String createdBy) {
        // FAN — отдельный путь через FanCardService
        if (cardType == CardTemplateType.FAN) {
            return createFanCard(fields, createdBy);
        }

        // Остальные типы — через фабрику
        BaseProductCard card = cardFactory.createCard(cardType, fields);
        card.setCreatedBy(createdBy);

        String code = generateCode(card);
        card.setCode(code);

        BaseProductCard savedCard = productCardRepository.save(card);

        eventPublisher.publishEvent(new AuditEvent(
                this,
                createdBy,
                AuditAction.CREATE_CARD,
                "Создана карточка: " + cardType.getDisplayName() + " - " + card.getName()
        ));

        log.info("Карточка создана: type={}, id={}, name={}",
                cardType, savedCard.getId(), savedCard.getName());

        return savedCard;
    }

    private BaseProductCard createFanCard(Map<String, Object> fields, String createdBy) {
        Object tvIdRaw = fields.get("templateVersionId");
        if (tvIdRaw == null) {
            throw new IllegalArgumentException(
                    "Для FAN-карточки обязательно поле 'templateVersionId'");
        }

        Long templateVersionId;
        try {
            templateVersionId = (tvIdRaw instanceof Number n)
                    ? n.longValue()
                    : Long.parseLong(tvIdRaw.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Поле 'templateVersionId' должно быть числом: " + tvIdRaw);
        }

        return fanCardService.createFromTemplate(templateVersionId, fields, createdBy);
    }

    // ==========================================================
    // UPDATE
    // ==========================================================

    @Override
    @Transactional
    public BaseProductCard updateCard(Long id, Map<String, Object> fields) {
        BaseProductCard existingCard = productCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена: " + id));

        CardTemplateType cardType = CardTemplateType.valueOf(existingCard.getCardType());

        // FAN — отдельный путь
        if (cardType == CardTemplateType.FAN) {
            return fanCardService.updateFromValues(id, fields, getCurrentUser());
        }

        // Остальные типы: применяем поля к существующей сущности
        // (не создаём новую с тем же id — так теряются createdAt, code, isTemporary)
        cardFactory.applyFields(existingCard, fields);

        BaseProductCard savedCard = productCardRepository.save(existingCard);

        eventPublisher.publishEvent(new AuditEvent(
                this,
                getCurrentUser(),
                AuditAction.UPDATE_CARD,
                "Обновлена карточка: " + savedCard.getName()
        ));

        log.info("Карточка обновлена: type={}, id={}, name={}",
                cardType, id, savedCard.getName());

        return savedCard;
    }

    // ==========================================================
    // READ
    // ==========================================================

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
    public Page<BaseProductCard> getFanCardsByFanType(String fanType, Pageable pageable) {
        return fanCardRepository.findByFanType(fanType, pageable)
                .map(card -> card);
    }

    // ==========================================================
    // DELETE
    // ==========================================================

    @Override
    @Transactional
    public void deleteCard(Long id) {
        BaseProductCard card = productCardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена: " + id));

        if (card.isTemporary()) {
            productCardRepository.deleteById(id);
            return;
        }

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

        productCardRepository.deleteById(id);

        eventPublisher.publishEvent(new AuditEvent(
                this,
                getCurrentUser(),
                AuditAction.DELETE_CARD,
                "Удалена карточка: " + card.getName()
        ));
    }

    // ==========================================================
    // CODE GENERATION
    // ==========================================================

    @Override
    public String generateCode(BaseProductCard card) {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        return card.getCardType().toLowerCase() + "_" + timestamp + "_" + uniqueId;
    }

    @Override
    public boolean isCodeUnique(String code) {
        return !productCardRepository.existsByCode(code);
    }

    // ==========================================================
    //  SEARCH
    // ==========================================================

    @Override
    public List<BaseProductCard> searchByFields(String query) {
        String likePattern = "%" + query.toLowerCase() + "%";

        List<MotorCardEntity> motors = motorCardRepository.searchByFields(likePattern);
        List<BaseProductCard> results = new ArrayList<>(motors);

        List<MotorWheelCardEntity> motorWheels = motorWheelCardRepository.searchByFields(likePattern);
        results.addAll(motorWheels);

        List<RadialWheelCardEntity> radialWheels = radialWheelCardRepository.searchByFields(likePattern);
        results.addAll(radialWheels);

        List<AxialWheelCardEntity> axialWheels = axialWheelCardRepository.searchByFields(likePattern);
        results.addAll(axialWheels);

        return results;
    }

    // ==========================================================
    // ПРОВЕРКИ ИСПОЛЬЗОВАНИЯ СБОРОЧНЫХ УЗЛОВ
    // ==========================================================

    @Override
    public boolean isAssemblyUnitUsedInFanCards(Long cardId) {
        try {
            BaseProductCard card = productCardRepository.findById(cardId)
                    .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена: " + cardId));

            String cardType = card.getCardType();
            long count;

            switch (cardType) {
                case "MOTOR" -> count = productCardRepository.countFanCardsUsingMotor(cardId);
                case "MOTOR_WHEEL" -> count = productCardRepository.countFanCardsUsingMotorWheel(cardId);
                case "RADIAL_WHEEL" -> count = productCardRepository.countFanCardsUsingRadialWheel(cardId);
                case "AXIAL_WHEEL" -> count = productCardRepository.countFanCardsUsingAxialWheel(cardId);
                default -> {
                    return false;
                }
            }

            return count > 0;
        } catch (Exception e) {
            log.error("Error in isAssemblyUnitUsedInFanCards: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<String> getAssemblyUnitUsageInfo(Long cardId) {
        List<String> usage = new ArrayList<>();

        try {
            BaseProductCard card = productCardRepository.findById(cardId)
                    .orElseThrow(() -> new IllegalArgumentException("Карточка не найдена: " + cardId));

            String cardType = card.getCardType();
            List<FanCardEntity> fanCards;

            switch (cardType) {
                case "MOTOR" -> fanCards = productCardRepository.findFanCardsByMotorId(cardId);
                case "MOTOR_WHEEL" -> fanCards = productCardRepository.findFanCardsByMotorWheelId(cardId);
                case "RADIAL_WHEEL" -> fanCards = productCardRepository.findFanCardsByRadialWheelId(cardId);
                case "AXIAL_WHEEL" -> fanCards = productCardRepository.findFanCardsByAxialWheelId(cardId);
                default -> {
                    return usage;
                }
            }

            for (FanCardEntity fanCard : fanCards) {
                usage.add(String.format("%s (ID: %d)", fanCard.getName(), fanCard.getId()));
            }
        } catch (Exception e) {
            log.error("Error in getAssemblyUnitUsageInfo: {}", e.getMessage(), e);
        }

        return usage;
    }

    // ==========================================================
    // АТОМАРНОЕ СОХРАНЕНИЕ (FR11)
    // ==========================================================

    @Override
    @Transactional
    public BaseProductCard createFullCard(CardTemplateType cardType,
                                          String name,
                                          Map<String, Object> fields,
                                          List<ProductComponentItemData> components,
                                          List<ProductMaterialItemData> materials,
                                          String createdBy) {
        Map<String, Object> safeFields = buildSafeFields(fields, name);

        BaseProductCard savedCard = createCard(cardType, safeFields, createdBy);
        Long cardId = savedCard.getId();

        // Применяем компоненты
        if (components != null) {
            for (ProductComponentItemData c : components) {
                componentService.addComponentToProduct(
                        cardId,
                        c.componentId(),
                        c.quantity() != null ? c.quantity() : 1.0,
                        c.position(),
                        c.note()
                );
            }
        }

        // Применяем материалы
        if (materials != null) {
            for (ProductMaterialItemData m : materials) {
                materialService.addMaterialToProduct(
                        cardId,
                        m.materialId(),
                        m.quantityPerUnit() != null ? m.quantityPerUnit() : 1.0,
                        m.note()
                );
            }
        }

        log.info("createFullCard: cardId={}, components={}, materials={}",
                cardId,
                components != null ? components.size() : 0,
                materials != null ? materials.size() : 0);

        return savedCard;
    }

    @Override
    @Transactional
    public BaseProductCard updateFullCard(Long id,
                                          String name,
                                          Map<String, Object> fields,
                                          List<ProductComponentItemData> components,
                                          List<Long> componentsToRemove,
                                          List<ProductMaterialItemData> materials,
                                          List<Long> materialsToRemove) {
        Map<String, Object> safeFields = buildSafeFields(fields, name);

        BaseProductCard updatedCard = updateCard(id, safeFields);

        // Удаляем указанные компоненты
        if (componentsToRemove != null) {
            for (Long componentId : componentsToRemove) {
                try {
                    componentService.removeComponentFromProduct(id, componentId);
                } catch (Exception e) {
                    log.debug("Компонент {} уже отсутствует у карточки {}", componentId, id);
                }
            }
        }

        // Обрабатываем компоненты (add/update)
        if (components != null && !components.isEmpty()) {
            Map<Long, ProductComponentEntity> existingMap =
                    componentService.getComponentsByProductCard(id).stream()
                            .collect(Collectors.toMap(
                                    ProductComponentEntity::getComponentId,
                                    x -> x,
                                    (a, b) -> a));

            for (ProductComponentItemData c : components) {
                ProductComponentEntity existing = existingMap.get(c.componentId());

                if (existing != null) {
                    if (c.quantity() != null) {
                        componentService.updateComponentQuantity(id, c.componentId(), c.quantity());
                    }
                    if (c.position() != null) {
                        componentService.updateComponentPosition(id, c.componentId(), c.position());
                    }
                    if (c.note() != null) {
                        componentService.updateComponentNote(id, c.componentId(), c.note());
                    }
                } else {
                    componentService.addComponentToProduct(
                            id,
                            c.componentId(),
                            c.quantity() != null ? c.quantity() : 1.0,
                            c.position(),
                            c.note()
                    );
                }
            }
        }

        // Удаляем указанные материалы
        if (materialsToRemove != null) {
            for (Long materialId : materialsToRemove) {
                try {
                    materialService.removeMaterialFromProduct(id, materialId);
                } catch (Exception e) {
                    log.debug("Материал {} уже отсутствует у карточки {}", materialId, id);
                }
            }
        }

        // Обрабатываем материалы (add/update)
        if (materials != null && !materials.isEmpty()) {
            Map<Long, ProductMaterialRequirementEntity> existingMap =
                    materialService.getMaterialsByProductCard(id).stream()
                            .collect(Collectors.toMap(
                                    ProductMaterialRequirementEntity::getMaterialId,
                                    x -> x,
                                    (a, b) -> a));

            for (ProductMaterialItemData m : materials) {
                ProductMaterialRequirementEntity existing = existingMap.get(m.materialId());

                if (existing != null) {
                    if (m.quantityPerUnit() != null) {
                        materialService.updateMaterialQuantity(id, m.materialId(), m.quantityPerUnit());
                    }
                    if (m.note() != null) {
                        materialService.updateMaterialNote(id, m.materialId(), m.note());
                    }
                } else {
                    materialService.addMaterialToProduct(
                            id,
                            m.materialId(),
                            m.quantityPerUnit() != null ? m.quantityPerUnit() : 1.0,
                            m.note()
                    );
                }
            }
        }

        log.info("updateFullCard: cardId={}, components={}, materials={}",
                id,
                components != null ? components.size() : 0,
                materials != null ? materials.size() : 0);

        return updatedCard;
    }

    /**
     * Подготовить Map полей: копия + добавление name, если задано.
     */
    private Map<String, Object> buildSafeFields(Map<String, Object> fields, String name) {
        Map<String, Object> safeFields = fields != null
                ? new HashMap<>(fields)
                : new HashMap<>();
        if (name != null && !name.isBlank()) {
            safeFields.put("name", name);
        }
        return safeFields;
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    private String getCurrentUser() {
        String email = currentUserProvider.getCurrentUserEmail();
        return email != null ? email : "system";
    }
}

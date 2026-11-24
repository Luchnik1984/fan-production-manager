package com.fanproduction.template.service;

import com.fanproduction.template.entity.CardRole;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.repository.CardRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Справочник ролей продукции (US10.7.2).
 * <p>
 * Роль — назначение изделия при комплектации (осевое колесо, клапан…).
 * Системные роли неизменяемы; администратор может добавлять свои.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardRoleService {

    private final CardRoleRepository cardRoleRepository;

    public List<CardRole> getAll() {
        return cardRoleRepository.findAllByOrderBySortOrderAscIdAsc();
    }

    /**
     * Добавить роль. Код приводится к верхнему регистру.
     */
    @Transactional
    public CardRole create(String code, String name, String createdBy) {
        if (code == null || code.isBlank()) {
            throw new TemplateValidationException("Код роли обязателен");
        }
        if (name == null || name.isBlank()) {
            throw new TemplateValidationException("Наименование роли обязательно");
        }
        String cleanCode = code.trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        if (cardRoleRepository.existsByCodeIgnoreCase(cleanCode)) {
            throw new TemplateValidationException("Роль с кодом '" + cleanCode + "' уже существует");
        }

        CardRole role = new CardRole();
        role.setCode(cleanCode);
        role.setName(name.trim());
        role.setIsSystem(false);
        role.setSortOrder(100);
        role.setCreatedBy(createdBy);

        CardRole saved = cardRoleRepository.save(role);
        log.info("Роль продукции создана: code={}", saved.getCode());
        return saved;
    }
}

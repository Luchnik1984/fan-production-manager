package com.fanproduction.core.enums;

/**
 * Действия, подлежащие аудиту.
 */
public enum AuditAction {

    // Аутентификация и пользователи
    LOGIN_SUCCESS,
    LOGIN_FAILED,
    LOGOUT,
    REGISTER,
    APPROVE_USER,
    REJECT_USER,
    BLOCK_USER,
    UNBLOCK_USER,

    // Карточки продукции
    CREATE_CARD,
    UPDATE_CARD,
    DELETE_CARD,

    // Генерация документов
    GENERATE_TZ,
    GENERATE_PASSPORT,
    GENERATE_PLATE,

    // Профиль и пароль
    UPDATE_PROFILE,
    CHANGE_PASSWORD,

    // Аудит
    UPDATE_AUDIT_SETTINGS,

    // ========== Конструктор шаблонов (Спринт 7) ==========
    TEMPLATE_CREATE,
    TEMPLATE_UPDATE,
    TEMPLATE_DELETE,
    TEMPLATE_PUBLISH,
    TEMPLATE_DEPRECATE,
    TEMPLATE_CLONE,

    // ========== Миграция и клонирование карточек (FR12) ==========
    CARD_MIGRATED_TO_TEMPLATE_VERSION,
    CARD_CLONED_TO_TEMPLATE_VERSION
}

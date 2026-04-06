package com.fanproduction.core.enums;

/**
 * Роли пользователей в системе
 */
public enum Role {
    ENGINEER,   /// Инженер производства
    MANAGER,    /// Менеджер (может смотреть, но не редактировать)
    ADMIN       /// Администратор (полный доступ)
}

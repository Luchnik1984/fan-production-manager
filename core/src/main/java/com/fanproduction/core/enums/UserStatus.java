package com.fanproduction.core.enums;

/**
 * Статус учётной записи пользователя
 */
public enum UserStatus {

    PENDING,    // ожидает подтверждения администратором
    ACTIVE,     // активен, может работать в системе
    REJECTED,   // отклонён администратором (с указанием причины)
    BLOCKED     // заблокирован за нарушения
}

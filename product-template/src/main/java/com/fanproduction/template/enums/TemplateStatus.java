package com.fanproduction.template.enums;

/**
 * Статус версии шаблона.
 * <p>
 * Жизненный цикл:
 * <pre>
 * DRAFT → PUBLISHED → LEGACY → DEPRECATED → ARCHIVED
 * </pre>
 * <ul>
 *   <li>{@link #DRAFT}       — черновик, виден только администратору. Карточки создать нельзя.</li>
 *   <li>{@link #PUBLISHED}   — текущая версия, используется по умолчанию.</li>
 *   <li>{@link #LEGACY}      — старая, но поддерживаемая. Карточки можно создавать по явному выбору.</li>
 *   <li>{@link #DEPRECATED}  — устаревшая. Новые карточки нельзя.</li>
 *   <li>{@link #ARCHIVED}    — скрыта из UI. Данные сохранены для существующих карточек.</li>
 * </ul>
 */
public enum TemplateStatus {
    DRAFT,
    PUBLISHED,
    LEGACY,
    DEPRECATED,
    ARCHIVED
}

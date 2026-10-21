package com.fanproduction.core.entity.product;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Метаданные полей {@link FanCardEntity}.
 * <p>
 * Содержит множество «системных» полей — тех, что хранятся в отдельных колонках
 * таблицы {@code fan_card}. Все остальные поля карточки идут в {@code dynamic_fields}
 * (JSONB).
 * <p>
 * Множество вычисляется **автоматически** через reflection при загрузке класса.
 * Это гарантирует синхронизацию с {@link FanCardEntity}: добавили поле — оно
 * автоматически становится системным. Никакой ручной поддержки списка.
 * <p>
 * Исключения:
 * <ul>
 *   <li>{@code dynamicFields} — не системное, хранит пользовательские поля.</li>
 *   <li>Поля {@code BaseProductCard} ({@code id}, {@code name}, {@code code},
 *       {@code cardType}, {@code createdAt}, {@code updatedAt}, {@code createdBy})
 *       — обрабатываются отдельно.</li>
 * </ul>
 */
public final class FanCardFields {

    /**
     * Множество системных полей {@link FanCardEntity} (все {@code declaredFields},
     * кроме {@code dynamicFields}).
     */
    public static final Set<String> SYSTEM_FIELDS = computeSystemFields();

    private FanCardFields() {
        // Утилитный класс
    }

    private static Set<String> computeSystemFields() {
        Set<String> result = new HashSet<>();
        for (Field field : FanCardEntity.class.getDeclaredFields()) {
            String name = field.getName();

            // dynamicFields хранит пользовательские поля — не системное
            if ("dynamicFields".equals(name)) {
                continue;
            }

            result.add(name);
        }
        return Collections.unmodifiableSet(result);
    }
}

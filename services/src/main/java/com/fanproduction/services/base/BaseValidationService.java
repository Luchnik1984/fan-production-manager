package com.fanproduction.services.base;

import java.util.Optional;
import java.util.function.Supplier;

public abstract class BaseValidationService {

    /**
     * Проверяет уникальность и выбрасывает исключение при дубликате
     */
    protected <T> void checkUnique(Supplier<Optional<T>> finder, String errorMessage) {
        finder.get().ifPresent(existing -> {
            throw new IllegalArgumentException(errorMessage);
        });
    }

    /**
     * Проверяет уникальность при обновлении (исключает текущую запись)
     */
    protected <T> void checkUniqueOnUpdate(Supplier<Optional<T>> finder, Long currentId, String errorMessage) {
        finder.get().ifPresent(existing -> {
            // Получаем ID существующей записи через рефлексию или предполагаем, что T имеет метод getId()
            Long existingId = extractId(existing);
            if (!existingId.equals(currentId)) {
                throw new IllegalArgumentException(errorMessage);
            }
        });
    }

    /**
     * Извлекает ID из сущности (предполагается, что сущность имеет метод getId())
     */
    private Long extractId(Object entity) {
        try {
            return (Long) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            throw new IllegalArgumentException("Не удалось получить ID сущности");
        }
    }
}

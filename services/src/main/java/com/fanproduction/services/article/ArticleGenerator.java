package com.fanproduction.services.article;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Генератор артикулов (US10.7.7).
 * <p>
 * Артикул — внутренний идентификатор изделия: {@code PREFIX-000123}.
 * Выдаётся атомарно из таблицы {@code article_sequence}, поэтому
 * параллельное создание карточек не приводит к дублям.
 * <p>
 * Номер не переиспользуется: удаление карточки счётчик не откатывает.
 */
@Slf4j
@Service
public class ArticleGenerator {

    private static final String NEXT_VALUE_SQL = """
            INSERT INTO article_sequence (prefix, next_value)
            VALUES (:prefix, 1)
            ON CONFLICT (prefix)
            DO UPDATE SET next_value = article_sequence.next_value + 1
            RETURNING next_value
            """;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Следующий артикул для префикса.
     *
     * @param prefix префикс типа продукции или справочника (AW, RW, CMP…)
     * @return артикул вида {@code AW-000123}
     */
    @Transactional
    public String next(String prefix) {
        String cleanPrefix = normalizePrefix(prefix);

        Object raw = entityManager
                .createNativeQuery(NEXT_VALUE_SQL)
                .setParameter("prefix", cleanPrefix)
                .getSingleResult();

        long number = ((Number) raw).longValue();
        String article = String.format("%s-%06d", cleanPrefix, number);

        log.debug("Выдан артикул: {}", article);
        return article;
    }

    /** Префикс: только заглавные буквы и цифры, максимум 8 символов. */
    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "GEN";
        }
        String clean = prefix.trim().toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (clean.isEmpty()) {
            return "GEN";
        }
        return clean.length() > 8 ? clean.substring(0, 8) : clean;
    }
}

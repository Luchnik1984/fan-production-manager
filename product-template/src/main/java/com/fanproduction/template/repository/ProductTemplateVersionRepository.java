package com.fanproduction.template.repository;

import com.fanproduction.template.entity.ProductTemplateVersion;
import com.fanproduction.template.enums.TemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий версий шаблонов.
 */
@Repository
public interface ProductTemplateVersionRepository extends JpaRepository<ProductTemplateVersion, Long> {

    /**
     * Все версии шаблона, отсортированные по номеру версии (убывание).
     */
    List<ProductTemplateVersion> findByTemplateIdOrderByVersionDesc(Long templateId);

    /**
     * Найти версию по номеру в рамках шаблона.
     */
    Optional<ProductTemplateVersion> findByTemplateIdAndVersion(Long templateId, Integer version);

    /**
     * Первая версия шаблона с указанным статусом (для PUBLISHED — единственная).
     */
    Optional<ProductTemplateVersion> findFirstByTemplateIdAndStatus(Long templateId, TemplateStatus status);

    /**
     * Все версии шаблона с указанным статусом.
     */
    List<ProductTemplateVersion> findByTemplateIdAndStatus(Long templateId, TemplateStatus status);

    /**
     * Все версии шаблона с любым из указанных статусов.
     */
    List<ProductTemplateVersion> findByTemplateIdAndStatusIn(Long templateId, Collection<TemplateStatus> statuses);

    /**
     * Проверить, есть ли у шаблона версия в указанном статусе.
     */
    boolean existsByTemplateIdAndStatus(Long templateId, TemplateStatus status);

    /**
     * Количество версий у шаблона.
     */
    long countByTemplateId(Long templateId);

    /**
     * Максимальный номер версии у шаблона.
     * Возвращает Optional.empty(), если версий нет.
     */
    @Query("SELECT MAX(v.version) FROM ProductTemplateVersion v WHERE v.templateId = :templateId")
    Optional<Integer> findMaxVersion(@Param("templateId") Long templateId);
}

package com.fanproduction.repositories.settings;

import com.fanproduction.core.entity.settings.AuditSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditSettingsRepository extends JpaRepository<AuditSettingsEntity, Long> {

    // Так как в таблице только одна строка, можно получать первую
    default AuditSettingsEntity findFirst() {
        return findAll().stream().findFirst().orElse(null);
    }
}
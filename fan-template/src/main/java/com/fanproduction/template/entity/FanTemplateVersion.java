package com.fanproduction.template.entity;

import com.fanproduction.template.enums.TemplateStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Версия шаблона карточки вентилятора.
 * <p>
 * Неизменяемый снимок полей и правила маркировки.
 * После публикации содержимое {@code fieldsJson} и {@code markingRuleJson}
 * не редактируется — создаётся новая версия.
 * <p>
 * Поля {@code fieldsJson} и {@code markingRuleJson} хранятся как JSONB.
 * На этапе US7.3 объявлены как {@code Map<String, Object>}. В US7.8
 * будут заменены на типизированные структуры ({@code List<FieldDefinition>},
 * {@code MarkingRule}).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fan_template_version")
public class FanTemplateVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на логический шаблон.
     */
    @Column(name = "template_id", nullable = false)
    private Long templateId;

    /**
     * Номер версии в рамках шаблона (1, 2, 3...).
     */
    @Column(name = "version", nullable = false)
    private Integer version;

    /**
     * Статус версии.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TemplateStatus status;

    /**
     * Список полей шаблона (JSONB).
     * На этапе US7.3 — Map. В US7.8 станет List<FieldDefinition>.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fields_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> fieldsJson;

    /**
     * Правило маркировки (JSONB).
     * На этапе US7.3 — Map. В US7.8 станет MarkingRule.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "marking_rule_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> markingRuleJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "published_by", length = 100)
    private String publishedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
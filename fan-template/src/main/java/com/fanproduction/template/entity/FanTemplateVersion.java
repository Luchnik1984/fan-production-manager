package com.fanproduction.template.entity;

import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Версия шаблона карточки вентилятора.
 * <p>
 * Неизменяемый снимок полей и правила маркировки.
 * Поля {@code fieldsJson} и {@code markingRuleJson} хранятся как JSONB.
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

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TemplateStatus status;

    /**
     * Список полей шаблона (JSONB).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fields_json", nullable = false, columnDefinition = "jsonb")
    private List<FieldDefinition> fieldsJson;

    /**
     * Правило маркировки (JSONB).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "marking_rule_json", nullable = false, columnDefinition = "jsonb")
    private MarkingRule markingRuleJson;

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
        if (fieldsJson == null) {
            fieldsJson = List.of();
        }
        if (markingRuleJson == null) {
            markingRuleJson = MarkingRule.empty();
        }
    }
}
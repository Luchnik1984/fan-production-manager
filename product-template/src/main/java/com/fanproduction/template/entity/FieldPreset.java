package com.fanproduction.template.entity;

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
 * Пресет полей шаблона — готовый набор определений полей.
 * <p>
 * Позволяет администратору не заводить вручную десятки полей для типового
 * изделия («Осевое колесо», «Радиальное колесо», «Электродвигатель AC»),
 * а применить готовый пресет к DRAFT-версии шаблона.
 * <p>
 * Формат {@code fieldsJson} совпадает с
 * {@code product_template_version.fields_json}, поэтому копирование
 * выполняется без конвертаций.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "field_preset")
public class FieldPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название пресета, например «Осевое колесо». Уникально (регистронезависимо).
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    /**
     * Набор полей в формате версии шаблона.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fields_json", nullable = false, columnDefinition = "jsonb")
    private List<FieldDefinition> fieldsJson;

    /**
     * Правило маркировки в формате версии шаблона (US10.7.3).
     * Применяется вместе с полями, если у версии правило ещё пустое.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "marking_rule_json", nullable = false, columnDefinition = "jsonb")
    private MarkingRule markingRuleJson;

    /**
     * Системный пресет — нельзя изменить или удалить.
     */
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isSystem == null) {
            isSystem = false;
        }
        if (fieldsJson == null) {
            fieldsJson = List.of();
        }
        if (markingRuleJson == null) {
            markingRuleJson = MarkingRule.empty();
        }
    }
}

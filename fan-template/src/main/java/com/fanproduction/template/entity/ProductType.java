package com.fanproduction.template.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Тип вентилятора (верхний уровень иерархии).
 * <p>
 * Пример: «Вентилятор канальный (VRK-PatAIR)».
 * <p>
 * Тип участвует в маркировке всех вентиляторов этого типа:
 * они начинаются с designation (например, «VRK-PatAIR-...»).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_type")
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Наименование типа, например «Вентилятор канальный».
     * Может быть пустым — тогда отображаемое имя = designation.
     */
    @Column(name = "name", length = 200)
    private String name;

    /**
     * Обозначение типа, например «VRK-PatAIR».
     * Уникальное (регистронезависимо). Обязательное.
     */
    @Column(name = "designation", nullable = false, length = 50)
    private String designation;

    /**
     * Системный тип — нельзя удалить.
     */
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    /**
     * Раздел продукции, к которому относится тип.
     * {@code null} — тип ещё не распределён по разделам.
     */
    @Column(name = "section_id")
    private Long sectionId;

    /**
     * Префикс номера изделия для журнала: ED, MW, RW, AW, VRK.
     * Уникальный. Для динамических типов задаётся администратором.
     */
    @Column(name = "code_prefix", length = 4)
    private String codePrefix;

    /**
     * Хардкод-тип: MOTOR, MOTOR_WHEEL, RADIAL_WHEEL, AXIAL_WHEEL.
     * {@code null} — динамический тип (поля и маркировка задаются шаблоном).
     */
    @Column(name = "system_card_type", length = 30)
    private String systemCardType;

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
    }
}

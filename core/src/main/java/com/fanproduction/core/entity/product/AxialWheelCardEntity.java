package com.fanproduction.core.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Карточка осевого колеса.
 *
 * Поддерживает два типа колес:
 * - Партнёрское (свободная маркировка)
 * - Фирменное (сборное из компонентов или сварное из материалов)
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "axial_wheel_card")
public class AxialWheelCardEntity extends BaseProductCard {

    // ==========================================================
    // 1. ОСНОВНАЯ ИНФОРМАЦИЯ
    // ==========================================================

    /**
     * Производитель
     */
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    /**
     * Типоразмер (например, 5,6; 6,3)
     */
    @Column(name = "size")
    private Double size;

    /**
     * Коэффициент подрезки (%)
     */
    @Column(name = "trim_coefficient")
    private Double trimCoefficient;

    // ==========================================================
    // 2. ТИП КОЛЕСА
    // ==========================================================

    /**
     * Партнёрское рабочее колесо
     */
    @Column(name = "is_partner_wheel")
    private Boolean isPartnerWheel = false;

    /**
     * Фирменное рабочее колесо
     */
    @Column(name = "is_own_production")
    private Boolean isOwnProduction = false;

    /**
     * Маркировка производителя (для партнёрского колеса)
     */
    @Column(name = "marking", length = 100)
    private String marking;

    // ==========================================================
    // 3. ТИП ИЗГОТОВЛЕНИЯ (для фирменного колеса)
    // ==========================================================

    /**
     * Сборное из компонентов
     */
    @Column(name = "is_assembled_from_components")
    private Boolean isAssembledFromComponents = false;

    /**
     * Сварное из материалов
     */
    @Column(name = "is_welded_from_materials")
    private Boolean isWeldedFromMaterials = false;

    // ==========================================================
    // 4. ХАБ КОЛЕСА
    // ==========================================================

    /**
     * Тип хаба (для сварного колеса, заполняется вручную)
     * Пример: 109_50/6-6
     */
    @Column(name = "wheel_hub_type", length = 100)
    private String wheelHubType;

    /**
     * Ссылка на компонент "Хаб" (для сборного колеса)
     */
    @Column(name = "wheel_hub_component_id")
    private Long wheelHubComponentId;

    /**
     * Максимально возможное количество лопаток для данного хаба
     */
    @Column(name = "max_blade_count")
    private Integer maxBladeCount;

    // ==========================================================
    // 5. ЛОПАТКА КОЛЕСА
    // ==========================================================

    /**
     * Тип лопатки (для сварного колеса, заполняется вручную)
     * Пример: 109_50
     */
    @Column(name = "blade_type", length = 100)
    private String bladeType;

    /**
     * Ссылка на компонент "Лопатка" (для сборного колеса)
     */
    @Column(name = "blade_component_id")
    private Long bladeComponentId;

    /**
     * Материал лопатки
     * - Для сборного: заполняется автоматически из компонента
     * - Для сварного: заполняется вручную
     */
    @Column(name = "blade_material", length = 100)
    private String bladeMaterial;

    /**
     * Количество установленных лопаток
     */
    @Column(name = "blade_count")
    private Integer bladeCount;

    /**
     * Угол установки лопаток (градусы)
     */
    @Column(name = "blade_angle")
    private Integer bladeAngle;

    // ==========================================================
    // 6. УСТАНОВОЧНАЯ СТУПИЦА
    // ==========================================================

    /**
     * Ссылка на компонент "Установочная ступица"
     */
    @Column(name = "hub_component_id")
    private Long hubComponentId;

    // ==========================================================
    // 7. РАСЧЁТНЫЕ ПОЛЯ
    // ==========================================================

    /**
     * Диаметр колеса (рассчитывается автоматически)
     * Формула: size * (100 - trimCoefficient)
     */
    @Column(name = "wheel_diameter")
    private Integer wheelDiameter;

    /**
     * Формула колеса (формируется автоматически)
     * Формат: диаметр/количество-максКоличество/лопатка/угол/материал
     */
    @Column(name = "wheel_formula", length = 200)
    private String wheelFormula;

    // ==========================================================
    // 8. ОБЩИЕ ПОЛЯ
    // ==========================================================

    /**
     * Максимальная скорость вращения (об/мин)
     */
    @Column(name = "max_speed_rpm")
    private Integer maxSpeedRpm;

    /**
     * Масса (кг)
     */
    @Column(name = "weight_kg")
    private Double weightKg;

    // ==========================================================
    // 9. ИСПОЛНЕНИЕ
    // ==========================================================

    @Column(name = "general_purpose")
    private Boolean generalPurpose = true;

    @Column(name = "fireproof")
    private Boolean fireproof = false;

    @Column(name = "fireproof_marking", length = 50)
    private String fireproofMarking;

    @Column(name = "max_temperature")
    private Integer maxTemperature;

    @Column(name = "explosion_proof")
    private Boolean explosionProof = false;

    @Column(name = "explosion_marking", length = 100)
    private String explosionMarking;

    // ==========================================================
    // 10. ПОЛНАЯ МАРКИРОВКА
    // ==========================================================

    @Column(name = "full_marking", length = 200)
    private String fullMarking;
}
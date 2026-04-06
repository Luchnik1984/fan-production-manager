package com.fanproduction.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Карточка мотор-колеса.
 * Используется в канальных вентиляторах с вперед-загнутыми лопатками.
 * Пример: RO310F-4D
 * Расшифровка:
 * - RO: тип лопаток (впередзагнутые)
 * - 310: размер
 * - F: серия
 * - 4: количество полюсов
 * - D: рабочее напряжение (380В)
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "motor_wheel_card")
public class MotorWheelCardEntity extends BaseProductCard {

    /**
     * Производитель
     */
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    /**
     * Тип лопаток
     * - RO: впередзагнутые (Radial forward)
     * - RE: назадзагнутые (Radial backward)
     */
    @Column(name = "blade_type", length = 10)
    private String bladeType;

    /**
     * Размер мотор-колеса (например: 310)
     */
    @Column(name = "size")
    private Integer size;

    /**
     * Количество полюсов (2, 4, 6, 8)
     */
    @Column(name = "poles")
    private Integer poles;

    /**
     * Код напряжения
     * - E: 220В
     * - D: 380В
     */
    @Column(name = "voltage_code", length = 10)
    private String voltageCode;

    /**
     * Мощность (КВт)
     */
    @Column(name = "power_kw")
    private Double powerKw;

    /**
     * Скорость вращения для маркировки (об/мин)
     * Рассчитывается: 6000 / poles
     */
    @Column(name = "rated_speed_rpm")
    private Integer ratedSpeedRpm;

    /**
     * Фактическая скорость вращения (об/мин)
     */
    @Column(name = "actual_speed_rpm")
    private Integer actualSpeedRpm;

    /**
     * Рабочее напряжение (220, 380)
     */
    @Column(name = "voltage")
    private Integer voltage;

    /**
     * Масса (кг)
     */
    @Column(name = "weight_kg")
    private Double weightKg;
}

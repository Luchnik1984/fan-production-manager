package com.fanproduction.template.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Серия вентилятора (третий уровень иерархии).
 * <p>
 * Пример: «P», «PS», «PKV», «N», «Vn».
 * <p>
 * Серия участвует в маркировке: если у серии есть name,
 * оно входит в маркировку (например, «VRK-PatAIR-P-...»).
 * Если name = NULL, в дереве отображается как «No_series».
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fan_series")
public class FanSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на вид.
     */
    @Column(name = "species_id", nullable = false)
    private Long speciesId;

    /**
     * Наименование серии. NULL = «No_series».
     * Уникально в рамках вида (регистронезависимо).
     */
    @Column(name = "name", length = 100)
    private String name;

    /**
     * Системная серия — нельзя удалить.
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
    }
}

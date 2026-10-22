package com.fanproduction.gui.dto.fan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Правило маркировки — последовательность элементов.
 * <p>
 * Дублирует серверный {@code com.fanproduction.template.model.MarkingRule}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkingRuleDto {

    private List<MarkingRuleElementDto> sequence;

    public static MarkingRuleDto empty() {
        return new MarkingRuleDto(List.of());
    }
}

package com.fanproduction.gui.dto.fan;

import com.fanproduction.gui.dto.config.ConditionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Элемент правила маркировки.
 * <p>
 * Дублирует серверный {@code com.fanproduction.template.model.MarkingRuleElement}.
 * <ul>
 *   <li>{@code LITERAL} / {@code SEPARATOR} — используется {@link #value}.</li>
 *   <li>{@code FIELD} — используется {@link #fieldKey}.</li>
 *   <li>{@code CONDITION} — используются {@link #condition}, {@link #then}, {@link #otherwise}.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarkingRuleElementDto {

    private RuleElementType type;

    private String value;

    private String fieldKey;

    private ConditionDto condition;

    private List<MarkingRuleElementDto> then;

    private List<MarkingRuleElementDto> otherwise;
}

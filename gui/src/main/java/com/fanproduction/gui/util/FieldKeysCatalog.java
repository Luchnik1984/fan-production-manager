package com.fanproduction.gui.util;

import com.fanproduction.gui.dto.fan.FieldDefinitionDto;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Каталог доступных ключей полей для редакторов правил и условий.
 * <p>
 * Используется в {@code FieldBlockEditDialog} и {@code ConditionEditDialog}.
 * <p>
 * Состав:
 * <ol>
 *   <li>Поля текущего шаблона (из {@code version.getFieldsJson()}).</li>
 *   <li>Системные поля метаданных шаблона (US9.8):
 *       typeDesignation, typeName, seriesName, speciesName.</li>
 *   <li>Системные вычисляемые поля (ComputedFieldsLibrary).</li>
 * </ol>
 * <p>
 * Порядок сохраняется (LinkedHashSet): сначала поля шаблона,
 * потом системные метаданные, потом вычисляемые.
 */
public final class FieldKeysCatalog {

    /** Системные поля метаданных шаблона (US9.8). */
    public static final List<String> SYSTEM_METADATA_FIELDS = List.of(
            "typeDesignation",
            "typeName",
            "seriesName",
            "speciesName"
    );

    /** Системные вычисляемые поля (ComputedFieldsLibrary). */
    public static final List<String> SYSTEM_COMPUTED_FIELDS = List.of(
            "wheelSizeMarking", "voltageCode", "executionMarking",
            "sizeMarking", "powerMarking", "bladeMarking",
            "wheelDiameter", "wheelFormulaAxial", "wheelFormulaRadial",
            "fireproofMarking", "explosionMarking",
            "climateTypeMarking", "positionMarking", "connectionTypeMarking"
    );

    /** Все системные поля (метаданные + вычисляемые). */
    public static final List<String> ALL_SYSTEM_FIELDS;

    static {
        java.util.ArrayList<String> all = new java.util.ArrayList<>();
        all.addAll(SYSTEM_METADATA_FIELDS);
        all.addAll(SYSTEM_COMPUTED_FIELDS);
        ALL_SYSTEM_FIELDS = List.copyOf(all);
    }

    private FieldKeysCatalog() {
    }

    /**
     * Собрать полный список ключей:
     * поля шаблона + системные (метаданные + вычисляемые).
     */
    public static Set<String> collect(List<FieldDefinitionDto> templateFields) {
        Set<String> result = new LinkedHashSet<>();
        if (templateFields != null) {
            for (FieldDefinitionDto f : templateFields) {
                if (f != null && f.getKey() != null) {
                    result.add(f.getKey());
                }
            }
        }
        result.addAll(ALL_SYSTEM_FIELDS);
        return result;
    }
}

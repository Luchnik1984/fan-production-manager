package com.fanproduction.template.formula;

import com.fanproduction.template.formula.function.FormulaFunctions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверка формул системных пресетов колёс (US10.7.3a).
 * <p>
 * Формулы совпадают с теми, что лежат в V24__create_field_preset.sql.
 * Тест ловит расхождение с DSL: ошибки синтаксиса находятся при сборке,
 * а не при публикации шаблона у пользователя.
 */
class PresetFormulaTest {

    /** Как в пресете «Осевое колесо». */
    private static final String AXIAL_DIAMETER =
            "ROUND(MULTIPLY(FIELD('size'), SUBTRACT(100, FIELD('trimCoefficient'))))";

    private static final String AXIAL_FORMULA =
            "CONCAT(FIELD('wheelDiameter'), '/', FIELD('bladeCount'), '-', FIELD('maxBladeCount'), "
                    + "'/', FIELD('bladeDesignation'), '/', FIELD('bladeAngle'), '/', FIELD('bladeMaterial'))";

    /** Как в пресете «Радиальное колесо». */
    private static final String RADIAL_FORMULA =
            "CONCAT(FIELD('bladeType'), '.', FIELD('wheelCode'), '/', FIELD('frontDiskMod'), '.', "
                    + "FIELD('wheelWidth'), '/', FIELD('bladeCount'), '/', FIELD('bladeLengthCoeff'))";

    /** Как в пресетах колёс (US10.7.4). */
    private static final String FIRE_MARKING =
            "CONCAT('F-', FIELD('fireResistanceHours'), '/', FIELD('maxTemperature'))";

    private final FormulaEngine engine = createEngine();

    /** Регистрация функций живёт в {@code @PostConstruct} — вызываем явно. */
    private static FormulaEngine createEngine() {
        FormulaFunctions functions = new FormulaFunctions();
        functions.init();
        return new FormulaEngineImpl(functions, null);
    }

    @Test
    @DisplayName("диаметр осевого колеса: 6,3 мм с подрезкой 1% -> 624")
    void axialDiameter() {
        Map<String, Object> values = new HashMap<>();
        values.put("size", 6.3);
        values.put("trimCoefficient", 1);

        assertThat(engine.evaluateToString(AXIAL_DIAMETER, values)).isEqualTo("624");
    }

    @Test
    @DisplayName("формула осевого колеса: 624/3-9/4ZL/43/PAG")
    void axialFormula() {
        Map<String, Object> values = new HashMap<>();
        values.put("wheelDiameter", 624);
        values.put("bladeCount", 3);
        values.put("maxBladeCount", 9);
        values.put("bladeDesignation", "4ZL");
        values.put("bladeAngle", 43);
        values.put("bladeMaterial", "PAG");

        assertThat(engine.evaluateToString(AXIAL_FORMULA, values))
                .isEqualTo("624/3-9/4ZL/43/PAG");
    }

    @Test
    @DisplayName("формула радиального колеса собирается из кода лопаток и модификаций")
    void radialFormula() {
        // TODO(10.7.4): ширину по документу надо печатать как «030» — нужен PAD в DSL
        Map<String, Object> values = new HashMap<>();
        values.put("bladeType", "BC/BI");
        values.put("wheelCode", "12U");
        values.put("frontDiskMod", "A");
        values.put("wheelWidth", 0.03);
        values.put("bladeCount", 9);
        values.put("bladeLengthCoeff", 1.03);

        assertThat(engine.evaluateToString(RADIAL_FORMULA, values))
                .isEqualTo("BC/BI.12U/A.0.03/9/1.03");
    }

    @Test
    @DisplayName("маркировка огнестойкости: 2 часа и 400 °C -> F-2/400")
    void fireResistanceMarking() {
        Map<String, Object> values = new HashMap<>();
        values.put("fireResistanceHours", 2);
        values.put("maxTemperature", 400);

        assertThat(engine.evaluateToString(FIRE_MARKING, values)).isEqualTo("F-2/400");
    }
}
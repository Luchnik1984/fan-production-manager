package com.fanproduction.api.controller;

import com.fanproduction.api.dto.request.preview.FormulaPreviewRequest;
import com.fanproduction.template.formula.FormulaEngine;
import com.fanproduction.template.formula.FormulaEngineImpl;
import com.fanproduction.template.formula.function.FormulaFunctions;
import com.fanproduction.template.service.impl.ComputedFieldsLibraryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты контроллера предпросмотра формул.
 * <p>
 * Используется реальный {@link FormulaEngineImpl} с инициализированными
 * {@link FormulaFunctions} и {@link ComputedFieldsLibraryImpl} — это
 * интеграционный тест логики, без Spring-контекста.
 */
@DisplayName("FormulaPreviewController")
class FormulaPreviewControllerTest {

    private FormulaPreviewController controller;

    @BeforeEach
    void setUp() {
        FormulaFunctions functions = new FormulaFunctions();
        functions.init();
        ComputedFieldsLibraryImpl computedFields = new ComputedFieldsLibraryImpl();
        computedFields.init();
        FormulaEngine engine = new FormulaEngineImpl(functions, computedFields);
        controller = new FormulaPreviewController(engine);
    }

    @Test
    @DisplayName("FLOOR(DIVIDE(wheelSize, 10)) → 28 для 285")
    void floorDivide() {
        var response = controller.previewFormula(new FormulaPreviewRequest(
                "FLOOR(DIVIDE(FIELD('wheelSize'), 10))",
                Map.of("wheelSize", 285)
        ));
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().result()).isEqualTo("28");
    }

    @Test
    @DisplayName("MAP(voltage) → D для 380")
    void mapVoltage() {
        var response = controller.previewFormula(new FormulaPreviewRequest(
                "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})",
                Map.of("voltage", 380)
        ));
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().result()).isEqualTo("D");
    }

    @Test
    @DisplayName("CONCAT('A', 'B') → AB")
    void concat() {
        var response = controller.previewFormula(new FormulaPreviewRequest(
                "CONCAT('A', 'B')", Map.of()
        ));
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().result()).isEqualTo("AB");
    }

    @Test
    @DisplayName("Ошибка синтаксиса → success=false, message")
    void syntaxError() {
        var response = controller.previewFormula(new FormulaPreviewRequest(
                "FLOOR(", Map.of()
        ));
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("Отсутствующее поле → null (не ошибка)")
    void missingField() {
        var response = controller.previewFormula(new FormulaPreviewRequest(
                "FIELD('unknown')", Map.of()
        ));
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().result()).isNull();
    }
}

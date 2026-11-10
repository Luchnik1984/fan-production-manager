package com.fanproduction.gui.component;

import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.FieldType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Универсальная панель предпросмотра (US9.9.e.2).
 * <p>
 * Состав:
 * <ul>
 *   <li>GridPane с полями шаблона для ввода тестовых значений.</li>
 *   <li>Кнопка «▶ Вычислить» / «▶ Проверить».</li>
 *   <li>Label для результата (или ошибки).</li>
 * </ul>
 * <p>
 * Используется в:
 * <ul>
 *   <li>{@code FieldEditDialog} — предпросмотр формулы COMPUTED
 *       (через {@code FormulaPreviewClient}).</li>
 *   <li>{@code ConditionEditDialog} — предпросмотр условия CONDITION
 *       (через {@code ConditionPreviewClient}).</li>
 * </ul>
 * <p>
 * Логика вызова — через {@link PreviewFunction} (лямбда).
 * Форматирование результата — через {@link ResultFormatter}.
 */
@Slf4j
public class PreviewPanel extends VBox {

    /**
     * Функция вычисления. Принимает карту тестовых значений.
     * Может бросать исключения — они показываются в result label красным.
     */
    @FunctionalInterface
    public interface PreviewFunction {
        Object preview(Map<String, Object> values) throws Exception;
    }

    /**
     * Форматирование результата в строку для отображения.
     */
    @FunctionalInterface
    public interface ResultFormatter {
        String format(Object result);
    }

    private final PreviewFunction previewFunction;
    private final ResultFormatter resultFormatter;

    private final GridPane valuesGrid = new GridPane();
    private final Button computeButton = new Button();
    private final Label resultLabel = new Label();

    public PreviewPanel(String title,
                        String buttonLabel,
                        List<FieldDefinitionDto> templateFields,
                        PreviewFunction previewFunction,
                        ResultFormatter resultFormatter) {

        this.previewFunction = previewFunction;
        this.resultFormatter = resultFormatter;

        setSpacing(8);
        setStyle("-fx-background-color: #F9F9F9; -fx-padding: 10; " +
                "-fx-border-color: #E0E0E0; -fx-border-radius: 3;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold;");

        valuesGrid.setHgap(10);
        valuesGrid.setVgap(6);
        valuesGrid.setPadding(new Insets(5, 0, 5, 0));

        buildValuesGrid(templateFields);

        ScrollPane valuesScroll = new ScrollPane(valuesGrid);
        valuesScroll.setFitToWidth(true);
        valuesScroll.setPrefHeight(140);
        valuesScroll.setStyle("-fx-background-color: transparent;");

        computeButton.setText(buttonLabel);
        computeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        computeButton.setOnAction(e -> onCompute());

        HBox actionBar = new HBox(10, computeButton);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        resultLabel.setWrapText(true);
        resultLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        resultLabel.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(titleLabel, valuesScroll, actionBar, resultLabel);
    }

    private void buildValuesGrid(List<FieldDefinitionDto> templateFields) {
        // Собираем поля шаблона (без системных вычисляемых)
        List<FieldDefinitionDto> fieldsForValues = templateFields == null
                ? List.of()
                : templateFields.stream()
                .filter(f -> f.getType() != FieldType.SEPARATOR
                        && f.getType() != FieldType.HIDDEN
                        && f.getType() != FieldType.COMPUTED)
                .toList();

        if (fieldsForValues.isEmpty()) {
            Label empty = new Label("Нет полей шаблона для тестовых значений");
            empty.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
            valuesGrid.add(empty, 0, 0);
            return;
        }

        int row = 0;
        for (FieldDefinitionDto field : fieldsForValues) {
            Label lbl = new Label(field.getLabel() + " (" + field.getKey() + "):");
            lbl.setStyle("-fx-font-size: 11px;");
            TextField tf = new TextField();
            tf.setPromptText("тестовое значение");
            tf.setPrefWidth(200);
            tf.setUserData(field.getKey());
            valuesGrid.add(lbl, 0, row);
            valuesGrid.add(tf, 1, row);
            row++;
        }
    }

    private void onCompute() {
        Map<String, Object> values = collectValues();
        computeButton.setDisable(true);
        resultLabel.setText("Вычисление…");
        resultLabel.setStyle("-fx-text-fill: #666; -fx-font-family: monospace;");

        new Thread(() -> {
            try {
                Object res = previewFunction.preview(values);
                String formatted = resultFormatter.format(res);
                Platform.runLater(() -> {
                    computeButton.setDisable(false);
                    resultLabel.setText(formatted);
                    resultLabel.setStyle("-fx-text-fill: #2E7D32; " +
                            "-fx-font-family: monospace; -fx-font-weight: bold;");
                });
            } catch (Exception ex) {
                log.warn("Preview error: {}", ex.getMessage());
                Platform.runLater(() -> {
                    computeButton.setDisable(false);
                    resultLabel.setText("Ошибка: " + ex.getMessage());
                    resultLabel.setStyle("-fx-text-fill: #E53935; -fx-font-family: monospace;");
                });
            }
        }, "PreviewPanel-compute").start();
    }

    private Map<String, Object> collectValues() {
        Map<String, Object> values = new HashMap<>();
        for (Node n : valuesGrid.getChildren()) {
            if (n instanceof TextField tf) {
                String key = (String) tf.getUserData();
                String text = tf.getText() != null ? tf.getText().trim() : "";
                if (key != null && !text.isEmpty()) {
                    values.put(key, text);
                }
            }
        }
        return values;
    }
}

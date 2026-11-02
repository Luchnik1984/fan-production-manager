package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.FanTemplateVersionClient;
import com.fanproduction.gui.dto.response.DynamicFormFieldDto;
import com.fanproduction.gui.dto.response.DynamicFormMetadataDto;
import com.fanproduction.gui.dto.response.FanTemplateVersionDto;
import com.fanproduction.gui.service.FieldMetadataService;
import com.fanproduction.gui.util.FieldValueExtractor;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер предпросмотра шаблона (US9.5).
 * <p>
 * Строит слева простую форму тестовых значений (без FieldControlFactory —
 * никаких запросов к API, добавлений компонентов и т.п.).
 * Справа — результат маркировки из {@code /api/fan-template-versions/{id}/preview}.
 * <p>
 * Авто-обновление — с debounce 500 мс при изменении любого поля.
 * Кнопка «Вычислить» — принудительно.
 */
@Slf4j
public class TemplatePreviewController {

    // ========== FXML ==========
    @FXML private Label titleLabel;
    @FXML private Label statusLabel;

    @FXML private GridPane fieldsGrid;
    @FXML private TextArea markingArea;
    @FXML private TextArea validationArea;
    @FXML private CheckBox autoUpdateCheck;
    @FXML private Button computeButton;
    @FXML private Button validateButton;
    @FXML private Button resetButton;
    @FXML private Button closeButton;

    // ========== Состояние ==========
    private FanTemplateVersionDto version;
    private DynamicFormMetadataDto metadata;

    /** Поле → контрол ввода (для сбора значений). */
    private final Map<String, Node> fieldControls = new LinkedHashMap<>();

    /** Debounce-таймер для авто-обновления. */
    private PauseTransition debounce;

    private final FieldMetadataService metadataService = new FieldMetadataService();

    // ==========================================================
    // INIT
    // ==========================================================

    public void init(FanTemplateVersionDto version) {
        this.version = version;
        titleLabel.setText("Предпросмотр — v" + version.getVersion()
                + " [" + version.getStatus() + "]");

        // Загрузка метаданных шаблона (название типа, серии — для заголовка)
        Long templateVersionId = version.getId();
        new Thread(() -> {
            try {
                DynamicFormMetadataDto meta = metadataService.getFanFormMetadata(templateVersionId);
                Platform.runLater(() -> {
                    this.metadata = meta;
                    buildForm();
                    if (autoUpdateCheck.isSelected()) {
                        triggerDebouncedCompute();
                    }
                });
            } catch (Exception e) {
                log.warn("Не удалось загрузить метаданные шаблона: {}", e.getMessage());
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка загрузки метаданных: " + e.getMessage());
                    markingArea.setText("(не удалось загрузить поля шаблона)");
                });
            }
        }, "TemplatePreview-loader").start();

        // Debounce
        debounce = new PauseTransition(Duration.millis(500));
        debounce.setOnFinished(e -> computeMarking());
    }

    @FXML
    private void initialize() {
        // Ничего не делаем — основная инициализация в init(...)
    }

    // ==========================================================
    // ПОСТРОЕНИЕ ФОРМЫ
    // ==========================================================

    private void buildForm() {
        fieldsGrid.getChildren().clear();
        fieldControls.clear();

        if (metadata == null || metadata.getFields() == null || metadata.getFields().isEmpty()) {
            Label empty = new Label("В шаблоне нет полей");
            empty.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
            fieldsGrid.add(empty, 0, 0, 2, 1);
            return;
        }

        int row = 0;
        for (DynamicFormFieldDto field : metadata.getFields()) {
            String type = field.getType();

            // HIDDEN — пропускаем
            if ("HIDDEN".equals(type)) continue;
            // SEPARATOR — разделитель
            if ("SEPARATOR".equals(type)) {
                Separator sep = new Separator();
                fieldsGrid.add(sep, 0, row++, 2, 1);
                Label title = new Label(field.getLabel() != null ? field.getLabel() : "");
                title.setStyle("-fx-font-weight: bold; -fx-text-fill: #555;");
                fieldsGrid.add(title, 0, row++, 2, 1);
                continue;
            }

            Label label = new Label((field.getLabel() != null ? field.getLabel() : field.getKey())
                    + (field.isRequired() ? " *" : ":"));
            label.setStyle("-fx-font-weight: bold;");

            Node control = createControl(field);
            fieldControls.put(field.getKey(), control);

            fieldsGrid.add(label, 0, row);
            fieldsGrid.add(control, 1, row);
            row++;
        }
    }

    private Node createControl(DynamicFormFieldDto field) {
        String type = field.getType();

        switch (type) {
            case "TEXT", "COMPUTED" -> {
                TextField tf = new TextField();
                tf.setPromptText(field.getHint());
                if ("COMPUTED".equals(type)) {
                    tf.setEditable(false);
                    tf.setStyle("-fx-background-color: #F0F0F0; -fx-text-fill: #555;");
                    tf.setPromptText("(вычисляется)");
                } else {
                    tf.textProperty().addListener((obs, o, n) -> onFieldChanged());
                }
                return tf;
            }
            case "NUMBER" -> {
                TextField tf = new TextField();
                tf.setPromptText(field.getHint() != null ? field.getHint() : "Число");
                tf.textProperty().addListener((obs, o, n) -> {
                    if (n != null && !n.matches("\\d*")) tf.setText(o);
                    else onFieldChanged();
                });
                return tf;
            }
            case "DOUBLE" -> {
                TextField tf = new TextField();
                tf.setPromptText(field.getHint() != null ? field.getHint() : "Число (5,5)");
                tf.textProperty().addListener((obs, o, n) -> {
                    if (n != null && !n.matches("\\d*([,.]\\d*)?")) tf.setText(o);
                    else onFieldChanged();
                });
                return tf;
            }
            case "COMBOBOX" -> {
                ComboBox<String> cb = new ComboBox<>();
                if (field.getOptions() != null) cb.getItems().addAll(field.getOptions());
                if (field.getDefaultValue() != null) cb.setValue(field.getDefaultValue());
                cb.setPromptText(field.getHint());
                cb.valueProperty().addListener((obs, o, n) -> onFieldChanged());
                return cb;
            }
            case "BOOLEAN" -> {
                CheckBox ch = new CheckBox();
                if (field.getDefaultValue() != null) {
                    ch.setSelected(Boolean.parseBoolean(field.getDefaultValue()));
                }
                ch.selectedProperty().addListener((obs, o, n) -> onFieldChanged());
                return ch;
            }
            case "SELECTABLE" -> {
                // Для предпросмотра — просто TextField. Пользователь вводит
                // тестовое значение (ID или displayName — не важно, главное, чтобы
                // сервер мог вернуть что-то осмысленное; в pullFrom-полях можно
                // отдельно ввести реальные значения).
                TextField tf = new TextField();
                tf.setPromptText((field.getHint() != null ? field.getHint() + " " : "")
                        + "(тестовое значение: ID или строка)");
                tf.textProperty().addListener((obs, o, n) -> onFieldChanged());
                return tf;
            }
            default -> {
                TextField tf = new TextField();
                tf.setPromptText(field.getHint());
                tf.textProperty().addListener((obs, o, n) -> onFieldChanged());
                return tf;
            }
        }
    }

    private void onFieldChanged() {
        if (autoUpdateCheck.isSelected()) {
            triggerDebouncedCompute();
        }
    }

    private void triggerDebouncedCompute() {
        if (debounce != null) {
            debounce.playFromStart();
        }
    }

    // ==========================================================
    // СБОР ЗНАЧЕНИЙ
    // ==========================================================

    private Map<String, Object> collectValues() {
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<String, Node> entry : fieldControls.entrySet()) {
            String key = entry.getKey();
            Node control = entry.getValue();
            Object value = FieldValueExtractor.extract(control);
            if (value != null) {
                values.put(key, value);
            }
        }
        return values;
    }

    // ==========================================================
    // ВЫЧИСЛЕНИЕ МАРКИРОВКИ
    // ==========================================================

    @FXML
    private void handleCompute() {
        computeMarking();
    }

    private void computeMarking() {
        if (version == null || version.getId() == null) return;

        statusLabel.setText("Вычисление…");
        computeButton.setDisable(true);

        Map<String, Object> values = collectValues();
        Long versionId = version.getId();

        new Thread(() -> {
            try {
                String marking = FanTemplateVersionClient.previewMarking(versionId, values);
                Platform.runLater(() -> {
                    computeButton.setDisable(false);
                    markingArea.setText(marking != null && !marking.isEmpty()
                            ? marking
                            : "(пустой результат)");
                    statusLabel.setText(marking != null && !marking.isEmpty()
                            ? "Готово" : "Пустой результат — проверьте значения полей");
                });
            } catch (Exception e) {
                log.warn("Ошибка preview: {}", e.getMessage());
                Platform.runLater(() -> {
                    computeButton.setDisable(false);
                    markingArea.setText("(ошибка)");
                    statusLabel.setText("Ошибка: " + e.getMessage());
                });
            }
        }, "TemplatePreview-compute").start();
    }

    // ==========================================================
    // ВАЛИДАЦИЯ
    // ==========================================================

    @FXML
    private void handleValidate() {
        if (version == null) return;
        validateButton.setDisable(true);
        statusLabel.setText("Проверка…");

        new Thread(() -> {
            try {
                List<String> errors = FanTemplateVersionClient.validateVersion(version.getId());
                Platform.runLater(() -> {
                    validateButton.setDisable(false);
                    statusLabel.setText("Готово");
                    if (errors == null || errors.isEmpty()) {
                        validationArea.setText("✔ Ошибок не найдено. Шаблон готов к публикации.");
                        validationArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11px; -fx-text-fill: #2E7D32;");
                    } else {
                        StringBuilder sb = new StringBuilder("Найдены ошибки:\n");
                        for (String err : errors) sb.append("• ").append(err).append("\n");
                        validationArea.setText(sb.toString());
                        validationArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11px; -fx-text-fill: #B71C1C;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    validateButton.setDisable(false);
                    statusLabel.setText("Ошибка валидации");
                    validationArea.setText("Не удалось выполнить валидацию: " + e.getMessage());
                    validationArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11px; -fx-text-fill: #B71C1C;");
                });
            }
        }, "TemplatePreview-validate").start();
    }

    // ==========================================================
    // ОЧИСТКА / ЗАКРЫТИЕ
    // ==========================================================

    @FXML
    private void handleReset() {
        for (Node control : fieldControls.values()) {
            if (control instanceof TextField tf) {
                if (tf.isEditable()) tf.clear();
            } else if (control instanceof ComboBox<?> cb) {
                cb.setValue(null);
            } else if (control instanceof CheckBox ch) {
                ch.setSelected(false);
            }
        }
        markingArea.clear();
        validationArea.clear();
        statusLabel.setText("Очищено");
    }

    @FXML
    private void handleClose() {
        Window w = owner();
        if (w instanceof javafx.stage.Stage s) {
            s.close();
        }
    }

    // ==========================================================
    // УТИЛИТЫ
    // ==========================================================

    private Window owner() {
        return TemplateControllerHelper.resolveOwner(fieldsGrid);
    }
}

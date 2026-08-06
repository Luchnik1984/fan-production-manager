package com.fanproduction.gui.component;

import com.fanproduction.core.dto.TechnicalSpec;
import com.fanproduction.gui.dto.response.UnitOfMeasureDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TechnicalSpecsEditor extends VBox {

    private final TableView<TechnicalSpec> tableView;
    private final ObservableList<TechnicalSpec> items;
    private final List<UnitOfMeasureDto> allUnits;
    private final Map<Long, UnitOfMeasureDto> unitsById;

    public TechnicalSpecsEditor(List<UnitOfMeasureDto> allUnits) {
        this.allUnits = allUnits;
        this.unitsById = allUnits.stream()
                .collect(Collectors.toMap(UnitOfMeasureDto::getId, u -> u));

        setSpacing(10);
        setPadding(new Insets(5));

        this.items = FXCollections.observableArrayList();

        tableView = new TableView<>();
        tableView.setPrefHeight(200);
        tableView.setEditable(true);
        tableView.setItems(items);

        // ========== КОЛОНКА "ХАРАКТЕРИСТИКА" ==========
        TableColumn<TechnicalSpec, String> nameCol = new TableColumn<>("Характеристика");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name()));
        nameCol.setCellFactory(column -> new EditableStringCell(
                (spec, newValue) -> {
                    int index = items.indexOf(spec);
                    if (index >= 0) {
                        TechnicalSpec updated = new TechnicalSpec(
                                newValue,
                                spec.value(),
                                spec.unitId(),
                                spec.unitCode()
                        );
                        items.set(index, updated);
                    }
                },
                () -> moveToNextCell(nameCol, 0)
        ));
        nameCol.setPrefWidth(200);

        // ========== КОЛОНКА "ЗНАЧЕНИЕ" ==========
        TableColumn<TechnicalSpec, String> valueCol = new TableColumn<>("Значение");
        valueCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().value()));
        valueCol.setCellFactory(column -> new EditableStringCell(
                (spec, newValue) -> {
                    int index = items.indexOf(spec);
                    if (index >= 0) {
                        TechnicalSpec updated = new TechnicalSpec(
                                spec.name(),
                                newValue,
                                spec.unitId(),
                                spec.unitCode()
                        );
                        items.set(index, updated);
                    }
                },
                () -> moveToNextCell(valueCol, 1)
        ));
        valueCol.setPrefWidth(150);

        // ========== КОЛОНКА "ЕДИНИЦА ИЗМЕРЕНИЯ" ==========
        TableColumn<TechnicalSpec, UnitOfMeasureDto> unitCol = new TableColumn<>("Ед. измерения");
        unitCol.setCellValueFactory(cellData -> {
            Long unitId = cellData.getValue().unitId();
            if (unitId != null && unitsById.containsKey(unitId)) {
                return new SimpleObjectProperty<>(unitsById.get(unitId));
            }
            return new SimpleObjectProperty<>(null);
        });
        // Передаём unitsById в конструктор ячейки
        unitCol.setCellFactory(column -> new EditableUnitCell(
                allUnits,
                unitsById,
                (spec, newUnit) -> {
                    int index = items.indexOf(spec);
                    if (index >= 0) {
                        TechnicalSpec updated = new TechnicalSpec(
                                spec.name(),
                                spec.value(),
                                newUnit != null ? newUnit.getId() : null,
                                newUnit != null ? newUnit.getCode() : null
                        );
                        items.set(index, updated);
                    }
                },
                () -> {
                    // После выбора единицы измерения переходим на следующую строку
                    int currentRow = tableView.getSelectionModel().getSelectedIndex();
                    if (currentRow >= 0 && currentRow < items.size() - 1) {
                        int nextRow = currentRow + 1;
                        tableView.getSelectionModel().select(nextRow);
                        Platform.runLater(() -> tableView.edit(nextRow, tableView.getColumns().get(0)));
                    } else if (currentRow >= 0 && currentRow == items.size() - 1) {
                        addEmptyRow();
                    }
                }
        ));
        unitCol.setPrefWidth(150);

        // ========== КОЛОНКА С КНОПКОЙ УДАЛЕНИЯ ==========
        TableColumn<TechnicalSpec, Void> deleteCol = new TableColumn<>("");
        deleteCol.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("✖");

            {
                deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
                deleteButton.setOnAction(e -> {
                    TechnicalSpec item = getTableRow().getItem();
                    if (item != null) {
                        items.remove(item);
                        tableView.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        deleteCol.setPrefWidth(40);
        deleteCol.setEditable(false);

        tableView.getColumns().add(nameCol);
        tableView.getColumns().add(valueCol);
        tableView.getColumns().add(unitCol);
        tableView.getColumns().add(deleteCol);

        // ========== КНОПКА ДОБАВЛЕНИЯ ==========
        Button addButton = new Button("➕ Добавить характеристику");
        addButton.setOnAction(e -> addEmptyRow());

        getChildren().addAll(tableView, addButton);
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================================

    private void moveToNextCell(TableColumn<TechnicalSpec, ?> currentColumn, int currentColIndex) {
        int currentRow = tableView.getSelectionModel().getSelectedIndex();
        if (currentRow < 0) return;

        int nextCol = currentColIndex + 1;
        if (nextCol >= tableView.getColumns().size() - 1) {
            int nextRow = currentRow + 1;
            if (nextRow < items.size()) {
                tableView.getSelectionModel().select(nextRow);
                Platform.runLater(() -> tableView.edit(nextRow, tableView.getColumns().get(0)));
            } else {
                addEmptyRow();
            }
        } else {
            tableView.edit(currentRow, tableView.getColumns().get(nextCol));
        }
    }

    private void addEmptyRow() {
        if (!items.isEmpty()) {
            TechnicalSpec last = items.get(items.size() - 1);
            if ((last.name() == null || last.name().trim().isEmpty()) &&
                    (last.value() == null || last.value().trim().isEmpty())) {
                int lastIndex = items.size() - 1;
                tableView.scrollTo(lastIndex);
                tableView.getSelectionModel().select(lastIndex);
                Platform.runLater(() -> tableView.edit(lastIndex, tableView.getColumns().get(0)));
                return;
            }
        }

        items.add(new TechnicalSpec("", "", null, null));
        int newRowIndex = items.size() - 1;
        tableView.scrollTo(newRowIndex);

        Platform.runLater(() -> {
            tableView.getSelectionModel().select(newRowIndex);
            tableView.requestFocus();
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                    javafx.util.Duration.millis(50)
            );
            pause.setOnFinished(e -> tableView.edit(newRowIndex, tableView.getColumns().get(0)));
            pause.play();
        });
    }

    // ==========================================================
    // ВНУТРЕННИЙ КЛАСС: РЕДАКТИРУЕМАЯ ЯЧЕЙКА (ТЕКСТ)
    // ==========================================================

    private static class EditableStringCell extends TableCell<TechnicalSpec, String> {
        private final TextField textField;
        private final OnStringCommit onCommit;
        private final Runnable onEnter;

        public EditableStringCell(OnStringCommit onCommit, Runnable onEnter) {
            this.onCommit = onCommit;
            this.onEnter = onEnter;
            this.textField = new TextField();

            textField.setOnAction(e -> {
                commitEdit();
                if (onEnter != null) {
                    onEnter.run();
                }
            });

            textField.focusedProperty().addListener((obs, old, newVal) -> {
                if (!newVal) {
                    commitEdit();
                }
            });

            textField.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
        }

        private void commitEdit() {
            String newValue = textField.getText();
            TechnicalSpec spec = getTableRow() != null ? getTableRow().getItem() : null;
            if (spec != null && onCommit != null) {
                onCommit.commit(spec, newValue);
            }
            cancelEdit();
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
                textField.setText(item);
                setGraphic(textField);
                setText(null);
            } else {
                setText(item);
                setGraphic(null);
            }
        }

        @Override
        public void startEdit() {
            if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                return;
            }
            super.startEdit();
            String currentValue = getItem() != null ? getItem() : "";
            textField.setText(currentValue);
            setGraphic(textField);
            setText(null);
            textField.selectAll();
            textField.requestFocus();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem() != null ? getItem() : "");
            setGraphic(null);
        }

        @FunctionalInterface
        public interface OnStringCommit {
            void commit(TechnicalSpec spec, String newValue);
        }
    }

    // ==========================================================
    // ВНУТРЕННИЙ КЛАСС: РЕДАКТИРУЕМАЯ ЯЧЕЙКА (ЕДИНИЦА ИЗМЕРЕНИЯ)
    // ==========================================================

    private static class EditableUnitCell extends TableCell<TechnicalSpec, UnitOfMeasureDto> {
        private final ComboBox<UnitOfMeasureDto> comboBox;
        private final OnUnitCommit onCommit;
        private final Runnable onEnter;
        private final Map<Long, UnitOfMeasureDto> unitsById;

        public EditableUnitCell(List<UnitOfMeasureDto> allUnits,
                                Map<Long, UnitOfMeasureDto> unitsById,
                                OnUnitCommit onCommit,
                                Runnable onEnter) {
            this.onCommit = onCommit;
            this.onEnter = onEnter;
            this.unitsById = unitsById;
            this.comboBox = new ComboBox<>();
            comboBox.setItems(FXCollections.observableArrayList(allUnits));
            comboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(UnitOfMeasureDto unit) {
                    return unit == null ? "" : unit.getCode();
                }

                @Override
                public UnitOfMeasureDto fromString(String string) {
                    return comboBox.getItems().stream()
                            .filter(u -> u.getCode().equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });

            // Обработка выбора в ComboBox
            comboBox.valueProperty().addListener((obs, old, newVal) -> {
                if (newVal != null) {
                    TechnicalSpec spec = getTableRow() != null ? getTableRow().getItem() : null;
                    if (spec != null && onCommit != null) {
                        onCommit.commit(spec, newVal);
                        cancelEdit();
                        if (onEnter != null) {
                            onEnter.run();
                        }
                    }
                }
            });

            comboBox.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });

            // Когда ячейка становится невидимой — завершаем редактирование
            visibleProperty().addListener((obs, old, newVal) -> {
                if (!newVal && isEditing()) {
                    cancelEdit();
                }
            });
        }

        @Override
        protected void updateItem(UnitOfMeasureDto item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
                setText(null);
            } else {
                // ВСЕГДА показываем текст, график только в режиме редактирования
                setText(item != null ? item.getCode() : "");
                if (isEditing()) {
                    setGraphic(comboBox);
                } else {
                    setGraphic(null);
                }
            }
        }

        @Override
        public void startEdit() {
            if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                return;
            }
            super.startEdit();

            TechnicalSpec spec = getTableRow() != null ? getTableRow().getItem() : null;
            if (spec != null && spec.unitId() != null && unitsById.containsKey(spec.unitId())) {
                comboBox.setValue(unitsById.get(spec.unitId()));
            } else {
                comboBox.setValue(null);
            }

            setGraphic(comboBox);
            setText(null);
            comboBox.requestFocus();
            comboBox.show();  // ← ПРИНУДИТЕЛЬНО ПОКАЗЫВАЕМ ВЫПАДАЮЩИЙ СПИСОК
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem() != null ? getItem().getCode() : "");
            setGraphic(null);
        }

        @FunctionalInterface
        public interface OnUnitCommit {
            void commit(TechnicalSpec spec, UnitOfMeasureDto newUnit);
        }
    }

    // ==========================================================
    // ПУБЛИЧНЫЕ МЕТОДЫ
    // ==========================================================

    public void setTechnicalSpecs(Map<String, Object> specs) {
        items.clear();
        if (specs == null) return;

        for (Map.Entry<String, Object> entry : specs.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            String valueStr;
            Long unitId = null;
            String unitCode = null;

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> valueMap = (Map<String, Object>) value;
                valueStr = valueMap.get("value") != null ? valueMap.get("value").toString() : "";
                Object unitIdObj = valueMap.get("unitId");
                if (unitIdObj instanceof Number) {
                    unitId = ((Number) unitIdObj).longValue();
                    if (unitsById.containsKey(unitId)) {
                        unitCode = unitsById.get(unitId).getCode();
                    }
                }
            } else {
                valueStr = value != null ? value.toString() : "";
            }
            items.add(new TechnicalSpec(name, valueStr, unitId, unitCode));
        }
    }

    public Map<String, Object> getTechnicalSpecs() {
        return items.stream()
                .filter(spec -> {
                    String name = spec.name() != null ? spec.name().trim() : "";
                    String value = spec.value() != null ? spec.value().trim() : "";
                    return !name.isEmpty() && !value.isEmpty();
                })
                .collect(HashMap::new, (map, spec) -> {
                    String name = spec.name().trim();
                    Map<String, Object> valueMap = new HashMap<>();
                    valueMap.put("value", spec.value());
                    if (spec.unitId() != null) {
                        valueMap.put("unitId", spec.unitId());
                        if (spec.unitCode() != null) {
                            valueMap.put("unitCode", spec.unitCode());
                        }
                    }
                    map.put(name, valueMap);
                }, HashMap::putAll);
    }

    public void clear() {
        items.clear();
    }
}
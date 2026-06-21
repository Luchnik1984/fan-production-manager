package com.fanproduction.gui.component;

import com.fanproduction.core.dto.TechnicalSpec;
import com.fanproduction.gui.dto.response.UnitOfMeasureDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
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

        // Колонка "Характеристика"
        TableColumn<TechnicalSpec, String> nameCol = new TableColumn<>("Характеристика");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name()));
        nameCol.setCellFactory(column -> new EditableStringCellWithFocus(items, (spec, newValue) -> {
            int index = items.indexOf(spec);
            if (index >= 0) {
                TechnicalSpec updated = new TechnicalSpec(newValue, spec.value(), spec.unitId(), spec.unitCode());
                items.set(index, updated);
            }
        }));
        nameCol.setPrefWidth(200);

        // Колонка "Значение"
        TableColumn<TechnicalSpec, String> valueCol = new TableColumn<>("Значение");
        valueCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().value()));
        valueCol.setCellFactory(column -> new EditableStringCellWithFocus(items, (spec, newValue) -> {
            int index = items.indexOf(spec);
            if (index >= 0) {
                TechnicalSpec updated = new TechnicalSpec(spec.name(), newValue, spec.unitId(), spec.unitCode());
                items.set(index, updated);
            }
        }));
        valueCol.setPrefWidth(150);

        // Колонка "Единица измерения" (вынесена в отдельный метод)
        TableColumn<TechnicalSpec, UnitOfMeasureDto> unitCol = createUnitColumn();

        // Колонка с кнопкой удаления
        TableColumn<TechnicalSpec, Void> deleteCol = new TableColumn<>("");
        deleteCol.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("✖");

            {
                deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
                deleteButton.setOnAction(e -> {
                    TechnicalSpec item = getTableRow().getItem();
                    if (item != null) {
                        items.remove(item);
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

        tableView.getColumns().add(nameCol);
        tableView.getColumns().add(valueCol);
        tableView.getColumns().add(unitCol);
        tableView.getColumns().add(deleteCol);

        Button addButton = new Button("➕ Добавить характеристику");
        addButton.setOnAction(e -> addEmptyRow());

        getChildren().addAll(tableView, addButton);
    }

    // ==========================================================
    // ВЫНЕСЕННЫЙ МЕТОД ДЛЯ КОЛОНКИ "ЕДИНИЦА ИЗМЕРЕНИЯ"
    // ==========================================================

    private TableColumn<TechnicalSpec, UnitOfMeasureDto> createUnitColumn() {
        TableColumn<TechnicalSpec, UnitOfMeasureDto> unitCol = new TableColumn<>("Единица измерения");

        unitCol.setCellValueFactory(cellData -> {
            Long unitId = cellData.getValue().unitId();
            if (unitId != null && unitsById.containsKey(unitId)) {
                return new SimpleObjectProperty<>(unitsById.get(unitId));
            }
            return new SimpleObjectProperty<>(null);
        });

        unitCol.setCellFactory(column -> new UnitComboBoxCell());
        unitCol.setOnEditCommit(event -> {
            TechnicalSpec spec = event.getRowValue();
            UnitOfMeasureDto newUnit = event.getNewValue();
            int index = items.indexOf(spec);
            if (index >= 0 && newUnit != null) {
                TechnicalSpec updated = new TechnicalSpec(spec.name(), spec.value(), newUnit.getId(), newUnit.getCode());
                items.set(index, updated);
            }
        });
        unitCol.setPrefWidth(150);

        return unitCol;
    }

    // ==========================================================
    // ВНУТРЕННИЙ КЛАСС ДЛЯ ЯЧЕЙКИ С КОМБОБОКСОМ
    // ==========================================================

    private class UnitComboBoxCell extends TableCell<TechnicalSpec, UnitOfMeasureDto> {
        private final ComboBox<UnitOfMeasureDto> comboBox = new ComboBox<>();

        public UnitComboBoxCell() {
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
            // Сохраняем значение СРАЗУ при выборе в ComboBox
            comboBox.valueProperty().addListener((obs, old, newVal) -> {
                if (newVal != null) {
                    TechnicalSpec spec = getTableRow().getItem();
                    if (spec != null) {
                        // Обновляем TechnicalSpec в списке items
                        TechnicalSpec updated = new TechnicalSpec(
                                spec.name(),
                                spec.value(),
                                newVal.getId(),
                                newVal.getCode()
                        );
                        int index = items.indexOf(spec);
                        if (index >= 0) {
                            items.set(index, updated);
                        }
                    }
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
                updateComboBoxValue();
            }
        }

        private void updateComboBoxValue() {
            TechnicalSpec spec = getTableRow().getItem();
            if (spec != null && spec.unitId() != null && unitsById.containsKey(spec.unitId())) {
                comboBox.setValue(unitsById.get(spec.unitId()));
            } else {
                comboBox.setValue(null);
            }
            setGraphic(comboBox);
            setText(null);
        }

        @Override
        public void startEdit() {
            if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
                return;
            }
            super.startEdit();
            updateComboBoxValue();
            comboBox.requestFocus();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setGraphic(null);
            setText(getItem() == null ? "" : getItem().getCode());
        }
    }

    // ==========================================================
    // ОСТАЛЬНЫЕ МЕТОДЫ (БЕЗ ИЗМЕНЕНИЙ)
    // ==========================================================

    private void addEmptyRow() {
        // Проверяем, есть ли уже пустая строка в конце
        if (!items.isEmpty()) {
            TechnicalSpec last = items.get(items.size() - 1);
            if ((last.name() == null || last.name().trim().isEmpty()) &&
                    (last.value() == null || last.value().trim().isEmpty())) {
                // Уже есть пустая строка — не добавляем новую
                tableView.scrollTo(items.size() - 1);
                tableView.edit(items.size() - 1, tableView.getColumns().get(0));
                return;
            }
        }
        items.add(new TechnicalSpec("", "", null, null));
        tableView.scrollTo(items.size() - 1);
        tableView.edit(items.size() - 1, tableView.getColumns().get(0));
    }

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

    // ==========================================================
    // ВНУТРЕННИЙ КЛАСС ДЛЯ РЕДАКТИРУЕМЫХ ЯЧЕЕК
    // ==========================================================

    private static class EditableStringCellWithFocus extends TableCell<TechnicalSpec, String> {
        private final TextField textField;
        private final OnEditCommit onEditCommit;
        private final ObservableList<TechnicalSpec> items;

        public EditableStringCellWithFocus(ObservableList<TechnicalSpec> items, OnEditCommit onEditCommit) {
            this.items = items;
            this.onEditCommit = onEditCommit;
            this.textField = new TextField();

            textField.setOnAction(e -> commitEdit());

            textField.focusedProperty().addListener((obs, old, newVal) -> {
                if (!newVal) {
                    commitEdit();
                }
            });

            textField.setOnKeyPressed(e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                    cancelEdit();
                }
            });
        }

        private void commitEdit() {
            String newValue = textField.getText();
            TechnicalSpec spec = getTableRow().getItem();
            if (spec != null && onEditCommit != null) {
                onEditCommit.commit(spec, newValue);
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
        public interface OnEditCommit {
            void commit(TechnicalSpec spec, String newValue);
        }
    }

    // ==========================================================
    // SimpleObjectProperty для unitCol
    // ==========================================================

    private static class SimpleObjectProperty<T> extends javafx.beans.property.SimpleObjectProperty<T> {
        public SimpleObjectProperty(T initialValue) {
            super(initialValue);
        }
    }
}
package com.fanproduction.gui.factory;

import com.fanproduction.gui.client.ComponentClient;
import com.fanproduction.gui.component.TreeSelectableComponentBox;
import com.fanproduction.gui.dto.SelectableItem;
import com.fanproduction.gui.dto.metadata.FieldMetadataDto;
import com.fanproduction.gui.dto.response.ComponentDto;
import com.fanproduction.gui.dto.response.ProductCardDto;
import com.fanproduction.gui.client.ProductCardClient;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Фабрика для создания контролов (полей ввода) на основе метаданных.
 */
public class FieldControlFactory {

    private final Stage ownerStage;
    private final BiConsumer<String, Long> autoFillCallback;  // (referenceType, selectedId) -> авто-заполнение

    public FieldControlFactory(Stage ownerStage, BiConsumer<String, Long> autoFillCallback) {
        this.ownerStage = ownerStage;
        this.autoFillCallback = autoFillCallback;
    }

    /**
     * Создаёт контрол на основе метаданных поля
     *
     * @param field         метаданные поля
     * @param existingValue существующее значение (при редактировании)
     * @param fieldControls карта контролов (нужна для selectable типов)
     * @return созданный Node
     */
    public Node createControl(FieldMetadataDto field, Object existingValue, Map<String, Node> fieldControls) {
        return switch (field.getType()) {
            case "text" -> createTextField(field, existingValue);
            case "number" -> createNumberField(field, existingValue);
            case "double" -> createDoubleField(field, existingValue);
            case "combobox" -> createComboBox(field, existingValue);
            case "boolean" -> createCheckBox(field, existingValue);
            case "selectable" -> createSelectableComboBox(field, existingValue, fieldControls);
            case "hidden" -> createHiddenField(field, existingValue);
            default -> createDefaultField(field, existingValue);
        };
    }

    // ========== МЕТОДЫ СОЗДАНИЯ ==========

    private Node createTextField(FieldMetadataDto field, Object existingValue) {
        TextField textField = new TextField();
        if (existingValue != null) textField.setText(String.valueOf(existingValue));
        if (field.getDefaultValue() != null && existingValue == null) textField.setText(field.getDefaultValue());
        textField.setPromptText(field.getHint());
        return textField;
    }

    private Node createNumberField(FieldMetadataDto field, Object existingValue) {
        TextField numberField = new TextField();
        if (existingValue != null) numberField.setText(String.valueOf(existingValue));
        if (field.getDefaultValue() != null && existingValue == null) numberField.setText(field.getDefaultValue());
        numberField.setPromptText("Введите число");

        numberField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                numberField.setText(old);
            }
        });

        return numberField;
    }

    private Node createDoubleField(FieldMetadataDto field, Object existingValue) {
        TextField doubleField = new TextField();
        if (existingValue != null) doubleField.setText(String.valueOf(existingValue));
        if (field.getDefaultValue() != null && existingValue == null) doubleField.setText(field.getDefaultValue());

        // Используем hint из метаданных, если он есть
        String hint = field.getHint() != null ? field.getHint() : "Введите число (например: 5,5)";
        doubleField.setPromptText(hint);

        doubleField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*([,.]\\d*)?")) {
                doubleField.setText(old);
            }
        });

        return doubleField;
    }

    private Node createComboBox(FieldMetadataDto field, Object existingValue) {
        String refType = field.getReferenceType();
        boolean isReference = refType != null && !refType.isEmpty();

        if (isReference) {
            ComboBox<SelectableItem> refComboBox = new ComboBox<>();
            refComboBox.setPromptText(field.getHint() != null ? field.getHint() : "Выберите");

            Long existingId = null;
            if (existingValue instanceof Number) {
                existingId = ((Number) existingValue).longValue();
            } else if (existingValue instanceof String) {
                try {
                    existingId = Long.parseLong((String) existingValue);
                } catch (NumberFormatException e) {
                    // Игнорируем
                }
            }

            loadReferenceData(refComboBox, refType, existingId);

            refComboBox.valueProperty().addListener((obs, old, newVal) -> {
                if (newVal != null && newVal.getId() != null) {
                    if (autoFillCallback != null) {
                        autoFillCallback.accept(refType, newVal.getId());
                    }
                }
            });

            return refComboBox;
        } else {
            ComboBox<String> comboBox = new ComboBox<>();
            if (field.getOptions() != null) {
                comboBox.getItems().addAll(field.getOptions());
            }
            if (existingValue != null) {
                comboBox.setValue(String.valueOf(existingValue));
            }
            if (field.getDefaultValue() != null && existingValue == null) {
                comboBox.setValue(field.getDefaultValue());
            }
            if (field.getHint() != null && !field.getHint().isEmpty()) {
                comboBox.setPromptText(field.getHint());
            }
            return comboBox;
        }
    }

    private Node createCheckBox(FieldMetadataDto field, Object existingValue) {
        CheckBox checkBox = new CheckBox();
        if (existingValue instanceof Boolean) checkBox.setSelected((Boolean) existingValue);
        if (field.getDefaultValue() != null && existingValue == null) {
            checkBox.setSelected(Boolean.parseBoolean(field.getDefaultValue()));
        }
        return checkBox;
    }

    private Node createSelectableComboBox(FieldMetadataDto field, Object existingValue, Map<String, Node> fieldControls) {
        String refType = field.getReferenceType();

        TreeSelectableComponentBox treeBox = new TreeSelectableComponentBox(
                ownerStage, refType,
                id -> {
                    if (autoFillCallback != null) {
                        autoFillCallback.accept(refType, id);
                    }
                }
        );

        if (existingValue instanceof Number) {
            treeBox.setSelectedId(((Number) existingValue).longValue());
        }

        return treeBox.getContainer();
    }

    private Node createHiddenField(FieldMetadataDto field, Object existingValue) {
        TextField hiddenField = new TextField();
        hiddenField.setVisible(false);
        hiddenField.setManaged(false);
        if (existingValue != null) {
            hiddenField.setText(String.valueOf(existingValue));
        }
        return hiddenField;
    }

    private Node createDefaultField(FieldMetadataDto field, Object existingValue) {
        TextField defaultField = new TextField();
        if (existingValue != null) defaultField.setText(String.valueOf(existingValue));
        return defaultField;
    }

    /**
     * Загружает справочные данные для ComboBox
     */
    private void loadReferenceData(ComboBox<SelectableItem> comboBox, String referenceType, Long existingId) {
        comboBox.getItems().clear();
        comboBox.getItems().add(new SelectableItem(null, "Загрузка..."));

        new Thread(() -> {
            try {
                List<ProductCardDto> items = switch (referenceType) {
                    case "MOTOR_WHEEL" -> ProductCardClient.getCardsByType("MOTOR_WHEEL");
                    case "RADIAL_WHEEL" -> ProductCardClient.getCardsByType("RADIAL_WHEEL");
                    case "MOTOR" -> ProductCardClient.getCardsByType("MOTOR");
                    case "AXIAL_WHEEL" -> ProductCardClient.getCardsByType("AXIAL_WHEEL");
                    case "COMPONENT" -> convertComponentsToProductCards(ComponentClient.getAllComponents());
                    default -> new ArrayList<>();
                };

                // Подготовка данных вне UI потока
                List<SelectableItem> comboItems = new ArrayList<>();
                SelectableItem preselectedItem = null;

                for (ProductCardDto dto : items) {
                    String fullMarking = (String) dto.getFields().get("fullMarking");
                    String displayName;
                    if (fullMarking != null && !fullMarking.isEmpty()) {
                        displayName = fullMarking;
                    } else {
                        displayName = dto.getName() + " (" + dto.getCode() + ")";
                    }
                    SelectableItem item = new SelectableItem(dto.getId(), displayName);
                    comboItems.add(item);

                    if (existingId != null && existingId.equals(dto.getId())) {
                        preselectedItem = item;
                    }
                }

                final List<SelectableItem> finalItems = comboItems;
                final SelectableItem finalPreselected = preselectedItem;

                // Обновление UI
                javafx.application.Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    if (!finalItems.isEmpty()) {
                        comboBox.getItems().addAll(finalItems);
                        if (finalPreselected != null) {
                            comboBox.setValue(finalPreselected);
                        }
                    } else {
                        comboBox.getItems().add(new SelectableItem(null, "Нет данных"));
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    comboBox.getItems().add(new SelectableItem(null, "Ошибка загрузки"));
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Конвертирует ComponentDto в ProductCardDto для совместимости с SelectableItem
     */
    private List<ProductCardDto> convertComponentsToProductCards(List<ComponentDto> components) {
        List<ProductCardDto> result = new ArrayList<>();
        for (ComponentDto comp : components) {
            ProductCardDto dto = new ProductCardDto();
            dto.setId(comp.getId());
            dto.setName(comp.getName());
            dto.setCode(comp.getVendorCode());

            Map<String, Object> fields = new HashMap<>();
            fields.put("fullMarking", comp.getName());
            dto.setFields(fields);

            result.add(dto);
        }
        return result;
    }
}

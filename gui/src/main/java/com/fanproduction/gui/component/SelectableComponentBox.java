package com.fanproduction.gui.component;

import com.fanproduction.gui.client.ProductCardClient;
import com.fanproduction.gui.controller.CardFormController;
import com.fanproduction.gui.dto.SelectableItem;
import com.fanproduction.gui.dto.response.ProductCardDto;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Consumer;

/**
 * Компонент для выбора компонента из базы данных с кнопкой "Создать".
 * Пример использования:
 * <pre>
 * SelectableComponentBox motorWheelSelector = new SelectableComponentBox(
 *     ownerStage,
 *     "MOTOR_WHEEL",
 *     this::updateFullMarking,
 *     this::loadMotorWheelData
 * );
 * fieldControls.put("motorWheelId", motorWheelSelector.getContainer());
 * motorWheelSelector.loadData();
 * </pre>
 */
public class SelectableComponentBox {

    private final ComboBox<SelectableItem> comboBox;
    private final Button createButton;
    /**
     * -- GETTER --
     *  Получает контейнер для добавления в форму
     */
    @Getter
    private final HBox container;
    private final String referenceType;
    private final Stage ownerStage;
    private final Runnable onSelectionChanged;
    /**
     * -- SETTER --
     *  Устанавливает колбэк для автозаполнения полей при выборе.
     */
    @Setter
    private Consumer<Long> onAutoFill;
    private Long currentSelectedId;

    /**
     * Конструктор
     * @param ownerStage родительское окно
     * @param referenceType тип компонента (MOTOR_WHEEL, RADIAL_WHEEL, MOTOR, AXIAL_WHEEL)
     * @param onSelectionChanged колбэк при изменении выбора
     */
    public SelectableComponentBox(Stage ownerStage, String referenceType, Runnable onSelectionChanged) {
        this.ownerStage = ownerStage;
        this.referenceType = referenceType;
        this.onSelectionChanged = onSelectionChanged;

        this.comboBox = new ComboBox<>();
        this.comboBox.setPromptText(getPromptText(referenceType));
        this.comboBox.setPrefWidth(280);
        this.comboBox.setMinWidth(200);

        this.createButton = new Button("➕ Создать");
        this.createButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        this.createButton.setOnAction(e -> openCreateDialog());

        this.container = new HBox(8, comboBox, createButton);
        this.container.setPadding(new Insets(2, 0, 2, 0));

        // Слушатель выбора
        this.comboBox.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.getId() != null) {
                currentSelectedId = newVal.getId();
                if (onAutoFill != null) {
                    onAutoFill.accept(newVal.getId());
                }
                if (onSelectionChanged != null) {
                    onSelectionChanged.run();
                }
            }
        });
    }

    /**
     * Конструктор с автозаполнением
     */
    public SelectableComponentBox(Stage ownerStage, String referenceType,
                                  Runnable onSelectionChanged, Consumer<Long> onAutoFill) {
        this(ownerStage, referenceType, onSelectionChanged);
        this.onAutoFill = onAutoFill;
    }

    private String getPromptText(String referenceType) {
        return switch (referenceType) {
            case "MOTOR_WHEEL" -> "Выберите мотор-колесо";
            case "RADIAL_WHEEL" -> "Выберите радиальное колесо";
            case "MOTOR" -> "Выберите электродвигатель";
            case "AXIAL_WHEEL" -> "Выберите осевое колесо";
            default -> "Выберите компонент";
        };
    }

    private String getCardTypeByReference() {
        return switch (referenceType) {
            case "MOTOR_WHEEL" -> "MOTOR_WHEEL";
            case "RADIAL_WHEEL" -> "RADIAL_WHEEL";
            case "MOTOR" -> "MOTOR";
            case "AXIAL_WHEEL" -> "AXIAL_WHEEL";
            default -> null;
        };
    }

    private String getDisplayName(ProductCardDto dto) {
        // Пытаемся получить полную маркировку
        if (dto.getFields() != null) {
            String fullMarking = (String) dto.getFields().get("fullMarking");
            if (fullMarking != null && !fullMarking.isEmpty()) {
                return fullMarking;
            }
        }
        return dto.getName() + " (" + dto.getCode() + ")";
    }

    private void openCreateDialog() {
        String cardType = getCardTypeByReference();
        if (cardType == null) return;

        // После создания обновляем список
        CardFormController form = new CardFormController(
                ownerStage,
                cardType,
                null,
                this::refreshData
        );
        form.show();
    }

    /**
     * Загружает данные из базы данных
     */
    public void loadData() {
        comboBox.getItems().clear();
        comboBox.getItems().add(new SelectableItem(null, "Загрузка..."));

        new Thread(() -> {
            try {
                List<ProductCardDto> items = ProductCardClient.getCardsByType(referenceType);
                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    if (items != null && !items.isEmpty()) {
                        for (ProductCardDto dto : items) {
                            String displayName = getDisplayName(dto);
                            comboBox.getItems().add(new SelectableItem(dto.getId(), displayName));
                        }
                        // Восстанавливаем выбранное значение, если было
                        if (currentSelectedId != null) {
                            setSelectedId(currentSelectedId);
                        }
                    } else {
                        comboBox.getItems().add(new SelectableItem(null, "Нет данных"));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    comboBox.getItems().add(new SelectableItem(null, "Ошибка загрузки"));
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Обновляет данные (после создания нового компонента)
     */
    public void refreshData() {
        loadData();
    }

    /**
     * Устанавливает выбранное значение по ID
     */
    public void setSelectedId(Long id) {
        for (SelectableItem item : comboBox.getItems()) {
            if (item.getId() != null && item.getId().equals(id)) {
                comboBox.setValue(item);
                currentSelectedId = id;
                break;
            }
        }
    }

    /**
     * Устанавливает выбранное значение по полной маркировке
     */
    public void setSelectedByFullMarking(String fullMarking) {
        if (fullMarking == null || fullMarking.isEmpty()) return;

        for (SelectableItem item : comboBox.getItems()) {
            if (item.toString().equals(fullMarking)) {
                comboBox.setValue(item);
                currentSelectedId = item.getId();
                break;
            }
        }
    }

    /**
     * Получает выбранный ID
     */
    public Long getSelectedId() {
        SelectableItem selected = comboBox.getValue();
        return selected != null ? selected.getId() : null;
    }

    /**
     * Получает выбранную полную маркировку
     */
    public String getSelectedFullMarking() {
        SelectableItem selected = comboBox.getValue();
        return selected != null ? selected.toString() : null;
    }

    /**
     * Проверяет, есть ли выбранное значение
     */
    public boolean hasSelection() {
        return getSelectedId() != null;
    }

    /**
     * Очищает выбранное значение
     */
    public void clear() {
        comboBox.setValue(null);
        currentSelectedId = null;
    }

    /**
     * Устанавливает видимость компонента
     */
    public void setVisible(boolean visible) {
        container.setVisible(visible);
        container.setManaged(visible);
    }
}

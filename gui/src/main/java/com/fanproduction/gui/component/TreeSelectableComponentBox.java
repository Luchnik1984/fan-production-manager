package com.fanproduction.gui.component;

import com.fanproduction.gui.builder.*;
import com.fanproduction.gui.client.*;
import com.fanproduction.gui.controller.CardFormController;
import com.fanproduction.gui.controller.ComponentsCatalogController;
import com.fanproduction.gui.dto.response.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class TreeSelectableComponentBox {

    @Getter
    private final HBox container;
    private final Button selectButton;
    private final Label selectedLabel;
    private final String referenceType;
    private final Stage ownerStage;
    private final Consumer<Long> onSelect;
    @Getter
    private Long selectedId;

    public TreeSelectableComponentBox(Stage ownerStage, String referenceType, Consumer<Long> onSelect) {
        this.ownerStage = ownerStage;
        this.referenceType = referenceType;
        this.onSelect = onSelect;

        this.selectButton = new Button("Выбрать");
        this.selectButton.setOnAction(e -> openSelector());
        this.selectedLabel = new Label("");
        this.selectedLabel.setVisible(false);
        this.selectedLabel.setManaged(false);
        this.selectedLabel.setStyle("-fx-text-fill: #0066cc;");

        this.container = new HBox(10, selectButton, selectedLabel);
        this.container.setPadding(new Insets(2, 0, 2, 0));
    }

    public void setSelectedId(Long id) {
        this.selectedId = id;
        selectedLabel.setText("ID: " + id);
        selectedLabel.setUserData(id);
    }

    private void openSelector() {
        System.out.println("=== openSelector() called, referenceType: " + referenceType);
        new Thread(() -> {
            try {
                if ("MOTOR_WHEEL".equals(referenceType)) {
                    System.out.println("Loading MOTOR_WHEEL...");
                    List<MotorWheelDto> items = MotorWheelClient.getAll();
                    System.out.println("Loaded " + items.size() + " motor wheels");
                    Platform.runLater(() -> {
                        MotorWheelTreeBuilder builder = new MotorWheelTreeBuilder();
                        TreeSelectorDialog dialog = new TreeSelectorDialog(
                                ownerStage, "Выбор мотор-колеса", builder.buildTree(items),
                                selected -> {
                                    if (selected instanceof MotorWheelDto dto) {
                                        selectedId = dto.getId();
                                        selectedLabel.setText(dto.getFullMarking());
                                        selectedLabel.setUserData(selectedId);
                                        if (onSelect != null) onSelect.accept(selectedId);
                                    }
                                },
                                this::runDialog
                        );
                        dialog.show();
                    });
                } else if ("RADIAL_WHEEL".equals(referenceType)) {
                    System.out.println("Loading RADIAL_WHEEL...");
                    List<RadialWheelDto> items = RadialWheelClient.getAll();
                    System.out.println("Loaded " + items.size() + " radial wheels");
                    Platform.runLater(() -> {
                        RadialWheelTreeBuilder builder = new RadialWheelTreeBuilder();
                        TreeSelectorDialog dialog = new TreeSelectorDialog(
                                ownerStage, "Выбор радиального колеса", builder.buildTree(items),
                                selected -> {
                                    if (selected instanceof RadialWheelDto dto) {
                                        selectedId = dto.getId();
                                        selectedLabel.setText(dto.getFullMarking());
                                        selectedLabel.setUserData(selectedId);
                                        if (onSelect != null) onSelect.accept(selectedId);
                                    }
                                },
                                this::runDialog
                        );
                        dialog.show();
                    });
                } else if ("MOTOR".equals(referenceType)) {
                    System.out.println("Loading MOTOR...");
                    List<MotorDto> items = MotorClient.getAll();
                    System.out.println("Loaded " + items.size() + " motors");
                    Platform.runLater(() -> {
                        MotorTreeBuilder builder = new MotorTreeBuilder();
                        TreeSelectorDialog dialog = new TreeSelectorDialog(
                                ownerStage, "Выбор электродвигателя", builder.buildTree(items),
                                selected -> {
                                    if (selected instanceof MotorDto dto) {
                                        selectedId = dto.getId();
                                        selectedLabel.setText(dto.getFullMarking());
                                        selectedLabel.setUserData(selectedId);
                                        if (onSelect != null) onSelect.accept(selectedId);
                                    }
                                },
                                this::runDialog
                        );
                        dialog.show();
                    });
                } else if ("AXIAL_WHEEL".equals(referenceType)) {
                    System.out.println("Loading AXIAL_WHEEL...");
                    List<AxialWheelDto> items = AxialWheelClient.getAll();
                    System.out.println("Loaded " + items.size() + " axial wheels");
                    Platform.runLater(() -> {
                        AxialWheelTreeBuilder builder = new AxialWheelTreeBuilder();
                        TreeSelectorDialog dialog = new TreeSelectorDialog(
                                ownerStage, "Выбор осевого колеса", builder.buildTree(items),
                                selected -> {
                                    if (selected instanceof AxialWheelDto dto) {
                                        selectedId = dto.getId();
                                        selectedLabel.setText(dto.getFullMarking());
                                        selectedLabel.setUserData(selectedId);
                                        if (onSelect != null) onSelect.accept(selectedId);
                                    }
                                },
                                this::runDialog
                        );
                        dialog.show();
                    });
                } else if ("COMPONENT".equals(referenceType)) {
                    System.out.println("Loading COMPONENT...");
                    List<ComponentDto> items = ComponentClient.getAllComponents();
                    List<ComponentCategoryDto> categories = ComponentCategoryClient.getAllCategories();
                    List<ComponentClassDto> classes = ComponentClassClient.getAllClasses();
                    System.out.println("Loaded " + items.size() + " components");

                    Platform.runLater(() -> {
                        ComponentTreeBuilder builder = new ComponentTreeBuilder(categories, classes, items);
                        TreeSelectorDialog dialog = new TreeSelectorDialog(
                                ownerStage, "Выбор компонента", builder.buildTree(),
                                selected -> {
                                    if (selected instanceof ComponentDto dto) {
                                        selectedId = dto.getId();
                                        String displayName = dto.getDisplayName();
                                        selectedLabel.setText(displayName);
                                        selectedLabel.setUserData(selectedId);
                                        if (onSelect != null) {
                                            onSelect.accept(selectedId);
                                        }
                                    }
                                },
                                this::runDialog
                        );
                        dialog.show();
                    });
                } else {
                    System.out.println("Unknown referenceType: " + referenceType);
                }
            } catch (Exception e) {
                System.err.println("Error loading items: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void runDialog() {
        if ("COMPONENT".equals(referenceType)) {
            // Открываем существующее окно создания компонента
            openComponentCreationDialog();
        } else {
            // Для остальных типов - форма создания карточки
            CardFormController form = new CardFormController(
                    ownerStage,
                    referenceType,
                    null,
                    () -> Platform.runLater(this::openSelector)
            );
            form.show();
        }
    }

    private void openComponentCreationDialog() {
        LoadingDialog loadingDialog = new LoadingDialog(ownerStage, "Загрузка");
        loadingDialog.show();

        loadingDialog.loadAsync(() -> {
            // Загружаем все необходимые данные для ComponentsCatalogController
            List<ComponentCategoryDto> categories = ComponentCategoryClient.getAllCategories();
            List<ComponentClassDto> classes = ComponentClassClient.getAllClasses();
            List<ComponentDto> components = ComponentClient.getAllComponents();
            return new Object[]{categories, classes, components};
        }, result -> {
            Object[] data = (Object[]) result;
            @SuppressWarnings("unchecked")
            List<ComponentCategoryDto> categories = (List<ComponentCategoryDto>) data[0];
            @SuppressWarnings("unchecked")
            List<ComponentClassDto> classes = (List<ComponentClassDto>) data[1];
            @SuppressWarnings("unchecked")
            List<ComponentDto> components = (List<ComponentDto>) data[2];

            showComponentCreationWindow(categories, classes, components);
        });
    }

    private void showComponentCreationWindow(List<ComponentCategoryDto> categories,
                                             List<ComponentClassDto> classes,
                                             List<ComponentDto> components) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/ComponentsCatalogView.fxml"));
            Parent root = loader.load();

            ComponentsCatalogController controller = loader.getController();
            // Передаём предзагруженные данные, чтобы избежать повторной загрузки
            controller.setPreloadedData(categories, classes, components);

            Stage stage = new Stage();
            stage.setTitle("Создание компонента");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(ownerStage);
            stage.setScene(new Scene(root, 900, 600));

            // Ждём закрытия окна, чтобы обновить список компонентов
            stage.setOnHidden(e -> {
                // После закрытия обновляем список в основном окне выбора
                openSelector();
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Не удалось открыть окно создания компонента");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

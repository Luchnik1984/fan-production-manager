package com.fanproduction.gui.component;

import com.fanproduction.gui.builder.MotorTreeBuilder;
import com.fanproduction.gui.builder.MotorWheelTreeBuilder;
import com.fanproduction.gui.builder.RadialWheelTreeBuilder;
import com.fanproduction.gui.client.MotorClient;
import com.fanproduction.gui.client.MotorWheelClient;
import com.fanproduction.gui.client.RadialWheelClient;
import com.fanproduction.gui.controller.CardFormController;
import com.fanproduction.gui.dto.response.MotorDto;
import com.fanproduction.gui.dto.response.MotorWheelDto;
import com.fanproduction.gui.dto.response.RadialWheelDto;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.Getter;

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
        this.selectedLabel = new Label("Не выбрано");
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
        new Thread(() -> {
            try {
                if ("MOTOR_WHEEL".equals(referenceType)) {
                    List<MotorWheelDto> items = MotorWheelClient.getAll();
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
                    List<RadialWheelDto> items = RadialWheelClient.getAll();
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
                    List<MotorDto> items = MotorClient.getAll();
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
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    selectedLabel.setText("Ошибка загрузки");
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private void runDialog() {
        // Просто открываем форму создания — она сама создаст временную карточку
        CardFormController form = new CardFormController(
                ownerStage,
                referenceType,
                null,
                () -> Platform.runLater(this::openSelector)
        );
        form.show();
    }
}

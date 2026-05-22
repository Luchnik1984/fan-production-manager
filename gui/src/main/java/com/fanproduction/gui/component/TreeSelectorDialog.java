package com.fanproduction.gui.component;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.function.Consumer;

public class TreeSelectorDialog {

    private final Stage dialog;
    private final TreeView<Object> treeView;
    private final Consumer<Object> onSelect;
    private final Runnable onCreate;
    private Object selectedItem;

    public TreeSelectorDialog(Stage owner, String title, TreeItem<Object> root,
                              Consumer<Object> onSelect, Runnable onCreate) {
        this.onSelect = onSelect;
        this.onCreate = onCreate;
        this.dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setResizable(true);

        this.treeView = new TreeView<>(root);
        treeView.setShowRoot(false);
        treeView.setPrefHeight(400);

        treeView.setCellFactory(tv -> new TreeCell<Object>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    setGraphic(getTreeItem().getGraphic());
                }
            }
        });

        treeView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.getValue() != null) {
                selectedItem = newVal.getValue();
            } else {
                selectedItem = null;
            }
        });

        Button selectButton = new Button("Выбрать");
        selectButton.setDefaultButton(true);
        selectButton.setDisable(true);

        treeView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            selectButton.setDisable(newVal == null || newVal.getValue() == null);
        });

        selectButton.setOnAction(e -> {
            if (selectedItem != null) {
                onSelect.accept(selectedItem);
                dialog.close();
            }
        });

        Button createButton = new Button("➕ Создать новый");
        createButton.setOnAction(e -> {
            dialog.close();
            if (onCreate != null) {
                onCreate.run();
            }
        });

        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(10, selectButton, createButton, cancelButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        VBox layout = new VBox(10, treeView, buttonBox);
        layout.setPadding(new Insets(10));
        layout.setPrefSize(500, 600);

        Scene scene = new Scene(layout);
        dialog.setScene(scene);
    }

    public void show() {
        dialog.showAndWait();
    }
}

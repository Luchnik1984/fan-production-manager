package com.fanproduction.gui.service;

import com.fanproduction.gui.client.ProductCardClient;
import com.fanproduction.gui.dto.SelectableItem;
import com.fanproduction.gui.dto.response.ProductCardDto;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;

import java.util.List;

public class ReferenceDataService {

    public static void loadMotorWheels(ComboBox<SelectableItem> comboBox) {
        comboBox.getItems().clear();
        comboBox.getItems().add(new SelectableItem(null, "Загрузка..."));

        new Thread(() -> {
            try {
                List<ProductCardDto> items = ProductCardClient.getCardsByType("MOTOR_WHEEL");

                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    if (items != null && !items.isEmpty()) {
                        for (ProductCardDto dto : items) {
                            String displayName = dto.getName() + " (" + dto.getCode() + ")";
                            comboBox.getItems().add(new SelectableItem(dto.getId(), displayName));
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

    public static void loadRadialWheels(ComboBox<SelectableItem> comboBox) {
        comboBox.getItems().clear();
        comboBox.getItems().add(new SelectableItem(null, "Загрузка..."));

        new Thread(() -> {
            try {
                List<ProductCardDto> items = ProductCardClient.getCardsByType("RADIAL_WHEEL");

                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    if (items != null && !items.isEmpty()) {
                        for (ProductCardDto dto : items) {
                            String displayName = dto.getName() + " (" + dto.getCode() + ")";
                            comboBox.getItems().add(new SelectableItem(dto.getId(), displayName));
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

    public static void loadMotors(ComboBox<SelectableItem> comboBox) {
        comboBox.getItems().clear();
        comboBox.getItems().add(new SelectableItem(null, "Загрузка..."));

        new Thread(() -> {
            try {
                List<ProductCardDto> items = ProductCardClient.getCardsByType("MOTOR");

                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    if (items != null && !items.isEmpty()) {
                        for (ProductCardDto dto : items) {
                            String displayName = dto.getName() + " (" + dto.getCode() + ")";
                            comboBox.getItems().add(new SelectableItem(dto.getId(), displayName));
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

    public static void loadAxialWheels(ComboBox<SelectableItem> comboBox) {
        comboBox.getItems().clear();
        comboBox.getItems().add(new SelectableItem(null, "Загрузка..."));

        new Thread(() -> {
            try {
                List<ProductCardDto> items = ProductCardClient.getCardsByType("AXIAL_WHEEL");

                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    if (items != null && !items.isEmpty()) {
                        for (ProductCardDto dto : items) {
                            String displayName = dto.getName() + " (" + dto.getCode() + ")";
                            comboBox.getItems().add(new SelectableItem(dto.getId(), displayName));
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
}

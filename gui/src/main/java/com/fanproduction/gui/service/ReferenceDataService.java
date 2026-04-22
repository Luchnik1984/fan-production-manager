package com.fanproduction.gui.service;

import com.fanproduction.gui.client.ProductCardClient;
import com.fanproduction.gui.dto.response.ProductCardDto;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;

import java.util.List;

public class ReferenceDataService {

    public static void loadMotorWheels(ComboBox<String> comboBox) {
        comboBox.getItems().clear();
        comboBox.getItems().add("Загрузка...");

        new Thread(() -> {
            try {
                List<ProductCardDto> items = ProductCardClient.getCardsByType("MOTOR_WHEEL");

                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    if (items != null && !items.isEmpty()) {
                        for (ProductCardDto dto : items) {
                            String displayName = dto.getName() + " (" + dto.getCode() + ")";
                            comboBox.getItems().add(displayName);
                        }
                    } else {
                        comboBox.getItems().add("Нет данных");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    comboBox.getItems().add("Ошибка загрузки");
                });
                e.printStackTrace();
            }
        }).start();
    }

    public static void loadRadialWheels(ComboBox<String> comboBox) {
        comboBox.getItems().clear();
        comboBox.getItems().add("Загрузка...");

        new Thread(() -> {
            try {
                List<ProductCardDto> items = ProductCardClient.getCardsByType("RADIAL_WHEEL");

                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    if (items != null && !items.isEmpty()) {
                        for (ProductCardDto dto : items) {
                            String displayName = dto.getName() + " (" + dto.getCode() + ")";
                            comboBox.getItems().add(displayName);
                        }
                    } else {
                        comboBox.getItems().add("Нет данных");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    comboBox.getItems().add("Ошибка загрузки");
                });
                e.printStackTrace();
            }
        }).start();
    }

    public static void loadMotors(ComboBox<String> comboBox) {
        comboBox.getItems().clear();
        comboBox.getItems().add("Загрузка...");

        new Thread(() -> {
            try {
                List<ProductCardDto> items = ProductCardClient.getCardsByType("MOTOR");

                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    if (items != null && !items.isEmpty()) {
                        for (ProductCardDto dto : items) {
                            String displayName = dto.getName() + " (" + dto.getCode() + ")";
                            comboBox.getItems().add(displayName);
                        }
                    } else {
                        comboBox.getItems().add("Нет данных");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    comboBox.getItems().add("Ошибка загрузки");
                });
                e.printStackTrace();
            }
        }).start();
    }

    public static void loadAxialWheels(ComboBox<String> comboBox) {
        comboBox.getItems().clear();
        comboBox.getItems().add("Загрузка...");

        new Thread(() -> {
            try {
                List<ProductCardDto> items = ProductCardClient.getCardsByType("AXIAL_WHEEL");

                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    if (items != null && !items.isEmpty()) {
                        for (ProductCardDto dto : items) {
                            String displayName = dto.getName() + " (" + dto.getCode() + ")";
                            comboBox.getItems().add(displayName);
                        }
                    } else {
                        comboBox.getItems().add("Нет данных");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    comboBox.getItems().add("Ошибка загрузки");
                });
                e.printStackTrace();
            }
        }).start();
    }
}

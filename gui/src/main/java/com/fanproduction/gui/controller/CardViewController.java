package com.fanproduction.gui.controller;

import com.fanproduction.core.enums.CardTemplateType;
import com.fanproduction.gui.client.ComponentClient;
import com.fanproduction.gui.dto.response.ComponentDto;
import com.fanproduction.gui.dto.response.ProductCardDto;
import com.fanproduction.gui.util.NumberFormatter;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для просмотра карточки продукции в режиме "только чтение".
 * Вызывается по двойному щелчку на строке таблицы.
 */
public class CardViewController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final Map<String, String> FIELD_RUSSIAN_NAMES = new HashMap<>();

    static {
        // Общие поля
        FIELD_RUSSIAN_NAMES.put("name", "Наименование");
        FIELD_RUSSIAN_NAMES.put("code", "Код");
        FIELD_RUSSIAN_NAMES.put("cardType", "Тип карточки");
        FIELD_RUSSIAN_NAMES.put("createdAt", "Дата создания");
        FIELD_RUSSIAN_NAMES.put("createdBy", "Создал");

        // Поля электродвигателя
        FIELD_RUSSIAN_NAMES.put("series", "Серия");
        FIELD_RUSSIAN_NAMES.put("motorType", "Тип двигателя");
        FIELD_RUSSIAN_NAMES.put("poles", "Количество полюсов");
        FIELD_RUSSIAN_NAMES.put("powerKw", "Мощность (КВт)");
        FIELD_RUSSIAN_NAMES.put("ratedSpeedRpm", "Номинальная скорость (об/мин)");
        FIELD_RUSSIAN_NAMES.put("actualSpeedRpm", "Фактическая скорость (об/мин)");
        FIELD_RUSSIAN_NAMES.put("shaftSize", "Размер вала (мм)");
        FIELD_RUSSIAN_NAMES.put("mountingType", "Исполнение по монтажу");
        FIELD_RUSSIAN_NAMES.put("climateType", "Климатическое исполнение");
        FIELD_RUSSIAN_NAMES.put("voltage", "Напряжение (В)");
        FIELD_RUSSIAN_NAMES.put("operationMode", "Режим работы");
        FIELD_RUSSIAN_NAMES.put("weightKg", "Масса (кг)");
        FIELD_RUSSIAN_NAMES.put("generalPurpose", "Общего применения");
        FIELD_RUSSIAN_NAMES.put("fireproof", "Огнестойкость");
        FIELD_RUSSIAN_NAMES.put("fireproofMarking", "Маркировка огнестойкости");
        FIELD_RUSSIAN_NAMES.put("maxTemperature", "Предельная температура (°C)");
        FIELD_RUSSIAN_NAMES.put("explosionProof", "Взрывозащита");
        FIELD_RUSSIAN_NAMES.put("explosionMarking", "Маркировка взрывозащиты");
        FIELD_RUSSIAN_NAMES.put("fullMarking", "Полная маркировка");

        // Поля мотор-колеса
        FIELD_RUSSIAN_NAMES.put("manufacturer", "Производитель");
        FIELD_RUSSIAN_NAMES.put("bladeType", "Тип лопаток");
        FIELD_RUSSIAN_NAMES.put("size", "Размер");
        FIELD_RUSSIAN_NAMES.put("voltageCode", "Код напряжения");
        FIELD_RUSSIAN_NAMES.put("motorCode", "Код двигателя");

        // Поля радиального колеса
        FIELD_RUSSIAN_NAMES.put("marking", "Маркировка колеса");
        FIELD_RUSSIAN_NAMES.put("bladeMod", "Модификация лопатки");
        FIELD_RUSSIAN_NAMES.put("wheelFormula", "Формула колеса");
        FIELD_RUSSIAN_NAMES.put("bladeCount", "Количество лопаток");
        FIELD_RUSSIAN_NAMES.put("hubType", "Ступица");
        FIELD_RUSSIAN_NAMES.put("hubName", "Ступица");
        FIELD_RUSSIAN_NAMES.put("maxSpeedRpm", "Максимальная скорость (об/мин)");
        FIELD_RUSSIAN_NAMES.put("frontDiskMod", "Модификация переднего диска");
        FIELD_RUSSIAN_NAMES.put("wheelWidth", "Ширина колеса");
        FIELD_RUSSIAN_NAMES.put("bladeLengthCoeff", "Коэффициент длины лопатки");
        FIELD_RUSSIAN_NAMES.put("wheelCode", "Код колеса");

        // Поля осевого колеса
        FIELD_RUSSIAN_NAMES.put("execution", "Исполнение");
        FIELD_RUSSIAN_NAMES.put("trimCoefficient", "Коэф. подрезки (%)");
        FIELD_RUSSIAN_NAMES.put("bladeSlots", "Посадочных мест");
        FIELD_RUSSIAN_NAMES.put("bladeShape", "Форма лопатки");
        FIELD_RUSSIAN_NAMES.put("bladeAngle", "Угол установки");
        FIELD_RUSSIAN_NAMES.put("bladeMaterial", "Материал лопатки");
        FIELD_RUSSIAN_NAMES.put("wheelDiameter", "Диаметр колеса (мм)");

        // Поля канального вентилятора
        FIELD_RUSSIAN_NAMES.put("seriesName", "Наименование серии");
        FIELD_RUSSIAN_NAMES.put("ductSize", "Типоразмер");
        FIELD_RUSSIAN_NAMES.put("executionType", "Исполнение");
        FIELD_RUSSIAN_NAMES.put("ductFanType", "Тип колеса");
        FIELD_RUSSIAN_NAMES.put("motorWheelId", "Мотор-колесо");
        FIELD_RUSSIAN_NAMES.put("radialWheelId", "Радиальное колесо");
        FIELD_RUSSIAN_NAMES.put("motorId", "Электродвигатель");
        FIELD_RUSSIAN_NAMES.put("wheelSize", "Размер колеса (мм)");

        // Поля для хранения полной маркировки компонентов
        FIELD_RUSSIAN_NAMES.put("motorWheelFullMarking", "Мотор-колесо");
        FIELD_RUSSIAN_NAMES.put("radialWheelFullMarking", "Радиальное колесо");
        FIELD_RUSSIAN_NAMES.put("axialWheelFullMarking", "Осевое колесо");
        FIELD_RUSSIAN_NAMES.put("motorFullMarking", "Электродвигатель");
    }

    public static void show(Stage owner, ProductCardDto card) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Просмотр карточки - " + card.getName());

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Заголовок
        Label titleLabel = new Label(card.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Разделитель
        Separator separator = new Separator();

        // Форма с данными
        GridPane grid = createInfoGrid(card);

        // Кнопка закрытия
        Button closeButton = new Button("Закрыть");
        closeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        closeButton.setOnAction(e -> dialog.close());

        mainLayout.getChildren().addAll(titleLabel, separator, grid, closeButton);

        // Прокрутка для длинных форм
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);

        Scene scene = new Scene(scrollPane, 550, 600);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static GridPane createInfoGrid(ProductCardDto card) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        int row = 0;

        // Основная информация
        addInfoRow(grid, row++, "Наименование:", card.getName());
        addInfoRow(grid, row++, "Код:", card.getCode() != null ? card.getCode() : "—");

        String cardTypeDisplay = getCardTypeDisplay(card.getCardType());
        addInfoRow(grid, row++, "Тип карточки:", cardTypeDisplay);

        if (card.getCreatedAt() != null) {
            addInfoRow(grid, row++, "Дата создания:", card.getCreatedAt().format(DATE_FORMATTER));
        }

        if (card.getCreatedBy() != null && !card.getCreatedBy().isEmpty()) {
            addInfoRow(grid, row++, "Создал:", card.getCreatedBy());
        }

        Map<String, Object> fields = card.getFields();
        if (fields != null && !fields.isEmpty()) {
            Label separatorLabel = new Label("Технические характеристики:");
            separatorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            grid.add(separatorLabel, 0, row, 2, 1);
            row++;

            // ========== СПЕЦИАЛЬНАЯ ОБРАБОТКА ДЛЯ РАЗНЫХ ТИПОВ КАРТОЧЕК ==========

            // Для канального вентилятора: показываем компоненты
            if ("DUCT_FAN".equals(card.getCardType())) {
                String ductFanType = (String) fields.get("ductFanType");

                if ("Мотор-колесо".equals(ductFanType)) {
                    String motorWheelMarking = (String) fields.get("motorWheelFullMarking");
                    if (motorWheelMarking != null && !motorWheelMarking.isEmpty()) {
                        addInfoRow(grid, row++, "Мотор-колесо:", motorWheelMarking);
                    }
                    Object powerKw = fields.get("powerKw");
                    if (powerKw != null) {
                        addInfoRow(grid, row++, "Мощность (КВт):", powerKw.toString());
                    }
                } else if ("Радиальное колесо".equals(ductFanType)) {
                    String radialWheelMarking = (String) fields.get("radialWheelFullMarking");
                    if (radialWheelMarking != null && !radialWheelMarking.isEmpty()) {
                        addInfoRow(grid, row++, "Радиальное колесо:", radialWheelMarking);
                    }
                    String motorMarking = (String) fields.get("motorFullMarking");
                    if (motorMarking != null && !motorMarking.isEmpty()) {
                        addInfoRow(grid, row++, "Электродвигатель:", motorMarking);
                    }
                    Object powerKw = fields.get("powerKw");
                    if (powerKw != null) {
                        addInfoRow(grid, row++, "Мощность (КВт):", powerKw.toString());
                    }
                }
            }

            // Для радиального колеса: специальная обработка компонента ступицы
            if ("RADIAL_WHEEL".equals(card.getCardType())) {
                Object hubComponentId = fields.get("hubComponentId");
                if (hubComponentId instanceof Number) {
                    Long componentId = ((Number) hubComponentId).longValue();
                    String componentDisplayName = getComponentDisplayName(componentId);
                    addInfoRow(grid, row++, "Ступица:", componentDisplayName != null ? componentDisplayName : "—");
                    fields.put("hubComponentId_processed", true);
                }
            }

            // Получаем упорядоченный список полей для данного типа карточки
            List<String> orderedFields = getOrderedFields(card.getCardType());

            for (String fieldName : orderedFields) {
                // Пропускаем уже обработанные поля
                if (fields.containsKey(fieldName + "_processed")) {
                    continue;
                }

                Object value = fields.get(fieldName);
                if (value == null) continue;

                // Пропускаем поля, которые уже показаны выше
                if ("DUCT_FAN".equals(card.getCardType())) {
                    if ("powerKw".equals(fieldName)) continue;
                    if ("motorWheelFullMarking".equals(fieldName) ||
                            "radialWheelFullMarking".equals(fieldName) ||
                            "motorFullMarking".equals(fieldName)) {
                        continue;
                    }
                }

                // Специальная логика для огнестойкости и взрывозащиты
                if ("fireproofMarking".equals(fieldName)) {
                    Boolean fireproof = (Boolean) fields.get("fireproof");
                    if (fireproof == null || !fireproof) continue;
                }
                if ("explosionMarking".equals(fieldName)) {
                    Boolean explosionProof = (Boolean) fields.get("explosionProof");
                    if (explosionProof == null || !explosionProof) continue;
                }
                if ("maxTemperature".equals(fieldName)) {
                    Boolean fireproof = (Boolean) fields.get("fireproof");
                    if (fireproof == null || !fireproof) continue;
                }

                // Для булевых полей показываем всегда
                if (value instanceof Boolean) {
                    String russianName = FIELD_RUSSIAN_NAMES.getOrDefault(fieldName, fieldName);
                    String stringValue = formatValue(value);
                    addInfoRow(grid, row++, russianName + ":", stringValue);
                    continue;
                }

                // Для остальных полей пропускаем пустые строки
                if (value instanceof String && ((String) value).isEmpty()) continue;

                // Для hubComponentId уже обработали отдельно
                if ("hubComponentId".equals(fieldName)) continue;

                String russianName = FIELD_RUSSIAN_NAMES.getOrDefault(fieldName, fieldName);
                String stringValue = formatValue(value);
                addInfoRow(grid, row++, russianName + ":", stringValue);
            }
        }

        return grid;
    }

    /**
     * Получает displayName = name+" "+"("+designation+")" компонента по его ID через API
     */
    private static String getComponentDisplayName(Long componentId) {
        if (componentId == null) return null;
        try {
            ComponentDto component = ComponentClient.getComponentById(componentId);
            return component != null ? component.getDisplayName() : "Компонент #" + componentId;
        } catch (Exception e) {
            System.err.println("Failed to load component: " + e.getMessage());
            return "Компонент #" + componentId;
        }
    }

    /**
     * Возвращает упорядоченный список полей для отображения
     */
    private static List<String> getOrderedFields(String cardType) {
        List<String> orderedFields = new ArrayList<>();

        if ("RADIAL_WHEEL".equals(cardType)) {
            // Раздел 1: Основная информация
            orderedFields.add("manufacturer");
            orderedFields.add("series");
            orderedFields.add("size");
            orderedFields.add("marking");

            // Раздел 2: Характеристики колеса
            orderedFields.add("bladeType");
            orderedFields.add("hubComponentId");
            orderedFields.add("maxSpeedRpm");
            orderedFields.add("weightKg");

            // Раздел 3: Дополнительные параметры
            orderedFields.add("wheelFormula");
            orderedFields.add("wheelCode");
            orderedFields.add("bladeMod");
            orderedFields.add("frontDiskMod");
            orderedFields.add("wheelWidth");
            orderedFields.add("bladeLengthCoeff");
            orderedFields.add("bladeCount");
            orderedFields.add("diameter");

            // Раздел 4: Исполнение
            orderedFields.add("generalPurpose");
            orderedFields.add("fireproof");
            orderedFields.add("fireproofMarking");
            orderedFields.add("maxTemperature");
            orderedFields.add("explosionProof");
            orderedFields.add("explosionMarking");

            // Полная маркировка
            orderedFields.add("fullMarking");

        } else if ("MOTOR".equals(cardType)) {
            orderedFields.add("series");
            orderedFields.add("motorType");
            orderedFields.add("poles");
            orderedFields.add("powerKw");
            orderedFields.add("ratedSpeedRpm");
            orderedFields.add("actualSpeedRpm");
            orderedFields.add("shaftSize");
            orderedFields.add("mountingType");
            orderedFields.add("climateType");
            orderedFields.add("voltage");
            orderedFields.add("operationMode");
            orderedFields.add("weightKg");
            orderedFields.add("generalPurpose");
            orderedFields.add("fireproof");
            orderedFields.add("maxTemperature");
            orderedFields.add("explosionProof");
            orderedFields.add("explosionMarking");
            orderedFields.add("fullMarking");

        } else if ("MOTOR_WHEEL".equals(cardType)) {
            orderedFields.add("manufacturer");
            orderedFields.add("bladeType");
            orderedFields.add("size");
            orderedFields.add("poles");
            orderedFields.add("voltageCode");
            orderedFields.add("voltage");
            orderedFields.add("powerKw");
            orderedFields.add("ratedSpeedRpm");
            orderedFields.add("actualSpeedRpm");
            orderedFields.add("weightKg");
            orderedFields.add("motorCode");
            orderedFields.add("fullMarking");

        } else if ("AXIAL_WHEEL".equals(cardType)) {
            orderedFields.add("manufacturer");
            orderedFields.add("marking");
            orderedFields.add("bladeType");
            orderedFields.add("size");
            orderedFields.add("execution");
            orderedFields.add("trimCoefficient");
            orderedFields.add("bladeCount");
            orderedFields.add("bladeSlots");
            orderedFields.add("bladeShape");
            orderedFields.add("bladeAngle");
            orderedFields.add("bladeMaterial");
            orderedFields.add("wheelDiameter");
            orderedFields.add("wheelFormula");
            orderedFields.add("generalPurpose");
            orderedFields.add("fireproof");
            orderedFields.add("maxTemperature");
            orderedFields.add("explosionProof");
            orderedFields.add("explosionMarking");
            orderedFields.add("fullMarking");

        } else if ("DUCT_FAN".equals(cardType)) {
            orderedFields.add("seriesName");
            orderedFields.add("ductSize");
            orderedFields.add("executionType");
            orderedFields.add("ductFanType");
            orderedFields.add("poles");
            orderedFields.add("voltage");
            orderedFields.add("voltageCode");
            orderedFields.add("ratedSpeedRpm");
            orderedFields.add("actualSpeedRpm");
            orderedFields.add("powerKw");
            orderedFields.add("fullMarking");
            orderedFields.add("generalPurpose");
            orderedFields.add("fireproof");
            orderedFields.add("fireproofMarking");
            orderedFields.add("maxTemperature");
            orderedFields.add("explosionProof");
            orderedFields.add("explosionMarking");

        } else if ("AXIAL_FAN".equals(cardType)) {
            orderedFields.add("size");
            orderedFields.add("seriesName");
            orderedFields.add("execution");
            orderedFields.add("position");
            orderedFields.add("climateType");
            orderedFields.add("motorId");
            orderedFields.add("hubType");
            orderedFields.add("bladeCount");
            orderedFields.add("bladeSlots");
            orderedFields.add("bladeShape");
            orderedFields.add("bladeAngle");
            orderedFields.add("cableSpec");
            orderedFields.add("fanClass");
            orderedFields.add("fullMarking");

        } else if ("RADIAL_FAN".equals(cardType)) {
            orderedFields.add("seriesName");
            orderedFields.add("size");
            orderedFields.add("execution");
            orderedFields.add("climateType");
            orderedFields.add("motorId");
            orderedFields.add("radialWheelId");
            orderedFields.add("housingAngle");
            orderedFields.add("rotationDirection");
            orderedFields.add("cableSpec");
            orderedFields.add("fanClass");
            orderedFields.add("fullMarking");

        } else if ("CUP".equals(cardType)) {
            orderedFields.add("diameter");
            orderedFields.add("height");
            orderedFields.add("material");
            orderedFields.add("thickness");
            orderedFields.add("fullMarking");

        } else if ("ACCESSORY".equals(cardType)) {
            orderedFields.add("accessoryType");
            orderedFields.add("compatibleModels");
            orderedFields.add("vendorCode");
            orderedFields.add("unit");
            orderedFields.add("price");
            orderedFields.add("fullMarking");

        } else {
            // Для остальных типов - просто все поля
            return new ArrayList<>(FIELD_RUSSIAN_NAMES.keySet());
        }

        return orderedFields;
    }

    private static void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelControl = new Label(label);
        labelControl.setStyle("-fx-font-weight: bold;");

        Label valueControl = new Label(value);
        valueControl.setWrapText(true);

        grid.add(labelControl, 0, row);
        grid.add(valueControl, 1, row);
    }

    private static String getCardTypeDisplay(String cardType) {
        try {
            return CardTemplateType.valueOf(cardType).getDisplayName();
        } catch (IllegalArgumentException e) {
            return cardType;
        }
    }

    private static String formatValue(Object value) {
        if (value == null) return "—";
        if (value instanceof Boolean) {
            return (Boolean) value ? "Да" : "Нет";
        }
        if (value instanceof Number) {
            return NumberFormatter.formatNumber(value);
        }
        return value.toString();
    }

}

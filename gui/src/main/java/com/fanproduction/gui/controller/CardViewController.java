package com.fanproduction.gui.controller;

import com.fanproduction.gui.dto.response.ProductCardDto;
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
        FIELD_RUSSIAN_NAMES.put("maxTemperature", "Предельная температура (°C)");
        FIELD_RUSSIAN_NAMES.put("explosionProof", "Взрывозащита");
        FIELD_RUSSIAN_NAMES.put("explosionMarking", "Маркировка взрывозащиты");
        FIELD_RUSSIAN_NAMES.put("fullMarking", "Полная маркировка");

        // Поля мотор-колеса
        FIELD_RUSSIAN_NAMES.put("manufacturer", "Производитель");
        FIELD_RUSSIAN_NAMES.put("bladeType", "Тип лопаток");
        FIELD_RUSSIAN_NAMES.put("size", "Размер");
        FIELD_RUSSIAN_NAMES.put("voltageCode", "Код напряжения");

        // Поля радиального колеса

        FIELD_RUSSIAN_NAMES.put("marking", "Маркировка колеса");
        FIELD_RUSSIAN_NAMES.put("bladeMod", "Модификация лопатки");
        FIELD_RUSSIAN_NAMES.put("wheelFormula", "Формула колеса");
        FIELD_RUSSIAN_NAMES.put("bladeCount", "Количество лопаток");
        FIELD_RUSSIAN_NAMES.put("hubType", "Ступица");
        FIELD_RUSSIAN_NAMES.put("maxSpeedRpm", "Максимальная скорость (об/мин)");

        // Поля осевого колеса
        FIELD_RUSSIAN_NAMES.put("execution", "Исполнение");
        FIELD_RUSSIAN_NAMES.put("trimCoefficient", "Коэф. подрезки (%)");
        FIELD_RUSSIAN_NAMES.put("bladeSlots", "Посадочных мест");
        FIELD_RUSSIAN_NAMES.put("bladeShape", "Форма лопатки");
        FIELD_RUSSIAN_NAMES.put("bladeAngle", "Угол установки");
        FIELD_RUSSIAN_NAMES.put("bladeMaterial", "Материал лопатки");
        FIELD_RUSSIAN_NAMES.put("wheelDiameter", "Диаметр колеса (мм)");



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

        // Тип карточки
        String cardTypeDisplay = getCardTypeDisplay(card.getCardType());
        addInfoRow(grid, row++, "Тип карточки:", cardTypeDisplay);

        // Дата создания
        if (card.getCreatedAt() != null) {
            addInfoRow(grid, row++, "Дата создания:", card.getCreatedAt().format(DATE_FORMATTER));
        }

        // Создал
        if (card.getCreatedBy() != null && !card.getCreatedBy().isEmpty()) {
            addInfoRow(grid, row++, "Создал:", card.getCreatedBy());
        }

        // Специфичные поля
        Map<String, Object> fields = card.getFields();
        if (fields != null && !fields.isEmpty()) {
            // Разделитель
            Label separatorLabel = new Label("Технические характеристики:");
            separatorLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            grid.add(separatorLabel, 0, row, 2, 1);
            row++;

            // Определяем порядок полей для радиального колеса
            List<String> orderedFields = getOrderedFields(card.getCardType());
            System.out.println("All fields in card: " + fields.keySet());

            for (String fieldName : orderedFields) {
                Object value = fields.get(fieldName);

                // Пропускаем null
                if (value == null) continue;

                // Для булевых полей показываем всегда (даже если false)
                if (value instanceof Boolean) {
                    String russianName = FIELD_RUSSIAN_NAMES.getOrDefault(fieldName, fieldName);
                    String stringValue = formatValue(value);
                    addInfoRow(grid, row++, russianName + ":", stringValue);
                    continue;
                }

                // Для остальных полей пропускаем пустые строки
                if (value instanceof String && ((String) value).isEmpty()) continue;

                String russianName = FIELD_RUSSIAN_NAMES.getOrDefault(fieldName, fieldName);
                String stringValue = formatValue(value);
                addInfoRow(grid, row++, russianName + ":", stringValue);
            }
        }

        return grid;
    }

    /**
     * Возвращает упорядоченный список полей для отображения
     */
    private static List<String> getOrderedFields(String cardType) {
        List<String> orderedFields = new ArrayList<>();

        if ("RADIAL_WHEEL".equals(cardType)) {
            orderedFields.add("manufacturer");
            orderedFields.add("marking");
            orderedFields.add("bladeType");
            orderedFields.add("bladeMod");
            orderedFields.add("size");
            orderedFields.add("wheelFormula");
            orderedFields.add("bladeCount");
            orderedFields.add("hubType");
            orderedFields.add("maxSpeedRpm");
            orderedFields.add("weightKg");
            orderedFields.add("generalPurpose");
            orderedFields.add("fireproof");
            orderedFields.add("maxTemperature");
            orderedFields.add("explosionProof");
            orderedFields.add("explosionMarking");
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
        } else if ("AXIAL_WHEEL".equals(cardType)) {
            orderedFields.add("manufacturer");
            orderedFields.add("marking");
            orderedFields.add("bladeType");
            orderedFields.add("size");
            orderedFields.add("execution");
            orderedFields.add("trimCoefficient");
            orderedFields.add("hubType");
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
        Map<String, String> displayMap = Map.of(
                "MOTOR", "Электродвигатель",
                "MOTOR_WHEEL", "Мотор-колесо",
                "RADIAL_WHEEL", "Радиальное колесо",
                "AXIAL_FAN", "Осевой вентилятор",
                "RADIAL_FAN", "Радиальный вентилятор",
                "DUCT_FAN", "Канальный вентилятор",
                "CUP", "Стакан",
                "ACCESSORY", "Комплектующее"
        );
        return displayMap.getOrDefault(cardType, cardType);
    }

    private static String formatValue(Object value) {
        if (value == null) return "—";
        if (value instanceof Boolean) {
            return (Boolean) value ? "Да" : "Нет";
        }
        if (value instanceof Double) {
            // Форматирование с одной десятой
            return String.format("%.1f", (Double) value);
        }
        return value.toString();
    }
}

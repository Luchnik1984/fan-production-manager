package com.fanproduction.gui.controller;

import com.fanproduction.gui.dto.ProductCardDto;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
        FIELD_RUSSIAN_NAMES.put("fireproof", "Огнестойкий");
        FIELD_RUSSIAN_NAMES.put("maxTemperature", "Предельная температура (°C)");
        FIELD_RUSSIAN_NAMES.put("explosionProof", "Взрывозащищённый");
        FIELD_RUSSIAN_NAMES.put("explosionMarking", "Маркировка взрывозащиты");
        FIELD_RUSSIAN_NAMES.put("fullMarking", "Полная маркировка");
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

            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                String fieldName = entry.getKey();
                Object value = entry.getValue();

                if (value == null) continue;
                if (value instanceof String && ((String) value).isEmpty()) continue;

                String russianName = FIELD_RUSSIAN_NAMES.getOrDefault(fieldName, fieldName);
                String stringValue = formatValue(value);

                addInfoRow(grid, row++, russianName + ":", stringValue);
            }
        }

        return grid;
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

package com.fanproduction.gui.service;

import com.fanproduction.gui.dto.ProductCardDto;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Класс для настройки колонок таблицы карточек в зависимости от типа продукции.
 */
public class TableColumnConfigurator {

    /**
     * Настраивает колонки таблицы в зависимости от типа карточки
     */
    public static void setupColumns(TableView<ProductCardDto> table, String cardType) {
        table.getColumns().clear();

        List<TableColumn<ProductCardDto, ?>> allColumns = new ArrayList<>();

        // Наименование (с полной маркировкой) - основная колонка
        TableColumn<ProductCardDto, String> nameCol = new TableColumn<>("Наименование");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCardTypeDisplay()));
        nameCol.setPrefWidth(300);
        allColumns.add(nameCol);

        // Код (опционально, но полезно для поиска)
        TableColumn<ProductCardDto, String> codeCol = new TableColumn<>("Код");
        codeCol.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCode() != null ? cellData.getValue().getCode() : "—"));
        codeCol.setPrefWidth(120);
        allColumns.add(codeCol);

        // Специфичные колонки для типа
        if (cardType != null) {
            allColumns.addAll(getSpecificColumns(cardType));
        }

        table.getColumns().addAll(allColumns);
    }

    /**
     * Вспомогательный метод для получения значения поля
     */
    private static String getFieldValue(ProductCardDto dto, String fieldName) {
        Map<String, Object> fields = dto.getFields();
        if (fields == null) return "";
        Object value = fields.get(fieldName);
        return value != null ? value.toString() : "";
    }

    /**
     * Создаёт колонку с фабрикой
     */
    private static TableColumn<ProductCardDto, String> createStringColumn(String title, String fieldName, double width) {
        TableColumn<ProductCardDto, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> new SimpleStringProperty(getFieldValue(cellData.getValue(), fieldName)));
        column.setPrefWidth(width);
        return column;
    }

    /**
     * Получить специфичные колонки для типа
     */
    private static List<TableColumn<ProductCardDto, ?>> getSpecificColumns(String cardType) {
        List<TableColumn<ProductCardDto, ?>> columns = new ArrayList<>();

        switch (cardType) {
            case "MOTOR":
                columns.add(createStringColumn("Тип двигателя", "motorType", 100));
                columns.add(createStringColumn("Полюсов", "poles", 80));
                columns.add(createStringColumn("Мощность (КВт)", "powerKw", 100));
                columns.add(createStringColumn("Скорость (об/мин)", "ratedSpeedRpm", 110));
                columns.add(createStringColumn("Монтаж", "mountingType", 100));
                columns.add(createStringColumn("Климат", "climateType", 80));
                columns.add(createStringColumn("Напряжение (В)", "voltage", 90));
                columns.add(createStringColumn("Масса (кг)", "weightKg", 80));
                break;

            case "AXIAL_FAN":
                columns.add(createStringColumn("Серия", "seriesName", 120));
                columns.add(createStringColumn("Типоразмер", "size", 80));
                columns.add(createStringColumn("Лопатки", "bladeCount", 80));
                break;

            case "RADIAL_FAN":
                columns.add(createStringColumn("Серия", "seriesName", 120));
                columns.add(createStringColumn("Типоразмер", "size", 80));
                columns.add(createStringColumn("Лопатки", "bladeCount", 80));
                break;

            case "DUCT_FAN":
                columns.add(createStringColumn("Серия", "seriesName", 120));
                columns.add(createStringColumn("Тип", "ductFanType", 100));
                columns.add(createStringColumn("Размер", "wheelSize", 80));
                break;

            case "MOTOR_WHEEL":
                columns.add(createStringColumn("Размер", "size", 80));
                columns.add(createStringColumn("Полюсов", "poles", 80));
                columns.add(createStringColumn("Мощность (КВт)", "powerKw", 100));
                break;

            case "RADIAL_WHEEL":
                columns.add(createStringColumn("Маркировка", "marking", 120));
                columns.add(createStringColumn("Размер", "size", 80));
                columns.add(createStringColumn("Тип лопаток", "bladeType", 120));
                break;

            case "CUP":
                columns.add(createStringColumn("Диаметр (мм)", "diameter", 100));
                columns.add(createStringColumn("Высота (мм)", "height", 100));
                columns.add(createStringColumn("Материал", "material", 120));
                break;

            case "ACCESSORY":
                columns.add(createStringColumn("Тип", "accessoryType", 120));
                columns.add(createStringColumn("Артикул", "vendorCode", 120));
                break;
        }

        return columns;
    }
}

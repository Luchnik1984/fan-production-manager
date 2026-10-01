package com.fanproduction.gui.util;

import com.fanproduction.core.dto.TechnicalSpec;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Утилитный класс для экспорта данных в Excel
 */
public class ExcelExportUtil {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Данные для строки Excel (заголовок + значение)
     */
    public record ExcelRowData(String label, String value) {}

    /**
     * Экспортирует данные в Excel с двумя листами
     *
     * @param owner          родительское окно для диалога сохранения
     * @param fileNamePrefix префикс имени файла (например, "компонент" или "материал")
     * @param mainSheetName  название первого листа (основные поля)
     * @param mainSheetData  данные для первого листа (заголовок + значение)
     * @param specsSheetName название второго листа (технические характеристики)
     * @param specsSheetData данные для второго листа (технические характеристики)
     */
    public static void exportToExcel(Window owner,
                                     String fileNamePrefix,
                                     String mainSheetName,
                                     List<ExcelRowData> mainSheetData,
                                     String specsSheetName,
                                     List<TechnicalSpec> specsSheetData) {

        // Если fileNamePrefix содержит недопустимые символы, заменяем их
        String safePrefix = fileNamePrefix.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (safePrefix.isEmpty()) {
            safePrefix = "экспорт";
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить " + safePrefix);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel файлы", "*.xlsx")
        );

        String fileName = safePrefix + "_" + LocalDate.now().format(DATE_FORMATTER) + ".xlsx";
        fileChooser.setInitialFileName(fileName);

        File file = fileChooser.showSaveDialog(owner);
        if (file == null) {
            return;
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            // Лист 1: Основные поля
            Sheet mainSheet = workbook.createSheet(mainSheetName);
            createMainSheet(mainSheet, mainSheetData);

            // Лист 2: Технические характеристики
            Sheet specsSheet = workbook.createSheet(specsSheetName);
            createSpecsSheet(specsSheet, specsSheetData);

            // Авто-ширина колонок
            autoSizeColumns(mainSheet, 2);
            autoSizeColumns(specsSheet, 3);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            showSuccess("Экспорт " + safePrefix + " завершён");

        } catch (IOException e) {
            showError("Ошибка экспорта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Создаёт лист с основными полями
     */
    private static void createMainSheet(Sheet sheet, List<ExcelRowData> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        int rowNum = 0;
        for (ExcelRowData rowData : data) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowData.label());
            row.createCell(1).setCellValue(rowData.value() != null ? rowData.value() : "");
        }
    }

    /**
     * Создаёт лист с техническими характеристиками
     */
    private static void createSpecsSheet(Sheet sheet, List<TechnicalSpec> specs) {
        if (specs == null || specs.isEmpty()) {
            return;
        }

        // Фильтруем пустые строки
        List<TechnicalSpec> nonEmptySpecs = specs.stream()
                .filter(spec -> {
                    String name = spec.name();
                    String value = spec.value();
                    return (name != null && !name.trim().isEmpty()) ||
                            (value != null && !value.trim().isEmpty());
                })
                .toList();

        if (nonEmptySpecs.isEmpty()) {
            return;
        }

        // Создаём стиль для заголовков
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Заголовки
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "Характеристика", headerStyle);
        createCell(headerRow, 1, "Значение", headerStyle);
        createCell(headerRow, 2, "Ед. изм.", headerStyle);

        // Данные
        int rowNum = 1;
        for (TechnicalSpec spec : nonEmptySpecs) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(spec.name() != null ? spec.name() : "");
            row.createCell(1).setCellValue(spec.value() != null ? spec.value() : "");
            row.createCell(2).setCellValue(spec.unitCode() != null ? spec.unitCode() : "");
        }
    }

    /**
     * Создаёт ячейку с заданным стилем
     */
    private static void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    /**
     * Автоматически подгоняет ширину колонок
     */
    private static void autoSizeColumns(Sheet sheet, int columnCount) {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            return;
        }
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void showDialog(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setMinWidth(350);
            dialogPane.setMinHeight(150);

            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setPrefWidth(80);
                okButton.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            }

            alert.showAndWait();
        });
    }

    private static void showSuccess(String message) {
        showDialog("Успешно", message, Alert.AlertType.INFORMATION);
    }

    private static void showError(String message) {
        showDialog("Ошибка", message, Alert.AlertType.ERROR);
    }
}

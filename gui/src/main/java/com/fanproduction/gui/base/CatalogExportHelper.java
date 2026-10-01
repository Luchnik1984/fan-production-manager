package com.fanproduction.gui.base;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Хелпер для экспорта данных в Excel.
 */
public class CatalogExportHelper {

    private final Stage ownerStage;

    public CatalogExportHelper(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }

    /**
     * Экспортирует данные в Excel файл
     * @param data данные для экспорта
     * @param headers заголовки колонок
     * @param sheetName название листа
     * @param fileNamePrefix префикс имени файла
     */
    public void exportToExcel(List<ExportRowDto> data,
                              String[] headers,
                              String sheetName,
                              String fileNamePrefix) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить список");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel файлы", "*.xlsx"));
        fileChooser.setInitialFileName(fileNamePrefix + "_" + LocalDate.now() + ".xlsx");

        File file = fileChooser.showSaveDialog(ownerStage);
        if (file != null) {
            performExport(file, data, headers, sheetName);
        }
    }

    private void performExport(File file, List<ExportRowDto> data, String[] headers, String sheetName) {
        new Thread(() -> {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(sheetName);

                // Заголовки
                Row headerRow = sheet.createRow(0);
                CellStyle headerStyle = createHeaderStyle(workbook);

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Данные
                int rowNum = 1;
                for (ExportRowDto rowData : data) {
                    Row row = sheet.createRow(rowNum++);
                    List<String> values = rowData.getValues();
                    for (int i = 0; i < values.size() && i < headers.length; i++) {
                        row.createCell(i).setCellValue(values.get(i));
                    }
                }

                // Авто-ширина колонок
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                javafx.application.Platform.runLater(() -> showSuccess("Экспорт завершён"));

            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        showError("Ошибка экспорта: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void showSuccess(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Успешно");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

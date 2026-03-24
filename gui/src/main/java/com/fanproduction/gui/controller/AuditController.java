package com.fanproduction.gui.controller;

import com.fanproduction.core.dto.AuditLogDto;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.services.AuditService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AuditController {

    @FXML
    private ComboBox<AuditAction> actionFilterComboBox;

    @FXML
    private TextField userFilterField;

    @FXML
    private TableView<AuditLogDto> auditTable;

    @FXML
    private TableColumn<AuditLogDto, String> timestampColumn;

    @FXML
    private TableColumn<AuditLogDto, String> usernameColumn;

    @FXML
    private TableColumn<AuditLogDto, String> actionColumn;

    @FXML
    private TableColumn<AuditLogDto, String> detailsColumn;

    @FXML
    private TableColumn<AuditLogDto, String> ipAddressColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Label pageLabel;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button searchButton;

    @FXML
    private Button resetButton;

    private SpringContextProvider springContext;
    private AuditService auditService;
    private final ObservableList<AuditLogDto> auditList = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int totalPages = 0;
    private static final int PAGE_SIZE = 20;
    private String currentUserFilter = null;
    private AuditAction currentActionFilter = null;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public void setSpringContext(SpringContextProvider springContext) {
        this.springContext = springContext;
        this.auditService = springContext.getBean(AuditService.class);
        setupTable();
        loadAuditLogs();
        updateStatusInfo();
    }

    @FXML
    private void initialize() {
        actionFilterComboBox.setItems(FXCollections.observableArrayList(AuditAction.values()));
        actionFilterComboBox.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> handleSearch()
        );
        userFilterField.setOnAction(e -> handleSearch());
    }

    private void setupTable() {
        timestampColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().timestamp().format(DATE_FORMATTER))
        );
        usernameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().username())
        );
        actionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().action().toString())
        );
        detailsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().details() != null ? cellData.getValue().details() : "")
        );
        ipAddressColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().ipAddress() != null ? cellData.getValue().ipAddress() : "")
        );

        actionColumn.setCellFactory(column -> new TableCell<AuditLogDto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("SUCCESS") || item.contains("APPROVE") || item.contains("UNBLOCK")) {
                        setStyle("-fx-text-fill: green;");
                    } else if (item.contains("FAILED") || item.contains("REJECT") || item.contains("BLOCK")) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: #333;");
                    }
                }
            }
        });
    }

    private void loadAuditLogs() {
        Pageable pageable = PageRequest.of(currentPage, PAGE_SIZE, Sort.by("timestamp").descending());

        try {
            Page<AuditLogDto> page;

            if (currentUserFilter != null && !currentUserFilter.isEmpty()) {
                page = auditService.getLogsByUser(currentUserFilter, pageable);
            } else if (currentActionFilter != null) {
                page = auditService.getLogsByAction(currentActionFilter, pageable);
            } else {
                page = auditService.getLogs(pageable);
            }

            totalPages = page.getTotalPages();
            auditList.setAll(page.getContent());
            auditTable.setItems(auditList);

            pageLabel.setText("Страница " + (currentPage + 1) + " из " + Math.max(1, totalPages));

            prevButton.setDisable(currentPage == 0);
            nextButton.setDisable(currentPage >= totalPages - 1);

        } catch (Exception e) {
            statusLabel.setText("Ошибка загрузки: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatusInfo() {
        try {
            int total = auditService.getTotalCount();
            statusLabel.setText("Всего записей: " + total);
        } catch (Exception e) {
            statusLabel.setText("Всего записей: ?");
        }
    }

    @FXML
    private void handleSearch() {
        currentPage = 0;
        currentUserFilter = userFilterField.getText().trim();
        currentActionFilter = actionFilterComboBox.getValue();
        loadAuditLogs();
    }

    @FXML
    private void handleReset() {
        userFilterField.clear();
        actionFilterComboBox.getSelectionModel().clearSelection();
        currentPage = 0;
        currentUserFilter = null;
        currentActionFilter = null;
        loadAuditLogs();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadAuditLogs();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadAuditLogs();
        }
    }

    @FXML
    private void showExportDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Excel (XLSX)", "Excel (XLSX)", "CSV");
        dialog.setTitle("Экспорт журнала аудита");
        dialog.setHeaderText("Выберите формат экспорта");
        dialog.setContentText("Формат:");

        dialog.showAndWait().ifPresent(format -> {
            if ("Excel (XLSX)".equals(format)) {
                exportToExcel();
            } else {
                exportToCsv();
            }
        });
    }

    private void exportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить журнал аудита");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel файлы", "*.xlsx")
        );
        fileChooser.setInitialFileName("audit_log_" + LocalDate.now() + ".xlsx");

        File file = fileChooser.showSaveDialog(auditTable.getScene().getWindow());
        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Журнал аудита");

                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                String[] headers = {"Время", "Пользователь", "Действие", "Детали", "IP адрес"};
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowNum = 1;
                for (AuditLogDto log : auditList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(log.timestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));
                    row.createCell(1).setCellValue(log.username());
                    row.createCell(2).setCellValue(log.action().toString());
                    row.createCell(3).setCellValue(log.details() != null ? log.details() : "");
                    row.createCell(4).setCellValue(log.ipAddress() != null ? log.ipAddress() : "");
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                showInfo("Экспорт в Excel завершён: " + file.getName());

            } catch (IOException e) {
                showError("Ошибка экспорта в Excel: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exportToCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить журнал аудита");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV файлы", "*.csv")
        );
        fileChooser.setInitialFileName("audit_log_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(auditTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                writer.println("Время;Пользователь;Действие;Детали;IP адрес");

                for (AuditLogDto log : auditList) {
                    writer.printf("\"%s\";\"%s\";\"%s\";\"%s\";\"%s\"%n",
                            log.timestamp().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")),
                            escapeCsv(log.username()),
                            log.action().toString(),
                            escapeCsv(log.details() != null ? log.details() : ""),
                            escapeCsv(log.ipAddress() != null ? log.ipAddress() : "")
                    );
                }

                showInfo("Экспорт в CSV завершён: " + file.getName());
            } catch (IOException e) {
                showError("Ошибка экспорта в CSV: " + e.getMessage());
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

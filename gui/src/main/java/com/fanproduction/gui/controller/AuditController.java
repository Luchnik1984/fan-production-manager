package com.fanproduction.gui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.ApiResponse;
import com.fanproduction.gui.dto.AuditLogDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для просмотра журнала аудита.
 * Доступен только для пользователей с ролью ADMIN.
 */
public class AuditController {


    @FXML
    private ComboBox<String> actionFilterComboBox;

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


    private final ObservableList<AuditLogDto> auditList = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int totalPages = 0;
    private int totalElements = 0;
    private static final int PAGE_SIZE = 20;

    private String currentUserFilter = null;
    private String currentActionFilter = null;

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private static final DateTimeFormatter API_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Список возможных действий для фильтра
    private static final List<String> ACTION_OPTIONS = List.of(
            "LOGIN_SUCCESS", "LOGIN_FAILED", "LOGOUT", "REGISTER",
            "APPROVE_USER", "REJECT_USER", "BLOCK_USER", "UNBLOCK_USER",
            "CREATE_CARD", "UPDATE_CARD", "DELETE_CARD",
            "GENERATE_TZ", "GENERATE_PASSPORT", "GENERATE_PLATE",
            "UPDATE_PROFILE", "CHANGE_PASSWORD"
    );


    @FXML
    private void initialize() {
        setupFilters();
        setupTable();
        loadAuditLogs();
    }

    /**
     * Настраивает фильтры (выпадающий список и поле ввода)
     */
    private void setupFilters() {
        // Добавляем пустой элемент для сброса фильтра
        actionFilterComboBox.getItems().add("");
        actionFilterComboBox.getItems().addAll(ACTION_OPTIONS);
        actionFilterComboBox.setValue("");

        actionFilterComboBox.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                currentActionFilter = newVal;
            } else {
                currentActionFilter = null;
            }
            handleSearch();
        });

        userFilterField.setOnAction(e -> handleSearch());
    }

    /**
     * Настраивает таблицу (колонки и их отображение)
     */
    private void setupTable() {
        timestampColumn.setCellValueFactory(cellData -> {
            LocalDateTime timestamp = cellData.getValue().getTimestamp();
            String formatted = timestamp != null ? timestamp.format(DISPLAY_FORMATTER) : "";
            return new SimpleStringProperty(formatted);
        });

        usernameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUsername()));

        actionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getAction()));

        detailsColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDetails() != null ?
                        cellData.getValue().getDetails() : ""));

        ipAddressColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getIpAddress() != null ?
                        cellData.getValue().getIpAddress() : ""));

        // Цветовая подсветка действий
        actionColumn.setCellFactory(column -> new TableCell<AuditLogDto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("SUCCESS") || item.contains("APPROVE") ||
                            item.contains("UNBLOCK") || item.contains("LOGIN_SUCCESS")) {
                        setStyle("-fx-text-fill: green;");
                    } else if (item.contains("FAILED") || item.contains("REJECT") ||
                            item.contains("BLOCK") || item.contains("LOGIN_FAILED")) {
                        setStyle("-fx-text-fill: red;");
                    } else {
                        setStyle("-fx-text-fill: #333;");
                    }
                }
            }
        });

        auditTable.setItems(auditList);
    }


    /**
     * Загружает журнал аудита с сервера
     */
    private void loadAuditLogs() {
        // Показываем индикатор загрузки
        statusLabel.setText("Загрузка...");

        new Thread(() -> {
            try {
                StringBuilder url = new StringBuilder("/audit?page=" + currentPage + "&size=" + PAGE_SIZE);

                if (currentUserFilter != null && !currentUserFilter.isEmpty()) {
                    url.append("&username=").append(currentUserFilter);
                }
                if (currentActionFilter != null && !currentActionFilter.isEmpty()) {
                    url.append("&action=").append(currentActionFilter);
                }

                System.out.println("Loading audit logs: " + url);

                TypeReference<ApiResponse<Map<String, Object>>> typeRef =
                        new TypeReference<>() {};

                ApiResponse<Map<String, Object>> response = ApiClient.get(url.toString(), typeRef);

                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        Map<String, Object> data = response.getData();

                        // Извлекаем список записей
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> content =
                                (List<Map<String, Object>>) data.get("content");

                        auditList.clear();
                        for (Map<String, Object> item : content) {
                            AuditLogDto dto = new AuditLogDto();
                            dto.setId(((Number) item.get("id")).longValue());
                            dto.setUsername((String) item.get("username"));
                            dto.setAction((String) item.get("action"));
                            dto.setDetails((String) item.get("details"));
                            dto.setIpAddress((String) item.get("ipAddress"));

                            // Парсим timestamp (формат: "2026-04-01T14:33:36.274528")
                            String timestampStr = (String) item.get("timestamp");
                            if (timestampStr != null && !timestampStr.isEmpty()) {
                                try {
                                    // Удаляем наносекунды если нужно (оставляем только до миллисекунд)
                                    // LocalDateTime.parse работает с ISO форматом
                                    dto.setTimestamp(LocalDateTime.parse(timestampStr));
                                } catch (Exception e) {
                                    System.err.println("Failed to parse timestamp: " + timestampStr);
                                    e.printStackTrace();
                                }
                            }

                            auditList.add(dto);
                        }

                        // Извлекаем информацию о пагинации
                        totalPages = ((Number) data.get("totalPages")).intValue();
                        totalElements = ((Number) data.get("totalElements")).intValue();

                        updatePaginationControls();
                        statusLabel.setText("Всего записей: " + totalElements);

                        System.out.println("Loaded " + auditList.size() + " audit records");

                    } else {
                        statusLabel.setText("Ошибка загрузки: " + response.getMessage());
                        showAlert("Ошибка", "Не удалось загрузить журнал аудита: " + response.getMessage(),
                                Alert.AlertType.ERROR);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка: " + e.getMessage());
                    showAlert("Ошибка", "Не удалось загрузить журнал аудита: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Обновляет состояние элементов управления пагинацией
     */
    private void updatePaginationControls() {
        pageLabel.setText("Страница " + (currentPage + 1) + " из " + Math.max(1, totalPages));
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= totalPages - 1);
    }

    // ==================== ОБРАБОТЧИКИ СОБЫТИЙ ====================

    @FXML
    private void handleSearch() {
        currentPage = 0;
        currentUserFilter = userFilterField.getText().trim();
        if (currentUserFilter.isEmpty()) {
            currentUserFilter = null;
        }
        loadAuditLogs();
    }

    @FXML
    private void handleReset() {
        userFilterField.clear();
        actionFilterComboBox.setValue("");
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


    /**
     * Экспорт в Excel (XLSX)
     */
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

                // Стиль для заголовков
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                // Заголовки
                String[] headers = {"Время", "Пользователь", "Действие", "Детали", "IP адрес"};
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Данные
                int rowNum = 1;
                for (AuditLogDto log : auditList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(
                            log.getTimestamp() != null ? log.getTimestamp().format(DISPLAY_FORMATTER) : "");
                    row.createCell(1).setCellValue(log.getUsername());
                    row.createCell(2).setCellValue(log.getAction());
                    row.createCell(3).setCellValue(log.getDetails() != null ? log.getDetails() : "");
                    row.createCell(4).setCellValue(log.getIpAddress() != null ? log.getIpAddress() : "");
                }

                // Автоширина колонок
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                showAlert("Успешно", "Экспорт в Excel завершён: " + file.getName(),
                        Alert.AlertType.INFORMATION);

            } catch (IOException e) {
                showAlert("Ошибка", "Ошибка экспорта в Excel: " + e.getMessage(),
                        Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    /**
     * Экспорт в CSV
     */
    private void exportToCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить журнал аудита");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV файлы", "*.csv")
        );
        fileChooser.setInitialFileName("audit_log_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(auditTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
                writer.println("Время;Пользователь;Действие;Детали;IP адрес");

                for (AuditLogDto log : auditList) {
                    writer.printf("\"%s\";\"%s\";\"%s\";\"%s\";\"%s\"%n",
                            log.getTimestamp() != null ? log.getTimestamp().format(DISPLAY_FORMATTER) : "",
                            escapeCsv(log.getUsername()),
                            log.getAction(),
                            escapeCsv(log.getDetails() != null ? log.getDetails() : ""),
                            escapeCsv(log.getIpAddress() != null ? log.getIpAddress() : "")
                    );
                }

                showAlert("Успешно", "Экспорт в CSV завершён: " + file.getName(),
                        Alert.AlertType.INFORMATION);

            } catch (IOException e) {
                showAlert("Ошибка", "Ошибка экспорта в CSV: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

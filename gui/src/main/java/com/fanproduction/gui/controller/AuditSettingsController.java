package com.fanproduction.gui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.core.dto.AuditSettingsDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class AuditSettingsController {

    @FXML private TextField retentionDaysField;
    @FXML private TextField maxRecordsField;

    // Вместо TextField для cron
    @FXML private ComboBox<String> cleanupPeriodCombo;
    @FXML private ComboBox<String> cleanupTimeCombo;
    @FXML private Label cleanupPreviewLabel;
    @FXML private Label cleanupCronDisplay;

    @FXML private Label retentionDaysError;
    @FXML private Label maxRecordsError;

    @FXML private CheckBox logLoginSuccessCheck;
    @FXML private CheckBox logLoginFailedCheck;
    @FXML private CheckBox logLogoutCheck;
    @FXML private CheckBox logRegisterCheck;
    @FXML private CheckBox logUserManagementCheck;
    @FXML private CheckBox logCardCreateCheck;
    @FXML private CheckBox logCardUpdateCheck;
    @FXML private CheckBox logCardDeleteCheck;
    @FXML private CheckBox logDocumentGenerationCheck;
    @FXML private CheckBox logProfileChangesCheck;

    @FXML private Label updatedAtLabel;
    @FXML private Label updatedByLabel;

    @FXML private Label messageLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @Setter
    private Stage dialogStage;
    private AuditSettingsDto currentSettings;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private static final List<String> PERIOD_OPTIONS = Arrays.asList(
            "ежедневно",
            "каждые 2 дня",
            "каждые 3 дня",
            "каждую неделю",
            "каждые 2 недели",
            "каждый месяц"
    );

    @FXML
    private void initialize() {
        setupPeriodCombo();
        setupValidation();
        loadSettings();
    }

    private void setupPeriodCombo() {
        cleanupPeriodCombo.getItems().addAll(PERIOD_OPTIONS);
        cleanupPeriodCombo.setValue("ежедневно");

        // Заполняем временем с 00:00 до 23:00 с шагом 1 час
        List<String> times = Arrays.asList(
                "00:00", "01:00", "02:00", "03:00", "04:00", "05:00",
                "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
                "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
                "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
        );
        cleanupTimeCombo.getItems().addAll(times);
        cleanupTimeCombo.setValue("02:00");

        // Слушатели для обновления предпросмотра
        cleanupPeriodCombo.valueProperty().addListener((obs, old, val) -> updateCronPreview());
        cleanupTimeCombo.valueProperty().addListener((obs, old, val) -> updateCronPreview());
    }

    private void updateCronPreview() {
        String period = cleanupPeriodCombo.getValue();
        String time = cleanupTimeCombo.getValue();

        if (period == null || time == null) return;

        String cron = convertToCron(period, time);
        cleanupCronDisplay.setText("Cron: " + cron);

        String preview = formatPreview(period, time);
        cleanupPreviewLabel.setText(preview);

        // Скрываем технический лейбл, если он есть
        if (cleanupCronDisplay != null) {
            cleanupCronDisplay.setVisible(false);
            cleanupCronDisplay.setManaged(false);
        }
    }

    private String convertToCron(String period, String time) {
        String[] timeParts = time.split(":");
        String hour = timeParts[0];
        String minute = timeParts[1];

        return switch (period) {
            case "ежедневно" -> "0 " + minute + " " + hour + " * * *";
            case "каждые 2 дня" -> "0 " + minute + " " + hour + " */2 * *";
            case "каждые 3 дня" -> "0 " + minute + " " + hour + " */3 * *";
            case "каждую неделю" -> "0 " + minute + " " + hour + " * * MON";
            case "каждые 2 недели" -> "0 " + minute + " " + hour + " * * MON/2";
            case "каждый месяц" -> "0 " + minute + " " + hour + " 1 * *";
            default -> "0 " + minute + " " + hour + " * * *";
        };
    }

    private String formatPreview(String period, String time) {
        return switch (period) {
            case "ежедневно" -> "Очистка будет выполняться каждый день в " + time;
            case "каждые 2 дня" -> "Очистка будет выполняться каждые 2 дня в " + time;
            case "каждые 3 дня" -> "Очистка будет выполняться каждые 3 дня в " + time;
            case "каждую неделю" -> "Очистка будет выполняться каждую неделю в " + time + " (по понедельникам)";
            case "каждые 2 недели" -> "Очистка будет выполняться каждые 2 недели в " + time + " (по понедельникам)";
            case "каждый месяц" -> "Очистка будет выполняться первого числа каждого месяца в " + time;
            default -> "Очистка будет выполняться " + period + " в " + time;
        };
    }

    private void parseCronToUI(String cron) {
        if (cron == null || cron.isEmpty()) return;

        String[] parts = cron.split(" ");
        if (parts.length < 6) return;

        String minute = parts[1];
        String hour = parts[2];
        String dayOfMonth = parts[3];
        String month = parts[4];
        String dayOfWeek = parts[5];

        // Устанавливаем время
        cleanupTimeCombo.setValue(String.format("%02d:%02d",
                Integer.parseInt(hour), Integer.parseInt(minute)));

        // Определяем период
        if ("*".equals(dayOfMonth) && "*".equals(month) && "*".equals(dayOfWeek)) {
            cleanupPeriodCombo.setValue("ежедневно");
        } else if ("*/2".equals(dayOfMonth)) {
            cleanupPeriodCombo.setValue("каждые 2 дня");
        } else if ("*/3".equals(dayOfMonth)) {
            cleanupPeriodCombo.setValue("каждые 3 дня");
        } else if ("1".equals(dayOfMonth)) {
            cleanupPeriodCombo.setValue("каждый месяц");
        } else if ("MON".equals(dayOfWeek)) {
            cleanupPeriodCombo.setValue("каждую неделю");
        } else if ("MON/2".equals(dayOfWeek)) {
            cleanupPeriodCombo.setValue("каждые 2 недели");
        } else {
            cleanupPeriodCombo.setValue("ежедневно");
        }
    }

    private void setupValidation() {
        retentionDaysField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                retentionDaysField.setText(old);
            }
            retentionDaysError.setText("");
        });

        maxRecordsField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                maxRecordsField.setText(old);
            }
            maxRecordsError.setText("");
        });

        retentionDaysField.textProperty().addListener((obs, old, newVal) -> clearMessage());
        maxRecordsField.textProperty().addListener((obs, old, newVal) -> clearMessage());
    }

    private void clearMessage() {
        messageLabel.setText("");
        retentionDaysError.setText("");
        maxRecordsError.setText("");
    }

    private void loadSettings() {
        new Thread(() -> {
            try {
                TypeReference<ApiResponse<AuditSettingsDto>> typeRef = new TypeReference<>() {};
                ApiResponse<AuditSettingsDto> response = ApiClient.get("/audit/settings", typeRef);

                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        currentSettings = response.getData();
                        updateUI();
                    } else {
                        showError("Ошибка загрузки настроек: " + response.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Ошибка загрузки: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private void updateUI() {
        if (currentSettings == null) return;

        retentionDaysField.setText(String.valueOf(currentSettings.retentionDays()));
        maxRecordsField.setText(String.valueOf(currentSettings.maxRecords()));

        // Преобразуем cron в понятный вид
        if (currentSettings.cleanupCron() != null && !currentSettings.cleanupCron().isEmpty()) {
            parseCronToUI(currentSettings.cleanupCron());
        }

        logLoginSuccessCheck.setSelected(currentSettings.logLoginSuccess());
        logLoginFailedCheck.setSelected(currentSettings.logLoginFailed());
        logLogoutCheck.setSelected(currentSettings.logLogout());
        logRegisterCheck.setSelected(currentSettings.logRegister());
        logUserManagementCheck.setSelected(currentSettings.logUserManagement());
        logCardCreateCheck.setSelected(currentSettings.logCardCreate());
        logCardUpdateCheck.setSelected(currentSettings.logCardUpdate());
        logCardDeleteCheck.setSelected(currentSettings.logCardDelete());
        logDocumentGenerationCheck.setSelected(currentSettings.logDocumentGeneration());
        logProfileChangesCheck.setSelected(currentSettings.logProfileChanges());

        if (currentSettings.updatedAt() != null) {
            updatedAtLabel.setText(currentSettings.updatedAt().format(DATE_FORMATTER));
        } else {
            updatedAtLabel.setText("—");
        }
        updatedByLabel.setText(currentSettings.updatedBy() != null ? currentSettings.updatedBy() : "—");

        // Обновляем предпросмотр
        updateCronPreview();
    }

    @FXML
    private void handleSave() {
        clearMessage();

        if (!validateInputs()) {
            return;
        }

        AuditSettingsDto updatedSettings = createAuditSettingsDto();

        saveButton.setDisable(true);
        saveButton.setText("Сохранение...");

        new Thread(() -> {
            try {
                TypeReference<ApiResponse<AuditSettingsDto>> typeRef = new TypeReference<>() {};
                ApiResponse<AuditSettingsDto> response = ApiClient.put("/audit/settings", updatedSettings, typeRef);

                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    saveButton.setText("Сохранить");

                    if (response.isSuccess() && response.getData() != null) {
                        currentSettings = response.getData();
                        updateUI();
                        showSuccess("Настройки успешно сохранены");

                        new Thread(() -> {
                            try {
                                Thread.sleep(1500);
                                Platform.runLater(() -> dialogStage.close());
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    } else {
                        showError("Ошибка сохранения: " + response.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    saveButton.setText("Сохранить");
                    showError("Ошибка: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }

    private AuditSettingsDto createAuditSettingsDto() {
        int retentionDays = Integer.parseInt(retentionDaysField.getText().trim());
        int maxRecords = Integer.parseInt(maxRecordsField.getText().trim());

        String period = cleanupPeriodCombo.getValue();
        String time = cleanupTimeCombo.getValue();
        String cleanupCron = convertToCron(period, time);

        return new AuditSettingsDto(
                currentSettings.id(),
                retentionDays,
                maxRecords,
                cleanupCron,
                logLoginSuccessCheck.isSelected(),
                logLoginFailedCheck.isSelected(),
                logLogoutCheck.isSelected(),
                logRegisterCheck.isSelected(),
                logUserManagementCheck.isSelected(),
                logCardCreateCheck.isSelected(),
                logCardUpdateCheck.isSelected(),
                logCardDeleteCheck.isSelected(),
                logDocumentGenerationCheck.isSelected(),
                logProfileChangesCheck.isSelected(),
                null,
                null
        );
    }

    private boolean validateInputs() {
        boolean valid = true;

        String retentionDaysText = retentionDaysField.getText().trim();
        if (retentionDaysText.isEmpty()) {
            retentionDaysError.setText("Введите срок хранения");
            valid = false;
        } else {
            try {
                int days = Integer.parseInt(retentionDaysText);
                if (days < 0) {
                    retentionDaysError.setText("Срок хранения не может быть отрицательным");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                retentionDaysError.setText("Введите число");
                valid = false;
            }
        }

        String maxRecordsText = maxRecordsField.getText().trim();
        if (maxRecordsText.isEmpty()) {
            maxRecordsError.setText("Введите максимальное количество записей");
            valid = false;
        } else {
            try {
                int records = Integer.parseInt(maxRecordsText);
                if (records < 0) {
                    maxRecordsError.setText("Количество записей не может быть отрицательным");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                maxRecordsError.setText("Введите число");
                valid = false;
            }
        }

        return valid;
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: green;");
    }
}
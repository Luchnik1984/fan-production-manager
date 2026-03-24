package com.fanproduction.gui.controller;

import com.fanproduction.core.dto.UserDto;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.services.AuditService;
import com.fanproduction.services.UserService;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ModerationController {

    private static final Logger log = LoggerFactory.getLogger(ModerationController.class);

    @FXML
    private ComboBox<UserStatus> statusFilterComboBox;

    @FXML
    private TableView<UserDto> usersTable;

    @FXML
    private TableColumn<UserDto, Long> idColumn;

    @FXML
    private TableColumn<UserDto, String> emailColumn;

    @FXML
    private TableColumn<UserDto, String> nameColumn;

    @FXML
    private TableColumn<UserDto, String> phoneColumn;

    @FXML
    private TableColumn<UserDto, String> roleColumn;

    @FXML
    private TableColumn<UserDto, UserStatus> statusColumn;

    @FXML
    private TableColumn<UserDto, String> createdAtColumn;

    @FXML
    private TableColumn<UserDto, String> approvedByColumn;

    @FXML
    private TableColumn<UserDto, String> rejectionReasonColumn;

    @FXML
    private Button approveButton;

    @FXML
    private Button rejectButton;

    @FXML
    private Button blockButton;

    @FXML
    private Button unblockButton;

    @FXML
    private Label statusLabel;

    private SpringContextProvider springContext;
    private UserService userService;
    private AuditService auditService;
    private String currentAdminEmail;
    private ObservableList<UserDto> userList = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void setSpringContext(SpringContextProvider springContext) {
        this.springContext = springContext;
        this.userService = springContext.getBean(UserService.class);
        this.auditService = springContext.getBean(AuditService.class);
        loadCurrentUser();
        setupTable();
        loadUsers();
    }

    private void loadCurrentUser() {
        currentAdminEmail = com.fanproduction.core.context.SessionContext.getCurrentUser().getEmail();
    }

    @FXML
    private void initialize() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(UserStatus.values()));
        statusFilterComboBox.setValue(UserStatus.PENDING);
        statusFilterComboBox.valueProperty().addListener((obs, old, newVal) -> loadUsers());

        approveButton.setOnAction(e -> approveUser());
        rejectButton.setOnAction(e -> rejectUser());
        blockButton.setOnAction(e -> blockUser());
        unblockButton.setOnAction(e -> unblockUser());

        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> updateButtonsState(newVal));
    }

    private void updateButtonsState(UserDto selected) {
        boolean hasSelection = selected != null;
        approveButton.setDisable(!hasSelection);
        rejectButton.setDisable(!hasSelection);
        blockButton.setDisable(!hasSelection);
        unblockButton.setDisable(!hasSelection);

        if (selected != null) {
            boolean isPending = selected.status() == UserStatus.PENDING;
            boolean isActive = selected.status() == UserStatus.ACTIVE;
            boolean isBlocked = selected.status() == UserStatus.BLOCKED;
            boolean isRejected = selected.status() == UserStatus.REJECTED;

            approveButton.setDisable(!isPending);
            rejectButton.setDisable(!isPending);
            blockButton.setDisable(!isActive);
            unblockButton.setDisable(!isBlocked);
        }
    }

    private void setupTable() {
        idColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().id()).asObject());
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().email()));
        nameColumn.setCellValueFactory(cellData -> {
            String firstName = cellData.getValue().firstName() != null ? cellData.getValue().firstName() : "";
            String lastName = cellData.getValue().lastName() != null ? cellData.getValue().lastName() : "";
            String fullName = firstName + " " + lastName;
            return new SimpleStringProperty(fullName.trim());
        });
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().phone() != null ? cellData.getValue().phone() : ""
        ));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().role().toString()
        ));
        statusColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().status()));
        createdAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().createdAt() != null) {
                return new SimpleStringProperty(cellData.getValue().createdAt().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("");
        });
        approvedByColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().approvedBy() != null ? cellData.getValue().approvedBy() : ""
        ));
        rejectionReasonColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().rejectionReason() != null ? cellData.getValue().rejectionReason() : ""
        ));

        statusColumn.setCellFactory(column -> new TableCell<UserDto, UserStatus>() {
            @Override
            protected void updateItem(UserStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    switch (item) {
                        case PENDING -> setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        case ACTIVE -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        case REJECTED -> setStyle("-fx-text-fill: red;");
                        case BLOCKED -> setStyle("-fx-text-fill: gray;");
                    }
                }
            }
        });
    }

    private void loadUsers() {
        try {
            UserStatus selectedStatus = statusFilterComboBox.getValue();
            List<UserDto> users;

            if (selectedStatus != null) {
                users = userService.getUsersByStatus(selectedStatus);
            } else {
                users = userService.getAllUsers();
            }

            userList.setAll(users);
            usersTable.setItems(userList);
            statusLabel.setText("Найдено пользователей: " + userList.size());

        } catch (Exception e) {
            log.error("Ошибка загрузки пользователей", e);
            statusLabel.setText("Ошибка загрузки: " + e.getMessage());
        }
    }

    private void approveUser() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение");
        confirmAlert.setHeaderText("Подтверждение регистрации");
        confirmAlert.setContentText("Вы уверены, что хотите подтвердить регистрацию пользователя " + selected.email() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                userService.approveUser(selected.id(), currentAdminEmail);
                showSuccessMessage("Пользователь " + selected.email() + " подтверждён");
                auditService.log(currentAdminEmail, com.fanproduction.core.enums.AuditAction.APPROVE_USER,
                        "Подтверждена регистрация пользователя: " + selected.email());
                loadUsers();
            } catch (Exception e) {
                showErrorMessage("Ошибка при подтверждении: " + e.getMessage());
            }
        }
    }

    private void rejectUser() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Отклонение");
        reasonDialog.setHeaderText("Отклонение регистрации пользователя " + selected.email());
        reasonDialog.setContentText("Укажите причину отклонения:");

        Optional<String> result = reasonDialog.showAndWait();
        if (result.isPresent()) {
            String reason = result.get().trim();
            if (reason.isEmpty()) {
                showErrorMessage("Необходимо указать причину отклонения");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Подтверждение");
            confirmAlert.setHeaderText("Отклонение регистрации");
            confirmAlert.setContentText("Вы уверены, что хотите отклонить регистрацию пользователя " + selected.email() + "?\n\nПричина: " + reason);

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    userService.rejectUser(selected.id(), currentAdminEmail, reason);
                    showSuccessMessage("Регистрация пользователя " + selected.email() + " отклонена");
                    auditService.log(currentAdminEmail, com.fanproduction.core.enums.AuditAction.REJECT_USER,
                            "Отклонена регистрация пользователя: " + selected.email() + ". Причина: " + reason);
                    loadUsers();
                } catch (Exception e) {
                    showErrorMessage("Ошибка при отклонении: " + e.getMessage());
                }
            }
        }
    }

    private void blockUser() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Блокировка");
        reasonDialog.setHeaderText("Блокировка пользователя " + selected.email());
        reasonDialog.setContentText("Укажите причину блокировки (необязательно):");

        Optional<String> result = reasonDialog.showAndWait();
        String reason = result.orElse("");

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение");
        confirmAlert.setHeaderText("Блокировка пользователя");
        confirmAlert.setContentText("Вы уверены, что хотите заблокировать пользователя " + selected.email() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                userService.blockUser(selected.id(), currentAdminEmail, reason);
                showSuccessMessage("Пользователь " + selected.email() + " заблокирован");
                auditService.log(currentAdminEmail, com.fanproduction.core.enums.AuditAction.BLOCK_USER,
                        "Заблокирован пользователь: " + selected.email() + ". Причина: " + reason);
                loadUsers();
            } catch (Exception e) {
                showErrorMessage("Ошибка при блокировке: " + e.getMessage());
            }
        }
    }

    private void unblockUser() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение");
        confirmAlert.setHeaderText("Разблокировка пользователя");
        confirmAlert.setContentText("Вы уверены, что хотите разблокировать пользователя " + selected.email() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                userService.unblockUser(selected.id(), currentAdminEmail);
                showSuccessMessage("Пользователь " + selected.email() + " разблокирован");
                auditService.log(currentAdminEmail, com.fanproduction.core.enums.AuditAction.UNBLOCK_USER,
                        "Разблокирован пользователь: " + selected.email());
                loadUsers();
            } catch (Exception e) {
                showErrorMessage("Ошибка при разблокировке: " + e.getMessage());
            }
        }
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успешно");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

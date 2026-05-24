package com.fanproduction.gui.controller;

import com.fanproduction.gui.base.BaseCatalogController;
import com.fanproduction.gui.base.CatalogHelper;
import com.fanproduction.gui.base.CategoryTreeItem;
import com.fanproduction.gui.base.ExportRowDto;
import com.fanproduction.gui.client.*;
import com.fanproduction.gui.component.GroupedComboBox;
import com.fanproduction.gui.dto.request.CreateComponentRequest;
import com.fanproduction.gui.dto.response.*;
import com.fanproduction.gui.util.TooltipUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.*;
import java.util.stream.Collectors;

public class ComponentsCatalogController extends BaseCatalogController<ComponentDto, ComponentCategoryDto, ComponentClassDto> {

    @FXML private TextField searchField;
    @FXML private TableView<ComponentDto> componentsTable;
    @FXML private TableColumn<ComponentDto, String> nameColumn;
    @FXML private TableColumn<ComponentDto, String> classNameColumn;
    @FXML private TableColumn<ComponentDto, String> vendorCodeColumn;
    @FXML private TableColumn<ComponentDto, String> unitColumn;
    @FXML private TableColumn<ComponentDto, String> descriptionColumn;
    @FXML private Label statusLabel;
    @FXML private Button createCategoryButton;
    @FXML private Button createClassButton;
    @FXML private Button createButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button exportButton;
    @FXML private TreeView<CategoryTreeItem> categoryTreeView;

    @FXML
    private void initialize() {
        setupTable();
        loadData();
        getTableView().getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> updateButtonsState(newVal));
    }

    @Override
    protected void setupTable() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        classNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClassName()));
        classNameColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        vendorCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getVendorCode() != null ? cellData.getValue().getVendorCode() : ""));
        vendorCodeColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getUnitCode() != null ? cellData.getValue().getUnitCode() : ""));

        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : ""));
        descriptionColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        componentsTable.setItems(itemList);

        componentsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ComponentDto selected = componentsTable.getSelectionModel().getSelectedItem();
                if (selected != null) showEditItemDialog(selected);
            }
        });
    }

    private void updateButtonsState(ComponentDto selected) {
        boolean hasSelection = selected != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    // ==========================================
    // РЕАЛИЗАЦИЯ АБСТРАКТНЫХ МЕТОДОВ
    // ==========================================

    @Override
    protected List<ComponentCategoryDto> fetchCategories() throws Exception {
        return ComponentCategoryClient.getAllCategories();
    }

    @Override
    protected List<ComponentClassDto> fetchClasses() throws Exception {
        return ComponentClassClient.getAllClasses();
    }

    @Override
    protected List<ComponentDto> fetchAllItems() throws Exception {
        return ComponentClient.getAllComponents();
    }

    @Override
    protected void deleteCategoryById(Long id) throws Exception {
        ComponentCategoryClient.deleteCategory(id);
    }

    @Override
    protected void deleteClassById(Long id) throws Exception {
        ComponentClassClient.deleteClass(id);
    }

    @Override
    protected void deleteItemById(Long id) throws Exception {
        ComponentClient.deleteComponent(id);
    }

    @Override
    protected void updateCategory(Long id, String newName, Long parentId, String description) {
        new Thread(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("name", newName);
                if (parentId != null) request.put("parentId", parentId);
                if (description != null) request.put("description", description);
                ApiClient.put("/components/categories/" + id, request, new TypeReference<ApiResponse<Void>>() {});
                Platform.runLater(this::loadData);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", e.getMessage()));
            }
        }).start();
    }

    @Override
    protected void updateClass(Long id, String newName, String description, Long categoryId) {
        new Thread(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("name", newName);
                request.put("categoryId", categoryId);
                if (description != null) request.put("description", description);
                ApiClient.put("/components/classes/" + id, request, new TypeReference<ApiResponse<Void>>() {});
                Platform.runLater(this::loadData);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", e.getMessage()));
            }
        }).start();
    }

    @Override
    protected void createClass(Long categoryId, String name, String description, Long unitId) throws Exception {
        ComponentClassClient.createClass(categoryId, name, description, unitId);
    }

    @Override
    protected List<ExportRowDto> getExportData() {
        List<ExportRowDto> data = new ArrayList<>();
        for (ComponentDto dto : itemList) {
            List<String> values = Arrays.asList(
                    dto.getName(),
                    dto.getClassName() != null ? dto.getClassName() : "",
                    dto.getVendorCode() != null ? dto.getVendorCode() : "",
                    dto.getUnitCode() != null ? dto.getUnitCode() : "",
                    dto.getDescription() != null ? dto.getDescription() : ""
            );
            data.add(new ExportRowDto(values));
        }
        return data;
    }

    @Override
    protected String[] getExportHeaders() {
        return new String[]{"Наименование", "Класс", "Артикул", "Ед. изм.", "Описание"};
    }

    @Override
    protected String getItemTypeName() {
        return "компоненты";
    }

    // ==========================================
    // GETTERS ДЛЯ ПОЛЕЙ (для хелперов)
    // ==========================================

    @Override
    protected Long getCategoryId(ComponentCategoryDto category) {
        return category.getId();
    }

    @Override
    protected String getCategoryName(ComponentCategoryDto category) {
        return category.getName();
    }

    @Override
    protected Long getCategoryParentId(ComponentCategoryDto category) {
        return category.getParentId();
    }

    @Override
    protected String getCategoryDescription(ComponentCategoryDto category) {
        return category.getDescription();
    }

    @Override
    protected Long getClassId(ComponentClassDto cls) {
        return cls.getId();
    }

    @Override
    protected String getClassName(ComponentClassDto cls) {
        return cls.getName();
    }

    @Override
    protected Long getClassCategoryId(ComponentClassDto cls) {
        return cls.getCategoryId();
    }

    @Override
    protected String getClassDescription(ComponentClassDto cls) {
        return cls.getDescription();
    }

    @Override
    protected Long getItemId(ComponentDto item) {
        return item.getId();
    }

    @Override
    protected String getItemName(ComponentDto item) {
        return item.getName();
    }

    @Override
    protected Long getItemClassId(ComponentDto item) {
        return item.getClassId();
    }

    // ==========================================
    // UI ДОСТУП
    // ==========================================

    @Override
    protected TreeView<CategoryTreeItem> getTreeView() {
        return categoryTreeView;
    }

    @Override
    protected Label getStatusLabel() {
        return statusLabel;
    }

    @Override
    protected TableView<ComponentDto> getTableView() {
        return componentsTable;
    }

    // ==========================================
    // СОЗДАНИЕ/РЕДАКТИРОВАНИЕ ЭЛЕМЕНТОВ
    // ==========================================

    @Override
    protected void showCreateItemDialog() {
        showComponentDialog(null);
    }

    @Override
    protected void showEditItemDialog(ComponentDto existing) {
        showComponentDialog(existing);
    }

    private void showComponentDialog(ComponentDto existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Создание компонента" : "Редактирование компонента");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Выбор категории
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().add("— Все категории —");
        Map<String, Long> categoryIdMap = new HashMap<>();
        for (ComponentCategoryDto rootCat : allCategories.stream()
                .filter(c -> c.getParentId() == null).toList()) {
            categoryCombo.getItems().add(rootCat.getName());
            categoryIdMap.put(rootCat.getName(), rootCat.getId());
            addChildCategoriesToCombo(categoryCombo, rootCat, 1, categoryIdMap);
        }
        categoryCombo.setValue("— Все категории —");

        // Выбор класса
        ComboBox<String> classCombo = new ComboBox<>();
        classCombo.setPromptText("Выберите класс");
        classCombo.setDisable(true);
        Map<String, ComponentClassDto> classMap = new HashMap<>();

        Label warningLabel = new Label();
        warningLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        warningLabel.setVisible(false);

        // Поля компонента
        TextField nameField = new TextField();
        nameField.setPromptText("Наименование компонента");
        TextField vendorCodeField = new TextField();
        vendorCodeField.setPromptText("Артикул производителя");

        GroupedComboBox<UnitOfMeasureDto> unitCombo = CatalogHelper.createUnitCombo(allUnits);
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание");
        descriptionField.setPrefRowCount(3);

        // Логика выбора категории -> класс
        categoryCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || "— Все категории —".equals(newVal)) {
                classCombo.setDisable(true);
                classCombo.getItems().clear();
                warningLabel.setVisible(false);
            } else {
                Long selectedCategoryId = categoryIdMap.get(newVal);
                if (selectedCategoryId != null) {
                    classMap.clear();
                    List<ComponentClassDto> filteredClasses = allClasses.stream()
                            .filter(cls -> cls.getCategoryId() != null && cls.getCategoryId().equals(selectedCategoryId))
                            .toList();
                    classCombo.getItems().clear();
                    for (ComponentClassDto cls : filteredClasses) {
                        classCombo.getItems().add(cls.getName());
                        classMap.put(cls.getName(), cls);
                    }
                    if (filteredClasses.isEmpty()) {
                        classCombo.setDisable(true);
                        classCombo.setPromptText("Нет классов");
                        warningLabel.setText("⚠ Сначала создайте классы в этой категории");
                        warningLabel.setVisible(true);
                    } else {
                        classCombo.setDisable(false);
                        warningLabel.setVisible(false);
                    }
                }
            }
        });

        // Заполняем существующие значения
        if (existing != null) {
            nameField.setText(existing.getName());
            if (existing.getVendorCode() != null) vendorCodeField.setText(existing.getVendorCode());
            if (existing.getDescription() != null) descriptionField.setText(existing.getDescription());

            if (existing.getClassId() != null) {
                for (ComponentClassDto cls : allClasses) {
                    if (cls.getId().equals(existing.getClassId())) {
                        classCombo.setValue(cls.getName());
                        classMap.put(cls.getName(), cls);
                        for (ComponentCategoryDto cat : allCategories) {
                            if (cat.getId().equals(cls.getCategoryId())) {
                                categoryCombo.setValue(cat.getName());
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            if (existing.getUnitId() != null) {
                for (UnitOfMeasureDto unit : allUnits) {
                    if (unit.getId().equals(existing.getUnitId())) {
                        unitCombo.setValue(unit);
                        break;
                    }
                }
            }
        }

        // Сборка формы
        int row = 0;
        grid.add(new Label("Категория:*"), 0, row);
        grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Класс:*"), 0, row);
        grid.add(classCombo, 1, row++);
        grid.add(warningLabel, 1, row++);
        grid.add(new Label("Наименование:*"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Артикул:"), 0, row);
        grid.add(vendorCodeField, 1, row++);
        grid.add(new Label("Единица измерения:*"), 0, row);
        grid.add(unitCombo, 1, row++);
        grid.add(new Label("Описание:"), 0, row);
        grid.add(descriptionField, 1, row);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String selectedClass = classCombo.getValue();
                String name = nameField.getText().trim();
                String vendorCode = vendorCodeField.getText().trim();
                UnitOfMeasureDto selectedUnit = unitCombo.getValue();
                String description = descriptionField.getText().trim();

                if (selectedClass == null || selectedClass.isEmpty()) {
                    showAlert("Ошибка", "Выберите класс");
                    return;
                }
                if (name.isEmpty()) {
                    showAlert("Ошибка", "Введите наименование");
                    return;
                }
                if (selectedUnit == null) {
                    showAlert("Ошибка", "Выберите единицу измерения");
                    return;
                }

                ComponentClassDto selectedClassDto = classMap.get(selectedClass);
                if (selectedClassDto == null) {
                    showAlert("Ошибка", "Класс не найден");
                    return;
                }

                CreateComponentRequest request = new CreateComponentRequest(
                        selectedClassDto.getId(), name, vendorCode, selectedUnit.getId(), description);

                new Thread(() -> {
                    try {
                        if (existing == null) {
                            ComponentClient.createComponent(request);
                        } else {
                            ComponentClient.updateComponent(existing.getId(), request);
                        }
                        Platform.runLater(() -> {
                            showAlert("Успешно", "Компонент " + (existing == null ? "создан" : "обновлён"));
                            loadData();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Не удалось сохранить: " + e.getMessage()));
                    }
                }).start();
            }
        });
    }

    private void addChildCategoriesToCombo(ComboBox<String> combo, ComponentCategoryDto parent, int depth, Map<String, Long> idMap) {
        String indent = "    ".repeat(depth + 1);
        List<ComponentCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(parent.getId()))
                .toList();
        for (ComponentCategoryDto child : children) {
            String display = indent + child.getName();
            combo.getItems().add(display);
            idMap.put(display, child.getId());
            addChildCategoriesToCombo(combo, child, depth + 1, idMap);
        }
    }

    // ==========================================
    // ОБРАБОТЧИКИ СОБЫТИЙ
    // ==========================================

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadData();
        } else {
            performSearch(searchText);
        }
    }

    private void performSearch(String searchText) {
        new Thread(() -> {
            try {
                List<ComponentDto> allComponents = ComponentClient.getAllComponents();
                List<ComponentDto> filtered = allComponents.stream()
                        .filter(c -> c.getName().toLowerCase().contains(searchText) ||
                                (c.getVendorCode() != null && c.getVendorCode().toLowerCase().contains(searchText)))
                        .collect(Collectors.toList());
                updateItemList(filtered);
                Platform.runLater(() -> getStatusLabel().setText("Найдено: " + filtered.size()));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Ошибка поиска: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadData();
    }

    @FXML
    private void handleCreateCategory() {
        dialogHelper.showCategoryDialog(null, result -> new Thread(() -> {
            try {
                ComponentCategoryClient.createCategory(result.name(), result.parentId(), result.description());
                Platform.runLater(() -> {
                    showAlert("Успешно", "Категория создана");
                    loadData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось создать категорию: " + e.getMessage()));
            }
        }).start());
    }

    @FXML
    private void handleCreateClass() {
        dialogHelper.showClassDialog(null, result -> new Thread(() -> {
            try {
                createClass(result.categoryId(), result.name(), result.description(), null);
                Platform.runLater(() -> {
                    showAlert("Успешно", "Класс создан");
                    loadData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось создать класс: " + e.getMessage()));
            }
        }).start());
    }

    @FXML
    private void handleCreate() {
        showCreateItemDialog();
    }

    @FXML
    private void handleEdit() {
        ComponentDto selected = getTableView().getSelectionModel().getSelectedItem();
        if (selected != null) showEditItemDialog(selected);
        else showAlert("Внимание", "Выберите компонент для редактирования");
    }

    @FXML
    private void handleDelete() {
        ComponentDto selected = getTableView().getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите компонент для удаления");
            return;
        }
        deleteItem(selected.getId(), selected.getName());
    }
}
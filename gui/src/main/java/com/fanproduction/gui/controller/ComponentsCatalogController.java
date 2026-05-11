package com.fanproduction.gui.controller;

import com.fanproduction.gui.base.BaseCatalogController;
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
                if (selected != null) showItemDialog(selected);
            }
        });
    }

    private void updateButtonsState(ComponentDto selected) {
        boolean hasSelection = selected != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    private List<Long> getAllCategoryIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        List<ComponentCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(categoryId))
                .toList();
        for (ComponentCategoryDto child : children) {
            ids.addAll(getAllCategoryIds(child.getId()));
        }
        return ids;
    }

    // ==========================================
    // РЕАЛИЗАЦИЯ АБСТРАКТНЫХ МЕТОДОВ
    // ==========================================

    @Override
    protected void loadAllItems() {
        new Thread(() -> {
            try {
                List<ComponentDto> components = ComponentClient.getAllComponents();
                updateItemList(components);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить компоненты: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        }).start();
    }

    @Override
    protected List<ComponentDto> fetchAllItems() throws Exception {
        return ComponentClient.getAllComponents();
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

    @Override
    protected void filterByClassId(Long classId) {
        if (classId == null) {
            loadAllItems();
            return;
        }
        new Thread(() -> {
            try {
                List<ComponentDto> allComponents = ComponentClient.getAllComponents();
                List<ComponentDto> filtered = allComponents.stream()
                        .filter(c -> c.getClassId().equals(classId))
                        .collect(Collectors.toList());
                updateItemList(filtered);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Ошибка фильтрации: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        }).start();
    }

    @Override
    protected void filterByCategoryId(Long categoryId) {
        if (categoryId == null) {
            loadAllItems();
            return;
        }
        List<Long> categoryIds = getAllCategoryIds(categoryId);
        List<Long> classIds = allClasses.stream()
                .filter(cls -> cls.getCategoryId() != null && categoryIds.contains(cls.getCategoryId()))
                .map(ComponentClassDto::getId)
                .toList();

        if (classIds.isEmpty()) {
            updateItemList(new ArrayList<>());
            return;
        }

        new Thread(() -> {
            try {
                List<ComponentDto> allComponents = ComponentClient.getAllComponents();
                List<ComponentDto> filtered = allComponents.stream()
                        .filter(c -> classIds.contains(c.getClassId()))
                        .collect(Collectors.toList());
                updateItemList(filtered);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Ошибка фильтрации: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        }).start();
    }

    @Override
    protected void showCreateCategoryDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Создание категории компонентов");
        dialog.setHeaderText("Создание новой категории");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> parentCombo = new ComboBox<>();
        parentCombo.getItems().add("— Корневая категория —");
        for (ComponentCategoryDto rootCat : allCategories.stream()
                .filter(c -> c.getParentId() == null).toList()) {
            parentCombo.getItems().add(rootCat.getName());
            addChildCategoriesToParentComboSimple(parentCombo, rootCat, 1);
        }
        parentCombo.setValue("— Корневая категория —");

        TextField nameField = new TextField();
        nameField.setPromptText("Например: Кронштейны");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание (необязательно)");
        descriptionField.setPrefRowCount(3);

        grid.add(new Label("Родительская категория:"), 0, 0);
        grid.add(parentCombo, 1, 0);
        grid.add(new Label("Название категории:*"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Описание:"), 0, 2);
        grid.add(descriptionField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showAlert("Ошибка", "Введите название категории", Alert.AlertType.ERROR);
                    return;
                }
                String parentName = parentCombo.getValue();
                Long parentId = null;
                if (!"— Корневая категория —".equals(parentName) && parentName != null) {
                    String cleanName = parentName.replaceAll("^\\s+", "");
                    for (ComponentCategoryDto cat : allCategories) {
                        if (cat.getName().equals(cleanName)) {
                            parentId = cat.getId();
                            break;
                        }
                    }
                }
                final Long finalParentId = parentId;
                new Thread(() -> {
                    try {
                        ComponentCategoryClient.createCategory(name, finalParentId, descriptionField.getText());
                        Platform.runLater(() -> {
                            showAlert("Успешно", "Категория создана", Alert.AlertType.INFORMATION);
                            loadData();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Не удалось создать категорию: " + e.getMessage(),
                                Alert.AlertType.ERROR));
                    }
                }).start();
            }
        });
    }

    @Override
    protected void showCreateClassDialog() {
        showCreateClassDialogCommon();
    }

    @Override
    protected void createClass(Long categoryId, String name, String description, Long unitId) throws Exception {
        ComponentClassClient.createClass(categoryId, name, description, unitId);
    }

    @Override
    protected void showItemDialog(ComponentDto existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Создание компонента" : "Редактирование компонента");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().add("— Все категории —");
        Map<String, Long> categoryIdMap = new HashMap<>();
        for (ComponentCategoryDto rootCat : allCategories.stream()
                .filter(c -> c.getParentId() == null).toList()) {
            categoryCombo.getItems().add(rootCat.getName());
            categoryIdMap.put(rootCat.getName(), rootCat.getId());
            addChildCategoriesToParentComboWithMap(categoryCombo, rootCat, 1, categoryIdMap);
        }
        categoryCombo.setValue("— Все категории —");

        ComboBox<String> classCombo = new ComboBox<>();
        classCombo.setPromptText("Выберите класс");
        classCombo.setDisable(true);
        Map<String, ComponentClassDto> classMap = new HashMap<>();

        Label warningLabel = new Label();
        warningLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        warningLabel.setVisible(false);

        TextField nameField = new TextField();
        nameField.setPromptText("Наименование компонента");
        TextField vendorCodeField = new TextField();
        vendorCodeField.setPromptText("Артикул производителя");

        GroupedComboBox<UnitOfMeasureDto> unitCombo = new GroupedComboBox<>();
        Map<String, List<UnitOfMeasureDto>> groupedUnits = allUnits.stream()
                .collect(Collectors.groupingBy(UnitOfMeasureDto::getCategory));
        unitCombo.setGroupedItems(groupedUnits);
        unitCombo.setPromptText("Выберите единицу измерения");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание");
        descriptionField.setPrefRowCount(3);

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
        grid.add(descriptionField, 1, row++);

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
                    showAlert("Ошибка", "Выберите класс", Alert.AlertType.ERROR);
                    return;
                }
                if (name.isEmpty()) {
                    showAlert("Ошибка", "Введите наименование", Alert.AlertType.ERROR);
                    return;
                }
                if (selectedUnit == null) {
                    showAlert("Ошибка", "Выберите единицу измерения", Alert.AlertType.ERROR);
                    return;
                }

                ComponentClassDto selectedClassDto = classMap.get(selectedClass);
                if (selectedClassDto == null) {
                    showAlert("Ошибка", "Класс не найден", Alert.AlertType.ERROR);
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
                            showAlert("Успешно", "Компонент " + (existing == null ? "создан" : "обновлён"),
                                    Alert.AlertType.INFORMATION);
                            loadData();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Не удалось сохранить: " + e.getMessage(),
                                Alert.AlertType.ERROR));
                    }
                }).start();
            }
        });
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
    protected String getCategoryUpdatePath() {
        return "/components/categories/";
    }

    @Override
    protected String getClassUpdatePath() {
        return "/components/classes/";
    }

    @Override
    protected List<ComponentCategoryDto> fetchCategories() throws Exception {
        return ComponentCategoryClient.getAllCategories();
    }

    @Override
    protected List<ComponentClassDto> fetchClasses() throws Exception {
        return ComponentClassClient.getAllClasses();
    }

    @Override
    protected String getItemTypeName() {
        return "компоненты";
    }

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
    protected String getClassDescription(ComponentClassDto cls) {
        return cls.getDescription();
    }

    @Override
    protected String getCategoryDescription(ComponentCategoryDto category) {
        return category.getDescription();
    }

    @Override
    protected void updateCategory(Long id, String newName, Long parentId, String description) {
        new Thread(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("name", newName);
                if (parentId != null) {
                    request.put("parentId", parentId);
                }
                if (description != null) {
                    request.put("description", description);
                }
                ApiClient.put("/components/categories/" + id, request, new TypeReference<ApiResponse<Void>>() {});
                Platform.runLater(this::loadData);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", e.getMessage(), Alert.AlertType.ERROR));
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
                if (description != null) {
                    request.put("description", description);
                }
                ApiClient.put("/components/classes/" + id, request, new TypeReference<ApiResponse<Void>>() {});
                Platform.runLater(this::loadData);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    // ==========================================
    // ОБРАБОТЧИКИ СОБЫТИЙ
    // ==========================================

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadAllItems();
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
                Platform.runLater(() -> showAlert("Ошибка", "Ошибка поиска: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadData();
    }

    @FXML
    private void handleCreateCategory() { showCreateCategoryDialog(); }

    @FXML
    private void handleCreateClass() { showCreateClassDialog(); }

    @FXML
    private void handleCreate() { showItemDialog(null); }

    @FXML
    private void handleEdit() {
        ComponentDto selected = getTableView().getSelectionModel().getSelectedItem();
        if (selected != null) showItemDialog(selected);
        else showAlert("Внимание", "Выберите компонент для редактирования", Alert.AlertType.WARNING);
    }

    @FXML
    private void handleDelete() {
        ComponentDto selected = getTableView().getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите компонент для удаления", Alert.AlertType.WARNING);
            return;
        }
        deleteItem(selected.getId(), selected.getName());
    }

    @FXML
    protected void handleExportToExcel() {
        super.handleExportToExcel();
    }

}
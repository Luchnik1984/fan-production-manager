package com.fanproduction.gui.controller;

import com.fanproduction.gui.base.BaseCatalogController;
import com.fanproduction.gui.base.CategoryTreeItem;
import com.fanproduction.gui.client.*;
import com.fanproduction.gui.component.GroupedComboBox;
import com.fanproduction.gui.dto.request.CreateMaterialRequest;
import com.fanproduction.gui.dto.response.*;
import com.fanproduction.gui.util.TooltipUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.*;
import java.util.stream.Collectors;

public class MaterialsCatalogController extends BaseCatalogController<MaterialDto, MaterialCategoryDto, MaterialClassDto> {

    @FXML private TextField searchField;
    @FXML private TableView<MaterialDto> materialsTable;
    @FXML private TableColumn<MaterialDto, String> nameColumn;
    @FXML private TableColumn<MaterialDto, String> classNameColumn;
    @FXML private TableColumn<MaterialDto, String> unitColumn;
    @FXML private TableColumn<MaterialDto, String> standardColumn;
    @FXML private TableColumn<MaterialDto, String> specificationColumn;
    @FXML private TableColumn<MaterialDto, String> materialTypeColumn;
    @FXML private TableColumn<MaterialDto, String> vendorCodeColumn;
    @FXML private TableColumn<MaterialDto, String> descriptionColumn;
    @FXML private Label statusLabel;
    @FXML private Button createCategoryButton;
    @FXML private Button createClassButton;
    @FXML private Button createButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
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

        unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getUnitCode() != null ? cellData.getValue().getUnitCode() : ""));

        standardColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStandard() != null ? cellData.getValue().getStandard() : ""));
        standardColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        specificationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getSpecification() != null ? cellData.getValue().getSpecification() : ""));
        specificationColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        materialTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getMaterialType() != null ? cellData.getValue().getMaterialType() : ""));
        materialTypeColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        vendorCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getVendorCode() != null ? cellData.getValue().getVendorCode() : ""));
        vendorCodeColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : ""));
        descriptionColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        getTableView().setItems(itemList);
        getTableView().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                MaterialDto selected = getTableView().getSelectionModel().getSelectedItem();
                if (selected != null) showItemDialog(selected);
            }
        });
    }

    private void updateButtonsState(MaterialDto selected) {
        boolean hasSelection = selected != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    // ==========================================
    // РЕАЛИЗАЦИЯ АБСТРАКТНЫХ МЕТОДОВ
    // ==========================================

    @Override
    protected void loadAllItems() {
        new Thread(() -> {
            try {
                List<MaterialDto> materials = MaterialClient.getAllMaterials();
                updateItemList(materials);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить материалы: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        }).start();
    }

    @Override
    protected List<MaterialDto> fetchAllItems() throws Exception {
        return MaterialClient.getAllMaterials();
    }

    @Override
    protected Long getItemClassId(MaterialDto item) {
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
                List<MaterialDto> allMaterials = MaterialClient.getAllMaterials();
                List<MaterialDto> filtered = allMaterials.stream()
                        .filter(m -> m.getClassId().equals(classId))
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
        filterByCategoryIdCommon(categoryId);
    }

    @Override
    protected void showCreateCategoryDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Создание категории материалов");
        dialog.setHeaderText("Создание новой категории");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> parentCombo = new ComboBox<>();
        parentCombo.getItems().add("— Корневая категория —");
        for (MaterialCategoryDto rootCat : allCategories.stream().filter(c -> c.getParentId() == null).toList()) {
            parentCombo.getItems().add(rootCat.getName());
            addChildCategoriesToParentComboSimple(parentCombo, rootCat, 1);
        }
        parentCombo.setValue("— Корневая категория —");

        TextField nameField = new TextField();
        nameField.setPromptText("Например: Крепёж");
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
                    for (MaterialCategoryDto cat : allCategories) {
                        if (cat.getName().equals(cleanName)) {
                            parentId = cat.getId();
                            break;
                        }
                    }
                }
                final Long finalParentId = parentId;
                new Thread(() -> {
                    try {
                        MaterialCategoryClient.createCategory(name, finalParentId, descriptionField.getText());
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
        MaterialClassClient.createClass(categoryId, name, description, unitId);
    }

    @Override
    protected String getExampleClassName() {
        return "Болты с шестигранной головкой";
    }

    @Override
    protected void showItemDialog(MaterialDto existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Создание материала" : "Редактирование материала");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Категории
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().add("— Все категории —");
        Map<String, Long> categoryIdMap = new HashMap<>();
        for (MaterialCategoryDto rootCat : allCategories.stream().filter(c -> c.getParentId() == null).toList()) {
            categoryCombo.getItems().add(rootCat.getName());
            categoryIdMap.put(rootCat.getName(), rootCat.getId());
            addChildCategoriesToParentComboWithMap(categoryCombo, rootCat, 1, categoryIdMap);
        }
        categoryCombo.setValue("— Все категории —");

        // Классы
        ComboBox<String> classCombo = new ComboBox<>();
        classCombo.setPromptText("Выберите класс");
        classCombo.setDisable(true);
        Map<String, MaterialClassDto> classMap = new HashMap<>();

        // Предупреждение
        Label warningLabel = new Label();
        warningLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        warningLabel.setVisible(false);

        // Поля ввода
        TextField nameField = new TextField();
        nameField.setPromptText("Наименование материала");

        TextField standardField = new TextField();
        standardField.setPromptText("ГОСТ/ТУ (необязательно)");

        TextField specificationField = new TextField();
        specificationField.setPromptText("Тех. параметры (необязательно)");

        TextField materialTypeField = new TextField();
        materialTypeField.setPromptText("Тип материала (крепёж, металл...)");

        TextField vendorCodeField = new TextField();
        vendorCodeField.setPromptText("Артикул (необязательно)");

        TextField densityField = new TextField();
        densityField.setPromptText("Плотность (кг/м³) (необязательно)");

        // Единица измерения
        GroupedComboBox<UnitOfMeasureDto> unitCombo = new GroupedComboBox<>();
        Map<String, List<UnitOfMeasureDto>> groupedUnits = allUnits.stream()
                .collect(Collectors.groupingBy(UnitOfMeasureDto::getCategory));
        unitCombo.setGroupedItems(groupedUnits);
        unitCombo.setPromptText("Выберите единицу измерения");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание");
        descriptionField.setPrefRowCount(3);

        // Зависимость категория → классы
        categoryCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || "— Все категории —".equals(newVal)) {
                classCombo.setDisable(true);
                classCombo.getItems().clear();
                warningLabel.setVisible(false);
            } else {
                Long selectedCategoryId = categoryIdMap.get(newVal);
                if (selectedCategoryId != null) {
                    classMap.clear();
                    List<MaterialClassDto> filteredClasses = allClasses.stream()
                            .filter(cls -> cls.getCategoryId() != null && cls.getCategoryId().equals(selectedCategoryId))
                            .toList();
                    classCombo.getItems().clear();
                    for (MaterialClassDto cls : filteredClasses) {
                        classCombo.getItems().add(cls.getName());
                        classMap.put(cls.getName(), cls);
                    }
                    if (filteredClasses.isEmpty()) {
                        classCombo.setDisable(true);
                        classCombo.setPromptText("Нет классов");
                        warningLabel.setText("⚠️ Сначала создайте классы в этой категории");
                        warningLabel.setVisible(true);
                    } else {
                        classCombo.setDisable(false);
                        warningLabel.setVisible(false);
                    }
                }
            }
        });

        // Заполнение при редактировании
        if (existing != null) {
            nameField.setText(existing.getName());
            if (existing.getStandard() != null) standardField.setText(existing.getStandard());
            if (existing.getSpecification() != null) specificationField.setText(existing.getSpecification());
            if (existing.getMaterialType() != null) materialTypeField.setText(existing.getMaterialType());
            if (existing.getVendorCode() != null) vendorCodeField.setText(existing.getVendorCode());
            if (existing.getDensity() != null) densityField.setText(String.valueOf(existing.getDensity()));
            if (existing.getDescription() != null) descriptionField.setText(existing.getDescription());

            if (existing.getClassId() != null) {
                for (MaterialClassDto cls : allClasses) {
                    if (cls.getId().equals(existing.getClassId())) {
                        classCombo.setValue(cls.getName());
                        for (MaterialCategoryDto cat : allCategories) {
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

        // Сборка сетки
        int row = 0;
        grid.add(new Label("Категория:*"), 0, row);
        grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Класс:*"), 0, row);
        grid.add(classCombo, 1, row++);
        grid.add(warningLabel, 1, row++);
        grid.add(new Label("Наименование:*"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("ГОСТ/ТУ:"), 0, row);
        grid.add(standardField, 1, row++);
        grid.add(new Label("Тех. параметры:"), 0, row);
        grid.add(specificationField, 1, row++);
        grid.add(new Label("Тип материала:"), 0, row);
        grid.add(materialTypeField, 1, row++);
        grid.add(new Label("Артикул:"), 0, row);
        grid.add(vendorCodeField, 1, row++);
        grid.add(new Label("Единица измерения:*"), 0, row);
        grid.add(unitCombo, 1, row++);
        grid.add(new Label("Плотность (кг/м³):"), 0, row);
        grid.add(densityField, 1, row++);
        grid.add(new Label("Описание:"), 0, row);
        grid.add(descriptionField, 1, row);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String selectedClass = classCombo.getValue();
                String name = nameField.getText().trim();
                String standard = standardField.getText().trim();
                String specification = specificationField.getText().trim();
                String materialType = materialTypeField.getText().trim();
                String vendorCode = vendorCodeField.getText().trim();
                Double density = null;
                try {
                    if (!densityField.getText().trim().isEmpty()) {
                        density = Double.parseDouble(densityField.getText().trim());
                    }
                } catch (NumberFormatException ignored) {}
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

                MaterialClassDto selectedClassDto = classMap.get(selectedClass);
                if (selectedClassDto == null) {
                    showAlert("Ошибка", "Класс не найден", Alert.AlertType.ERROR);
                    return;
                }

                CreateMaterialRequest request = new CreateMaterialRequest(
                        selectedClassDto.getId(), name, standard, specification, materialType,
                        selectedUnit.getId(), density, vendorCode, null, description);

                new Thread(() -> {
                    try {
                        if (existing == null) {
                            MaterialClient.createMaterial(request);
                        } else {
                            MaterialClient.updateMaterial(existing.getId(), request);
                        }
                        Platform.runLater(() -> {
                            showAlert("Успешно", "Материал " + (existing == null ? "создан" : "обновлён"),
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

    // ==========================================
    // РЕАЛИЗАЦИЯ АБСТРАКТНЫХ МЕТОДОВ БАЗОВОГО КЛАССА
    // ==========================================

    @Override
    protected List<MaterialCategoryDto> fetchCategories() throws Exception {
        return MaterialCategoryClient.getAllCategories();
    }

    @Override
    protected List<MaterialClassDto> fetchClasses() throws Exception {
        return MaterialClassClient.getAllClasses();
    }

    @Override
    protected String getItemTypeName() {
        return "материалы";
    }

    @Override
    protected Long getCategoryId(MaterialCategoryDto category) {
        return category.getId();
    }

    @Override
    protected String getCategoryName(MaterialCategoryDto category) {
        return category.getName();
    }

    @Override
    protected Long getCategoryParentId(MaterialCategoryDto category) {
        return category.getParentId();
    }

    @Override
    protected Long getClassId(MaterialClassDto cls) {
        return cls.getId();
    }

    @Override
    protected String getClassName(MaterialClassDto cls) {
        return cls.getName();
    }

    @Override
    protected Long getClassCategoryId(MaterialClassDto cls) {
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
    protected TableView<MaterialDto> getTableView() {
        return materialsTable;
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
                List<MaterialDto> allMaterials = MaterialClient.getAllMaterials();
                List<MaterialDto> filtered = allMaterials.stream()
                        .filter(m -> m.getName().toLowerCase().contains(searchText) ||
                                (m.getStandard() != null && m.getStandard().toLowerCase().contains(searchText)) ||
                                (m.getVendorCode() != null && m.getVendorCode().toLowerCase().contains(searchText)))
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
        MaterialDto selected = getTableView().getSelectionModel().getSelectedItem();
        if (selected != null) showItemDialog(selected);
        else showAlert("Внимание", "Выберите материал для редактирования", Alert.AlertType.WARNING);
    }

    @FXML
    private void handleDelete() {
        MaterialDto selected = getTableView().getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите материал для удаления", Alert.AlertType.WARNING);
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setContentText("Удалить материал \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        MaterialClient.deleteMaterial(selected.getId());
                        Platform.runLater(() -> {
                            showAlert("Успешно", "Материал удалён", Alert.AlertType.INFORMATION);
                            loadData();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Не удалось удалить: " + e.getMessage(),
                                Alert.AlertType.ERROR));
                    }
                }).start();
            }
        });
    }
}
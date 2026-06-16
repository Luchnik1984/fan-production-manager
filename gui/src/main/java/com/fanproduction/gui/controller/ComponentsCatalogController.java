package com.fanproduction.gui.controller;

import com.fanproduction.gui.base.BaseCatalogController;
import com.fanproduction.gui.base.CatalogHelper;
import com.fanproduction.gui.base.CategoryTreeItem;
import com.fanproduction.gui.base.ExportRowDto;
import com.fanproduction.gui.client.*;
import com.fanproduction.gui.component.GroupedComboBox;
import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.component.TechnicalSpecsEditor;
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
    @FXML private TableColumn<ComponentDto, String> weightColumn;
    @FXML private TableColumn<ComponentDto, String> materialColumn;
    @FXML private TableColumn<ComponentDto, String> descriptionColumn;
    @FXML private Label statusLabel;
    @FXML private Button createCategoryButton;
    @FXML private Button createClassButton;
    @FXML private Button createButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button exportButton;
    @FXML private TreeView<CategoryTreeItem> categoryTreeView;

    // ========== RECORD ДЛЯ ПОЛЕЙ ФОРМЫ ==========
    private record ComponentFormFields(
            TextField name,
            TextField designation,
            TextField vendorCode,
            GroupedComboBox<UnitOfMeasureDto> unit,
            TextArea description,
            TextField weightKg,
            TextField material,
            TechnicalSpecsEditor technicalSpecs
    ) {}

    @FXML
    private void initialize() {
        setupTable();
        loadData();
        getTableView().getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> updateButtonsState(newVal));
    }

    @Override
    protected void setupTable() {

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDisplayName()));
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

        weightColumn.setCellValueFactory(cellData -> {
            Double weight = cellData.getValue().getWeightKg();
            return new SimpleStringProperty(weight != null ? String.valueOf(weight) : "");
        });

        materialColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMaterial() != null ?
                        cellData.getValue().getMaterial() : ""));

        componentsTable.setItems(itemList);

        componentsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ComponentDto selected = componentsTable.getSelectionModel().getSelectedItem();
                if (selected != null) showEditItemDialog(selected);
            }
        });
    }

    private void updateButtonsState(ComponentDto selected) {
        updateButtonsState(selected != null, editButton, deleteButton);
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
    protected List<ExportRowDto> getExportData() {
        List<ExportRowDto> data = new ArrayList<>();
        for (ComponentDto dto : itemList) {
            String displayName = dto.getDisplayName();
            List<String> values = Arrays.asList(
                    displayName,
                    dto.getClassName() != null ? dto.getClassName() : "",
                    dto.getVendorCode() != null ? dto.getVendorCode() : "",
                    dto.getUnitCode() != null ? dto.getUnitCode() : "",
                    dto.getWeightKg() != null ? String.valueOf(dto.getWeightKg()) : "",
                    dto.getMaterial() != null ? dto.getMaterial() : "",
                    dto.getDescription() != null ? dto.getDescription() : ""
            );
            data.add(new ExportRowDto(values));
        }
        return data;
    }

    @Override
    protected String[] getExportHeaders() {
        return new String[]{"Наименование", "Класс", "Артикул", "Ед. изм.","Масса (кг)", "Материал", "Описание"};
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
        return item.getDisplayName();
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



    // ========== ОСНОВНОЙ МЕТОД ==========

    private void showComponentDialog(ComponentDto existing) {
        String title = existing == null ? "Создание компонента" : "Редактирование компонента";
        Dialog<ButtonType> dialog = createBaseDialog(title);

        TabPane tabPane = createBaseTabPane();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Tab mainTab = createMainTab("Основные поля", grid);

        // Выбор категории
        ComboBox<String> categoryCombo = createCategoryCombo();
        ComboBox<String> classCombo = createClassCombo();
        ComponentFormFields formFields = createComponentFormFields();

        Label warningLabel = new Label();
        warningLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        warningLabel.setVisible(false);

        // Настраиваем зависимость категория → класс
        setupCategoryClassDependency(categoryCombo, classCombo, warningLabel);

        // Заполняем поля при редактировании
        Map<String, ComponentClassDto> classMap = new HashMap<>();
        if (existing != null) {
            loadExistingComponentData(existing, formFields, categoryCombo, classCombo, classMap);
        }

        // Сборка формы (основные поля)
        int row = 0;
        grid.add(new Label("Категория:*"), 0, row);
        grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Класс:*"), 0, row);
        grid.add(classCombo, 1, row++);
        grid.add(warningLabel, 1, row++);
        grid.add(new Label("Наименование:*"), 0, row);
        grid.add(formFields.name(), 1, row++);
        grid.add(new Label("Обозначение:"), 0, row);
        grid.add(formFields.designation(), 1, row++);
        grid.add(new Label("Артикул:"), 0, row);
        grid.add(formFields.vendorCode(), 1, row++);
        grid.add(new Label("Единица измерения:*"), 0, row);
        grid.add(formFields.unit(), 1, row++);
        grid.add(new Label("Масса (кг):"), 0, row);
        grid.add(formFields.weightKg(), 1, row++);
        grid.add(new Label("Материал:"), 0, row);
        grid.add(formFields.material(), 1, row++);
        grid.add(new Label("Описание:"), 0, row);
        grid.add(formFields.description(), 1, row);

        TechnicalSpecsEditor technicalSpecsEditor = new TechnicalSpecsEditor(allUnits);
        if (existing != null && existing.getTechnicalSpecs() != null) {
            technicalSpecsEditor.setTechnicalSpecs(existing.getTechnicalSpecs());
        }

        setupDialogWithTabs(dialog, tabPane, mainTab, grid, technicalSpecsEditor,
                () -> collectAndSaveComponent(existing, formFields, classCombo, classMap, technicalSpecsEditor)
        );

        dialog.getDialogPane().setContent(tabPane);
        dialog.showAndWait();
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private ComponentFormFields createComponentFormFields() {
        TextField nameField = new TextField();
        nameField.setPromptText("Наименование компонента");

        TextField designationField = new TextField();
        designationField.setPromptText("Обозначение (например: SM1210)");

        TextField vendorCodeField = new TextField();
        vendorCodeField.setPromptText("Артикул производителя");

        GroupedComboBox<UnitOfMeasureDto> unitCombo = CatalogHelper.createUnitCombo(allUnits);

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание");
        descriptionField.setPrefRowCount(3);

        TextField weightKgField = new TextField();
        weightKgField.setPromptText("Масса (кг)");

        TextField materialField = new TextField();
        materialField.setPromptText("Материал");

        TechnicalSpecsEditor technicalSpecsEditor = createTechnicalSpecsEditor();

        return new ComponentFormFields(
                nameField,
                designationField,
                vendorCodeField,
                unitCombo,
                descriptionField,
                weightKgField,
                materialField,
                technicalSpecsEditor);
    }


    @Override
    protected void updateClassComboForCategory(Long categoryId,
                                             ComboBox<String> classCombo,
                                             Label warningLabel) {
        List<ComponentClassDto> filteredClasses = allClasses.stream()
                .filter(cls -> cls.getCategoryId() != null && cls.getCategoryId().equals(categoryId))
                .toList();

        classMap.clear();
        classCombo.getItems().clear();

        for (ComponentClassDto cls : filteredClasses) {
            classCombo.getItems().add(cls.getName());
            classMap.put(cls.getName(), cls);
        }

        if (filteredClasses.isEmpty()) {
            classCombo.setDisable(true);
            classCombo.setPromptText("Нет классов");
            warningLabel.setGraphic(IconFactory.createWarningIcon());
            warningLabel.setText(" Сначала создайте классы в этой категории");
        } else {
            classCombo.setDisable(false);
            warningLabel.setVisible(false);
        }
    }

    private void loadExistingComponentData(ComponentDto existing,
                                           ComponentFormFields formFields,
                                           ComboBox<String> categoryCombo,
                                           ComboBox<String> classCombo,
                                           Map<String, ComponentClassDto> classMap) {
        // Заполнение текстовых полей
        fillTextField(formFields.name(), existing.getName());
        fillTextField(formFields.designation(), existing.getDesignation());
        fillTextField(formFields.vendorCode(), existing.getVendorCode());

        if (existing.getWeightKg() != null) {
            formFields.weightKg().setText(String.valueOf(existing.getWeightKg()));
        }
        if (existing.getMaterial() != null) {
            formFields.material().setText(existing.getMaterial());
        }

        // Заполнение TextArea (отдельно)
        if (existing.getDescription() != null) {
            formFields.description().setText(existing.getDescription());
        }

        // ========== ТЕХНИЧЕСКИЕ ХАРАКТЕРИСТИКИ ==========
        if (existing.getTechnicalSpecs() != null) {
            formFields.technicalSpecs().setTechnicalSpecs(existing.getTechnicalSpecs());
        }

        // Восстановление выбранных значений (категория, класс, единица измерения)
        restoreCategoryAndClass(existing, categoryCombo, classCombo, classMap);
        restoreUnit(existing, formFields.unit());
    }

    private void restoreCategoryAndClass(ComponentDto existing,
                                         ComboBox<String> categoryCombo,
                                         ComboBox<String> classCombo,
                                         Map<String, ComponentClassDto> classMap) {
        if (existing.getClassId() == null) return;

        for (ComponentClassDto cls : allClasses) {
            if (!cls.getId().equals(existing.getClassId())) continue;

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

    private void restoreUnit(ComponentDto existing, GroupedComboBox<UnitOfMeasureDto> unitCombo) {
        if (existing.getUnitId() == null) return;

        for (UnitOfMeasureDto unit : allUnits) {
            if (unit.getId().equals(existing.getUnitId())) {
                unitCombo.setValue(unit);
                break;
            }
        }
    }

    private void collectAndSaveComponent(ComponentDto existing,
                                         ComponentFormFields formFields,
                                         ComboBox<String> classCombo,
                                         Map<String, ComponentClassDto> classMap,
                                         TechnicalSpecsEditor technicalSpecsEditor) {
        // ========== СБОР ДАННЫХ ИЗ ФОРМЫ ==========
        String selectedClass = classCombo.getValue();
        String name = formFields.name().getText().trim();
        String designation = formFields.designation().getText().trim();
        String vendorCode = formFields.vendorCode().getText().trim();
        UnitOfMeasureDto selectedUnit = formFields.unit().getValue();
        String description = formFields.description().getText().trim();
        Double weightKg = parseDouble(formFields.weightKg().getText().trim());
        String material = formFields.material().getText().trim();
        if (material.isEmpty()) material = null;
        Map<String, Object> technicalSpecs = technicalSpecsEditor.getTechnicalSpecs();

        // Если характеристик нет, отправляем пустой объект (а не null)
        if (technicalSpecs == null) {
            technicalSpecs = new HashMap<>();
        }

        // ========== ВАЛИДАЦИЯ ==========
        if (selectedClass == null || selectedClass.isEmpty()) {
            showAlert("Ошибка", "Выберите класс");
            return;
        }
        if (name.isEmpty()) {
            showAlert("Ошибка", "Введите наименование");
            return;
        }
        if (designation.isEmpty()) {
            showAlert("Ошибка", "Введите обозначение");
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

        // ========== СОЗДАНИЕ ЗАПРОСА ==========
        CreateComponentRequest request = new CreateComponentRequest(
                selectedClassDto.getId(),
                name,
                designation,
                vendorCode,
                selectedUnit.getId(),
                weightKg,
                material,
                description,
                technicalSpecs
                );

        // ========== ОТПРАВКА НА СЕРВЕР ==========
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
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось сохранить: " + e.getMessage()));
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

    @Override
    protected void createCategory(String name, Long parentId, String description) throws Exception {
        ComponentCategoryClient.createCategory(name, parentId, description);
    }

    @Override
    protected void createClass(Long categoryId, String name, String description, Long unitId) throws Exception {
        ComponentClassClient.createClass(categoryId, name, description, unitId);
    }

    @FXML
    private void handleCreate() {
        showCreateItemDialog();
    }

    @FXML
    private void handleEdit() {
        ComponentDto selected = getTableView().getSelectionModel().getSelectedItem();
        if (selected != null) showEditItemDialog(selected);
        else showAlert("Внимание", "Выберите компонент для редактирования", Alert.AlertType.CONFIRMATION);
    }

    @FXML
    private void handleDelete() {
        ComponentDto selected = getTableView().getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите компонент для удаления", Alert.AlertType.CONFIRMATION);
            return;
        }
        deleteItem(selected.getId(), selected.getName());
    }

    @Override
    protected List<UnitOfMeasureDto> fetchUnits() throws Exception {
        return ComponentClient.getAllUnits();
    }

    /**
     * Устанавливает предзагруженные данные, чтобы избежать повторной загрузки
     */
    public void setPreloadedData(List<ComponentCategoryDto> categories,
                                 List<ComponentClassDto> classes,
                                 List<ComponentDto> components) {
        this.allCategories = categories;
        this.allClasses = classes;
        this.itemList.setAll(components);

        // Обновляем UI
        Platform.runLater(() -> {
            getStatusLabel().setText("Всего: " + components.size());
            refreshTree();
        });
    }

}
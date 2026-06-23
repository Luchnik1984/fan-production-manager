package com.fanproduction.gui.controller;

import com.fanproduction.gui.base.*;
import com.fanproduction.gui.client.*;
import com.fanproduction.gui.component.GroupedComboBox;
import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.component.TechnicalSpecsEditor;
import com.fanproduction.gui.dto.request.CreateMaterialRequest;
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
    @FXML private Button exportButton;
    @FXML private TreeView<CategoryTreeItem> categoryTreeView;


    // ========== RECORD ДЛЯ ПОЛЕЙ ФОРМЫ ==========
    private record MaterialFormFields(
            TextField name,
            TextField designation,
            TextField standard,
            TextField specification,
            TextField materialType,
            TextField vendorCode,
            TextField density,
            GroupedComboBox<UnitOfMeasureDto> unit,
            TextArea description,
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

        materialsTable.setItems(itemList);

        materialsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                MaterialDto selected = materialsTable.getSelectionModel().getSelectedItem();
                if (selected != null) showEditItemDialog(selected);
            }
        });
    }

    private void updateButtonsState(MaterialDto selected) {
        updateButtonsState(selected != null, editButton, deleteButton);
    }

    // ==========================================
    // РЕАЛИЗАЦИЯ АБСТРАКТНЫХ МЕТОДОВ
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
    protected List<MaterialDto> fetchAllItems() throws Exception {
        return MaterialClient.getAllMaterials();
    }

    @Override
    protected void deleteCategoryById(Long id) throws Exception {
        MaterialCategoryClient.deleteCategory(id);
    }

    @Override
    protected void deleteClassById(Long id) throws Exception {
        MaterialClassClient.deleteClass(id);
    }

    @Override
    protected void deleteItemById(Long id) throws Exception {
        MaterialClient.deleteMaterial(id);
    }

    @Override
    protected void updateCategory(Long id, String newName, Long parentId, String description) {
        new Thread(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("name", newName);
                if (parentId != null) request.put("parentId", parentId);
                if (description != null) request.put("description", description);
                ApiClient.put("/materials/categories/" + id, request, new TypeReference<ApiResponse<Void>>() {});
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
                ApiClient.put("/materials/classes/" + id, request, new TypeReference<ApiResponse<Void>>() {});
                Platform.runLater(this::loadData);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", e.getMessage()));
            }
        }).start();
    }

    @Override
    protected List<ExportRowDto> getExportData() {
        List<ExportRowDto> data = new ArrayList<>();
        for (MaterialDto dto : itemList) {
            List<String> values = Arrays.asList(
                    dto.getDisplayName(),
                    dto.getDesignation(),
                    dto.getClassName() != null ? dto.getClassName() : "",
                    dto.getUnitCode() != null ? dto.getUnitCode() : "",
                    dto.getStandard() != null ? dto.getStandard() : "",
                    dto.getSpecification() != null ? dto.getSpecification() : "",
                    dto.getMaterialType() != null ? dto.getMaterialType() : "",
                    dto.getVendorCode() != null ? dto.getVendorCode() : "",
                    dto.getDescription() != null ? dto.getDescription() : ""
            );
            data.add(new ExportRowDto(values));
        }
        return data;
    }

    @Override
    protected String[] getExportHeaders() {
        return new String[]{"Наименование", "Обозначение", "Класс", "Ед. измер.", "ГОСТ/ТУ", "Тех. параметры", "Тип", "Артикул", "Описание"};
    }

    @Override
    protected String[][] getExportDataForItem(Object existing, Object formFields) {
        MaterialDto dto = (MaterialDto) existing;
        MaterialFormFields fields = (MaterialFormFields) formFields;

        String name = dto != null ? dto.getName() : fields.name().getText().trim();
        String designation = dto != null ? dto.getDesignation() : fields.designation().getText().trim();
        String className = dto != null ? dto.getClassName() : "";
        String unitCode = dto != null ? dto.getUnitCode() :
                (fields.unit().getValue() != null ? fields.unit().getValue().getCode() : "");
        String standard = dto != null ? (dto.getStandard() != null ? dto.getStandard() : "") : fields.standard().getText().trim();
        String specification = dto != null ? (dto.getSpecification() != null ? dto.getSpecification() : "") : fields.specification().getText().trim();
        String materialType = dto != null ? (dto.getMaterialType() != null ? dto.getMaterialType() : "") : fields.materialType().getText().trim();
        String vendorCode = dto != null ? (dto.getVendorCode() != null ? dto.getVendorCode() : "") : fields.vendorCode().getText().trim();
        String density = dto != null ? (dto.getDensity() != null ? String.valueOf(dto.getDensity()) : "") : fields.density().getText().trim();
        String description = dto != null ? (dto.getDescription() != null ? dto.getDescription() : "") :
                fields.description().getText().trim();

        return new String[][]{
                {"Наименование", name},
                {"Обозначение", designation},
                {"Класс", className},
                {"Единица измерения", unitCode},
                {"ГОСТ/ТУ", standard},
                {"Тех. параметры", specification},
                {"Тип материала", materialType},
                {"Артикул", vendorCode},
                {"Плотность (кг/м³)", density},
                {"Описание", description}
        };
    }



    @Override
    protected String getItemTypeName() {
        return "материалы";
    }

    // ==========================================
    // GETTERS ДЛЯ ПОЛЕЙ (для хелперов)
    // ==========================================

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
    protected String getCategoryDescription(MaterialCategoryDto category) {
        return category.getDescription();
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
    protected String getClassDescription(MaterialClassDto cls) {
        return cls.getDescription();
    }

    @Override
    protected Long getItemId(MaterialDto item) {
        return item.getId();
    }

    @Override
    protected String getItemName(MaterialDto item) {
        return item.getDisplayName();
    }

    @Override
    protected Long getItemClassId(MaterialDto item) {
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
    protected TableView<MaterialDto> getTableView() {
        return materialsTable;
    }

    // ==========================================
    // СОЗДАНИЕ/РЕДАКТИРОВАНИЕ ЭЛЕМЕНТОВ
    // ==========================================

    @Override
    protected void showCreateItemDialog() {
        showMaterialDialog(null);
    }

    @Override
    protected void showEditItemDialog(MaterialDto existing) {
        showMaterialDialog(existing);
    }

    // ========== ОСНОВНОЙ МЕТОД ==========
    private void showMaterialDialog(MaterialDto existing) {
        String title = existing == null ? "Создание материала" : "Редактирование материала";
        Dialog<ButtonType> dialog = createBaseDialog(title);

        // ========== ЛОКАЛЬНАЯ ПЕРЕМЕННАЯ ДЛЯ СОСТОЯНИЯ ==========
        final Long[] currentMaterialId = {existing != null ? existing.getId() : null};
        final boolean[] isEditingMode = {existing != null};

        TabPane tabPane = createBaseTabPane();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // ========== СОЗДАЁМ ВКЛАДКУ "ОСНОВНЫЕ ПОЛЯ" ==========
        Tab mainTab = new Tab("Основные поля");
        mainTab.setClosable(false);
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);
        mainTab.setContent(scrollPane);
        tabPane.getTabs().add(mainTab);

        // ========== ПОЛЯ ФОРМЫ ==========
        ComboBox<String> categoryCombo = createCategoryCombo();
        ComboBox<String> classCombo = createClassCombo();
        MaterialFormFields formFields = createMaterialFormFields();

        Label warningLabel = new Label();
        warningLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        warningLabel.setVisible(false);

        // ========== ЗАГРУЗКА ДАННЫХ ПРИ РЕДАКТИРОВАНИИ ==========
        Map<String, MaterialClassDto> classMap = this.classMap;
        if (existing != null) {
            loadExistingMaterialData(existing, formFields, categoryCombo, classCombo, classMap);
        } else if (currentMaterialId[0] != null) {
            // Если диалог переключился в режим редактирования (после создания)
            try {
                MaterialDto loaded = MaterialClient.getMaterialById(currentMaterialId[0]);
                loadExistingMaterialData(loaded, formFields, categoryCombo, classCombo, classMap);
                dialog.setTitle("Редактирование материала - " + loaded.getName());
            } catch (Exception e) {
                System.err.println("Failed to load material for editing: " + e.getMessage());
            }
        }

        // ========== НАСТРОЙКА ЗАВИСИМОСТИ КАТЕГОРИЯ → КЛАСС ==========
        setupCategoryClassDependency(categoryCombo, classCombo, warningLabel);

        // ========== СБОРКА ФОРМЫ ==========
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
        grid.add(new Label("ГОСТ/ТУ:"), 0, row);
        grid.add(formFields.standard(), 1, row++);
        grid.add(new Label("Тех. параметры:"), 0, row);
        grid.add(formFields.specification(), 1, row++);
        grid.add(new Label("Тип материала:"), 0, row);
        grid.add(formFields.materialType(), 1, row++);
        grid.add(new Label("Артикул:"), 0, row);
        grid.add(formFields.vendorCode(), 1, row++);
        grid.add(new Label("Единица измерения:*"), 0, row);
        grid.add(formFields.unit(), 1, row++);
        grid.add(new Label("Плотность (кг/м³):"), 0, row);
        grid.add(formFields.density(), 1, row++);
        grid.add(new Label("Описание:"), 0, row);
        grid.add(formFields.description(), 1, row);

        // ========== ВКЛАДКА "ТЕХНИЧЕСКИЕ ХАРАКТЕРИСТИКИ" ==========
        TechnicalSpecsEditor technicalSpecsEditor = new TechnicalSpecsEditor(allUnits);
        if (existing != null && existing.getTechnicalSpecs() != null) {
            technicalSpecsEditor.setTechnicalSpecs(existing.getTechnicalSpecs());
        } else if (currentMaterialId[0] != null) {
            try {
                MaterialDto loaded = MaterialClient.getMaterialById(currentMaterialId[0]);
                if (loaded.getTechnicalSpecs() != null) {
                    technicalSpecsEditor.setTechnicalSpecs(loaded.getTechnicalSpecs());
                }
            } catch (Exception e) {
                System.err.println("Failed to load technical specs: " + e.getMessage());
            }
        }

        Tab technicalTab = new Tab("Технические характеристики");
        technicalTab.setClosable(false);
        technicalTab.setContent(technicalSpecsEditor);
        tabPane.getTabs().add(technicalTab);

        // ========== КНОПКИ ==========
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        dialog.getDialogPane().setContent(tabPane);

        // ========== ОБРАБОТКА КНОПКИ "СОХРАНИТЬ" ==========
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            collectAndSaveMaterial(
                    existing,
                    formFields,
                    classCombo,
                    classMap,
                    technicalSpecsEditor,
                    dialog,
                    currentMaterialId,
                    isEditingMode
            );
        });

        dialog.setOnCloseRequest(e -> loadData());

        dialog.showAndWait();
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private MaterialFormFields createMaterialFormFields() {
        TextField nameField = new TextField();
        nameField.setPromptText("Наименование материала");

        TextField designationField = new TextField();
        designationField.setPromptText("Обозначение (например: 08Пс)");

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

        GroupedComboBox<UnitOfMeasureDto> unitCombo = CatalogHelper.createUnitCombo(allUnits);

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание");
        descriptionField.setPrefRowCount(3);

        TechnicalSpecsEditor technicalSpecsEditor = createTechnicalSpecsEditor();
        return new MaterialFormFields(
                nameField,
                designationField,
                standardField,
                specificationField,
                materialTypeField,
                vendorCodeField,
                densityField,
                unitCombo,
                descriptionField,
                technicalSpecsEditor
        );
    }

    @Override
    protected void updateClassComboForCategory(Long categoryId,
                                             ComboBox<String> classCombo,
                                             Label warningLabel) {
        List<MaterialClassDto> filteredClasses = allClasses.stream()
                .filter(cls -> cls.getCategoryId() != null && cls.getCategoryId().equals(categoryId))
                .toList();

        classMap.clear();
        classCombo.getItems().clear();

        for (MaterialClassDto cls : filteredClasses) {
            classCombo.getItems().add(cls.getName());
            classMap.put(cls.getName(), cls);
        }

        if (filteredClasses.isEmpty()) {
            classCombo.setDisable(true);
            classCombo.setPromptText("Нет классов");
            warningLabel.setGraphic(IconFactory.createWarningIcon());
            warningLabel.setText(" Сначала создайте классы в этой категории");
            warningLabel.setVisible(true);
        } else {
            classCombo.setDisable(false);
            warningLabel.setVisible(false);
        }
    }

    private void loadExistingMaterialData(MaterialDto existing,
                                          MaterialFormFields formFields,
                                          ComboBox<String> categoryCombo,
                                          ComboBox<String> classCombo,
                                          Map<String, MaterialClassDto> classMap) {
        // Заполнение текстовых полей
        if (existing.getName() != null) formFields.name().setText(existing.getName());
        if (existing.getDesignation() != null) formFields.designation().setText(existing.getDesignation());
        if (existing.getStandard() != null) formFields.standard().setText(existing.getStandard());
        if (existing.getSpecification() != null) formFields.specification().setText(existing.getSpecification());
        if (existing.getMaterialType() != null) formFields.materialType().setText(existing.getMaterialType());
        if (existing.getVendorCode() != null) formFields.vendorCode().setText(existing.getVendorCode());
        if (existing.getDensity() != null) formFields.density().setText(String.valueOf(existing.getDensity()));
        if (existing.getDescription() != null) formFields.description().setText(existing.getDescription());

        // Технические характеристики
        if (existing.getTechnicalSpecs() != null) {
            formFields.technicalSpecs().setTechnicalSpecs(existing.getTechnicalSpecs());
        }

        // Восстановление выбранных значений
        restoreCategoryAndClass(existing, categoryCombo, classCombo, classMap);
        restoreUnit(existing, formFields.unit());
    }

    private void restoreCategoryAndClass(MaterialDto existing,
                                         ComboBox<String> categoryCombo,
                                         ComboBox<String> classCombo,
                                         Map<String, MaterialClassDto> classMap) {
        if (existing.getClassId() == null) return;

        for (MaterialClassDto cls : allClasses) {
            if (!cls.getId().equals(existing.getClassId())) continue;

            classCombo.setValue(cls.getName());
            classMap.put(cls.getName(), cls);

            for (MaterialCategoryDto cat : allCategories) {
                if (cat.getId().equals(cls.getCategoryId())) {
                    categoryCombo.setValue(cat.getName());
                    break;
                }
            }
            break;
        }
    }

    private void restoreUnit(MaterialDto existing, GroupedComboBox<UnitOfMeasureDto> unitCombo) {
        if (existing.getUnitId() == null) return;

        for (UnitOfMeasureDto unit : allUnits) {
            if (unit.getId().equals(existing.getUnitId())) {
                unitCombo.setValue(unit);
                break;
            }
        }
    }

    private void collectAndSaveMaterial(MaterialDto existing,
                                        MaterialFormFields formFields,
                                        ComboBox<String> classCombo,
                                        Map<String, MaterialClassDto> classMap,
                                        TechnicalSpecsEditor technicalSpecsEditor,
                                        Dialog<ButtonType> dialog,
                                        Long[] currentMaterialId,
                                        boolean[] isEditingMode) {
        System.out.println("=== collectAndSaveMaterial START ===");
        System.out.println("existing: " + (existing != null ? "not null, id=" + existing.getId() : "null"));
        System.out.println("currentMaterialId[0]: " + currentMaterialId[0]);

        // ========== СБОР ДАННЫХ ==========
        String selectedClass = classCombo.getValue();
        String name = formFields.name().getText().trim();
        String designation = formFields.designation().getText().trim();
        String standard = formFields.standard().getText().trim();
        String specification = formFields.specification().getText().trim();
        String materialType = formFields.materialType().getText().trim();
        String vendorCode = formFields.vendorCode().getText().trim();
        if (vendorCode.isEmpty()) {vendorCode = null;}
        Double density = parseDouble(formFields.density().getText().trim());
        UnitOfMeasureDto selectedUnit = formFields.unit().getValue();
        String description = formFields.description().getText().trim();
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

        MaterialClassDto selectedClassDto = classMap.get(selectedClass);
        if (selectedClassDto == null) {
            showAlert("Ошибка", "Класс не найден");
            return;
        }

        // ========== ОПРЕДЕЛЯЕМ РЕЖИМ ==========
        Long materialId = existing != null ? existing.getId() : currentMaterialId[0];
        boolean isNew = materialId == null;

        System.out.println("isNew: " + isNew);

        // ========== СОЗДАНИЕ ЗАПРОСА ==========
        CreateMaterialRequest request = new CreateMaterialRequest(
                selectedClassDto.getId(),
                name,
                designation,
                standard,
                specification,
                materialType,
                selectedUnit.getId(),
                density,
                vendorCode,
                null,
                description,
                technicalSpecs
        );

        // ========== ОТПРАВКА НА СЕРВЕР ==========
        final boolean finalIsNew = isNew;
        final Long finalMaterialId = materialId;

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(
                new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE));
        if (saveButton != null) {
            saveButton.setDisable(true);
            saveButton.setText("Сохранение...");
        }

        new Thread(() -> {
            try {
                MaterialDto savedMaterial;

                if (finalIsNew) {
                    savedMaterial = MaterialClient.createMaterial(request);
                    System.out.println("✅ Material created with name: " + name);
                } else {
                    savedMaterial = MaterialClient.updateMaterial(finalMaterialId, request);
                    System.out.println("✅ Material updated, name: " + name);
                }

                final MaterialDto finalSavedMaterial = savedMaterial;

                Platform.runLater(() -> {
                    if (finalIsNew) {
                        currentMaterialId[0] = finalSavedMaterial.getId();
                        isEditingMode[0] = true;
                        dialog.setTitle("Редактирование материала - " + finalSavedMaterial.getName());
                    }

                    if (saveButton != null) {
                        saveButton.setDisable(false);
                        saveButton.setText("Сохранить");
                    }

                    showAlert("Успешно", "Материал " + (finalIsNew ? "создан" : "обновлён"),
                            Alert.AlertType.INFORMATION);
                    loadData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (saveButton != null) {
                        saveButton.setDisable(false);
                        saveButton.setText("Сохранить");
                    }
                    showAlert("Ошибка", "Не удалось сохранить: " + e.getMessage());
                    System.err.println("Error saving: " + e.getMessage());
                });
                e.printStackTrace();
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
                List<MaterialDto> allMaterials = MaterialClient.getAllMaterials();
                List<MaterialDto> filtered = allMaterials.stream()
                        .filter(m -> m.getName().toLowerCase().contains(searchText) ||
                                (m.getStandard() != null && m.getStandard().toLowerCase().contains(searchText)) ||
                                (m.getVendorCode() != null && m.getVendorCode().toLowerCase().contains(searchText)))
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
        MaterialCategoryClient.createCategory(name, parentId, description);
    }

    @Override
    protected void createClass(Long categoryId, String name, String description, Long unitId) throws Exception {
        MaterialClassClient.createClass(categoryId, name, description, unitId);
    }

    @FXML
    private void handleCreate() {
        showCreateItemDialog();
    }

    @FXML
    private void handleEdit() {
        MaterialDto selected = getTableView().getSelectionModel().getSelectedItem();
        if (selected != null) showEditItemDialog(selected);
        else showAlert("Внимание", "Выберите материал для редактирования", Alert.AlertType.CONFIRMATION);
    }

    @FXML
    private void handleDelete() {
        MaterialDto selected = getTableView().getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите материал для удаления", Alert.AlertType.CONFIRMATION);
            return;
        }
        deleteItem(selected.getId(), selected.getName());
    }

    @Override
    protected List<UnitOfMeasureDto> fetchUnits() throws Exception {
        return MaterialClient.getAllUnits();
    }

}
package com.fanproduction.gui.controller;

import com.fanproduction.gui.dto.response.ProductCardDto;
import com.fanproduction.gui.dto.view.CardViewField;
import com.fanproduction.gui.dto.view.CardViewLayout;
import com.fanproduction.gui.dto.view.CardViewSection;
import com.fanproduction.gui.dto.view.CardViewSubsection;
import com.fanproduction.gui.dto.view.CardViewTab;
import com.fanproduction.gui.service.CardViewBuilder;
import com.fanproduction.gui.service.CardViewBuilderImpl;
import com.fanproduction.gui.service.FieldMetadataService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Окно просмотра карточки продукции в режиме «только чтение».
 * <p>
 * Строится на основе {@link CardViewLayout}, который возвращает
 * {@link CardViewBuilder}. Логика разметки полностью вынесена из контроллера:
 * <ul>
 *   <li>Шапка и «Технические характеристики» — синхронно ({@link CardViewBuilder#buildForFan}
 *       или {@link CardViewBuilder#buildForSystemCard});</li>
 *   <li>Связанные элементы (электродвигатель, колёса, компоненты по ссылкам) —
 *       асинхронно ({@link CardViewBuilder#buildLinkedElements});</li>
 *   <li>Компоненты и материалы — асинхронно ({@link CardViewBuilder#buildComponentsSection},
 *       {@link CardViewBuilder#buildMaterialsSection}).</li>
 * </ul>
 * <p>
 * В GUI Spring-контекст не поднимается, поэтому {@link CardViewBuilder} и
 * {@link FieldMetadataService} создаются через {@code new}. Статическое поле
 * {@link #BUILDER} живёт всё время работы приложения — это позволяет шарить
 * кэш метаданных шаблонов (TTL 10 минут в {@link FieldMetadataService}) между
 * всеми окнами.
 */
@Slf4j
public class CardViewController {

    // ==========================================================
    // КОНСТАНТЫ
    // ==========================================================

    private static final String MAIN_TAB_TITLE = "Основные поля";
    private static final String TECH_SPECS_TITLE = "Технические характеристики";
    private static final String COMPONENTS_TITLE = "Компоненты";
    private static final String MATERIALS_TITLE = "Материалы";

    private static final String LOADING_TEXT = "Загрузка…";
    private static final String EMPTY_COMPONENTS_TEXT = "Нет компонентов";
    private static final String EMPTY_MATERIALS_TEXT = "Нет материалов";

    /**
     * Единый экземпляр builder'а — шарит кэш метаданных шаблонов.
     * В GUI Spring-контекст не используется — создаём через {@code new}.
     */
    private static final CardViewBuilder BUILDER =
            new CardViewBuilderImpl(new FieldMetadataService());

    // ==========================================================
    // ПОЛЯ ЭКЗЕМПЛЯРА
    // ==========================================================

    private final Stage dialog;
    private final ProductCardDto card;

    /** Контейнер для подсекций ТХ — сюда добавляются связанные элементы. */
    private VBox techSpecsSubsectionsContainer;

    /** Контейнер вкладки «Компоненты». */
    private VBox componentsContainer;

    /** Контейнер вкладки «Материалы». */
    private VBox materialsContainer;

    // ==========================================================
    // PUBLIC API
    // ==========================================================

    /**
     * Открыть окно просмотра карточки.
     * <p>
     * Обратная совместимость: сохранена сигнатура для {@code CatalogController}
     * и других точек вызова. Внутри создаётся экземпляр контроллера (stateless,
     * живёт до закрытия окна) и вызывается {@link #showInternal()}.
     */
    public static void show(Stage owner, ProductCardDto card) {
        if (card == null) {
            showAlert("Ошибка", "Карточка не получена", Alert.AlertType.ERROR);
            return;
        }
        new CardViewController(owner, card).showInternal();
    }

    // ==========================================================
    // КОНСТРУКТОР
    // ==========================================================

    private CardViewController(Stage owner, ProductCardDto card) {
        this.card = card;
        this.dialog = new Stage();
        this.dialog.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            this.dialog.initOwner(owner);
        }
    }

    // ==========================================================
    // ОСНОВНОЙ ЦИКЛ
    // ==========================================================

    private void showInternal() {
        CardViewLayout layout;
        try {
            layout = isFan()
                    ? BUILDER.buildForFan(card)
                    : BUILDER.buildForSystemCard(card);
        } catch (Exception e) {
            log.error("Ошибка построения layout для карточки id={}: {}",
                    card.getId(), e.getMessage(), e);
            showAlert("Ошибка", "Не удалось построить разметку: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            return;
        }

        if (layout == null || layout.tabs() == null || layout.tabs().isEmpty()) {
            showAlert("Ошибка", "Разметка карточки пуста", Alert.AlertType.ERROR);
            return;
        }

        String windowTitle = layout.title() != null ? layout.title() : card.getName();
        dialog.setTitle("Просмотр карточки — " + windowTitle);

        buildUI(layout);

        // Запускаем асинхронные загрузки ДО showAndWait — фоновые потоки
        // не блокируют UI, результат применяется через Platform.runLater.
        startAsyncLoads();

        dialog.showAndWait();
    }

    private boolean isFan() {
        return "FAN".equals(card.getCardType());
    }

    // ==========================================================
    // ПОСТРОЕНИЕ UI
    // ==========================================================

    private void buildUI(CardViewLayout layout) {
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        Label titleLabel = new Label(layout.title() != null ? layout.title() : card.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);

        Separator separator = new Separator();

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        for (CardViewTab tab : layout.tabs()) {
            Tab uiTab = new Tab(tab.title() != null ? tab.title() : "—");
            uiTab.setClosable(false);
            uiTab.setContent(buildTabContent(tab));
            tabPane.getTabs().add(uiTab);
        }

        Button closeButton = new Button("Закрыть");
        closeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        closeButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(closeButton);

        mainLayout.getChildren().addAll(titleLabel, separator, tabPane, buttonBox);

        Scene scene = new Scene(mainLayout, 750, 700);
        dialog.setScene(scene);
    }

    /**
     * Строит содержимое вкладки в зависимости от её назначения:
     * <ul>
     *   <li>«Основные поля» — шапка + секции (с особым контейнером для ТХ);</li>
     *   <li>«Компоненты» — плейсхолдер «Загрузка…», потом заменяется;</li>
     *   <li>«Материалы» — плейсхолдер «Загрузка…», потом заменяется;</li>
     *   <li>Остальные — все секции подряд.</li>
     * </ul>
     */
    private Node buildTabContent(CardViewTab tab) {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        String title = tab.title();

        if (MAIN_TAB_TITLE.equals(title)) {
            renderMainTab(tab, content);
        } else if (COMPONENTS_TITLE.equals(title)) {
            this.componentsContainer = content;
            content.getChildren().add(placeholder(LOADING_TEXT));
        } else if (MATERIALS_TITLE.equals(title)) {
            this.materialsContainer = content;
            content.getChildren().add(placeholder(LOADING_TEXT));
        } else {
            // Неизвестная вкладка — рендерим как есть
            if (tab.header() != null && !tab.header().isEmpty()) {
                content.getChildren().add(renderHeader(tab.header()));
                content.getChildren().add(new Separator());
            }
            if (tab.sections() != null) {
                for (CardViewSection section : tab.sections()) {
                    content.getChildren().add(renderSection(section));
                }
            }
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    /**
     * Рендер вкладки «Основные поля»: шапка + ТХ + (если есть) другие секции.
     * <p>
     * Контейнер подсекций ТХ сохраняется в {@link #techSpecsSubsectionsContainer},
     * чтобы {@link #applyLinkedElements(List)} мог добавить туда связанные
     * элементы после асинхронной загрузки.
     */
    private void renderMainTab(CardViewTab tab, VBox parent) {
        // Шапка
        if (tab.header() != null && !tab.header().isEmpty()) {
            parent.getChildren().add(renderHeader(tab.header()));
            parent.getChildren().add(new Separator());
        }

        List<CardViewSection> sections = tab.sections() != null ? tab.sections() : List.of();

        // Находим секцию ТХ (если есть)
        CardViewSection techSpecsSection = null;
        for (CardViewSection section : sections) {
            if (TECH_SPECS_TITLE.equals(section.title())) {
                techSpecsSection = section;
            } else {
                parent.getChildren().add(renderSection(section));
            }
        }

        // Всегда создаём контейнер ТХ — даже если секции не было,
        // чтобы связанные элементы могли быть добавлены асинхронно.
        parent.getChildren().add(buildTechSpecsBox(techSpecsSection));
    }

    /**
     * Собирает «Технические характеристики» из секции (или пустой каркас).
     * <p>
     * Сохраняет {@link #techSpecsSubsectionsContainer} — VBox, в который
     * {@link #applyLinkedElements(List)} добавляет подсекции.
     */
    private Node buildTechSpecsBox(CardViewSection techSpecsSection) {
        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 5 0 5 0;");

        Label title = new Label(TECH_SPECS_TITLE);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        box.getChildren().add(title);

        // Поля секции ТХ (без подсекций)
        if (techSpecsSection != null
                && techSpecsSection.fields() != null
                && !techSpecsSection.fields().isEmpty()) {
            VBox fieldsBox = new VBox(6);
            for (CardViewField field : techSpecsSection.fields()) {
                fieldsBox.getChildren().add(renderField(field));
            }
            box.getChildren().add(fieldsBox);
        }

        // Контейнер для подсекций (сначала шаблонные, потом — связанные)
        VBox subsectionsBox = new VBox(10);
        if (techSpecsSection != null && techSpecsSection.subsections() != null) {
            for (CardViewSubsection sub : techSpecsSection.subsections()) {
                subsectionsBox.getChildren().add(renderSubsection(sub));
            }
        }
        this.techSpecsSubsectionsContainer = subsectionsBox;
        box.getChildren().add(subsectionsBox);

        return box;
    }

    // ==========================================================
    // РЕНДЕР ЭЛЕМЕНТОВ
    // ==========================================================

    private Node renderHeader(List<CardViewField> header) {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(8);
        grid.setPadding(new Insets(5, 0, 10, 0));

        int row = 0;
        for (CardViewField field : header) {
            Label label = new Label(field.label() + ":");
            label.setStyle("-fx-font-weight: bold;");
            label.setMinWidth(180);

            Label value = new Label(field.value() != null ? field.value() : "—");
            value.setWrapText(true);

            grid.add(label, 0, row);
            grid.add(value, 1, row);
            row++;
        }
        return grid;
    }

    private Node renderSection(CardViewSection section) {
        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 5 0 5 0;");

        if (section.title() != null && !section.title().isEmpty()) {
            Label title = new Label(section.title());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            box.getChildren().add(title);
        }

        if (section.fields() != null && !section.fields().isEmpty()) {
            VBox fieldsBox = new VBox(6);
            for (CardViewField field : section.fields()) {
                fieldsBox.getChildren().add(renderField(field));
            }
            box.getChildren().add(fieldsBox);
        }

        if (section.subsections() != null && !section.subsections().isEmpty()) {
            VBox subsectionsBox = new VBox(10);
            for (CardViewSubsection sub : section.subsections()) {
                subsectionsBox.getChildren().add(renderSubsection(sub));
            }
            box.getChildren().add(subsectionsBox);
        }

        return box;
    }

    private Node renderSubsection(CardViewSubsection sub) {
        VBox box = new VBox(6);
        box.setStyle("-fx-padding: 5 0 5 15;");

        if (sub.title() != null && !sub.title().isEmpty()) {
            Label title = new Label(sub.title());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #555;");
            box.getChildren().add(title);
        }

        if (sub.fields() != null) {
            for (CardViewField field : sub.fields()) {
                box.getChildren().add(renderField(field));
            }
        }

        return box;
    }

    private Node renderField(CardViewField field) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.TOP_LEFT);

        Label label = new Label(field.label() + ":");
        label.setStyle("-fx-font-weight: bold;");
        label.setMinWidth(200);
        label.setPrefWidth(200);

        Label value = new Label(field.value() != null ? field.value() : "—");
        value.setWrapText(true);
        HBox.setHgrow(value, Priority.ALWAYS);

        box.getChildren().addAll(label, value);
        return box;
    }

    private Label placeholder(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
        return label;
    }

    // ==========================================================
    // АСИНХРОННЫЕ ЗАГРУЗКИ
    // ==========================================================

    private void startAsyncLoads() {
        loadLinkedElementsAsync();
        loadComponentsAsync();
        loadMaterialsAsync();
    }

    private void loadLinkedElementsAsync() {
        new Thread(() -> {
            try {
                List<CardViewSubsection> linked = BUILDER.buildLinkedElements(card);
                if (linked != null && !linked.isEmpty()) {
                    Platform.runLater(() -> applyLinkedElements(linked));
                }
            } catch (Exception e) {
                log.warn("Ошибка загрузки связанных элементов для карточки id={}: {}",
                        card.getId(), e.getMessage());
            }
        }, "CardView-linked").start();
    }

    private void loadComponentsAsync() {
        Long cardId = card.getId();
        new Thread(() -> {
            CardViewSection section;
            try {
                section = BUILDER.buildComponentsSection(cardId);
            } catch (Exception e) {
                log.warn("Ошибка загрузки компонентов для карточки id={}: {}",
                        cardId, e.getMessage());
                section = new CardViewSection(COMPONENTS_TITLE, List.of(), List.of());
            }
            CardViewSection finalSection = section;
            Platform.runLater(() -> applyComponentsSection(finalSection));
        }, "CardView-components").start();
    }

    private void loadMaterialsAsync() {
        Long cardId = card.getId();
        new Thread(() -> {
            CardViewSection section;
            try {
                section = BUILDER.buildMaterialsSection(cardId);
            } catch (Exception e) {
                log.warn("Ошибка загрузки материалов для карточки id={}: {}",
                        cardId, e.getMessage());
                section = new CardViewSection(MATERIALS_TITLE, List.of(), List.of());
            }
            CardViewSection finalSection = section;
            Platform.runLater(() -> applyMaterialsSection(finalSection));
        }, "CardView-materials").start();
    }

    // ==========================================================
    // ПРИМЕНЕНИЕ РЕЗУЛЬТАТОВ АСИНХРОННЫХ ЗАГРУЗОК
    // ==========================================================

    /**
     * Добавляет подсекции связанных элементов в контейнер ТХ.
     * <p>
     * Вызывается в FX-потоке через {@code Platform.runLater}.
     */
    private void applyLinkedElements(List<CardViewSubsection> linked) {
        if (techSpecsSubsectionsContainer == null) {
            log.warn("techSpecsSubsectionsContainer не инициализирован — пропуск связанных элементов");
            return;
        }
        for (CardViewSubsection sub : linked) {
            techSpecsSubsectionsContainer.getChildren().add(renderSubsection(sub));
        }
    }

    private void applyComponentsSection(CardViewSection section) {
        applySectionToContainer(section, componentsContainer, EMPTY_COMPONENTS_TEXT);
    }

    private void applyMaterialsSection(CardViewSection section) {
        applySectionToContainer(section, materialsContainer, EMPTY_MATERIALS_TEXT);
    }

    /**
     * Универсальная логика для «Компонентов» и «Материалов»:
     * очищает контейнер, рендерит поля и подсекции, либо показывает «Нет …».
     */
    private void applySectionToContainer(CardViewSection section,
                                         VBox container,
                                         String emptyText) {
        if (container == null) {
            return;
        }
        container.getChildren().clear();

        boolean hasFields = section != null
                && section.fields() != null && !section.fields().isEmpty();
        boolean hasSubsections = section != null
                && section.subsections() != null && !section.subsections().isEmpty();

        if (!hasFields && !hasSubsections) {
            container.getChildren().add(placeholder(emptyText));
            return;
        }

        if (hasFields) {
            VBox fieldsBox = new VBox(6);
            for (CardViewField field : section.fields()) {
                fieldsBox.getChildren().add(renderField(field));
            }
            container.getChildren().add(fieldsBox);
        }

        if (hasSubsections) {
            for (CardViewSubsection sub : section.subsections()) {
                container.getChildren().add(renderSubsection(sub));
            }
        }
    }

    // ==========================================================
    // УТИЛИТЫ
    // ==========================================================

    private static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

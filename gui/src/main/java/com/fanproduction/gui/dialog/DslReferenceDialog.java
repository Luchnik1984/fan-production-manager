package com.fanproduction.gui.dialog;


import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Справочник по DSL формулам (Спринт 9, US9.4.а).
 * <p>
 * UI-часть: слева дерево разделов (из {@link DslReferenceContent}), справа
 * текстовое описание выбранного раздела. Внизу — кнопки:
 * <ul>
 *   <li>«📋 Скопировать пример» — копирует {@link DslReferenceContent.Entry#sample()}
 *       в системный буфер обмена (если пример задан).</li>
 *   <li>«Вставить в поле» — вставляет пример в {@code targetFormula}
 *       (если он передан в конструктор; кнопка скрыта, если {@code null}).</li>
 *   <li>«Закрыть».</li>
 * </ul>
 * <p>
 * Модальность — {@link Modality#WINDOW_MODAL} относительно owner'а,
 * если он передан. В диалоге можно менять размер (prefWidth 900, prefHeight 700).
 */
public class DslReferenceDialog {

    private final Stage stage;
    private final TextInputControl targetFormula;

    private final TreeView<String> sectionsTree = new TreeView<>();
    private final TextArea bodyArea = new TextArea();
    private final Button copyButton = new Button("📋 Скопировать пример");
    private final Button insertButton = new Button("Вставить в поле");

    private DslReferenceContent.Entry currentEntry;

    /**
     * @param owner         родительское окно (может быть null)
     * @param targetFormula поле, в которое можно вставить пример
     *                      ({@link TextField} или {@link TextArea}).
     *                      Может быть {@code null} — тогда кнопка «Вставить в поле» скрыта.
     */
    public DslReferenceDialog(Window owner, TextInputControl targetFormula) {
        this.targetFormula = targetFormula;
        this.stage = new Stage();
        stage.setTitle("Справочник DSL — формулы маркировки");
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.setMinWidth(800);
        stage.setMinHeight(600);

        buildUI();
    }

    public void show() {
        stage.showAndWait();
    }

    /**
     * Показать справочник и сразу перейти к указанному разделу.
     * <p>
     * Пример: {@code show("Операторы условий")} — открывает справочник
     * на разделе «Операторы условий».
     *
     * @param sectionTitle точное имя раздела верхнего уровня (см. {@link DslReferenceContent})
     */
    public void show(String sectionTitle) {
        showSection(sectionTitle);
        stage.showAndWait();
    }

    /**
     * Перейти к разделу (без показа окна).
     * <p>
     * Если раздел не найден — ничего не делает. Используется в
     * {@link #show(String)} и потенциально для навигации из внешних
     * контроллеров.
     */
    public void showSection(String sectionTitle) {
        if (sectionTitle == null) return;

        TreeItem<String> root = sectionsTree.getRoot();
        if (root == null) return;

        for (TreeItem<String> sectionItem : root.getChildren()) {
            if (sectionTitle.equals(sectionItem.getValue())) {
                sectionItem.setExpanded(true);
                sectionsTree.getSelectionModel().select(sectionItem);
                int row = sectionsTree.getRow(sectionItem);
                if (row >= 0) {
                    sectionsTree.scrollTo(row);
                }
                return;
            }
        }
    }

    // ==========================================================
    // UI
    // ==========================================================

    private void buildUI() {
        // --- Левая панель: дерево разделов ---
        TreeItem<String> root = new TreeItem<>("DSL");
        root.setExpanded(true);

        for (DslReferenceContent.Section section : DslReferenceContent.getSections()) {
            TreeItem<String> sectionItem = new TreeItem<>(section.title());
            sectionItem.setExpanded(false);

            for (DslReferenceContent.Entry entry : section.entries()) {
                TreeItem<String> entryItem = new TreeItem<>(entry.title());
                entryItem.setGraphic(null);
                sectionItem.getChildren().add(entryItem);
            }

            root.getChildren().add(sectionItem);
        }

        sectionsTree.setRoot(root);
        sectionsTree.setShowRoot(false);
        sectionsTree.setPrefWidth(260);
        sectionsTree.setMinWidth(200);
        sectionsTree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    // Раздел (с детьми) — жирным; лист — обычным
                    if (!getTreeItem().isLeaf()) {
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-font-family: monospace;");
                    }
                }
            }
        });

        // --- Правая панель: тело ---
        bodyArea.setEditable(false);
        bodyArea.setWrapText(true);
        bodyArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        bodyArea.setPrefColumnCount(80);

        // --- Bottom toolbar ---
        copyButton.setDisable(true);
        copyButton.setOnAction(e -> copyExample());

        insertButton.setDisable(true);
        insertButton.setOnAction(e -> insertExample());
        if (targetFormula == null) {
            insertButton.setVisible(false);
            insertButton.setManaged(false);
        }

        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> stage.close());

        HBox bottom = new HBox(10, copyButton, insertButton, new Region(), closeButton);
        HBox.setHgrow(bottom.getChildren().get(2), Priority.ALWAYS);
        bottom.setPadding(new Insets(10, 0, 0, 0));

        // --- SplitPane ---
        SplitPane splitPane = new SplitPane(sectionsTree, bodyArea);
        splitPane.setDividerPositions(0.30);
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        VBox rootBox = new VBox(10, new Label("Выберите раздел слева для просмотра"),
                splitPane, bottom);
        rootBox.setPadding(new Insets(15));
        VBox.setVgrow(splitPane, Priority.ALWAYS);

        // --- Слушатель выбора ---
        sectionsTree.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> onSelectionChanged(sel));

        stage.setScene(new javafx.scene.Scene(rootBox, 950, 720));
    }

    private void onSelectionChanged(TreeItem<String> selected) {
        if (selected == null) {
            bodyArea.clear();
            copyButton.setDisable(true);
            insertButton.setDisable(true);
            currentEntry = null;
            return;
        }

        // Ищем Entry по пути: section → entry
        TreeItem<String> parent = selected.getParent();
        if (parent == null || parent.getParent() == null) {
            // Это раздел (или корень) — ничего не показываем
            bodyArea.setText("Выберите подраздел слева.\n\nРаздел: " + selected.getValue());
            copyButton.setDisable(true);
            insertButton.setDisable(true);
            currentEntry = null;
            return;
        }

        String sectionTitle = parent.getValue();
        String entryTitle = selected.getValue();

        DslReferenceContent.Entry entry = findEntry(sectionTitle, entryTitle);
        currentEntry = entry;

        if (entry == null) {
            bodyArea.setText("Описание не найдено.");
            copyButton.setDisable(true);
            insertButton.setDisable(true);
        } else {
            StringBuilder text = new StringBuilder(entry.body());
            if (entry.sample() != null && !entry.sample().isEmpty()) {
                text.append("\n\n─────────────────────────────────────────────\n");
                text.append("Пример (нажмите «Скопировать пример»):\n\n");
                text.append(entry.sample());
            }
            bodyArea.setText(text.toString());
            copyButton.setDisable(entry.sample() == null || entry.sample().isEmpty());
            insertButton.setDisable(entry.sample() == null
                    || entry.sample().isEmpty()
                    || targetFormula == null);
        }
    }

    private DslReferenceContent.Entry findEntry(String sectionTitle, String entryTitle) {
        for (DslReferenceContent.Section section : DslReferenceContent.getSections()) {
            if (!section.title().equals(sectionTitle)) continue;
            for (DslReferenceContent.Entry entry : section.entries()) {
                if (entry.title().equals(entryTitle)) {
                    return entry;
                }
            }
        }
        return null;
    }

    // ==========================================================
    // ДЕЙСТВИЯ
    // ==========================================================

    private void copyExample() {
        if (currentEntry == null || currentEntry.sample() == null) return;
        ClipboardContent content = new ClipboardContent();
        content.putString(currentEntry.sample());
        Clipboard.getSystemClipboard().setContent(content);
        // Мягкий фидбек: мигаем тултипом
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.initOwner(stage);
        info.setTitle("Скопировано");
        info.setHeaderText(null);
        info.setContentText("Пример формулы скопирован в буфер обмена.");
        info.showAndWait();
    }

    private void insertExample() {
        if (currentEntry == null || currentEntry.sample() == null || targetFormula == null) return;
        targetFormula.setText(currentEntry.sample());
        targetFormula.requestFocus();
        stage.close();
    }

    private static class Region extends javafx.scene.layout.Region {
    }
}
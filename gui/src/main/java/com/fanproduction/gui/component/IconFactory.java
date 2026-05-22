package com.fanproduction.gui.component;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

public class IconFactory {

    private static final int DEFAULT_SIZE = 14;

    // ==================== ИКОНКИ ДЛЯ ДЕРЕВА ====================
    public static Node createFolderIcon() {
        FontIcon icon = new FontIcon(FontAwesome.FOLDER);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#e6b422"));
        return icon;
    }

    public static Node createFolderOpenIcon() {
        FontIcon icon = new FontIcon(FontAwesome.FOLDER_OPEN);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#e6b422"));
        return icon;
    }

    public static Node createFileIcon() {
        FontIcon icon = new FontIcon(FontAwesome.FILE);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#555555"));
        return icon;
    }

    public static Node createClassIcon() {
        return createFolderOpenIcon();
    }

    // ==================== ИКОНКИ ДЕЙСТВИЙ ====================
    public static Node createEditIcon() {
        FontIcon icon = new FontIcon(FontAwesome.PENCIL_SQUARE_O);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#2196F3"));
        return icon;
    }

    public static Node createDeleteIcon() {
        FontIcon icon = new FontIcon(FontAwesome.TRASH_O);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#f44336"));
        return icon;
    }

    public static Node createAddIcon() {
        FontIcon icon = new FontIcon(FontAwesome.PLUS_SQUARE_O);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#4CAF50"));
        return icon;
    }

    public static Node createExportIcon() {
        FontIcon icon = new FontIcon(FontAwesome.FILE_EXCEL_O);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#4CAF50"));
        return icon;
    }

    public static Node createPrintIcon() {
        FontIcon icon = new FontIcon(FontAwesome.PRINT);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#607D8B"));
        return icon;
    }

    public static Node createRefreshIcon() {
        FontIcon icon = new FontIcon(FontAwesome.REFRESH);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#2196F3"));
        return icon;
    }

    public static Node createSaveIcon() {
        FontIcon icon = new FontIcon(FontAwesome.SAVE);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#4CAF50"));
        return icon;
    }

    public static Node createCancelIcon() {
        FontIcon icon = new FontIcon(FontAwesome.TIMES);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#f44336"));
        return icon;
    }

    public static Node createSearchIcon() {
        FontIcon icon = new FontIcon(FontAwesome.SEARCH);
        icon.setIconSize(DEFAULT_SIZE);
        icon.setIconColor(Color.web("#607D8B"));
        return icon;
    }
}

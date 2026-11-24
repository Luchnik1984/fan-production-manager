package com.fanproduction.gui.builder;

import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.dto.response.CardSelectionDto;
import javafx.scene.control.TreeItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Построитель дерева комплектующих по роли (US10.7.2).
 * <p>
 * Иерархия: тип продукции → карточки этого типа. Роль задаёт смысл
 * (осевое колесо, клапан…), поэтому в одном дереве оказываются карточки
 * разных типов с одинаковой ролью — например AW и RAW.
 */
public class CardRoleTreeBuilder {

    private static final String NULL_PLACEHOLDER = "—";

    public TreeItem<Object> buildTree(List<CardSelectionDto> items) {
        TreeItem<Object> root = new TreeItem<>();
        root.setValue(null);
        root.setExpanded(true);

        Map<String, List<CardSelectionDto>> byType = new LinkedHashMap<>();
        for (CardSelectionDto item : items) {
            String key = item.getTypeName() != null && !item.getTypeName().isBlank()
                    ? item.getTypeName()
                    : NULL_PLACEHOLDER;
            byType.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(item);
        }

        for (Map.Entry<String, List<CardSelectionDto>> entry : byType.entrySet()) {
            TreeItem<Object> typeItem = new TreeItem<>(entry.getKey());
            typeItem.setExpanded(true);
            typeItem.setGraphic(IconFactory.createFolderIcon());

            for (CardSelectionDto card : entry.getValue()) {
                TreeItem<Object> cardItem = new TreeItem<>(card);
                cardItem.setGraphic(IconFactory.createFileIcon());
                typeItem.getChildren().add(cardItem);
            }
            root.getChildren().add(typeItem);
        }

        return root;
    }
}

package com.fanproduction.gui.component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Separator;

import java.util.List;
import java.util.Map;

public class GroupedComboBox<T extends HasCategory> extends ComboBox<T> {

    public GroupedComboBox() {
        setCellFactory(lv -> new GroupedListCell());
        setButtonCell(new GroupedListCell());
    }

    public void setGroupedItems(Map<String, List<T>> groupedItems) {
        ObservableList<T> flatList = FXCollections.observableArrayList();

        for (Map.Entry<String, List<T>> entry : groupedItems.entrySet()) {
            if (!flatList.isEmpty()) {
                flatList.add(null); // разделитель между категориями
            }
            flatList.addAll(entry.getValue());
        }

        setItems(flatList);
    }

    private class GroupedListCell extends ListCell<T> {
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (item == null) {
                // Разделитель между категориями
                Separator separator = new Separator();
                setGraphic(separator);
                setText(null);
            } else {
                setText(item.getDisplayName());
                setGraphic(null);
            }
        }
    }
}

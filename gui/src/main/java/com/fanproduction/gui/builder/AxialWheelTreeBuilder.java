package com.fanproduction.gui.builder;

import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.dto.response.AxialWheelDto;
import javafx.scene.control.TreeItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Построитель дерева осевых колёс.
 * <p>
 * Иерархия: Производитель → Тип лопаток → Размер → Экземпляр.
 * <p>
 * <b>Null-safe.</b>
 */
public class AxialWheelTreeBuilder {

    private static final String NULL_PLACEHOLDER = "—";

    public TreeItem<Object> buildTree(List<AxialWheelDto> items) {
        TreeItem<Object> root = new TreeItem<>();
        root.setValue(null);
        root.setExpanded(true);

        Map<String, List<AxialWheelDto>> byManufacturer = items.stream()
                .collect(Collectors.groupingBy(
                        item -> safe(item.getManufacturer())));

        for (Map.Entry<String, List<AxialWheelDto>> manufacturerEntry : byManufacturer.entrySet()) {
            TreeItem<Object> manufacturerItem = new TreeItem<>(manufacturerEntry.getKey());
            manufacturerItem.setExpanded(true);
            manufacturerItem.setGraphic(IconFactory.createFolderIcon());

            Map<String, List<AxialWheelDto>> byBladeType = manufacturerEntry.getValue().stream()
                    .collect(Collectors.groupingBy(
                            item -> safe(item.getBladeType())));

            for (Map.Entry<String, List<AxialWheelDto>> bladeEntry : byBladeType.entrySet()) {
                TreeItem<Object> bladeItem = new TreeItem<>(bladeEntry.getKey());
                bladeItem.setExpanded(true);
                bladeItem.setGraphic(IconFactory.createFolderIcon());

                Map<String, List<AxialWheelDto>> bySize = bladeEntry.getValue().stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getSize() != null ? item.getSize() + " мм" : NULL_PLACEHOLDER));

                for (Map.Entry<String, List<AxialWheelDto>> sizeEntry : bySize.entrySet()) {
                    TreeItem<Object> sizeItem = new TreeItem<>(sizeEntry.getKey());
                    sizeItem.setExpanded(true);
                    sizeItem.setGraphic(IconFactory.createFolderIcon());

                    for (AxialWheelDto item : sizeEntry.getValue()) {
                        TreeItem<Object> componentItem = new TreeItem<>(item);
                        componentItem.setGraphic(IconFactory.createFileIcon());
                        sizeItem.getChildren().add(componentItem);
                    }
                    bladeItem.getChildren().add(sizeItem);
                }
                manufacturerItem.getChildren().add(bladeItem);
            }
            root.getChildren().add(manufacturerItem);
        }

        return root;
    }

    private static String safe(String value) {
        return value != null ? value : NULL_PLACEHOLDER;
    }
}
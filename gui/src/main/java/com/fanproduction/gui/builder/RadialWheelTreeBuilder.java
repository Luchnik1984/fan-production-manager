package com.fanproduction.gui.builder;

import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.dto.response.RadialWheelDto;
import javafx.scene.control.TreeItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Построитель дерева радиальных колёс.
 * <p>
 * Иерархия: Производитель → Тип лопаток → Размер → Экземпляр.
 * <p>
 * <b>Null-safe.</b>
 */
public class RadialWheelTreeBuilder {

    private static final String NULL_PLACEHOLDER = "—";

    public TreeItem<Object> buildTree(List<RadialWheelDto> items) {
        TreeItem<Object> root = new TreeItem<>();
        root.setValue(null);
        root.setExpanded(true);

        Map<String, List<RadialWheelDto>> byManufacturer = items.stream()
                .collect(Collectors.groupingBy(
                        item -> safe(item.getManufacturer())));

        for (Map.Entry<String, List<RadialWheelDto>> manufacturerEntry : byManufacturer.entrySet()) {
            TreeItem<Object> manufacturerItem = new TreeItem<>(manufacturerEntry.getKey());
            manufacturerItem.setExpanded(true);
            manufacturerItem.setGraphic(IconFactory.createFolderIcon());

            Map<String, List<RadialWheelDto>> byBladeType = manufacturerEntry.getValue().stream()
                    .collect(Collectors.groupingBy(
                            item -> safe(item.getBladeType())));

            for (Map.Entry<String, List<RadialWheelDto>> bladeEntry : byBladeType.entrySet()) {
                TreeItem<Object> bladeItem = new TreeItem<>(bladeEntry.getKey());
                bladeItem.setExpanded(true);
                bladeItem.setGraphic(IconFactory.createFolderIcon());

                Map<String, List<RadialWheelDto>> bySize = bladeEntry.getValue().stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getSize() != null ? item.getSize() + " мм" : NULL_PLACEHOLDER));

                for (Map.Entry<String, List<RadialWheelDto>> sizeEntry : bySize.entrySet()) {
                    TreeItem<Object> sizeItem = new TreeItem<>(sizeEntry.getKey());
                    sizeItem.setExpanded(true);
                    sizeItem.setGraphic(IconFactory.createFolderIcon());

                    for (RadialWheelDto item : sizeEntry.getValue()) {
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

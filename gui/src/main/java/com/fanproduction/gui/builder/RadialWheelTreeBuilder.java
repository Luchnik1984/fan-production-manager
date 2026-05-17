package com.fanproduction.gui.builder;

import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.dto.response.RadialWheelDto;
import javafx.scene.control.TreeItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RadialWheelTreeBuilder {

    public TreeItem<Object> buildTree(List<RadialWheelDto> items) {
        TreeItem<Object> root = new TreeItem<>();
        root.setValue(null);
        root.setExpanded(true);

        Map<String, List<RadialWheelDto>> byManufacturer = items.stream()
                .filter(item -> item.getManufacturer() != null)
                .collect(Collectors.groupingBy(RadialWheelDto::getManufacturer));

        for (Map.Entry<String, List<RadialWheelDto>> manufacturerEntry : byManufacturer.entrySet()) {
            TreeItem<Object> manufacturerItem = new TreeItem<>(manufacturerEntry.getKey());
            manufacturerItem.setExpanded(true);
            manufacturerItem.setGraphic(IconFactory.createFolderIcon());

            Map<String, List<RadialWheelDto>> byBladeType = manufacturerEntry.getValue().stream()
                    .collect(Collectors.groupingBy(RadialWheelDto::getBladeType));

            for (Map.Entry<String, List<RadialWheelDto>> bladeEntry : byBladeType.entrySet()) {
                TreeItem<Object> bladeItem = new TreeItem<>(bladeEntry.getKey());
                bladeItem.setExpanded(true);
                bladeItem.setGraphic(IconFactory.createFolderIcon());

                Map<Double, List<RadialWheelDto>> bySize = bladeEntry.getValue().stream()
                        .collect(Collectors.groupingBy(RadialWheelDto::getSize));

                for (Map.Entry<Double, List<RadialWheelDto>> sizeEntry : bySize.entrySet()) {
                    TreeItem<Object> sizeItem = new TreeItem<>(sizeEntry.getKey() + " мм");
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
}

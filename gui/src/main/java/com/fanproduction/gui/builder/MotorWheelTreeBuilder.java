package com.fanproduction.gui.builder;

import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.dto.response.MotorWheelDto;
import javafx.scene.control.TreeItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MotorWheelTreeBuilder {

    public TreeItem<Object> buildTree(List<MotorWheelDto> items) {
        TreeItem<Object> root = new TreeItem<>();
        root.setValue(null);
        root.setExpanded(true);

        Map<String, List<MotorWheelDto>> byManufacturer = items.stream()
                .filter(item -> item.getManufacturer() != null)
                .collect(Collectors.groupingBy(MotorWheelDto::getManufacturer));

        for (Map.Entry<String, List<MotorWheelDto>> manufacturerEntry : byManufacturer.entrySet()) {
            TreeItem<Object> manufacturerItem = new TreeItem<>(manufacturerEntry.getKey());
            manufacturerItem.setExpanded(true);
            manufacturerItem.setGraphic(IconFactory.createFolderIcon());

            Map<String, List<MotorWheelDto>> byBladeType = manufacturerEntry.getValue().stream()
                    .collect(Collectors.groupingBy(MotorWheelDto::getBladeType));

            for (Map.Entry<String, List<MotorWheelDto>> bladeEntry : byBladeType.entrySet()) {
                TreeItem<Object> bladeItem = new TreeItem<>(bladeEntry.getKey());
                bladeItem.setExpanded(true);
                bladeItem.setGraphic(IconFactory.createFolderIcon());

                Map<Integer, List<MotorWheelDto>> bySize = bladeEntry.getValue().stream()
                        .collect(Collectors.groupingBy(MotorWheelDto::getSize));

                for (Map.Entry<Integer, List<MotorWheelDto>> sizeEntry : bySize.entrySet()) {
                    TreeItem<Object> sizeItem = new TreeItem<>(sizeEntry.getKey() + " мм");
                    sizeItem.setExpanded(true);
                    sizeItem.setGraphic(IconFactory.createFolderIcon());

                    for (MotorWheelDto item : sizeEntry.getValue()) {
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

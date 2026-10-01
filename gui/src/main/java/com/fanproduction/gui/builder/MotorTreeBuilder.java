package com.fanproduction.gui.builder;

import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.dto.response.MotorDto;
import javafx.scene.control.TreeItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MotorTreeBuilder {

    public TreeItem<Object> buildTree(List<MotorDto> items) {
        TreeItem<Object> root = new TreeItem<>();
        root.setValue(null);
        root.setExpanded(true);

        Map<String, List<MotorDto>> bySeries = items.stream()
                .filter(item -> item.getSeries() != null)
                .collect(Collectors.groupingBy(MotorDto::getSeries));

        for (Map.Entry<String, List<MotorDto>> seriesEntry : bySeries.entrySet()) {
            TreeItem<Object> seriesItem = new TreeItem<>(seriesEntry.getKey());
            seriesItem.setExpanded(true);
            seriesItem.setGraphic(IconFactory.createFolderIcon());

            Map<String, List<MotorDto>> byMounting = seriesEntry.getValue().stream()
                    .collect(Collectors.groupingBy(MotorDto::getMountingType));

            for (Map.Entry<String, List<MotorDto>> mountingEntry : byMounting.entrySet()) {
                TreeItem<Object> mountingItem = new TreeItem<>(mountingEntry.getKey());
                mountingItem.setExpanded(true);
                mountingItem.setGraphic(IconFactory.createFolderIcon());

                Map<String, List<MotorDto>> byMotorType = mountingEntry.getValue().stream()
                        .collect(Collectors.groupingBy(MotorDto::getMotorType));

                for (Map.Entry<String, List<MotorDto>> motorTypeEntry : byMotorType.entrySet()) {
                    TreeItem<Object> motorTypeItem = new TreeItem<>(motorTypeEntry.getKey());
                    motorTypeItem.setExpanded(true);
                    motorTypeItem.setGraphic(IconFactory.createFolderIcon());

                    for (MotorDto item : motorTypeEntry.getValue()) {
                        TreeItem<Object> componentItem = new TreeItem<>(item);
                        componentItem.setGraphic(IconFactory.createFileIcon());
                        motorTypeItem.getChildren().add(componentItem);
                    }
                    mountingItem.getChildren().add(motorTypeItem);
                }
                seriesItem.getChildren().add(mountingItem);
            }
            root.getChildren().add(seriesItem);
        }

        return root;
    }
}

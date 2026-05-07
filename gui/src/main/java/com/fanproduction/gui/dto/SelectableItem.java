package com.fanproduction.gui.dto;

import lombok.Getter;

public class SelectableItem {
    @Getter
    private final Long id;
    private final String displayName;

    public SelectableItem(Long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

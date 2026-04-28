package com.fanproduction.gui.dto;

public class SelectableItem {
    private final Long id;
    private final String displayName;

    public SelectableItem(Long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

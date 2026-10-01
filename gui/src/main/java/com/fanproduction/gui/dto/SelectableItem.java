package com.fanproduction.gui.dto;

import com.fanproduction.core.dto.Displayable;
import lombok.Getter;

public class SelectableItem implements Displayable {
    @Getter
    private final Long id;
    private final String displayName;

    public SelectableItem(Long id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

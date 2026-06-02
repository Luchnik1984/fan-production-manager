package com.fanproduction.gui.base;

import com.fanproduction.core.dto.Displayable;
import lombok.Getter;

@Getter
public class CategoryTreeItem implements Displayable {
    private final Long id;
    private final String displayName;
    private final String type;
    private final Long classId;

    public CategoryTreeItem(Long id, String displayName, String type) {
        this(id, displayName, type, null);
    }

    public CategoryTreeItem(Long id, String displayName, String type, Long classId) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.classId = classId;
    }

    public Long getCategoryId() { return id; }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
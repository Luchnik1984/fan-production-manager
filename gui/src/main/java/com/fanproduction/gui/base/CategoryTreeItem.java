package com.fanproduction.gui.base;

import lombok.Getter;

@Getter
public class CategoryTreeItem {
    private final Long id;
    private final String displayName;
    private final String type; // "root", "category", "class"
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
}
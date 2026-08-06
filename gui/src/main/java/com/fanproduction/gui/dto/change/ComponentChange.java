package com.fanproduction.gui.dto.change;

public class ComponentChange {
    public enum Type { ADD, REMOVE, UPDATE_QUANTITY, UPDATE_POSITION, UPDATE_NOTE }

    public final Type type;
    public final Long componentId;
    public Double quantity;
    public String position;
    public String note;

    public ComponentChange(Type type, Long componentId, Double quantity, String position, String note) {
        this.type = type;
        this.componentId = componentId;
        this.quantity = quantity;
        this.position = position;
        this.note = note;
    }
}

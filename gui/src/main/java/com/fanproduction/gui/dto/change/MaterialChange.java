package com.fanproduction.gui.dto.change;

public class MaterialChange {
    public enum Type { ADD, REMOVE, UPDATE_QUANTITY, UPDATE_NOTE }

    public final Type type;
    public final Long materialId;
    public Double quantity;
    public String note;

    public MaterialChange(Type type, Long materialId, Double quantity, String note) {
        this.type = type;
        this.materialId = materialId;
        this.quantity = quantity;
        this.note = note;
    }
}

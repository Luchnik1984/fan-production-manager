package com.fanproduction.gui.manager;

import com.fanproduction.gui.controller.CardFormController;
import com.fanproduction.gui.controller.ProductComponentsController;
import com.fanproduction.gui.dto.ProductComponentItemDto;
import com.fanproduction.gui.dto.change.ComponentChange;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.client.ApiClient;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentChangeManager {

    private final ProductComponentsController componentsController;
    private final CardFormController parentController;
    private final List<ComponentChange> pendingChanges = new ArrayList<>();

    public ComponentChangeManager(ProductComponentsController componentsController,
                                  CardFormController parentController) {
        this.componentsController = componentsController;
        this.parentController = parentController;
    }

    // ===================== ЛОКАЛЬНЫЕ МЕТОДЫ =====================

    public void addLocal(Long componentId, Double quantity, String position, String note) {
        if (componentId == null) return;
        ProductComponentItemDto newItem = new ProductComponentItemDto();
        newItem.setComponentId(componentId);
        newItem.setQuantity(quantity != null ? quantity : 1.0);
        newItem.setPosition(position);
        newItem.setNote(note);
        newItem.setName("Загрузка...");
        componentsController.addItem(newItem);
        pendingChanges.add(new ComponentChange(ComponentChange.Type.ADD, componentId, quantity, position, note));
        parentController.loadComponentNameAndUpdate(componentId, newItem);
    }

    public void removeLocal(Long componentId) {
        if (componentId == null) return;
        ProductComponentItemDto toRemove = null;
        for (ProductComponentItemDto item : componentsController.getItems()) {
            if (item.getComponentId().equals(componentId)) {
                toRemove = item;
                break;
            }
        }
        if (toRemove == null) return;
        componentsController.removeItem(toRemove);

        // Если есть ADD – удаляем (отмена добавления)
        pendingChanges.removeIf(change -> change.type == ComponentChange.Type.ADD && change.componentId.equals(componentId));
        // Иначе – REMOVE
        pendingChanges.add(new ComponentChange(ComponentChange.Type.REMOVE, componentId, null, null, null));
    }

    public void updateQuantityLocal(Long componentId, Double newQuantity) {
        if (componentId == null || newQuantity == null) return;
        for (ProductComponentItemDto item : componentsController.getItems()) {
            if (item.getComponentId().equals(componentId)) {
                item.setQuantity(newQuantity);
                componentsController.refreshItem(item);
                break;
            }
        }
        for (ComponentChange change : pendingChanges) {
            if (change.type == ComponentChange.Type.ADD && change.componentId.equals(componentId)) {
                change.quantity = newQuantity;
                return;
            }
        }
        pendingChanges.add(new ComponentChange(ComponentChange.Type.UPDATE_QUANTITY, componentId, newQuantity, null, null));
    }

    public void updatePositionLocal(Long componentId, String newPosition) {
        if (componentId == null) return;
        for (ProductComponentItemDto item : componentsController.getItems()) {
            if (item.getComponentId().equals(componentId)) {
                item.setPosition(newPosition);
                componentsController.refreshItem(item);
                break;
            }
        }
        for (ComponentChange change : pendingChanges) {
            if (change.type == ComponentChange.Type.ADD && change.componentId.equals(componentId)) {
                change.position = newPosition;
                return;
            }
        }
        pendingChanges.add(new ComponentChange(ComponentChange.Type.UPDATE_POSITION, componentId, null, newPosition, null));
    }

    public void updateNoteLocal(Long componentId, String newNote) {
        if (componentId == null) return;
        for (ProductComponentItemDto item : componentsController.getItems()) {
            if (item.getComponentId().equals(componentId)) {
                item.setNote(newNote);
                componentsController.refreshItem(item);
                break;
            }
        }
        for (ComponentChange change : pendingChanges) {
            if (change.type == ComponentChange.Type.ADD && change.componentId.equals(componentId)) {
                change.note = newNote;
                return;
            }
        }
        pendingChanges.add(new ComponentChange(ComponentChange.Type.UPDATE_NOTE, componentId, null, null, newNote));
    }

    // ===================== ПРИМЕНЕНИЕ ИЗМЕНЕНИЙ =====================

    public void applyChanges(Long cardId) {
        if (pendingChanges.isEmpty()) return;
        List<ComponentChange> changes = new ArrayList<>(pendingChanges);
        pendingChanges.clear();

        // Сортируем: сначала REMOVE, потом остальное
        changes.sort((a, b) -> {
            if (a.type == ComponentChange.Type.REMOVE && b.type != ComponentChange.Type.REMOVE) return -1;
            if (a.type != ComponentChange.Type.REMOVE && b.type == ComponentChange.Type.REMOVE) return 1;
            return 0;
        });

        for (ComponentChange change : changes) {
            try {
                switch (change.type) {
                    case REMOVE:
                        ApiClient.delete("/components/product/" + cardId + "/" + change.componentId);
                        break;
                    case ADD:
                        Map<String, Object> req = new HashMap<>();
                        req.put("componentId", change.componentId);
                        req.put("quantity", change.quantity != null ? change.quantity : 1.0);
                        if (change.position != null && !change.position.isEmpty()) req.put("position", change.position);
                        if (change.note != null && !change.note.isEmpty()) req.put("note", change.note);
                        ApiClient.post("/components/product/" + cardId, req,
                                new TypeReference<ApiResponse<Map<String, Object>>>() {});
                        break;
                    case UPDATE_QUANTITY:
                        Map<String, Double> qty = new HashMap<>();
                        qty.put("quantity", change.quantity);
                        ApiClient.put("/components/product/" + cardId + "/" + change.componentId + "/quantity", qty,
                                new TypeReference<ApiResponse<Void>>() {});
                        break;
                    case UPDATE_POSITION:
                        Map<String, String> pos = new HashMap<>();
                        pos.put("position", change.position != null ? change.position : "");
                        ApiClient.put("/components/product/" + cardId + "/" + change.componentId + "/position", pos,
                                new TypeReference<ApiResponse<Void>>() {});
                        break;
                    case UPDATE_NOTE:
                        Map<String, String> noteMap = new HashMap<>();
                        noteMap.put("note", change.note != null ? change.note : "");
                        ApiClient.put("/components/product/" + cardId + "/" + change.componentId + "/note", noteMap,
                                new TypeReference<ApiResponse<Void>>() {});
                        break;
                }
            } catch (Exception e) {
                System.err.println("Failed to apply component change: " + e.getMessage());
                parentController.showAlert("Ошибка", "Не удалось применить изменение компонента: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    // ===================== ОЧИСТКА =====================

    public void clear() {
        pendingChanges.clear();
    }

    // ===================== ЗАГРУЗКА ИЗ БД (опционально) =====================

    public void loadFromDatabase(Long cardId) {
        // Можно перенести сюда логику loadComponents из ProductComponentsController,
        // но пока оставим как есть.
        // Этот метод может быть вызван из CardFormController при открытии карточки.
        // Для простоты мы будем использовать существующий componentsController.refresh(cardId)
    }
}

package com.fanproduction.gui.manager;

import com.fanproduction.gui.controller.CardFormController;
import com.fanproduction.gui.controller.ProductComponentsController;
import com.fanproduction.gui.dto.ProductComponentItemDto;
import com.fanproduction.gui.dto.change.ComponentChange;
import com.fanproduction.gui.dto.request.ProductFullRequestDto;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.client.ApiClient;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Менеджер изменений по компонентам.
 * <p>
 * Работает в двух режимах:
 * <ul>
 *   <li><b>Локальный (для FAN):</b> изменения копятся, при сохранении
 *       формируется {@link ProductFullRequestDto} через {@link # buildRequest()}.</li>
 *   <li><b>Старый (для системных карточек):</b> изменения применяются
 *       через {@link #applyChanges(Long)} (это будет удалено в 8.6.c).</li>
 * </ul>
 */
public class ComponentChangeManager {

    private final ProductComponentsController componentsController;
    private final CardFormController parentController;
    private final List<ComponentChange> pendingChanges = new ArrayList<>();

    public ComponentChangeManager(ProductComponentsController componentsController,
                                  CardFormController parentController) {
        this.componentsController = componentsController;
        this.parentController = parentController;
    }

    // ===================== ЛОКАЛЬНЫЕ ОПЕРАЦИИ =====================

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

        ProductComponentItemDto itemToRemove = null;
        for (ProductComponentItemDto item : componentsController.getItems()) {
            if (item.getComponentId().equals(componentId)) {
                itemToRemove = item;
                break;
            }
        }
        if (itemToRemove == null) return;

        if (itemToRemove.getPosition() != null && !itemToRemove.getPosition().isEmpty()) {
            parentController.showAlert(
                    "Ошибка",
                    "Компонент '" + itemToRemove.getDisplayName() + "' используется в поле '" + itemToRemove.getPosition() + "'. " +
                            "Сначала удалите его из основного поля карточки.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        componentsController.removeItem(itemToRemove);
        pendingChanges.removeIf(change -> change.type == ComponentChange.Type.ADD && change.componentId.equals(componentId));
        pendingChanges.add(new ComponentChange(ComponentChange.Type.REMOVE, componentId, null, null, null));
    }

    public void forceRemoveLocal(Long componentId) {
        if (componentId == null) return;

        ProductComponentItemDto itemToRemove = null;
        for (ProductComponentItemDto item : componentsController.getItems()) {
            if (item.getComponentId().equals(componentId)) {
                itemToRemove = item;
                break;
            }
        }
        if (itemToRemove == null) return;

        componentsController.removeItem(itemToRemove);
        pendingChanges.removeIf(change -> change.type == ComponentChange.Type.ADD && change.componentId.equals(componentId));
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

    // ===================== ФОРМИРОВАНИЕ ЗАПРОСА (для FAN) =====================

    /**
     * Собрать данные для атомарного запроса.
     * <p>
     * Используется для FAN — вместо {@link #applyChanges(Long)}.
     * <p>
     * Логика:
     * <ol>
     *   <li>Все текущие элементы из UI (componentsController.getItems()) —
     *       в список {@code components}.</li>
     *   <li>Все pendingChanges типа REMOVE — в список {@code componentsToRemove}.</li>
     * </ol>
     * <p>
     * После вызова — pendingChanges очищаются.
     */
    public void buildRequest(ProductFullRequestDto target) {
        List<ProductFullRequestDto.ComponentItemDto> components = new ArrayList<>();
        for (ProductComponentItemDto item : componentsController.getItems()) {
            if (item.getComponentId() == null) continue;
            components.add(new ProductFullRequestDto.ComponentItemDto(
                    item.getComponentId(),
                    item.getQuantity() != null ? item.getQuantity() : 1.0,
                    item.getPosition(),
                    item.getNote()
            ));
        }

        List<Long> toRemove = new ArrayList<>();
        for (ComponentChange change : pendingChanges) {
            if (change.type == ComponentChange.Type.REMOVE) {
                toRemove.add(change.componentId);
            }
        }

        target.setComponents(components);
        target.setComponentsToRemove(toRemove);

        pendingChanges.clear();
    }

    // ===================== СТАРОЕ (для системных карточек) =====================

    /**
     * Применить изменения через отдельные endpoints.
     * <p>
     * <b>DEPRECATED:</b> используется только для системных карточек.
     * Будет удалён в 8.6.c.
     */
    public void applyChanges(Long cardId) {
        if (pendingChanges.isEmpty()) return;
        List<ComponentChange> changes = new ArrayList<>(pendingChanges);
        pendingChanges.clear();

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

    public void clear() {
        pendingChanges.clear();
    }
}

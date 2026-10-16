package com.fanproduction.gui.manager;

import com.fanproduction.gui.controller.CardFormController;
import com.fanproduction.gui.controller.ProductComponentsController;
import com.fanproduction.gui.dto.ProductComponentItemDto;
import com.fanproduction.gui.dto.change.ComponentChange;
import com.fanproduction.gui.dto.request.ProductFullRequestDto;
import javafx.scene.control.Alert;

import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер изменений по компонентам.
 * <p>
 * Работает в двух режимах:
 * <ul>
 *   <li><b>Локальный (для FAN):</b> изменения копятся, при сохранении
 *       формируется {@link ProductFullRequestDto} через {@link # buildRequest()}.</li>
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

    /**
     * Удалить компонент из UI (с проверкой роли).
     * <p>
     * Запрещает удаление, если у компонента есть {@code position} —
     * он используется в основном поле карточки.
     */
    public void removeLocal(Long componentId) {
        removeLocalInternal(componentId, true);
    }

    /**
     * Удалить компонент из UI без проверки роли.
     * <p>
     * Используется при замене компонента через основное поле —
     * старый компонент удаляется принудительно.
     */
    public void forceRemoveLocal(Long componentId) {
        removeLocalInternal(componentId, false);
    }

    /**
     * Общая логика удаления.
     *
     * @param componentId  ID компонента
     * @param checkPosition проверять ли role-позицию (true для user-initiated)
     */
    private void removeLocalInternal(Long componentId, boolean checkPosition) {
        if (componentId == null) return;

        ProductComponentItemDto itemToRemove = findItemByComponentId(componentId);
        if (itemToRemove == null) return;

        if (checkPosition) {
            if (itemToRemove.getPosition() != null && !itemToRemove.getPosition().isEmpty()) {
                parentController.showAlert(
                        "Ошибка",
                        "Компонент '" + itemToRemove.getDisplayName() + "' используется в поле '"
                                + itemToRemove.getPosition() + "'. "
                                + "Сначала удалите его из основного поля карточки.",
                        Alert.AlertType.WARNING
                );
                return;
            }
        }

        componentsController.removeItem(itemToRemove);
        pendingChanges.removeIf(change -> change.type == ComponentChange.Type.ADD
                && change.componentId.equals(componentId));
        pendingChanges.add(new ComponentChange(
                ComponentChange.Type.REMOVE, componentId, null, null, null));
    }

    /**
     * Найти DTO-элемент в UI по ID компонента.
     */
    private ProductComponentItemDto findItemByComponentId(Long componentId) {
        for (ProductComponentItemDto item : componentsController.getItems()) {
            if (item.getComponentId().equals(componentId)) {
                return item;
            }
        }
        return null;
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
     * Используется для FAN.
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

    public void clear() {
        pendingChanges.clear();
    }
}

package com.fanproduction.gui.manager;

import com.fanproduction.gui.controller.CardFormController;
import com.fanproduction.gui.controller.ProductMaterialsController;
import com.fanproduction.gui.dto.ProductMaterialItemDto;
import com.fanproduction.gui.dto.change.MaterialChange;
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
 * Менеджер изменений по материалам.
 * <p>
 * Работает в двух режимах:
 * <ul>
 *   <li><b>Локальный (для FAN):</b> изменения копятся, при сохранении
 *       формируется {@link ProductFullRequestDto} через {@link #buildRequest(ProductFullRequestDto)}.</li>
 *   <li><b>Старый (для системных карточек):</b> изменения применяются
 *       через {@link #applyChanges(Long)} (это будет удалено в 8.6.c).</li>
 * </ul>
 */
public class MaterialChangeManager {

    private final ProductMaterialsController materialsController;
    private final CardFormController parentController;
    private final List<MaterialChange> pendingChanges = new ArrayList<>();

    public MaterialChangeManager(ProductMaterialsController materialsController,
                                 CardFormController parentController) {
        this.materialsController = materialsController;
        this.parentController = parentController;
    }

    // ===================== ЛОКАЛЬНЫЕ ОПЕРАЦИИ =====================

    public void addLocal(Long materialId, Double quantity, String note) {
        if (materialId == null) return;
        ProductMaterialItemDto newItem = new ProductMaterialItemDto();
        newItem.setMaterialId(materialId);
        newItem.setQuantityPerUnit(quantity != null ? quantity : 1.0);
        newItem.setNote(note);
        newItem.setName("Загрузка...");
        materialsController.addItem(newItem);
        pendingChanges.add(new MaterialChange(MaterialChange.Type.ADD, materialId, quantity, note));
        parentController.loadMaterialNameAndUpdate(materialId, newItem);
    }

    public void removeLocal(Long materialId) {
        if (materialId == null) return;
        ProductMaterialItemDto toRemove = null;
        for (ProductMaterialItemDto item : materialsController.getItems()) {
            if (item.getMaterialId().equals(materialId)) {
                toRemove = item;
                break;
            }
        }
        if (toRemove == null) return;
        materialsController.removeItem(toRemove);
        pendingChanges.removeIf(change -> change.type == MaterialChange.Type.ADD && change.materialId.equals(materialId));
        pendingChanges.add(new MaterialChange(MaterialChange.Type.REMOVE, materialId, null, null));
    }

    public void updateQuantityLocal(Long materialId, Double newQuantity) {
        if (materialId == null || newQuantity == null) return;
        for (ProductMaterialItemDto item : materialsController.getItems()) {
            if (item.getMaterialId().equals(materialId)) {
                item.setQuantityPerUnit(newQuantity);
                materialsController.refreshItem(item);
                break;
            }
        }
        for (MaterialChange change : pendingChanges) {
            if (change.type == MaterialChange.Type.ADD && change.materialId.equals(materialId)) {
                change.quantity = newQuantity;
                return;
            }
        }
        pendingChanges.add(new MaterialChange(MaterialChange.Type.UPDATE_QUANTITY, materialId, newQuantity, null));
    }

    public void updateNoteLocal(Long materialId, String newNote) {
        if (materialId == null) return;
        for (ProductMaterialItemDto item : materialsController.getItems()) {
            if (item.getMaterialId().equals(materialId)) {
                item.setNote(newNote);
                materialsController.refreshItem(item);
                break;
            }
        }
        for (MaterialChange change : pendingChanges) {
            if (change.type == MaterialChange.Type.ADD && change.materialId.equals(materialId)) {
                change.note = newNote;
                return;
            }
        }
        pendingChanges.add(new MaterialChange(MaterialChange.Type.UPDATE_NOTE, materialId, null, newNote));
    }

    // ===================== ФОРМИРОВАНИЕ ЗАПРОСА (для FAN) =====================

    /**
     * Собрать данные для атомарного запроса.
     * <p>
     * Аналог {@code ComponentChangeManager.buildRequest}.
     */
    public void buildRequest(ProductFullRequestDto target) {
        List<ProductFullRequestDto.MaterialItemDto> materials = new ArrayList<>();
        for (ProductMaterialItemDto item : materialsController.getItems()) {
            if (item.getMaterialId() == null) continue;
            materials.add(new ProductFullRequestDto.MaterialItemDto(
                    item.getMaterialId(),
                    item.getQuantityPerUnit() != null ? item.getQuantityPerUnit() : 1.0,
                    item.getNote()
            ));
        }

        List<Long> toRemove = new ArrayList<>();
        for (MaterialChange change : pendingChanges) {
            if (change.type == MaterialChange.Type.REMOVE) {
                toRemove.add(change.materialId);
            }
        }

        target.setMaterials(materials);
        target.setMaterialsToRemove(toRemove);

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
        List<MaterialChange> changes = new ArrayList<>(pendingChanges);
        pendingChanges.clear();

        changes.sort((a, b) -> {
            if (a.type == MaterialChange.Type.REMOVE && b.type != MaterialChange.Type.REMOVE) return -1;
            if (a.type != MaterialChange.Type.REMOVE && b.type == MaterialChange.Type.REMOVE) return 1;
            return 0;
        });

        for (MaterialChange change : changes) {
            try {
                switch (change.type) {
                    case REMOVE:
                        ApiClient.delete("/materials/product/" + cardId + "/" + change.materialId);
                        break;
                    case ADD:
                        Map<String, Object> req = new HashMap<>();
                        req.put("materialId", change.materialId);
                        req.put("quantityPerUnit", change.quantity != null ? change.quantity : 1.0);
                        if (change.note != null && !change.note.isEmpty()) req.put("note", change.note);
                        ApiClient.post("/materials/product/" + cardId, req,
                                new TypeReference<ApiResponse<Map<String, Object>>>() {});
                        break;
                    case UPDATE_QUANTITY:
                        Map<String, Double> qty = new HashMap<>();
                        qty.put("quantityPerUnit", change.quantity);
                        ApiClient.put("/materials/product/" + cardId + "/" + change.materialId + "/quantity", qty,
                                new TypeReference<ApiResponse<Void>>() {});
                        break;
                    case UPDATE_NOTE:
                        Map<String, String> noteMap = new HashMap<>();
                        noteMap.put("note", change.note != null ? change.note : "");
                        ApiClient.put("/materials/product/" + cardId + "/" + change.materialId + "/note", noteMap,
                                new TypeReference<ApiResponse<Void>>() {});
                        break;
                }
            } catch (Exception e) {
                System.err.println("Failed to apply material change: " + e.getMessage());
                parentController.showAlert("Ошибка", "Не удалось применить изменение материала: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    public void clear() {
        pendingChanges.clear();
    }
}

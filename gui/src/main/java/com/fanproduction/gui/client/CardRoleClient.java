package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.CardRoleDto;
import com.fanproduction.gui.dto.response.CardSelectionDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Клиент выбора комплектующих по роли (US10.7.2).
 * <p>
 * Заменяет прежние клиенты колёс: селектор спрашивает «все осевые колёса»,
 * а сервер находит карточки всех типов с ролью {@code AXIAL_WHEEL}.
 */
public final class CardRoleClient {

    private static final String BASE_PATH = "/card-roles";

    private CardRoleClient() {
    }

    /**
     * Карточки всех типов с указанной ролью.
     */
    public static List<CardSelectionDto> getCardsByRole(String roleCode) throws Exception {
        TypeReference<ApiResponse<List<CardSelectionDto>>> typeRef = new TypeReference<>() {
        };
        ApiResponse<List<CardSelectionDto>> response =
                ApiClient.get(BASE_PATH + "/" + roleCode + "/cards", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить карточки по роли: " + response.getMessage());
    }
    /**
     * Все роли продукции (системные и пользовательские).
     */
    public static List<CardRoleDto> getAll() throws Exception {
        TypeReference<ApiResponse<List<CardRoleDto>>> typeRef = new TypeReference<>() {
        };
        ApiResponse<List<CardRoleDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить роли: " + response.getMessage());
    }

    /**
     * Создать пользовательскую роль.
     */
    public static CardRoleDto create(String code, String name) throws Exception {
        TypeReference<ApiResponse<CardRoleDto>> typeRef = new TypeReference<>() {
        };
        ApiResponse<CardRoleDto> response =
                ApiClient.post(BASE_PATH, new CreateRoleRequest(code, name), typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException(response.getMessage());
    }

    /**
     * Назначить роль типу продукции; {@code roleId = null} — снять роль.
     */
    public static void assignRole(Long typeId, Long roleId) throws Exception {
        String path = "/product-types/" + typeId + "/role"
                + (roleId != null ? "?cardRoleId=" + roleId : "");
        TypeReference<ApiResponse<com.fanproduction.gui.dto.response.ProductTypeDto>> typeRef =
                new TypeReference<>() {
                };
        ApiResponse<com.fanproduction.gui.dto.response.ProductTypeDto> response =
                ApiClient.put(path, null, typeRef);
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMessage());
        }
    }

    /** Тело запроса создания роли. */
    public record CreateRoleRequest(String code, String name) {
    }
}
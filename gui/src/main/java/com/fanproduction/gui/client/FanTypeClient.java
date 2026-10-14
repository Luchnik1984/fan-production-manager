package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.FanTypeShortDto;

import java.util.List;

/**
 * Клиент для REST API типов вентиляторов.
 */
public class FanTypeClient {

    private static final String BASE_PATH = "/fan-types";

    /** Кэш краткого списка (загружается один раз). */
    private static List<FanTypeShortDto> cachedShortList = null;

    /**
     * Краткий список типов для ComboBox.
     * <p>
     * Кэшируется в памяти после первой загрузки.
     */
    public static synchronized List<FanTypeShortDto> getShortList() throws Exception {
        if (cachedShortList != null) {
            return cachedShortList;
        }
        TypeReference<ApiResponse<List<FanTypeShortDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanTypeShortDto>> response = ApiClient.get(
                BASE_PATH + "/short-list", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            cachedShortList = response.getData();
            return cachedShortList;
        }
        throw new RuntimeException("Не удалось загрузить типы вентиляторов: "
                + response.getMessage());
    }

    /**
     * Сбросить кэш (например, после редактирования типов).
     */
    public static synchronized void clearCache() {
        cachedShortList = null;
    }
}

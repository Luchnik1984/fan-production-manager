package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.DynamicFormMetadataDto;

/**
 * Клиент для работы с REST API версий шаблонов FAN.
 * <p>
 * Использует {@link ApiClient} для HTTP-вызовов.
 */
public class FanTemplateVersionClient {

    private static final String BASE_PATH = "/fan-template-versions";

    /**
     * Получить метаданные формы по версии шаблона.
     *
     * @param templateVersionId ID версии шаблона
     * @return метаданные формы
     * @throws RuntimeException при ошибке или если ответ неуспешен
     */
    public static DynamicFormMetadataDto getFormMetadata(Long templateVersionId) throws Exception {
        TypeReference<ApiResponse<DynamicFormMetadataDto>> typeRef = new TypeReference<>() {};

        ApiResponse<DynamicFormMetadataDto> response = ApiClient.get(
                BASE_PATH + "/" + templateVersionId + "/form",
                typeRef
        );

        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }

        throw new RuntimeException("Не удалось загрузить метаданные формы: " + response.getMessage());
    }

    /**
     * Получить версию шаблона по ID (для чтения статуса, номера).
     */
    public static ApiResponse<Object> getVersionById(Long templateVersionId) throws Exception {
        TypeReference<ApiResponse<Object>> typeRef = new TypeReference<>() {};
        return ApiClient.get(BASE_PATH + "/" + templateVersionId, typeRef);
    }
}

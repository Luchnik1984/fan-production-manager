package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.MotorDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MotorClient {
    @SuppressWarnings("unchecked")

    public static List<MotorDto> getAll() throws Exception {
        TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
        ApiResponse<Map<String, Object>> response = ApiClient.get("/products?cardType=MOTOR&size=1000", typeRef);

        List<MotorDto> result = new ArrayList<>();
        if (response.isSuccess() && response.getData() != null) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getData().get("content");
            for (Map<String, Object> item : content) {
                MotorDto dto = new MotorDto();
                dto.setId(((Number) item.get("id")).longValue());

                Map<String, Object> fields = (Map<String, Object>) item.get("fields");
                dto.setSeries((String) fields.get("series"));
                dto.setMotorType((String) fields.get("motorType"));
                dto.setPoles(fields.get("poles") != null ? ((Number) fields.get("poles")).intValue() : null);
                dto.setPowerKw(fields.get("powerKw") != null ? ((Number) fields.get("powerKw")).doubleValue() : null);
                dto.setRatedSpeedRpm(fields.get("ratedSpeedRpm") != null ? ((Number) fields.get("ratedSpeedRpm")).intValue() : null);
                dto.setActualSpeedRpm(fields.get("actualSpeedRpm") != null ? ((Number) fields.get("actualSpeedRpm")).intValue() : null);
                dto.setMountingType((String) fields.get("mountingType"));
                dto.setVoltage(fields.get("voltage") != null ? ((Number) fields.get("voltage")).intValue() : null);
                dto.setFullMarking((String) fields.get("fullMarking"));

                result.add(dto);
            }
        }
        return result;
    }
}

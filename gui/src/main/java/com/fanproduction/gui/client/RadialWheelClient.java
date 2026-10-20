package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.RadialWheelDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RadialWheelClient {
    @SuppressWarnings("unchecked")

    public static List<RadialWheelDto> getAll() throws Exception {
        TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
        ApiResponse<Map<String, Object>> response = ApiClient.get("/products?cardType=RADIAL_WHEEL&size=1000", typeRef);

        List<RadialWheelDto> result = new ArrayList<>();
        if (response.isSuccess() && response.getData() != null) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getData().get("content");
            for (Map<String, Object> item : content) {
                RadialWheelDto dto = new RadialWheelDto();
                dto.setId(((Number) item.get("id")).longValue());

                Map<String, Object> fields = (Map<String, Object>) item.get("fields");
                dto.setManufacturer((String) fields.get("manufacturer"));
                dto.setMarking((String) fields.get("marking"));
                dto.setBladeType((String) fields.get("bladeType"));
                dto.setSize(fields.get("size") != null ? ((Number) fields.get("size")).doubleValue() : null);
                dto.setHubType((String) fields.get("hubType"));
                dto.setWeightKg(fields.get("weightKg") != null ? ((Number) fields.get("weightKg")).doubleValue() : null);
                dto.setFullMarking((String) fields.get("fullMarking"));

                result.add(dto);
            }
        }
        return result;
    }

    public static RadialWheelDto getById(Long id) throws Exception {
        if (id == null) return null;

        var response = com.fanproduction.gui.client.ProductCardClient.getCardById(id);
        if (response == null || !response.isSuccess() || response.getData() == null) {
            return null;
        }

        com.fanproduction.gui.dto.response.ProductCardDto card = response.getData();
        if (!"RADIAL_WHEEL".equals(card.getCardType())) {
            return null;
        }

        RadialWheelDto dto = new RadialWheelDto();
        dto.setId(card.getId());
        dto.setName(card.getName());

        Map<String, Object> fields = card.getFields();
        if (fields == null) return dto;

        dto.setManufacturer((String) fields.get("manufacturer"));
        dto.setMarking((String) fields.get("marking"));
        dto.setBladeType((String) fields.get("bladeType"));
        dto.setSize(fields.get("size") != null ? ((Number) fields.get("size")).doubleValue() : null);
        dto.setHubType((String) fields.get("hubType"));
        dto.setWeightKg(fields.get("weightKg") != null ? ((Number) fields.get("weightKg")).doubleValue() : null);
        dto.setFullMarking((String) fields.get("fullMarking"));

        return dto;
    }
}
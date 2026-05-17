package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.AxialWheelDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AxialWheelClient {

    @SuppressWarnings("unchecked")
    public static List<AxialWheelDto> getAll() throws Exception {
        TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
        ApiResponse<Map<String, Object>> response = ApiClient.get("/products?cardType=AXIAL_WHEEL&size=1000", typeRef);

        List<AxialWheelDto> result = new ArrayList<>();
        if (response.isSuccess() && response.getData() != null) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getData().get("content");
            for (Map<String, Object> item : content) {
                AxialWheelDto dto = new AxialWheelDto();
                dto.setId(((Number) item.get("id")).longValue());

                Map<String, Object> fields = (Map<String, Object>) item.get("fields");
                dto.setManufacturer((String) fields.get("manufacturer"));
                dto.setMarking((String) fields.get("marking"));
                dto.setBladeType((String) fields.get("bladeType"));
                dto.setSize(fields.get("size") != null ? ((Number) fields.get("size")).doubleValue() : null);
                dto.setExecution((String) fields.get("execution"));
                dto.setTrimCoefficient(fields.get("trimCoefficient") != null ? ((Number) fields.get("trimCoefficient")).doubleValue() : null);
                dto.setHubType((String) fields.get("hubType"));
                dto.setBladeCount(fields.get("bladeCount") != null ? ((Number) fields.get("bladeCount")).intValue() : null);
                dto.setBladeSlots(fields.get("bladeSlots") != null ? ((Number) fields.get("bladeSlots")).intValue() : null);
                dto.setBladeShape((String) fields.get("bladeShape"));
                dto.setBladeAngle(fields.get("bladeAngle") != null ? ((Number) fields.get("bladeAngle")).intValue() : null);
                dto.setBladeMaterial((String) fields.get("bladeMaterial"));
                dto.setWheelDiameter(fields.get("wheelDiameter") != null ? ((Number) fields.get("wheelDiameter")).intValue() : null);
                dto.setWheelFormula((String) fields.get("wheelFormula"));
                dto.setGeneralPurpose(fields.get("generalPurpose") != null ? (Boolean) fields.get("generalPurpose") : null);
                dto.setFireproof(fields.get("fireproof") != null ? (Boolean) fields.get("fireproof") : null);
                dto.setFireproofMarking((String) fields.get("fireproofMarking"));
                dto.setMaxTemperature(fields.get("maxTemperature") != null ? ((Number) fields.get("maxTemperature")).intValue() : null);
                dto.setExplosionProof(fields.get("explosionProof") != null ? (Boolean) fields.get("explosionProof") : null);
                dto.setExplosionMarking((String) fields.get("explosionMarking"));
                dto.setFullMarking((String) fields.get("fullMarking"));

                result.add(dto);
            }
        }
        return result;
    }
}


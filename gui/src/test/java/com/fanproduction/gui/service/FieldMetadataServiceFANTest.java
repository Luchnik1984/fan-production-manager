package com.fanproduction.gui.service;

import com.fanproduction.gui.dto.metadata.FieldMetadataDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты загрузки полей FAN с реального API.
 * <p>
 * <b>Внимание:</b> это интеграционный тест — требует запущенного API
 * и созданного шаблона. Помечен {@code @Disabled} по умолчанию.
 * Для запуска: раскомментировать аннотацию и указать реальный
 * {@code templateVersionId}.
 */
@DisplayName("FieldMetadataService — загрузка полей FAN")
class FieldMetadataServiceFANTest {

    private FieldMetadataService service;

    @BeforeEach
    void setUp() {
        service = new FieldMetadataService();
    }

    @Test
    @DisplayName("(интеграционный) Загрузка полей FAN по версии шаблона")
    @org.junit.jupiter.api.Disabled("Требует запущенного API и реального templateVersionId")
    void loadFanFieldsFromApi() {
        // Укажи реальный ID опубликованной версии шаблона
        Long templateVersionId = 3L;

        List<FieldMetadataDto> fields = service.getFieldsForFan(templateVersionId);

        assertThat(fields).isNotEmpty();
        // ... проверить ожидаемые поля
    }
}

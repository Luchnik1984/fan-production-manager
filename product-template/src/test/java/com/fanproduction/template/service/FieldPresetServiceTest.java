package com.fanproduction.template.service;

import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.FieldPresetDto;
import com.fanproduction.template.dto.ProductTemplateVersionDto;
import com.fanproduction.template.entity.FieldPreset;
import com.fanproduction.template.entity.ProductTemplateVersion;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.repository.FieldPresetRepository;
import com.fanproduction.template.repository.ProductTemplateVersionRepository;
import com.fanproduction.template.service.impl.FieldPresetServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ApplicationEventMulticaster;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты пресетов полей (US10.6).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FieldPresetService")
class FieldPresetServiceTest {

    @Mock
    private FieldPresetRepository fieldPresetRepository;

    @Mock
    private ProductTemplateVersionRepository productTemplateVersionRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FieldPresetServiceImpl service;

    private static FieldDefinition field(String key) {
        return new FieldDefinition(key, key.toUpperCase(), FieldType.TEXT, false,
                null, null, null, null, null, null,
                null, null, null, null, null, 1, null);
    }

    private static FieldPreset preset(Long id, String name, boolean system, List<FieldDefinition> fields) {
        FieldPreset p = new FieldPreset();
        p.setId(id);
        p.setName(name);
        p.setIsSystem(system);
        p.setFieldsJson(fields);
        return p;
    }

    private static ProductTemplateVersion version(TemplateStatus status, List<FieldDefinition> fields) {
        ProductTemplateVersion v = new ProductTemplateVersion();
        v.setId(100L);
        v.setTemplateId(1L);
        v.setVersion(2);
        v.setStatus(status);
        v.setFieldsJson(new ArrayList<>(fields));
        v.setMarkingRuleJson(MarkingRule.empty());
        return v;
    }

    @Nested
    @DisplayName("Применение пресета")
    class Apply {

        @Test
        @DisplayName("добавляет только отсутствующие поля и не трогает существующие")
        void addsOnlyMissing() {
            ProductTemplateVersion v = version(TemplateStatus.DRAFT, List.of(field("series")));
            FieldPreset p = preset(1L, "Осевое колесо", true,
                    List.of(field("series"), field("size"), field("bladeCount")));

            when(productTemplateVersionRepository.findById(100L)).thenReturn(Optional.of(v));
            when(fieldPresetRepository.findById(1L)).thenReturn(Optional.of(p));
            when(productTemplateVersionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateVersionDto result = service.applyToVersion(100L, 1L, "admin@test");

            assertThat(result.fieldsJson()).extracting(FieldDefinition::key)
                    .containsExactly("series", "size", "bladeCount");
        }

        @Test
        @DisplayName("повторное применение идемпотентно")
        void idempotent() {
            ProductTemplateVersion v = version(TemplateStatus.DRAFT,
                    List.of(field("series"), field("size")));
            FieldPreset p = preset(1L, "Пресет", false, List.of(field("series"), field("size")));

            when(productTemplateVersionRepository.findById(100L)).thenReturn(Optional.of(v));
            when(fieldPresetRepository.findById(1L)).thenReturn(Optional.of(p));
            when(productTemplateVersionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateVersionDto result = service.applyToVersion(100L, 1L, "admin@test");

            assertThat(result.fieldsJson()).hasSize(2);
        }

        @Test
        @DisplayName("к не-DRAFT версии применять нельзя")
        void nonDraft_throws() {
            when(productTemplateVersionRepository.findById(100L))
                    .thenReturn(Optional.of(version(TemplateStatus.PUBLISHED, List.of())));

            assertThatThrownBy(() -> service.applyToVersion(100L, 1L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("DRAFT");
        }

        @Test
        @DisplayName("несуществующий пресет — ошибка")
        void presetNotFound_throws() {
            when(productTemplateVersionRepository.findById(100L))
                    .thenReturn(Optional.of(version(TemplateStatus.DRAFT, List.of())));
            when(fieldPresetRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.applyToVersion(100L, 99L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("Пресет не найден");
        }
    }

    @Nested
    @DisplayName("Системные пресеты")
    class SystemPresets {

        @Test
        @DisplayName("системный пресет нельзя удалить")
        void cannotDelete() {
            when(fieldPresetRepository.findById(1L))
                    .thenReturn(Optional.of(preset(1L, "Осевое колесо", true, List.of(field("a")))));

            assertThatThrownBy(() -> service.delete(1L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("системный");

            verify(fieldPresetRepository, never()).delete(any());
        }

        @Test
        @DisplayName("системный пресет нельзя изменить")
        void cannotUpdate() {
            when(fieldPresetRepository.findById(1L))
                    .thenReturn(Optional.of(preset(1L, "Осевое колесо", true, List.of(field("a")))));

            assertThatThrownBy(() -> service.update(1L, "Новое имя", null, null, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("системный");
        }
    }

    @Nested
    @DisplayName("Создание")
    class Create {

        @Test
        @DisplayName("дубль названия — ошибка")
        void duplicateName_throws() {
            when(fieldPresetRepository.existsByNameIgnoreCase("Осевое колесо")).thenReturn(true);

            assertThatThrownBy(() -> service.create("Осевое колесо", null, List.of(field("a")), "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("уже существует");

            verify(fieldPresetRepository, never()).save(any());
        }

        @Test
        @DisplayName("пустой набор полей — ошибка")
        void emptyFields_throws() {
            when(fieldPresetRepository.existsByNameIgnoreCase("Пустой")).thenReturn(false);

            assertThatThrownBy(() -> service.create("Пустой", null, List.of(), "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("хотя бы одно поле");
        }

        @Test
        @DisplayName("пользовательский пресет создаётся")
        void createsCustom() {
            when(fieldPresetRepository.existsByNameIgnoreCase("Мой пресет")).thenReturn(false);
            when(fieldPresetRepository.save(any(FieldPreset.class))).thenAnswer(inv -> {
                FieldPreset p = inv.getArgument(0);
                p.setId(5L);
                return p;
            });

            FieldPresetDto created = service.create("Мой пресет", "описание",
                    List.of(field("a"), field("b")), "admin@test");

            assertThat(created.id()).isEqualTo(5L);
            assertThat(created.isSystem()).isFalse();
            assertThat(created.fields()).hasSize(2);
        }
    }
}

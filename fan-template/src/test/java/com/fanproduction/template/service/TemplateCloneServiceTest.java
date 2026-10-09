package com.fanproduction.template.service;

import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.FanTemplateDto;
import com.fanproduction.template.entity.FanTemplate;
import com.fanproduction.template.entity.FanTemplateVersion;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.repository.FanSeriesRepository;
import com.fanproduction.template.repository.FanTemplateRepository;
import com.fanproduction.template.repository.FanTemplateVersionRepository;
import com.fanproduction.template.service.impl.TemplateCloneServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты сервиса клонирования шаблонов.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateCloneService")
class TemplateCloneServiceTest {

    @Mock
    private FanTemplateRepository fanTemplateRepository;

    @Mock
    private FanTemplateVersionRepository fanTemplateVersionRepository;

    @Mock
    private FanSeriesRepository fanSeriesRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TemplateCloneServiceImpl service;

    private FanTemplate sourceTemplate;
    private FanTemplateVersion sourceVersion;

    @BeforeEach
    void setUp() {
        sourceTemplate = new FanTemplate();
        sourceTemplate.setId(100L);
        sourceTemplate.setSeriesId(1L);
        sourceTemplate.setName("Шаблон P-серии");
        sourceTemplate.setDescription("Описание исходного");
        sourceTemplate.setCurrentVersionId(200L);

        sourceVersion = new FanTemplateVersion();
        sourceVersion.setId(200L);
        sourceVersion.setTemplateId(100L);
        sourceVersion.setVersion(2);
        sourceVersion.setStatus(TemplateStatus.PUBLISHED);
        sourceVersion.setFieldsJson(List.of(
                new FieldDefinition("series", "Серия", FieldType.TEXT,
                        false, null, null, null, null, null, null,
                        null, null, null, null, null, 1, null)
        ));
        sourceVersion.setMarkingRuleJson(MarkingRule.empty());
    }

    // ==========================================================
    // УСПЕШНОЕ КЛОНИРОВАНИЕ
    // ==========================================================

    @Nested
    @DisplayName("Успешное клонирование")
    class SuccessTests {

        @Test
        @DisplayName("Клонирование через current_version_id")
        void cloneFromCurrentVersion() {
            when(fanTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(fanSeriesRepository.existsById(2L)).thenReturn(true);
            when(fanTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(fanTemplateVersionRepository.findById(200L))
                    .thenReturn(Optional.of(sourceVersion));
            when(fanTemplateRepository.save(any(FanTemplate.class)))
                    .thenAnswer(inv -> {
                        FanTemplate t = inv.getArgument(0);
                        t.setId(500L);
                        return t;
                    });
            when(fanTemplateVersionRepository.save(any(FanTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            FanTemplateDto result = service.cloneTemplate(100L, 2L, null, "admin@test");

            assertThat(result.id()).isEqualTo(500L);
            assertThat(result.seriesId()).isEqualTo(2L);
            assertThat(result.name()).isEqualTo("Шаблон P-серии");
            assertThat(result.description()).isEqualTo("Описание исходного");
        }

        @Test
        @DisplayName("Кастомное имя нового шаблона")
        void cloneWithCustomName() {
            when(fanTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(fanSeriesRepository.existsById(2L)).thenReturn(true);
            when(fanTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(fanTemplateVersionRepository.findById(200L))
                    .thenReturn(Optional.of(sourceVersion));
            when(fanTemplateRepository.save(any(FanTemplate.class)))
                    .thenAnswer(inv -> {
                        FanTemplate t = inv.getArgument(0);
                        t.setId(500L);
                        return t;
                    });
            when(fanTemplateVersionRepository.save(any(FanTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            FanTemplateDto result = service.cloneTemplate(
                    100L, 2L, "Шаблон PS-серии", "admin@test");

            assertThat(result.name()).isEqualTo("Шаблон PS-серии");
        }

        @Test
        @DisplayName("DRAFT-версия используется, если current_version_id = null")
        void cloneFromDraft() {
            sourceTemplate.setCurrentVersionId(null);

            FanTemplateVersion draft = new FanTemplateVersion();
            draft.setId(300L);
            draft.setTemplateId(100L);
            draft.setVersion(3);
            draft.setStatus(TemplateStatus.DRAFT);
            draft.setFieldsJson(sourceVersion.getFieldsJson());
            draft.setMarkingRuleJson(sourceVersion.getMarkingRuleJson());

            when(fanTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(fanSeriesRepository.existsById(2L)).thenReturn(true);
            when(fanTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(fanTemplateVersionRepository.findFirstByTemplateIdAndStatus(
                    100L, TemplateStatus.DRAFT)).thenReturn(Optional.of(draft));
            when(fanTemplateRepository.save(any(FanTemplate.class)))
                    .thenAnswer(inv -> {
                        FanTemplate t = inv.getArgument(0);
                        t.setId(500L);
                        return t;
                    });
            when(fanTemplateVersionRepository.save(any(FanTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            FanTemplateDto result = service.cloneTemplate(100L, 2L, null, "admin@test");

            assertThat(result.id()).isEqualTo(500L);
        }

        @Test
        @DisplayName("Поля и правило копируются в новую DRAFT-версию")
        void fieldsAndRuleCopiedToNewVersion() {
            MarkingRule rule = MarkingRule.empty();

            when(fanTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(fanSeriesRepository.existsById(2L)).thenReturn(true);
            when(fanTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(fanTemplateVersionRepository.findById(200L))
                    .thenReturn(Optional.of(sourceVersion));
            when(fanTemplateRepository.save(any(FanTemplate.class)))
                    .thenAnswer(inv -> {
                        FanTemplate t = inv.getArgument(0);
                        t.setId(500L);
                        return t;
                    });
            when(fanTemplateVersionRepository.save(any(FanTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.cloneTemplate(100L, 2L, null, "admin@test");

            ArgumentCaptor<FanTemplateVersion> captor =
                    ArgumentCaptor.forClass(FanTemplateVersion.class);
            verify(fanTemplateVersionRepository).save(captor.capture());

            FanTemplateVersion saved = captor.getValue();
            assertThat(saved.getTemplateId()).isEqualTo(500L);
            assertThat(saved.getVersion()).isEqualTo(1);
            assertThat(saved.getStatus()).isEqualTo(TemplateStatus.DRAFT);
            assertThat(saved.getFieldsJson()).isEqualTo(sourceVersion.getFieldsJson());
            assertThat(saved.getMarkingRuleJson()).isEqualTo(sourceVersion.getMarkingRuleJson());
        }

        @Test
        @DisplayName("Аудит публикуется")
        void auditPublished() {
            when(fanTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(fanSeriesRepository.existsById(2L)).thenReturn(true);
            when(fanTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(fanTemplateVersionRepository.findById(200L))
                    .thenReturn(Optional.of(sourceVersion));
            when(fanTemplateRepository.save(any(FanTemplate.class)))
                    .thenAnswer(inv -> {
                        FanTemplate t = inv.getArgument(0);
                        t.setId(500L);
                        return t;
                    });
            when(fanTemplateVersionRepository.save(any(FanTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.cloneTemplate(100L, 2L, null, "admin@test");

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            AuditEvent event = captor.getValue();
            assertThat(event.getUsername()).isEqualTo("admin@test");
            assertThat(event.getAction().name()).isEqualTo("TEMPLATE_CLONE");
            assertThat(event.getDetails()).contains("Клонирован шаблон");
        }
    }

    // ==========================================================
    // ОШИБКИ
    // ==========================================================

    @Nested
    @DisplayName("Ошибки")
    class ErrorTests {

        @Test
        @DisplayName("Источник не найден → исключение")
        void sourceNotFound() {
            when(fanTemplateRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cloneTemplate(999L, 2L, null, "admin"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("Шаблон-источник не найден");

            verify(fanTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Целевая серия не найдена → исключение")
        void targetSeriesNotFound() {
            when(fanTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(fanSeriesRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.cloneTemplate(100L, 999L, null, "admin"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("Целевая серия не найдена");

            verify(fanTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("У целевой серии уже есть шаблон → исключение")
        void targetSeriesHasTemplate() {
            when(fanTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(fanSeriesRepository.existsById(2L)).thenReturn(true);
            when(fanTemplateRepository.existsBySeriesId(2L)).thenReturn(true);

            assertThatThrownBy(() -> service.cloneTemplate(100L, 2L, null, "admin"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("уже есть шаблон");

            verify(fanTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("У источника нет версий → исключение")
        void sourceHasNoVersions() {
            sourceTemplate.setCurrentVersionId(null);

            when(fanTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(fanSeriesRepository.existsById(2L)).thenReturn(true);
            when(fanTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(fanTemplateVersionRepository.findFirstByTemplateIdAndStatus(
                    100L, TemplateStatus.DRAFT)).thenReturn(Optional.empty());
            when(fanTemplateVersionRepository.findByTemplateIdOrderByVersionDesc(100L))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> service.cloneTemplate(100L, 2L, null, "admin"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("нет ни одной версии");

            verify(fanTemplateRepository, never()).save(any());
        }
    }
}

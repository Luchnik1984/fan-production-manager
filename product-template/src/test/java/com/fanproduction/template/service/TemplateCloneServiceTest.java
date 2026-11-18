package com.fanproduction.template.service;

import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.ProductTemplateDto;
import com.fanproduction.template.entity.ProductTemplate;
import com.fanproduction.template.entity.ProductTemplateVersion;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.repository.ProductSeriesRepository;
import com.fanproduction.template.repository.ProductTemplateRepository;
import com.fanproduction.template.repository.ProductTemplateVersionRepository;
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
    private ProductTemplateRepository productTemplateRepository;

    @Mock
    private ProductTemplateVersionRepository productTemplateVersionRepository;

    @Mock
    private ProductSeriesRepository productSeriesRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TemplateCloneServiceImpl service;

    private ProductTemplate sourceTemplate;
    private ProductTemplateVersion sourceVersion;

    @BeforeEach
    void setUp() {
        sourceTemplate = new ProductTemplate();
        sourceTemplate.setId(100L);
        sourceTemplate.setSeriesId(1L);
        sourceTemplate.setName("Шаблон P-серии");
        sourceTemplate.setDescription("Описание исходного");
        sourceTemplate.setCurrentVersionId(200L);

        sourceVersion = new ProductTemplateVersion();
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
            when(productTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(productSeriesRepository.existsById(2L)).thenReturn(true);
            when(productTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(productTemplateVersionRepository.findById(200L))
                    .thenReturn(Optional.of(sourceVersion));
            when(productTemplateRepository.save(any(ProductTemplate.class)))
                    .thenAnswer(inv -> {
                        ProductTemplate t = inv.getArgument(0);
                        t.setId(500L);
                        return t;
                    });
            when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateDto result = service.cloneTemplate(100L, 2L, null, "admin@test");

            assertThat(result.id()).isEqualTo(500L);
            assertThat(result.seriesId()).isEqualTo(2L);
            assertThat(result.name()).isEqualTo("Шаблон P-серии");
            assertThat(result.description()).isEqualTo("Описание исходного");
        }

        @Test
        @DisplayName("Кастомное имя нового шаблона")
        void cloneWithCustomName() {
            when(productTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(productSeriesRepository.existsById(2L)).thenReturn(true);
            when(productTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(productTemplateVersionRepository.findById(200L))
                    .thenReturn(Optional.of(sourceVersion));
            when(productTemplateRepository.save(any(ProductTemplate.class)))
                    .thenAnswer(inv -> {
                        ProductTemplate t = inv.getArgument(0);
                        t.setId(500L);
                        return t;
                    });
            when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateDto result = service.cloneTemplate(
                    100L, 2L, "Шаблон PS-серии", "admin@test");

            assertThat(result.name()).isEqualTo("Шаблон PS-серии");
        }

        @Test
        @DisplayName("DRAFT-версия используется, если current_version_id = null")
        void cloneFromDraft() {
            sourceTemplate.setCurrentVersionId(null);

            ProductTemplateVersion draft = new ProductTemplateVersion();
            draft.setId(300L);
            draft.setTemplateId(100L);
            draft.setVersion(3);
            draft.setStatus(TemplateStatus.DRAFT);
            draft.setFieldsJson(sourceVersion.getFieldsJson());
            draft.setMarkingRuleJson(sourceVersion.getMarkingRuleJson());

            when(productTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(productSeriesRepository.existsById(2L)).thenReturn(true);
            when(productTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(productTemplateVersionRepository.findFirstByTemplateIdAndStatus(
                    100L, TemplateStatus.DRAFT)).thenReturn(Optional.of(draft));
            when(productTemplateRepository.save(any(ProductTemplate.class)))
                    .thenAnswer(inv -> {
                        ProductTemplate t = inv.getArgument(0);
                        t.setId(500L);
                        return t;
                    });
            when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateDto result = service.cloneTemplate(100L, 2L, null, "admin@test");

            assertThat(result.id()).isEqualTo(500L);
        }

        @Test
        @DisplayName("Поля и правило копируются в новую DRAFT-версию")
        void fieldsAndRuleCopiedToNewVersion() {

            when(productTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(productSeriesRepository.existsById(2L)).thenReturn(true);
            when(productTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(productTemplateVersionRepository.findById(200L))
                    .thenReturn(Optional.of(sourceVersion));
            when(productTemplateRepository.save(any(ProductTemplate.class)))
                    .thenAnswer(inv -> {
                        ProductTemplate t = inv.getArgument(0);
                        t.setId(500L);
                        return t;
                    });
            when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            service.cloneTemplate(100L, 2L, null, "admin@test");

            ArgumentCaptor<ProductTemplateVersion> captor =
                    ArgumentCaptor.forClass(ProductTemplateVersion.class);
            verify(productTemplateVersionRepository).save(captor.capture());

            ProductTemplateVersion saved = captor.getValue();
            assertThat(saved.getTemplateId()).isEqualTo(500L);
            assertThat(saved.getVersion()).isEqualTo(1);
            assertThat(saved.getStatus()).isEqualTo(TemplateStatus.DRAFT);
            assertThat(saved.getFieldsJson()).isEqualTo(sourceVersion.getFieldsJson());
            assertThat(saved.getMarkingRuleJson()).isEqualTo(sourceVersion.getMarkingRuleJson());
        }

        @Test
        @DisplayName("Аудит публикуется")
        void auditPublished() {
            when(productTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(productSeriesRepository.existsById(2L)).thenReturn(true);
            when(productTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(productTemplateVersionRepository.findById(200L))
                    .thenReturn(Optional.of(sourceVersion));
            when(productTemplateRepository.save(any(ProductTemplate.class)))
                    .thenAnswer(inv -> {
                        ProductTemplate t = inv.getArgument(0);
                        t.setId(500L);
                        return t;
                    });
            when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
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
            when(productTemplateRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.cloneTemplate(999L, 2L, null, "admin"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("Шаблон-источник не найден");

            verify(productTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Целевая серия не найдена → исключение")
        void targetSeriesNotFound() {
            when(productTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(productSeriesRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.cloneTemplate(100L, 999L, null, "admin"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("Целевая серия не найдена");

            verify(productTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("У целевой серии уже есть шаблон → исключение")
        void targetSeriesHasTemplate() {
            when(productTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(productSeriesRepository.existsById(2L)).thenReturn(true);
            when(productTemplateRepository.existsBySeriesId(2L)).thenReturn(true);

            assertThatThrownBy(() -> service.cloneTemplate(100L, 2L, null, "admin"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("уже есть шаблон");

            verify(productTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("У источника нет версий → исключение")
        void sourceHasNoVersions() {
            sourceTemplate.setCurrentVersionId(null);

            when(productTemplateRepository.findById(100L)).thenReturn(Optional.of(sourceTemplate));
            when(productSeriesRepository.existsById(2L)).thenReturn(true);
            when(productTemplateRepository.existsBySeriesId(2L)).thenReturn(false);
            when(productTemplateVersionRepository.findFirstByTemplateIdAndStatus(
                    100L, TemplateStatus.DRAFT)).thenReturn(Optional.empty());
            when(productTemplateVersionRepository.findByTemplateIdOrderByVersionDesc(100L))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> service.cloneTemplate(100L, 2L, null, "admin"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("нет ни одной версии");

            verify(productTemplateRepository, never()).save(any());
        }
    }
}

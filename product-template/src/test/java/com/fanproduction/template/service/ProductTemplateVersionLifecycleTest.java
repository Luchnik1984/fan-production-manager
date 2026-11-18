package com.fanproduction.template.service;

import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.ProductTemplateVersionDto;
import com.fanproduction.template.entity.ProductTemplate;
import com.fanproduction.template.entity.ProductTemplateVersion;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.repository.ProductTemplateRepository;
import com.fanproduction.template.repository.ProductTemplateVersionRepository;
import com.fanproduction.template.service.impl.ProductTemplateServiceImpl;
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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты обратимости жизненного цикла версий шаблонов (US10.3).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductTemplateService — обратимость версий")
class ProductTemplateVersionLifecycleTest {

    @Mock
    private ProductTemplateRepository productTemplateRepository;

    @Mock
    private ProductTemplateVersionRepository productTemplateVersionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TemplateValidationService templateValidationService;

    @InjectMocks
    private ProductTemplateServiceImpl service;

    private static final Long TEMPLATE_ID = 10L;

    private static ProductTemplateVersion version(Long id, TemplateStatus status, int number) {
        ProductTemplateVersion v = new ProductTemplateVersion();
        v.setId(id);
        v.setTemplateId(TEMPLATE_ID);
        v.setVersion(number);
        v.setStatus(status);
        v.setFieldsJson(List.of());
        v.setMarkingRuleJson(MarkingRule.empty());
        return v;
    }

    private static ProductTemplate template() {
        ProductTemplate t = new ProductTemplate();
        t.setId(TEMPLATE_ID);
        t.setName("Шаблон P-серии");
        t.setCurrentVersionId(100L);
        return t;
    }

    private AuditEvent captureAudit() {
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        return captor.getValue();
    }

    @Nested
    @DisplayName("restore: ARCHIVED → DEPRECATED")
    class Restore {

        @Test
        @DisplayName("архивированная версия возвращается в DEPRECATED")
        void archived_becomesDeprecated() {
            ProductTemplateVersion archived = version(1L, TemplateStatus.ARCHIVED, 2);
            when(productTemplateVersionRepository.findById(1L)).thenReturn(Optional.of(archived));
            when(productTemplateVersionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateVersionDto result = service.restoreVersion(1L, "admin@test");

            assertThat(result.status()).isEqualTo(TemplateStatus.DEPRECATED);
            assertThat(archived.getStatus()).isEqualTo(TemplateStatus.DEPRECATED);
        }

        @Test
        @DisplayName("версия не в ARCHIVED — ошибка")
        void nonArchived_throws() {
            when(productTemplateVersionRepository.findById(1L))
                    .thenReturn(Optional.of(version(1L, TemplateStatus.DEPRECATED, 2)));

            assertThatThrownBy(() -> service.restoreVersion(1L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("только ARCHIVED");

            verify(productTemplateVersionRepository, never()).save(any());
        }

        @Test
        @DisplayName("аудит пишется с действием TEMPLATE_RESTORE")
        void auditAction() {
            when(productTemplateVersionRepository.findById(1L))
                    .thenReturn(Optional.of(version(1L, TemplateStatus.ARCHIVED, 2)));
            when(productTemplateVersionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.restoreVersion(1L, "admin@test");

            assertThat(captureAudit().getAction()).isEqualTo(AuditAction.TEMPLATE_RESTORE);
        }
    }

    @Nested
    @DisplayName("un-deprecate: DEPRECATED → LEGACY")
    class Undeprecate {

        @Test
        @DisplayName("деприкейтнутая версия возвращается в LEGACY")
        void deprecated_becomesLegacy() {
            ProductTemplateVersion deprecated = version(2L, TemplateStatus.DEPRECATED, 1);
            when(productTemplateVersionRepository.findById(2L)).thenReturn(Optional.of(deprecated));
            when(productTemplateVersionRepository.findByTemplateIdAndStatusIn(anyLong(), any(Set.class)))
                    .thenReturn(List.of());
            when(productTemplateVersionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateVersionDto result = service.undeprecateVersion(2L, "admin@test");

            assertThat(result.status()).isEqualTo(TemplateStatus.LEGACY);
        }

        @Test
        @DisplayName("версия не в DEPRECATED — ошибка")
        void nonDeprecated_throws() {
            when(productTemplateVersionRepository.findById(2L))
                    .thenReturn(Optional.of(version(2L, TemplateStatus.ARCHIVED, 1)));

            assertThatThrownBy(() -> service.undeprecateVersion(2L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("только с DEPRECATED");
        }

        @Test
        @DisplayName("лимит активных версий исчерпан — ошибка")
        void activeLimitReached_throws() {
            when(productTemplateVersionRepository.findById(2L))
                    .thenReturn(Optional.of(version(2L, TemplateStatus.DEPRECATED, 1)));
            when(productTemplateVersionRepository.findByTemplateIdAndStatusIn(anyLong(), any(Set.class)))
                    .thenReturn(java.util.Collections.nCopies(20, version(3L, TemplateStatus.LEGACY, 5)));

            assertThatThrownBy(() -> service.undeprecateVersion(2L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("лимит");
        }

        @Test
        @DisplayName("аудит пишется с действием TEMPLATE_UN_DEPRECATE")
        void auditAction() {
            when(productTemplateVersionRepository.findById(2L))
                    .thenReturn(Optional.of(version(2L, TemplateStatus.DEPRECATED, 1)));
            when(productTemplateVersionRepository.findByTemplateIdAndStatusIn(anyLong(), any(Set.class)))
                    .thenReturn(List.of());
            when(productTemplateVersionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.undeprecateVersion(2L, "admin@test");

            assertThat(captureAudit().getAction()).isEqualTo(AuditAction.TEMPLATE_UN_DEPRECATE);
        }
    }

    @Nested
    @DisplayName("createVersionFrom: DRAFT на основе выбранной версии")
    class CreateFrom {

        @Test
        @DisplayName("поля и правило копируются из источника, номер = max + 1")
        void copiesFromSource() {
            ProductTemplateVersion source = version(50L, TemplateStatus.LEGACY, 1);

            when(productTemplateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template()));
            when(productTemplateVersionRepository.existsByTemplateIdAndStatus(TEMPLATE_ID, TemplateStatus.DRAFT))
                    .thenReturn(false);
            when(productTemplateVersionRepository.findById(50L)).thenReturn(Optional.of(source));
            when(productTemplateVersionRepository.findByTemplateIdAndStatusIn(anyLong(), any(Set.class)))
                    .thenReturn(List.of(source));
            when(productTemplateVersionRepository.findMaxVersion(TEMPLATE_ID)).thenReturn(Optional.of(3));
            when(productTemplateVersionRepository.save(any())).thenAnswer(inv -> {
                ProductTemplateVersion v = inv.getArgument(0);
                v.setId(99L);
                return v;
            });

            ProductTemplateVersionDto created = service.createVersionFrom(TEMPLATE_ID, 50L, "admin@test");

            assertThat(created.version()).isEqualTo(4);
            assertThat(created.status()).isEqualTo(TemplateStatus.DRAFT);
            assertThat(created.fieldsJson()).isEqualTo(source.getFieldsJson());
            assertThat(created.markingRuleJson()).isEqualTo(source.getMarkingRuleJson());
            assertThat(source.getStatus()).isEqualTo(TemplateStatus.LEGACY);
        }

        @Test
        @DisplayName("источник из другого шаблона — ошибка")
        void sourceFromAnotherTemplate_throws() {
            ProductTemplateVersion foreign = version(50L, TemplateStatus.LEGACY, 1);
            foreign.setTemplateId(999L);

            when(productTemplateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template()));
            when(productTemplateVersionRepository.existsByTemplateIdAndStatus(TEMPLATE_ID, TemplateStatus.DRAFT))
                    .thenReturn(false);
            when(productTemplateVersionRepository.findById(50L)).thenReturn(Optional.of(foreign));

            assertThatThrownBy(() -> service.createVersionFrom(TEMPLATE_ID, 50L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("другому шаблону");
        }

        @Test
        @DisplayName("у шаблона уже есть DRAFT — ошибка")
        void existingDraft_throws() {
            when(productTemplateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template()));
            when(productTemplateVersionRepository.existsByTemplateIdAndStatus(TEMPLATE_ID, TemplateStatus.DRAFT))
                    .thenReturn(true);

            assertThatThrownBy(() -> service.createVersionFrom(TEMPLATE_ID, 50L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("черновик");
        }

        @Test
        @DisplayName("без sourceVersionId берётся текущая версия шаблона")
        void nullSource_usesCurrentVersion() {
            ProductTemplateVersion current = version(100L, TemplateStatus.PUBLISHED, 3);

            when(productTemplateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template()));
            when(productTemplateVersionRepository.existsByTemplateIdAndStatus(TEMPLATE_ID, TemplateStatus.DRAFT))
                    .thenReturn(false);
            when(productTemplateVersionRepository.findById(100L)).thenReturn(Optional.of(current));
            when(productTemplateVersionRepository.findByTemplateIdAndStatusIn(anyLong(), any(Set.class)))
                    .thenReturn(List.of(current));
            when(productTemplateVersionRepository.findMaxVersion(TEMPLATE_ID)).thenReturn(Optional.of(3));
            when(productTemplateVersionRepository.save(any())).thenAnswer(inv -> {
                ProductTemplateVersion v = inv.getArgument(0);
                v.setId(101L);
                return v;
            });

            ProductTemplateVersionDto created = service.createVersionFrom(TEMPLATE_ID, null, "admin@test");

            assertThat(created.version()).isEqualTo(4);
        }

        @Test
        @DisplayName("у шаблона нет версий — ошибка")
        void noVersions_throws() {
            ProductTemplate empty = template();
            empty.setCurrentVersionId(null);

            when(productTemplateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(empty));
            when(productTemplateVersionRepository.existsByTemplateIdAndStatus(TEMPLATE_ID, TemplateStatus.DRAFT))
                    .thenReturn(false);
            when(productTemplateVersionRepository.findByTemplateIdOrderByVersionDesc(TEMPLATE_ID))
                    .thenReturn(List.of());

            assertThatThrownBy(() -> service.createVersionFrom(TEMPLATE_ID, null, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("нет версий");
        }
    }
}

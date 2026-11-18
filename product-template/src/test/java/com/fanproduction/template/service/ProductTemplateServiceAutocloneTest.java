package com.fanproduction.template.service;

import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.ProductTemplateVersionDto;
import com.fanproduction.template.entity.ProductTemplate;
import com.fanproduction.template.entity.ProductTemplateVersion;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.enums.RuleElementType;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.model.MarkingRuleElement;
import com.fanproduction.template.repository.ProductTemplateRepository;
import com.fanproduction.template.repository.ProductTemplateVersionRepository;
import com.fanproduction.template.service.impl.ProductTemplateServiceImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты автокопирования полей и правила при создании новой версии.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductTemplateService — автокопирование при createNewVersion")
class ProductTemplateServiceAutocloneTest {

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

    private ProductTemplate template;
    private ProductTemplateVersion currentVersion;

    private final List<FieldDefinition> sampleFields = List.of(
            new FieldDefinition("series", "Серия", FieldType.TEXT,
                    false, null, null, null, null, null, null,
                    null, null, null, null, null, 1, null)
    );

    private final MarkingRule sampleRule = new MarkingRule(List.of(
            new MarkingRuleElement(RuleElementType.LITERAL, "TEST", null, null, null, null)
    ));

    @BeforeEach
    void setUp() {
        template = new ProductTemplate();
        template.setId(1L);
        template.setSeriesId(100L);
        template.setName("Шаблон P-серии");
        template.setCurrentVersionId(10L);

        currentVersion = new ProductTemplateVersion();
        currentVersion.setId(10L);
        currentVersion.setTemplateId(1L);
        currentVersion.setVersion(1);
        currentVersion.setStatus(TemplateStatus.PUBLISHED);
        currentVersion.setFieldsJson(sampleFields);
        currentVersion.setMarkingRuleJson(sampleRule);
    }

    // ==========================================================
    // АВТОКОПИРОВАНИЕ ИЗ current_version_id
    // ==========================================================

    @Nested
    @DisplayName("Автокопирование из current_version_id")
    class FromCurrentVersion {

        @Test
        @DisplayName("fields и markingRule = null → копируются из current_version")
        void bothNull() {
            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(productTemplateVersionRepository.existsByTemplateIdAndStatus(
                    1L, TemplateStatus.DRAFT)).thenReturn(false);
            when(productTemplateVersionRepository.findById(10L))
                    .thenReturn(Optional.of(currentVersion));
            when(productTemplateVersionRepository.findMaxVersion(1L)).thenReturn(Optional.of(1));
            when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateVersionDto result = service.createNewVersion(
                    1L, null, null, "admin@test");

            assertThat(result.fieldsJson()).isEqualTo(sampleFields);
            assertThat(result.markingRuleJson()).isEqualTo(sampleRule);
            assertThat(result.version()).isEqualTo(2);
            assertThat(result.status()).isEqualTo(TemplateStatus.DRAFT);
        }

        @Test
        @DisplayName("fields передан, markingRule = null → копируется только правило")
        void onlyFieldsExplicit() {
            List<FieldDefinition> customFields = List.of(
                    new FieldDefinition("x", "X", FieldType.NUMBER,
                            false, null, null, null, null, null, null,
                            null, null, null, null, null, 1, null)
            );

            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(productTemplateVersionRepository.existsByTemplateIdAndStatus(
                    1L, TemplateStatus.DRAFT)).thenReturn(false);
            when(productTemplateVersionRepository.findById(10L))
                    .thenReturn(Optional.of(currentVersion));
            when(productTemplateVersionRepository.findMaxVersion(1L)).thenReturn(Optional.of(1));
            when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateVersionDto result = service.createNewVersion(
                    1L, customFields, null, "admin@test");

            assertThat(result.fieldsJson()).isEqualTo(customFields);
            assertThat(result.markingRuleJson()).isEqualTo(sampleRule);
        }

        @Test
        @DisplayName("fields = null, markingRule передан → копируются только поля")
        void onlyRuleExplicit() {
            MarkingRule customRule = new MarkingRule(List.of(
                    new MarkingRuleElement(RuleElementType.LITERAL, "NEW", null, null, null, null)
            ));

            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(productTemplateVersionRepository.existsByTemplateIdAndStatus(
                    1L, TemplateStatus.DRAFT)).thenReturn(false);
            when(productTemplateVersionRepository.findById(10L))
                    .thenReturn(Optional.of(currentVersion));
            when(productTemplateVersionRepository.findMaxVersion(1L)).thenReturn(Optional.of(1));
            when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateVersionDto result = service.createNewVersion(
                    1L, null, customRule, "admin@test");

            assertThat(result.fieldsJson()).isEqualTo(sampleFields);
            assertThat(result.markingRuleJson()).isEqualTo(customRule);
        }

        @Test
        @DisplayName("Оба переданы → ничего не копируется")
        void bothExplicit() {
            List<FieldDefinition> customFields = List.of(
                    new FieldDefinition("y", "Y", FieldType.TEXT,
                            false, null, null, null, null, null, null,
                            null, null, null, null, null, 1, null)
            );
            MarkingRule customRule = new MarkingRule(List.of());

            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(productTemplateVersionRepository.existsByTemplateIdAndStatus(
                    1L, TemplateStatus.DRAFT)).thenReturn(false);
            when(productTemplateVersionRepository.findMaxVersion(1L)).thenReturn(Optional.of(1));
            when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateVersionDto result = service.createNewVersion(
                    1L, customFields, customRule, "admin@test");

            assertThat(result.fieldsJson()).isEqualTo(customFields);
            assertThat(result.markingRuleJson()).isEqualTo(customRule);

            // Проверяем, что репозиторий источника не вызывался
            verify(productTemplateVersionRepository, never()).findById(10L);
        }
    }

    // ==========================================================
    // АВТОКОПИРОВАНИЕ ИЗ ПОСЛЕДНЕЙ ВЕРСИИ
    // ==========================================================

    @Nested
    @DisplayName("Автокопирование из последней версии (нет current)")
    class FromLastVersion {

        @Test
        @DisplayName("current = null → берётся последняя версия")
        void noCurrentVersion() {
            template.setCurrentVersionId(null);

            ProductTemplateVersion lastVersion = new ProductTemplateVersion();
            lastVersion.setId(20L);
            lastVersion.setTemplateId(1L);
            lastVersion.setVersion(3);
            lastVersion.setStatus(TemplateStatus.LEGACY);
            lastVersion.setFieldsJson(sampleFields);
            lastVersion.setMarkingRuleJson(sampleRule);

            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(productTemplateVersionRepository.existsByTemplateIdAndStatus(
                    1L, TemplateStatus.DRAFT)).thenReturn(false);
            when(productTemplateVersionRepository.findByTemplateIdOrderByVersionDesc(1L))
                    .thenReturn(List.of(lastVersion));
            when(productTemplateVersionRepository.findMaxVersion(1L)).thenReturn(Optional.of(3));
            when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateVersionDto result = service.createNewVersion(
                    1L, null, null, "admin@test");

            assertThat(result.fieldsJson()).isEqualTo(sampleFields);
            assertThat(result.markingRuleJson()).isEqualTo(sampleRule);
        }

        @Test
        @DisplayName("current_version_id указывает на несуществующую запись → fallback на последнюю")
        void brokenCurrentVersionId() {
            template.setCurrentVersionId(999L);

            ProductTemplateVersion lastVersion = new ProductTemplateVersion();
            lastVersion.setId(20L);
            lastVersion.setTemplateId(1L);
            lastVersion.setVersion(2);
            lastVersion.setStatus(TemplateStatus.LEGACY);
            lastVersion.setFieldsJson(sampleFields);
            lastVersion.setMarkingRuleJson(sampleRule);

            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(productTemplateVersionRepository.existsByTemplateIdAndStatus(
                    1L, TemplateStatus.DRAFT)).thenReturn(false);
            when(productTemplateVersionRepository.findById(999L)).thenReturn(Optional.empty());
            when(productTemplateVersionRepository.findByTemplateIdOrderByVersionDesc(1L))
                    .thenReturn(List.of(lastVersion));
            when(productTemplateVersionRepository.findMaxVersion(1L)).thenReturn(Optional.of(2));
            when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            ProductTemplateVersionDto result = service.createNewVersion(
                    1L, null, null, "admin@test");

            assertThat(result.fieldsJson()).isEqualTo(sampleFields);
        }
    }

    // ==========================================================
    // АУДИТ
    // ==========================================================

    @Test
    @DisplayName("Аудит содержит пометку об автокопировании")
    void auditMentionsAutoclone() {
        when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(productTemplateVersionRepository.existsByTemplateIdAndStatus(
                1L, TemplateStatus.DRAFT)).thenReturn(false);
        when(productTemplateVersionRepository.findById(10L))
                .thenReturn(Optional.of(currentVersion));
        when(productTemplateVersionRepository.findMaxVersion(1L)).thenReturn(Optional.of(1));
        when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.createNewVersion(1L, null, null, "admin@test");

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue().getDetails())
                .contains("скопированы из предыдущей версии");
    }

    @Test
    @DisplayName("Аудит БЕЗ пометки, когда всё передано явно")
    void auditNoAutocloneMention() {
        when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(productTemplateVersionRepository.existsByTemplateIdAndStatus(
                1L, TemplateStatus.DRAFT)).thenReturn(false);
        when(productTemplateVersionRepository.findMaxVersion(1L)).thenReturn(Optional.of(1));
        when(productTemplateVersionRepository.save(any(ProductTemplateVersion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.createNewVersion(1L, sampleFields, sampleRule, "admin@test");

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        assertThat(captor.getValue().getDetails())
                .doesNotContain("скопированы из предыдущей версии");
    }
}

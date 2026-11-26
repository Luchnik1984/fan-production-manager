package com.fanproduction.services.impl;

import com.fanproduction.core.entity.product.DynamicCardEntity;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.repositories.product.DynamicCardRepository;
import com.fanproduction.services.exception.DynamicCardCreationException;
import com.fanproduction.template.entity.ProductSeries;
import com.fanproduction.template.entity.ProductSpecies;
import com.fanproduction.template.entity.ProductTemplate;
import com.fanproduction.template.entity.ProductTemplateVersion;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.enums.RuleElementType;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.model.MarkingRuleElement;
import com.fanproduction.template.repository.ProductSeriesRepository;
import com.fanproduction.template.repository.ProductSpeciesRepository;
import com.fanproduction.template.repository.ProductTemplateRepository;
import com.fanproduction.template.repository.ProductTemplateVersionRepository;
import com.fanproduction.template.service.MarkingEngine;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link DynamicCardServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DynamicCardService")
class DynamicCardServiceTest {

    @Mock
    private DynamicCardRepository fanCardRepository;

    @Mock
    private ProductTemplateRepository productTemplateRepository;

    @Mock
    private ProductTemplateVersionRepository productTemplateVersionRepository;

    @Mock
    private ProductSeriesRepository productSeriesRepository;

    @Mock
    private ProductSpeciesRepository productSpeciesRepository;

    @Mock
    private MarkingEngine markingEngine;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private com.fanproduction.template.repository.ProductTypeRepository productTypeRepository;

    @Mock
    private com.fanproduction.services.article.ArticleGenerator articleGenerator;

    @InjectMocks
    private DynamicCardServiceImpl service;

    // ==========================================================
    // ФИКСТУРЫ
    // ==========================================================

    private ProductTemplate template;
    private ProductTemplateVersion version;

    private final List<FieldDefinition> sampleFields = List.of(
            new FieldDefinition("series", "Серия", FieldType.TEXT, true,
                    null, null, null, null, null, null,
                    null, null, null, null, null, 1, null),
            new FieldDefinition("ductSize", "Типоразмер", FieldType.TEXT, true,
                    null, null, null, null, null, null,
                    null, null, null, null, null, 2, null)
    );

    private final MarkingRule sampleRule = new MarkingRule(List.of(
            new MarkingRuleElement(RuleElementType.LITERAL, "VRK-PatAIR-", null, null, null, null),
            new MarkingRuleElement(RuleElementType.FIELD, null, "series", null, null, null)
    ));

    @BeforeEach
    void setUp() {
        template = new ProductTemplate();
        template.setId(1L);
        template.setSeriesId(100L);
        template.setName("Шаблон P-серии");
        template.setCurrentVersionId(10L);

        version = new ProductTemplateVersion();
        version.setId(10L);
        version.setTemplateId(1L);
        version.setVersion(1);
        version.setStatus(TemplateStatus.PUBLISHED);
        version.setFieldsJson(sampleFields);
        version.setMarkingRuleJson(sampleRule);

        lenient().when(articleGenerator.next(anyString())).thenReturn("AW-000001");
    }

    // ==========================================================
    // CREATE
    // ==========================================================

    @Nested
    @DisplayName("createFromTemplate")
    class CreateTests {

        @Test
        @DisplayName("Тип продукции определяется по шаблону: серия → вид → тип")
        void resolvesProductTypeId() {
            ProductSeries series = new ProductSeries();
            series.setId(100L);
            series.setSpeciesId(50L);

            ProductSpecies species = new ProductSpecies();
            species.setId(50L);
            species.setTypeId(7L);

            when(productSeriesRepository.findById(100L)).thenReturn(Optional.of(series));
            when(productSpeciesRepository.findById(50L)).thenReturn(Optional.of(species));
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any(DynamicCardEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            DynamicCardEntity result = service.createFromTemplate(10L, Map.of("series", "P", "ductSize", "40-20"), "admin");

            assertThat(result.getProductTypeId()).isEqualTo(7L);
        }

        @Test
        @DisplayName("Без серии в шаблоне тип продукции остаётся пустым")
        void noSeries_noProductType() {
            template.setSeriesId(null);

            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any(DynamicCardEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            DynamicCardEntity result = service.createFromTemplate(10L, Map.of("series", "P", "ductSize", "40-20"), "admin");

            assertThat(result.getProductTypeId()).isNull();
        }

        @Test
        @DisplayName("Успешное создание карточки")
        void success() {
            Map<String, Object> values = new HashMap<>();
            values.put("series", "P");
            values.put("ductSize", "40-20");

            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), eq(sampleFields),  anyLong()))
                    .thenReturn("VRK-PatAIR-P");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any(DynamicCardEntity.class)))
                    .thenAnswer(inv -> {
                        DynamicCardEntity c = inv.getArgument(0);
                        c.setId(500L);
                        return c;
                    });

            DynamicCardEntity result = service.createFromTemplate(10L, values, "admin@test");

            assertThat(result.getId()).isEqualTo(500L);
            assertThat(result.getCardType()).isEqualTo("DYNAMIC");
            assertThat(result.getTemplateVersionId()).isEqualTo(10L);
            assertThat(result.getProductTemplateId()).isEqualTo(1L);
            assertThat(result.getFullMarking()).isEqualTo("VRK-PatAIR-P");
            assertThat(result.getCreatedBy()).isEqualTo("admin@test");
        }

        @Test
        @DisplayName("Системные поля → колонки, пользовательские → dynamicFields")
        void splitsFields() {
            Map<String, Object> values = new HashMap<>();
            values.put("series", "P");                  // системное
            values.put("ductSize", "40-20");            // не системное
            values.put("poles", 4);                     // системное
            values.put("voltage", 220);                 // системное
            values.put("customField", "custom");        // не системное

            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("MARKING");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any(DynamicCardEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            DynamicCardEntity result = service.createFromTemplate(10L, values, "admin");

            assertThat(result.getSeries()).isEqualTo("P");
            assertThat(result.getPoles()).isEqualTo(4);
            assertThat(result.getVoltage()).isEqualTo(220);

            assertThat(result.getDynamicFields()).containsEntry("ductSize", "40-20");
            assertThat(result.getDynamicFields()).containsEntry("customField", "custom");
            assertThat(result.getDynamicFields()).doesNotContainKey("series");
            assertThat(result.getDynamicFields()).doesNotContainKey("poles");
        }

        @Test
        @DisplayName("Значения приводятся к правильным типам")
        void convertsTypes() {
            Map<String, Object> values = new HashMap<>();
            values.put("series", "P");
            values.put("ductSize", "40-20");
            values.put("poles", "4");        // строка → Integer
            values.put("voltage", "380");    // строка → Integer

            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DynamicCardEntity result = service.createFromTemplate(10L, values, "admin");

            assertThat(result.getPoles()).isEqualTo(4);
            assertThat(result.getVoltage()).isEqualTo(380);
        }

        @Test
        @DisplayName("Публикуется событие аудита CREATE_CARD")
        void publishesAudit() {
            Map<String, Object> values = Map.of("series", "P", "ductSize", "40-20");

            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("VRK-PatAIR-P");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.createFromTemplate(10L, values, "admin@test");

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            AuditEvent event = captor.getValue();
            assertThat(event.getUsername()).isEqualTo("admin@test");
            assertThat(event.getAction()).isEqualTo(AuditAction.CREATE_CARD);
            assertThat(event.getDetails()).contains("VRK-PatAIR-P");
        }

        @Test
        @DisplayName("Версия шаблона не найдена → исключение")
        void versionNotFound() {
            when(productTemplateVersionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createFromTemplate(999L, Map.of(), "admin"))
                    .isInstanceOf(DynamicCardCreationException.class)
                    .hasMessageContaining("Версия шаблона не найдена");

            verify(fanCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("Версия в статусе DRAFT → исключение")
        void draftVersionForbidden() {
            version.setStatus(TemplateStatus.DRAFT);
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));

            assertThatThrownBy(() -> service.createFromTemplate(
                    10L, Map.of("series", "P", "ductSize", "40"), "admin"))
                    .isInstanceOf(DynamicCardCreationException.class)
                    .hasMessageContaining("PUBLISHED или LEGACY");
        }

        @Test
        @DisplayName("Обязательное поле не заполнено → исключение")
        void requiredFieldMissing() {
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

            // Не передаём ductSize
            Map<String, Object> values = Map.of("series", "P");

            assertThatThrownBy(() -> service.createFromTemplate(10L, values, "admin"))
                    .isInstanceOf(DynamicCardCreationException.class)
                    .hasMessageContaining("Обязательное поле не заполнено")
                    .hasMessageContaining("Типоразмер");
        }

        @Test
        @DisplayName("Маркировка уже существует → исключение")
        void markingAlreadyExists() {
            DynamicCardEntity existing = new DynamicCardEntity();
            existing.setId(999L);
            existing.setFullMarking("VRK-PatAIR-P");

            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("VRK-PatAIR-P");
            when(fanCardRepository.findByFullMarkingIgnoreCase("VRK-PatAIR-P"))
                    .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> service.createFromTemplate(
                    10L, Map.of("series", "P", "ductSize", "40"), "admin"))
                    .isInstanceOf(DynamicCardCreationException.class)
                    .hasMessageContaining("уже существует");
        }

        @Test
        @DisplayName("LEGACY-версия разрешена для создания")
        void legacyVersionAllowed() {
            version.setStatus(TemplateStatus.LEGACY);

            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.createFromTemplate(10L, Map.of("series", "P", "ductSize", "40"), "admin");

            verify(fanCardRepository).save(any());
        }
    }

    // ==========================================================
    // UPDATE
    // ==========================================================

    @Nested
    @DisplayName("updateFromValues")
    class UpdateTests {

        private DynamicCardEntity existingCard;

        @BeforeEach
        void setUpCard() {
            existingCard = new DynamicCardEntity();
            existingCard.setId(500L);
            existingCard.setCardType("DYNAMIC");
            existingCard.setName("Вентилятор");
            existingCard.setTemplateVersionId(10L);
            existingCard.setProductTemplateId(1L);
            existingCard.setFullMarking("OLD-MARKING");
            existingCard.setSeries("P");
            existingCard.setPoles(4);
            existingCard.setDynamicFields(new HashMap<>(Map.of("ductSize", "40-20")));
        }

        @Test
        @DisplayName("Успешное обновление полей и маркировки")
        void success() {
            Map<String, Object> values = new HashMap<>();
            values.put("series", "PS");
            values.put("ductSize", "60-30");
            values.put("poles", 6);

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("NEW-MARKING");
            when(fanCardRepository.findByFullMarkingIgnoreCase("NEW-MARKING"))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DynamicCardEntity result = service.updateFromValues(500L, values, "admin");

            assertThat(result.getSeries()).isEqualTo("PS");
            assertThat(result.getPoles()).isEqualTo(6);
            assertThat(result.getDynamicFields()).containsEntry("ductSize", "60-30");
            assertThat(result.getFullMarking()).isEqualTo("NEW-MARKING");
        }

        @Test
        @DisplayName("Не переданные поля сбрасываются (форма отправляется целиком)")
        void clearsUnsetFields() {
            Map<String, Object> values = new HashMap<>();
            values.put("series", "PS");
            values.put("ductSize", "60-30");
            // poles не передаём → должен сброситься в null

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DynamicCardEntity result = service.updateFromValues(500L, values, "admin");

            assertThat(result.getPoles()).isNull();
        }

        @Test
        @DisplayName("Технические поля сохраняются (templateVersionId, productTemplateId)")
        void technicalFieldsPreserved() {
            Map<String, Object> values = Map.of("series", "PS", "ductSize", "60-30");

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DynamicCardEntity result = service.updateFromValues(500L, values, "admin");

            assertThat(result.getTemplateVersionId()).isEqualTo(10L);
            assertThat(result.getProductTemplateId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Маркировка не изменилась → аудит без стрелки")
        void markingUnchanged() {
            Map<String, Object> values = Map.of("series", "P", "ductSize", "40-20");

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("OLD-MARKING");
            when(fanCardRepository.findByFullMarkingIgnoreCase("OLD-MARKING"))
                    .thenReturn(Optional.of(existingCard));  // та же карточка
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.updateFromValues(500L, values, "admin");

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            assertThat(captor.getValue().getDetails())
                    .doesNotContain("→");
        }

        @Test
        @DisplayName("Маркировка изменилась → аудит со стрелкой")
        void markingChanged() {
            Map<String, Object> values = Map.of("series", "PS", "ductSize", "60-30");

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("NEW-MARKING");
            when(fanCardRepository.findByFullMarkingIgnoreCase("NEW-MARKING"))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.updateFromValues(500L, values, "admin");

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            assertThat(captor.getValue().getDetails())
                    .contains("OLD-MARKING")
                    .contains("→")
                    .contains("NEW-MARKING");
        }

        @Test
        @DisplayName("Маркировка занята другой карточкой → исключение")
        void markingTakenByAnother() {
            DynamicCardEntity anotherCard = new DynamicCardEntity();
            anotherCard.setId(999L);
            anotherCard.setFullMarking("NEW-MARKING");

            Map<String, Object> values = Map.of("series", "PS", "ductSize", "60-30");

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("NEW-MARKING");
            when(fanCardRepository.findByFullMarkingIgnoreCase("NEW-MARKING"))
                    .thenReturn(Optional.of(anotherCard));

            assertThatThrownBy(() -> service.updateFromValues(500L, values, "admin"))
                    .isInstanceOf(DynamicCardCreationException.class)
                    .hasMessageContaining("уже существует");
        }

        @Test
        @DisplayName("Карточка не найдена → исключение")
        void cardNotFound() {
            when(fanCardRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateFromValues(999L, Map.of(), "admin"))
                    .isInstanceOf(DynamicCardCreationException.class)
                    .hasMessageContaining("не найдена");
        }

        @Test
        @DisplayName("Не FAN-карточка → исключение")
        void notFanCard() {
            existingCard.setCardType("MOTOR");

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));

            assertThatThrownBy(() -> service.updateFromValues(500L, Map.of(), "admin"))
                    .isInstanceOf(DynamicCardCreationException.class)
                    .hasMessageContaining("не является динамической");
        }

        @Test
        @DisplayName("DEPRECATED-версия разрешена для обновления (старые карточки)")
        void deprecatedVersionAllowed() {
            version.setStatus(TemplateStatus.DEPRECATED);

            Map<String, Object> values = Map.of("series", "P", "ductSize", "40-20");

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.updateFromValues(500L, values, "admin");

            verify(fanCardRepository).save(any());
        }
    }

    // ==========================================================
    // RECALCULATE
    // ==========================================================

    @Nested
    @DisplayName("recalculateMarking")
    class RecalculateTests {

        private DynamicCardEntity existingCard;

        @BeforeEach
        void setUpCard() {
            existingCard = new DynamicCardEntity();
            existingCard.setId(500L);
            existingCard.setCardType("DYNAMIC");
            existingCard.setTemplateVersionId(10L);
            existingCard.setProductTemplateId(1L);
            existingCard.setSeries("P");
            existingCard.setDynamicFields(new HashMap<>(Map.of("ductSize", "40-20")));
            existingCard.setFullMarking("OLD");
        }

        @Test
        @DisplayName("Успешный пересчёт маркировки без изменения полей")
        void success() {
            Map<String, Object> expectedDynamic = new HashMap<>(existingCard.getDynamicFields());

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("NEW");
            when(fanCardRepository.findByFullMarkingIgnoreCase("NEW"))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DynamicCardEntity result = service.recalculateMarking(500L, "admin");

            assertThat(result.getFullMarking()).isEqualTo("NEW");
            assertThat(result.getSeries()).isEqualTo("P");
            assertThat(result.getDynamicFields()).isEqualTo(expectedDynamic);
        }

        @Test
        @DisplayName("Аудит с пометкой «пересчитана»")
        void auditMentionsRecalculation() {
            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("NEW");
            when(fanCardRepository.findByFullMarkingIgnoreCase("NEW"))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.recalculateMarking(500L, "admin");

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            assertThat(captor.getValue().getDetails())
                    .contains("Пересчитана маркировка");
        }

        @Test
        @DisplayName("У карточки нет templateVersionId → исключение")
        void noTemplateVersionId() {
            existingCard.setTemplateVersionId(null);
            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));

            assertThatThrownBy(() -> service.recalculateMarking(500L, "admin"))
                    .isInstanceOf(DynamicCardCreationException.class)
                    .hasMessageContaining("нет привязки к версии шаблона");
        }
    }

    // ==========================================================
    // АУДИТ — ОБЩИЕ ПРОВЕРКИ
    // ==========================================================

    @Nested
    @DisplayName("Аудит — общие проверки")
    class AuditTests {

        @Test
        @DisplayName("Публикуется ровно одно событие аудита за операцию")
        void exactlyOneAuditEvent() {
            Map<String, Object> values = Map.of("series", "P", "ductSize", "40-20");

            when(productTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(productTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any(), anyLong())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.createFromTemplate(10L, values, "admin@test");

            verify(eventPublisher, times(1)).publishEvent(any(AuditEvent.class));
        }
    }
}

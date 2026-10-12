package com.fanproduction.services.impl;

import com.fanproduction.core.entity.product.FanCardEntity;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.repositories.product.FanCardRepository;
import com.fanproduction.services.exception.FanCardCreationException;
import com.fanproduction.template.entity.FanTemplate;
import com.fanproduction.template.entity.FanTemplateVersion;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.enums.RuleElementType;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.model.MarkingRuleElement;
import com.fanproduction.template.repository.FanTemplateRepository;
import com.fanproduction.template.repository.FanTemplateVersionRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link FanCardServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FanCardService")
class FanCardServiceTest {

    @Mock
    private FanCardRepository fanCardRepository;

    @Mock
    private FanTemplateRepository fanTemplateRepository;

    @Mock
    private FanTemplateVersionRepository fanTemplateVersionRepository;

    @Mock
    private MarkingEngine markingEngine;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FanCardServiceImpl service;

    // ==========================================================
    // ФИКСТУРЫ
    // ==========================================================

    private FanTemplate template;
    private FanTemplateVersion version;

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
        template = new FanTemplate();
        template.setId(1L);
        template.setSeriesId(100L);
        template.setName("Шаблон P-серии");
        template.setCurrentVersionId(10L);

        version = new FanTemplateVersion();
        version.setId(10L);
        version.setTemplateId(1L);
        version.setVersion(1);
        version.setStatus(TemplateStatus.PUBLISHED);
        version.setFieldsJson(sampleFields);
        version.setMarkingRuleJson(sampleRule);
    }

    // ==========================================================
    // CREATE
    // ==========================================================

    @Nested
    @DisplayName("createFromTemplate")
    class CreateTests {

        @Test
        @DisplayName("Успешное создание карточки")
        void success() {
            Map<String, Object> values = new HashMap<>();
            values.put("series", "P");
            values.put("ductSize", "40-20");

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), eq(sampleFields)))
                    .thenReturn("VRK-PatAIR-P");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any(FanCardEntity.class)))
                    .thenAnswer(inv -> {
                        FanCardEntity c = inv.getArgument(0);
                        c.setId(500L);
                        return c;
                    });

            FanCardEntity result = service.createFromTemplate(10L, values, "admin@test");

            assertThat(result.getId()).isEqualTo(500L);
            assertThat(result.getCardType()).isEqualTo("FAN");
            assertThat(result.getTemplateVersionId()).isEqualTo(10L);
            assertThat(result.getFanTemplateId()).isEqualTo(1L);
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

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("MARKING");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any(FanCardEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            FanCardEntity result = service.createFromTemplate(10L, values, "admin");

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

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            FanCardEntity result = service.createFromTemplate(10L, values, "admin");

            assertThat(result.getPoles()).isEqualTo(4);
            assertThat(result.getVoltage()).isEqualTo(380);
        }

        @Test
        @DisplayName("Публикуется событие аудита CREATE_CARD")
        void publishesAudit() {
            Map<String, Object> values = Map.of("series", "P", "ductSize", "40-20");

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("VRK-PatAIR-P");
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
            when(fanTemplateVersionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createFromTemplate(999L, Map.of(), "admin"))
                    .isInstanceOf(FanCardCreationException.class)
                    .hasMessageContaining("Версия шаблона не найдена");

            verify(fanCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("Версия в статусе DRAFT → исключение")
        void draftVersionForbidden() {
            version.setStatus(TemplateStatus.DRAFT);
            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));

            assertThatThrownBy(() -> service.createFromTemplate(
                    10L, Map.of("series", "P", "ductSize", "40"), "admin"))
                    .isInstanceOf(FanCardCreationException.class)
                    .hasMessageContaining("PUBLISHED или LEGACY");
        }

        @Test
        @DisplayName("Обязательное поле не заполнено → исключение")
        void requiredFieldMissing() {
            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

            // Не передаём ductSize
            Map<String, Object> values = Map.of("series", "P");

            assertThatThrownBy(() -> service.createFromTemplate(10L, values, "admin"))
                    .isInstanceOf(FanCardCreationException.class)
                    .hasMessageContaining("Обязательное поле не заполнено")
                    .hasMessageContaining("Типоразмер");
        }

        @Test
        @DisplayName("Маркировка уже существует → исключение")
        void markingAlreadyExists() {
            FanCardEntity existing = new FanCardEntity();
            existing.setId(999L);
            existing.setFullMarking("VRK-PatAIR-P");

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("VRK-PatAIR-P");
            when(fanCardRepository.findByFullMarkingIgnoreCase("VRK-PatAIR-P"))
                    .thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> service.createFromTemplate(
                    10L, Map.of("series", "P", "ductSize", "40"), "admin"))
                    .isInstanceOf(FanCardCreationException.class)
                    .hasMessageContaining("уже существует");
        }

        @Test
        @DisplayName("LEGACY-версия разрешена для создания")
        void legacyVersionAllowed() {
            version.setStatus(TemplateStatus.LEGACY);

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("M");
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

        private FanCardEntity existingCard;

        @BeforeEach
        void setUpCard() {
            existingCard = new FanCardEntity();
            existingCard.setId(500L);
            existingCard.setCardType("FAN");
            existingCard.setName("Вентилятор");
            existingCard.setTemplateVersionId(10L);
            existingCard.setFanTemplateId(1L);
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
            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("NEW-MARKING");
            when(fanCardRepository.findByFullMarkingIgnoreCase("NEW-MARKING"))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            FanCardEntity result = service.updateFromValues(500L, values, "admin");

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
            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            FanCardEntity result = service.updateFromValues(500L, values, "admin");

            assertThat(result.getPoles()).isNull();
        }

        @Test
        @DisplayName("Технические поля сохраняются (templateVersionId, fanTemplateId)")
        void technicalFieldsPreserved() {
            Map<String, Object> values = Map.of("series", "PS", "ductSize", "60-30");

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            FanCardEntity result = service.updateFromValues(500L, values, "admin");

            assertThat(result.getTemplateVersionId()).isEqualTo(10L);
            assertThat(result.getFanTemplateId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Маркировка не изменилась → аудит без стрелки")
        void markingUnchanged() {
            Map<String, Object> values = Map.of("series", "P", "ductSize", "40-20");

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("OLD-MARKING");
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
            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("NEW-MARKING");
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
            FanCardEntity anotherCard = new FanCardEntity();
            anotherCard.setId(999L);
            anotherCard.setFullMarking("NEW-MARKING");

            Map<String, Object> values = Map.of("series", "PS", "ductSize", "60-30");

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("NEW-MARKING");
            when(fanCardRepository.findByFullMarkingIgnoreCase("NEW-MARKING"))
                    .thenReturn(Optional.of(anotherCard));

            assertThatThrownBy(() -> service.updateFromValues(500L, values, "admin"))
                    .isInstanceOf(FanCardCreationException.class)
                    .hasMessageContaining("уже существует");
        }

        @Test
        @DisplayName("Карточка не найдена → исключение")
        void cardNotFound() {
            when(fanCardRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateFromValues(999L, Map.of(), "admin"))
                    .isInstanceOf(FanCardCreationException.class)
                    .hasMessageContaining("не найдена");
        }

        @Test
        @DisplayName("Не FAN-карточка → исключение")
        void notFanCard() {
            existingCard.setCardType("MOTOR");

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));

            assertThatThrownBy(() -> service.updateFromValues(500L, Map.of(), "admin"))
                    .isInstanceOf(FanCardCreationException.class)
                    .hasMessageContaining("не является FAN-карточкой");
        }

        @Test
        @DisplayName("DEPRECATED-версия разрешена для обновления (старые карточки)")
        void deprecatedVersionAllowed() {
            version.setStatus(TemplateStatus.DEPRECATED);

            Map<String, Object> values = Map.of("series", "P", "ductSize", "40-20");

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("M");
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

        private FanCardEntity existingCard;

        @BeforeEach
        void setUpCard() {
            existingCard = new FanCardEntity();
            existingCard.setId(500L);
            existingCard.setCardType("FAN");
            existingCard.setTemplateVersionId(10L);
            existingCard.setFanTemplateId(1L);
            existingCard.setSeries("P");
            existingCard.setDynamicFields(new HashMap<>(Map.of("ductSize", "40-20")));
            existingCard.setFullMarking("OLD");
        }

        @Test
        @DisplayName("Успешный пересчёт маркировки без изменения полей")
        void success() {
            Map<String, Object> expectedDynamic = new HashMap<>(existingCard.getDynamicFields());

            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("NEW");
            when(fanCardRepository.findByFullMarkingIgnoreCase("NEW"))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            FanCardEntity result = service.recalculateMarking(500L, "admin");

            assertThat(result.getFullMarking()).isEqualTo("NEW");
            assertThat(result.getSeries()).isEqualTo("P");
            assertThat(result.getDynamicFields()).isEqualTo(expectedDynamic);
        }

        @Test
        @DisplayName("Аудит с пометкой «пересчитана»")
        void auditMentionsRecalculation() {
            when(fanCardRepository.findById(500L)).thenReturn(Optional.of(existingCard));
            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("NEW");
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
                    .isInstanceOf(FanCardCreationException.class)
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

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(markingEngine.evaluate(any(), any(), any())).thenReturn("M");
            when(fanCardRepository.findByFullMarkingIgnoreCase(anyString()))
                    .thenReturn(Optional.empty());
            when(fanCardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.createFromTemplate(10L, values, "admin@test");

            verify(eventPublisher, times(1)).publishEvent(any(AuditEvent.class));
        }
    }
}

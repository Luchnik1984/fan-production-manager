package com.fanproduction.template.service;

import com.fanproduction.template.dto.DynamicFormFieldDto;
import com.fanproduction.template.dto.DynamicFormMetadataDto;
import com.fanproduction.template.entity.FanTemplate;
import com.fanproduction.template.entity.FanTemplateVersion;
import com.fanproduction.template.enums.ConditionOperator;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.model.Condition;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.repository.FanTemplateRepository;
import com.fanproduction.template.repository.FanTemplateVersionRepository;
import com.fanproduction.template.service.impl.DynamicFormBuilderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты построителя метаданных формы.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DynamicFormBuilder")
class DynamicFormBuilderTest {

    @Mock
    private FanTemplateRepository fanTemplateRepository;

    @Mock
    private FanTemplateVersionRepository fanTemplateVersionRepository;

    @InjectMocks
    private DynamicFormBuilderImpl builder;

    private FanTemplateVersion version;
    private FanTemplate template;

    @BeforeEach
    void setUp() {
        template = new FanTemplate();
        template.setId(1L);
        template.setName("Шаблон P-серии");
        template.setSeriesId(100L);

        version = new FanTemplateVersion();
        version.setId(10L);
        version.setTemplateId(1L);
        version.setVersion(1);
        version.setStatus(TemplateStatus.PUBLISHED);
    }

    // ==========================================================
    // ОСНОВНОЙ СЦЕНАРИЙ
    // ==========================================================

    @Test
    @DisplayName("Построение формы: поля сортируются по displayOrder")
    void sortByDisplayOrder() {
        version.setFieldsJson(List.of(
                field("c", 30),
                field("a", 10),
                field("b", 20)
        ));

        when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

        DynamicFormMetadataDto result = builder.buildFormMetadata(10L);

        assertThat(result.fields()).extracting(DynamicFormFieldDto::key)
                .containsExactly("a", "b", "c");
    }

    @Test
    @DisplayName("Поля без displayOrder идут в конце, в порядке шаблона")
    void nullDisplayOrderGoesLast() {
        version.setFieldsJson(List.of(
                field("withOrder", 10),
                field("noOrder1", null),
                field("noOrder2", null)
        ));

        when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

        DynamicFormMetadataDto result = builder.buildFormMetadata(10L);

        assertThat(result.fields()).extracting(DynamicFormFieldDto::key)
                .containsExactly("withOrder", "noOrder1", "noOrder2");
    }

    // ==========================================================
    // ФИЛЬТРАЦИЯ
    // ==========================================================

    @Nested
    @DisplayName("Фильтрация типов")
    class FilteringTests {

        @Test
        @DisplayName("SEPARATOR не включается в список полей")
        void separatorExcluded() {
            version.setFieldsJson(List.of(
                    field("a", 1),
                    separatorField("Заголовок", 2),
                    field("b", 3)
            ));

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

            DynamicFormMetadataDto result = builder.buildFormMetadata(10L);

            assertThat(result.fields()).extracting(DynamicFormFieldDto::key)
                    .containsExactly("a", "b");
        }

        @Test
        @DisplayName("HIDDEN не включается в список полей")
        void hiddenExcluded() {
            version.setFieldsJson(List.of(
                    field("a", 1),
                    hiddenField("secret", 2),
                    field("b", 3)
            ));

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

            DynamicFormMetadataDto result = builder.buildFormMetadata(10L);

            assertThat(result.fields()).extracting(DynamicFormFieldDto::key)
                    .containsExactly("a", "b");
        }

        @Test
        @DisplayName("Пустой список полей → пустой результат")
        void emptyFields() {
            version.setFieldsJson(List.of());

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

            DynamicFormMetadataDto result = builder.buildFormMetadata(10L);

            assertThat(result.fields()).isEmpty();
        }

        @Test
        @DisplayName("null список полей → пустой результат")
        void nullFields() {
            version.setFieldsJson(null);

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

            DynamicFormMetadataDto result = builder.buildFormMetadata(10L);

            assertThat(result.fields()).isEmpty();
        }
    }

    // ==========================================================
    // МЕТАДАННЫЕ ВЕРСИИ
    // ==========================================================

    @Test
    @DisplayName("Метаданные версии передаются корректно")
    void metadataPassedCorrectly() {
        version.setFieldsJson(List.of(field("a", 1)));

        when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

        DynamicFormMetadataDto result = builder.buildFormMetadata(10L);

        assertThat(result.templateId()).isEqualTo(1L);
        assertThat(result.templateVersionId()).isEqualTo(10L);
        assertThat(result.version()).isEqualTo(1);
        assertThat(result.status()).isEqualTo(TemplateStatus.PUBLISHED);
        assertThat(result.templateName()).isEqualTo("Шаблон P-серии");
    }

    // ==========================================================
    // СОДЕРЖИМОЕ ПОЛЯ
    // ==========================================================

    @Test
    @DisplayName("Поля-конфигурации сохраняются в DTO")
    void fieldConfigurationPreserved() {
        FieldDefinition fullField = new FieldDefinition(
                "motorWheelId",
                "Мотор-колесо",
                FieldType.SELECTABLE,
                true,
                null,
                null,
                "Выберите мотор-колесо",
                "MOTOR_WHEEL",
                "motorWheelFullMarking",
                "Мотор-колесо",
                false,
                null,
                new Condition("isOwnProduction", ConditionOperator.EQUALS, true, null),
                null,
                null,
                1,
                null
        );

        version.setFieldsJson(List.of(fullField));

        when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

        DynamicFormMetadataDto result = builder.buildFormMetadata(10L);

        DynamicFormFieldDto dto = result.fields().get(0);
        assertThat(dto.key()).isEqualTo("motorWheelId");
        assertThat(dto.label()).isEqualTo("Мотор-колесо");
        assertThat(dto.type()).isEqualTo(FieldType.SELECTABLE);
        assertThat(dto.required()).isTrue();
        assertThat(dto.hint()).isEqualTo("Выберите мотор-колесо");
        assertThat(dto.referenceType()).isEqualTo("MOTOR_WHEEL");
        assertThat(dto.targetFieldName()).isEqualTo("motorWheelFullMarking");
        assertThat(dto.role()).isEqualTo("Мотор-колесо");
        assertThat(dto.visibleIf()).isNotNull();
        assertThat(dto.visibleIf().field()).isEqualTo("isOwnProduction");
    }

    @Test
    @DisplayName("COMBOBOX-поле с options")
    void comboBoxWithOptions() {
        FieldDefinition comboField = new FieldDefinition(
                "poles", "Полюсность", FieldType.COMBOBOX,
                true, "4",
                List.of("2", "4", "6", "8"),
                null, null, null, null, null, null,
                null, null, null, 1, null
        );

        version.setFieldsJson(List.of(comboField));

        when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(fanTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

        DynamicFormMetadataDto result = builder.buildFormMetadata(10L);

        DynamicFormFieldDto dto = result.fields().get(0);
        assertThat(dto.type()).isEqualTo(FieldType.COMBOBOX);
        assertThat(dto.options()).containsExactly("2", "4", "6", "8");
        assertThat(dto.defaultValue()).isEqualTo("4");
    }

    // ==========================================================
    // ОШИБКИ
    // ==========================================================

    @Nested
    @DisplayName("Ошибки")
    class ErrorTests {

        @Test
        @DisplayName("null templateVersionId → исключение")
        void nullTemplateVersionId() {
            assertThatThrownBy(() -> builder.buildFormMetadata(null))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("не может быть пустым");
        }

        @Test
        @DisplayName("Версия не найдена → исключение")
        void versionNotFound() {
            when(fanTemplateVersionRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> builder.buildFormMetadata(999L))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("Версия шаблона не найдена");
        }

        @Test
        @DisplayName("Шаблон не найден → исключение")
        void templateNotFound() {
            version.setFieldsJson(List.of());

            when(fanTemplateVersionRepository.findById(10L)).thenReturn(Optional.of(version));
            when(fanTemplateRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> builder.buildFormMetadata(10L))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("Шаблон не найден");
        }
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    private static FieldDefinition field(String key, Integer displayOrder) {
        return new FieldDefinition(
                key, "Label " + key, FieldType.TEXT,
                false, null, null, null, null, null, null,
                null, null, null, null, null, displayOrder, null
        );
    }

    private static FieldDefinition separatorField(String label, Integer displayOrder) {
        return new FieldDefinition(
                "sep_" + label, label, FieldType.SEPARATOR,
                false, null, null, null, null, null, null,
                null, null, null, null, null, displayOrder, null
        );
    }

    private static FieldDefinition hiddenField(String key, Integer displayOrder) {
        return new FieldDefinition(
                key, "Hidden " + key, FieldType.HIDDEN,
                false, null, null, null, null, null, null,
                null, null, null, null, null, displayOrder, null
        );
    }
}

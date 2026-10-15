package com.fanproduction.services.impl;

import com.fanproduction.core.entity.product.FanCardEntity;
import com.fanproduction.core.entity.product.MotorCardEntity;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.enums.CardTemplateType;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.core.security.CurrentUserProvider;
import com.fanproduction.repositories.product.*;
import com.fanproduction.services.ComponentService;
import com.fanproduction.services.FanCardService;
import com.fanproduction.services.MaterialService;
import com.fanproduction.services.factory.ProductCardFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты интеграции FAN в {@link ProductCardServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCardService — FAN-интеграция")
class ProductCardServiceFANTest {

    @Mock private ProductCardRepository productCardRepository;
    @Mock private MotorCardRepository motorCardRepository;
    @Mock private MotorWheelCardRepository motorWheelCardRepository;
    @Mock private RadialWheelCardRepository radialWheelCardRepository;
    @Mock private AxialWheelCardRepository axialWheelCardRepository;
    @Mock private FanCardRepository fanCardRepository;
    @Mock private FanCardService fanCardService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CurrentUserProvider currentUserProvider;
    @Mock private ComponentService componentService;
    @Mock private MaterialService materialService;

    private ProductCardServiceImpl service;
    private ProductCardFactory realFactory;

    @BeforeEach
    void setUp() {
        realFactory = new ProductCardFactory();
        service = new ProductCardServiceImpl(
                productCardRepository,
                motorCardRepository,
                motorWheelCardRepository,
                radialWheelCardRepository,
                axialWheelCardRepository,
                realFactory,
                fanCardService,
                eventPublisher,
                currentUserProvider,
                fanCardRepository,
                componentService,
                materialService
        );
    }

    // ==========================================================
    // CREATE
    // ==========================================================

    @Nested
    @DisplayName("createCard")
    class CreateTests {

        @Test
        @DisplayName("FAN: делегирует в FanCardService.createFromTemplate")
        void createFanDelegates() {
            FanCardEntity expected = new FanCardEntity();
            expected.setId(500L);
            expected.setCardType("FAN");
            expected.setFullMarking("VRK-PatAIR-P-40-20-4-220");

            Map<String, Object> fields = new HashMap<>();
            fields.put("templateVersionId", 10L);
            fields.put("series", "P");
            fields.put("ductSize", "40-20");

            when(fanCardService.createFromTemplate(eq(10L), any(), eq("admin@test")))
                    .thenReturn(expected);

            var result = service.createCard(CardTemplateType.FAN, fields, "admin@test");

            assertThat(result).isSameAs(expected);
            verify(fanCardService).createFromTemplate(eq(10L), any(), eq("admin@test"));
            verify(productCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("FAN без templateVersionId → исключение")
        void createFanWithoutTemplateVersionId() {
            Map<String, Object> fields = Map.of("series", "P");

            assertThatThrownBy(() -> service.createCard(CardTemplateType.FAN, fields, "admin"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("templateVersionId");

            verify(fanCardService, never()).createFromTemplate(anyLong(), any(), anyString());
        }

        @Test
        @DisplayName("FAN: templateVersionId как строка → парсится в Long")
        void createFanTemplateVersionIdAsString() {
            FanCardEntity expected = new FanCardEntity();
            expected.setId(500L);

            Map<String, Object> fields = new HashMap<>();
            fields.put("templateVersionId", "10");
            fields.put("series", "P");

            when(fanCardService.createFromTemplate(eq(10L), any(), eq("admin")))
                    .thenReturn(expected);

            var result = service.createCard(CardTemplateType.FAN, fields, "admin");

            assertThat(result).isSameAs(expected);
        }

        @Test
        @DisplayName("MOTOR: использует фабрику, сохраняет карточку, аудит")
        void createMotorUsesFactory() {
            Map<String, Object> fields = new HashMap<>();
            fields.put("name", "Электродвигатель 100L2");
            fields.put("series", "АИР");
            fields.put("motorType", "100L");
            fields.put("poles", 2);
            fields.put("powerKw", 5.5);

            when(productCardRepository.save(any(MotorCardEntity.class)))
                    .thenAnswer(inv -> {
                        MotorCardEntity c = inv.getArgument(0);
                        c.setId(500L);
                        return c;
                    });

            var result = service.createCard(CardTemplateType.MOTOR, fields, "admin@test");

            assertThat(result).isInstanceOf(MotorCardEntity.class);
            assertThat(result.getCardType()).isEqualTo("MOTOR");
            assertThat(((MotorCardEntity) result).getMotorType()).isEqualTo("100L");
            assertThat(result.getCode()).isNotNull();

            verify(productCardRepository).save(any(MotorCardEntity.class));
            verify(fanCardService, never()).createFromTemplate(anyLong(), any(), anyString());

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().getAction()).isEqualTo(AuditAction.CREATE_CARD);
        }
    }

    // ==========================================================
    // UPDATE
    // ==========================================================

    @Nested
    @DisplayName("updateCard")
    class UpdateTests {

        @Test
        @DisplayName("FAN: делегирует в FanCardService.updateFromValues")
        void updateFanDelegates() {
            FanCardEntity existing = new FanCardEntity();
            existing.setId(500L);
            existing.setCardType("FAN");

            FanCardEntity updated = new FanCardEntity();
            updated.setId(500L);
            updated.setCardType("FAN");
            updated.setFullMarking("NEW-MARKING");

            Map<String, Object> fields = Map.of("series", "PS");

            when(productCardRepository.findById(500L)).thenReturn(Optional.of(existing));
            when(currentUserProvider.getCurrentUserEmail()).thenReturn("admin@test");
            when(fanCardService.updateFromValues(eq(500L), any(), eq("admin@test")))
                    .thenReturn(updated);

            var result = service.updateCard(500L, fields);

            assertThat(result).isSameAs(updated);
            verify(fanCardService).updateFromValues(eq(500L), any(), eq("admin@test"));
            verify(productCardRepository, never()).save(any());
        }

        @Test
        @DisplayName("MOTOR: применяет поля к существующей, не создаёт новую")
        void updateMotorAppliesToExisting() {
            MotorCardEntity existing = new MotorCardEntity();
            existing.setId(500L);
            existing.setCardType("MOTOR");
            existing.setCode("motor_original_code");
            existing.setCreatedBy("original@test");
            existing.setMotorType("100L");
            existing.setPowerKw(5.5);
            existing.setTemporary(false);

            Map<String, Object> fields = new HashMap<>();
            fields.put("powerKw", 7.5);
            fields.put("poles", 4);

            when(productCardRepository.findById(500L)).thenReturn(Optional.of(existing));
            when(currentUserProvider.getCurrentUserEmail()).thenReturn("admin@test");
            when(productCardRepository.save(any(MotorCardEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var result = service.updateCard(500L, fields);

            // Поля обновились
            assertThat(((MotorCardEntity) result).getPowerKw()).isEqualTo(7.5);
            assertThat(((MotorCardEntity) result).getPoles()).isEqualTo(4);

            // Системные поля сохранились
            assertThat(result.getCode()).isEqualTo("motor_original_code");
            assertThat(result.getCreatedBy()).isEqualTo("original@test");
            assertThat(result.getId()).isEqualTo(500L);

            verify(fanCardService, never()).updateFromValues(anyLong(), any(), anyString());
        }

        @Test
        @DisplayName("MOTOR: поле, которого нет в fields, не сбрасывается")
        void updateMotorPartial() {
            MotorCardEntity existing = new MotorCardEntity();
            existing.setId(500L);
            existing.setCardType("MOTOR");
            existing.setMotorType("100L");
            existing.setPowerKw(5.5);
            existing.setSeries("АИР");

            Map<String, Object> fields = new HashMap<>();
            fields.put("powerKw", 7.5);
            // series не передаём — не должен сброситься

            when(productCardRepository.findById(500L)).thenReturn(Optional.of(existing));
            when(currentUserProvider.getCurrentUserEmail()).thenReturn("admin");
            when(productCardRepository.save(any(MotorCardEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            var result = service.updateCard(500L, fields);

            assertThat(((MotorCardEntity) result).getSeries()).isEqualTo("АИР");
        }

        @Test
        @DisplayName("Карточка не найдена → исключение")
        void updateNotFound() {
            when(productCardRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateCard(999L, Map.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("не найдена");
        }
    }
}

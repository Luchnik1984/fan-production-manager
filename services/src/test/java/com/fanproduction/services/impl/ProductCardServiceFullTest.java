package com.fanproduction.services.impl;

import com.fanproduction.core.entity.component.ProductComponentEntity;
import com.fanproduction.core.entity.product.BaseProductCard;
import com.fanproduction.core.entity.product.FanCardEntity;
import com.fanproduction.core.entity.product.MotorCardEntity;
import com.fanproduction.core.enums.CardTemplateType;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.core.security.CurrentUserProvider;
import com.fanproduction.repositories.product.*;
import com.fanproduction.services.ComponentService;
import com.fanproduction.services.FanCardService;
import com.fanproduction.services.MaterialService;
import com.fanproduction.services.dto.ProductComponentItemData;
import com.fanproduction.services.dto.ProductMaterialItemData;
import com.fanproduction.services.factory.ProductCardFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты атомарного сохранения карточек.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductCardService — атомарное сохранение")
class ProductCardServiceFullTest {

    @Mock private ProductCardRepository productCardRepository;
    @Mock private MotorCardRepository motorCardRepository;
    @Mock private MotorWheelCardRepository motorWheelCardRepository;
    @Mock private RadialWheelCardRepository radialWheelCardRepository;
    @Mock private AxialWheelCardRepository axialWheelCardRepository;
    @Mock private FanCardRepository fanCardRepository;
    @Mock private FanCardService fanCardService;
    @Mock private ComponentService componentService;
    @Mock private MaterialService materialService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CurrentUserProvider currentUserProvider;

    private ProductCardServiceImpl service;

    @BeforeEach
    void setUp() {
        ProductCardFactory realFactory = new ProductCardFactory();
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
    // createFullCard
    // ==========================================================

    @Nested
    @DisplayName("createFullCard")
    class CreateFullTests {

        @Test
        @DisplayName("Создать MOTOR с 2 компонентами и 1 материалом")
        void createMotorWithComponentsAndMaterials() {
            Map<String, Object> fields = new HashMap<>();
            fields.put("series", "АИР");
            fields.put("motorType", "100L");

            when(productCardRepository.save(any(MotorCardEntity.class)))
                    .thenAnswer(inv -> {
                        MotorCardEntity c = inv.getArgument(0);
                        c.setId(500L);
                        return c;
                    });

            List<ProductComponentItemData> components = List.of(
                    new ProductComponentItemData(10L, 1.0, "Ступица", null),
                    new ProductComponentItemData(11L, 4.0, "Лопатка", null)
            );
            List<ProductMaterialItemData> materials = List.of(
                    new ProductMaterialItemData(20L, 0.5, null)
            );

            BaseProductCard result = service.createFullCard(
                    CardTemplateType.MOTOR,
                    "Мотор 100L",
                    fields,
                    components,
                    materials,
                    "admin@test"
            );

            assertThat(result.getId()).isEqualTo(500L);
            assertThat(result.getName()).isEqualTo("Мотор 100L");

            // Все компоненты добавлены
            verify(componentService).addComponentToProduct(eq(500L), eq(10L), anyDouble(), eq("Ступица"), any());
            verify(componentService).addComponentToProduct(eq(500L), eq(11L), anyDouble(), eq("Лопатка"), any());

            // Материал добавлен
            verify(materialService).addMaterialToProduct(eq(500L), eq(20L), anyDouble(), any());

            // Аудит
            verify(eventPublisher).publishEvent(any(AuditEvent.class));
        }

        @Test
        @DisplayName("Создать FAN с компонентами — делегирует в FanCardService")
        void createFanWithComponents() {
            FanCardEntity fan = new FanCardEntity();
            fan.setId(600L);
            fan.setCardType("FAN");

            Map<String, Object> fields = new HashMap<>();
            fields.put("templateVersionId", 3L);

            when(fanCardService.createFromTemplate(eq(3L), any(), anyString()))
                    .thenReturn(fan);

            BaseProductCard result = service.createFullCard(
                    CardTemplateType.FAN,
                    "FAN-карточка",
                    fields,
                    List.of(new ProductComponentItemData(10L, 1.0, "Роль", null)),
                    List.of(),
                    "admin@test"
            );

            assertThat(result.getId()).isEqualTo(600L);
            verify(componentService).addComponentToProduct(eq(600L), eq(10L), anyDouble(), eq("Роль"), any());
            verify(materialService, never()).addMaterialToProduct(anyLong(), anyLong(), anyDouble(), any());
        }

        @Test
        @DisplayName("Пустые списки — карточка без связей")
        void emptyLists() {
            when(productCardRepository.save(any(MotorCardEntity.class)))
                    .thenAnswer(inv -> {
                        MotorCardEntity c = inv.getArgument(0);
                        c.setId(500L);
                        return c;
                    });

            service.createFullCard(
                    CardTemplateType.MOTOR,
                    "M",
                    Map.of("series", "АИР"),
                    null,
                    null,
                    "admin"
            );

            verify(componentService, never()).addComponentToProduct(anyLong(), anyLong(), anyDouble(), any(), any());
            verify(materialService, never()).addMaterialToProduct(anyLong(), anyLong(), anyDouble(), any());
        }
    }

    // ==========================================================
    // updateFullCard
    // ==========================================================

    @Nested
    @DisplayName("updateFullCard")
    class UpdateFullTests {

        @Test
        @DisplayName("Обновить карточку + добавить новый компонент")
        void updateAndAddComponent() {
            MotorCardEntity existing = new MotorCardEntity();
            existing.setId(500L);
            existing.setCardType("MOTOR");

            when(productCardRepository.findById(500L)).thenReturn(Optional.of(existing));
            when(productCardRepository.save(any(MotorCardEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(currentUserProvider.getCurrentUserEmail()).thenReturn("admin@test");

            // materials == null → getMaterialsByProductCard не вызывается
            when(componentService.getComponentsByProductCard(500L)).thenReturn(List.of());

            List<ProductComponentItemData> components = List.of(
                    new ProductComponentItemData(10L, 2.0, "Ступица", null)
            );

            service.updateFullCard(
                    500L,
                    "M",
                    Map.of("powerKw", 5.5),
                    components,
                    null,
                    null,
                    null
            );

            verify(componentService).addComponentToProduct(eq(500L), eq(10L), eq(2.0), eq("Ступица"), any());
        }

        @Test
        @DisplayName("Удалить указанные компоненты")
        void removeComponents() {
            MotorCardEntity existing = new MotorCardEntity();
            existing.setId(500L);
            existing.setCardType("MOTOR");

            when(productCardRepository.findById(500L)).thenReturn(Optional.of(existing));
            when(productCardRepository.save(any(MotorCardEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(currentUserProvider.getCurrentUserEmail()).thenReturn("admin");
            // components == null, materials == null → стабы не нужны

            service.updateFullCard(
                    500L,
                    "M",
                    Map.of(),
                    null,
                    List.of(10L, 11L),
                    null,
                    null
            );

            verify(componentService).removeComponentFromProduct(500L, 10L);
            verify(componentService).removeComponentFromProduct(500L, 11L);
        }

        @Test
        @DisplayName("Обновить существующий компонент (количество)")
        void updateExistingComponent() {
            MotorCardEntity existing = new MotorCardEntity();
            existing.setId(500L);
            existing.setCardType("MOTOR");

            ProductComponentEntity existingLink = new ProductComponentEntity();
            existingLink.setComponentId(10L);
            existingLink.setProductCardId(500L);

            when(productCardRepository.findById(500L)).thenReturn(Optional.of(existing));
            when(productCardRepository.save(any(MotorCardEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(currentUserProvider.getCurrentUserEmail()).thenReturn("admin");
            when(componentService.getComponentsByProductCard(500L)).thenReturn(List.of(existingLink));
            // materials == null → getMaterialsByProductCard не нужен

            service.updateFullCard(
                    500L,
                    "M",
                    Map.of(),
                    List.of(new ProductComponentItemData(10L, 5.0, "Ступица", null)),
                    null,
                    null,
                    null
            );

            verify(componentService).updateComponentQuantity(500L, 10L, 5.0);
            verify(componentService).updateComponentPosition(500L, 10L, "Ступица");
            verify(componentService, never()).addComponentToProduct(anyLong(), anyLong(), anyDouble(), any(), any());
        }
    }
}

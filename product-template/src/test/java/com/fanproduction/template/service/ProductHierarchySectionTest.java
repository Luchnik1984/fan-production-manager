package com.fanproduction.template.service;

import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.ProductSectionDto;
import com.fanproduction.template.entity.ProductSection;
import com.fanproduction.template.entity.ProductType;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.repository.ProductSectionRepository;
import com.fanproduction.template.repository.ProductSeriesRepository;
import com.fanproduction.template.repository.ProductSpeciesRepository;
import com.fanproduction.template.repository.ProductTypeRepository;
import com.fanproduction.template.service.impl.ProductHierarchyServiceImpl;
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
 * Юнит-тесты разделов продукции: CRUD, защита от циклов, построение дерева.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductHierarchyService — разделы")
class ProductHierarchySectionTest {

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private ProductSpeciesRepository productSpeciesRepository;

    @Mock
    private ProductSeriesRepository productSeriesRepository;

    @Mock
    private ProductSectionRepository productSectionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductHierarchyServiceImpl service;

    private static ProductSection section(Long id, Long parentId, String name, int sortOrder) {
        ProductSection s = new ProductSection();
        s.setId(id);
        s.setParentId(parentId);
        s.setName(name);
        s.setSortOrder(sortOrder);
        s.setIsSystem(false);
        return s;
    }

    private static ProductType type(Long id, Long sectionId, String designation) {
        ProductType t = new ProductType();
        t.setId(id);
        t.setSectionId(sectionId);
        t.setDesignation(designation);
        t.setName(designation);
        t.setIsSystem(false);
        return t;
    }

    @Nested
    @DisplayName("Создание раздела")
    class Create {

        @Test
        @DisplayName("корневой раздел получает sort_order на 10 больше максимального")
        void rootSection_getsNextSortOrder() {
            when(productSectionRepository.findByParentIdIsNullAndNameIgnoreCase("Клапаны"))
                    .thenReturn(Optional.empty());
            when(productSectionRepository.findByParentIdIsNullOrderBySortOrderAscIdAsc())
                    .thenReturn(List.of(section(1L, null, "Комплектующие", 10),
                            section(2L, null, "Вентиляторы", 20)));
            when(productSectionRepository.save(any(ProductSection.class)))
                    .thenAnswer(inv -> {
                        ProductSection s = inv.getArgument(0);
                        s.setId(99L);
                        return s;
                    });

            ProductSectionDto created = service.createSection(null, "Клапаны", null, "admin@test");

            assertThat(created.id()).isEqualTo(99L);
            assertThat(created.sortOrder()).isEqualTo(30);
            assertThat(created.name()).isEqualTo("Клапаны");
        }

        @Test
        @DisplayName("дубль имени среди соседей — ошибка")
        void duplicateName_throws() {
            when(productSectionRepository.findByParentIdIsNullAndNameIgnoreCase(any()))
                    .thenReturn(Optional.of(section(2L, null, "Вентиляторы", 20)));

            assertThatThrownBy(() -> service.createSection(null, "вентиляторы", 10, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("уже существует");

            verify(productSectionRepository, never()).save(any());
        }

        @Test
        @DisplayName("несуществующий родитель — ошибка")
        void parentNotFound_throws() {
            when(productSectionRepository.existsById(404L)).thenReturn(false);

            assertThatThrownBy(() -> service.createSection(404L, "Подраздел", null, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("Родительский раздел не найден");
        }

        @Test
        @DisplayName("создание пишет событие аудита")
        void publishesAudit() {
            when(productSectionRepository.findByParentIdIsNullAndNameIgnoreCase("Стаканы"))
                    .thenReturn(Optional.empty());
            when(productSectionRepository.save(any(ProductSection.class)))
                    .thenAnswer(inv -> {
                        ProductSection s = inv.getArgument(0);
                        s.setId(7L);
                        return s;
                    });

            service.createSection(null, "Стаканы", 5, "admin@test");

            ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().getDetails()).contains("Стаканы");
        }
    }

    @Nested
    @DisplayName("Перемещение раздела")
    class Move {

        @Test
        @DisplayName("перемещение в самого себя — ошибка")
        void intoItself_throws() {
            when(productSectionRepository.findById(1L))
                    .thenReturn(Optional.of(section(1L, null, "Вентиляторы", 10)));

            assertThatThrownBy(() -> service.moveSection(1L, 1L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("в самого себя");
        }

        @Test
        @DisplayName("перемещение внутрь собственного потомка — ошибка (цикл)")
        void intoOwnDescendant_throws() {
            ProductSection root = section(1L, null, "Вентиляторы", 10);
            ProductSection child = section(2L, 1L, "Канальные", 10);
            ProductSection grandChild = section(3L, 2L, "Прямоугольные", 10);

            when(productSectionRepository.findById(1L)).thenReturn(Optional.of(root));
            when(productSectionRepository.existsById(3L)).thenReturn(true);
            when(productSectionRepository.findById(3L)).thenReturn(Optional.of(grandChild));
            when(productSectionRepository.findById(2L)).thenReturn(Optional.of(child));

            assertThatThrownBy(() -> service.moveSection(1L, 3L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("цикл");
        }

        @Test
        @DisplayName("корректное перемещение меняет родителя")
        void validMove_changesParent() {
            ProductSection moving = section(3L, null, "Козырьки", 40);
            ProductSection target = section(1L, null, "Комплектующие", 10);

            when(productSectionRepository.findById(3L)).thenReturn(Optional.of(moving));
            when(productSectionRepository.existsById(1L)).thenReturn(true);
            when(productSectionRepository.findById(1L)).thenReturn(Optional.of(target));
            when(productSectionRepository.save(any(ProductSection.class))).thenAnswer(inv -> inv.getArgument(0));

            service.moveSection(3L, 1L, "admin@test");

            assertThat(moving.getParentId()).isEqualTo(1L);
            verify(productSectionRepository).save(moving);
        }
    }

    @Nested
    @DisplayName("Перемещение типа в раздел")
    class MoveTypeToSection {

        @Test
        @DisplayName("тип переносится в указанный раздел")
        void movesType() {
            ProductType type = type(10L, null, "VRK-PatAIR");
            ProductSection target = section(2L, null, "Вентиляторы", 20);

            when(productTypeRepository.findById(10L)).thenReturn(Optional.of(type));
            when(productSectionRepository.findById(2L)).thenReturn(Optional.of(target));
            when(productTypeRepository.save(any(ProductType.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.moveTypeToSection(10L, 2L, "admin@test");

            assertThat(result.sectionId()).isEqualTo(2L);
            assertThat(type.getSectionId()).isEqualTo(2L);
            verify(productTypeRepository).save(type);
        }

        @Test
        @DisplayName("несуществующий раздел — ошибка")
        void unknownSection_throws() {
            when(productTypeRepository.findById(10L))
                    .thenReturn(Optional.of(type(10L, null, "VRK-PatAIR")));
            when(productSectionRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.moveTypeToSection(10L, 404L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("Раздел не найден");

            verify(productTypeRepository, never()).save(any());
        }

        @Test
        @DisplayName("перемещение в «без раздела» очищает sectionId")
        void moveToNoSection() {
            ProductType type = type(10L, 2L, "VRK-PatAIR");
            when(productTypeRepository.findById(10L)).thenReturn(Optional.of(type));
            when(productTypeRepository.save(any(ProductType.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.moveTypeToSection(10L, null, "admin@test");

            assertThat(result.sectionId()).isNull();
        }
    }

    @Nested
    @DisplayName("Удаление раздела")
    class Delete {

        @Test
        @DisplayName("системный раздел удалить нельзя")
        void systemSection_throws() {
            ProductSection system = section(1L, null, "Вентиляторы", 10);
            system.setIsSystem(true);
            when(productSectionRepository.findById(1L)).thenReturn(Optional.of(system));

            assertThatThrownBy(() -> service.deleteSection(1L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("Системный раздел");
        }

        @Test
        @DisplayName("раздел с подразделами удалить нельзя")
        void withChildren_throws() {
            when(productSectionRepository.findById(1L))
                    .thenReturn(Optional.of(section(1L, null, "Вентиляторы", 10)));
            when(productSectionRepository.countByParentId(1L)).thenReturn(2L);

            assertThatThrownBy(() -> service.deleteSection(1L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("есть подразделы");
        }

        @Test
        @DisplayName("раздел с типами продукции удалить нельзя")
        void withTypes_throws() {
            when(productSectionRepository.findById(1L))
                    .thenReturn(Optional.of(section(1L, null, "Вентиляторы", 10)));
            when(productSectionRepository.countByParentId(1L)).thenReturn(0L);
            when(productTypeRepository.countBySectionId(1L)).thenReturn(3L);

            assertThatThrownBy(() -> service.deleteSection(1L, "admin@test"))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("есть типы продукции");
        }

        @Test
        @DisplayName("пустой пользовательский раздел удаляется")
        void emptySection_deleted() {
            ProductSection empty = section(5L, null, "Козырьки", 50);
            when(productSectionRepository.findById(5L)).thenReturn(Optional.of(empty));
            when(productSectionRepository.countByParentId(5L)).thenReturn(0L);
            when(productTypeRepository.countBySectionId(5L)).thenReturn(0L);

            service.deleteSection(5L, "admin@test");

            verify(productSectionRepository).delete(empty);
        }
    }

    @Nested
    @DisplayName("Дерево разделов")
    class Tree {

        @Test
        @DisplayName("типы раскладываются по разделам, подразделы вложены")
        void buildsHierarchy() {
            ProductSection root = section(1L, null, "Вентиляторы", 10);
            ProductSection child = section(2L, 1L, "Канальные", 10);

            when(productSectionRepository.findAllByOrderBySortOrderAscIdAsc())
                    .thenReturn(List.of(root, child));
            when(productTypeRepository.findAllByOrderByDesignationAsc())
                    .thenReturn(List.of(type(10L, 2L, "VRK-PatAIR")));
            when(productSpeciesRepository.findAll()).thenReturn(List.of());
            when(productSeriesRepository.findAll()).thenReturn(List.of());

            List<ProductSectionDto> tree = service.getSectionTree();

            assertThat(tree).hasSize(1);
            ProductSectionDto rootDto = tree.get(0);
            assertThat(rootDto.name()).isEqualTo("Вентиляторы");
            assertThat(rootDto.types()).isEmpty();
            assertThat(rootDto.sections()).hasSize(1);

            ProductSectionDto childDto = rootDto.sections().get(0);
            assertThat(childDto.name()).isEqualTo("Канальные");
            assertThat(childDto.types()).hasSize(1);
            assertThat(childDto.types().get(0).designation()).isEqualTo("VRK-PatAIR");
        }
    }
}

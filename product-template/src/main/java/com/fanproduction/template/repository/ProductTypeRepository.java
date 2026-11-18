package com.fanproduction.template.repository;

import com.fanproduction.template.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий типов продукции (верхний уровень иерархии).
 */
@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

    /**
     * Найти тип по обозначению (точное совпадение, регистронезависимо).
     * Обозначение уникально — вернёт один результат или пусто.
     */
    Optional<ProductType> findByDesignationIgnoreCase(String designation);

    /**
     * Проверить существование типа с таким обозначением (регистронезависимо).
     * Используется для проверки уникальности при создании/обновлении.
     */
    boolean existsByDesignationIgnoreCase(String designation);

    /**
     * Найти тип по наименованию (точное совпадение).
     */
    Optional<ProductType> findByName(String name);

    /**
     * Поиск по части наименования (регистронезависимо).
     * Для автодополнения при вводе.
     */
    List<ProductType> findByNameContainingIgnoreCase(String namePart);

    /**
     * Все типы, отсортированные по обозначению.
     * Используется для отображения в UI.
     */
    List<ProductType> findAllByOrderByDesignationAsc();

    /**
     * Типы указанного раздела, отсортированные по обозначению.
     */
    List<ProductType> findAllBySectionIdOrderByDesignationAsc(Long sectionId);

    /**
     * Количество типов в разделе — защита от удаления непустого раздела.
     */
    long countBySectionId(Long sectionId);
}

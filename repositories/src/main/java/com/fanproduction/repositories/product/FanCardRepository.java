package com.fanproduction.repositories.product;

import com.fanproduction.core.entity.product.FanCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface FanCardRepository extends JpaRepository<FanCardEntity, Long> {

    /**
     * Поиск вентиляторов по типоразмеру
     */
    List<FanCardEntity> findBySize(Double size);

    /**
     * Поиск по классу (общеобменный, дымоудаление)
     */
    List<FanCardEntity> findByFanClass(String fanClass);

    /**
     * Поиск по типу вентилятора (осевой, радиальный, канальный)
     */
    List<FanCardEntity> findByFanType(String fanType);


    /**
     * Поиск по диапазону типоразмеров
     */
    @Query("SELECT f FROM FanCardEntity f WHERE f.size BETWEEN :minSize AND :maxSize")
    List<FanCardEntity> findBySizeRange(@Param("minSize") Double minSize,
                                        @Param("maxSize") Double maxSize);

    /**
     * Поиск по полной маркировке (регистронезависимо).
     */
    Optional<FanCardEntity> findByFullMarkingIgnoreCase(String fullMarking);

    /**
     * Проверить существование карточки с такой маркировкой.
     */
    boolean existsByFullMarkingIgnoreCase(String fullMarking);

    /**
     * Поиск FAN-карточек по имени типа вентилятора.
     * <p>
     * Пример значения: {@code "Вентилятор канальный"}.
     */
    Page<FanCardEntity> findByFanType(String fanType, Pageable pageable);
}

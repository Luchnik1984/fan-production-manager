package com.fanproduction.template.service.impl;

import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.ProductSeriesDto;
import com.fanproduction.template.dto.ProductSpeciesDto;
import com.fanproduction.template.dto.ProductTypeDto;
import com.fanproduction.template.dto.ProductTypeShortDto;
import com.fanproduction.template.entity.ProductSeries;
import com.fanproduction.template.entity.ProductSpecies;
import com.fanproduction.template.entity.ProductType;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.repository.ProductSeriesRepository;
import com.fanproduction.template.repository.ProductSpeciesRepository;
import com.fanproduction.template.repository.ProductTypeRepository;
import com.fanproduction.template.service.ProductHierarchyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Реализация сервиса управления иерархией вентиляторов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductHierarchyServiceImpl implements ProductHierarchyService {

    private final ProductTypeRepository productTypeRepository;
    private final ProductSpeciesRepository productSpeciesRepository;
    private final ProductSeriesRepository productSeriesRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ==========================================================
    // TYPE
    // ==========================================================

    @Override
    public List<ProductTypeDto> getAllTypes() {
        return productTypeRepository.findAllByOrderByDesignationAsc().stream()
                .map(type -> toTypeDto(type, null))
                .toList();
    }

    @Override
    public ProductTypeDto getTypeById(Long id) {
        ProductType type = productTypeRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Тип не найден: " + id));
        return toTypeDto(type, null);
    }

    @Override
    @Transactional
    public ProductTypeDto createType(String name, String designation, String createdBy) {
        validateNotEmpty(designation, "Обозначение типа");

        if (productTypeRepository.existsByDesignationIgnoreCase(designation)) {
            throw new TemplateValidationException(
                    "Тип с обозначением '" + designation.trim() + "' уже существует");
        }

        ProductType type = new ProductType();
        type.setName(trimOrNull(name));
        type.setDesignation(designation.trim());
        type.setIsSystem(false);
        type.setCreatedBy(createdBy);

        ProductType saved = productTypeRepository.save(type);

        publishAudit(createdBy, AuditAction.TEMPLATE_CREATE,
                "Создан тип вентилятора: " + saved.getDesignation());

        log.info("Тип вентилятора создан: id={}, designation={}", saved.getId(), saved.getDesignation());

        return toTypeDto(saved, null);
    }

    @Override
    @Transactional
    public ProductTypeDto updateType(Long id, String name, String designation, String updatedBy) {
        ProductType type = productTypeRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Тип не найден: " + id));

        String newDesignation = designation != null ? designation.trim() : null;
        String oldDesignation = type.getDesignation();

        // Если обозначение изменилось — проверяем уникальность
        if (newDesignation != null && !newDesignation.equalsIgnoreCase(oldDesignation)) {
            validateNotEmpty(newDesignation, "Обозначение типа");
            if (productTypeRepository.existsByDesignationIgnoreCase(newDesignation)) {
                throw new TemplateValidationException(
                        "Тип с обозначением '" + newDesignation + "' уже существует");
            }
            type.setDesignation(newDesignation);
        }

        // Обновляем name
        String newName = trimOrNull(name);
        if (!Objects.equals(newName, type.getName())) {
            type.setName(newName);
        }

        ProductType saved = productTypeRepository.save(type);

        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Обновлён тип вентилятора: " + saved.getDesignation());

        return toTypeDto(saved, null);
    }

    @Override
    @Transactional
    public void deleteType(Long id, String deletedBy) {
        ProductType type = productTypeRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Тип не найден: " + id));

        if (type.getIsSystem() != null && type.getIsSystem()) {
            throw new TemplateValidationException("Системный тип нельзя удалить: " + type.getDesignation());
        }

        long speciesCount = productSpeciesRepository.countByTypeId(id);
        if (speciesCount > 0) {
            throw new TemplateValidationException(
                    "Нельзя удалить тип '" + type.getDesignation()
                            + "': в нём есть виды (" + speciesCount + "). Сначала удалите виды.");
        }

        productTypeRepository.delete(type);

        publishAudit(deletedBy, AuditAction.TEMPLATE_DELETE,
                "Удалён тип вентилятора: " + type.getDesignation());

        log.info("Тип вентилятора удалён: id={}, designation={}", id, type.getDesignation());
    }

    // ==========================================================
    // SPECIES
    // ==========================================================

    @Override
    public List<ProductSpeciesDto> getSpeciesByType(Long typeId) {
        return productSpeciesRepository.findByTypeIdOrderByNameAsc(typeId).stream()
                .map(species -> toSpeciesDto(species, null))
                .toList();
    }

    @Override
    public ProductSpeciesDto getSpeciesById(Long id) {
        ProductSpecies species = productSpeciesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Вид не найден: " + id));
        return toSpeciesDto(species, null);
    }

    @Override
    @Transactional
    public ProductSpeciesDto createSpecies(Long typeId, String name, String createdBy) {
        // Проверяем, что тип существует
        if (!productTypeRepository.existsById(typeId)) {
            throw new TemplateValidationException("Тип не найден: " + typeId);
        }

        String trimmedName = trimOrNull(name);
        validateSpeciesNameUnique(typeId, trimmedName, null);

        ProductSpecies species = new ProductSpecies();
        species.setTypeId(typeId);
        species.setName(trimmedName);
        species.setIsSystem(false);
        species.setCreatedBy(createdBy);

        ProductSpecies saved = productSpeciesRepository.save(species);

        String displayForLog = trimmedName != null ? trimmedName : "No_species";
        publishAudit(createdBy, AuditAction.TEMPLATE_CREATE,
                "Создан вид вентилятора: " + displayForLog + " (тип ID=" + typeId + ")");

        return toSpeciesDto(saved, null);
    }

    @Override
    @Transactional
    public ProductSpeciesDto updateSpecies(Long id, String name, String updatedBy) {
        ProductSpecies species = productSpeciesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Вид не найден: " + id));

        String newName = trimOrNull(name);
        String oldName = species.getName();

        if (!Objects.equals(newName, oldName)) {
            validateSpeciesNameUnique(species.getTypeId(), newName, id);
            species.setName(newName);
        }

        ProductSpecies saved = productSpeciesRepository.save(species);

        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Обновлён вид вентилятора: ID=" + id);

        return toSpeciesDto(saved, null);
    }

    @Override
    @Transactional
    public void deleteSpecies(Long id, String deletedBy) {
        ProductSpecies species = productSpeciesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Вид не найден: " + id));

        if (species.getIsSystem() != null && species.getIsSystem()) {
            throw new TemplateValidationException("Системный вид нельзя удалить");
        }

        long seriesCount = productSeriesRepository.countBySpeciesId(id);
        if (seriesCount > 0) {
            throw new TemplateValidationException(
                    "Нельзя удалить вид: в нём есть серии (" + seriesCount + "). Сначала удалите серии.");
        }

        productSpeciesRepository.delete(species);

        publishAudit(deletedBy, AuditAction.TEMPLATE_DELETE,
                "Удалён вид вентилятора: ID=" + id);
    }

    // ==========================================================
    // SERIES
    // ==========================================================

    @Override
    public List<ProductSeriesDto> getSeriesBySpecies(Long speciesId) {
        return productSeriesRepository.findBySpeciesIdOrderByNameAsc(speciesId).stream()
                .map(this::toSeriesDto)
                .toList();
    }

    @Override
    public ProductSeriesDto getSeriesById(Long id) {
        ProductSeries series = productSeriesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Серия не найдена: " + id));
        return toSeriesDto(series);
    }

    @Override
    @Transactional
    public ProductSeriesDto createSeries(Long speciesId, String name, String createdBy) {
        if (!productSpeciesRepository.existsById(speciesId)) {
            throw new TemplateValidationException("Вид не найден: " + speciesId);
        }

        String trimmedName = trimOrNull(name);
        validateSeriesNameUnique(speciesId, trimmedName, null);

        ProductSeries series = new ProductSeries();
        series.setSpeciesId(speciesId);
        series.setName(trimmedName);
        series.setIsSystem(false);
        series.setCreatedBy(createdBy);

        ProductSeries saved = productSeriesRepository.save(series);

        String displayForLog = trimmedName != null ? trimmedName : "No_series";
        publishAudit(createdBy, AuditAction.TEMPLATE_CREATE,
                "Создана серия вентилятора: " + displayForLog + " (вид ID=" + speciesId + ")");

        return toSeriesDto(saved);
    }

    @Override
    @Transactional
    public ProductSeriesDto updateSeries(Long id, String name, String updatedBy) {
        ProductSeries series = productSeriesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Серия не найдена: " + id));

        String newName = trimOrNull(name);
        String oldName = series.getName();

        if (!Objects.equals(newName, oldName)) {
            validateSeriesNameUnique(series.getSpeciesId(), newName, id);
            series.setName(newName);
        }

        ProductSeries saved = productSeriesRepository.save(series);

        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Обновлена серия вентилятора: ID=" + id);

        return toSeriesDto(saved);
    }

    @Override
    @Transactional
    public void deleteSeries(Long id, String deletedBy) {
        ProductSeries series = productSeriesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Серия не найдена: " + id));

        if (series.getIsSystem() != null && series.getIsSystem()) {
            throw new TemplateValidationException("Системную серию нельзя удалить");
        }

        productSeriesRepository.delete(series);

        publishAudit(deletedBy, AuditAction.TEMPLATE_DELETE,
                "Удалена серия вентилятора: ID=" + id);
    }

    // ==========================================================
    // SHORT LIST (для ComboBox)
    // ==========================================================

    @Override
    public List<ProductTypeShortDto> getShortList() {
        return productTypeRepository.findAllByOrderByDesignationAsc().stream()
                .map(this::toShortDto)
                .toList();
    }

    private ProductTypeShortDto toShortDto(ProductType type) {
        String filterValue = (type.getName() != null && !type.getName().isBlank())
                ? type.getName()
                : type.getDesignation();

        String displayName = (type.getName() != null && !type.getName().isBlank())
                ? type.getName() + " (" + type.getDesignation() + ")"
                : type.getDesignation();

        return new ProductTypeShortDto(type.getId(), displayName, filterValue);
    }

    // ==========================================================
    // TREE
    // ==========================================================

    @Override
    public List<ProductTypeDto> getTree() {
        // Загружаем всё одним махом (на масштабах < 1000 записей это дешевле, чем N+1)
        List<ProductType> types = productTypeRepository.findAllByOrderByDesignationAsc();
        List<ProductSpecies> allSpecies = productSpeciesRepository.findAll();
        List<ProductSeries> allSeries = productSeriesRepository.findAll();

        // Группируем species по typeId
        Map<Long, List<ProductSpecies>> speciesByType = allSpecies.stream()
                .collect(Collectors.groupingBy(ProductSpecies::getTypeId));

        // Группируем series по speciesId
        Map<Long, List<ProductSeries>> seriesBySpecies = allSeries.stream()
                .collect(Collectors.groupingBy(ProductSeries::getSpeciesId));

        return types.stream()
                .map(type -> {
                    List<ProductSpeciesDto> speciesDtos = speciesByType
                            .getOrDefault(type.getId(), List.of()).stream()
                            .map(species -> {
                                List<ProductSeriesDto> seriesDtos = seriesBySpecies
                                        .getOrDefault(species.getId(), List.of()).stream()
                                        .map(this::toSeriesDto)
                                        .toList();
                                return toSpeciesDto(species, seriesDtos);
                            })
                            .toList();
                    return toTypeDto(type, speciesDtos);
                })
                .toList();
    }

    // ==========================================================
    // ВАЛИДАЦИЯ
    // ==========================================================

    private void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new TemplateValidationException(fieldName + " не может быть пустым");
        }
    }

    private void validateSpeciesNameUnique(Long typeId, String name, Long excludeId) {
        if (name == null) {
            // Проверяем, что нет другого No_species
            boolean exists = productSpeciesRepository.existsByTypeIdAndNameIsNull(typeId);
            if (exists) {
                throw new TemplateValidationException(
                        "В этом типе уже есть вид без наименования (No_species)");
            }
        } else {
            boolean exists = productSpeciesRepository.existsByTypeIdAndNameIgnoreCase(typeId, name);
            if (exists) {
                // При обновлении нужно исключить себя
                if (excludeId != null) {
                    ProductSpecies existing = productSpeciesRepository
                            .findByTypeIdAndNameIgnoreCase(typeId, name)
                            .orElse(null);
                    if (existing != null && existing.getId().equals(excludeId)) {
                        return; // это тот же самый — не считаем дубликатом
                    }
                }
                throw new TemplateValidationException(
                        "Вид с именем '" + name + "' уже существует в этом типе");
            }
        }
    }

    private void validateSeriesNameUnique(Long speciesId, String name, Long excludeId) {
        if (name == null) {
            boolean exists = productSeriesRepository.existsBySpeciesIdAndNameIsNull(speciesId);
            if (exists) {
                throw new TemplateValidationException(
                        "В этом виде уже есть серия без наименования (No_series)");
            }
        } else {
            boolean exists = productSeriesRepository.existsBySpeciesIdAndNameIgnoreCase(speciesId, name);
            if (exists) {
                if (excludeId != null) {
                    ProductSeries existing = productSeriesRepository
                            .findBySpeciesIdAndNameIgnoreCase(speciesId, name)
                            .orElse(null);
                    if (existing != null && existing.getId().equals(excludeId)) {
                        return;
                    }
                }
                throw new TemplateValidationException(
                        "Серия с именем '" + name + "' уже существует в этом виде");
            }
        }
    }

    // ==========================================================
    // МАППИНГ
    // ==========================================================

    private ProductTypeDto toTypeDto(ProductType type, List<ProductSpeciesDto> species) {
        String displayName = (type.getName() != null && !type.getName().isBlank())
                ? type.getName() + " (" + type.getDesignation() + ")"
                : type.getDesignation();
        return new ProductTypeDto(
                type.getId(),
                type.getName(),
                type.getDesignation(),
                displayName,
                type.getIsSystem(),
                type.getCreatedAt(),
                type.getCreatedBy(),
                species
        );
    }

    private ProductSpeciesDto toSpeciesDto(ProductSpecies species, List<ProductSeriesDto> series) {
        String displayName = (species.getName() != null && !species.getName().isBlank())
                ? species.getName()
                : "No_species";
        return new ProductSpeciesDto(
                species.getId(),
                species.getTypeId(),
                species.getName(),
                displayName,
                species.getIsSystem(),
                species.getCreatedAt(),
                species.getCreatedBy(),
                series
        );
    }

    private ProductSeriesDto toSeriesDto(ProductSeries series) {
        String displayName = (series.getName() != null && !series.getName().isBlank())
                ? series.getName()
                : "No_series";
        return new ProductSeriesDto(
                series.getId(),
                series.getSpeciesId(),
                series.getName(),
                displayName,
                series.getIsSystem(),
                series.getCreatedAt(),
                series.getCreatedBy()
        );
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void publishAudit(String username, AuditAction action, String details) {
        String actor = (username != null && !username.isBlank()) ? username : "system";
        eventPublisher.publishEvent(new AuditEvent(this, actor, action, details));
    }
}

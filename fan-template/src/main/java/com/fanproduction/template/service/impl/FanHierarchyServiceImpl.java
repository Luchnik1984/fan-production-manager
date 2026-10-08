package com.fanproduction.template.service.impl;

import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.FanSeriesDto;
import com.fanproduction.template.dto.FanSpeciesDto;
import com.fanproduction.template.dto.FanTypeDto;
import com.fanproduction.template.entity.FanSeries;
import com.fanproduction.template.entity.FanSpecies;
import com.fanproduction.template.entity.FanType;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.repository.FanSeriesRepository;
import com.fanproduction.template.repository.FanSpeciesRepository;
import com.fanproduction.template.repository.FanTypeRepository;
import com.fanproduction.template.service.FanHierarchyService;
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
public class FanHierarchyServiceImpl implements FanHierarchyService {

    private final FanTypeRepository fanTypeRepository;
    private final FanSpeciesRepository fanSpeciesRepository;
    private final FanSeriesRepository fanSeriesRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ==========================================================
    // TYPE
    // ==========================================================

    @Override
    public List<FanTypeDto> getAllTypes() {
        return fanTypeRepository.findAllByOrderByDesignationAsc().stream()
                .map(type -> toTypeDto(type, null))
                .toList();
    }

    @Override
    public FanTypeDto getTypeById(Long id) {
        FanType type = fanTypeRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Тип не найден: " + id));
        return toTypeDto(type, null);
    }

    @Override
    @Transactional
    public FanTypeDto createType(String name, String designation, String createdBy) {
        validateNotEmpty(designation, "Обозначение типа");

        if (fanTypeRepository.existsByDesignationIgnoreCase(designation)) {
            throw new TemplateValidationException(
                    "Тип с обозначением '" + designation.trim() + "' уже существует");
        }

        FanType type = new FanType();
        type.setName(trimOrNull(name));
        type.setDesignation(designation.trim());
        type.setIsSystem(false);
        type.setCreatedBy(createdBy);

        FanType saved = fanTypeRepository.save(type);

        publishAudit(createdBy, AuditAction.TEMPLATE_CREATE,
                "Создан тип вентилятора: " + saved.getDesignation());

        log.info("Тип вентилятора создан: id={}, designation={}", saved.getId(), saved.getDesignation());

        return toTypeDto(saved, null);
    }

    @Override
    @Transactional
    public FanTypeDto updateType(Long id, String name, String designation, String updatedBy) {
        FanType type = fanTypeRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Тип не найден: " + id));

        String newDesignation = designation != null ? designation.trim() : null;
        String oldDesignation = type.getDesignation();

        // Если обозначение изменилось — проверяем уникальность
        if (newDesignation != null && !newDesignation.equalsIgnoreCase(oldDesignation)) {
            validateNotEmpty(newDesignation, "Обозначение типа");
            if (fanTypeRepository.existsByDesignationIgnoreCase(newDesignation)) {
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

        FanType saved = fanTypeRepository.save(type);

        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Обновлён тип вентилятора: " + saved.getDesignation());

        return toTypeDto(saved, null);
    }

    @Override
    @Transactional
    public void deleteType(Long id, String deletedBy) {
        FanType type = fanTypeRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Тип не найден: " + id));

        if (type.getIsSystem() != null && type.getIsSystem()) {
            throw new TemplateValidationException("Системный тип нельзя удалить: " + type.getDesignation());
        }

        long speciesCount = fanSpeciesRepository.countByTypeId(id);
        if (speciesCount > 0) {
            throw new TemplateValidationException(
                    "Нельзя удалить тип '" + type.getDesignation()
                            + "': в нём есть виды (" + speciesCount + "). Сначала удалите виды.");
        }

        fanTypeRepository.delete(type);

        publishAudit(deletedBy, AuditAction.TEMPLATE_DELETE,
                "Удалён тип вентилятора: " + type.getDesignation());

        log.info("Тип вентилятора удалён: id={}, designation={}", id, type.getDesignation());
    }

    // ==========================================================
    // SPECIES
    // ==========================================================

    @Override
    public List<FanSpeciesDto> getSpeciesByType(Long typeId) {
        return fanSpeciesRepository.findByTypeIdOrderByNameAsc(typeId).stream()
                .map(species -> toSpeciesDto(species, null))
                .toList();
    }

    @Override
    public FanSpeciesDto getSpeciesById(Long id) {
        FanSpecies species = fanSpeciesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Вид не найден: " + id));
        return toSpeciesDto(species, null);
    }

    @Override
    @Transactional
    public FanSpeciesDto createSpecies(Long typeId, String name, String createdBy) {
        // Проверяем, что тип существует
        if (!fanTypeRepository.existsById(typeId)) {
            throw new TemplateValidationException("Тип не найден: " + typeId);
        }

        String trimmedName = trimOrNull(name);
        validateSpeciesNameUnique(typeId, trimmedName, null);

        FanSpecies species = new FanSpecies();
        species.setTypeId(typeId);
        species.setName(trimmedName);
        species.setIsSystem(false);
        species.setCreatedBy(createdBy);

        FanSpecies saved = fanSpeciesRepository.save(species);

        String displayForLog = trimmedName != null ? trimmedName : "No_species";
        publishAudit(createdBy, AuditAction.TEMPLATE_CREATE,
                "Создан вид вентилятора: " + displayForLog + " (тип ID=" + typeId + ")");

        return toSpeciesDto(saved, null);
    }

    @Override
    @Transactional
    public FanSpeciesDto updateSpecies(Long id, String name, String updatedBy) {
        FanSpecies species = fanSpeciesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Вид не найден: " + id));

        String newName = trimOrNull(name);
        String oldName = species.getName();

        if (!Objects.equals(newName, oldName)) {
            validateSpeciesNameUnique(species.getTypeId(), newName, id);
            species.setName(newName);
        }

        FanSpecies saved = fanSpeciesRepository.save(species);

        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Обновлён вид вентилятора: ID=" + id);

        return toSpeciesDto(saved, null);
    }

    @Override
    @Transactional
    public void deleteSpecies(Long id, String deletedBy) {
        FanSpecies species = fanSpeciesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Вид не найден: " + id));

        if (species.getIsSystem() != null && species.getIsSystem()) {
            throw new TemplateValidationException("Системный вид нельзя удалить");
        }

        long seriesCount = fanSeriesRepository.countBySpeciesId(id);
        if (seriesCount > 0) {
            throw new TemplateValidationException(
                    "Нельзя удалить вид: в нём есть серии (" + seriesCount + "). Сначала удалите серии.");
        }

        fanSpeciesRepository.delete(species);

        publishAudit(deletedBy, AuditAction.TEMPLATE_DELETE,
                "Удалён вид вентилятора: ID=" + id);
    }

    // ==========================================================
    // SERIES
    // ==========================================================

    @Override
    public List<FanSeriesDto> getSeriesBySpecies(Long speciesId) {
        return fanSeriesRepository.findBySpeciesIdOrderByNameAsc(speciesId).stream()
                .map(this::toSeriesDto)
                .toList();
    }

    @Override
    public FanSeriesDto getSeriesById(Long id) {
        FanSeries series = fanSeriesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Серия не найдена: " + id));
        return toSeriesDto(series);
    }

    @Override
    @Transactional
    public FanSeriesDto createSeries(Long speciesId, String name, String createdBy) {
        if (!fanSpeciesRepository.existsById(speciesId)) {
            throw new TemplateValidationException("Вид не найден: " + speciesId);
        }

        String trimmedName = trimOrNull(name);
        validateSeriesNameUnique(speciesId, trimmedName, null);

        FanSeries series = new FanSeries();
        series.setSpeciesId(speciesId);
        series.setName(trimmedName);
        series.setIsSystem(false);
        series.setCreatedBy(createdBy);

        FanSeries saved = fanSeriesRepository.save(series);

        String displayForLog = trimmedName != null ? trimmedName : "No_series";
        publishAudit(createdBy, AuditAction.TEMPLATE_CREATE,
                "Создана серия вентилятора: " + displayForLog + " (вид ID=" + speciesId + ")");

        return toSeriesDto(saved);
    }

    @Override
    @Transactional
    public FanSeriesDto updateSeries(Long id, String name, String updatedBy) {
        FanSeries series = fanSeriesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Серия не найдена: " + id));

        String newName = trimOrNull(name);
        String oldName = series.getName();

        if (!Objects.equals(newName, oldName)) {
            validateSeriesNameUnique(series.getSpeciesId(), newName, id);
            series.setName(newName);
        }

        FanSeries saved = fanSeriesRepository.save(series);

        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Обновлена серия вентилятора: ID=" + id);

        return toSeriesDto(saved);
    }

    @Override
    @Transactional
    public void deleteSeries(Long id, String deletedBy) {
        FanSeries series = fanSeriesRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Серия не найдена: " + id));

        if (series.getIsSystem() != null && series.getIsSystem()) {
            throw new TemplateValidationException("Системную серию нельзя удалить");
        }

        fanSeriesRepository.delete(series);

        publishAudit(deletedBy, AuditAction.TEMPLATE_DELETE,
                "Удалена серия вентилятора: ID=" + id);
    }

    // ==========================================================
    // TREE
    // ==========================================================

    @Override
    public List<FanTypeDto> getTree() {
        // Загружаем всё одним махом (на масштабах < 1000 записей это дешевле, чем N+1)
        List<FanType> types = fanTypeRepository.findAllByOrderByDesignationAsc();
        List<FanSpecies> allSpecies = fanSpeciesRepository.findAll();
        List<FanSeries> allSeries = fanSeriesRepository.findAll();

        // Группируем species по typeId
        Map<Long, List<FanSpecies>> speciesByType = allSpecies.stream()
                .collect(Collectors.groupingBy(FanSpecies::getTypeId));

        // Группируем series по speciesId
        Map<Long, List<FanSeries>> seriesBySpecies = allSeries.stream()
                .collect(Collectors.groupingBy(FanSeries::getSpeciesId));

        return types.stream()
                .map(type -> {
                    List<FanSpeciesDto> speciesDtos = speciesByType
                            .getOrDefault(type.getId(), List.of()).stream()
                            .map(species -> {
                                List<FanSeriesDto> seriesDtos = seriesBySpecies
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
            boolean exists = fanSpeciesRepository.existsByTypeIdAndNameIsNull(typeId);
            if (exists) {
                throw new TemplateValidationException(
                        "В этом типе уже есть вид без наименования (No_species)");
            }
        } else {
            boolean exists = fanSpeciesRepository.existsByTypeIdAndNameIgnoreCase(typeId, name);
            if (exists) {
                // При обновлении нужно исключить себя
                if (excludeId != null) {
                    FanSpecies existing = fanSpeciesRepository
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
            boolean exists = fanSeriesRepository.existsBySpeciesIdAndNameIsNull(speciesId);
            if (exists) {
                throw new TemplateValidationException(
                        "В этом виде уже есть серия без наименования (No_series)");
            }
        } else {
            boolean exists = fanSeriesRepository.existsBySpeciesIdAndNameIgnoreCase(speciesId, name);
            if (exists) {
                if (excludeId != null) {
                    FanSeries existing = fanSeriesRepository
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

    private FanTypeDto toTypeDto(FanType type, List<FanSpeciesDto> species) {
        String displayName = (type.getName() != null && !type.getName().isBlank())
                ? type.getName() + " (" + type.getDesignation() + ")"
                : type.getDesignation();
        return new FanTypeDto(
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

    private FanSpeciesDto toSpeciesDto(FanSpecies species, List<FanSeriesDto> series) {
        String displayName = (species.getName() != null && !species.getName().isBlank())
                ? species.getName()
                : "No_species";
        return new FanSpeciesDto(
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

    private FanSeriesDto toSeriesDto(FanSeries series) {
        String displayName = (series.getName() != null && !series.getName().isBlank())
                ? series.getName()
                : "No_series";
        return new FanSeriesDto(
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

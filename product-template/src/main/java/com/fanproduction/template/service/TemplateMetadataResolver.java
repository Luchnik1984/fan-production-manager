package com.fanproduction.template.service;

import com.fanproduction.template.entity.ProductSeries;
import com.fanproduction.template.entity.ProductSpecies;
import com.fanproduction.template.entity.ProductTemplate;
import com.fanproduction.template.entity.ProductTemplateVersion;
import com.fanproduction.template.entity.ProductType;
import com.fanproduction.template.repository.ProductSeriesRepository;
import com.fanproduction.template.repository.ProductSpeciesRepository;
import com.fanproduction.template.repository.ProductTemplateRepository;
import com.fanproduction.template.repository.ProductTemplateVersionRepository;
import com.fanproduction.template.repository.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Резолвер метаданных шаблона (US9.8).
 * <p>
 * По {@code templateVersionId} возвращает системные поля типа/серии/вида,
 * доступные в правиле маркировки через {@code FIELD(...)}:
 * <ul>
 *   <li>{@code typeDesignation} — designation типа (например, «VRK-PatAIR»).</li>
 *   <li>{@code typeName} — имя типа (например, «Вентилятор канальный»).</li>
 *   <li>{@code seriesName} — имя серии (например, «PRV»).</li>
 *   <li>{@code speciesName} — имя вида (например, «Прямоугольный»).</li>
 * </ul>
 * <p>
 * Возвращает {@code null} для отсутствующих полей (например, если у типа
 * нет name). Пустые строки также заменяются на {@code null}, чтобы
 * в маркировку не попадали пробелы.
 * <p>
 * Кэширование — на уровне вызывающего (MarkingContext). Метод сам кэш не держит:
 * при вызове из одного контекста маркировки — один запрос; при повторных
 * вызовах в рамках одной транзакции — JPA-кэш первого уровня.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateMetadataResolver {

    private final ProductTemplateVersionRepository versionRepository;
    private final ProductTemplateRepository templateRepository;
    private final ProductSeriesRepository seriesRepository;
    private final ProductSpeciesRepository speciesRepository;
    private final ProductTypeRepository typeRepository;

    /**
     * Резолвит метаданные шаблона по ID версии.
     *
     * @param templateVersionId ID версии шаблона
     * @return карта из 4 ключей (typeDesignation, typeName, seriesName, speciesName).
     *         Значения могут быть {@code null}. Если версия не найдена — пустая карта.
     */
    @Transactional(readOnly = true)
    public Map<String, String> resolve(Long templateVersionId) {
        Map<String, String> result = new HashMap<>();
        if (templateVersionId == null) {
            return result;
        }

        // 1. version → template
        ProductTemplateVersion version = versionRepository.findById(templateVersionId).orElse(null);
        if (version == null) {
            log.debug("Версия шаблона не найдена: {}", templateVersionId);
            return result;
        }

        ProductTemplate template = templateRepository.findById(version.getTemplateId()).orElse(null);
        if (template == null) {
            return result;
        }

        // 2. template → series
        ProductSeries series = seriesRepository.findById(template.getSeriesId()).orElse(null);
        if (series != null) {
            result.put("seriesName", emptyToNull(series.getName()));
        }

        // 3. series → species
        ProductSpecies species = null;
        if (series != null) {
            species = speciesRepository.findById(series.getSpeciesId()).orElse(null);
            if (species != null) {
                result.put("speciesName", emptyToNull(species.getName()));
            }
        }

        // 4. species → type
        if (species != null) {
            ProductType type = typeRepository.findById(species.getTypeId()).orElse(null);
            if (type != null) {
                result.put("typeDesignation", emptyToNull(type.getDesignation()));
                result.put("typeName", emptyToNull(type.getName()));
            }
        }

        return result;
    }

    private String emptyToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

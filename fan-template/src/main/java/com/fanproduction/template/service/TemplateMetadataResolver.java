package com.fanproduction.template.service;

import com.fanproduction.template.entity.FanSeries;
import com.fanproduction.template.entity.FanSpecies;
import com.fanproduction.template.entity.FanTemplate;
import com.fanproduction.template.entity.FanTemplateVersion;
import com.fanproduction.template.entity.FanType;
import com.fanproduction.template.repository.FanSeriesRepository;
import com.fanproduction.template.repository.FanSpeciesRepository;
import com.fanproduction.template.repository.FanTemplateRepository;
import com.fanproduction.template.repository.FanTemplateVersionRepository;
import com.fanproduction.template.repository.FanTypeRepository;
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

    private final FanTemplateVersionRepository versionRepository;
    private final FanTemplateRepository templateRepository;
    private final FanSeriesRepository seriesRepository;
    private final FanSpeciesRepository speciesRepository;
    private final FanTypeRepository typeRepository;

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
        FanTemplateVersion version = versionRepository.findById(templateVersionId).orElse(null);
        if (version == null) {
            log.debug("Версия шаблона не найдена: {}", templateVersionId);
            return result;
        }

        FanTemplate template = templateRepository.findById(version.getTemplateId()).orElse(null);
        if (template == null) {
            return result;
        }

        // 2. template → series
        FanSeries series = seriesRepository.findById(template.getSeriesId()).orElse(null);
        if (series != null) {
            result.put("seriesName", emptyToNull(series.getName()));
        }

        // 3. series → species
        FanSpecies species = null;
        if (series != null) {
            species = speciesRepository.findById(series.getSpeciesId()).orElse(null);
            if (species != null) {
                result.put("speciesName", emptyToNull(species.getName()));
            }
        }

        // 4. species → type
        if (species != null) {
            FanType type = typeRepository.findById(species.getTypeId()).orElse(null);
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

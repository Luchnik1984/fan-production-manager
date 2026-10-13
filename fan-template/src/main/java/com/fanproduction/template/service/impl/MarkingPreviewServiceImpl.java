package com.fanproduction.template.service.impl;

import com.fanproduction.template.entity.FanTemplateVersion;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.repository.FanTemplateVersionRepository;
import com.fanproduction.template.service.MarkingEngine;
import com.fanproduction.template.service.MarkingPreviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Реализация сервиса предпросмотра маркировки.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MarkingPreviewServiceImpl implements MarkingPreviewService {

    private final FanTemplateVersionRepository fanTemplateVersionRepository;
    private final MarkingEngine markingEngine;

    @Override
    public String preview(Long templateVersionId, Map<String, Object> values) {
        if (templateVersionId == null) {
            throw new TemplateValidationException("ID версии шаблона не может быть пустым");
        }

        FanTemplateVersion version = fanTemplateVersionRepository.findById(templateVersionId)
                .orElseThrow(() -> new TemplateValidationException(
                        "Версия шаблона не найдена: " + templateVersionId));

        List<FieldDefinition> fields = version.getFieldsJson();
        MarkingRule rule = version.getMarkingRuleJson();

        if (fields == null || fields.isEmpty() || rule == null
                || rule.sequence() == null || rule.sequence().isEmpty()) {
            return "";
        }

        Map<String, Object> safeValues = values != null ? values : Map.of();

        try {
            String marking = markingEngine.evaluate(rule, safeValues, fields);
            return marking != null ? marking : "";
        } catch (Exception e) {
            log.warn("Ошибка вычисления маркировки для preview (versionId={}): {}",
                    templateVersionId, e.getMessage());
            return "";
        }
    }
}

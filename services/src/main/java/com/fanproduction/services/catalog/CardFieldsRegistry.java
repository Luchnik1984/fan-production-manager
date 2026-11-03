package com.fanproduction.services.catalog;

import com.fanproduction.core.dto.FieldInfoDto;
import com.fanproduction.core.entity.component.ComponentEntity;
import com.fanproduction.core.entity.product.AxialWheelCardEntity;
import com.fanproduction.core.entity.product.MotorCardEntity;
import com.fanproduction.core.entity.product.MotorWheelCardEntity;
import com.fanproduction.core.entity.product.RadialWheelCardEntity;
import com.fanproduction.core.enums.CardTemplateType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Каталог полей связанных сущностей.
 * <p>
 * Возвращает список полей для:
 * <ul>
 *   <li>{@link CardTemplateType#MOTOR} — {@link MotorCardEntity};</li>
 *   <li>{@link CardTemplateType#MOTOR_WHEEL} — {@link MotorWheelCardEntity};</li>
 *   <li>{@link CardTemplateType#RADIAL_WHEEL} — {@link RadialWheelCardEntity};</li>
 *   <li>{@link CardTemplateType#AXIAL_WHEEL} — {@link AxialWheelCardEntity};</li>
 *   <li>{@code "COMPONENT"} — {@link ComponentEntity} (не входит в CardTemplateType).</li>
 * </ul>
 * <p>
 * Для поддерживаемых типов — список {@link FieldInfoDto} (key, label, type).
 * <p>
 * <b>Архитектурное решение:</b> хардкод в Java, а не reflection по сущности.
 * Причины:
 * <ul>
 *   <li>Нужен русский label — reflection его не даёт (пришлось бы аннотировать
 *       каждое поле 20+ сущностей).</li>
 *   <li>Нужен порядок — сначала важные поля (poles, voltage, powerKw).</li>
 *   <li>Хардкод в одном месте + тест на соответствие сущности
 *       ({@link #validateAgainstEntity(Class, List)}) — проще, чем аннотировать.</li>
 * </ul>
 */
@Slf4j
@Service
public class CardFieldsRegistry {

    /** Псевдо-тип для компонентов (не входит в CardTemplateType). */
    public static final String COMPONENT_TYPE = "COMPONENT";

    private final Map<CardTemplateType, List<FieldInfoDto>> fieldsByType =
            new EnumMap<>(CardTemplateType.class);
    private final List<FieldInfoDto> componentFields;

    public CardFieldsRegistry() {
        fieldsByType.put(CardTemplateType.MOTOR, motorFields());
        fieldsByType.put(CardTemplateType.MOTOR_WHEEL, motorWheelFields());
        fieldsByType.put(CardTemplateType.RADIAL_WHEEL, radialWheelFields());
        fieldsByType.put(CardTemplateType.AXIAL_WHEEL, axialWheelFields());
        componentFields = componentFields();

        // Валидация на старте — все поля из реестра должны существовать в сущности
        validateAgainstEntity(MotorCardEntity.class, fieldsByType.get(CardTemplateType.MOTOR));
        validateAgainstEntity(MotorWheelCardEntity.class, fieldsByType.get(CardTemplateType.MOTOR_WHEEL));
        validateAgainstEntity(RadialWheelCardEntity.class, fieldsByType.get(CardTemplateType.RADIAL_WHEEL));
        validateAgainstEntity(AxialWheelCardEntity.class, fieldsByType.get(CardTemplateType.AXIAL_WHEEL));
        validateAgainstEntity(ComponentEntity.class, componentFields);
    }

    // ==========================================================
    // PUBLIC API
    // ==========================================================

    /**
     * Получить список полей по коду типа.
     *
     * @param type код типа ({@code MOTOR}, {@code MOTOR_WHEEL}, {@code RADIAL_WHEEL},
     *             {@code AXIAL_WHEEL}, {@code COMPONENT}), регистронезависимо
     * @return список полей (пустой, если тип неизвестен)
     */
    public List<FieldInfoDto> getFields(String type) {
        if (type == null || type.isBlank()) {
            return Collections.emptyList();
        }
        String upper = type.trim().toUpperCase();

        if (COMPONENT_TYPE.equals(upper)) {
            return componentFields;
        }

        try {
            CardTemplateType cardType = CardTemplateType.valueOf(upper);
            return fieldsByType.getOrDefault(cardType, Collections.emptyList());
        } catch (IllegalArgumentException e) {
            log.debug("Неизвестный тип карточки для fields: {}", type);
            return Collections.emptyList();
        }
    }

    // ==========================================================
    // КАТАЛОГИ
    // ==========================================================

    private List<FieldInfoDto> motorFields() {
        List<FieldInfoDto> list = new ArrayList<>();
        list.add(new FieldInfoDto("series", "Серия", "TEXT"));
        list.add(new FieldInfoDto("motorType", "Тип двигателя", "TEXT"));
        list.add(new FieldInfoDto("poles", "Количество полюсов", "INTEGER"));
        list.add(new FieldInfoDto("powerKw", "Мощность (кВт)", "DOUBLE"));
        list.add(new FieldInfoDto("ratedSpeedRpm", "Номинальная скорость (об/мин)", "INTEGER"));
        list.add(new FieldInfoDto("actualSpeedRpm", "Фактическая скорость (об/мин)", "INTEGER"));
        list.add(new FieldInfoDto("shaftSize", "Размер вала (мм)", "INTEGER"));
        list.add(new FieldInfoDto("mountingType", "Исполнение по монтажу", "TEXT"));
        list.add(new FieldInfoDto("climateType", "Климатическое исполнение", "TEXT"));
        list.add(new FieldInfoDto("voltage", "Напряжение (В)", "INTEGER"));
        list.add(new FieldInfoDto("operationMode", "Режим работы", "TEXT"));
        list.add(new FieldInfoDto("weightKg", "Масса (кг)", "DOUBLE"));
        list.add(new FieldInfoDto("fullMarking", "Полная маркировка", "TEXT"));
        return list;
    }

    private List<FieldInfoDto> motorWheelFields() {
        List<FieldInfoDto> list = new ArrayList<>();
        list.add(new FieldInfoDto("manufacturer", "Производитель", "TEXT"));
        list.add(new FieldInfoDto("manufacturerMarking", "Маркировка производителя", "TEXT"));
        list.add(new FieldInfoDto("bladeType", "Тип лопаток", "TEXT"));
        list.add(new FieldInfoDto("size", "Размер (мм)", "INTEGER"));
        list.add(new FieldInfoDto("poles", "Количество полюсов", "INTEGER"));
        list.add(new FieldInfoDto("voltageCode", "Код напряжения", "TEXT"));
        list.add(new FieldInfoDto("powerKw", "Мощность (кВт)", "DOUBLE"));
        list.add(new FieldInfoDto("ratedSpeedRpm", "Номинальная скорость (об/мин)", "INTEGER"));
        list.add(new FieldInfoDto("actualSpeedRpm", "Фактическая скорость (об/мин)", "INTEGER"));
        list.add(new FieldInfoDto("voltage", "Напряжение (В)", "INTEGER"));
        list.add(new FieldInfoDto("weightKg", "Масса (кг)", "DOUBLE"));
        list.add(new FieldInfoDto("motorCode", "Код двигателя", "TEXT"));
        list.add(new FieldInfoDto("fullMarking", "Полная маркировка", "TEXT"));
        return list;
    }

    private List<FieldInfoDto> radialWheelFields() {
        List<FieldInfoDto> list = new ArrayList<>();
        list.add(new FieldInfoDto("manufacturer", "Производитель", "TEXT"));
        list.add(new FieldInfoDto("series", "Серия колеса", "TEXT"));
        list.add(new FieldInfoDto("size", "Размер колеса (мм)", "DOUBLE"));
        list.add(new FieldInfoDto("marking", "Маркировка", "TEXT"));
        list.add(new FieldInfoDto("bladeType", "Тип лопаток", "TEXT"));
        list.add(new FieldInfoDto("bladeMod", "Модификация лопатки", "TEXT"));
        list.add(new FieldInfoDto("frontDiskMod", "Модификация переднего диска", "TEXT"));
        list.add(new FieldInfoDto("wheelWidth", "Ширина колеса", "DOUBLE"));
        list.add(new FieldInfoDto("bladeCount", "Количество лопаток", "INTEGER"));
        list.add(new FieldInfoDto("bladeLengthCoeff", "Коэф. длины лопатки", "DOUBLE"));
        list.add(new FieldInfoDto("wheelCode", "Код колеса", "TEXT"));
        list.add(new FieldInfoDto("wheelFormula", "Формула колеса", "TEXT"));
        list.add(new FieldInfoDto("diameter", "Диаметр (мм)", "INTEGER"));
        list.add(new FieldInfoDto("maxSpeedRpm", "Максимальная скорость (об/мин)", "INTEGER"));
        list.add(new FieldInfoDto("weightKg", "Масса (кг)", "DOUBLE"));
        list.add(new FieldInfoDto("fullMarking", "Полная маркировка", "TEXT"));
        return list;
    }

    private List<FieldInfoDto> axialWheelFields() {
        List<FieldInfoDto> list = new ArrayList<>();
        list.add(new FieldInfoDto("manufacturer", "Производитель", "TEXT"));
        list.add(new FieldInfoDto("series", "Серия колеса", "TEXT"));
        list.add(new FieldInfoDto("size", "Типоразмер", "DOUBLE"));
        list.add(new FieldInfoDto("marking", "Маркировка", "TEXT"));
        list.add(new FieldInfoDto("trimCoefficient", "Коэф. подрезки (%)", "DOUBLE"));
        list.add(new FieldInfoDto("bladeType", "Тип лопатки", "TEXT"));
        list.add(new FieldInfoDto("bladeCount", "Количество лопаток", "INTEGER"));
        list.add(new FieldInfoDto("bladeAngle", "Угол установки", "INTEGER"));
        list.add(new FieldInfoDto("bladeMaterial", "Материал лопатки", "TEXT"));
        list.add(new FieldInfoDto("maxBladeCount", "Макс. кол-во лопаток", "INTEGER"));
        list.add(new FieldInfoDto("wheelDiameter", "Диаметр колеса (мм)", "INTEGER"));
        list.add(new FieldInfoDto("wheelFormula", "Формула колеса", "TEXT"));
        list.add(new FieldInfoDto("maxSpeedRpm", "Максимальная скорость (об/мин)", "INTEGER"));
        list.add(new FieldInfoDto("weightKg", "Масса (кг)", "DOUBLE"));
        list.add(new FieldInfoDto("fullMarking", "Полная маркировка", "TEXT"));
        return list;
    }

    private List<FieldInfoDto> componentFields() {
        List<FieldInfoDto> list = new ArrayList<>();
        list.add(new FieldInfoDto("name", "Наименование", "TEXT"));
        list.add(new FieldInfoDto("designation", "Обозначение", "TEXT"));
        list.add(new FieldInfoDto("vendorCode", "Артикул", "TEXT"));
        list.add(new FieldInfoDto("description", "Описание", "TEXT"));
        list.add(new FieldInfoDto("weightKg", "Масса (кг)", "DOUBLE"));
        list.add(new FieldInfoDto("material", "Материал", "TEXT"));
        list.add(new FieldInfoDto("classId", "ID класса", "INTEGER"));
        list.add(new FieldInfoDto("unitId", "ID единицы измерения", "INTEGER"));
        return list;
    }

    // ==========================================================
    // ВАЛИДАЦИЯ
    // ==========================================================

    /**
     * Проверить, что все поля из реестра существуют в сущности.
     * <p>
     * Вызывается на старте (в конструкторе). Если поле в реестре указано
     * с опечаткой (или удалено из сущности) — бросается {@link IllegalStateException}
     * при инициализации Spring, чтобы проблема обнаружилась сразу.
     */
    private void validateAgainstEntity(Class<?> entityClass, List<FieldInfoDto> fields) {
        if (entityClass == null) {
            throw new IllegalArgumentException("entityClass не может быть null");
        }
        // Собираем все поля сущности (включая унаследованные от BaseProductCard)
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = entityClass;
        while (current != null && current != Object.class) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }

        for (FieldInfoDto field : fields) {
            boolean found = false;
            for (Class<?> clazz : hierarchy) {
                try {
                    Field f = clazz.getDeclaredField(field.key());
                    if (!Modifier.isStatic(f.getModifiers())) {
                        found = true;
                        break;
                    }
                } catch (NoSuchFieldException ignored) {
                    // Пробуем следующий класс иерархии
                }
            }
            if (!found) {
                throw new IllegalStateException(
                        "CardFieldsRegistry: поле '" + field.key() + "' не найдено в сущности "
                                + entityClass.getSimpleName() + ". Проверьте каталог.");
            }
        }

        log.debug("CardFieldsRegistry: {} — {} полей, все найдены в сущности",
                entityClass.getSimpleName(), fields.size());
    }
}
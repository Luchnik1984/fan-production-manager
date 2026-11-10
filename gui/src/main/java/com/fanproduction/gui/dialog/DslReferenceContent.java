package com.fanproduction.gui.dialog;

import java.util.List;

/**
 * Содержимое справочника DSL (Спринт 9, US9.4.а).
 * <p>
 * Хардкод разделов, описаний и примеров. Сознательно НЕ читается из файлов:
 * <ul>
 *   <li>Fat JAR: не нужны пути к ресурсам.</li>
 *   <li>Единый источник правды: текст обновляется вместе с кодом.</li>
 *   <li>При добавлении новой функции DSL — обновить этот класс.</li>
 * </ul>
 * <p>
 * Структура: список {@link Section}, каждый раздел имеет заголовок и
 * список {@link Entry} — подразделов (имя + тело).
 */
public final class DslReferenceContent {

    private DslReferenceContent() {
    }

    /**
     * Раздел справочника верхнего уровня.
     *
     * @param title   заголовок в дереве
     * @param entries подразделы (имя + текст)
     */
    public record Section(String title, List<Entry> entries) {
    }

    /**
     * Подраздел — «лист» дерева.
     *
     * @param title  имя (например, «DIVIDE»)
     * @param body   тело (markdown-подобный текст, показывается в TextArea)
     * @param sample пример, который копируется по кнопке (может быть {@code null})
     */
    public record Entry(String title, String body, String sample) {
    }

    // ==========================================================
    // ВСЕ РАЗДЕЛЫ
    // ==========================================================

    public static final List<Section> SECTIONS = List.of(

            // ========== ВВЕДЕНИЕ ==========
            new Section("Введение", List.of(
                    new Entry("Что такое DSL",
                            """
                            DSL (Domain Specific Language) — декларативный язык формул,
                            на котором администратор описывает вычисляемые поля
                            в шаблоне карточки вентилятора.

                            Формула — это выражение (не последовательность команд),
                            возвращающее значение. Пример:

                              FLOOR(DIVIDE(FIELD('wheelSize'), 10))

                            Это значит: взять поле wheelSize, разделить на 10,
                            округлить вниз.

                            ─────────────────────────────────────────────
                            ГДЕ ИСПОЛЬЗУЕТСЯ

                            • Поля типа COMPUTED (вычисляемые поля)
                            • Значения в CONDITION (условия видимости,
                              обязательности, ветвления в маркировке)

                            ─────────────────────────────────────────────
                            БЕЗОПАСНОСТЬ

                            DSL исполняется интерпретатором на сервере.
                            Никакого доступа к Java, файлам, сети.
                            Ограничения:
                              • длина формулы ≤ 5000 символов
                              • глубина вложенности ≤ 50
                            """,
                            null),

                    new Entry("Обзор синтаксиса",
                            """
                            ЛИТЕРАЛЫ
                              'текст'       строка в одинарных кавычках
                              10            целое число
                              3.14          дробное число
                              true, false   логические

                            ПОЛЕ КАРТОЧКИ
                              FIELD('имя')  значение поля карточки

                            ВЫЗОВ ФУНКЦИИ
                              ИМЯ(арг1, арг2, ...)
                              Имена функций регистронезависимы:
                              floor(...) = FLOOR(...)

                            СЛОВАРЬ (для MAP)
                              {380: 'D', 220: 'E'}
                              {ключ: значение, ...}

                            ─────────────────────────────────────────────
                            ВЛОЖЕННОСТЬ

                            Любые вызовы можно вкладывать:
                              FLOOR(DIVIDE(FIELD('x'), 10))
                              IF(EQUALS(FIELD('y'), 1), 'A', 'B')
                              MAP(FIELD('voltage'), {380: 'D', 220: 'E'})

                            ─────────────────────────────────────────────
                            ЧТО ВОЗВРАЩАЕТ

                            Формула возвращает:
                              • строку  (например, CONCAT(...))
                              • число   (например, DIVIDE(...))
                              • boolean (например, EQUALS(...))
                              • null    (если вычислить невозможно)

                            ─────────────────────────────────────────────
                            ОШИБКИ

                            При ошибке COMPUTED-поля возвращается null
                            (не ломается вся карточка). Подробности —
                            в логах сервера.
                            """,
                            null)
            )),

            // ========== МАТЕМАТИЧЕСКИЕ ==========
            new Section("Функции: математические", List.of(
                    new Entry("DIVIDE", """
                            DIVIDE(a, b) — деление.

                            Возвращает: число (double).
                            Ошибка: если b = 0.

                            Пример: FLOOR(DIVIDE(FIELD('wheelSize'), 10))
                            """, "DIVIDE(225, 10)"),

                    new Entry("MULTIPLY", """
                            MULTIPLY(a, b) — умножение.

                            Возвращает: число.
                            """, "MULTIPLY(3, 4)"),

                    new Entry("ADD", """
                            ADD(a, b) — сложение.

                            Возвращает: число.
                            """, "ADD(3, 4)"),

                    new Entry("SUBTRACT", """
                            SUBTRACT(a, b) — вычитание (a − b).

                            Возвращает: число.
                            """, "SUBTRACT(10, 3)"),

                    new Entry("FLOOR", """
                            FLOOR(x) — округление вниз (к меньшему целому).

                            Пример: FLOOR(22.7) → 22
                            """, "FLOOR(22.7)"),

                    new Entry("CEIL", """
                            CEIL(x) — округление вверх (к большему целому).

                            Пример: CEIL(22.2) → 23
                            """, "CEIL(22.2)"),

                    new Entry("ROUND", """
                            ROUND(x) — математическое округление (0.5 вверх).

                            Пример: ROUND(22.5) → 23
                            """, "ROUND(22.5)")
            )),

            // ========== СТРОКОВЫЕ ==========
            new Section("Функции: строковые", List.of(
                    new Entry("CONCAT", """
                            CONCAT(a, b, ...) — конкатенация строк.

                            Любое количество аргументов. null → пустая строка.

                            Пример: CONCAT('VRK-', FIELD('series'), '-40-20')
                            """, "CONCAT('VRK-', FIELD('series'), '-40-20')"),

                    new Entry("UPPER", """
                            UPPER(s) — верхний регистр.
                            """, "UPPER('abc')"),

                    new Entry("LOWER", """
                            LOWER(s) — нижний регистр.
                            """, "LOWER('ABC')"),

                    new Entry("TRIM", """
                            TRIM(s) — удалить пробелы по краям.
                            """, "TRIM('  x  ')"),

                    new Entry("FORMAT_NUMBER", """
                            FORMAT_NUMBER(x) — число → строка.

                            Целое — без дроби: 5.0 → «5»
                            Дробное — с запятой: 5.6 → «5,6»
                            """, "FORMAT_NUMBER(5.6)")
            )),

            // ========== ЛОГИЧЕСКИЕ ==========
            new Section("Функции: логические", List.of(
                    new Entry("IF", """
                            IF(условие, then, else) — условный оператор.

                            Возвращает: значение then или else
                            в зависимости от условия.

                            Пример: IF(EQUALS(FIELD('x'), 1), 'A', 'B')
                            """, "IF(EQUALS(FIELD('x'), 1), 'A', 'B')"),

                    new Entry("EQUALS", """
                            EQUALS(a, b) — равенство.

                            Числа сравниваются как числа,
                            строки — как строки.
                            EQUALS(380, '380') → true
                            """, "EQUALS(380, '380')"),

                    new Entry("NOT_EQUALS", """
                            NOT_EQUALS(a, b) — неравенство.
                            """, "NOT_EQUALS(1, 2)"),

                    new Entry("GREATER", """
                            GREATER(a, b) — a > b (числовое).
                            """, "GREATER(5, 3)"),

                    new Entry("LESS", """
                            LESS(a, b) — a < b (числовое).
                            """, "LESS(5, 3)"),

                    new Entry("AND", """
                            AND(a, b, ...) — логическое И.

                            true, только если ВСЕ аргументы true.
                            """, "AND(true, true, false)"),

                    new Entry("OR", """
                            OR(a, b, ...) — логическое ИЛИ.

                            true, если ХОТЯ БЫ ОДИН аргумент true.
                            """, "OR(false, false, true)"),

                    new Entry("NOT", """
                            NOT(a) — логическое отрицание.
                            """, "NOT(true)")
            )),

            // ========== ПРОВЕРКИ ==========
            new Section("Функции: проверки", List.of(
                    new Entry("EMPTY", """
                            EMPTY(x) — true, если x пусто.

                            Пусто = null, пустая строка, пустая коллекция.
                            """, "EMPTY(FIELD('x'))"),

                    new Entry("NOT_EMPTY", """
                            NOT_EMPTY(x) — true, если x НЕ пусто.
                            """, "NOT_EMPTY(FIELD('x'))"),

                    new Entry("DEFAULT", """
                            DEFAULT(x, значение) — если x пусто, вернуть значение.

                            Пример: DEFAULT(FIELD('x'), 'N/A')
                            """, "DEFAULT(FIELD('x'), 'N/A')"),

                    new Entry("COALESCE", """
                            COALESCE(a, b, c, ...) — первое непустое значение.

                            Пример: COALESCE(FIELD('a'), FIELD('b'), 'X')
                            """, "COALESCE(FIELD('a'), FIELD('b'), 'X')")
            )),

            // ========== MAP ==========
            new Section("Функции: MAP", List.of(
                    new Entry("MAP", """
                            MAP(value, словарь) — маппинг значений.

                            Возвращает значение из словаря по ключу = value,
                            или null, если ключа нет.

                            Пример: MAP(FIELD('voltage'), {380: 'D', 220: 'E'})
                            → для 380 вернёт 'D'
                            """, "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})")
            )),

            // ========== ТЕРМИНЫ ==========
            new Section("Термины", List.of(
                    new Entry("referenceType", """
                            Тип связанной сущности. Только для полей типа SELECTABLE.

                            Возможные значения:
                              • MOTOR          — электродвигатель
                              • MOTOR_WHEEL    — мотор-колесо
                              • RADIAL_WHEEL   — радиальное колесо
                              • AXIAL_WHEEL    — осевое колесо
                              • COMPONENT      — компонент из базы

                            Что делает:
                              При открытии диалога выбора в поле SELECTABLE
                              открывается дерево соответствующей сущности
                              (например, дерево электродвигателей для MOTOR).

                            Пример:
                              {
                                "key": "motorId",
                                "type": "SELECTABLE",
                                "referenceType": "MOTOR",
                                "label": "Электродвигатель"
                              }
                            """,
                            null),

                    new Entry("pullFrom", """
                            Наследование полей. Копирует значения из связанной
                            сущности в поля текущей карточки.

                            Формат: { "поле_сущности": "поле_карточки" }

                            Что делает:
                              При выборе значения в SELECTABLE-поле — из выбранной
                              сущности берутся значения указанных полей и записываются
                              в одноимённые (или указанные) поля карточки.

                            Пример:
                              {
                                "key": "motorId",
                                "type": "SELECTABLE",
                                "referenceType": "MOTOR",
                                "pullFrom": {
                                  "powerKw": "powerKw",
                                  "poles": "poles",
                                  "voltage": "voltage",
                                  "ratedSpeedRpm": "ratedSpeedRpm"
                                }
                              }

                            Это значит: при выборе электродвигателя — его powerKw,
                            poles, voltage, ratedSpeedRpm автоматически подставятся
                            в одноимённые поля карточки.

                            Полный список полей каждой сущности — см. раздел
                            «Поля сущностей».
                            """,
                            null),

                    new Entry("targetFieldName", """
                            Куда сохранять отображаемое имя выбранной сущности.
                            Только для SELECTABLE-полей.

                            Что делает:
                              При выборе значения — его displayName (например,
                              fullMarking электродвигателя) записывается в указанное
                              поле карточки.

                            Пример:
                              {
                                "key": "motorId",
                                "type": "SELECTABLE",
                                "referenceType": "MOTOR",
                                "targetFieldName": "motorFullMarking"
                              }

                            Это значит: при выборе двигателя его полная маркировка
                            сохранится в поле motorFullMarking.

                            Важно: поле targetFieldName должно быть в шаблоне
                            (или системным вычисляемым). Иначе — ошибка валидации.
                            """,
                            null),

                    new Entry("visibleIf", """
                            Условие видимости поля. Показывает поле только
                            при выполнении условия.

                            Формат:
                              {
                                "field": "имя_поля",
                                "operator": "EQUALS|NOT_EQUALS|NOT_EMPTY|EMPTY|IN",
                                "value": ...     // для EQUALS / NOT_EQUALS
                                "values": [...]  // для IN
                              }

                            Пример:
                              {
                                "key": "marking",
                                "label": "Маркировка производителя",
                                "visibleIf": {
                                  "field": "isPartnerProduction",
                                  "operator": "EQUALS",
                                  "value": true
                                }
                              }

                            Это значит: поле marking видно только когда
                            isPartnerProduction = true.

                            Скрытое поле также не участвует в валидации.
                            """,
                            null),

                    new Entry("requiredIf", """
                            Условие обязательности поля. Поле обязательно только
                            при выполнении условия.

                            Формат — такой же, как у visibleIf.

                            Пример:
                              {
                                "key": "marking",
                                "label": "Маркировка производителя",
                                "requiredIf": {
                                  "field": "isPartnerProduction",
                                  "operator": "EQUALS",
                                  "value": true
                                }
                              }

                            Это значит: поле marking обязательно, только если
                            isPartnerProduction = true.
                            """,
                            null),

                    new Entry("onSelect", """
                            Side-effect при выборе значения. Используется для
                            автоматического добавления компонента в таблицу
                            «Компоненты» карточки.

                            Формат:
                              {
                                "action": "ADD_TO_PRODUCT",
                                "role": "Ступица",
                                "quantity": 1.0
                              }

                            Возможные action:
                              • ADD_TO_PRODUCT — добавить выбранный компонент
                                в таблицу product_components карточки.

                            Пример:
                              {
                                "key": "hubComponentId",
                                "type": "SELECTABLE",
                                "referenceType": "COMPONENT",
                                "onSelect": {
                                  "action": "ADD_TO_PRODUCT",
                                  "role": "Ступица",
                                  "quantity": 1.0
                                }
                              }

                            Это значит: при выборе ступицы — она автоматически
                            добавится в таблицу «Компоненты» с ролью «Ступица»
                            и количеством 1.
                            """,
                            null),

                    new Entry("addToProduct", """
                            Флаг «добавлять в компоненты изделия». Только для
                            SELECTABLE-полей с referenceType = COMPONENT.

                            Что делает:
                              Эквивалент onSelect с action = ADD_TO_PRODUCT.
                              Удобно, когда не нужны дополнительные параметры
                              (role берётся из поля role, quantity = 1.0).

                            Пример:
                              {
                                "key": "hubComponentId",
                                "type": "SELECTABLE",
                                "referenceType": "COMPONENT",
                                "role": "Ступица",
                                "addToProduct": true
                              }

                            Ограничение: работает только для COMPONENT.
                            Для MOTOR / MOTOR_WHEEL / RADIAL_WHEEL / AXIAL_WHEEL
                            флаг игнорируется.
                            """,
                            null)
            )),

            // ========== ОПЕРАТОРЫ УСЛОВИЙ ==========
            new Section("Операторы условий", List.of(
                    new Entry("EQUALS", """
                EQUALS — поле равно значению.

                Пример: fireproof EQUALS true
                Пример: voltage EQUALS 380
                Пример: climateType EQUALS «У1»

                Требует значение в поле «Значение».

                Используется:
                  • в CONDITION (условие в правиле маркировки),
                  • в visibleIf / requiredIf (условие видимости/обязательности).
                """,
                            null),

                    new Entry("NOT_EQUALS", """
                NOT_EQUALS — поле не равно значению.

                Пример: climateType NOT_EQUALS «У1»

                Требует значение в поле «Значение».
                """,
                            null),

                    new Entry("EMPTY", """
                EMPTY — поле пусто (null или пустая строка).

                Пример: motorWheelId EMPTY
                (если мотор-колесо не выбрано)

                Не требует значения.
                """,
                            null),

                    new Entry("NOT_EMPTY", """
                NOT_EMPTY — поле заполнено.

                Пример: motorWheelId NOT_EMPTY
                (если мотор-колесо выбрано)

                Не требует значения.

                Самый частый оператор для необязательных
                частей маркировки (угол установки, направление
                вращения, код колеса).
                """,
                            null),

                    new Entry("IN", """
                IN — значение поля входит в список.

                Пример: climate IN [У1, У2, УХЛ1]
                В поле «Значение» — список через запятую.

                Требует список в поле «Значение».
                """,
                            null),

                    new Entry("THEN / ELSE в CONDITION", """
                CONDITION состоит из:
                  • условия (поле + оператор + значение);
                  • ветки THEN — блоки, добавляемые в маркировку,
                    если условие ИСТИННО;
                  • ветки ELSE — блоки, добавляемые в маркировку,
                    если условие ЛОЖНО.

                Пример:
                  IF fireproof EQUALS true
                    THEN: SEP '-' LITERAL 'F-2/400'
                    ELSE: (пусто)

                В каждой ветке (THEN/ELSE) можно добавлять
                любые блоки, включая другие CONDITION —
                получается вложенная маркировка.
                """,
                            null)
            )),

            // ========== ПОЛЯ МЕТАДАННЫХ ==========
            new Section("Системные поля метаданных", List.of(
                    new Entry("typeDesignation", """
                Обозначение типа вентилятора (designation).
                
                Пример: «VRK-PatAIR», «VR-PatAIR», «VO-PatAIR».
                
                Используется в правиле маркировки, чтобы префикс
                формировался автоматически по типу, а не через LITERAL.
                
                Пример правила:
                  FIELD('typeDesignation')
                  SEP '-'
                  FIELD('series')
                  SEP '-'
                  FIELD('ductSize')
                
                Результат для типа VRK-PatAIR, серии PRV, типоразмера 60-30:
                VRK-PatAIR-PRV-60-30
                """,
                            "FIELD('typeDesignation')"),

                    new Entry("typeName", """
                Имя типа вентилятора (name).
                
                Пример: «Вентилятор канальный», «Вентилятор радиальный».
                
                Используется редко — обычно в маркировке нужен typeDesignation,
                а не name.
                """,
                            "FIELD('typeName')"),

                    new Entry("seriesName", """
                Имя серии вентилятора (name).
                
                Пример: «PRV», «P», «Vn».
                
                Внимание: если серия не задана (NULL), seriesName = пусто,
                и в маркировку ничего не попадёт.
                
                Обычно используется, когда у нескольких серий общая часть
                маркировки (например, все канальные начинаются с typeDesignation),
                и различие — в серии.
                """,
                            "FIELD('seriesName')"),

                    new Entry("speciesName", """
                Имя вида вентилятора (name).
                
                Пример: «Прямоугольный», «Круглый», «Спиральный».
                
                В маркировке используется редко — обычно вид не участвует
                (это систематизация).
                """,
                            "FIELD('speciesName')")
            )),

            // ========== ПОЛЯ СУЩНОСТЕЙ ==========
            // Синхронизировано с services/catalog/CardFieldsRegistry.java —
            // при изменении сущностей обновить оба места.
            new Section("Поля сущностей", List.of(
                    new Entry("MOTOR — электродвигатель", """
                            Поля, доступные для pullFrom при referenceType = MOTOR:

                            • series            — Серия (TEXT)               — «АИР»
                            • motorType         — Тип двигателя (TEXT)        — «100L»
                            • poles             — Количество полюсов (INTEGER) — 2, 4, 6, 8
                            • powerKw           — Мощность, кВт (DOUBLE)      — 5.5
                            • ratedSpeedRpm     — Номинальная скорость (INTEGER)
                            • actualSpeedRpm    — Фактическая скорость (INTEGER)
                            • shaftSize         — Размер вала, мм (INTEGER)   — 28
                            • mountingType      — Исполнение по монтажу (TEXT) — «IM1081»
                            • climateType       — Климатическое исполнение (TEXT) — «У1»
                            • voltage           — Напряжение, В (INTEGER)     — 220, 380
                            • operationMode     — Режим работы (TEXT)         — S1, S4
                            • weightKg          — Масса, кг (DOUBLE)
                            • fullMarking       — Полная маркировка (TEXT)
                            """,
                            "MOTOR"),

                    new Entry("MOTOR_WHEEL — мотор-колесо", """
                            Поля, доступные для pullFrom при referenceType = MOTOR_WHEEL:

                            • manufacturer         — Производитель (TEXT)
                            • manufacturerMarking  — Маркировка производителя (TEXT)
                            • bladeType            — Тип лопаток (TEXT)        — впередзагнутые / назадзагнутые
                            • size                 — Размер, мм (INTEGER)      — 310
                            • poles                — Количество полюсов (INTEGER)
                            • voltageCode          — Код напряжения (TEXT)     — E (220), D (380)
                            • powerKw              — Мощность, кВт (DOUBLE)
                            • ratedSpeedRpm        — Номинальная скорость (INTEGER)
                            • actualSpeedRpm       — Фактическая скорость (INTEGER)
                            • voltage              — Напряжение, В (INTEGER)
                            • weightKg             — Масса, кг (DOUBLE)
                            • motorCode            — Код двигателя (TEXT)
                            • fullMarking          — Полная маркировка (TEXT)
                            """,
                            "MOTOR_WHEEL"),

                    new Entry("RADIAL_WHEEL — радиальное колесо", """
                            Поля, доступные для pullFrom при referenceType = RADIAL_WHEEL:

                            • manufacturer       — Производитель (TEXT)
                            • series             — Серия колеса (TEXT)        — КЦ, РК
                            • size               — Размер колеса, мм (DOUBLE) — 220, 560
                            • marking            — Маркировка (TEXT)
                            • bladeType          — Тип лопаток (TEXT)         — V, N, RO
                            • bladeMod           — Модификация лопатки (TEXT) — 14, 12U
                            • frontDiskMod       — Модификация переднего диска (TEXT) — A, B
                            • wheelWidth         — Ширина колеса (DOUBLE)     — 0.27, 0.33
                            • bladeCount         — Количество лопаток (INTEGER) — 6, 7, 9
                            • bladeLengthCoeff   — Коэф. длины лопатки (DOUBLE) — 1.03
                            • wheelCode          — Код колеса (TEXT)
                            • wheelFormula       — Формула колеса (TEXT)
                            • diameter           — Диаметр, мм (INTEGER)
                            • maxSpeedRpm        — Максимальная скорость (INTEGER)
                            • weightKg           — Масса, кг (DOUBLE)
                            • fullMarking        — Полная маркировка (TEXT)
                            """,
                            "RADIAL_WHEEL"),

                    new Entry("AXIAL_WHEEL — осевое колесо", """
                            Поля, доступные для pullFrom при referenceType = AXIAL_WHEEL:

                            • manufacturer       — Производитель (TEXT)
                            • series             — Серия колеса (TEXT)
                            • size               — Типоразмер (DOUBLE)        — 5.6, 6.3
                            • marking            — Маркировка (TEXT)
                            • trimCoefficient    — Коэф. подрезки, % (DOUBLE)
                            • bladeType          — Тип лопатки (TEXT)
                            • bladeCount         — Количество лопаток (INTEGER)
                            • bladeAngle         — Угол установки (INTEGER)   — 27, 30, 43
                            • bladeMaterial      — Материал лопатки (TEXT)
                            • maxBladeCount      — Макс. кол-во лопаток (INTEGER)
                            • wheelDiameter      — Диаметр колеса, мм (INTEGER)
                            • wheelFormula       — Формула колеса (TEXT)
                            • maxSpeedRpm        — Максимальная скорость (INTEGER)
                            • weightKg           — Масса, кг (DOUBLE)
                            • fullMarking        — Полная маркировка (TEXT)
                            """,
                            "AXIAL_WHEEL"),

                    new Entry("COMPONENT — компонент", """
                            Поля, доступные для pullFrom при referenceType = COMPONENT:

                            • name          — Наименование (TEXT)
                            • designation   — Обозначение (TEXT)         — «SM1210»
                            • vendorCode    — Артикул (TEXT)
                            • description   — Описание (TEXT)
                            • weightKg      — Масса, кг (DOUBLE)
                            • material      — Материал (TEXT)
                            • classId       — ID класса (INTEGER)
                            • unitId        — ID единицы измерения (INTEGER)
                            """,
                            "COMPONENT")
            )),

            // ========== СИСТЕМНЫЕ ВЫЧИСЛЯЕМЫЕ ПОЛЯ ==========
            // Синхронизировано с ComputedFieldsLibraryImpl — при изменении
            // вычисляемых полей обновить оба места.
            new Section("Системные вычисляемые поля", List.of(
                    new Entry("wheelSizeMarking", """
                            Маркировка размера колеса. Вход: wheelSize (мм).
                            Формула: floor(wheelSize / 10).

                            Примеры:
                              315 → 31
                              225 → 22
                            """,
                            "FIELD('wheelSizeMarking')"),

                    new Entry("voltageCode", """
                            Код напряжения. Вход: voltage (В).

                            Примеры:
                              220 → E
                              380 → D
                              660 → (пусто)
                            """,
                            "FIELD('voltageCode')"),

                    new Entry("executionMarking", """
                            Маркировка исполнения. Входы: generalPurpose,
                            fireproof, explosionProof.

                            Примеры:
                              generalPurpose = true → C
                              fireproof = true      → F-2/400 (или из fireproofMarking)
                              explosionProof = true → 1Ex d IIC T4 Gb (или из explosionMarking)
                            """,
                            "FIELD('executionMarking')"),

                    new Entry("sizeMarking", """
                            Форматирование размера. Вход: size.

                            Примеры:
                              5.6 → «5,6»
                              5.0 → «5»
                            """,
                            "FIELD('sizeMarking')"),

                    new Entry("powerMarking", """
                            Форматирование мощности. Вход: powerKw.

                            Примеры:
                              5.5  → «5,5»
                              11.0 → «11»
                            """,
                            "FIELD('powerMarking')"),

                    new Entry("bladeMarking", """
                            Маркировка лопаток. Входы: bladeCount, bladeSlots,
                            hasHa (направляющий аппарат), hasCa (спрямляющий аппарат).

                            Примеры:
                              bladeCount=3, bladeSlots=9 → «3/9»
                              + hasHa=true              → «3/9HA»
                              + hasCa=true              → «3/9HACA»
                            """,
                            "FIELD('bladeMarking')"),

                    new Entry("wheelDiameter", """
                            Диаметр колеса (осевое). Входы: size, trimCoefficient (%).
                            Формула: round(size * (100 - trim)).

                            Примеры:
                              size=6.3, trim=1.0 → 624
                              size=5.6, trim=0   → 560
                            """,
                            "FIELD('wheelDiameter')"),

                    new Entry("wheelFormulaAxial", """
                            Формула осевого колеса. Входы: wheelDiameter,
                            bladeCount, maxBladeCount, bladeName, bladeAngle,
                            bladeMaterial.

                            Формат: диаметр/лопатки-макс.кол/лопатка/угол/материал
                            Пример: 624/3-9/4Z/43/PAG
                            """,
                            "FIELD('wheelFormulaAxial')"),

                    new Entry("wheelFormulaRadial", """
                            Формула радиального колеса. Входы: bladeType,
                            wheelCode, frontDiskMod, wheelWidth, bladeCount,
                            bladeLengthCoeff.

                            Формат: bladeType.wheelCode/frontDiskMod.wheelWidth/bladeCount/bladeLengthCoeff
                            Пример: N.14/B.027/6/1.03
                            """,
                            "FIELD('wheelFormulaRadial')"),

                    new Entry("fireproofMarking", """
                            Маркировка огнестойкости. Входы: fireproof (флаг),
                            fireproofTime (часы), maxTemperature (°C, по умолчанию 400).

                            Формат: F-{time}/{temp}

                            Примеры:
                              fireproof=true, time=2, temp=600 → «F-2/600»
                              fireproof=true                   → «F/400»
                              fireproof=false                  → null
                            """,
                            "FIELD('fireproofMarking')"),

                    new Entry("explosionMarking", """
                            Маркировка взрывозащиты. Входы: explosionProof (флаг),
                            explosionMarking (строка, по умолчанию «1Ex d IIC T4 Gb»).

                            Примеры:
                              explosionProof=true              → «1Ex d IIC T4 Gb»
                              explosionProof=true, marking=X   → «X»
                              explosionProof=false             → null
                            """,
                            "FIELD('explosionMarking')"),

                    new Entry("climateTypeMarking", """
                            Климатическое исполнение. Вход: climateType.

                            Пример: climateType=«У1» → «У1»
                            """,
                            "FIELD('climateTypeMarking')"),

                    new Entry("positionMarking", """
                            Положение вентилятора. Вход: position.

                            Пример: position=«Г» → «Г» (горизонтальное)
                            """,
                            "FIELD('positionMarking')"),

                    new Entry("connectionTypeMarking", """
                            Маркировка направляющего/спрямляющего аппарата.
                            Входы: hasHa, hasCa.

                            Примеры:
                              hasHa=true, hasCa=true  → «HACA»
                              hasHa=true, hasCa=false → «HA»
                              оба false               → null
                            """,
                            "FIELD('connectionTypeMarking')")
            )),

            // ========== ПРИМЕРЫ ==========
            new Section("Примеры формул", List.of(
                    new Entry("Маркировка размера колеса", """
                            Задача: из размера колеса в мм получить «сжатый» вид
                            (первая часть типоразмера в маркировке).

                            Пример: 315 мм → 31; 225 мм → 22.

                            Формула:
                              FLOOR(DIVIDE(FIELD('wheelSize'), 10))
                            """,
                            "FLOOR(DIVIDE(FIELD('wheelSize'), 10))"),

                    new Entry("Код напряжения", """
                            Задача: преобразовать вольтаж в код-букву.

                            380 → D; 220 → E.

                            Формула:
                              MAP(FIELD('voltage'), {380: 'D', 220: 'E'})
                            """,
                            "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})"),

                    new Entry("Полная маркировка PKV", """
                            Пример: «VRK-PatAIR-PKV-50-30/22.2D»

                            Формула:
                              CONCAT(
                                'VRK-PatAIR-',
                                FIELD('series'), '-',
                                FIELD('ductSize'), '/',
                                FLOOR(DIVIDE(FIELD('wheelSize'), 10)), '.',
                                FIELD('poles'),
                                MAP(FIELD('voltage'), {380: 'D', 220: 'E'})
                              )
                            """,
                            "CONCAT('VRK-PatAIR-', FIELD('series'), '-', FIELD('ductSize'), '/', FLOOR(DIVIDE(FIELD('wheelSize'), 10)), '.', FIELD('poles'), MAP(FIELD('voltage'), {380: 'D', 220: 'E'}))"),

                    new Entry("Условный выбор партнёрское/фирменное", """
                            Если партнёрское — маркировка производителя.
                            Иначе — собрать свою.

                            Формула:
                              IF(EQUALS(FIELD('isPartnerProduction'), true),
                                 FIELD('marking'),
                                 CONCAT(FIELD('seriesName'), '-', FIELD('series')))
                            """,
                            "IF(EQUALS(FIELD('isPartnerProduction'), true), FIELD('marking'), CONCAT(FIELD('seriesName'), '-', FIELD('series')))"),

                    new Entry("Осевой вентилятор", """
                            Пример: «VO-PatAIR-5,6-C-3/9-5,5-2-У1»

                            Формула:
                              CONCAT(
                                'VO-PatAIR-',
                                FORMAT_NUMBER(FIELD('size')), '-',
                                FIELD('executionMarking'), '-',
                                FIELD('bladeCount'), '/',
                                FIELD('bladeSlots'), '-',
                                FORMAT_NUMBER(FIELD('powerKw')), '-',
                                FIELD('poles'), '-',
                                FIELD('climateType')
                              )
                            """,
                            "CONCAT('VO-PatAIR-', FORMAT_NUMBER(FIELD('size')), '-', FIELD('executionMarking'), '-', FIELD('bladeCount'), '/', FIELD('bladeSlots')," +
                                    " '-', FORMAT_NUMBER(FIELD('powerKw')), '-', FIELD('poles'), '-', FIELD('climateType'))")
            ))
    );

    public static List<Section> getSections() {
        return SECTIONS;
    }
}

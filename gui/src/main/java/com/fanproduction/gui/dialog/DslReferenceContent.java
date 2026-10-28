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
                            "CONCAT('VO-PatAIR-', FORMAT_NUMBER(FIELD('size')), '-', FIELD('executionMarking'), '-', FIELD('bladeCount'), '/', FIELD('bladeSlots'), '-', FORMAT_NUMBER(FIELD('powerKw')), '-', FIELD('poles'), '-', FIELD('climateType'))"),

                    new Entry("Системные вычисляемые поля", """
                            Помимо функций, доступны готовые вычисляемые поля,
                            зарегистрированные в ComputedFieldsLibrary.

                            К ним можно обращаться через FIELD('имя'):
                              FIELD('voltageCode')
                              FIELD('wheelSizeMarking')
                              FIELD('executionMarking')
                              FIELD('fireproofMarking')
                              FIELD('explosionMarking')
                              FIELD('sizeMarking')
                              FIELD('powerMarking')
                              FIELD('bladeMarking')
                              FIELD('wheelDiameter')
                              FIELD('wheelFormulaAxial')
                              FIELD('wheelFormulaRadial')
                              FIELD('climateTypeMarking')
                              FIELD('positionMarking')
                              FIELD('connectionTypeMarking')

                            Пример: CONCAT(FIELD('series'), '-', FIELD('voltageCode'))
                            """,
                            "CONCAT(FIELD('series'), '-', FIELD('voltageCode'))")
            ))
    );

    public static List<Section> getSections() {
        return SECTIONS;
    }
}

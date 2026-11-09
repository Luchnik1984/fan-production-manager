# Соглашения проекта FanProductionManager
Единый документ с архитектурными и стилистическими решениями.
Все новые изменения — в соответствии с этим файлом.
Спорные вопросы обсуждаются и вносятся сюда.

---

## 1. Модули и зависимости
api → services → repositories → core
api → fan-template
gui → core + fan-template (только DTO)
services → fan-template

**Обратные зависимости запрещены.**
- `core` не зависит ни от чего (кроме внешних библиотек).
- `repositories` не знает про `services` и `api`.
- `services` не знает про `api`.
- `gui` — клиент, ходит в `api` по HTTP; из внутренних модулей использует только DTO из `core` и `fan-template`.
- `fan-template` — самодостаточный (свои сущности, репозитории, сервисы, контроллеры).

Проверка: `mvn dependency:tree` не должен показывать обратных зависимостей.
---

## 2. DTO: `record` vs класс

### `record` — для immutable DTO

- Ответы REST (`<Something>Response`).
- Простые запросы без вложенных мутабельных полей (`<Something>Request`).
- Все DTO модуля `fan-template`.
- DTO внутри FAN-части `gui`.

Примеры: `FanTemplateVersionDto`, `DynamicFormMetadataDto`, `FieldDefinitionDto`, `FormulaPreviewResponse`.

### Класс с `@Data` — для мутабельных DTO

- Формы карточек, куда GUI дописывает поля после создания (`ProductCardDto.cardTypeDisplay`).
- Generic-конверты: `ApiResponse<T>` (Jackson десериализует через сеттеры).
- DTO с частичной инициализацией (поля заполняются постепенно).

Примеры: `ProductCardDto`, `ApiResponse`, `MotorDto`, `MaterialDto`.

### Переходный период

Старые DTO (`AuthResponse`, `UserDto`, `ComponentDto`, `MaterialDto`, `MotorDto` и т.д.) — **не трогаем** в рамках текущих спринтов. Если понадобится изменить — только при явной задаче.

---

## 3. Подпапки в `dto/`

**Правило:** `dto/request/` и `dto/response/` симметричны по подпапкам.

**Текущие группы (после US13.8):**
- `fan/` — DTO конструктора шаблонов.
- `preview/` — preview формул и условий (US9.9).
- `component/` — DTO компонентов.
- `material/` — DTO материалов.
- `card/` — DTO карточек продукции.
- `user/` — DTO пользователей.
- `common/` — общие DTO (`ApiResponse`).

**Подпапка создаётся, если в ней 2+ файла одной логической группы.**

**Плоские файлы допускаются** только в переходный период; при рефакторинге — переносятся в подпапки.

---

## 4. Логирование

- Используется `@Slf4j` (Lombok) — **везде**.
- `System.out.println` / `System.err.println` / `e.printStackTrace()` — **запрещены**.
- Уровни:
  - `log.trace` — тела запросов/ответов, только для отладки сети.
  - `log.debug` — отладка бизнес-логики.
  - `log.info` — бизнес-события (`Card created`, `Template published`).
  - `log.warn` — бизнес-ограничения (`Vendor code already exists`).
  - `log.error` — технические ошибки (с передачей `e`).
- **Не логировать:** пароли, токены, `JWT_SECRET`, `ADMIN_SECRET_KEY`.
- Ротация логов: настроена в `application.yml` (10 MB / 30 дней).

---

## 5. Обработка ошибок

### GUI

- **Все сообщения пользователю — через `e.getMessage()`** (после нормализации в `ApiClient`, US8.9).
- Классификация `Alert.AlertType`:
  - **ERROR** — техническая ошибка (сеть, парсинг, HTTP 5xx).
  - **WARNING** — бизнес-ограничение (HTTP 409: «используется», «уже существует», «нельзя удалить»).
  - **INFORMATION** — успех, подтверждение.
- Единая точка для неперехваченных — `gui.util.GlobalExceptionHandler` (регистрируется в `LoginApplication`).
- Для контроллеров конструктора шаблонов — через `TemplateControllerHelper.showAlert`.
- Для каталогов — через `BaseCatalogController.showAlert`.

### API

- Единая точка — `api.exception.GlobalExceptionHandler`.
- Возвращает `ApiResponse<T>` с корректным HTTP-кодом (400/401/403/404/405/409/415/500).
- `MethodArgumentNotValidException` → `Map<String, String>` (field → message) — контракт с GUI.

---

## 6. Тесты

- `mvn test` **должен быть зелёным перед коммитом**.
- Unit-тесты — для сервисов (Mockito, `@ExtendWith(MockitoExtension.class)`).
- Интеграционные тесты — для критических сценариев (реальные сервисы без Spring-контекста).
- Интеграционные с внешним API — `@Disabled` по умолчанию, запускать вручную.
- **Моки на `MarkingEngine.evaluate` — 4 аргумента** (`rule`, `values`, `fields`, `templateVersionId`).
- Именование тестов: `<MethodName>_<scenario>_<expected>` или `@DisplayName` на русском.

---

## 7. Стиль коммитов

| Формат | Когда |
|---|---|
| `<US-код>: <краткое описание>` | Новая фича |
| `<US-код>: fix — <что>` | Исправление в рамках подшага |
| `<US-код>-fix: <что>` | Отдельный подшаг-исправление |
| `<US-код>.<подшаг>: <что>` | Вложенный подшаг (a/b/c) |

Примеры:
- `US9.9.e: рефакторинг FieldEditDialog — ComboBox для pullFrom/targetFieldName`
- `US9.9.e: fix — FanCardServiceTest, 4-арг MarkingEngine.evaluate`
- `US13.8.1: рефакторинг api/dto/request/ — подпапки`

**Тело коммита** — маркированным списком, что сделано. В конце — `mvn clean install — BUILD SUCCESS`.

---

## 8. Стиль JavaDoc

- `/** ... */` для **публичных** классов и методов.
- `@param`, `@return`, `@throws` — где применимо.
- Русский язык.
- Внутренние/приватные методы — короткий комментарий или без него.
- Для сложных алгоритмов — блок с описанием в начале метода.

---

## 9. Миграции БД

- Flyway, `V<number>__<description>.sql` (двойное подчёркивание).
- **Применённые миграции не редактируются** (кроме исправления опечаток до первого применения в prod).
- Новые изменения — новая миграция.
- Все миграции — идемпотентны (`CREATE TABLE IF NOT EXISTS`, `ADD COLUMN IF NOT EXISTS`).
- Индексы — с осмысленными именами (`idx_<table>_<column>`).
- Уникальные — `idx_<table>_<column>_unique`.
- Комментарии к таблицам/колонкам — `COMMENT ON ...`.

---

## 10. Фронтенд (JavaFX)

- FXML — только структура. Логика — в контроллерах.
- Контроллеры — тонкие: бизнес-логика в сервисах/хелперах.
- Модальность:
  - **`WINDOW_MODAL`** — для всех диалогов редактирования (блокирует только владельца).
  - **`APPLICATION_MODAL`** — только для критических подтверждений и splash-экранов.
  - **`NONE`** — для главного окна и независимых view-only окон.
- Долгие операции — в фоновом потоке + `Platform.runLater` для обновления UI.
- HTTP-клиенты — статические методы (singleton) с `ApiClient` внутри.

---

## 11. REST API

- URL: `/api/<resource>` в kebab-case (`/api/fan-types`, `/api/fan-templates`).
- Ответ — `ApiResponse<T>` (`success`, `message`, `data`).
- Ошибки — через `GlobalExceptionHandler`.
- Аутентификация — JWT Bearer в заголовке `Authorization`.
- Роли: `@PreAuthorize("hasRole('ADMIN')")`, `@PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")`.
- Swagger — `springdoc-openapi` (доступен в `/swagger-ui.html`).
- Пагинация — `Pageable` (`page`, `size`, `sortBy`, `sortDir`).
- Валидация DTO — `@Valid` + `jakarta.validation.constraints`.

---

## 12. Ветки и PR

- `main` / `v2-constructor` — защищённые ветки.
- Рабочая ветка: `US<номер>-<краткое-описание>` (`US9.9.e-field-edit-dialog`).
- Подшаги: `US<номер>.<подшаг>-<описание>` (`US9.9.e.1-pullfrom-table`).
- PR — только после зелёных тестов.
- Merge — squash (один коммит на PR) или merge-commit (по договорённости).
- После merge — удалить рабочую ветку.

---

## 13. Быстрые правила

- **Никогда** не коммитить `.env`, пароли, секреты.
- **Никогда** не менять применённые миграции.
- **Никогда** не коммитить `System.out.println` / `e.printStackTrace()`.
- **Всегда** писать тесты для новой бизнес-логики.
- **Всегда** проверять `mvn clean install` перед push.
- **Всегда** указывать US-код в сообщении коммита.
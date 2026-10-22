# Приложение для автоматизации документооборота 

##  Описание проекта
Полноценное десктопное приложение (Java/Spring Boot + JavaFX)
для автоматизации производственного учета и документооборота
на предприятии, изготавливающем вентиляторы.

# Запуск проекта
### Требования:
- Java 15 или выше
- Maven 3.6+
- Docker desktop v29.1.2

## Команды:
### Файл настроек окружения
- Создайте файл настроек окружения.
  Файл содержит настройки подключения к БД и приложения.
  Отредактируйте его
```bash
  cp .env.example .env
```

```bash
  cp .env.example .env.dev
```

### Компиляция проекта
```bash
  ./mvnw clean compile 
```
### Запуск API
```bash
mvn -pl api spring-boot:run
```

## Сборка пакета:
```bash
   ./mvnw package 
```

## Docker Compose для PostgreSQL:

### Запуск PostgreSQL
```bash
     docker-compose -f docker-compose.yml up -d
```
### Проверка, что контейнер запущен
```bash
     docker ps | Select-String "postgres"
```

### Посмотреть какие контейнеры запущены
```bash
     docker ps 
```

### Посмотреть логи PostgreSQL
```bash
     docker logs fan-production-db
```

### Остановка PostgreSQL
```bash
     docker-compose -f docker-compose.yml down
```

### Остановка с удалением данных PostgreSQL
```bash
     docker-compose -f docker-compose.yml down -v
```

### Удаление контейнера, созданного docker-compose.db.yml
```bash
     docker-compose -f docker-compose.yml down --rmi all
```


## Запуски тестов
```bash
mvn -pl application exec:java "-Dexec.mainClass=com.fanproduction.application.test.ManualTest" "-Dexec.classpathScope=test"
```

```bash
mvn -pl application exec:java "-Dexec.mainClass=com.fanproduction.application.test.ManualTest" "-Dexec.classpathScope=test" "-Dapp.env=dev"
```

## Запуск приложения

### Компиляция проекта
```bash
  mvn clean compile 
```

### Инсталляция проекта
```bash
  mvn clean install 
```

### Запуск API
```bash
mvn -pl api spring-boot:run
```

### Запуск GUI
```bash
mvn -pl gui clean compile exec:java "-Dexec.mainClass=com.fanproduction.gui.LoginApplication"
```

### Собрать и запустить fat JAR
```bash
java -jar gui/target/gui-0.0.1-SNAPSHOT.jar
mvn clean package -DskipTests
```



## Синхронизация клиентских DTO с серверными

`gui` не зависит от `fan-template` (чтобы не тянуть Hibernate/Spring Data
в fat JAR десктопа). Поэтому следующие классы дублируются:

| Серверный (fan-template)                        | Клиентский (gui)                        |
|-------------------------------------------------|-----------------------------------------|
| template.enums.FieldType                        | gui.dto.fan.FieldType                   |
| template.enums.RuleElementType                  | gui.dto.fan.RuleElementType             |
| template.enums.ConditionOperator                | gui.dto.fan.ConditionOperator           |
| template.model.FieldDefinition                  | gui.dto.fan.FieldDefinitionDto          |
| template.model.MarkingRule                      | gui.dto.fan.MarkingRuleDto              |
| template.model.MarkingRuleElement               | gui.dto.fan.MarkingRuleElementDto       |

**Правило:** при изменении серверного класса — синхронизировать клиентский.
Несоблюдение ловится при первом REST-ответе (Jackson упадёт с
"Unrecognized field" / "Ошибка разбора ответа сервера").
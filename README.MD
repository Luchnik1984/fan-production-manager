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
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

### Компиляция проекта
```bash
  ./mvnw clean compile 
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

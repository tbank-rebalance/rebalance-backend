# rebalance-backend

API приложения для учёта личного бюджета: распределение дохода по категориям, автоклассификация транзакций, контроль лимитов и цели-накопления.

## Стек

Java 25, Spring Boot 4, PostgreSQL 18, Maven.

## Начало работы

Для запуска необходимы:

- JDK 25
- Docker с Docker Compose

Maven ставить не нужно, в репозитории есть Maven Wrapper (`./mvnw`).

В терминале:

```bash
docker compose up -d
./mvnw spring-boot:run
```

Проверка: http://localhost:8080/actuator/health

Документация API и тестирование запросов: http://localhost:8080/swagger-ui/index.html.
OpenAPI-схема доступна по адресу http://localhost:8080/v3/api-docs.
Swagger UI показывает эндпоинты приложения и доступные Actuator-эндпоинты.

Параметры подключения к базе задаются переменными окружения `DB_URL`, `DB_USER`, `DB_PASSWORD`. По умолчанию используется база из `compose.yaml`.

## Команды

| Команда | Что делает |
|---|---|
| `./mvnw verify` | Сборка, линтер, проверка форматирования, тесты, отчёт о покрытии |
| `./mvnw spotless:apply` | Форматирует код |
| `./mvnw test` | Только тесты |

Для тестов нужен запущенный Docker: Testcontainers поднимает PostgreSQL сам.

Отчёт о покрытии: `target/site/jacoco/index.html`.

## Как мы работаем

Правила веток, коммитов и ревью описаны в [CONTRIBUTING.md](CONTRIBUTING.md).

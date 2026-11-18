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
cp -n .env.example .env
```

В `.env` укажите пароль локальной БД. Для постоянного ключа JWT впишите в `AUTH_JWT_SECRET` результат команды `openssl rand -base64 32`. В профиле `dev` это поле можно оставить пустым.

Перед запуском экспортируйте значения из `.env` в терминал: Docker Compose использует файл для подстановки, но Spring Boot, запущенный через Maven, сам его не загружает.

```bash
set -a
source .env
set +a
docker compose up -d
SPRING_PROFILES_ACTIVE=dev ./mvnw -q spring-boot:run
```

Проверка: http://localhost:8080/actuator/health

Профиль `dev` показывает детали health-проверок и разрешает временный ключ JWT. После перезапуска приложения ранее выданные токены перестанут действовать. Для запуска без профиля `dev` заполните `AUTH_JWT_SECRET` в `.env` и запустите:

```bash
./mvnw -q spring-boot:run
```

Сохраняйте одно значение `AUTH_JWT_SECRET` между перезапусками. `.env` исключён из Git; `.env.example` содержит только локальные примеры и пустое поле JWT.

По умолчанию в консоли остаются предупреждения, ошибки и сообщения Tomcat с фактическим портом сервера. Для подробных логов и стандартного баннера Spring Boot:

```bash
SPRING_PROFILES_ACTIVE=verbose ./mvnw spring-boot:run
```

Параметры подключения к базе задаются переменными окружения `DB_URL`, `DB_USER`, `DB_PASSWORD`. Адрес по умолчанию указывает на локальную базу из `compose.yaml`.

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

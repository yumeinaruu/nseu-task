# Task Manager (nseutask)

REST-сервис управления задачами: регистрация и вход по JWT, CRUD задач, фильтры и пагинация

**Стек:** Java 17, Spring Boot 3.5, Spring Security (stateless JWT), Spring Data JPA, Liquibase, PostgreSQL, springdoc-openapi, Docker

## Запуск

### Docker (приложение + PostgreSQL)

```bash
docker compose up -d --build
```

Приложение: http://localhost:8080

### Локально

Нужен PostgreSQL с базой `nseutask` и пользователем `root` / `root`

```bash
docker compose up -d postgres
./gradlew bootRun
```

### Тесты

```bash
./gradlew test
```

Тесты идут на in-memory H2 в режиме PostgreSQL, внешняя БД не нужна

## Адреса

| Что | URL                                                      |
|---|----------------------------------------------------------|
| Swagger UI | http://localhost:8080/swagger-ui/index.html              |
| OpenAPI JSON | http://localhost:8080/v3/api-docs                        |
| Health | http://localhost:8080/actuator/health                    |

В Swagger UI нажмите **Authorize** и вставьте токен из `/api/auth/login`

### Postman

Импортируйте из каталога `docs/postman/` оба файла: `nseutask.postman_collection.json` и `nseutask.postman_environment.json`, выберите окружение **nseutask local** и запустите коллекцию через Runner (сверху вниз) или отправляйте запросы по одному

- Запросы логина сами сохраняют JWT в окружение (`token`, `adminToken`, `otherToken`), коллекция подставляет `Bearer {{token}}` во все запросы
- `taskId` и `version` обновляются после каждого создания и изменения задачи
- В каждом запросе есть тесты в After Response скриптах, ошибочные сценарии проверяют код ошибки приложения

## API

| Метод | Путь | Описание | Доступ |
|---|---|---|---|
| POST | `/api/auth/register` | Регистрация (роль `USER`) | все |
| POST | `/api/auth/login` | Вход, возвращает JWT | все |
| POST | `/api/tasks` | Создать задачу, автор: текущий пользователь | авторизованные |
| GET | `/api/tasks` | Список. Параметры: `status`, `assigneeId`, `authorId`, `page`, `size` (по умолчанию 10, макс. 100), `sort` | авторизованные |
| GET | `/api/tasks/{id}` | Одна задача | авторизованные |
| PUT | `/api/tasks/{id}` | Обновить задачу | автор или `ADMIN` |
| DELETE | `/api/tasks/{id}` | Удалить задачу | автор или `ADMIN` |

Токен передаётся заголовком `Authorization: Bearer <token>`

### Пример

```bash
curl -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"12345678"}'

curl -X POST localhost:8080/api/tasks -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" -d '{"title":"Example title 1","description":"Example description 1","priority":"HIGH"}'
```

## Формат ошибок

```json
{ "timestamp": "...", "status": 409, "code": 2003, "message": "Task was modified by someone else", "path": "/api/tasks/1" }
```

Клиенту всегда возвращается фиксированное сообщение для кода. Детали исключения пишутся только в лог дабы ничего системного не ушло клиенту на фронт

| Код | HTTP | Значение |
|---|---|---|
| 1001 | 404 | Пользователь не найден |
| 1002 | 409 | Username занят |
| 1003 | 409 | Email уже зарегистрирован |
| 1004 | 401 | Неверный логин или пароль |
| 2001 | 404 | Задача не найдена |
| 2002 | 403 | Менять задачу может только автор или админ |
| 2003 | 409 | Задачу изменили параллельно (устаревший `version`) |
| 2004 | 409 | Недопустимый переход статуса |
| 3001 | 500 | Внутренняя ошибка |
| 3002 | 400 | Ошибка валидации запроса |
| 3003 | 401 | Нужна аутентификация |
| 3004 | 403 | Доступ запрещён |

## Другое

- **Первый админ.** При старте создаётся пользователь `admin` / `12345678` (роль `ADMIN`), если его ещё нет. Значения настраиваются через `nseutask.admin.username|email|password`
- **Секрет JWT.** Значение по умолчанию задано в коде и подходит только для разработки. Настраивается через `nseutask.jwt.secret|expiration|issuer`. **В проде обязательно переопределить секрет и пароль админа через енвы** (например, `NSEUTASK_JWT_SECRET`, `NSEUTASK_ADMIN_PASSWORD`)
- **Права.** Читать задачи может любой авторизованный пользователь. Менять и удалять: автор или `ADMIN`
- **Параллельные правки.** Используется оптимистичная блокировка (`@Version`). Клиент получает `version` в ответе и обязан прислать её в `PUT`. При расхождении ответ 409 с кодом 2003
- **Статусы.** `TODO`, `IN_PROGRESS`, `DONE`. Запрещён только переход `DONE → TODO` (код 2004), завершённую задачу переоткрывают через `IN_PROGRESS`
- **`PUT`** заменяет задачу целиком: `assigneeId: null` снимает исполнителя
- **Приоритет** при создании по умолчанию `MEDIUM`, статус `TODO`
- **Время** хранится как UTC (`Instant`, `TIMESTAMP`), в ответах ISO-8601
- **Регистрация.** Уникальность `username` и `email` дополнительно защищена ограничениями БД, гонка запросов даёт 409, а не 500
- **Схема БД** управляется только Liquibase, миграции в `src/main/resources/db/changelog`

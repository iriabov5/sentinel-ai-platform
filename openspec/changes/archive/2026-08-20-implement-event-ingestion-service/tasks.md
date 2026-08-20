## 1. OpenSpec

- [x] 1.1 Описать proposal scope для `event-ingestion-service`.
- [x] 1.2 Описать service capability и validation expectations.
- [x] 1.3 Описать implementation design и non-goals.
- [x] 1.4 Запустить OpenSpec validation.

## 2. Build Setup

- [x] 2.1 Добавить Gradle wrapper и root Gradle files.
- [x] 2.2 Добавить module `services:event-ingestion-service`.
- [x] 2.3 Настроить Kotlin, Micronaut, validation, serde, management, OpenAPI,
  JaCoCo и SonarQube.

## 3. Service Implementation

- [x] 3.1 Добавить Micronaut application entrypoint.
- [x] 3.2 Добавить request/response DTO и enums для security event contract.
- [x] 3.3 Добавить service layer для acceptance id/status generation.
- [x] 3.4 Добавить `POST /api/v1/events` с `202 Accepted`.
- [x] 3.5 Добавить metadata validation limits.

## 4. Tests / Verification

- [x] 4.1 Покрыть valid event acceptance.
- [x] 4.2 Покрыть validation errors для invalid event.
- [x] 4.3 Покрыть metadata limits.
- [x] 4.4 Запустить tests и coverage verification.
- [x] 4.5 Запустить SonarQube analysis или явно зафиксировать, почему он
  недоступен. SonarQube server ответил `401 Unauthorized`: не передан
  `sonar.token` или `SONAR_TOKEN`.

## 5. Commit / PR

- [x] 5.1 Обновить tasks statuses после verification.
- [x] 5.2 Закоммитить change после успешных обязательных проверок.
- [x] 5.3 Запушить feature branch и открыть PR в `dev`.

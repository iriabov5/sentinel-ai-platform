## Why

После фиксации `security-event-contract` платформе нужен первый executable
slice: `event-ingestion-service`, который реально принимает security events по
REST, валидирует их и возвращает asynchronous acceptance response. Это создаст
первую runtime основу для будущего pipeline `REST -> Kafka -> behavior analysis`.

## What Changes

- Добавить Gradle multi-project baseline для Kotlin/Micronaut services.
- Добавить первый service module:
  `services/event-ingestion-service`.
- Реализовать `POST /api/v1/events`.
- Валидировать MVP security event envelope:
  - `eventType`
  - `subject.type`
  - `subject.id`
  - `occurredAt`
  - `source.application`
  - bounded `metadata`
- Возвращать `202 Accepted` с `eventId`, `status` и `receivedAt`.
- Добавить health endpoint через Micronaut Management.
- Добавить OpenAPI generation setup.
- Добавить focused automated tests и coverage verification.
- Добавить SonarQube project configuration.
- Non-goals:
  - Не подключать Kafka producer в этом change.
  - Не добавлять Docker Compose.
  - Не добавлять persistence.
  - Не добавлять authentication/API key на endpoint.
  - Не создавать другие микросервисы.

## Capabilities

### New Capabilities

- `event-ingestion-service`: первый Kotlin/Micronaut runtime service, который
  принимает security events через REST и готовит их к downstream processing.

### Modified Capabilities

- Нет.

## Impact

- Добавляет application code и Gradle build setup.
- Делает `event-ingestion-service` первым проверяемым backend module.
- Закладывает основу для следующего change: Kafka publishing в
  `security.events.raw`.
- Требует перед commit выполнить OpenSpec validation, tests, coverage check и
  по возможности SonarQube analysis.

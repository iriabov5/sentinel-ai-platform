## Why

Первый executable slice платформы начинается с приема security events, поэтому
до реализации `event-ingestion-service` нужен явный contract: какие события
принимаются, какие поля обязательны, как работает validation и какой event будет
публиковаться дальше в Kafka. Это предотвращает ситуацию, где DTO, OpenAPI и
Kafka payload расходятся уже на первом сервисе.

## What Changes

- Ввести capability `security-event-contract`.
- Определить базовый security event envelope для первого MVP.
- Зафиксировать первый REST ingestion boundary: `POST /api/v1/events`.
- Зафиксировать ожидаемый successful response: `202 Accepted`.
- Зафиксировать validation expectations для обязательных полей и invalid events.
- Зафиксировать первый Kafka topic: `security.events.raw`.
- Выбрать JSON Schema как initial schema strategy для MVP, с возможностью
  пересмотреть Avro позже отдельным architecture change.
- Определить MVP event types:
  - `LOGIN_SUCCESS`
  - `LOGIN_FAILED`
  - `API_REQUEST`
  - `FILE_DOWNLOAD`
  - `PERMISSION_CHANGE`
  - `DEVICE_LOGIN`
  - `TOKEN_CREATED`
  - `PRIVILEGE_ESCALATION`
  - `DATA_EXPORT`
  - `ADMIN_ACTION`
- Non-goals:
  - Не реализовывать `event-ingestion-service` в этом change.
  - Не добавлять Gradle/Micronaut project setup.
  - Не поднимать Kafka или Docker Compose.
  - Не реализовывать producer/consumer code.
  - Не создавать `behavior-analysis-service`.

## Capabilities

### New Capabilities

- `security-event-contract`: описывает MVP contract для security events,
  ingestion REST boundary, validation, accepted event types, Kafka topic и
  schema strategy.

### Modified Capabilities

- Нет.

## Impact

- Затрагивает OpenSpec planning artifacts и будущие API/event contracts.
- Создает основу для будущего implementation change `implement-event-ingestion-service`.
- Не добавляет application code, runtime dependencies, Docker Compose, Kafka,
  OpenAPI files или generated schemas в этом change.

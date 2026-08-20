## Context

Мотивация описана в `proposal.md`. Сейчас repository содержит только bootstrap
specs и OpenSpec workflow. Первым runtime service по roadmap будет
`event-ingestion-service`, но до него нужно зафиксировать contract, чтобы REST
DTO, validation, OpenAPI и Kafka payload развивались из одного источника.

## Goals / Non-Goals

**Goals:**

- Определить MVP security event envelope.
- Определить первый REST boundary: `POST /api/v1/events`.
- Определить первый Kafka topic: `security.events.raw`.
- Определить supported MVP event types.
- Выбрать initial schema strategy.
- Сохранить future compatibility с behavior analysis и Kafka publishing.

**Non-Goals:**

- Не реализовывать Kotlin/Micronaut service.
- Не создавать Gradle project или service directory.
- Не добавлять OpenAPI YAML, AsyncAPI YAML или JSON Schema file в этом change.
- Не поднимать Kafka или Docker Compose.
- Не реализовывать security/authentication для endpoint.
- Не реализовывать behavior-analysis-service.

## Decisions

### Decision: Start with explicit event envelope

Security event должен иметь общий envelope:

```json
{
  "eventType": "LOGIN_FAILED",
  "subject": {
    "type": "USER",
    "id": "user-123"
  },
  "occurredAt": "2026-08-20T10:15:00Z",
  "source": {
    "application": "billing-api",
    "ip": "203.0.113.42",
    "deviceId": "device-abc"
  },
  "metadata": {
    "reason": "INVALID_PASSWORD"
  }
}
```

Envelope держит общие поля отдельно от `metadata`, чтобы behavior analysis мог
строить общие features независимо от конкретного event type.

Alternative considered: сделать отдельный DTO для каждого event type сразу. Это
строже, но слишком рано усложняет первый MVP и потребует больше schema work до
первого working slice.

### Decision: Use `POST /api/v1/events` as the first ingestion boundary

Внешние applications, agents и будущий Go `security-agent` должны отправлять
events в единый ingestion endpoint. Endpoint возвращает `202 Accepted`, потому
что downstream processing будет asynchronous.

Alternative considered: возвращать `200 OK` с результатом анализа. Это
превратило бы ingestion в synchronous analysis API и связало бы первый сервис с
behavior/AI pipeline слишком рано.

### Decision: Use `security.events.raw` as first Kafka topic

Accepted normalized events позже будут публиковаться в `security.events.raw`.
Название отражает, что событие уже принято/нормализовано ingestion service, но
еще не обогащено behavior analysis.

Alternative considered: topic per event type, например `security.login.failed`.
Это может быть полезно позже, но для MVP усложнит routing, contracts и consumer
setup до появления реальных нагрузочных требований.

### Decision: Use JSON Schema first

JSON Schema выбран как initial schema strategy, потому что REST payload уже JSON,
схема легко читается, хорошо подходит для MVP validation и не требует сразу
Schema Registry. Avro можно рассмотреть позже, когда появятся требования к
binary format, schema evolution discipline и managed Kafka ecosystem.

Alternative considered: Avro first. Это сильный production option, но он добавит
операционную сложность раньше, чем у нас появится первый event pipeline.

### Decision: Keep sensitive data out of common envelope

Common envelope не должен требовать secrets или raw confidential payloads.
Sensitive values, если они появятся в metadata, должны обрабатываться отдельными
rules: masking, hashing, rejection или retention limits.

Alternative considered: разрешить любые metadata без ограничений. Это быстрее,
но опасно для security platform, потому что она сама может начать хранить
секреты, tokens или PII без контроля.

## Risks / Trade-offs

- [Risk] Общий envelope может оказаться слишком мягким для некоторых event types.
  -> Mitigation: добавлять event-specific schemas через future OpenSpec changes.
- [Risk] JSON Schema может стать недостаточным для долгой Kafka schema evolution.
  -> Mitigation: оставить Avro/Schema Registry как explicit later architecture
  option.
- [Risk] Один topic `security.events.raw` может стать слишком широким.
  -> Mitigation: пересмотреть topic strategy после появления behavior analysis
  и реальных routing requirements.
- [Risk] Metadata может принести sensitive data.
  -> Mitigation: в implementation change добавить size limits и sensitive-data
  handling rules before persistence.

## Migration Plan

Runtime migration не требуется, потому что change создает planning artifacts.
После review этот change должен быть applied/archived, создав main spec
`security-event-contract`. Следующий implementation change сможет создать
`event-ingestion-service` по этому contract.

## Open Questions

- Какие exact size limits установить для `metadata` в первом implementation
  change?
- Должен ли first endpoint требовать API key сразу или authentication будет
  отдельным follow-up после минимального validation slice?

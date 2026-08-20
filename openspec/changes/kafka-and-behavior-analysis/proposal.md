## Why

`event-ingestion-service` уже принимает security events по REST, но accepted
event никуда не уходит: Kafka producer отсутствует, `behavior-analysis-service`
не существует, event history не сохраняется. Phase 3 roadmap требует первый
end-to-end pipeline, иначе ingestion остаётся тупиковым HTTP-слоем и нельзя
проверять asynchronous processing.

## What Changes

- Зафиксировать Kafka event contract для accepted security events: topic
  `security.events.raw`, JSON Schema payload, partition key, retry и
  dead-letter expectations, AsyncAPI.
- Научить `event-ingestion-service` публиковать accepted event в Kafka после
  успешной validation.
- Создать `behavior-analysis-service` как Kotlin/Micronaut consumer, который
  читает `security.events.raw` и сохраняет event history в MongoDB.
- Добавить local Docker Compose для Kafka и MongoDB.
- Вынести Kafka/Mongo runtime settings в typed configuration и
  `.env.example`.
- Добавить tests для publish, consume, persistence и failure paths.
- Non-goals:
  - Не извлекать behavioral features (Phase 4).
  - Не создавать `ai-detection-service`, `incident-service`,
    `notification-service`, `identity-access-service` или dashboard.
  - Не вводить Avro или Schema Registry.
  - Не добавлять client idempotency key / authentication на REST endpoint.
  - Не настраивать Railway или Kubernetes deployment.
  - Не вводить outbox pattern: producer публикует напрямую после acceptance.

## Capabilities

### New Capabilities

- `kafka-event-contract`: contract-first описание Kafka topic
  `security.events.raw`, payload envelope, schema strategy, partition key,
  producer/consumer ownership, retry и dead-letter topic.
- `behavior-analysis-service`: Kotlin/Micronaut consumer, который читает
  accepted events из Kafka и сохраняет owned event history в MongoDB.

### Modified Capabilities

- `event-ingestion-service`: после успешной validation SHALL публиковать
  accepted event в Kafka; invalid event SHALL NOT публиковаться; Kafka
  unavailability SHALL не маскироваться успешным `202 Accepted`.
- `security-event-contract`: публикация в `security.events.raw` больше не
  откладывается на later change и связывается с Kafka payload envelope.

## Impact

- Добавляет Kafka producer в `services/event-ingestion-service`.
- Добавляет новый Gradle module `services/behavior-analysis-service`.
- Добавляет AsyncAPI/JSON Schema artifacts для Kafka contract.
- Добавляет local Docker Compose для Kafka и MongoDB. Railway и Kubernetes
  не затрагиваются.
- Расширяет `.env.example` безопасными local broker/database examples.
- Не меняет REST path `POST /api/v1/events`; successful response по-прежнему
  `202 Accepted` с `eventId`, `status` и `receivedAt`.
- Breaking для runtime-ожиданий: `202 Accepted` теперь означает, что event
  принят **и** успешно опубликован в Kafka, а не только прошёл validation.

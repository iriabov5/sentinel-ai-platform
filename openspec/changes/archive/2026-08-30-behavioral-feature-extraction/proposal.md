## Why

Phase 3 закрыта: `behavior-analysis-service` накапливает owned event history в
MongoDB, но история пока нигде не используется. Roadmap Phase 4 требует
извлекать explainable behavioral features из event history, иначе нельзя
проверить следующий slice — anomaly scoring в `ai-detection-service` (Phase 5),
которому нужны фичи для одного subject.

## What Changes

- Добавить в `behavior-analysis-service` on-demand REST endpoint
  `GET /api/v1/subjects/{subjectId}/features`, который считает фичи по event
  history из MongoDB на момент запроса (без feature store и без
  материализации).
- Определить первый набор из четырёх explainable behavioral features:
  `new_ip_device`, `unusual_time`, `request_rate`, `download_volume` — каждая с
  явной семантикой и конфигурируемым time window.
- Вынести параметры фич (window, пороги) в typed runtime configuration.
- Зафиксировать response-контракт фич (JSON Schema / OpenAPI), на который в
  Phase 5 сможет опираться `ai-detection-service`.
- Использовать существующий MongoDB индекс `subject.id + occurredAt` для
  per-subject reads; новых storage-структур не вводить.
- Non-goals:
  - Не создавать feature store и не материализовать фичи при consume.
  - Не вызывать `ai-detection-service` и не возвращать anomaly score (Phase 5).
  - Не создавать incidents и не менять `event-ingestion-service`.
  - Не вводить Kafka-вывод фич, caching, retention или background jobs.
  - Не трогать Docker Compose, Railway и Kubernetes deployment.

## Capabilities

### New Capabilities

- `behavioral-features`: contract-first описание первого набора behavioral
  features (семантика, формулы, time window, JSON Schema response) как
  независимого контракта для future `ai-detection-service`.

### Modified Capabilities

- `behavior-analysis-service`: добавляется REST endpoint фич, on-demand
  вычисление по owned event history и typed runtime configuration параметров
  фич.

## Impact

- `services/behavior-analysis-service`: новый controller, feature service,
  query path в repository (read-only по существующему индексу), typed
  `@ConfigurationProperties` для window/порогов, OpenAPI для нового endpoint.
- MongoDB: только read-запросы по существующему unique `eventId` и
  `subject.id + occurredAt` индексам; схема документов не меняется.
- Docker Compose / Railway / Kubernetes: не затрагиваются — runtime настройки
  фич приходят через environment variables как обычно.
- REST: аддитивный endpoint, существующий контракт `POST /api/v1/events` не
  меняется. BREAKING-изменений нет.

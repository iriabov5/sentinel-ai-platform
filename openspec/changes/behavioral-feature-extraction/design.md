## Context

См. `proposal.md` — Why. Phase 3 закрыта: `behavior-analysis-service` хранит
owned event history в MongoDB (unique `eventId`, индекс `subject.id +
occurredAt`), но не имеет query API. Этот design описывает Phase 4: on-demand
вычисление четырёх explainable features по history через новый REST endpoint.

Constraints (см. AGENTS.md и archived Phase 3 design):

- Kotlin/Micronaut, coroutines-first, без Spring Boot.
- Runtime settings через typed `@ConfigurationProperties` с validation.
- Contract-first: JSON Schema/OpenAPI для нового endpoint.
- Без новых storage-структур: только read по существующим индексам.
- Local Docker Compose, Railway и Kubernetes не затрагиваются.

## Goals / Non-Goals

**Goals:**

- On-demand REST endpoint фич с консистентными значениями на момент запроса.
- 4 explainable features с простыми, проверяемыми формулами.
- Typed runtime configuration для windows и порогов.
- Контракт фич (JSON Schema), готовый к потреблению `ai-detection-service`.
- Tests: unit (in-memory doubles) + Testcontainers MongoDB integration.

**Non-Goals (design-level):**

- Не строить feature store, aggregation pipeline или background jobs.
- Не кэшировать фичи и не вводить retention.
- Не добавлять authentication/authorization (identity-access-service позже).
- Не вводить pagination или limit API на этом шаге.
- Не вызывать `ai-detection-service` (Phase 5).

## Decisions

### Decision: Features are computed on demand from MongoDB history

`GET /api/v1/subjects/{subjectId}/features` считает фичи в момент запроса по
owned history: два read-запроса — события внутри window и baseline до начала
window. Значения всегда актуальны, storage-схема не меняется.

Alternative considered: материализованный feature store при consume. Быстрее
чтение, но требует пересчёта, инвалидации и retention — избыточно до появления
реальных нагрузок и второго потребителя фич.

### Decision: Feature formulas stay simple and explainable

- `new_ip_device`: distinct IP/device в window, отсутствовавшие в baseline
  (history до начала window). Explanation перечисляет конкретные новые
  IP/device. Значение — count.
- `unusual_time`: доля событий window, попавших в часы, нехарактерные для
  subject. Типичные часы — часы, в которые baseline содержит не меньше
  `unusualTime.minEventsPerHour` событий; нехарактерный час — не входящий в
  этот набор. Значение — fraction (0..1), explanation — набор типичных часов.
- `request_rate`: `windowEvents / windowDurationHours` — events per hour.
- `download_volume`: сумма `metadata.bytes` событий типов `FILE_DOWNLOAD` и
  `DATA_EXPORT` в window. Конвенция: volume-события несут числовой
  `metadata.bytes`; при отсутствии поля событие даёт 0 и не роняет расчёт.

Baseline для `new_ip_device` и `unusual_time` ограничен
`baselineLookback` (конфиг), чтобы запрос оставался bounded.

Alternative considered: статистика с mean/std на базе всех history — точнее, но
тянет aggregation pipeline и неочевидные пороги; MVP использует простые
подсчёты, формулу можно менять позже без изменения контракта.

### Decision: Response follows the behavioral-features contract

Response JSON Schema живёт в `contracts/json-schema/subject-features.json`:

```text
{
  "subjectId": "...",
  "computedAt": "ISO-8601",
  "window": {"start": "...", "end": "..."},
  "features": [
    {"name": "new_ip_device", "value": 3, "unit": "count",
     "explanation": "new IPs: 203.0.113.7; new devices: d-42"},
    ...
  ]
}
```

OpenAPI генерируется в сервисе для `GET /api/v1/subjects/{subjectId}/features`
(200, validation error для malformed id, subject без history → нулевые/пустые
значения с 200).

Alternative considered: отдельный JVM DTO module — контрактный файл важнее,
shared library откладываем (как в Phase 3).

### Decision: Query uses existing indexes only

Window и baseline запросы используют индекс `subject.id + occurredAt`
(фильтр по `subject.id`, range по `occurredAt`). Новых индексов и коллекций
не добавляем; при реальных объёмах отдельный aggregation/feature-store change.

### Decision: Feature parameters are typed runtime configuration

`FeatureProperties` (`@ConfigurationProperties`, prefix `sentinel.behavior.features`),
с KDoc и validation:

- `window`: длительность окна фич (default: 24h)
- `baselineLookback`: глубина baseline (default: 30d)
- `unusualTime.minEventsPerHour`: порог «типичного» часа (default: 1)
- `downloadVolume.eventTypes`: типы, участвующие в объёме
  (default: `FILE_DOWNLOAD`, `DATA_EXPORT`)

Default values — safe local, переопределяются environment variables.

### Decision: Coroutines-first request path

Controller → `suspend` feature service → repository (Mongo driver reactive).
Blocking IO не протекает в request path. Ошибки Mongo → `503 Service
Unavailable` (сервис не может посчитать фичи), malformed id → `400`.

## Risks / Trade-offs

- [Risk] Baseline scans растут с объёмом history.
  -> Mitigation: bounded `baselineLookback`; для "новых" IP/device достаточно
  distinct в ограниченном окне; aggregation pipeline — отдельный change.
- [Risk] Конвенция `metadata.bytes` может разойтись с реальными событиями.
  -> Mitigation: фиксируется в контракте фич, отсутствие поля → 0, tests
  покрывают оба случая.
- [Risk] Простая формула `unusual_time` даёт грубые пороги.
  -> Mitigation: пороги конфигурируемы и объяснимы; точность — задача Phase 5
  scoring, не этого change.
- [Risk] Subject с огромным числом событий в window замедляет ответ.
  -> Mitigation: локальный scale MVP; limit/aggregation — отдельный change,
  зафиксирован в Open Questions.
- [Risk] Без auth endpoint открыт любому клиенту.
  -> Mitigation: осознанно для MVP, закрывается identity-access-service
  (Phase 8); не блокирует Phase 4.

## Migration Plan

Change аддитивный: новый endpoint, новый контрактный файл, новый
configuration bean. Существующие consume/persist/DLQ пути не меняются.

Rollback: revert feature branch/PR. Откат не влияет на ingestion и history
pipeline — endpoint просто исчезает.

## Open Questions

- Нужен ли per-feature enable/disable флаг до появления реальных scoring
  сценариев? Можно добавить позже как чисто конфигурационное расширение без
  изменения контракта.
- Достаточно ли одного window для всех фич или `request_rate` нужен
  собственный короткий window? Решится по итогам Phase 5 интеграции.

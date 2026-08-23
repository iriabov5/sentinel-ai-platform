## Context

См. `proposal.md` — Why. `event-ingestion-service` уже принимает REST events и
возвращает `202 Accepted`, но не публикует их дальше. Main specs уже называют
первый slice `event-ingestion-service -> Kafka -> behavior-analysis-service ->
MongoDB`. Этот design описывает, как провести этот slice без outbox, без
feature extraction и без cloud deployment.

Constraints:

- Kotlin/Micronaut, coroutines-first, без Spring Boot.
- Runtime settings через typed `@ConfigurationProperties`.
- Contract-first: AsyncAPI + JSON Schema до producer/consumer code.
- Local runtime через Docker Compose; Railway/Kubernetes не входят в change.

## Goals / Non-Goals

**Goals:**

- Сделать `202 Accepted` честным: event принят только после успешной Kafka
  publish.
- Зафиксировать общий Kafka payload для producer и consumer.
- Дать `behavior-analysis-service` собственную MongoDB history.
- Поднять Kafka и MongoDB локально через Docker Compose.
- Проверять pipeline тестами, включая failure и duplicate `eventId`.

**Non-Goals:**

- Не проектировать transactional outbox.
- Не вводить shared JVM library для DTO.
- Не считать Docker Compose deployment-манифестом для самих Kotlin services:
  в этом change Compose поднимает только Kafka и MongoDB.
- Не проектировать query API, feature store или retention job.

## Decisions

### Decision: Publish directly after validation, without outbox

Producer в `event-ingestion-service` публикует в Kafka в том же request path
после validation и генерации `eventId`. `202 Accepted` возвращается только после
успешного ack. Если publish исчерпал retries/timeout, клиент получает `503
Service Unavailable`.

Это сохраняет простой stateless service и делает success-семантику проверяемой
без дополнительной таблицы outbox.

Alternative considered: transactional outbox в MongoDB или PostgreSQL. Надёжнее
при crash между persist и publish, но добавляет storage в ingestion service,
фоновый publisher и лишний failure domain до первого working pipeline.

### Decision: Kafka record key is `subject.id`

Key равен `subject.id`, чтобы события одного actor шли в одну partition и позже
feature extraction мог опираться на per-subject порядок.

Alternative considered: key = `eventId`. Это равномернее распределяет нагрузку,
но ломает per-subject ordering до появления реальных partitioning требований.

### Decision: Flattened JSON payload with ingestion identity

Kafka value — JSON object с полями REST envelope плюс `eventId` и `receivedAt`.
`eventId` в payload совпадает с REST response. Schema живёт в repository как
JSON Schema, topic — в AsyncAPI.

Предлагаемые пути:

```text
contracts/asyncapi/security-events-raw.yaml
contracts/json-schema/accepted-security-event.json
```

Kotlin DTO в каждом сервисе маппятся на этот schema independently. Общий JVM
module не создаём: контрактный файл важнее, чем преждевременная shared library.

Alternative considered: Avro + Schema Registry. Отклонён существующим
`security-event-contract`; binary evolution можно добавить отдельным change.

### Decision: Use Micronaut Kafka and coroutine-friendly service boundaries

Оба сервиса используют Micronaut Kafka. Producer вызывается из `suspend`
application service с `withTimeout`. Consumer listener делегирует в `suspend`
persist use-case. Blocking Kafka client IO, если он останется в библиотеке,
изолируется на adapter boundary и не протекает в controller/use-case.

Reactor Kafka не используем: default async style проекта — coroutines.

### Decision: MongoDB is owned by behavior-analysis-service

MongoDB нужен как document store для неоднородных security events и metadata.
Collection event history принадлежит только `behavior-analysis-service`. Другие
сервисы не пишут в эту collection.

Документ хранит Kafka payload и server-side stored timestamp. Unique index по
`eventId` даёт идемпотентность consumer: duplicate key трактуется как успешная
обработка (at-least-once).

Indexes:

- unique `eventId`
- `subject.id` + `occurredAt` для будущих per-subject reads, без query API в
  этом change

Retention явно не внедряем; default MongoDB retention без TTL.

Alternative considered: PostgreSQL JSONB. Подходит для transactional entities,
но event history ближе к document workload, и platform-architecture уже назначил
MongoDB этому capability.

### Decision: Application-level dead-letter topic

Если consume/persist не удался после bounded retries, consumer публикует запись
в `security.events.raw.dlq` и коммитит исходный offset. Это не блокирует
partition на poison payload.

Default retry/timeout values (typed config, можно override):

- producer timeout: 2s
- producer retries: 3
- consumer processing retries: 3

Alternative considered: останавливать consumer на ошибке. Проще, но один
невалидный record останавливает history pipeline.

### Decision: Docker Compose provides Kafka (KRaft) and MongoDB only

Первый Compose file поднимает broker без ZooKeeper и MongoDB. Kotlin services
пока запускаются через Gradle/`Application`, чтобы не тащить Dockerfiles до
появления стабильного local/runtime shape. Health и порты сервисов остаются
typed config: ingestion `8081`, behavior-analysis `8082`.

`.env.example` получает safe local values для bootstrap servers, topic,
MongoDB URI и service port.

Unit tests используют in-memory doubles и не поднимают брокер/Mongo.
Integration tests через Testcontainers поднимают Kafka и MongoDB, чтобы
quality check не зависел от вручную запущенного Compose. Если Docker
недоступен, эти IT пропускаются.

Alternative considered: сразу упаковать оба сервиса в Compose. Удобнее для
ручного e2e, но требует Dockerfiles, image tags и ещё один deployment surface
раньше Railway/K8s change.

## Risks / Trade-offs

- [Risk] Crash после Kafka ack, но до HTTP response, даёт client retry и второй
  `eventId`.
  -> Mitigation: at-least-once на HTTP-слое принимаем в MVP; client idempotency
  key — отдельный change.
- [Risk] Прямой publish делает ingestion зависимым от Kafka availability.
  -> Mitigation: это осознанная семантика `202`; timeout/retry/503 документированы.
- [Risk] Один topic и один consumer group могут стать узкими.
  -> Mitigation: пересмотреть после появления feature extraction и второго
  consumer.
- [Risk] Дублирование Kotlin DTO разойдётся с JSON Schema.
  -> Mitigation: tests producer/consumer против одной schema; shared library
  только если drift повторится.
- [Risk] Application-level DLQ может потерять record, если DLQ publish тоже
  падает.
  -> Mitigation: не коммитить исходный offset, пока DLQ publish не успешен;
  после bounded failures логировать и оставить retry/consumer lag видимым.
- [Risk] Compose без самих сервисов не даёт one-command platform run.
  -> Mitigation: достаточно для Phase 3 local infra; service containers — later
  deployment change.

## Migration Plan

Change аддитивный: новый module, новые contract files, новый Compose stack,
новый producer в существующем ingestion service.

Rollback: revert feature branch/PR. После merge откат producer вернёт старую
семантику `202` без Kafka, поэтому rollback допустим только до появления
реальных consumers в других environments.

Railway/Kubernetes не меняются; cloud broker/database values появятся, когда
deployment change начнёт их требовать.

## Open Questions

- Какой retention/TTL для MongoDB event history нужен после первых реальных
  объёмов?
- Сколько partitions нужно `security.events.raw` за пределами local single-broker
  Compose?

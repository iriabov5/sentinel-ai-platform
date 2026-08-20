## 1. OpenSpec

- [x] 1.1 Описать proposal scope для Kafka pipeline и `behavior-analysis-service`.
- [x] 1.2 Описать delta specs для Kafka contract, ingestion publishing и
  behavior analysis storage.
- [x] 1.3 Описать implementation design, failure modes и non-goals.
- [x] 1.4 Запустить `openspec validate --all --strict --no-interactive`.

## 2. Contracts / Local Infra

- [ ] 2.1 Добавить JSON Schema для Kafka payload accepted security event.
- [ ] 2.2 Добавить AsyncAPI для topics `security.events.raw` и
  `security.events.raw.dlq`.
- [ ] 2.3 Добавить Docker Compose с Kafka (KRaft) и MongoDB.
- [ ] 2.4 Расширить `.env.example` safe local values для Kafka, MongoDB и
  порта `behavior-analysis-service`.

## 3. event-ingestion-service Producer

- [ ] 3.1 Добавить Micronaut Kafka dependency и typed Kafka producer
  configuration.
- [ ] 3.2 Добавить Kafka payload model с `eventId`, `receivedAt` и event
  envelope.
- [ ] 3.3 Публиковать accepted event в `security.events.raw` с key
  `subject.id` до `202 Accepted`.
- [ ] 3.4 Возвращать `503 Service Unavailable`, если publish исчерпал timeout
  или retries.
- [ ] 3.5 Не публиковать invalid event.

## 4. behavior-analysis-service

- [ ] 4.1 Добавить Gradle module `services:behavior-analysis-service` с
  Kotlin, Micronaut, JaCoCo и SonarQube.
- [ ] 4.2 Добавить application entrypoint, health endpoint и typed Kafka/Mongo
  configuration.
- [ ] 4.3 Добавить consumer `security.events.raw`.
- [ ] 4.4 Сохранять event history в owned MongoDB collection с unique index
  по `eventId`.
- [ ] 4.5 Идемпотентно обрабатывать duplicate `eventId`.
- [ ] 4.6 Публиковать poison records в `security.events.raw.dlq` после
  bounded retries.

## 5. Tests / Verification

- [ ] 5.1 Покрыть successful Kafka publish и совпадение `eventId` с REST
  response.
- [ ] 5.2 Покрыть validation reject без publish и Kafka failure → `503`.
- [ ] 5.3 Покрыть consume-and-persist, duplicate `eventId` и DLQ path.
- [ ] 5.4 Запустить tests и coverage verification для обоих services.
- [ ] 5.5 Запустить SonarQube analysis или явно зафиксировать, почему он
  недоступен.

## 6. Commit / PR

- [ ] 6.1 Обновить tasks statuses после verification.
- [ ] 6.2 Закоммитить change после успешных обязательных проверок по явной
  просьбе пользователя.
- [ ] 6.3 Подготовить команду для ручного push feature branch и PR в `dev`.

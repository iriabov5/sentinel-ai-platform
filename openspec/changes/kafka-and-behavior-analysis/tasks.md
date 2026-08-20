## 1. OpenSpec

- [x] 1.1 Описать proposal scope для Kafka pipeline и `behavior-analysis-service`.
- [x] 1.2 Описать delta specs для Kafka contract, ingestion publishing и
  behavior analysis storage.
- [x] 1.3 Описать implementation design, failure modes и non-goals.
- [x] 1.4 Запустить `openspec validate --all --strict --no-interactive`.

## 2. Contracts / Local Infra

- [x] 2.1 Добавить JSON Schema для Kafka payload accepted security event.
- [x] 2.2 Добавить AsyncAPI для topics `security.events.raw` и
  `security.events.raw.dlq`.
- [x] 2.3 Добавить Docker Compose с Kafka (KRaft) и MongoDB.
- [x] 2.4 Расширить `.env.example` safe local values для Kafka, MongoDB и
  порта `behavior-analysis-service`.

## 3. event-ingestion-service Producer

- [x] 3.1 Добавить Micronaut Kafka dependency и typed Kafka producer
  configuration.
- [x] 3.2 Добавить Kafka payload model с `eventId`, `receivedAt` и event
  envelope.
- [x] 3.3 Публиковать accepted event в `security.events.raw` с key
  `subject.id` до `202 Accepted`.
- [x] 3.4 Возвращать `503 Service Unavailable`, если publish исчерпал timeout
  или retries.
- [x] 3.5 Не публиковать invalid event.

## 4. behavior-analysis-service

- [x] 4.1 Добавить Gradle module `services:behavior-analysis-service` с
  Kotlin, Micronaut, JaCoCo и SonarQube.
- [x] 4.2 Добавить application entrypoint, health endpoint и typed Kafka/Mongo
  configuration.
- [x] 4.3 Добавить consumer `security.events.raw`.
- [x] 4.4 Сохранять event history в owned MongoDB collection с unique index
  по `eventId`.
- [x] 4.5 Идемпотентно обрабатывать duplicate `eventId`.
- [x] 4.6 Публиковать poison records в `security.events.raw.dlq` после
  bounded retries.

## 5. Tests / Verification

- [x] 5.1 Покрыть successful Kafka publish и совпадение `eventId` с REST
  response.
- [x] 5.2 Покрыть validation reject без publish и Kafka failure → `503`.
- [x] 5.3 Покрыть consume-and-persist, duplicate `eventId` и DLQ path.
- [x] 5.4 Запустить tests и coverage verification для обоих services.
- [x] 5.5 Запустить SonarQube analysis или явно зафиксировать, почему он
  недоступен. `./gradlew sonar` для `sentinel-ai-platform`: quality gate OK,
  0 bugs / 0 vulnerabilities / 0 code smells / 0 security hotspots,
  coverage 89.8%.

## 6. Commit / PR

- [x] 6.1 Обновить tasks statuses после verification.
- [x] 6.2 Закоммитить change после успешных обязательных проверок по явной
  просьбе пользователя.
- [ ] 6.3 Подготовить команду для ручного push feature branch и PR в `dev`.

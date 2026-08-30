## Why

Kotlin-сервисы уже на Kotlin, но местами читаются как Java на JVM-синтаксисе:
ручной `open class`/`open fun`, HTTP-handler на Reactor `Mono`, Java JUnit
assertions, `runBlocking` в тестах и незакреплённый official style. Это
расходится с [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
и с уже принятым coroutines-first правилом. Сейчас, до Phase 5, дешевле
зафиксировать идиоматический baseline, чем тащить Java-style в новые сервисы.

## What Changes

- Зафиксировать в conventions, что Kotlin-код SHALL следовать official
  Kotlin Coding Conventions и idiomatic Kotlin, а не Java-идиомам
  (properties вместо get/set, `val`/`data class`, expression bodies,
  collection API, string templates, coroutines вместо Reactor как HTTP API).
- Настроить `all-open` plugin (Micronaut annotations / official micronaut
  preset), чтобы не расставлять `open` вручную на beans.
- Привести `POST /api/v1/events` к `suspend` HTTP handler вместо `Mono`
  (контракт 202/400/503 не меняется).
- Добавить ktlint (official Kotlin style) и detekt в quality workflow;
  `kotlin.code.style=official` в Gradle.
- Почистить тестовый стиль: `kotlin.test` assertions, `runTest` вместо
  `runBlocking`, меньше field injection/`lateinit` там, где это не
  Micronaut Test constraint.
- Non-goals:
  - Не менять REST/Kafka/JSON Schema контракты и HTTP status semantics.
  - Не переписывать Java driver API (Mongo `Document`, Kafka `Future.get`
    на IO dispatcher, `TestPropertyProvider.getProperties()`).
  - Не вводить Kotest как новый test runner и не менять JUnit 5.
  - Не добавлять Spring Boot, не scaffold’ить `ai-detection-service`.
  - Не трогать Docker Compose, Railway и Kubernetes.
  - Не архивировать `behavioral-feature-extraction` в этом change.

## Capabilities

### New Capabilities

- (нет)

### Modified Capabilities

- `project-conventions`: Kotlin-код SHALL быть idiomatic Kotlin по official
  Coding Conventions, с явным списком запрещённых Java-идиом и допустимых
  Java-interop исключений на границах Micronaut/Kafka/Mongo.
- `quality-workflow`: перед commit Kotlin-изменений SHALL проходить ktlint
  и detekt в дополнение к tests/coverage/OpenSpec/SonarQube.
- `event-ingestion-service`: HTTP ingestion handler SHALL быть `suspend`,
  а не Reactor `Mono`/`Flux`, без изменения внешнего REST-контракта.

## Impact

- `services/event-ingestion-service` и `services/behavior-analysis-service`:
  исходники, tests, Gradle (all-open, ktlint, detekt, dependencies).
- Root `gradle.properties`: `kotlin.code.style=official`.
- REST/Kafka contracts, OpenAPI paths, Docker Compose, Railway: без
  breaking changes.

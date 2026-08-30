## 1. OpenSpec

- [x] 1.1 Описать proposal scope для idiomatic Kotlin style.
- [x] 1.2 Описать delta specs для `project-conventions`, `quality-workflow` и
  `event-ingestion-service`.
- [x] 1.3 Описать implementation design, Java-interop исключения и non-goals.
- [x] 1.4 Запустить `openspec validate --all --strict --no-interactive`.

## 2. Tooling / Gradle

- [x] 2.1 Добавить `kotlin.code.style=official` в `gradle.properties`.
- [x] 2.2 Сконфигурировать `all-open` для Micronaut annotations и продублировать
  список в KSP `kotlin.allopen.annotations` в обоих сервисах.
- [x] 2.3 Подключить ktlint (official Kotlin style) в оба Kotlin-модуля и в
  `check`.
- [x] 2.4 Подключить detekt без formatting-rules (чтобы не дублировать ktlint)
  в оба Kotlin-модуля и в `check`.
- [x] 2.5 Убрать явный `kotlin-stdlib-jdk8`; оставить транзитивный
  `kotlin-stdlib`. Добавить `kotlin-test` и `kotlinx-coroutines-test`.

## 3. Production Kotlin style

- [x] 3.1 Перевести `POST /api/v1/events` на `suspend` HTTP handler без `Mono`.
  Удалить `kotlinx-coroutines-reactor` / `micronaut-reactor` из ingestion,
  если больше не используются.
- [x] 3.2 После зелёной компиляции убрать ручные `open class`/`open fun` у
  beans, которые открывает all-open.
- [x] 3.3 Пройти production Kotlin обоих сервисов: properties/`data class`/`val`,
  string templates, Kotlin collection API, без Java get/set и без Reactor
  как HTTP return type. Java driver API на adapter-границах не переписывать.
- [x] 3.4 Оставить тонкий `runBlocking` только в Kafka listener, если callback
  не `suspend`; не размазывать его по domain/service коду.

## 4. Tests

- [x] 4.1 Заменить `org.junit.jupiter.api.Assertions` на `kotlin.test` в unit
  tests; сохранить JUnit 5 runner и русские `@DisplayName`.
- [x] 4.2 Перевести unit-тесты `suspend` функций на `runTest` вместо
  `runBlocking`.
- [x] 4.3 В `@MicronautTest` заменить `lateinit` + `@Inject` на constructor
  injection там, где Micronaut это позволяет.
- [x] 4.4 Убрать `!!` из tests в пользу `requireNotNull` или assertions.
  `TestPropertyProvider.getProperties()` и blocking HTTP client в tests
  оставить как допустимую Java-границу.

## 5. Verification

- [x] 5.1 Существующие HTTP/Kafka/Mongo tests остаются зелёными: ingestion
  202/400/503 и features 200/400/503 без смены контракта.
- [x] 5.2 Запустить `./gradlew check` (tests, jacoco, ktlint, detekt) для обоих
  сервисов.
- [x] 5.3 Запустить SonarQube analysis или явно зафиксировать, почему он
  недоступен. `./gradlew sonar` для `sentinel-ai-platform`: quality gate OK,
  0 bugs / 0 vulnerabilities / 0 code smells / 0 security hotspots,
  coverage 93.4% (new coverage 98.9%, new violations 0). CE task
  `36cbe45c-f990-4eff-8b76-5c35d6c7dbdd`.

## 6. Commit / PR

- [x] 6.1 Обновить tasks statuses после verification.
- [x] 6.2 Закоммитить change в `feature/idiomatic-kotlin-style` после успешных
  обязательных проверок по явной просьбе пользователя.
- [x] 6.3 Подготовить команду для ручного push feature branch и PR в `dev`.

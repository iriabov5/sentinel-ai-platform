## Context

См. `proposal.md` — Why. Оба Kotlin-сервиса уже работают end-to-end, но
исходники смешивают idiomatic Kotlin и Java/WebFlux-привычки. Источники
стиля для этого change:

- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
  — official JetBrains style (properties, immutability, expression bodies,
  collection API, coding conventions formatter).
- [All-open compiler plugin](https://kotlinlang.org/docs/all-open-plugin.html)
  — preset `micronaut`, чтобы не писать `open` вручную.
- [Micronaut Kotlin](https://docs.micronaut.io) — `suspend` HTTP handlers;
  all-open + KSP `kotlin.allopen.annotations`, потому что KSP не видит
  rewrite all-open.
- Android/Google Kotlin style и Effective Kotlin — как чеклист Java-идиом
  (`getX`/`setX`, `HashMap`/`ArrayList`, `!!`, Java assertions), не как
  Android-only правила.

Текущие наблюдаемые Java-style места:

- `all-open` plugin подключён, но **не сконфигурирован**; beans помечены
  `open class`/`open fun` руками.
- `SecurityEventController.accept` возвращает `Mono<HttpResponse<...>>`
  через `kotlinx-coroutines-reactor`, хотя service уже `suspend`, а
  features endpoint уже `suspend`.
- Тесты: `org.junit.jupiter.api.Assertions`, `runBlocking`, `lateinit` +
  `@Inject`, местами `!!`.
- `kotlin-stdlib-jdk8` при Kotlin 2.3 (API уже в `kotlin-stdlib`).
- Нет ktlint/detekt и нет `kotlin.code.style=official`.

Допустимые Java-границы, которые **не** переписываем в Kotlin-обёртки:

- MongoDB sync `Document` builder.
- Kafka producer `Future.get(timeout)` внутри `withContext(ioDispatcher)`.
- `TestPropertyProvider.getProperties(): MutableMap<String, String>`.
- Kafka listener callback, если Micronaut Kafka не даёт `suspend` listener:
  тонкий `runBlocking` только в adapter.

## Goals / Non-Goals

**Goals:**

- Сделать Kotlin-исходники читаемыми как Kotlin, не как транслированный Java.
- Выровнять HTTP API обоих сервисов на `suspend`.
- Включить воспроизводимый style gate в Gradle `check`.
- Оставить внешние контракты и runtime semantics без изменений.

**Non-Goals:**

- Не менять event/feature JSON Schema, Kafka topics, HTTP paths/status.
- Не заменять JUnit 5 на Kotest.
- Не вводить shared JVM library и не трогать Python/frontend.
- Не «улучшать» код через избыточные `let`/`apply`/`also` — official docs
  требуют уместных scope functions, не обязательных.

## Decisions

### Decision: Official Kotlin style + ktlint + detekt

`gradle.properties` получает `kotlin.code.style=official`. ktlint проверяет
formatting по Kotlin Coding Conventions. detekt ловит Java-style smells
(mutable Java collections, unnecessary apply, throws, complexity).

detekt formatting rules отключаем, чтобы не дублировать ktlint.

Alternative considered: только IntelliJ formatter. Отклонено — это не
воспроизводится в `./gradlew check`. Только detekt — слабее по official
formatting. ktfmt — другой dialect, не official conventions.

### Decision: Configure all-open for Micronaut, then drop manual `open`

Сконфигурировать all-open на Micronaut AOP/bean annotations
(`io.micronaut.aop.Around` как meta-annotation плюс явные
`@Controller`, `@Singleton`, `@KafkaListener`, `@Factory` по факту
компиляции) и продублировать список в KSP
`kotlin.allopen.annotations`. После зелёной компиляции убрать ручные
`open` у покрытых beans.

Test doubles, которые не являются Micronaut beans, остаются обычными
`class`, если им не нужен proxy.

Alternative considered: оставить ручной `open`. Это и есть Java-style
workaround, который change устраняет. `allopen { preset = "micronaut" }`
использовать, если Gradle DSL версии 2.3 его принимает; иначе явный
список annotations.

### Decision: Ingestion HTTP handler becomes `suspend`

`POST /api/v1/events` возвращает `HttpResponse<SecurityEventAcceptedResponse>`
из `suspend fun`. `mono { }` и зависимость `kotlinx-coroutines-reactor`
в ingestion убираются, если больше не используются. `micronaut-reactor`
удаляется из модуля, если после этого нет Reactor types.

Внешний контракт тот же: `202` после успешного publish, `400` validation,
`503` Kafka failure.

Alternative considered: оставить `Mono` «потому что Micronaut HTTP
reactive». Отклонено: main spec уже требует coroutines-first, features
endpoint уже `suspend`, AGENTS.md запрещает Reactor как default API.

### Decision: Keep blocking Java APIs at adapter edges

`Future.get` и Mongo `Document` остаются в persistence/kafka adapters на
IO dispatcher. Kafka listener может сохранить минимальный `runBlocking`,
если callback не `suspend`. Это не Java-style domain code, а граница
драйвера.

### Decision: Tests stay on JUnit 5 with Kotlin assertions

Подключить `kotlin-test` (JUnit 5) и `kotlinx-coroutines-test`. Unit-тесты
`suspend` функций переводятся на `runTest`. Assertions —
`kotlin.test.assertEquals` / `assertFailsWith`, не
`org.junit.jupiter.api.Assertions`. `@DisplayName` на русском остаётся.
`@MicronautTest` получает constructor injection вместо `lateinit` +
`@Inject`, где Micronaut это позволяет.

HTTP test client может остаться blocking: это test harness, не production
request path.

Alternative considered: Kotest. Сильнее «kotlin-native», но меняет весь
test stack без выигрыша для текущего объёма.

### Decision: Do not change runtime topology

Style change не трогает Docker Compose, порты, env vars, Railway.
`kotlin-stdlib-jdk8` заменяется на транзитивный `kotlin-stdlib`.

## Risks / Trade-offs

- [Risk] all-open + KSP рассинхрон: bean останется final, Micronaut AOP
  сломается в runtime.
  → Mitigation: повторить annotations в KSP arg; прогнать существующие
  MicronautTest до снятия ручного `open`.
- [Risk] ktlint массово переформатирует файлы и шумит в review.
  → Mitigation: один formatting pass, отдельно от semantic diffs, если
  diff слишком большой; оба прохода в одном change, но commits можно
  разделить по просьбе пользователя.
- [Risk] `suspend` ingestion handler меняет thread model HTTP.
  → Mitigation: Micronaut Kotlin runtime уже поддерживает suspend;
  существующие HTTP tests должны остаться зелёными без смены статусов.
- [Risk] detekt default rules слишком строгие для текущего объёма.
  → Mitigation: править код; baseline только для ложных срабатываний с
  комментарием, не как способ пропустить Java-style.

## Migration Plan

Локальный Gradle change, без schema/API migration. Rollback — revert
ветки. Runtime deploy не затрагивается.

## Open Questions

Нет. Выбор ktlint/detekt versions — implementation detail в `tasks.md`.

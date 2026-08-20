# Руководство по репозиторию

## Процесс разработки

Проект ведется через OpenSpec и Specification Driven Development. До первого
commit базовые правила живут прямо в `openspec/specs/`. После initial commit
каждое значимое изменение оформляется как отдельный OpenSpec change в
`openspec/changes/` с `proposal.md`, delta specs, `design.md` и `tasks.md`.

OpenSpec-документация пишется на русском языке. Технические термины, имена
сервисов, protocol names и framework names можно оставлять на английском.
Служебные заголовки OpenSpec должны сохранять формат `Requirement`, `Scenario`
и `SHALL`.

## Название

- Product name: `Sentinel AI Platform`
- Current repository/folder slug: `ai-security-platform`

Если позже репозиторий будет переименован, предпочтительный slug:
`sentinel-ai-platform`.

## Архитектура

Sentinel AI Platform — AI-powered SIEM/UEBA system. Платформа собирает security
events, строит behavioral context, ищет anomalies, создает incidents и помогает
security analyst расследовать подозрительное поведение.

Целевые backend services:

- `event-ingestion-service`
- `behavior-analysis-service`
- `ai-detection-service`
- `incident-service`
- `notification-service`
- `identity-access-service`

Frontend:

- `security-dashboard`

Future component:

- `security-agent` / `event-collector` на Go позже, после стабилизации core
  ingestion pipeline. Он не входит в первый MVP.

Первый implementation slice:

```text
event-ingestion-service -> Kafka -> behavior-analysis-service -> MongoDB
```

Не scaffold будущие сервисы без approved spec.

## Kotlin / Micronaut

Основной JVM stack: Kotlin, Micronaut, Gradle Kotlin DSL. Не добавляй Spring Boot
dependencies в Micronaut services.

Для async behavior по умолчанию предпочитай Kotlin Coroutines:

```text
suspend
coroutineScope
supervisorScope
async
withTimeout
Flow
```

Reactor не является default API style и требует design justification.

Внутри Kotlin service используй понятное разделение concerns:

```text
controller/
service/
model/
configuration/
security/
observability/
persistence/
```

## Качество перед commit

Перед commit выполняй применимые проверки:

```bash
openspec validate --all --strict --no-interactive
```

Когда появится Kotlin/Micronaut code, добавятся:

```bash
./gradlew test jacocoTestCoverageVerification
./gradlew sonar -Dsonar.token=<token>
```

SonarQube не является runtime dependency. `sonar.host.url` должен приходить из
Gradle property `sonarHostUrl`, environment variable `SONAR_HOST_URL` или
local-only fallback `http://localhost:9000`. Token не хранится в repository и
передается через `SONAR_TOKEN`, `-Dsonar.token=...` или user-level Gradle config.
Если SonarQube недоступен, tests/coverage все равно обязательны, а факт
пропуска SonarQube analysis нужно явно сообщить.

## Конфигурация

Repository хранит только safe defaults и examples. Значения, которые отличаются
между local, test, dev, staging, prod-like deployment или CI, должны приходить
через environment variables, system properties, Gradle properties, Railway
variables, Kubernetes ConfigMaps/Secrets или другой внешний механизм.

Не коммить реальные `.env`, credentials, tokens, passwords, private URLs или
секретные deployment values. Для локальных подсказок используй `.env.example`.

В Kotlin/Micronaut services runtime-настройки оформляй через typed
`@ConfigurationProperties` с validation и русским KDoc, если смысл параметров не
очевиден. Не читай environment variables напрямую в business logic.

## Git

- `main` / `master` — protected production branches.
- `dev` — integration branch.
- Новая функциональность: `feature/<english-kebab-name>`.
- Исправления: `bugfix/<english-kebab-name>`.
- Не коммить и не пушь напрямую в `main`, `master` или `dev`.
- Агент не выполняет `git push` самостоятельно. Push в remote выполняет
  пользователь.
- Агент не открывает pull request самостоятельно, если пользователь явно не
  попросил это сделать в текущей задаче.
- После commit агент сообщает hash, branch и что именно нужно push/review
  пользователю.
- Review и merge выполняет пользователь.

## Документация и тесты

Production Kotlin code получает русскоязычный KDoc/Javadoc, когда назначение,
контракт, concurrency, security или configuration semantics не очевидны из имени.
Не добавляй шумные комментарии к очевидному коду.

JUnit tests используют Russian `@DisplayName`, чтобы test report объяснял
проверяемое поведение.

REST APIs должны иметь OpenAPI specification и tests, которые проверяют важные
paths, schemas, validation errors и security schemes.

## Context

`security-event-contract` уже описывает общий event envelope и endpoint
`POST /api/v1/events`. Этот change превращает contract в первый работающий
Kotlin/Micronaut service, но сознательно оставляет Kafka на следующий change:
сначала нужно получить надежную, протестированную и observable HTTP boundary.

## Goals / Non-Goals

**Goals:**

- Создать минимальный, но production-oriented Micronaut service module.
- Реализовать REST ingestion endpoint с validation.
- Сохранить coroutines-first style для controller/service layer.
- Добавить OpenAPI generation setup.
- Добавить tests, JaCoCo coverage verification и SonarQube configuration.
- Сохранить stateless service design для горизонтального масштабирования.

**Non-Goals:**

- Не публиковать события в Kafka.
- Не хранить события в MongoDB/PostgreSQL.
- Не добавлять authentication или authorization.
- Не реализовывать behavior analysis.
- Не добавлять frontend.

## Decisions

### Decision: Use a Gradle multi-project baseline

Repository будет monorepo, поэтому root Gradle project должен подключать service
modules явно. Первый module будет `services:event-ingestion-service`, а будущие
Kotlin services смогут использовать тот же build style.

### Decision: Keep the first service stateless

`event-ingestion-service` в этом change не хранит состояние и не вызывает
downstream services. Он валидирует request, создает acceptance id и возвращает
`202 Accepted`. Это сохраняет простой масштабируемый runtime shape и готовит
место для Kafka producer без миграции API.

### Decision: Use coroutine-friendly endpoint shape

Controller method будет `suspend`, а application service будет отделен от HTTP
controller. На этом шаге нет blocking IO; когда появится Kafka producer, timeout,
retry и failure behavior будут описаны отдельным change.

### Decision: Bound metadata early

`metadata` остается flexible object-like data, но первый implementation ограничит
количество entries и длину ключей/значений. Это не решает всю sensitive-data
problem, но уже не позволяет endpoint принимать бесконтрольный payload.

### Decision: Authentication is postponed intentionally

Endpoint пока открыт внутри local/MVP boundary. API key/JWT для ingestion будет
отдельным security change, чтобы не смешивать первый runtime service с identity
architecture до появления `identity-access-service`.

## Validation Strategy

- OpenSpec validation: `openspec validate --all --strict --no-interactive`.
- Kotlin tests: Gradle `test`.
- Coverage: Gradle `jacocoTestCoverageVerification`.
- SonarQube: Gradle `sonar`, если local SonarQube и token доступны.

## Risks / Trade-offs

- [Risk] Endpoint пока не публикует события в Kafka.
  -> Mitigation: следующий change должен добавить producer и topic-level tests.
- [Risk] Authentication отсутствует.
  -> Mitigation: service не должен считаться public production ingress до
  отдельного security change.
- [Risk] Metadata limits могут оказаться слишком строгими или мягкими.
  -> Mitigation: limits вынести в configuration и уточнять после первых event
  examples.

## Open Questions

- Какие exact metadata limits считать достаточными для первого MVP?
- Нужен ли idempotency key уже до Kafka producer или добавить его вместе с
  Kafka publishing?

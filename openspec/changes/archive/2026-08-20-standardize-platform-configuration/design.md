## Context

Sentinel AI Platform будет polyglot monorepo с Kotlin/Micronaut services,
Python/FastAPI service, React frontend, Kafka, MongoDB, PostgreSQL, Redis,
Railway demo deployment и позже Kubernetes. Если configuration rules не
зафиксировать сейчас, каждый service начнет по-своему хранить ports, URLs,
tokens, credentials, limits и local-only values.

## Goals / Non-Goals

**Goals:**

- Разделить runtime configuration, build/quality configuration и local developer
  examples.
- Сохранить safe committed defaults для локального старта.
- Поддержать override через env vars/properties без изменения codebase.
- Запретить secrets в repository.
- Показать первый implementation example на `event-ingestion-service`.

**Non-Goals:**

- Не вводить centralized config server.
- Не создавать deployment manifests.
- Не добавлять secret manager integration.
- Не менять public REST contract.

## Decisions

### Decision: Use safe defaults plus external overrides

Committed config files могут содержать safe defaults, которые не раскрывают
секреты и подходят для local development. Все значения, которые меняются между
deploys, должны переопределяться через environment variables, system properties,
Gradle properties или платформенные mechanisms.

### Decision: Use Micronaut configuration model for runtime settings

Kotlin/Micronaut services должны использовать `@ConfigurationProperties` для
настроек, которые влияют на runtime behavior. Это дает type-safety, validation,
KDoc на смысл параметров и единый способ override через Micronaut environment.

### Decision: Keep local examples separate from real local state

Repository должен хранить `.env.example`, но не реальные `.env` файлы. Пример
должен объяснять variables и safe local values, а developer-specific values
остаются вне Git.

### Decision: Treat build/quality tools as engineering configuration

SonarQube, coverage paths, scanner tokens и CI-related values не являются runtime
configuration микросервисов. Project metadata можно хранить в Gradle files, но
hosts/tokens должны приходить извне. `localhost` допустим только как fallback
для local developer setup.

### Decision: Do not encode full dev/prod profiles yet

Будущие `application-dev.yml`, `application-prod.yml` или deployment-specific
files должны появляться только когда есть реальный deployment target. До этого
фиксируем naming and override rules, но не создаем fake production config.

## Risks / Trade-offs

- [Risk] Слишком много abstraction до deployment.
  -> Mitigation: change вводит только rules, `.env.example` и один typed config
  для уже существующего service.
- [Risk] Safe defaults могут быть ошибочно восприняты как production-ready.
  -> Mitigation: docs/specs явно называют их local defaults.
- [Risk] Environment variables могут стать несогласованными между services.
  -> Mitigation: namespacing by service/prefix and future `.env.example`
  expansion.

## Validation Strategy

- OpenSpec validation.
- Gradle build configuration compiles.
- `event-ingestion-service` tests and coverage pass.
- Configuration binding test validates default metadata limits.
- Sonar run remains optional when token/server is unavailable; command should be
  able to resolve host from property/env/fallback.

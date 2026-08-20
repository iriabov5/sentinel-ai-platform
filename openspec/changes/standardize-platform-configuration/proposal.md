## Why

После первого runtime service стало видно, что часть configuration values
зашивается в project files как будто они одинаковы для local, dev, CI и будущего
production-like deployment. Пример: `sonar.host.url = http://localhost:9000`.
Также runtime limits `event-ingestion-service` находятся в Kotlin constants, хотя
они относятся к deploy-time tuning.

Платформа должна заранее придерживаться 12-factor подхода: значения, которые
меняются между environments, не должны быть hardcoded в application code или
build scripts. Repository хранит безопасные defaults, examples и structure, а
реальные deploy values, credentials и secrets приходят извне.

## What Changes

- Ввести общий configuration management convention для runtime services,
  build/quality tooling и local development.
- Зафиксировать, что committed `application.yml` содержит только safe defaults.
- Зафиксировать environment-specific values через env vars, system properties,
  Gradle properties, Railway variables, Kubernetes ConfigMaps/Secrets или
  CI secrets.
- Зафиксировать, что secrets не коммитятся.
- Добавить `.env.example` с безопасными local examples.
- Сделать SonarQube host externalized:
  - Gradle property `sonarHostUrl`
  - environment variable `SONAR_HOST_URL`
  - local fallback `http://localhost:9000`
- Вынести metadata limits `event-ingestion-service` в typed
  `@ConfigurationProperties`.
- Добавить tests для configuration binding/validation.
- Non-goals:
  - Не добавлять real dev/prod secrets.
  - Не настраивать Railway/Kubernetes deployment.
  - Не подключать Kafka.
  - Не менять business contract security events.

## Capabilities

### New Capabilities

- Нет.

### Modified Capabilities

- `project-conventions`: добавляет platform-wide configuration management
  convention.
- `quality-workflow`: уточняет externalized build/quality tooling configuration.
- `event-ingestion-service`: переносит metadata limits в typed runtime
  configuration.

## Impact

- Уменьшает hardcoded local assumptions.
- Делает local/dev/CI/prod-like configuration model явным до появления Kafka,
  databases, Redis, frontend и deployment manifests.
- Добавляет первый пример typed Micronaut config class, который будущие services
  смогут повторять.

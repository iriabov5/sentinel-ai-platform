## 1. OpenSpec

- [x] 1.1 Описать proposal для platform configuration convention.
- [x] 1.2 Добавить delta spec для project conventions.
- [x] 1.3 Добавить delta spec для quality workflow.
- [x] 1.4 Добавить delta spec для `event-ingestion-service` configuration.
- [x] 1.5 Запустить OpenSpec validation.

## 2. Build / Quality Configuration

- [x] 2.1 Вынести SonarQube host в Gradle property/env var с local fallback.
- [x] 2.2 Обновить AGENTS guide для runtime/build/local configuration rules.
- [x] 2.3 Добавить `.env.example` с safe local examples.

## 3. Runtime Configuration

- [x] 3.1 Добавить typed Micronaut configuration для metadata limits.
- [x] 3.2 Перенести hardcoded metadata limits в `application.yml`.
- [x] 3.3 Обновить acceptance service, чтобы использовать configuration bean.
- [x] 3.4 Добавить Russian KDoc для configuration class.

## 4. Tests / Verification

- [x] 4.1 Добавить test для default metadata configuration binding.
- [x] 4.2 Запустить `openspec validate --all --strict --no-interactive`.
- [x] 4.3 Запустить `./gradlew :services:event-ingestion-service:check`.
- [x] 4.4 Проверить Sonar command behavior или явно зафиксировать недоступность
  внешней проверки. `./gradlew sonar` resolves local fallback
  `http://localhost:9000`, но завершается `401 Unauthorized`, потому что
  `sonar.token` / `SONAR_TOKEN` не задан.

## 5. Commit

- [x] 5.1 Проверить staged diff.
- [x] 5.2 Сделать local commit без push.

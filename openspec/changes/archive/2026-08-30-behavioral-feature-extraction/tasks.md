## 1. OpenSpec

- [x] 1.1 Описать proposal scope для on-demand feature extraction (Phase 4).
- [x] 1.2 Описать delta specs для контракта `behavioral-features` и
  `behavior-analysis-service`.
- [x] 1.3 Описать implementation design, failure modes и non-goals.
- [x] 1.4 Запустить `openspec validate --all --strict --no-interactive`.

## 2. Contracts

- [x] 2.1 Добавить JSON Schema `contracts/json-schema/subject-features.json`
  для response фич (subjectId, computedAt, window, features с name/value/unit/
  explanation).
- [x] 2.2 Зафиксировать в контракте `behavioral-features` конвенцию
  `metadata.bytes` для download-volume событий.
- [x] 2.3 Расширить `.env.example` safe local values для feature parameters.

## 3. behavior-analysis-service: configuration

- [x] 3.1 Добавить typed `FeatureProperties` (`@ConfigurationProperties`) с
  validation и KDoc для `window`, `baselineLookback`,
  `unusualTime.minEventsPerHour`, `downloadVolume.eventTypes`.
- [x] 3.2 Добавить safe local defaults: window 24h, baselineLookback 30d,
  minEventsPerHour 1, download types `FILE_DOWNLOAD` и `DATA_EXPORT`.

## 4. behavior-analysis-service: repository

- [x] 4.1 Добавить read-методы в repository: события subject внутри window и
  baseline до начала window по индексу `subject.id + occurredAt`.
- [x] 4.2 Обработать пустую history: запрос фич возвращает нулевые/пустые
  значения без ошибки.

## 5. behavior-analysis-service: feature computation

- [x] 5.1 Реализовать `new_ip_device`: distinct IP/device в window, не
  встречавшиеся в baseline; explanation с конкретными новыми IP/device.
- [x] 5.2 Реализовать `unusual_time`: доля событий window в часах,
  нехарактерных для subject (порог `minEventsPerHour` по baseline).
- [x] 5.3 Реализовать `request_rate`: количество событий window / длительность
  window в часах.
- [x] 5.4 Реализовать `download_volume`: сумма `metadata.bytes` событий
  download-типов в window; отсутствие поля даёт 0 без ошибки.
- [x] 5.5 Собрать response по контракту `behavioral-features` (subjectId,
  computedAt, window, features с explanation).

## 6. behavior-analysis-service: REST endpoint

- [x] 6.1 Добавить controller `GET /api/v1/subjects/{subjectId}/features` на
  coroutines (suspend), без blocking IO в request path.
- [x] 6.2 Маппинг ошибок: malformed subject id → `400`, MongoDB failure →
  `503 Service Unavailable`.
- [x] 6.3 Добавить OpenAPI annotations для нового endpoint.

## 7. Tests / Verification

- [x] 7.1 Unit tests: вычисление каждой фичи для известной history и пустой
  history.
- [x] 7.2 Unit tests: error paths (malformed id, repository failure).
- [x] 7.3 Integration tests через Testcontainers MongoDB: фичи по реальной
  history.
- [x] 7.4 Запустить tests и coverage verification для
  `behavior-analysis-service` (`./gradlew test jacocoTestCoverageVerification`).
- [x] 7.5 Запустить SonarQube analysis или явно зафиксировать, почему он
  недоступен. `./gradlew sonar` для `sentinel-ai-platform`: quality gate OK,
  0 bugs / 0 vulnerabilities / 0 code smells / 0 security hotspots,
  coverage 92.3%. SonarQube поднят заново (fresh community container на
  `localhost:9000`), токен сгенерирован и сохранён в local `.env`.

## 8. Commit / PR

- [x] 8.1 Обновить tasks statuses после verification.
- [x] 8.2 Закоммитить change после успешных обязательных проверок по явной
  просьбе пользователя.
- [x] 8.3 Подготовить команду для ручного push feature branch и PR в `dev`.

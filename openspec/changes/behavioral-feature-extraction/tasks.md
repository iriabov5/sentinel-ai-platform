## 1. OpenSpec

- [ ] 1.1 Описать proposal scope для on-demand feature extraction (Phase 4).
- [ ] 1.2 Описать delta specs для контракта `behavioral-features` и
  `behavior-analysis-service`.
- [ ] 1.3 Описать implementation design, failure modes и non-goals.
- [ ] 1.4 Запустить `openspec validate --all --strict --no-interactive`.

## 2. Contracts

- [ ] 2.1 Добавить JSON Schema `contracts/json-schema/subject-features.json`
  для response фич (subjectId, computedAt, window, features с name/value/unit/
  explanation).
- [ ] 2.2 Зафиксировать в контракте `behavioral-features` конвенцию
  `metadata.bytes` для download-volume событий.
- [ ] 2.3 Расширить `.env.example` safe local values для feature parameters.

## 3. behavior-analysis-service: configuration

- [ ] 3.1 Добавить typed `FeatureProperties` (`@ConfigurationProperties`) с
  validation и KDoc для `window`, `baselineLookback`,
  `unusualTime.minEventsPerHour`, `downloadVolume.eventTypes`.
- [ ] 3.2 Добавить safe local defaults: window 24h, baselineLookback 30d,
  minEventsPerHour 1, download types `FILE_DOWNLOAD` и `DATA_EXPORT`.

## 4. behavior-analysis-service: repository

- [ ] 4.1 Добавить read-методы в repository: события subject внутри window и
  baseline до начала window по индексу `subject.id + occurredAt`.
- [ ] 4.2 Обработать пустую history: запрос фич возвращает нулевые/пустые
  значения без ошибки.

## 5. behavior-analysis-service: feature computation

- [ ] 5.1 Реализовать `new_ip_device`: distinct IP/device в window, не
  встречавшиеся в baseline; explanation с конкретными новыми IP/device.
- [ ] 5.2 Реализовать `unusual_time`: доля событий window в часах,
  нехарактерных для subject (порог `minEventsPerHour` по baseline).
- [ ] 5.3 Реализовать `request_rate`: количество событий window / длительность
  window в часах.
- [ ] 5.4 Реализовать `download_volume`: сумма `metadata.bytes` событий
  download-типов в window; отсутствие поля даёт 0 без ошибки.
- [ ] 5.5 Собрать response по контракту `behavioral-features` (subjectId,
  computedAt, window, features с explanation).

## 6. behavior-analysis-service: REST endpoint

- [ ] 6.1 Добавить controller `GET /api/v1/subjects/{subjectId}/features` на
  coroutines (suspend), без blocking IO в request path.
- [ ] 6.2 Маппинг ошибок: malformed subject id → `400`, MongoDB failure →
  `503 Service Unavailable`.
- [ ] 6.3 Добавить OpenAPI annotations для нового endpoint.

## 7. Tests / Verification

- [ ] 7.1 Unit tests: вычисление каждой фичи для известной history и пустой
  history.
- [ ] 7.2 Unit tests: error paths (malformed id, repository failure).
- [ ] 7.3 Integration tests через Testcontainers MongoDB: фичи по реальной
  history.
- [ ] 7.4 Запустить tests и coverage verification для
  `behavior-analysis-service` (`./gradlew test jacocoTestCoverageVerification`).
- [ ] 7.5 Запустить SonarQube analysis или явно зафиксировать, почему он
  недоступен.

## 8. Commit / PR

- [ ] 8.1 Обновить tasks statuses после verification.
- [ ] 8.2 Закоммитить change после успешных обязательных проверок по явной
  просьбе пользователя.
- [ ] 8.3 Подготовить команду для ручного push feature branch и PR в `dev`.

## 1. Contract Review

- [x] 1.1 Проверить proposal scope и подтвердить, что change определяет contract,
  а не реализует `event-ingestion-service`.
- [x] 1.2 Проверить security event envelope: `eventType`, `subject`,
  `occurredAt`, `source`, `metadata`.
- [x] 1.3 Проверить MVP event types и убедиться, что они покрывают первый
  authentication/API/file/permission/token/admin activity scope.
- [x] 1.4 Проверить REST boundary `POST /api/v1/events` и expected `202 Accepted`
  response.
- [x] 1.5 Проверить Kafka topic `security.events.raw` и initial JSON Schema
  strategy.

## 2. Validation

- [x] 2.1 Запустить OpenSpec validation для `define-security-event-contract`.
- [x] 2.2 Исправить validation issues в proposal, specs, design или tasks.
- [x] 2.3 Подтвердить, что OpenSpec показывает planning artifacts как complete.

## 3. Apply / Archive

- [x] 3.1 После review применить change через OpenSpec archive.
- [x] 3.2 Проверить, что main spec появилась в
  `openspec/specs/security-event-contract/spec.md`.
- [ ] 3.3 Подготовить следующий implementation change:
  `implement-event-ingestion-service`.
- [ ] 3.4 В следующем implementation change отдельно решить exact metadata size
  limits и first endpoint authentication approach.

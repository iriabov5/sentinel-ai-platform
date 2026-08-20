## Purpose

Описывает целевую карту сервисов Sentinel AI Platform: 6 core backend services,
один frontend dashboard, future Go security agent, основные ownership
boundaries, synchronous/asynchronous interactions, databases и роль каждого
компонента в MVP и later stages.

## Requirements

### Requirement: Platform содержит 6 backend services и 1 frontend
Платформа SHALL развиваться как monorepo с 6 основными backend services и одним
frontend application, если future architecture change не докажет другой boundary.

#### Scenario: Описывается service map
- **WHEN** проект описывает целевую service map
- **THEN** backend services SHALL включать `event-ingestion-service`,
  `behavior-analysis-service`, `ai-detection-service`, `incident-service`,
  `notification-service` и `identity-access-service`
- **AND** frontend SHALL включать `security-dashboard`

### Requirement: Security agent является future component
Платформа SHALL рассматривать `security-agent` или `event-collector` как
future component, который может быть добавлен позже как lightweight Go component
для сбора events рядом с applications, servers или infrastructure sources.

#### Scenario: Future agent is planned
- **WHEN** platform needs local event collection near event sources
- **THEN** `security-agent` MAY collect local security events
- **AND** agent SHALL send normalized or raw events to `event-ingestion-service`
- **AND** agent SHALL NOT replace `event-ingestion-service`, Kafka or backend
  behavior analysis services

### Requirement: Event ingestion service принимает и публикует events
`event-ingestion-service` SHALL принимать security events, валидировать,
нормализовать и публиковать их в Kafka.

#### Scenario: Application отправляет security event
- **WHEN** application или agent отправляет security event
- **THEN** `event-ingestion-service` SHALL validate request
- **AND** `event-ingestion-service` SHALL publish accepted event to Kafka
- **AND** invalid event SHALL NOT be published

#### Scenario: Security agent отправляет event
- **WHEN** future `security-agent` отправляет collected event
- **THEN** `event-ingestion-service` SHALL remain the platform boundary for
  validation, normalization and Kafka publishing

### Requirement: Behavior analysis service строит behavioral context
`behavior-analysis-service` SHALL читать events из Kafka, сохранять behavioral
history в MongoDB и готовить features для anomaly detection.

#### Scenario: Event consumed from Kafka
- **WHEN** `behavior-analysis-service` получает event из Kafka
- **THEN** service SHALL update behavioral history in MongoDB
- **AND** service SHALL derive features for AI/anomaly detection when enough
  context is available

### Requirement: AI detection service оценивает anomaly score
`ai-detection-service` SHALL быть отдельным Python/FastAPI service для anomaly
scoring и explainable reasons.

#### Scenario: Features sent to AI detection
- **WHEN** behavior-analysis-service отправляет behavioral features
- **THEN** `ai-detection-service` SHALL return anomaly score
- **AND** response SHALL include explainable reasons or contributing factors

### Requirement: Incident service владеет incidents
`incident-service` SHALL создавать и вести incidents, хранить transactional
incident state в PostgreSQL и публиковать incident events для downstream
services.

#### Scenario: High-risk anomaly received
- **WHEN** high-risk anomaly требует расследования
- **THEN** `incident-service` SHALL create incident in PostgreSQL
- **AND** incident SHALL include status, severity, risk score, reasons and
  investigation metadata

### Requirement: Notification service отправляет alerts
`notification-service` SHALL слушать incident-related Kafka events и отправлять
уведомления по configured channels.

#### Scenario: Critical incident created
- **WHEN** Kafka содержит event о critical incident
- **THEN** `notification-service` SHALL prepare notification for security
  analyst or configured destination
- **AND** notification failure SHALL NOT rollback incident creation

### Requirement: Identity access service управляет доступом
`identity-access-service` SHALL управлять users, roles и access policies для
dashboard и protected platform APIs.

#### Scenario: Analyst opens dashboard
- **WHEN** user пытается войти в `security-dashboard`
- **THEN** `identity-access-service` SHALL authenticate user
- **AND** roles such as `SECURITY_ANALYST`, `ADMIN` and `AUDITOR` SHALL govern
  access to platform capabilities

### Requirement: Security dashboard является analyst UI
`security-dashboard` SHALL быть React/TypeScript frontend для расследования
incidents, просмотра anomalies, timelines, risk explanations и system state.

#### Scenario: Analyst investigates incident
- **WHEN** analyst открывает incident details
- **THEN** dashboard SHALL show incident status, severity, risk score, reasons,
  related events, timeline and available investigation actions

### Requirement: Kafka является asynchronous backbone
Kafka SHALL использоваться для event-driven communication между ingestion,
behavior analysis, incident и notification capabilities.

#### Scenario: Services exchange domain events
- **WHEN** service publishes security, anomaly or incident event
- **THEN** event SHALL be published to an explicit Kafka topic
- **AND** topic ownership, producer, consumers, partition key, retry and
  dead-letter expectations SHALL be defined in future event-contract specs

### Requirement: REST используется для query и selected synchronous calls
REST APIs SHALL использоваться для external ingestion, dashboard queries,
identity flows и selected service-to-service calls where synchronous response is
required.

#### Scenario: Dashboard requests incident details
- **WHEN** dashboard запрашивает incident details
- **THEN** dashboard SHALL use protected REST API rather than reading databases
  directly

### Requirement: Databases имеют ownership boundaries
MongoDB, PostgreSQL и future Redis usage SHALL have explicit owners and shall
not become shared mutable databases across services.

#### Scenario: Service needs persistent data
- **WHEN** service introduces persistent data
- **THEN** service SHALL own its data model
- **AND** other services SHALL access that data through APIs or events, not by
  directly sharing tables or collections

### Requirement: First MVP ограничен event pipeline
Первый MVP SHALL focus on event ingestion, Kafka publishing, behavior analysis
consumption and MongoDB event history before adding full AI, incident workflow,
notifications, identity or dashboard features.

#### Scenario: First implementation starts
- **WHEN** project begins application code
- **THEN** implementation SHALL start with
  `event-ingestion-service -> Kafka -> behavior-analysis-service -> MongoDB`
- **AND** other services SHALL remain planned but not scaffolded until their
  own approved specs exist

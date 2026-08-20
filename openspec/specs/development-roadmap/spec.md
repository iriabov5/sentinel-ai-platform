## Purpose

Описывает последовательный development roadmap Sentinel AI Platform: от
bootstrap main specs к первому event pipeline, затем AI detection, incidents,
dashboard, identity, notifications, observability/deployment и future Go
security agent.

## Requirements

### Requirement: Phase 0 фиксирует bootstrap baseline
Phase 0 SHALL establish project baseline before application code starts.

#### Scenario: Bootstrap is completed
- **WHEN** Phase 0 завершается
- **THEN** main specs, README, AGENTS guide, quality workflow and Git workflow
  SHALL exist
- **AND** repository SHALL be ready for initial commit

### Requirement: Phase 1 defines security event contracts
Phase 1 SHALL define the first security event contract before implementing event
ingestion.

#### Scenario: Event contract work starts
- **WHEN** Phase 1 starts
- **THEN** project SHALL define accepted security event shape, validation rules,
  first event types, OpenAPI ingestion contract and first Kafka topic contract

### Requirement: Phase 2 implements first Kotlin ingestion slice
Phase 2 SHALL implement `event-ingestion-service` as the first Kotlin/Micronaut
service.

#### Scenario: First JVM service is implemented
- **WHEN** Phase 2 starts
- **THEN** project SHALL create Gradle/Micronaut setup, REST ingestion endpoint,
  validation, OpenAPI generation, tests, coverage and local quality checks

### Requirement: Phase 3 adds Kafka and behavior analysis storage
Phase 3 SHALL connect ingestion to Kafka and introduce behavior analysis with
MongoDB event history.

#### Scenario: Event pipeline becomes end to end
- **WHEN** Phase 3 completes
- **THEN** accepted security event SHALL flow from `event-ingestion-service` to
  Kafka, then to `behavior-analysis-service`, then into MongoDB history

### Requirement: Phase 4 introduces feature extraction
Phase 4 SHALL derive explainable behavioral features from event history.

#### Scenario: Features are calculated
- **WHEN** behavior analysis has enough context
- **THEN** service SHALL derive initial features such as new IP/device,
  unusual time, request rate or download volume

### Requirement: Phase 5 adds AI detection service
Phase 5 SHALL introduce `ai-detection-service` with simple explainable anomaly
scoring before advanced ML.

#### Scenario: AI scoring is introduced
- **WHEN** Phase 5 starts
- **THEN** Python/FastAPI service SHALL accept features and return anomaly score
  with reasons
- **AND** simple statistical or rule-assisted scoring MAY come before PyTorch
  models

### Requirement: Phase 6 adds incident management
Phase 6 SHALL introduce `incident-service` and PostgreSQL-backed incident
lifecycle.

#### Scenario: High-risk anomaly creates incident
- **WHEN** high-risk anomaly is detected
- **THEN** incident-service SHALL create incident with severity, status, reasons
  and investigation state

### Requirement: Phase 7 adds dashboard MVP
Phase 7 SHALL introduce `security-dashboard` for analyst investigation.

#### Scenario: Analyst uses dashboard MVP
- **WHEN** dashboard MVP is available
- **THEN** analyst SHALL see incident feed, incident details, timeline and risk
  explanation backed by platform APIs or explicit development mock boundary

### Requirement: Phase 8 adds identity and access
Phase 8 SHALL introduce `identity-access-service` and role-aware access control.

#### Scenario: Roles are enforced
- **WHEN** identity phase completes
- **THEN** roles such as `SECURITY_ANALYST`, `ADMIN` and `AUDITOR` SHALL govern
  dashboard and protected API access

### Requirement: Phase 9 adds notifications
Phase 9 SHALL introduce `notification-service` for critical incident alerts.

#### Scenario: Critical incident is created
- **WHEN** critical incident event is published
- **THEN** notification-service SHALL send configured alert without coupling
  incident creation to notification delivery success

### Requirement: Phase 10 adds observability and deployment hardening
Phase 10 SHALL harden observability and deployment for local Docker Compose,
Railway demo deployment and later Kubernetes.

#### Scenario: Platform is prepared for demo deployment
- **WHEN** Phase 10 completes
- **THEN** services SHALL expose health, metrics and deployment configuration
  appropriate for local and Railway execution

### Requirement: Phase 11 may add Go security agent
Phase 11 SHALL reserve the option to introduce a Go-based `security-agent` or
`event-collector` after the core ingestion pipeline and service contracts are
stable.

#### Scenario: Go agent is added later
- **WHEN** platform needs lightweight event collection near applications,
  servers or infrastructure sources
- **THEN** future OpenSpec change MAY introduce Go `security-agent`
- **AND** agent SHALL collect local security events and send them to
  `event-ingestion-service`
- **AND** agent SHALL be distributed as a lightweight binary or container
- **AND** agent SHALL NOT be part of the first MVP implementation

### Requirement: Roadmap phases remain incremental
Each phase SHALL be implemented through small OpenSpec changes rather than one
large implementation request.

#### Scenario: New phase starts
- **WHEN** work begins on a roadmap phase
- **THEN** project SHALL create focused OpenSpec change for that phase or slice
- **AND** future phase services SHALL NOT be scaffolded without approved specs

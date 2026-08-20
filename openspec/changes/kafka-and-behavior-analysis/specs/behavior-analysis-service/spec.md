## Purpose

Определяет первый runtime capability `behavior-analysis-service`: Kotlin/Micronaut
service, который читает accepted security events из Kafka и сохраняет owned
event history в MongoDB без feature extraction.

## ADDED Requirements

### Requirement: Service consumes accepted security events
`behavior-analysis-service` SHALL consume records from Kafka topic
`security.events.raw`.

#### Scenario: Valid Kafka record is received
- **WHEN** topic contains a valid accepted security event
- **THEN** service SHALL process the record
- **AND** processing SHALL persist event history before considering the record
  complete

### Requirement: Service stores owned event history in MongoDB
`behavior-analysis-service` SHALL persist consumed events in MongoDB as owned
event history and SHALL NOT share that collection as a writable store with
other services.

#### Scenario: Event is stored
- **WHEN** a valid Kafka record is processed
- **THEN** MongoDB document SHALL include `eventId`, `receivedAt`, `eventType`,
  `subject`, `occurredAt`, `source` and stored timestamp
- **AND** document MAY include bounded `metadata`

#### Scenario: Duplicate eventId is consumed
- **WHEN** a record with an already stored `eventId` is consumed
- **THEN** service SHALL persist at most one history document for that
  `eventId`
- **AND** consumer SHALL treat the duplicate as successfully processed

### Requirement: Persistently failed records go to dead-letter topic
`behavior-analysis-service` SHALL send records that remain unprocessable after
bounded retries to `security.events.raw.dlq`.

#### Scenario: Poison payload cannot be stored
- **WHEN** record payload is structurally unusable or persistence keeps failing
  after retries
- **THEN** service SHALL publish the record to `security.events.raw.dlq`
- **AND** service SHALL continue consuming subsequent records

### Requirement: Service does not derive behavioral features yet
`behavior-analysis-service` SHALL complete Phase 3 processing by storing event
history and SHALL NOT require derived features, anomaly scores or incident
creation.

#### Scenario: Event history is stored without features
- **WHEN** a valid event is consumed
- **THEN** service SHALL persist the event
- **AND** service SHALL NOT fail processing because features are absent

### Requirement: Service uses Kotlin/Micronaut stack
`behavior-analysis-service` SHALL use Kotlin, JDK 21, Micronaut 4.x and Gradle
Kotlin DSL.

#### Scenario: Service build is inspected
- **WHEN** service module build configuration is reviewed
- **THEN** build SHALL use Kotlin and Micronaut plugins
- **AND** build SHALL NOT introduce Spring Boot dependencies

### Requirement: Service follows coroutines-first style
`behavior-analysis-service` SHALL use coroutine-friendly APIs for Kafka consume
and MongoDB persistence where practical.

#### Scenario: Consumer implementation is inspected
- **WHEN** Kafka consumption is implemented
- **THEN** service boundary SHALL use `suspend` functions or equivalent
  coroutine-friendly APIs
- **AND** implementation SHALL avoid blocking IO on the consume path

### Requirement: Kafka and MongoDB settings are runtime configuration
`behavior-analysis-service` SHALL expose Kafka and MongoDB connection settings
as typed runtime configuration.

#### Scenario: Default local settings are used
- **WHEN** service starts without explicit broker or database overrides
- **THEN** service SHALL use safe committed local defaults suitable for Docker
  Compose

#### Scenario: Connection settings are overridden
- **WHEN** environment or external configuration overrides Kafka or MongoDB
  settings
- **THEN** service SHALL apply those values through Micronaut configuration
  binding
- **AND** consume/persist logic SHALL NOT rely on hardcoded broker or database
  coordinates as the only source of truth

### Requirement: Service exposes runtime visibility
`behavior-analysis-service` SHALL expose basic runtime visibility suitable for
local development.

#### Scenario: Health endpoint is requested
- **WHEN** service is running
- **THEN** Micronaut Management SHALL expose health endpoint

### Requirement: Service has automated quality checks
`behavior-analysis-service` SHALL include automated tests and coverage check.

#### Scenario: Code is ready for commit
- **WHEN** implementation is complete
- **THEN** tests SHALL cover successful consume-and-persist, duplicate
  `eventId` and dead-letter failure path
- **AND** JaCoCo coverage verification SHALL pass before commit

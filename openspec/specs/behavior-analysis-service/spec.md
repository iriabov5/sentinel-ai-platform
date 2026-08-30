# behavior-analysis-service Specification

## Purpose

Определяет runtime capability `behavior-analysis-service`: Kotlin/Micronaut
service, который читает accepted security events из Kafka, сохраняет owned
event history в MongoDB и считает explainable behavioral features on demand.

## Requirements

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
- **AND** consume-and-persist and DLQ integration tests SHALL start Kafka and
  MongoDB through Testcontainers
- **AND** tests SHALL cover subject features computation for known history,
  empty history and error paths
- **AND** JaCoCo coverage verification SHALL pass before commit

### Requirement: Kafka and MongoDB behavior is verified with Testcontainers
`behavior-analysis-service` SHALL verify consume, persistence and dead-letter
paths through Testcontainers integration tests in addition to unit tests with
doubles.

#### Scenario: Docker is available
- **WHEN** Kafka and MongoDB integration tests run and Docker is available
- **THEN** tests SHALL start Kafka and MongoDB through Testcontainers
- **AND** tests SHALL persist a consumed event into owned event history
- **AND** tests SHALL keep a single history document for duplicate `eventId`
- **AND** tests SHALL publish a poison record to `security.events.raw.dlq`

#### Scenario: Docker is unavailable
- **WHEN** Docker is not available
- **THEN** Testcontainers Kafka and MongoDB tests SHALL be skipped
- **AND** unit tests SHALL still verify persist, duplicate `eventId` and
  dead-letter behavior

### Requirement: Service exposes subject features endpoint
`behavior-analysis-service` SHALL expose
`GET /api/v1/subjects/{subjectId}/features` for on-demand behavioral feature
computation.

#### Scenario: Features are requested for a subject
- **WHEN** client requests features for an existing subject id
- **THEN** service SHALL compute features from owned event history at request
  time
- **AND** response SHALL follow the `behavioral-features` contract

#### Scenario: Subject has no history
- **WHEN** client requests features for a subject without stored events
- **THEN** service SHALL return the feature set with empty or zero values
- **AND** service SHALL NOT fail the request

#### Scenario: Invalid subject id is requested
- **WHEN** client sends a malformed subject id
- **THEN** service SHALL return a validation error

### Requirement: Service computes behavioral features on demand
`behavior-analysis-service` SHALL compute `new_ip_device`, `unusual_time`,
`request_rate` and `download_volume` from event history at request time and
SHALL NOT materialize or cache features during consume.

#### Scenario: Features are computed at request time
- **WHEN** features endpoint is called
- **THEN** computation SHALL read owned event history for the subject
- **AND** feature values SHALL reflect history as of the request moment
- **AND** consume path SHALL NOT compute or store features

### Requirement: Event persistence does not require features
`behavior-analysis-service` SHALL persist event history without requiring
derived features, anomaly scores or incident creation.

#### Scenario: Event is stored without features
- **WHEN** a valid event is consumed
- **THEN** service SHALL persist the event
- **AND** service SHALL NOT fail processing because features are absent

### Requirement: Feature parameters are runtime configuration
`behavior-analysis-service` SHALL expose feature windows and thresholds as
typed runtime configuration.

#### Scenario: Default feature parameters are used
- **WHEN** service starts without explicit feature overrides
- **THEN** service SHALL use safe committed local defaults for windows and
  thresholds

#### Scenario: Feature parameters are overridden
- **WHEN** environment or external configuration overrides feature parameters
- **THEN** service SHALL apply those values through Micronaut configuration
  binding

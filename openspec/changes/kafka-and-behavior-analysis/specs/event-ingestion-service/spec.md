## MODIFIED Requirements

### Requirement: Service exposes REST ingestion endpoint
`event-ingestion-service` SHALL expose `POST /api/v1/events` for accepting MVP
security events.

#### Scenario: Valid event is accepted
- **WHEN** client submits a valid security event
- **AND** the event is successfully published to Kafka
- **THEN** service SHALL return `202 Accepted`
- **AND** response SHALL include `eventId`, `status` and `receivedAt`

#### Scenario: Invalid event is rejected
- **WHEN** client submits invalid security event payload
- **THEN** service SHALL return validation error
- **AND** service SHALL NOT create acceptance response for downstream processing
- **AND** service SHALL NOT publish the event to Kafka

### Requirement: Service has automated quality checks
`event-ingestion-service` SHALL include automated tests and coverage check.

#### Scenario: Code is ready for commit
- **WHEN** implementation is complete
- **THEN** tests SHALL cover successful acceptance and validation failures
- **AND** tests SHALL cover successful Kafka publish and Kafka publish failure
- **AND** Kafka publish integration tests SHALL start Kafka through Testcontainers
- **AND** JaCoCo coverage verification SHALL pass before commit

## ADDED Requirements

### Requirement: Accepted event is published to Kafka
`event-ingestion-service` SHALL publish an accepted security event to Kafka
topic `security.events.raw` before returning `202 Accepted`.

#### Scenario: Valid event is published
- **WHEN** client submits a valid security event
- **THEN** service SHALL publish JSON payload with the same `eventId` as the
  REST response
- **AND** Kafka record key SHALL equal `subject.id`

#### Scenario: Kafka publish fails
- **WHEN** client submits a valid security event
- **AND** Kafka publish fails after bounded retries or timeout
- **THEN** service SHALL NOT return `202 Accepted`
- **AND** service SHALL return a server error
- **AND** client-visible success SHALL NOT be returned for an unpublished event

### Requirement: Kafka producer settings are runtime configuration
`event-ingestion-service` SHALL expose Kafka bootstrap, topic, timeout and retry
settings as typed runtime configuration.

#### Scenario: Default Kafka settings are used
- **WHEN** service starts without explicit Kafka overrides
- **THEN** service SHALL use safe committed local defaults suitable for Docker
  Compose

#### Scenario: Kafka settings are overridden
- **WHEN** environment or external configuration overrides Kafka settings
- **THEN** service SHALL apply those values through Micronaut configuration
  binding
- **AND** producer logic SHALL NOT rely on hardcoded broker coordinates as the
  only source of truth

### Requirement: Kafka publish is verified with Testcontainers
`event-ingestion-service` SHALL verify Kafka publishing through Testcontainers
integration tests in addition to unit tests with doubles.

#### Scenario: Docker is available
- **WHEN** Kafka integration tests run and Docker is available
- **THEN** tests SHALL start Kafka through Testcontainers
- **AND** tests SHALL observe the accepted event on `security.events.raw`
- **AND** Kafka record key SHALL equal `subject.id`

#### Scenario: Docker is unavailable
- **WHEN** Docker is not available
- **THEN** Testcontainers Kafka tests SHALL be skipped
- **AND** unit tests SHALL still verify publish success and publish failure
  behavior

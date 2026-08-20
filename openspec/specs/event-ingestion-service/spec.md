# event-ingestion-service Specification

## Purpose
Определяет первый runtime capability `event-ingestion-service`: Kotlin/Micronaut
service для приема MVP security events через REST, validation и asynchronous
acceptance response.

## Requirements

### Requirement: Service exposes REST ingestion endpoint
`event-ingestion-service` SHALL expose `POST /api/v1/events` for accepting MVP
security events.

#### Scenario: Valid event is accepted
- **WHEN** client submits a valid security event
- **THEN** service SHALL return `202 Accepted`
- **AND** response SHALL include `eventId`, `status` and `receivedAt`

#### Scenario: Invalid event is rejected
- **WHEN** client submits invalid security event payload
- **THEN** service SHALL return validation error
- **AND** service SHALL NOT create acceptance response for downstream processing

### Requirement: Service implements security event validation
`event-ingestion-service` SHALL validate required fields from
`security-event-contract`.

#### Scenario: Required envelope fields are present
- **WHEN** event contains `eventType`, `subject`, `occurredAt` and `source`
- **THEN** service SHALL validate nested required fields before acceptance

#### Scenario: Required nested fields are missing
- **WHEN** event misses `subject.id`, `subject.type` or `source.application`
- **THEN** service SHALL reject the event with validation error

### Requirement: Service limits metadata payload shape
`event-ingestion-service` SHALL enforce initial metadata limits to keep accepted
payloads bounded.

#### Scenario: Metadata is within limits
- **WHEN** event metadata contains allowed key/value entries
- **THEN** service SHALL accept metadata as part of event payload

#### Scenario: Metadata exceeds limits
- **WHEN** event metadata exceeds configured entry, key length or value length
  limits
- **THEN** service SHALL reject the event with validation error

### Requirement: Service uses Kotlin/Micronaut stack
`event-ingestion-service` SHALL use Kotlin, JDK 21, Micronaut 4.x and Gradle
Kotlin DSL.

#### Scenario: Service build is inspected
- **WHEN** service module build configuration is reviewed
- **THEN** build SHALL use Kotlin and Micronaut plugins
- **AND** build SHALL NOT introduce Spring Boot dependencies

### Requirement: Service follows coroutines-first style
`event-ingestion-service` SHALL use coroutine-friendly APIs for request handling
where practical.

#### Scenario: Controller implementation is inspected
- **WHEN** ingestion endpoint is implemented
- **THEN** controller or service boundary SHALL use `suspend` functions
- **AND** implementation SHALL avoid blocking IO in the request path

### Requirement: Service exposes runtime visibility
`event-ingestion-service` SHALL expose basic runtime visibility suitable for
local development and later deployment.

#### Scenario: Health endpoint is requested
- **WHEN** service is running
- **THEN** Micronaut Management SHALL expose health endpoint

#### Scenario: API documentation is generated
- **WHEN** build runs OpenAPI annotation processing
- **THEN** service SHALL generate OpenAPI documentation for the ingestion API

### Requirement: Service has automated quality checks
`event-ingestion-service` SHALL include automated tests and coverage check.

#### Scenario: Code is ready for commit
- **WHEN** implementation is complete
- **THEN** tests SHALL cover successful acceptance and validation failures
- **AND** JaCoCo coverage verification SHALL pass before commit

### Requirement: Metadata limits are runtime configuration
`event-ingestion-service` SHALL expose metadata validation limits as typed
runtime configuration.

#### Scenario: Default metadata limits are used
- **WHEN** service starts without explicit metadata limit overrides
- **THEN** service SHALL use safe committed defaults for max entries, key length
  and value length

#### Scenario: Metadata limits are overridden
- **WHEN** environment or external configuration overrides metadata limits
- **THEN** service SHALL apply those values through Micronaut configuration
  binding
- **AND** acceptance logic SHALL NOT rely on hardcoded constants as the only
  source of truth

### Requirement: Metadata configuration is validated
`event-ingestion-service` SHALL validate metadata configuration values.

#### Scenario: Metadata configuration is bound
- **WHEN** configuration bean is created
- **THEN** max entries, key length and value length SHALL be positive values

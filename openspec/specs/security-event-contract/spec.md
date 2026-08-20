# security-event-contract Specification

## Purpose
Определяет первый MVP contract для security events: REST ingestion boundary,
обязательные поля event envelope, поддерживаемые event types, validation rules,
Kafka topic и schema strategy для дальнейшей обработки событий.

## Requirements

### Requirement: Security event имеет стабильный envelope
Security event SHALL иметь стабильный contract envelope, который может быть
принят через REST API и затем опубликован в Kafka без потери семантики.

#### Scenario: Valid security event принят
- **WHEN** client отправляет valid security event
- **THEN** event SHALL contain `eventType`, `subject`, `occurredAt`, `source` and
  optional `metadata`
- **AND** accepted event SHALL be suitable for later Kafka publishing

### Requirement: Event type является explicit
Security event SHALL include explicit `eventType` from the supported MVP event
type set.

#### Scenario: Supported event type is used
- **WHEN** event contains `eventType` equal to `LOGIN_FAILED`
- **THEN** event SHALL be eligible for validation and acceptance

#### Scenario: Unsupported event type is used
- **WHEN** event contains unknown `eventType`
- **THEN** event SHALL be rejected with validation error
- **AND** rejected event SHALL NOT be published to Kafka

### Requirement: MVP event types are defined
MVP SHALL support a limited initial event type set for authentication, API,
file, permission, token and administration activity.

#### Scenario: MVP event type list is inspected
- **WHEN** event contract is reviewed
- **THEN** supported event types SHALL include `LOGIN_SUCCESS`, `LOGIN_FAILED`,
  `API_REQUEST`, `FILE_DOWNLOAD`, `PERMISSION_CHANGE`, `DEVICE_LOGIN`,
  `TOKEN_CREATED`, `PRIVILEGE_ESCALATION`, `DATA_EXPORT` and `ADMIN_ACTION`

### Requirement: Subject identifies affected actor
Security event SHALL identify the affected actor through `subject`.

#### Scenario: User subject is provided
- **WHEN** event describes user behavior
- **THEN** `subject` SHALL include `type` and `id`
- **AND** `subject.type` SHALL be compatible with user-like actor semantics

#### Scenario: Service subject is provided
- **WHEN** event describes service account behavior
- **THEN** `subject` SHALL include `type` and `id`
- **AND** `subject.type` SHALL be compatible with service-like actor semantics

### Requirement: Source describes event origin
Security event SHALL include source information that helps later analysis reason
about application, IP, device, endpoint or infrastructure origin.

#### Scenario: Source is provided
- **WHEN** event is submitted
- **THEN** `source` SHALL include at least application or system identifier
- **AND** source MAY include IP address, device id, endpoint, region or other
  origin attributes

### Requirement: Occurred time is required
Security event SHALL include `occurredAt` as the time when the source activity
happened.

#### Scenario: Event includes occurredAt
- **WHEN** event includes valid ISO-8601 `occurredAt`
- **THEN** event SHALL be eligible for acceptance

#### Scenario: Event misses occurredAt
- **WHEN** event does not include `occurredAt`
- **THEN** event SHALL be rejected with validation error

### Requirement: Metadata is flexible but bounded
Security event MAY include event-specific `metadata`, but metadata SHALL remain
bounded and safe for storage and later analysis.

#### Scenario: Metadata is included
- **WHEN** event includes metadata
- **THEN** metadata SHALL be represented as object-like key/value data
- **AND** future implementation SHALL reject metadata that violates configured
  size or shape limits

### Requirement: Ingestion REST endpoint accepts events
`event-ingestion-service` SHALL expose `POST /api/v1/events` as the first REST
boundary for receiving security events.

#### Scenario: Valid event is submitted
- **WHEN** client sends valid event to `POST /api/v1/events`
- **THEN** endpoint SHALL return `202 Accepted`
- **AND** response SHALL include accepted event identifier or acceptance status

#### Scenario: Invalid event is submitted
- **WHEN** client sends invalid event to `POST /api/v1/events`
- **THEN** endpoint SHALL return validation error
- **AND** invalid event SHALL NOT be accepted for downstream processing

### Requirement: Kafka raw event topic is defined
Accepted security events SHALL be published to Kafka topic
`security.events.raw` in the later implementation change.

#### Scenario: Event is accepted for downstream processing
- **WHEN** valid event is accepted by ingestion service
- **THEN** later Kafka producer implementation SHALL publish normalized event to
  `security.events.raw`
- **AND** event key SHALL preserve a stable partitioning strategy based on
  subject or another explicitly approved key

### Requirement: JSON Schema is initial schema strategy
MVP security event contract SHALL use JSON Schema as the initial payload schema
strategy for REST and Kafka payload validation.

#### Scenario: Schema strategy is reviewed
- **WHEN** first event contract is implemented
- **THEN** JSON Schema SHALL be used as initial schema format
- **AND** Avro or Schema Registry SHALL require a later approved architecture
  change if introduced

### Requirement: Sensitive data handling is considered
Security event contract SHALL avoid requiring secrets or raw confidential payload
values in the common envelope.

#### Scenario: Event includes sensitive metadata
- **WHEN** event-specific metadata contains sensitive values
- **THEN** future implementation SHALL provide validation, masking, hashing or
  rejection behavior according to approved sensitive-data rules

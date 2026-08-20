## Purpose

Определяет contract-first Kafka boundary для accepted security events: topic,
JSON payload envelope, partition key, ownership producer/consumer, retry и
dead-letter поведение до реализации publisher и consumer.

## ADDED Requirements

### Requirement: Accepted events use raw security events topic
Accepted security events SHALL be published to Kafka topic
`security.events.raw`.

#### Scenario: Event is accepted for downstream processing
- **WHEN** `event-ingestion-service` accepts a valid security event
- **THEN** the accepted event SHALL be published to `security.events.raw`
- **AND** invalid REST events SHALL NOT appear on the topic

### Requirement: Kafka payload preserves ingestion identity and event envelope
Kafka message value SHALL be JSON and SHALL include ingestion identity together
with the accepted security event envelope.

#### Scenario: Payload is inspected
- **WHEN** a message is published to `security.events.raw`
- **THEN** payload SHALL contain `eventId`, `receivedAt`, `eventType`,
  `subject`, `occurredAt` and `source`
- **AND** payload MAY contain bounded `metadata`
- **AND** `eventId` SHALL match the identifier returned to the REST client

### Requirement: Partition key is subject identity
Kafka record key SHALL use a stable partitioning strategy based on subject
identity.

#### Scenario: Event for a subject is published
- **WHEN** accepted event contains `subject.id`
- **THEN** Kafka record key SHALL equal `subject.id`
- **AND** subsequent events for the same subject SHALL share that key

### Requirement: JSON Schema and AsyncAPI document the Kafka contract
Kafka payload and topic contract SHALL be documented with JSON Schema and
AsyncAPI before producer or consumer implementation depends on them.

#### Scenario: Contract artifacts are reviewed
- **WHEN** Kafka publishing is implemented
- **THEN** repository SHALL contain AsyncAPI description for
  `security.events.raw`
- **AND** payload SHALL have an explicit JSON Schema
- **AND** Avro or Schema Registry SHALL NOT be introduced in this change

### Requirement: Producer and consumer ownership is explicit
`event-ingestion-service` SHALL be the producer of `security.events.raw`, and
`behavior-analysis-service` SHALL be the consumer of that topic.

#### Scenario: Topic ownership is reviewed
- **WHEN** Kafka contract is inspected
- **THEN** only `event-ingestion-service` SHALL publish accepted security events
  to `security.events.raw`
- **AND** `behavior-analysis-service` SHALL consume that topic for event history

### Requirement: Delivery is at-least-once with a dead-letter topic
Kafka processing SHALL use at-least-once delivery and SHALL route persistently
failed consumer records to a dead-letter topic.

#### Scenario: Consumer cannot process a record
- **WHEN** `behavior-analysis-service` fails to process a record after bounded
  retries
- **THEN** the record SHALL be published to `security.events.raw.dlq`
- **AND** the poison record SHALL NOT block later records indefinitely

## MODIFIED Requirements

### Requirement: Kafka raw event topic is defined
Accepted security events SHALL be published to Kafka topic
`security.events.raw`.

#### Scenario: Event is accepted for downstream processing
- **WHEN** valid event is accepted by ingestion service
- **THEN** Kafka producer SHALL publish normalized event to
  `security.events.raw`
- **AND** event key SHALL preserve a stable partitioning strategy based on
  `subject.id`
- **AND** Kafka payload SHALL include `eventId` and `receivedAt` together with
  the accepted security event envelope

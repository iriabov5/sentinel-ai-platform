## ADDED Requirements

### Requirement: Kafka and MongoDB integration tests use Testcontainers
When a change introduces Kafka or MongoDB runtime dependencies, integration
tests SHALL start those dependencies through Testcontainers rather than a
manually started Docker Compose stack.

#### Scenario: Docker is available
- **WHEN** automated quality checks run and Docker is available
- **THEN** Kafka and MongoDB integration tests SHALL start containers through
  Testcontainers
- **AND** the quality check SHALL NOT require an operator to run
  `docker compose up` first

#### Scenario: Docker is unavailable
- **WHEN** Docker is not available
- **THEN** Testcontainers integration tests SHALL be skipped
- **AND** unit tests and coverage checks SHALL still run

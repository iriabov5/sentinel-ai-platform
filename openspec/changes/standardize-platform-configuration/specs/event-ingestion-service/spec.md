## Purpose

Уточняет runtime configuration expectations для `event-ingestion-service`.

## ADDED Requirements

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

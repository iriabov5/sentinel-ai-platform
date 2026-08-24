## REMOVED Requirements

### Requirement: Service does not derive behavioral features yet
**Reason**: Фазовый scope-маркер Phase 3 — feature derivation вводится этим
change (Phase 4 roadmap).
**Migration**: Поведение фич теперь описывается новым требованием "Service
computes behavioral features on demand" и capability `behavioral-features`;
persistence событий остаётся независимой от фич (см. "Event persistence does
not require features").

## MODIFIED Requirements

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

## ADDED Requirements

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

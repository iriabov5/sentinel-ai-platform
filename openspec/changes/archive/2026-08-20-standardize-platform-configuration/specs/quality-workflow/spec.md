## Purpose

Уточняет, что quality tooling configuration является externalized engineering
configuration, а не runtime configuration микросервисов.

## ADDED Requirements

### Requirement: Quality tooling endpoints are externalized
Quality tooling endpoints SHALL be configurable outside committed build scripts.

#### Scenario: SonarQube host is configured
- **WHEN** SonarQube analysis runs locally or in CI
- **THEN** `sonar.host.url` SHALL be resolved from Gradle property,
  environment variable or safe local fallback
- **AND** committed build scripts SHALL NOT assume `localhost` as the only
  possible SonarQube host

### Requirement: Quality tooling secrets stay outside repository
Quality tooling credentials SHALL NOT be committed.

#### Scenario: SonarQube token is needed
- **WHEN** SonarQube analysis requires authentication
- **THEN** token SHALL be supplied through external property, environment
  variable, local user-level Gradle config or CI secret
- **AND** token SHALL NOT be stored in repository files

### Requirement: Local tooling defaults are marked as local
Local defaults SHALL exist only as developer convenience.

#### Scenario: Local fallback is used
- **WHEN** quality tooling host is not explicitly provided
- **THEN** build MAY fallback to `http://localhost:9000`
- **AND** documentation SHALL describe fallback as local-only convenience

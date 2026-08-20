## Purpose

Уточняет platform-wide configuration conventions для local, test, dev, CI,
Railway и будущего Kubernetes deployment.

## ADDED Requirements

### Requirement: Configuration separates code from deploy-specific values
Platform SHALL keep deploy-specific configuration out of application code and
committed build scripts.

#### Scenario: Config value differs between deploys
- **WHEN** value differs between local, test, dev, staging, production-like or CI
  environments
- **THEN** value SHALL be supplied through environment variables, system
  properties, Gradle properties, platform variables, ConfigMaps, Secrets or
  equivalent external mechanism
- **AND** code SHALL NOT hardcode that value as the only supported option

### Requirement: Repository stores only safe defaults and examples
Repository SHALL store safe defaults and examples, not real secrets or
environment-specific private values.

#### Scenario: Local configuration is documented
- **WHEN** developer needs local configuration examples
- **THEN** repository SHALL provide `.env.example` or equivalent safe example
- **AND** real `.env` files SHALL NOT be committed

### Requirement: Runtime services use typed configuration
Kotlin/Micronaut services SHALL use type-safe configuration classes for runtime
settings that affect service behavior.

#### Scenario: Service runtime setting is introduced
- **WHEN** service adds limits, URLs, ports, feature flags, credentials handles
  or integration settings
- **THEN** service SHALL prefer `@ConfigurationProperties` or approved typed
  configuration binding
- **AND** configuration class SHALL validate required or bounded values where
  practical
- **AND** configuration semantics SHALL be documented with Russian KDoc when not
  obvious

### Requirement: Environment names are conventional but values stay granular
Platform SHALL use conventional environment names without relying on one large
profile as the only source of truth.

#### Scenario: Environment-specific runtime is needed
- **WHEN** runtime needs environment identity
- **THEN** platform MAY use `local`, `test`, `dev`, `staging` or `prod`
  environment names
- **AND** individual values SHALL remain overridable through granular variables
  or platform configuration

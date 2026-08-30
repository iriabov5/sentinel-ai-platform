## Purpose

Описывает проектные conventions, перенятые и адаптированные из
`prompt-injection-firewall`: русский OpenSpec, короткое agent guide, Kotlin /
Micronaut service layout, Coroutines-first style, generated OpenAPI, testing
pyramid, KDoc и security/observability expectations.

## Requirements

### Requirement: Project naming является explicit
Проект SHALL явно различать product name и repository slug.

#### Scenario: Project is referenced
- **WHEN** documentation или specs называют продукт
- **THEN** product name SHALL be `Sentinel AI Platform`
- **AND** current repository/folder slug MAY be `ai-security-platform`

### Requirement: OpenSpec documentation пишется на русском
OpenSpec specs, proposals, designs и tasks SHALL be written primarily in Russian,
while stable technical terms MAY remain in English.

#### Scenario: New OpenSpec artifact is created
- **WHEN** future OpenSpec artifact is created
- **THEN** explanatory text SHALL be written in Russian
- **AND** service names, protocol names, API terms and framework names MAY remain
  in English
- **AND** OpenSpec structural markers such as `Requirement`, `Scenario` and
  `SHALL` SHALL keep validator-compatible format

### Requirement: Repository contains AGENTS guide
Repository SHALL include an `AGENTS.md` guide summarizing project workflow,
quality checks, Git flow, language style and important architecture constraints.

#### Scenario: Agent starts work in repository
- **WHEN** agent opens repository
- **THEN** `AGENTS.md` SHALL explain OpenSpec-first workflow, quality-before-commit,
  protected branches, Russian documentation style and Micronaut without Spring
  Boot dependency drift

### Requirement: Kotlin services follow consistent layout
Each Kotlin/Micronaut service SHALL use a clear internal package layout instead
of mixing controllers, domain logic, configuration and persistence together.

#### Scenario: New Kotlin service is introduced
- **WHEN** a Kotlin/Micronaut service is created
- **THEN** service SHALL organize code into appropriate packages such as
  `controller`, `service`, `model`, `configuration`, `security`, `observability`
  and `persistence` when those concerns exist

### Requirement: Coroutines are default async style
Kotlin services SHALL prefer Kotlin Coroutines and structured concurrency for
new async behavior unless a specific integration requires another model.

#### Scenario: Async processing is implemented
- **WHEN** service implements asynchronous behavior
- **THEN** implementation SHALL prefer `suspend`, `coroutineScope`,
  `supervisorScope`, `async`, `withTimeout` or `Flow` where appropriate
- **AND** Reactor types SHALL NOT be introduced as default API style without
  design justification

### Requirement: Kotlin code follows official idiomatic style
Kotlin services SHALL follow official Kotlin Coding Conventions and write
idiomatic Kotlin rather than Java patterns expressed in Kotlin syntax.

#### Scenario: Production Kotlin sources are reviewed
- **WHEN** Kotlin production sources are inspected
- **THEN** state SHALL be expressed as properties, `data class` and `val`
  rather than Java getters/setters or mutable Java beans
- **AND** collection and string operations SHALL use Kotlin stdlib APIs and
  string templates rather than Java collection types or concatenation
- **AND** HTTP handlers SHALL use `suspend` functions rather than Reactor
  `Mono`/`Flux` return types unless a later approved design justifies Reactor

#### Scenario: Java interop remains at adapter boundaries
- **WHEN** code must call a Java driver, client or test interface that has no
  idiomatic Kotlin replacement
- **THEN** the Java API MAY be used at that adapter or test boundary
- **AND** domain logic SHALL NOT be written in the Java call style of that
  boundary

#### Scenario: New Kotlin file is added
- **WHEN** a new Kotlin source or test file is added
- **THEN** it SHALL NOT introduce Java-style `getX`/`setX` methods, Java
  mutable collections as the default collection type, or Reactor types as
  the HTTP handler return type without design justification

### Requirement: Micronaut beans are opened by compiler plugin
Kotlin/Micronaut services SHALL make beans open for AOP through the all-open
compiler plugin configured for Micronaut annotations, not by marking every
bean class and member `open` by hand.

#### Scenario: Bean class is inspected
- **WHEN** a Micronaut bean class is reviewed
- **THEN** the class SHALL NOT require a manual `open` keyword solely to
  satisfy proxy/AOP requirements
- **AND** build configuration SHALL declare the Micronaut annotations that
  all-open applies to

### Requirement: Kotlin tests use Kotlin test idioms on JUnit
JUnit 5 SHALL remain the test runner, and Kotlin tests SHALL use Kotlin
assertion APIs and coroutine test builders rather than Java assertion
classes and `runBlocking` wrappers where practical.

#### Scenario: Suspend function is unit-tested
- **WHEN** a unit test calls a `suspend` function
- **THEN** the test SHALL use kotlinx.coroutines test builders such as
  `runTest`
- **AND** the test SHALL NOT wrap the call in `runBlocking` unless the
  surrounding framework callback cannot be suspend

#### Scenario: Assertion style is inspected
- **WHEN** a Kotlin unit test asserts equality, truth or an expected
  exception
- **THEN** assertions SHALL use Kotlin test APIs rather than
  `org.junit.jupiter.api.Assertions`

### Requirement: OpenAPI is generated and verified
REST APIs SHALL have generated OpenAPI documentation and tests that verify
important endpoints, schemas, validation and security schemes.

#### Scenario: New public REST API is added
- **WHEN** service adds public REST API
- **THEN** OpenAPI specification SHALL be generated from current code or defined
  by approved contract
- **AND** tests SHALL verify that important paths, schemas, validation errors and
  security requirements are represented

### Requirement: Testing follows pyramid
Project tests SHALL follow testing pyramid: most tests cover pure domain logic,
fewer tests cover Micronaut integration, and a small number of smoke tests cover
main API or service flows.

#### Scenario: New behavior is tested
- **WHEN** implementation adds behavior
- **THEN** unit tests SHALL cover core logic without full framework context when
  possible
- **AND** integration tests SHALL cover Micronaut wiring, validation, security,
  persistence or messaging where relevant
- **AND** smoke tests SHALL verify important end-to-end paths

### Requirement: Tests use Russian DisplayName
JUnit tests in Kotlin services SHALL use Russian `@DisplayName` annotations for
test classes and scenarios.

#### Scenario: New JUnit test is added
- **WHEN** Kotlin test class or test method is added
- **THEN** it SHALL include Russian `@DisplayName`
- **AND** display name SHALL describe verified behavior rather than repeat method
  name mechanically

### Requirement: Meaningful production code has Russian KDoc
Production Kotlin code SHALL include Russian KDoc/Javadoc for public or important
internal declarations whose purpose, constraints, concurrency behavior, HTTP
role, security meaning or configuration semantics are not obvious.

#### Scenario: New meaningful declaration is added
- **WHEN** service adds controller, service, configuration, security component,
  domain model or non-trivial function
- **THEN** Russian KDoc/Javadoc SHALL explain its role when the declaration name
  alone is insufficient
- **AND** obvious comments SHALL be avoided

### Requirement: Security baseline is considered early
Services SHALL consider API protection, explicit public routes, secret handling,
CORS/security headers and sensitive-data exposure early in design.

#### Scenario: New protected endpoint is proposed
- **WHEN** service exposes endpoint with platform data or write behavior
- **THEN** design SHALL specify authentication/authorization expectation
- **AND** public anonymous routes SHALL be explicitly listed

### Requirement: Observability is part of service design
Services SHALL expose health and meaningful runtime metrics when they introduce
runtime behavior.

#### Scenario: New deployable service is introduced
- **WHEN** service becomes deployable
- **THEN** service SHALL define health visibility
- **AND** service SHALL define metrics for important request, event, latency or
  failure outcomes where relevant

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

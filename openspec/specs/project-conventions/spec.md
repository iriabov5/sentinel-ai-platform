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


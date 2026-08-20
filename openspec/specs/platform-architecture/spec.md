## Purpose

Описывает baseline architecture и development constraints для развития Sentinel
AI Platform как deployable, specification-driven cybersecurity system.

## Requirements

### Requirement: OpenSpec управляет значимыми platform changes
Проект SHALL использовать OpenSpec planning artifacts до реализации значимых
изменений behavior, architecture, API, event-contract, persistence, security или
deployment.

#### Scenario: Запрошено новое platform behavior
- **WHEN** запрос добавляет или меняет platform behavior
- **THEN** работа фиксируется в OpenSpec change до implementation

#### Scenario: Запрошен trivial maintenance
- **WHEN** запрос меняет только documentation, formatting или local metadata
- **THEN** работа может выполняться без behavior spec, если externally observable
  behavior не меняется

### Requirement: Платформа организована как monorepo
Проект SHALL хранить backend services, AI service, frontend application,
contract specifications, documentation и infrastructure assets в одном
repository, сохраняя independently buildable deployable units.

#### Scenario: Добавляется новый deployable service
- **WHEN** change добавляет новый deployable service
- **THEN** у service есть явный repository path, runtime configuration,
  health-check expectation, ownership boundary и independent build path

### Requirement: Service boundaries обоснованы
Проект SHALL добавлять или разделять services только тогда, когда boundary
обоснован data ownership, scaling behavior, failure isolation, security
boundary, communication pattern или independent lifecycle.

#### Scenario: Предложен candidate microservice
- **WHEN** proposal добавляет candidate microservice
- **THEN** design объясняет, почему он должен быть independent service, а не
  частью existing service

### Requirement: Первый MVP идет через vertical event-processing slice
Первый business implementation path SHALL фокусироваться на приеме security
event, asynchronous publish, consuming для behavior analysis и сохранении event
history до добавления incidents, frontend, authentication или advanced AI
behavior.

#### Scenario: Выбран initial implementation scope
- **WHEN** проект начинает business implementation
- **THEN** выбранный scope строится вокруг
  `event-ingestion-service -> Kafka -> behavior-analysis-service -> MongoDB`

### Requirement: APIs и events являются contract-first
Платформа SHALL определять REST API contracts и Kafka event contracts до
implementation producers, consumers или clients, которые зависят от этих
contracts.

#### Scenario: Добавляется REST endpoint
- **WHEN** change добавляет REST endpoint
- **THEN** OpenAPI contract определяется или обновляется до implementation

#### Scenario: Добавляется Kafka topic
- **WHEN** change добавляет Kafka topic или event payload
- **THEN** AsyncAPI contract и explicit schema strategy определяются или
  обновляются до implementation

### Requirement: Data ownership является explicit
Платформа SHALL назначать каждый persistent data set одному owning capability
или service и обосновывать выбранную storage technology.

#### Scenario: Предложен MongoDB storage
- **WHEN** change предлагает MongoDB storage
- **THEN** design объясняет document boundaries, read/write patterns, indexing
  expectations, retention expectations и почему document storage подходит

#### Scenario: Предложен PostgreSQL storage
- **WHEN** change предлагает PostgreSQL storage
- **THEN** design объясняет relational entities, consistency needs, migrations,
  indexes и transaction expectations

#### Scenario: Предложено Redis usage
- **WHEN** change предлагает Redis usage
- **THEN** design указывает short-lived state, cache, rate-limiting,
  deduplication, counter или coordination use case, который требует Redis

### Requirement: Deployment остается portable
Платформа SHALL запускаться локально через Docker Compose, деплоиться в Railway
из monorepo и оставаться portable to Kubernetes без привязки application code к
Railway-specific behavior.

#### Scenario: Добавляется deployable service
- **WHEN** change добавляет deployable service
- **THEN** service настраивается через environment variables и не требует
  Railway-specific APIs в application code

#### Scenario: Добавляется local development support
- **WHEN** change добавляет local runtime infrastructure
- **THEN** Docker Compose рассматривается как default local orchestration target

### Requirement: Future technology adoption идет incremental
Платформа SHALL добавлять Kafka, MongoDB, PostgreSQL, Redis, Python AI/ML, React
frontend, observability и Kubernetes assets только тогда, когда approved
OpenSpec change требует их для concrete capability.

#### Scenario: Future platform technology запланирована, но еще не нужна
- **WHEN** technology указана в long-term architecture, но ни один approved
  change пока не требует ее
- **THEN** проект не добавляет placeholder implementation для этой technology


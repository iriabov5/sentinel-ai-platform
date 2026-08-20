## Purpose

Описывает целевой technology stack Sentinel AI Platform: JVM backend, AI/ML
Python service, frontend, messaging, persistence, observability, infrastructure,
quality tooling и принципы async/non-blocking/high-load architecture.

## Requirements

### Requirement: Platform использует polyglot architecture
Платформа SHALL использовать разные технологии для разных частей системы, когда
это обосновано их ролью.

#### Scenario: Stack описывается целиком
- **WHEN** project documentation описывает technology stack
- **THEN** Kotlin/Micronaut SHALL be primary JVM backend stack
- **AND** Python/FastAPI SHALL be AI/ML service stack
- **AND** React/TypeScript SHALL be frontend stack
- **AND** Go MAY be introduced later for lightweight security agent or event
  collector components
- **AND** Kafka SHALL be messaging backbone

### Requirement: JVM backend stack основан на Kotlin и Micronaut
Backend services SHALL использовать Kotlin, JDK 21, Micronaut 4.x и Gradle
Kotlin DSL для JVM services.

#### Scenario: Kotlin service создается
- **WHEN** new JVM backend service is introduced
- **THEN** service SHALL use Kotlin, Micronaut and Gradle Kotlin DSL
- **AND** service SHALL NOT introduce Spring Boot dependencies without explicit
  design justification

### Requirement: Kotlin services используют Coroutines-first async style
Kotlin services SHALL prefer Kotlin Coroutines and structured concurrency for
asynchronous and non-blocking behavior.

#### Scenario: Async JVM code is implemented
- **WHEN** backend service implements async workflow
- **THEN** implementation SHALL prefer `suspend`, `coroutineScope`,
  `supervisorScope`, `async`, `withTimeout` or `Flow`
- **AND** blocking operations SHALL be isolated and justified

### Requirement: Micronaut capabilities используются осознанно
Kotlin services SHALL use Micronaut capabilities intentionally for HTTP,
configuration, validation, serialization, security, management and integrations.

#### Scenario: Service exposes HTTP API
- **WHEN** service exposes REST API
- **THEN** service SHALL use Micronaut HTTP controllers, validation and
  generated or approved OpenAPI documentation

#### Scenario: Service needs runtime visibility
- **WHEN** service becomes deployable
- **THEN** service SHALL expose health and metrics through Micronaut Management
  or approved equivalent

### Requirement: Kafka communication является asynchronous и contract-first
Kafka SHALL be used for asynchronous service communication where event-driven
flow, buffering, replay, ordering or decoupling is required.

#### Scenario: New Kafka event is introduced
- **WHEN** service introduces Kafka topic or event payload
- **THEN** AsyncAPI and schema strategy SHALL be defined before implementation
- **AND** producer, consumers, partition key, retry and dead-letter expectations
  SHALL be specified

### Requirement: AI/ML stack основан на Python
`ai-detection-service` SHALL use Python and FastAPI for model-serving and anomaly
scoring, with ML libraries introduced incrementally.

#### Scenario: AI detection capability is introduced
- **WHEN** AI detection implementation starts
- **THEN** service SHALL use Python, FastAPI and Pydantic-style request/response
  validation
- **AND** PyTorch, scikit-learn, NumPy or pandas SHALL be introduced only when
  required by approved model or feature-engineering design

### Requirement: Frontend stack основан на React и TypeScript
`security-dashboard` SHALL use React and TypeScript for analyst UI.

#### Scenario: Frontend application is introduced
- **WHEN** frontend implementation starts
- **THEN** dashboard SHALL use React, TypeScript and Vite or an approved modern
  frontend build tool
- **AND** data fetching and routing SHALL be defined before broad UI work starts

### Requirement: Go stack reserved for future agent components
Go SHALL be considered a later-stage technology for lightweight edge components,
not a replacement for the initial Kotlin/Micronaut platform services.

#### Scenario: Security agent is introduced
- **WHEN** platform needs a lightweight binary near applications, servers or
  infrastructure sources
- **THEN** Go MAY be used for `security-agent` or `event-collector`
- **AND** the agent SHALL collect local security events and send them to
  `event-ingestion-service`
- **AND** the agent SHALL be introduced only through an approved future
  OpenSpec change

#### Scenario: Core MVP services are implemented
- **WHEN** first MVP backend services are implemented
- **THEN** `event-ingestion-service` and `behavior-analysis-service` SHALL remain
  Kotlin/Micronaut unless an approved architecture change justifies otherwise

### Requirement: Persistence stack имеет clear ownership
MongoDB, PostgreSQL and Redis SHALL be used according to data ownership and
access patterns rather than added because they are part of the planned stack.

#### Scenario: MongoDB is used
- **WHEN** service stores high-volume heterogeneous behavior/security events
- **THEN** MongoDB MAY be used with explicit document boundaries, indexes and
  retention expectations

#### Scenario: PostgreSQL is used
- **WHEN** service stores transactional domain state
- **THEN** PostgreSQL MAY be used with migrations, constraints and indexes

#### Scenario: Redis is used
- **WHEN** service needs short-lived state, cache, rate limiting,
  deduplication, counters or coordination
- **THEN** Redis MAY be introduced with explicit expiration and ownership rules

### Requirement: Observability stack является обязательным для deployable services
Deployable services SHALL be observable through logs, metrics, health checks and
eventually distributed tracing.

#### Scenario: Service becomes deployable
- **WHEN** service is added as deployable unit
- **THEN** service SHALL define health checks, structured logging expectations
  and metrics for important success/failure/latency outcomes
- **AND** OpenTelemetry, Prometheus and Grafana integration SHALL be introduced
  through approved observability changes

### Requirement: Infrastructure stack остается portable
Infrastructure SHALL support local Docker Compose, Railway as first cloud/demo
deployment target and Kubernetes later without coupling application code to one
provider.

#### Scenario: Runtime dependency is added
- **WHEN** service requires database, broker or external runtime
- **THEN** local Docker Compose support SHALL be considered
- **AND** Railway compatibility SHALL be considered
- **AND** Kubernetes portability SHALL NOT be blocked by provider-specific code

### Requirement: Quality tooling проверяет code before commit
Project SHALL use appropriate testing and quality tools for each technology
area.

#### Scenario: Kotlin code is committed
- **WHEN** commit includes Kotlin code
- **THEN** JUnit 5, MockK, Testcontainers where relevant, JaCoCo and SonarQube
  SHALL be used according to quality workflow

#### Scenario: Python code is committed
- **WHEN** commit includes Python service code
- **THEN** Python tests, linting/type checks and SonarQube-compatible analysis
  SHALL be defined before production behavior is accepted

#### Scenario: Frontend code is committed
- **WHEN** commit includes frontend code
- **THEN** TypeScript checks, unit/component tests and build verification SHALL
  be defined before production UI behavior is accepted

### Requirement: High-load principles guide implementation
Services SHALL be designed for asynchronous, horizontally scalable and
observable operation without adding accidental shared-state bottlenecks.

#### Scenario: Service handles high event volume
- **WHEN** service processes many events or requests
- **THEN** design SHALL address non-blocking IO where practical, backpressure at
  event boundaries, timeouts, retries, idempotency and failure isolation
- **AND** service SHALL remain stateless where practical so it can scale
  horizontally

### Requirement: Resilience is explicit
Service interactions SHALL define timeout, retry, idempotency and fallback
expectations when failure can affect correctness or user-visible behavior.

#### Scenario: Service calls another service
- **WHEN** synchronous service-to-service call is introduced
- **THEN** design SHALL specify timeout and failure behavior
- **AND** retry SHALL be used only when operation is idempotent or safely
  deduplicated

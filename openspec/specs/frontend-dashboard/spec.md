## Purpose

Описывает frontend direction для `security-dashboard`: analyst-oriented React /
TypeScript UI, основные views, role-aware behavior, API-driven architecture,
работу с mock data на ранних этапах и constraints для production-like dashboard.

## Requirements

### Requirement: Dashboard является основным frontend, а не landing page
`security-dashboard` SHALL открываться как рабочий analyst dashboard, а не как
marketing landing page.

#### Scenario: User opens frontend
- **WHEN** user opens `security-dashboard`
- **THEN** first meaningful screen SHALL focus on incidents, anomalies or
  investigation workflow
- **AND** UI SHALL NOT start with a marketing hero page

### Requirement: Frontend stack uses React and TypeScript
Dashboard SHALL use React and TypeScript with a modern build setup.

#### Scenario: Frontend implementation starts
- **WHEN** frontend project is created
- **THEN** it SHALL use React and TypeScript
- **AND** it SHALL use Vite or another approved modern frontend build tool

### Requirement: Dashboard communicates through APIs only
Dashboard SHALL access platform data through protected APIs and SHALL NOT read
databases, Kafka or service internals directly.

#### Scenario: Dashboard loads incidents
- **WHEN** dashboard needs incident data
- **THEN** dashboard SHALL call protected REST API exposed by backend capability
- **AND** dashboard SHALL NOT connect directly to PostgreSQL, MongoDB, Redis or
  Kafka

### Requirement: Incident feed is a primary view
Dashboard SHALL provide an incident feed optimized for triage.

#### Scenario: Analyst reviews incidents
- **WHEN** analyst opens incident feed
- **THEN** UI SHALL show incidents with severity, status, risk score, affected
  user/service, created time and short reason summary
- **AND** UI SHALL support filtering by severity, status, time range, user/service
  and source

### Requirement: Incident details support investigation
Dashboard SHALL provide incident details with context required for investigation.

#### Scenario: Analyst opens incident
- **WHEN** analyst opens incident details
- **THEN** UI SHALL show status, severity, risk score, reasons, affected entity,
  timeline, related events and available investigation actions

### Requirement: Risk explanation is visible
Dashboard SHALL explain why an anomaly or incident received its score.

#### Scenario: Analyst inspects risk score
- **WHEN** analyst views anomaly or incident risk score
- **THEN** UI SHALL show contributing factors such as unknown device, unusual
  login time, new IP network, abnormal request rate or massive data download
- **AND** UI SHALL NOT show only a bare numeric score

### Requirement: Timeline view shows behavior sequence
Dashboard SHALL provide timeline-oriented investigation for suspicious behavior.

#### Scenario: Analyst investigates suspicious session
- **WHEN** analyst opens timeline view
- **THEN** UI SHALL show ordered events such as login attempts, API requests,
  downloads, permission changes and data exports
- **AND** UI SHALL highlight events contributing to anomaly score

### Requirement: Entity behavior profile is inspectable
Dashboard SHALL support user/service behavior profile views when backend APIs
provide the required data.

#### Scenario: Analyst opens user profile
- **WHEN** analyst views a user or service account
- **THEN** UI SHALL show baseline behavior, recent anomalies, known devices,
  common activity windows and notable deviations when available

### Requirement: Role-aware UI is required
Dashboard SHALL adapt visible actions and views based on user role.

#### Scenario: User has analyst role
- **WHEN** user role is `SECURITY_ANALYST`
- **THEN** UI SHALL allow incident investigation actions appropriate for analyst

#### Scenario: User has auditor role
- **WHEN** user role is `AUDITOR`
- **THEN** UI SHALL provide read-oriented investigation access and SHALL NOT show
  admin-only mutation actions

### Requirement: Authentication flow is part of dashboard
Dashboard SHALL integrate with identity/access capability for login, session and
protected API access.

#### Scenario: Unauthenticated user opens dashboard
- **WHEN** unauthenticated user opens protected dashboard route
- **THEN** UI SHALL redirect or show login flow
- **AND** protected API calls SHALL NOT be attempted without valid auth context

### Requirement: Mock data may be used only as an early development boundary
Dashboard MAY use mock data before backend APIs exist, but mock behavior SHALL be
clearly separated from production API integration.

#### Scenario: Frontend is developed before backend API exists
- **WHEN** backend endpoint is not implemented yet
- **THEN** dashboard MAY use mock data behind explicit development-only boundary
- **AND** future change SHALL replace mock data with real API integration before
  feature is considered production-ready

### Requirement: UI style is operational and dense
Dashboard SHALL prioritize operational clarity, scanning, filtering and repeated
analyst workflows over decorative or marketing-style UI.

#### Scenario: Dashboard layout is designed
- **WHEN** frontend view is created
- **THEN** UI SHALL use dense but organized information layout
- **AND** UI SHALL avoid oversized marketing composition that slows analyst
  workflows

### Requirement: Frontend has quality gates
Frontend code SHALL have type checks, build checks and tests appropriate to its
stage before commit.

#### Scenario: Frontend code changes
- **WHEN** commit includes frontend code
- **THEN** TypeScript check and frontend build SHALL pass
- **AND** unit or component tests SHALL be added for meaningful logic and views
  when testing infrastructure exists


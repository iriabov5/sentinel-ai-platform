## Purpose

Описывает продуктовую концепцию Sentinel AI Platform: какую проблему решает
платформа, от каких сценариев защищает, какие security events принимает и как
аномальное поведение превращается в расследуемый incident.

## Requirements

### Requirement: Платформа является AI-powered SIEM/UEBA system
Sentinel AI Platform SHALL собирать security events, строить behavioral context,
выявлять anomalies и помогать security analyst расследовать incidents.

#### Scenario: Описывается категория продукта
- **WHEN** проект описывает назначение платформы
- **THEN** платформа SHALL позиционироваться как AI-powered SIEM/UEBA system
- **AND** платформа SHALL NOT описываться как классический antivirus или network
  firewall

### Requirement: Платформа защищает от suspicious behavior
Платформа SHALL выявлять suspicious behavior пользователей, service accounts,
applications и infrastructure components на основе событий и behavioral history.

#### Scenario: Пользователь ведет себя необычно
- **GIVEN** пользователь обычно логинится днем, с известного устройства и
  выполняет небольшой объем запросов
- **WHEN** появляются login ночью, unknown device, unusual IP, всплеск API
  requests или massive data download
- **THEN** платформа SHALL рассматривать это как anomaly candidate

#### Scenario: Service account ведет себя необычно
- **GIVEN** service account обычно вызывает ограниченный набор APIs
- **WHEN** service account начинает вызывать unusual endpoints, создавать tokens
  или экспортировать необычно большой объем data
- **THEN** платформа SHALL рассматривать это как anomaly candidate

### Requirement: Security event types являются явными
Платформа SHALL принимать и развивать explicit event types для security-relevant
activity.

#### Scenario: Security event принимается
- **WHEN** external application или agent отправляет event
- **THEN** event SHALL иметь explicit event type, например `LOGIN_SUCCESS`,
  `LOGIN_FAILED`, `API_REQUEST`, `FILE_DOWNLOAD`, `PERMISSION_CHANGE`,
  `DEVICE_LOGIN`, `TOKEN_CREATED`, `PRIVILEGE_ESCALATION`, `DATA_EXPORT` или
  `ADMIN_ACTION`

### Requirement: Behavioral features объясняют anomaly
Платформа SHALL преобразовывать raw events в explainable behavioral features,
которые можно показать analyst.

#### Scenario: Behavior analysis готовит features
- **WHEN** behavior-analysis-service анализирует поток событий
- **THEN** он SHALL формировать features вроде `newIp`, `unusualTime`,
  `requestsPerMinute`, `downloadMb`, `newDevice` или других explainable factors

### Requirement: AI detection возвращает score и reasons
AI detection SHALL возвращать anomaly score и объяснимые reasons, а не только
черный ящик с числом.

#### Scenario: AI detection выявляет высокий риск
- **GIVEN** features указывают на unknown device, unusual login time и massive
  data download
- **WHEN** ai-detection-service оценивает features
- **THEN** response SHALL включать `anomalyScore`
- **AND** response SHALL включать explainable reasons или contributing factors

### Requirement: High-risk anomaly превращается в incident
Платформа SHALL создавать incident, когда anomaly score и policy rules
достаточно серьезны для расследования.

#### Scenario: Anomaly превышает threshold
- **WHEN** anomaly score превышает configured threshold
- **THEN** incident-service SHALL создать incident
- **AND** incident SHALL иметь severity, risk score, reasons и status

### Requirement: Incident lifecycle является расследуемым
Incident SHALL иметь lifecycle, пригодный для работы security analyst.

#### Scenario: Analyst расследует incident
- **WHEN** incident создан
- **THEN** его status SHALL проходить через controlled lifecycle, например
  `OPEN -> INVESTIGATING -> RESOLVED`
- **AND** analyst SHALL видеть user/service context, timeline, risk score и
  explainable reasons

### Requirement: Sensitive data обрабатывается осторожно
Платформа SHALL учитывать, что security events могут содержать secrets, PII или
другие sensitive fields.

#### Scenario: Event содержит sensitive fields
- **WHEN** event содержит token, password, secret, PII или confidential payload
- **THEN** storage, audit и dashboard views SHALL применять masking, hashing,
  retention rules или другие privacy controls, определенные future specs


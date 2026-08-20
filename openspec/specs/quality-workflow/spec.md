## Purpose

Определяет quality workflow проекта: какие проверки должны проходить изменения
перед commit, как используется local SonarQube и что делать, если SonarQube
временно недоступен.

## Requirements

### Requirement: Проверки выполняются перед commit
Проект SHALL требовать воспроизводимый quality workflow перед commit каждой
завершенной OpenSpec task или логического implementation step.

#### Scenario: Задача готовится к commit
- **WHEN** завершена OpenSpec task или логический implementation step
- **THEN** перед commit выполняются OpenSpec validation, tests, coverage check и
  SonarQube analysis
- **AND** commit создается только после успешных обязательных проверок или после
  явного документирования недоступной внешней проверки

### Requirement: OpenSpec validation является обязательной
Проект SHALL проверять OpenSpec artifacts перед commit, если change добавляет или
меняет specs, proposal, design или tasks.

#### Scenario: Изменены OpenSpec artifacts
- **WHEN** commit включает изменения в `openspec/`
- **THEN** OpenSpec validation SHALL быть выполнена до commit
- **AND** validation issues SHALL быть исправлены до commit

### Requirement: Tests и coverage являются обязательными для code changes
Проект SHALL запускать релевантные tests и coverage check перед commit, если
change добавляет или меняет application code.

#### Scenario: Изменен application code
- **WHEN** commit включает application code
- **THEN** релевантные tests SHALL быть выполнены до commit
- **AND** coverage check SHALL быть выполнен до commit
- **AND** failing tests или coverage violations SHALL быть исправлены до commit

### Requirement: SonarQube используется как local quality gate
Проект SHALL использовать local SonarQube для static analysis, security review и
quality issues перед commit, когда SonarQube runtime и token доступны.

#### Scenario: SonarQube доступен
- **GIVEN** local SonarQube запущен
- **AND** SonarQube token доступен вне repository
- **WHEN** change готовится к commit
- **THEN** SonarQube analysis SHALL быть выполнен до commit
- **AND** issues типа bugs, vulnerabilities, security hotspots и code smells
  SHALL быть исправлены или явно помечены как intentional false positive с
  причиной

### Requirement: SonarQube не является runtime dependency
Проект SHALL запускать SonarQube как local engineering tooling, не влияющий на
application runtime и deployment.

#### Scenario: Developer запускает local quality server
- **WHEN** запускается SonarQube для анализа качества
- **THEN** SonarQube SHALL запускаться отдельно от application runtime
- **AND** application configuration SHALL NOT требовать изменений из-за
  SonarQube

### Requirement: Secrets и служебные отчеты не коммитятся
Проект SHALL исключать SonarQube tokens, scanner work directories и generated
coverage reports из version control.

#### Scenario: SonarQube analysis выполнен
- **WHEN** SonarQube analysis или coverage check создает local artifacts
- **THEN** token files, scanner work directories и generated coverage reports
  SHALL NOT попадать в commit

### Requirement: Недоступный SonarQube явно фиксируется
Если local SonarQube или token недоступны, проект SHALL все равно требовать
локальные проверки, а отсутствие SonarQube analysis SHALL быть явно сообщено.

#### Scenario: SonarQube или token недоступны
- **GIVEN** local SonarQube не запущен или token отсутствует
- **WHEN** пользователь просит commit
- **THEN** OpenSpec validation и релевантные tests/coverage SHALL быть выполнены
  там, где они применимы
- **AND** агент SHALL явно сообщить, что SonarQube analysis не запускался
- **AND** агент SHALL NOT молча пропустить SonarQube analysis


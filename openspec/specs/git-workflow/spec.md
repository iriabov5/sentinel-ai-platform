## Purpose

Определяет Git flow проекта: назначение веток, правила commit/push/PR и границы
ответственности агента и пользователя при продвижении изменений.

## Requirements

### Requirement: Ветки имеют фиксированное назначение
Repository SHALL разделять production, integration и краткоживущую работу по
веткам.

#### Scenario: Описывается branch model
- **WHEN** описывается Git flow проекта
- **THEN** `main` SHALL считаться production branch
- **AND** `master` SHALL трактоваться как protected production branch, если она
  существует
- **AND** `dev` SHALL считаться integration branch
- **AND** новая функциональность SHALL выполняться в `feature/<english-kebab-name>`
- **AND** исправления SHALL выполняться в `bugfix/<english-kebab-name>`

### Requirement: Agent не коммитит в protected branches
Agent SHALL NOT создавать commits напрямую в `main`, `master` или `dev`.

#### Scenario: Agent находится в protected branch
- **GIVEN** текущая branch — `main`, `master` или `dev`
- **WHEN** пользователь просит commit
- **THEN** agent SHALL сначала создать или перейти в подходящую
  `feature/<english-kebab-name>` или `bugfix/<english-kebab-name>` branch
- **AND** agent SHALL NOT выполнять commit в `main`, `master` или `dev`

### Requirement: Feature work идет через feature branch
Новая функциональность SHALL коммититься в branch с префиксом `feature/` и
английским kebab-case именем.

#### Scenario: Пользователь просит commit новой функциональности
- **WHEN** change добавляет новую functionality
- **THEN** commit SHALL быть создан в `feature/<english-kebab-name>`
- **AND** branch name SHALL быть на английском, в kebab-case и отражать суть
  change

### Requirement: Bugfix work идет через bugfix branch
Исправление дефекта SHALL коммититься в branch с префиксом `bugfix/` и
английским kebab-case именем.

#### Scenario: Пользователь просит commit исправления
- **WHEN** change исправляет defect
- **THEN** commit SHALL быть создан в `bugfix/<english-kebab-name>`
- **AND** branch name SHALL быть на английском, в kebab-case и отражать суть
  fix

### Requirement: Commit request включает push и PR
Просьба пользователя commit SHALL означать полный delivery loop: commit в
рабочую branch, push этой branch и pull request в `dev`, если remote repository
и GitHub access доступны.

#### Scenario: Пользователь просит commit
- **WHEN** пользователь просит commit
- **THEN** agent SHALL создать commit в `feature/` или `bugfix/` branch
- **AND** agent SHALL push эту branch в `origin`, если remote доступен
- **AND** agent SHALL открыть pull request в `dev`, если GitHub access доступен
- **AND** agent SHALL остановиться после PR и дождаться review пользователя
- **AND** agent SHALL NOT выполнять merge в `dev`, `main` или `master`

### Requirement: Existing PR обновляется, а не дублируется
Если для текущей рабочей branch уже есть open PR, agent SHALL обновлять этот PR
новыми commits вместо создания duplicate PR.

#### Scenario: PR уже существует
- **GIVEN** для текущей `feature/` или `bugfix/` branch уже открыт PR в `dev`
- **WHEN** пользователь просит commit связанных изменений
- **THEN** agent SHALL добавить commit в текущую branch
- **AND** agent SHALL push обновление существующего PR
- **AND** agent SHALL NOT создавать duplicate PR

### Requirement: Review и merge выполняет пользователь
User SHALL выполнять review и merge changes в protected или integration
branches.

#### Scenario: PR создан
- **WHEN** agent создал pull request в `dev`
- **THEN** user SHALL выполнить review
- **AND** agent SHALL NOT считать change принятым до user merge
- **AND** agent SHALL NOT продвигать changes из `dev` в `main` или `master`


## ADDED Requirements

### Requirement: Kotlin style tooling is part of quality checks
When a change modifies Kotlin sources, the quality workflow SHALL run an
official-style Kotlin formatter/linter and Kotlin static analysis before
commit.

#### Scenario: Kotlin sources changed
- **WHEN** commit includes Kotlin source or test files
- **THEN** ktlint or an equivalent official Kotlin style formatter SHALL pass
- **AND** detekt or an equivalent Kotlin static analysis SHALL pass
- **AND** style or analysis violations SHALL be fixed before commit

#### Scenario: Commit has no Kotlin sources
- **WHEN** commit contains only OpenSpec, contracts or documentation
- **THEN** Kotlin style tooling MAY be skipped

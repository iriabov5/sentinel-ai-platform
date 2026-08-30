## ADDED Requirements

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

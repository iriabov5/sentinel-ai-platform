## MODIFIED Requirements

### Requirement: Service follows coroutines-first style
`event-ingestion-service` SHALL use coroutine-friendly APIs for request handling
where practical.

#### Scenario: Controller implementation is inspected
- **WHEN** ingestion endpoint is implemented
- **THEN** the HTTP handler SHALL be a `suspend` function
- **AND** the HTTP handler SHALL NOT return Reactor `Mono` or `Flux` as the
  response type
- **AND** implementation SHALL avoid blocking IO in the request path

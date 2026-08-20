# Sentinel AI Platform

AI-powered платформа для cybersecurity behavior analytics и anomaly detection.

Sentinel AI Platform — маленькая production-oriented AI-powered SIEM/UEBA
platform. Она собирает security events из приложений и агентов, строит
behavioral context, ищет anomalies, создает incidents и помогает security
analyst расследовать подозрительное поведение.

Текущая папка/repository slug: `ai-security-platform`. Product name:
`Sentinel AI Platform`.

Разработка идет через OpenSpec: значимые изменения поведения и архитектуры
сначала описываются в спецификации, и только потом реализуются.

## Планируемый Stack

- Kotlin, JDK 21, Micronaut 4.x, Gradle Kotlin DSL
- Kotlin Coroutines, `suspend`, `Flow`, structured concurrency
- Kafka, AsyncAPI, schema strategy to be decided
- MongoDB
- PostgreSQL
- Redis when justified
- Python, FastAPI, Pydantic, PyTorch, scikit-learn
- React, TypeScript, Vite
- Go later for `security-agent` / `event-collector`
- Docker Compose для local development
- Railway как первый cloud/demo deployment target
- Kubernetes позже
- OpenTelemetry, Prometheus, Grafana
- JUnit 5, MockK, Testcontainers, JaCoCo, SonarQube

Архитектурные принципы:

```text
event-driven
async
non-blocking where practical
horizontally scalable
observable
contract-first
resilient with timeouts, retries and idempotency
```

## Target Services

Backend:

- `event-ingestion-service` — принимает security events, валидирует и публикует
  их в Kafka.
- `behavior-analysis-service` — читает events из Kafka, хранит behavioral
  history в MongoDB и готовит features.
- `ai-detection-service` — Python/FastAPI service для anomaly score и
  explainable reasons.
- `incident-service` — создает и ведет incidents в PostgreSQL.
- `notification-service` — отправляет alerts по critical incidents.
- `identity-access-service` — users, roles, access policies для dashboard и APIs.

Frontend:

- `security-dashboard` — React/TypeScript UI для analyst investigation.

Future edge component:

- `security-agent` / `event-collector` — later Go component для lightweight
  collection рядом с applications, servers или infrastructure sources. Он будет
  отправлять events в `event-ingestion-service`, но не входит в первый MVP.

## Product Flow

```text
Applications / Agents
  -> optional future Go security-agent
  -> event-ingestion-service
  -> Kafka
  -> behavior-analysis-service
  -> MongoDB + features
  -> ai-detection-service
  -> anomaly score
  -> incident-service
  -> PostgreSQL
  -> notification-service
  -> security-dashboard
```

## Development Workflow

Для нетривиальных изменений используем OpenSpec:

```text
explore -> propose -> review -> apply -> verify -> sync/archive
```

Первая реализация должна быть маленьким vertical slice, а не полным каркасом
всей платформы. Хороший первый slice:

```text
security event -> event-ingestion-service -> Kafka -> behavior-analysis-service -> MongoDB
```

## Deployment Direction

Платформа должна оставаться cloud-provider agnostic:

```text
local development: Docker Compose
demo/cloud: Railway
later infrastructure learning: Kubernetes
```

Сервисы должны настраиваться через environment variables и оставаться
независимо собираемыми и деплоимыми из этого monorepo.

## License

MIT. See [LICENSE](LICENSE).

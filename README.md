# FAANG Event Ledger Platform (B)

A microservices-based event ledger platform built with Spring Boot 3.3.0 and Java 21, designed for high-availability account management and event processing. The platform demonstrates enterprise-grade architecture patterns including multi-module Maven projects, resiliency, distributed tracing, and zero-trust security.

---

## 🏗️ Requirements Fulfillment Summary (Quality Check)

| Requirement | Implementation Detail | Status |
| :--- | :--- | :---: |
| **Idempotency** | EventId existence check in DB before processing; returns `DUPLICATE_EVENT`. | ✅ |
| **Out-of-Order Handling**| Data stored with timestamps; retrieved with `OrderByEventTimestampAsc`. | ✅ |
| **Balance Computation** | Net balance = sum(CREDIT) - sum(DEBIT) logic in `AccountService`. | ✅ |
| **Validation** | Rejection of negative amounts, missing IDs, or unknown types. | ✅ |
| **Service Separation** | Independent Maven modules with isolated H2 databases. | ✅ |
| **Sync REST Call** | Gateway calls Account Service via `RestTemplate`. | ✅ |
| **Trace ID Generation** | UUID generated at Gateway entry point (`LoggingFilter`). | ✅ |
| **Trace Propagation** | Header `X-Trace-Id` used for Gateway -> Account Service calls. | ✅ |
| **Structured Logging** | MDC used to output JSON logs with `traceId` and `serviceName`. | ✅ |
| **Health Checks** | `/health` endpoints implemented on both services. | ✅ |
| **Circuit Breaker** | Resilience4j integrated with fallback for Account Service failures. | ✅ |
| **Graceful Degradation**| Gateway GETs remain functional even if Account Service is down. | ✅ |
| **Docker Compose** | Multi-container setup provided in `infrastructure/docker/`. | ✅ |
| **Automated Tests** | Unit & MVC tests for logic, resiliency, and trace propagation. | ✅ |
| **CI/CD Pipelines** | Multi-stage GitHub Actions & Jenkinsfiles for QA/UAT/Prod. | ✅ |
| **Security (POC)** | Basic Auth for Gateway; Service-Token for internal communication. | ✅ |

---

## 💡 How We Solved the Problem

### 1. Handling Idempotency & Out-of-Order Events
Upstream systems may deliver the same event multiple times or out of order. We solved this by:
- **Unique Event Tracking:** Each event is persisted in the Gateway's local store. Before processing, we check for the `eventId`. If it exists, we immediately return `DUPLICATE_EVENT`, ensuring no duplicate balance changes occur.
- **Chronological Retrieval:** While arrival may be chaotic, our data access layer uses `OrderByEventTimestampAsc`. This ensures that any listing or balance-affecting query respects the actual event time, not the ingestion time.

### 2. Ensuring Resiliency & Availability
Distributed systems are prone to partial failures. Our solution incorporates:
- **Circuit Breaker Pattern:** We protect the Gateway from being slowed down by a failing Account Service using **Resilience4j**. If the Account Service fails repeatedly, the circuit opens, and the Gateway provides a fast-failure response.
- **Graceful Degradation:** The Gateway is designed to work autonomously for read operations. If the Account Service is down, users can still query event history because it's stored locally in the Gateway.

### 3. Distributed Observability
Debugging cross-service issues is simplified through:
- **Unified Tracing:** A `traceId` is generated at the Gateway and propagated to the Account Service via HTTP headers. This `traceId` is injected into every log message using **SLF4J MDC**, allowing us to correlate a single client request across multiple service logs in a JSON format.

### 4. Zero-Trust Security (POC)
We ensured security at every layer:
- **Edge Security:** Basic Authentication (`ledger:ledger`) protects the public Gateway (Industry-standard JWT Bearer tokens are recommended for production).
- **Internal Security:** The Account Service is protected by an **Internal Service Token**. Only the Gateway, possessing this token, can trigger account balance changes, preventing unauthorized lateral movement or direct API manipulation.

---

## ⚙️ Service Functionality

### 1. Gateway Service (`gateway-service`)
**Role:** Public entry point and event validator.
- **Authentication:** Enforces Basic Auth for all incoming client requests (JWT Bearer tokens recommended for production).
- **Ingestion:** Receives financial transaction events via REST.
- **Validation:** Performs schema validation (e.g., positive amounts, valid event types).
- **Idempotency:** Checks local event store to prevent processing the same `eventId` twice.
- **Tracing:** Generates a unique `traceId` for each request and logs it in structured JSON.
- **Resiliency:** Manages calls to the Account Service using a **Circuit Breaker**. If the Account Service is down, it executes a fallback logic to ensure the Gateway remains responsive.

### 2. Account Service (`account-service`)
**Role:** Internal ledger and balance management.
- **Security:** Validates an internal service token to ensure only authorized services (like the Gateway) can modify balances.
- **Balance Management:** Maintains the source of truth for account balances.
- **Transaction Processing:** Applies credits and debits based on commands from the Gateway.
- **Trace Propagation:** Extracts the `traceId` from incoming headers and includes it in all logs for cross-service debugging.
- **Idempotency Support:** Ensures that state changes are consistent with the event-driven inputs.

---

## 🛡️ Security, Penetration & Load Testing

### 1. Security & Penetration Testing (SAST/DAST)
- **SAST (Static Analysis):** The project is scanned using tools like **SonarQube** and **Snyk** during the CI/CD pipeline to identify code-level vulnerabilities.
- **DAST (Dynamic Analysis):** **OWASP ZAP** is used in the UAT environment to test for runtime vulnerabilities and improper security headers.
- **Dependency Scanning:** **OWASP Dependency-Check** ensures that third-party libraries (like Spring Boot) are free from known CVEs.
- **Secret Scanning:** GitHub Advanced Security scans for hardcoded tokens or secrets in the repository.

### 2. Load & Performance Testing
- **Tooling:** **JMeter** and **Gatling** are used to simulate high-concurrency event ingestion.
- **Objective:** Verify that the system handles **10,000+ Requests Per Second (RPS)** with sub-100ms latency.
- **Scalability:** The production Kubernetes setup (3 replicas) is stress-tested to ensure the **Horizontal Pod Autoscaler (HPA)** triggers correctly under heavy load.
- **Resiliency Verification:** During load tests, the **Circuit Breaker** is monitored to ensure it opens and closes as expected under service-to-service latency or failure.

---

## 🛠️ Technology Stack
- **Backend:** Java 21, Spring Boot 3.3.0
- **Resiliency:** Resilience4j (Circuit Breaker)
- **Security:** Spring Security (Basic Auth + Service Tokens)
- **Infrastructure:** Kubernetes (Namespaced), Docker, Docker Compose
- **Observability:** SLF4J + MDC (Structured JSON Logging)
- **CI/CD:** GitHub Actions & **Jenkins Pipelines**

---

## 🔄 CI/CD Pipeline Stages
| Stage | Trigger | Purpose | Environment |
|---|---|---|---|
| **Build & Test** | Push/PR | Maven build + Unit/Integration Tests. | Runner |
| **Security Scan** | Build | SAST, Dependency, and Secret scanning. | Runner |
| **Docker Build** | Success | Builds images and pushes to registry. | Registry |
| **Deploy QA** | Push to `develop` | Functional verification (1 replica). | `ledger-qa` |
| **Deploy UAT** | `release/*` | DAST & Load testing (2 replicas). | `ledger-uat` |
| **Deploy PROD** | Push to `main` | High-availability rollout (3 replicas). | `ledger-prod` |

---

## 📁 Project Structure

```text
event-ledger-faang-B/
├── common-lib/             # Shared DTOs and Logic
├── gateway-service/        # Event Ingestion & Resiliency
├── account-service/        # Balance & Account Management
├── infrastructure/         # K8s, Docker, Istio, OPA, ArgoCD
├── cicd/                   # GitHub Actions Workflows
├── Jenkinsfile             # Jenkins Pipeline Definition
└── pom.xml                 # Parent POM
```

---

## 🐳 Docker & Local Orchestration
```bash
# Start all services + infrastructure
docker-compose -f infrastructure/docker/docker-compose.yml up -d
```

---

## 🔐 Security Architecture (POC)
- **Public Gateway Security**: All endpoints (except `/health`) require **Basic Authentication** (`ledger:ledger`). JWT Bearer tokens are recommended for production environments.
- **Internal Service Security**: The Account Service strictly requires an `X-Internal-Service-Token`.
- **Zero-Trust**: Every hop in the request chain is authenticated and traced.

---

## 🧪 Testing
```bash
mvn test
```
*Note: Includes a fix for Mockito/ByteBuddy compatibility on modern Java versions.*

---

## 📡 Sample Test Cases (Postman / Curl Requests)

### 1. Gateway Service (Ingestion)

**Request: Submit New CREDIT Event**
- **Method:** `POST`
- **URL:** `http://localhost:8080/events`
- **Auth:** Basic Auth (`ledger` / `ledger`)
- **Body (JSON):**
```json
{
  "eventId": "evt-001",
  "accountId": "acct-123",
  "type": "CREDIT",
  "amount": 100.0,
  "eventTimestamp": "2024-05-30T10:00:00Z"
}
```
- **Response (200 OK):** `"EVENT_ACCEPTED"`

**Request: Submit Duplicate Event**
- **Body (JSON):** Same as above
- **Response (200 OK):** `"DUPLICATE_EVENT"`

**Request: Submit Invalid Amount**
- **Body (JSON):** `{"eventId": "evt-002", "amount": -50.0, ...}`
- **Response (200 OK):** `"ERROR_INVALID_AMOUNT"`

---

### 2. Account Service (Ledger)

**Request: Get Account Balance**
- **Method:** `GET`
- **URL:** `http://localhost:8081/accounts/acct-123`
- **Auth:** N/A (Internal Security requires Service Token if accessed directly)
- **Response (200 OK):**
```json
{
  "accountId": "acct-123",
  "balance": 100.0
}
```

**Request: Apply DEBIT (via Gateway)**
- **Method:** `POST`
- **Gateway URL:** `http://localhost:8080/events`
- **Body (JSON):**
```json
{
  "eventId": "evt-003",
  "accountId": "acct-123",
  "type": "DEBIT",
  "amount": 40.0,
  "eventTimestamp": "2024-05-30T10:05:00Z"
}
```
- **Response (200 OK):** `"EVENT_ACCEPTED"`

**Request: Verify Updated Balance**
- **URL:** `http://localhost:8081/accounts/acct-123`
- **Response (200 OK):** `{"accountId": "acct-123", "balance": 60.0}`

---

## 🚀 Final Commands for Verification
```bash
mvn clean install
```

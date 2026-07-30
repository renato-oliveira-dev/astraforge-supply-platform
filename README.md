# AstraForge Supply Platform

A fictional industrial procurement and supply-orchestration platform built with **Java 21**, **Spring Boot**, **Clean Architecture**, **Domain-Driven Design (DDD)**, and production-oriented engineering practices.

> AstraForge is an original portfolio project. Its company, actors, data, workflows, policies, examples, and identifiers are fictional and are not derived from or affiliated with any employer, customer, or commercial software product.

---

## Project Status

**Current Phase:** Foundation Bootstrap

The project follows an **Architecture First** approach. The architectural baseline is documented before production features are implemented, allowing code, tests, and delivery controls to be traced to explicit decisions.

---

## Business Scenario

AstraForge coordinates procurement of industrial maintenance supplies across distributed facilities.

A requesting facility creates a requisition for catalog items such as safety equipment, replacement components, tools, and consumables. The platform validates organizational eligibility, pricing, stock availability, approval policies, sourcing options, fulfillment constraints, and audit requirements before converting an approved requisition into a purchase order.

The fictional domain includes:

- requesting organizations and facilities;
- supplier and catalog management;
- requisition preparation;
- purchase-order lifecycle management;
- policy-based approvals;
- inventory allocation;
- shipment orchestration;
- invoice reconciliation;
- notifications and immutable audit history;
- asynchronous integration events.

No production data, proprietary source code, private endpoint, employer-specific identifier, or confidential business rule is included.

---

## Engineering Objectives

- Demonstrate enterprise backend engineering with Java 21 and Spring Boot
- Apply Domain-Driven Design, Clean Architecture, and hexagonal boundaries
- Build scalable REST APIs and event-driven integrations
- Use automated architecture, quality, security, and repository-governance gates
- Document architectural decisions and implementation constraints
- Maintain an independently created, fictional portfolio domain

---

## Planned Business Capabilities

- Organization and facility registry
- Supplier management
- Industrial catalog
- Requisition management
- Purchase-order management
- Approval policy engine
- Pricing and landed-cost calculation
- Inventory allocation
- Shipment orchestration
- Invoice reconciliation
- Notifications
- Audit trail
- Integration-event publishing

---

## Technology Stack

### Backend
- Java 21
- Spring Boot 4.1
- Gradle 9.6.1

### Data and Integration
- PostgreSQL
- Redis
- Flyway
- Amazon SQS
- Transactional Outbox Pattern

### Security and Quality
- Spring Security and JWT
- JUnit 5, Mockito, AssertJ, Testcontainers
- JaCoCo and ArchUnit
- SonarQube / SonarCloud
- Dependency and secret scanning

### Delivery
- Docker and Docker Compose
- GitHub Actions
- OpenAPI
- Architecture Decision Records
- Markdown and Mermaid

---

## Architectural Direction

The system starts as a **modular monolith** and preserves boundaries that allow independently deployable services to emerge only when justified by operational or organizational needs.

```text
                        API Clients
                            |
                            v
                      Inbound Adapters
                            |
                            v
                    Application Use Cases
                            |
                            v
                         Domain
            (Aggregates, Policies, Value Objects)
                            |
                  +---------+---------+
                  |                   |
                  v                   v
            Outbound Ports       Domain Events
                  |
                  v
          Infrastructure Adapters
          - PostgreSQL
          - Redis
          - Amazon SQS
          - External systems
```

The domain remains independent of Spring, persistence, messaging, and transport details.

---

## Documentation

Documentation is maintained under `docs`.

- `docs/architecture/decisions`: ADR-001 through ADR-090
- `docs/domain`: domain model and ubiquitous language
- `docs/requirements`: product vision and requirements
- `docs/standards`: engineering standards
- `docs/governance`: originality, intellectual-property, and repository controls

## Implementation Status

- ADR governance baseline complete
- Java 21 and Spring Boot foundation complete
- Gradle wrapper and CI quality gates complete
- Project identity and fictional domain boundary established
- Purchase-order domain implementation pending

## First Local Build

```powershell
.\gradlew.bat clean check --no-daemon --stacktrace
```

## Repository Governance

```powershell
.\gradlew.bat validateRepository
```

Repository validation is implemented as a typed Java convention plugin in the
`build-logic` included build. Line-ending validation uses `.gitattributes` as
the source of truth, including LF for source files and CRLF for Windows batch
scripts. No Python installation is required.

## License and Independence

The source code in this repository is published under the repository license. Third-party libraries retain their respective licenses.

AstraForge is a fictional name used solely for this portfolio project. See [`docs/governance/originality-and-ip-policy.md`](docs/governance/originality-and-ip-policy.md).

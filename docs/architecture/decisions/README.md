# Architecture Decision Records — Master Index

> AstraForge Supply Platform — canonical index for ADR-001 through ADR-090.

## Purpose

This index is the authoritative navigation entry point for the architecture decision record set. It provides the canonical ADR number, title, primary category, current status, and supersession relationship without removing historical decisions.

## Inventory

- Total ADRs: **90**
- Accepted ADRs: **74**
- Superseded ADRs: **16**
- Numbering range: **ADR-001 through ADR-090**
- Missing numbers: **None**
- Duplicate numbers: **None**

## Status Semantics

- **Accepted** — current decision or an intentionally retained complementary decision.
- **Superseded** — historical decision retained for traceability; use the ADR identified by `Superseded By` for current guidance.
- Superseding an ADR does **not** delete history. The previous document remains part of the architectural record.

## Canonical Supersession Chains

The following chains are especially important when navigating overlapping decisions:

- **Messaging:** ADR-009 → ADR-090; ADR-030 → ADR-090; ADR-057 → ADR-090.
- **Production readiness:** ADR-027 → ADR-040.
- **Disaster recovery:** ADR-028 → ADR-045.
- **Data privacy and retention:** ADR-029 → ADR-046 → ADR-071.
- **Caching:** ADR-032 → ADR-059 → ADR-080.
- **API Gateway / Edge:** ADR-033 → ADR-073 → ADR-082.
- **Architecture fitness functions:** ADR-042 → ADR-051.
- **Legacy modernization:** ADR-047 → ADR-087.
- **Database migration:** ADR-076 → ADR-084.
- **Supply-chain security:** ADR-038 → ADR-085.
- **Code review / modernization split:** ADR-069 → ADR-086 and ADR-087.

## Complementary Decisions

Some ADRs overlap by design and remain simultaneously valid because they address different architectural depths or viewpoints:

- **ADR-065, ADR-088 and ADR-089** — integrated DDD/service-boundary baseline, detailed domain modeling, and Hexagonal/Clean Architecture respectively.
- **ADR-070 and ADR-085** — dependency/library governance and broader SBOM/SCA/supply-chain vulnerability governance respectively.
- **ADR-035, ADR-053 and ADR-068** — engineering quality baseline, enterprise testing strategy, and detailed test architecture/coverage governance respectively.
- **ADR-050 and ADR-051** — enterprise architecture operating model and executable architecture fitness-function implementation respectively.

## Messaging Canonical Decision

**ADR-090 is the current canonical messaging decision.** ADR-009, ADR-030 and ADR-057 are retained as historical records and are superseded by ADR-090. Any supporting documentation that still presents Kafka as the mandatory current broker must be reconciled with ADR-090 during documentation synchronization.

## ADR Catalog

| ADR | Title | Primary Category | Status | Supersedes | Superseded By |
|---|---|---|---|---|---|
| [ADR-001](ADR-001-adopt-clean-architecture.md) | Adopt Clean Architecture | Architecture & Design | Accepted | None | None |
| [ADR-002](ADR-002-adopt-domain-driven-design.md) | Adopt Domain-Driven Design | Architecture & Design | Accepted | None | None |
| [ADR-003](ADR-003-use-java-21.md) | Use Java 21 | Platform & Runtime | Accepted | None | None |
| [ADR-004](ADR-004-use-spring-boot.md) | Use Spring Boot | Platform & Runtime | Accepted | None | None |
| [ADR-005](ADR-005-use-postgresql-as-primary-database.md) | Use PostgreSQL as the Primary Database | Data & Persistence | Accepted | None | None |
| [ADR-006](ADR-006-use-flyway-for-database-migrations.md) | Use Flyway for Database Migrations | Data & Persistence | Accepted | None | None |
| [ADR-007](ADR-007-adopt-transactional-outbox.md) | Adopt the Transactional Outbox Pattern | Messaging & Distributed Systems | Accepted | None | None |
| [ADR-008](ADR-008-assume-at-least-once-message-delivery.md) | Assume At-Least-Once Message Delivery | Messaging & Distributed Systems | Accepted | None | None |
| [ADR-009](ADR-009-use-kafka-for-integration-events.md) | Use Apache Kafka for Integration Events | Messaging & Distributed Systems | Superseded | None | ADR-090 |
| [ADR-010](ADR-010-use-redis-for-distributed-caching.md) | Use Redis for Distributed Caching | Data & Caching | Accepted | None | None |
| [ADR-011](ADR-011-adopt-openapi-first-api-design.md) | Adopt OpenAPI-First API Design | API & Integration | Accepted | None | None |
| [ADR-012](ADR-012-adopt-saga-pattern-for-distributed-workflows.md) | Adopt the Saga Pattern for Distributed Workflows | Messaging & Distributed Systems | Accepted | None | None |
| [ADR-013](ADR-013-use-testcontainers-for-integration-testing.md) | Use Testcontainers for Integration Testing | Testing & Quality | Accepted | None | None |
| [ADR-014](ADR-014-adopt-opentelemetry-for-distributed-observability.md) | Adopt OpenTelemetry for Distributed Observability | Observability & Operations | Accepted | None | None |
| [ADR-015](ADR-015-deploy-workloads-on-kubernetes.md) | Deploy Workloads on Kubernetes | Platform & Runtime | Accepted | None | None |
| [ADR-016](ADR-016-adopt-resilience4j-for-application-resilience.md) | Adopt Resilience4j for Application Resilience | Reliability & Resilience | Accepted | None | None |
| [ADR-017](ADR-017-adopt-optimistic-locking-for-concurrent-aggregate-updates.md) | Adopt Optimistic Locking for Concurrent Aggregate Updates | Data & Persistence | Accepted | None | None |
| [ADR-018](ADR-018-version-integration-event-contracts.md) | Version Integration Event Contracts | Messaging & Distributed Systems | Accepted | None | None |
| [ADR-019](ADR-019-adopt-structured-logging.md) | Adopt Structured Logging | Observability & Operations | Accepted | None | None |
| [ADR-020](ADR-020-define-service-level-objectives.md) | Define Service-Level Objectives | Reliability & Resilience | Accepted | None | None |
| [ADR-021](ADR-021-adopt-zero-downtime-deployment-practices.md) | Adopt Zero-Downtime Deployment Practices | Delivery & Deployment | Accepted | None | None |
| [ADR-022](ADR-022-adopt-api-contract-governance.md) | Adopt API Contract Governance | API & Integration | Accepted | None | None |
| [ADR-023](ADR-023-adopt-api-security-standards.md) | Adopt API Security Standards | Security | Accepted | None | None |
| [ADR-024](ADR-024-adopt-software-supply-chain-security.md) | Adopt Software Supply Chain Security | Security & Supply Chain | Accepted | None | None |
| [ADR-025](ADR-025-adopt-kubernetes-runtime-security-standards.md) | Adopt Kubernetes Runtime Security Standards | Security & Platform | Accepted | None | None |
| [ADR-026](ADR-026-adopt-platform-configuration-and-secret-management-standards.md) | Adopt Platform Configuration and Secret Management Standards | Configuration & Secrets | Accepted | None | None |
| [ADR-027](ADR-027-adopt-production-incident-management-and-operational-readiness-standards.md) | Adopt Production Incident Management and Operational Readiness Standards | Observability & Operations | Superseded | None | ADR-040 |
| [ADR-028](ADR-028-adopt-disaster-recovery-and-business-continuity-standards.md) | Adopt Disaster Recovery and Business Continuity Standards | Reliability & Resilience | Superseded | None | ADR-045 |
| [ADR-029](ADR-029-adopt-data-protection-privacy-and-retention-standards.md) | Adopt Data Protection, Privacy and Retention Standards | Data Governance & Privacy | Superseded | None | ADR-046 |
| [ADR-030](ADR-030-adopt-kafka-event-governance-and-schema-evolution-standards.md) | Adopt Kafka Event Governance and Schema Evolution Standards | Messaging & Distributed Systems | Superseded | None | ADR-090 |
| [ADR-031](ADR-031-adopt-database-performance-and-data-access-standards.md) | Adopt Database Performance and Data Access Standards | Data & Persistence | Accepted | None | None |
| [ADR-032](ADR-032-adopt-distributed-caching-and-cache-consistency-standards.md) | Adopt Distributed Caching and Cache Consistency Standards | Data & Caching | Superseded | None | ADR-059 |
| [ADR-033](ADR-033-adopt-api-gateway-and-edge-architecture-standards.md) | Adopt API Gateway and Edge Architecture Standards | API & Integration | Superseded | None | ADR-073 |
| [ADR-034](ADR-034-adopt-java-21-concurrency-and-parallelism-standards.md) | Adopt Java 21 Concurrency and Parallelism Standards | Platform & Runtime | Accepted | None | None |
| [ADR-035](ADR-035-adopt-engineering-quality-and-testing-standards.md) | Adopt Engineering Quality and Testing Standards | Testing & Quality | Accepted | None | None |
| [ADR-036](ADR-036-adopt-api-design-rest-contract-and-compatibility-standards.md) | Adopt API Design, REST Contract and Compatibility Standards | API & Integration | Accepted | None | None |
| [ADR-037](ADR-037-adopt-application-security-and-secure-coding-standards.md) | Adopt Application Security and Secure Coding Standards | Security | Accepted | None | None |
| [ADR-038](ADR-038-adopt-dependency-and-software-supply-chain-security-standards.md) | Adopt Dependency and Software Supply Chain Security Standards | Security & Supply Chain | Superseded | None | ADR-085 |
| [ADR-039](ADR-039-adopt-cicd-release-and-deployment-governance-standards.md) | Adopt CI/CD, Release and Deployment Governance Standards | Delivery & Deployment | Accepted | None | None |
| [ADR-040](ADR-040-adopt-production-reliability-incident-response-and-operational-readiness-standards.md) | Adopt Production Reliability, Incident Response and Operational Readiness Standards | Observability & Operations | Accepted | ADR-027 | None |
| [ADR-041](ADR-041-adopt-architecture-governance-and-technical-debt-management-standards.md) | Adopt Architecture Governance and Technical Debt Management Standards | Architecture Governance | Accepted | None | None |
| [ADR-042](ADR-042-adopt-architecture-fitness-functions-and-automated-governance-standards.md) | Adopt Architecture Fitness Functions and Automated Governance Standards | Architecture Governance | Superseded | None | ADR-051 |
| [ADR-043](ADR-043-adopt-service-ownership-platform-boundaries-and-team-topology-standards.md) | Adopt Service Ownership, Platform Boundaries and Team Topology Standards | Architecture Governance | Accepted | None | None |
| [ADR-044](ADR-044-adopt-finops-capacity-efficiency-and-cloud-cost-governance-standards.md) | Adopt FinOps, Capacity Efficiency and Cloud Cost Governance Standards | Cloud & Cost Governance | Accepted | None | None |
| [ADR-045](ADR-045-adopt-business-continuity-disaster-recovery-and-regional-resilience-standards.md) | Adopt Business Continuity, Disaster Recovery and Regional Resilience Standards | Reliability & Resilience | Accepted | ADR-028 | None |
| [ADR-046](ADR-046-adopt-data-governance-privacy-retention-and-lifecycle-standards.md) | Adopt Data Governance, Privacy, Retention and Lifecycle Standards | Data Governance & Privacy | Superseded | ADR-029 | ADR-071 |
| [ADR-047](ADR-047-adopt-legacy-modernization-strangler-migration-and-technical-evolution-standards.md) | Adopt Legacy Modernization, Strangler Migration and Technical Evolution Standards | Modernization & Technical Debt | Superseded | None | ADR-087 |
| [ADR-048](ADR-048-adopt-engineering-productivity-developer-experience-and-innersource-standards.md) | Adopt Engineering Productivity, Developer Experience and InnerSource Standards | Engineering Productivity | Accepted | None | None |
| [ADR-049](ADR-049-adopt-ai-assisted-software-engineering-and-responsible-ai-development-standards.md) | Adopt AI-Assisted Software Engineering and Responsible AI Development Standards | AI-Assisted Engineering | Accepted | None | None |
| [ADR-050](ADR-050-adopt-enterprise-architecture-baseline-and-governance-operating-model.md) | Adopt Enterprise Architecture Baseline and Architecture Governance Operating Model | Architecture Governance | Accepted | None | None |
| [ADR-051](ADR-051-adopt-software-architecture-testing-and-automated-fitness-functions-implementation-standard.md) | Adopt Software Architecture Testing and Automated Fitness Functions Implementation Standard | Architecture Governance | Accepted | ADR-042 | None |
| [ADR-052](ADR-052-adopt-java-21-spring-boot-enterprise-coding-and-clean-code-standard.md) | Adopt Java 21 / Spring Boot Enterprise Coding and Clean Code Standard | Java & Coding Standards | Accepted | None | None |
| [ADR-053](ADR-053-adopt-enterprise-testing-strategy-test-pyramid-and-quality-engineering-standard.md) | Adopt Enterprise Testing Strategy, Test Pyramid and Quality Engineering Standard | Testing & Quality | Accepted | None | None |
| [ADR-054](ADR-054-adopt-enterprise-performance-engineering-capacity-testing-and-jvm-optimization-standard.md) | Adopt Enterprise Performance Engineering, Capacity Testing and JVM Optimization Standard | Performance & Capacity | Accepted | None | None |
| [ADR-055](ADR-055-adopt-enterprise-resilience-engineering-fault-tolerance-and-graceful-degradation-standard.md) | Adopt Enterprise Resilience Engineering, Fault Tolerance and Graceful Degradation Standard | Reliability & Resilience | Accepted | None | None |
| [ADR-056](ADR-056-adopt-enterprise-rest-api-design-versioning-error-handling-and-integration-contract-standard.md) | Adopt Enterprise REST API Design, Versioning, Error Handling and Integration Contract Standard | API & Integration | Accepted | None | None |
| [ADR-057](ADR-057-adopt-enterprise-event-driven-architecture-kafka-messaging-and-transactional-outbox-standard.md) | Adopt Enterprise Event-Driven Architecture, Kafka Messaging and Transactional Outbox Standard | Messaging & Distributed Systems | Superseded | None | ADR-090 |
| [ADR-058](ADR-058-adopt-enterprise-postgresql-persistence-transaction-management-and-database-engineering-standard.md) | Adopt Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard | Data & Persistence | Accepted | None | None |
| [ADR-059](ADR-059-adopt-enterprise-redis-caching-distributed-cache-and-data-consistency-standard.md) | Adopt Enterprise Redis Caching, Distributed Cache and Data Consistency Standard | Data & Caching | Superseded | ADR-032 | ADR-080 |
| [ADR-060](ADR-060-adopt-enterprise-aws-cloud-kubernetes-container-and-runtime-deployment-standard.md) | Adopt Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard | Platform & Runtime | Accepted | None | None |
| [ADR-061](ADR-061-adopt-enterprise-cicd-devsecops-software-supply-chain-and-release-engineering-standard.md) | Adopt Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard | Delivery & Deployment | Accepted | None | None |
| [ADR-062](ADR-062-adopt-enterprise-logging-observability-opentelemetry-and-production-diagnostics-standard.md) | Adopt Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard | Observability & Operations | Accepted | None | None |
| [ADR-063](ADR-063-adopt-enterprise-configuration-secrets-feature-flags-and-runtime-parameter-governance-standard.md) | Adopt Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard | Configuration & Secrets | Accepted | None | None |
| [ADR-064](ADR-064-adopt-enterprise-authentication-authorization-oauth2-oidc-jwt-and-service-to-service-security-standard.md) | Adopt Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard | Security | Accepted | None | None |
| [ADR-065](ADR-065-adopt-enterprise-domain-driven-design-service-boundaries-clean-architecture-and-modularization-standard.md) | Adopt Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard | Architecture & Design | Accepted | None | None |
| [ADR-066](ADR-066-adopt-enterprise-api-performance-data-retrieval-pagination-filtering-sorting-and-bulk-processing-standard.md) | Adopt Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard | API & Integration | Accepted | None | None |
| [ADR-067](ADR-067-adopt-enterprise-error-handling-exception-taxonomy-problem-details-and-failure-contract-standard.md) | Adopt Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard | API & Integration | Accepted | None | None |
| [ADR-068](ADR-068-adopt-enterprise-test-architecture-test-data-mocking-testcontainers-and-coverage-governance-standard.md) | Adopt Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard | Testing & Quality | Accepted | None | None |
| [ADR-069](ADR-069-adopt-enterprise-code-review-refactoring-technical-debt-and-legacy-modernization-standard.md) | Adopt Enterprise Code Review, Refactoring, Technical Debt and Legacy Modernization Standard | Modernization & Technical Debt | Superseded | None | ADR-086, ADR-087 |
| [ADR-070](ADR-070-adopt-enterprise-dependency-management-gradle-version-catalog-bom-library-governance-and-java-supply-chain-standard.md) | Adopt Enterprise Dependency Management, Gradle, Version Catalog, BOM, Library Governance and Java Supply Chain Standard | Security & Supply Chain | Accepted | None | None |
| [ADR-071](ADR-071-adopt-enterprise-data-privacy-pii-auditability-retention-and-secure-data-handling-standard.md) | Adopt Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard | Data Governance & Privacy | Accepted | ADR-046 | None |
| [ADR-072](ADR-072-adopt-enterprise-distributed-transactions-saga-idempotency-consistency-and-compensation-standard.md) | Adopt Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard | Messaging & Distributed Systems | Accepted | None | None |
| [ADR-073](ADR-073-adopt-enterprise-api-gateway-bff-edge-security-traffic-management-and-rate-limiting-standard.md) | Adopt Enterprise API Gateway, BFF, Edge Security, Traffic Management and Rate Limiting Standard | API & Integration | Superseded | ADR-033 | ADR-082 |
| [ADR-074](ADR-074-adopt-enterprise-service-discovery-dns-load-balancing-and-internal-network-communication-standard.md) | Adopt Enterprise Service Discovery, DNS, Load Balancing and Internal Network Communication Standard | Platform & Networking | Accepted | None | None |
| [ADR-075](ADR-075-adopt-enterprise-application-lifecycle-health-checks-readiness-liveness-startup-and-graceful-shutdown-standard.md) | Adopt Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard | Platform & Runtime | Accepted | None | None |
| [ADR-076](ADR-076-adopt-enterprise-database-migration-zero-downtime-schema-evolution-and-expand-contract-standard.md) | Adopt Enterprise Database Migration, Zero-Downtime Schema Evolution and Expand/Contract Standard | Data & Persistence | Superseded | None | ADR-084 |
| [ADR-077](ADR-077-adopt-enterprise-scheduled-jobs-batch-processing-distributed-scheduling-and-workload-coordination-standard.md) | Adopt Enterprise Scheduled Jobs, Batch Processing, Distributed Scheduling and Workload Coordination Standard | Batch & Workload Processing | Accepted | None | None |
| [ADR-078](ADR-078-adopt-enterprise-file-processing-upload-download-streaming-and-large-file-handling-standard.md) | Adopt Enterprise File Processing, Upload, Download, Streaming and Large File Handling Standard | File & Data Processing | Accepted | None | None |
| [ADR-079](ADR-079-adopt-enterprise-notification-email-sms-push-and-external-communication-standard.md) | Adopt Enterprise Notification, Email, SMS, Push and External Communication Standard | Notifications & Communications | Accepted | None | None |
| [ADR-080](ADR-080-adopt-enterprise-caching-redis-local-cache-invalidation-and-resilient-fallback-standard.md) | Adopt Enterprise Caching, Redis, Local Cache, Cache Invalidation and Resilient Fallback Standard | Data & Caching | Accepted | ADR-059 | None |
| [ADR-081](ADR-081-adopt-enterprise-search-indexing-opensearch-full-text-search-and-read-model-standard.md) | Adopt Enterprise Search, Indexing, OpenSearch/Elasticsearch, Full-Text Search and Read Model Standard | Search & Read Models | Accepted | None | None |
| [ADR-082](ADR-082-adopt-enterprise-api-gateway-bff-edge-security-routing-and-traffic-management-standard.md) | Adopt Enterprise API Gateway, BFF, Edge Security, Routing and Traffic Management Standard | API & Integration | Accepted | ADR-073 | None |
| [ADR-083](ADR-083-adopt-enterprise-service-to-service-communication-service-discovery-internal-api-and-zero-trust-networking-standard.md) | Adopt Enterprise Service-to-Service Communication, Service Discovery, Internal APIs and Zero-Trust Networking Standard | Platform & Networking | Accepted | None | None |
| [ADR-084](ADR-084-adopt-enterprise-database-schema-evolution-flyway-zero-downtime-migration-and-data-backfill-standard.md) | Adopt Enterprise Database Schema Evolution, Flyway, Zero-Downtime Migration and Data Backfill Standard | Data & Persistence | Accepted | ADR-076 | None |
| [ADR-085](ADR-085-adopt-enterprise-dependency-management-gradle-sbom-supply-chain-security-and-vulnerability-governance-standard.md) | Adopt Enterprise Dependency Management, Gradle, SBOM, Supply Chain Security and Vulnerability Governance Standard | Security & Supply Chain | Accepted | ADR-038 | None |
| [ADR-086](ADR-086-adopt-enterprise-code-review-pull-request-branching-commit-cicd-quality-gates-and-definition-of-done-standard.md) | Adopt Enterprise Code Review, Pull Request, Branching, Commit, CI/CD Quality Gates and Definition of Done Standard | Delivery & Quality Governance | Accepted | ADR-069 | None |
| [ADR-087](ADR-087-adopt-enterprise-technical-debt-legacy-modernization-refactoring-and-continuous-architecture-governance-standard.md) | Adopt Enterprise Technical Debt, Legacy Modernization, Refactoring and Continuous Architecture Governance Standard | Modernization & Technical Debt | Accepted | ADR-047, ADR-069 | None |
| [ADR-088](ADR-088-adopt-enterprise-domain-driven-design-bounded-context-aggregate-domain-event-and-business-rule-modeling-standard.md) | Adopt Enterprise Domain-Driven Design, Bounded Context, Aggregate, Domain Event and Business Rule Modeling Standard | Architecture & Design | Accepted | None | None |
| [ADR-089](ADR-089-adopt-enterprise-hexagonal-clean-architecture-ports-adapters-and-module-boundary-standard.md) | Adopt Enterprise Hexagonal Architecture, Clean Architecture, Ports & Adapters and Module Boundary Standard | Architecture & Design | Accepted | None | None |
| [ADR-090](ADR-090-adopt-enterprise-event-driven-architecture-sqs-transactional-outbox-idempotency-event-contract-and-messaging-governance-standard.md) | Adopt Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard | Messaging & Distributed Systems | Accepted | ADR-009, ADR-030, ADR-057 | None |

## Navigation by Primary Category

### AI-Assisted Engineering

- [ADR-049 — Adopt AI-Assisted Software Engineering and Responsible AI Development Standards](ADR-049-adopt-ai-assisted-software-engineering-and-responsible-ai-development-standards.md)

### API & Integration

- [ADR-011 — Adopt OpenAPI-First API Design](ADR-011-adopt-openapi-first-api-design.md)
- [ADR-022 — Adopt API Contract Governance](ADR-022-adopt-api-contract-governance.md)
- [ADR-033 — Adopt API Gateway and Edge Architecture Standards](ADR-033-adopt-api-gateway-and-edge-architecture-standards.md) — **Superseded**
- [ADR-036 — Adopt API Design, REST Contract and Compatibility Standards](ADR-036-adopt-api-design-rest-contract-and-compatibility-standards.md)
- [ADR-056 — Adopt Enterprise REST API Design, Versioning, Error Handling and Integration Contract Standard](ADR-056-adopt-enterprise-rest-api-design-versioning-error-handling-and-integration-contract-standard.md)
- [ADR-066 — Adopt Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard](ADR-066-adopt-enterprise-api-performance-data-retrieval-pagination-filtering-sorting-and-bulk-processing-standard.md)
- [ADR-067 — Adopt Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard](ADR-067-adopt-enterprise-error-handling-exception-taxonomy-problem-details-and-failure-contract-standard.md)
- [ADR-073 — Adopt Enterprise API Gateway, BFF, Edge Security, Traffic Management and Rate Limiting Standard](ADR-073-adopt-enterprise-api-gateway-bff-edge-security-traffic-management-and-rate-limiting-standard.md) — **Superseded**
- [ADR-082 — Adopt Enterprise API Gateway, BFF, Edge Security, Routing and Traffic Management Standard](ADR-082-adopt-enterprise-api-gateway-bff-edge-security-routing-and-traffic-management-standard.md)

### Architecture & Design

- [ADR-001 — Adopt Clean Architecture](ADR-001-adopt-clean-architecture.md)
- [ADR-002 — Adopt Domain-Driven Design](ADR-002-adopt-domain-driven-design.md)
- [ADR-065 — Adopt Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard](ADR-065-adopt-enterprise-domain-driven-design-service-boundaries-clean-architecture-and-modularization-standard.md)
- [ADR-088 — Adopt Enterprise Domain-Driven Design, Bounded Context, Aggregate, Domain Event and Business Rule Modeling Standard](ADR-088-adopt-enterprise-domain-driven-design-bounded-context-aggregate-domain-event-and-business-rule-modeling-standard.md)
- [ADR-089 — Adopt Enterprise Hexagonal Architecture, Clean Architecture, Ports & Adapters and Module Boundary Standard](ADR-089-adopt-enterprise-hexagonal-clean-architecture-ports-adapters-and-module-boundary-standard.md)

### Architecture Governance

- [ADR-041 — Adopt Architecture Governance and Technical Debt Management Standards](ADR-041-adopt-architecture-governance-and-technical-debt-management-standards.md)
- [ADR-042 — Adopt Architecture Fitness Functions and Automated Governance Standards](ADR-042-adopt-architecture-fitness-functions-and-automated-governance-standards.md) — **Superseded**
- [ADR-043 — Adopt Service Ownership, Platform Boundaries and Team Topology Standards](ADR-043-adopt-service-ownership-platform-boundaries-and-team-topology-standards.md)
- [ADR-050 — Adopt Enterprise Architecture Baseline and Architecture Governance Operating Model](ADR-050-adopt-enterprise-architecture-baseline-and-governance-operating-model.md)
- [ADR-051 — Adopt Software Architecture Testing and Automated Fitness Functions Implementation Standard](ADR-051-adopt-software-architecture-testing-and-automated-fitness-functions-implementation-standard.md)

### Batch & Workload Processing

- [ADR-077 — Adopt Enterprise Scheduled Jobs, Batch Processing, Distributed Scheduling and Workload Coordination Standard](ADR-077-adopt-enterprise-scheduled-jobs-batch-processing-distributed-scheduling-and-workload-coordination-standard.md)

### Cloud & Cost Governance

- [ADR-044 — Adopt FinOps, Capacity Efficiency and Cloud Cost Governance Standards](ADR-044-adopt-finops-capacity-efficiency-and-cloud-cost-governance-standards.md)

### Configuration & Secrets

- [ADR-026 — Adopt Platform Configuration and Secret Management Standards](ADR-026-adopt-platform-configuration-and-secret-management-standards.md)
- [ADR-063 — Adopt Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard](ADR-063-adopt-enterprise-configuration-secrets-feature-flags-and-runtime-parameter-governance-standard.md)

### Data & Caching

- [ADR-010 — Use Redis for Distributed Caching](ADR-010-use-redis-for-distributed-caching.md)
- [ADR-032 — Adopt Distributed Caching and Cache Consistency Standards](ADR-032-adopt-distributed-caching-and-cache-consistency-standards.md) — **Superseded**
- [ADR-059 — Adopt Enterprise Redis Caching, Distributed Cache and Data Consistency Standard](ADR-059-adopt-enterprise-redis-caching-distributed-cache-and-data-consistency-standard.md) — **Superseded**
- [ADR-080 — Adopt Enterprise Caching, Redis, Local Cache, Cache Invalidation and Resilient Fallback Standard](ADR-080-adopt-enterprise-caching-redis-local-cache-invalidation-and-resilient-fallback-standard.md)

### Data & Persistence

- [ADR-005 — Use PostgreSQL as the Primary Database](ADR-005-use-postgresql-as-primary-database.md)
- [ADR-006 — Use Flyway for Database Migrations](ADR-006-use-flyway-for-database-migrations.md)
- [ADR-017 — Adopt Optimistic Locking for Concurrent Aggregate Updates](ADR-017-adopt-optimistic-locking-for-concurrent-aggregate-updates.md)
- [ADR-031 — Adopt Database Performance and Data Access Standards](ADR-031-adopt-database-performance-and-data-access-standards.md)
- [ADR-058 — Adopt Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard](ADR-058-adopt-enterprise-postgresql-persistence-transaction-management-and-database-engineering-standard.md)
- [ADR-076 — Adopt Enterprise Database Migration, Zero-Downtime Schema Evolution and Expand/Contract Standard](ADR-076-adopt-enterprise-database-migration-zero-downtime-schema-evolution-and-expand-contract-standard.md) — **Superseded**
- [ADR-084 — Adopt Enterprise Database Schema Evolution, Flyway, Zero-Downtime Migration and Data Backfill Standard](ADR-084-adopt-enterprise-database-schema-evolution-flyway-zero-downtime-migration-and-data-backfill-standard.md)

### Data Governance & Privacy

- [ADR-029 — Adopt Data Protection, Privacy and Retention Standards](ADR-029-adopt-data-protection-privacy-and-retention-standards.md) — **Superseded**
- [ADR-046 — Adopt Data Governance, Privacy, Retention and Lifecycle Standards](ADR-046-adopt-data-governance-privacy-retention-and-lifecycle-standards.md) — **Superseded**
- [ADR-071 — Adopt Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard](ADR-071-adopt-enterprise-data-privacy-pii-auditability-retention-and-secure-data-handling-standard.md)

### Delivery & Deployment

- [ADR-021 — Adopt Zero-Downtime Deployment Practices](ADR-021-adopt-zero-downtime-deployment-practices.md)
- [ADR-039 — Adopt CI/CD, Release and Deployment Governance Standards](ADR-039-adopt-cicd-release-and-deployment-governance-standards.md)
- [ADR-061 — Adopt Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard](ADR-061-adopt-enterprise-cicd-devsecops-software-supply-chain-and-release-engineering-standard.md)

### Delivery & Quality Governance

- [ADR-086 — Adopt Enterprise Code Review, Pull Request, Branching, Commit, CI/CD Quality Gates and Definition of Done Standard](ADR-086-adopt-enterprise-code-review-pull-request-branching-commit-cicd-quality-gates-and-definition-of-done-standard.md)

### Engineering Productivity

- [ADR-048 — Adopt Engineering Productivity, Developer Experience and InnerSource Standards](ADR-048-adopt-engineering-productivity-developer-experience-and-innersource-standards.md)

### File & Data Processing

- [ADR-078 — Adopt Enterprise File Processing, Upload, Download, Streaming and Large File Handling Standard](ADR-078-adopt-enterprise-file-processing-upload-download-streaming-and-large-file-handling-standard.md)

### Java & Coding Standards

- [ADR-052 — Adopt Java 21 / Spring Boot Enterprise Coding and Clean Code Standard](ADR-052-adopt-java-21-spring-boot-enterprise-coding-and-clean-code-standard.md)

### Messaging & Distributed Systems

- [ADR-007 — Adopt the Transactional Outbox Pattern](ADR-007-adopt-transactional-outbox.md)
- [ADR-008 — Assume At-Least-Once Message Delivery](ADR-008-assume-at-least-once-message-delivery.md)
- [ADR-009 — Use Apache Kafka for Integration Events](ADR-009-use-kafka-for-integration-events.md) — **Superseded**
- [ADR-012 — Adopt the Saga Pattern for Distributed Workflows](ADR-012-adopt-saga-pattern-for-distributed-workflows.md)
- [ADR-018 — Version Integration Event Contracts](ADR-018-version-integration-event-contracts.md)
- [ADR-030 — Adopt Kafka Event Governance and Schema Evolution Standards](ADR-030-adopt-kafka-event-governance-and-schema-evolution-standards.md) — **Superseded**
- [ADR-057 — Adopt Enterprise Event-Driven Architecture, Kafka Messaging and Transactional Outbox Standard](ADR-057-adopt-enterprise-event-driven-architecture-kafka-messaging-and-transactional-outbox-standard.md) — **Superseded**
- [ADR-072 — Adopt Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard](ADR-072-adopt-enterprise-distributed-transactions-saga-idempotency-consistency-and-compensation-standard.md)
- [ADR-090 — Adopt Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard](ADR-090-adopt-enterprise-event-driven-architecture-sqs-transactional-outbox-idempotency-event-contract-and-messaging-governance-standard.md)

### Modernization & Technical Debt

- [ADR-047 — Adopt Legacy Modernization, Strangler Migration and Technical Evolution Standards](ADR-047-adopt-legacy-modernization-strangler-migration-and-technical-evolution-standards.md) — **Superseded**
- [ADR-069 — Adopt Enterprise Code Review, Refactoring, Technical Debt and Legacy Modernization Standard](ADR-069-adopt-enterprise-code-review-refactoring-technical-debt-and-legacy-modernization-standard.md) — **Superseded**
- [ADR-087 — Adopt Enterprise Technical Debt, Legacy Modernization, Refactoring and Continuous Architecture Governance Standard](ADR-087-adopt-enterprise-technical-debt-legacy-modernization-refactoring-and-continuous-architecture-governance-standard.md)

### Notifications & Communications

- [ADR-079 — Adopt Enterprise Notification, Email, SMS, Push and External Communication Standard](ADR-079-adopt-enterprise-notification-email-sms-push-and-external-communication-standard.md)

### Observability & Operations

- [ADR-014 — Adopt OpenTelemetry for Distributed Observability](ADR-014-adopt-opentelemetry-for-distributed-observability.md)
- [ADR-019 — Adopt Structured Logging](ADR-019-adopt-structured-logging.md)
- [ADR-027 — Adopt Production Incident Management and Operational Readiness Standards](ADR-027-adopt-production-incident-management-and-operational-readiness-standards.md) — **Superseded**
- [ADR-040 — Adopt Production Reliability, Incident Response and Operational Readiness Standards](ADR-040-adopt-production-reliability-incident-response-and-operational-readiness-standards.md)
- [ADR-062 — Adopt Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard](ADR-062-adopt-enterprise-logging-observability-opentelemetry-and-production-diagnostics-standard.md)

### Performance & Capacity

- [ADR-054 — Adopt Enterprise Performance Engineering, Capacity Testing and JVM Optimization Standard](ADR-054-adopt-enterprise-performance-engineering-capacity-testing-and-jvm-optimization-standard.md)

### Platform & Networking

- [ADR-074 — Adopt Enterprise Service Discovery, DNS, Load Balancing and Internal Network Communication Standard](ADR-074-adopt-enterprise-service-discovery-dns-load-balancing-and-internal-network-communication-standard.md)
- [ADR-083 — Adopt Enterprise Service-to-Service Communication, Service Discovery, Internal APIs and Zero-Trust Networking Standard](ADR-083-adopt-enterprise-service-to-service-communication-service-discovery-internal-api-and-zero-trust-networking-standard.md)

### Platform & Runtime

- [ADR-003 — Use Java 21](ADR-003-use-java-21.md)
- [ADR-004 — Use Spring Boot](ADR-004-use-spring-boot.md)
- [ADR-015 — Deploy Workloads on Kubernetes](ADR-015-deploy-workloads-on-kubernetes.md)
- [ADR-034 — Adopt Java 21 Concurrency and Parallelism Standards](ADR-034-adopt-java-21-concurrency-and-parallelism-standards.md)
- [ADR-060 — Adopt Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard](ADR-060-adopt-enterprise-aws-cloud-kubernetes-container-and-runtime-deployment-standard.md)
- [ADR-075 — Adopt Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard](ADR-075-adopt-enterprise-application-lifecycle-health-checks-readiness-liveness-startup-and-graceful-shutdown-standard.md)

### Reliability & Resilience

- [ADR-016 — Adopt Resilience4j for Application Resilience](ADR-016-adopt-resilience4j-for-application-resilience.md)
- [ADR-020 — Define Service-Level Objectives](ADR-020-define-service-level-objectives.md)
- [ADR-028 — Adopt Disaster Recovery and Business Continuity Standards](ADR-028-adopt-disaster-recovery-and-business-continuity-standards.md) — **Superseded**
- [ADR-045 — Adopt Business Continuity, Disaster Recovery and Regional Resilience Standards](ADR-045-adopt-business-continuity-disaster-recovery-and-regional-resilience-standards.md)
- [ADR-055 — Adopt Enterprise Resilience Engineering, Fault Tolerance and Graceful Degradation Standard](ADR-055-adopt-enterprise-resilience-engineering-fault-tolerance-and-graceful-degradation-standard.md)

### Search & Read Models

- [ADR-081 — Adopt Enterprise Search, Indexing, OpenSearch/Elasticsearch, Full-Text Search and Read Model Standard](ADR-081-adopt-enterprise-search-indexing-opensearch-full-text-search-and-read-model-standard.md)

### Security

- [ADR-023 — Adopt API Security Standards](ADR-023-adopt-api-security-standards.md)
- [ADR-037 — Adopt Application Security and Secure Coding Standards](ADR-037-adopt-application-security-and-secure-coding-standards.md)
- [ADR-064 — Adopt Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard](ADR-064-adopt-enterprise-authentication-authorization-oauth2-oidc-jwt-and-service-to-service-security-standard.md)

### Security & Platform

- [ADR-025 — Adopt Kubernetes Runtime Security Standards](ADR-025-adopt-kubernetes-runtime-security-standards.md)

### Security & Supply Chain

- [ADR-024 — Adopt Software Supply Chain Security](ADR-024-adopt-software-supply-chain-security.md)
- [ADR-038 — Adopt Dependency and Software Supply Chain Security Standards](ADR-038-adopt-dependency-and-software-supply-chain-security-standards.md) — **Superseded**
- [ADR-070 — Adopt Enterprise Dependency Management, Gradle, Version Catalog, BOM, Library Governance and Java Supply Chain Standard](ADR-070-adopt-enterprise-dependency-management-gradle-version-catalog-bom-library-governance-and-java-supply-chain-standard.md)
- [ADR-085 — Adopt Enterprise Dependency Management, Gradle, SBOM, Supply Chain Security and Vulnerability Governance Standard](ADR-085-adopt-enterprise-dependency-management-gradle-sbom-supply-chain-security-and-vulnerability-governance-standard.md)

### Testing & Quality

- [ADR-013 — Use Testcontainers for Integration Testing](ADR-013-use-testcontainers-for-integration-testing.md)
- [ADR-035 — Adopt Engineering Quality and Testing Standards](ADR-035-adopt-engineering-quality-and-testing-standards.md)
- [ADR-053 — Adopt Enterprise Testing Strategy, Test Pyramid and Quality Engineering Standard](ADR-053-adopt-enterprise-testing-strategy-test-pyramid-and-quality-engineering-standard.md)
- [ADR-068 — Adopt Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard](ADR-068-adopt-enterprise-test-architecture-test-data-mocking-testcontainers-and-coverage-governance-standard.md)

## Usage Rules

1. Start architecture research from this index instead of searching ADR files by title alone.
2. When an ADR is marked **Superseded**, follow `Superseded By` before implementing a new solution.
3. Do not delete or rewrite historical ADRs merely because a newer decision exists.
4. When a future decision replaces an existing ADR, update both directions: `Supersedes` on the new ADR and `Superseded By` on the old ADR.
5. New cross-references should preferably use relative Markdown links and the canonical official ADR title.
6. The current ADR series intentionally ends at **ADR-090**. New documents should only be introduced when a genuinely new architecture decision is required, not merely to restate an existing standard.

## Maintenance Checklist

- [ ] ADR number is unique.
- [ ] Filename follows the ADR naming convention.
- [ ] Document title matches the filename number.
- [ ] Status is valid.
- [ ] Supersession is reciprocal when applicable.
- [ ] Related decisions reference existing ADRs.
- [ ] Supporting documentation is synchronized with canonical decisions.
- [ ] No historical ADR is silently rewritten to hide an architecture change.

## Current Baseline

The ADR set **ADR-001 through ADR-090** constitutes the current documented architecture decision baseline after the first two consolidation rounds. This index is the navigation layer for that baseline; supporting documentation remains subject to the synchronization round described in the architecture audit.

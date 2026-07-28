# ADR-015: Deploy Workloads on Kubernetes

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-015 |
| Title | Deploy Workloads on Kubernetes |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Platform Infrastructure |
| Related Work Items | Cloud Platform, Scalability and Operations |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform consists of multiple independently deployable services.

Examples include:

- Orders Service
- Customer Service
- Inventory Service
- Payment Service
- Notification Service
- Workflow Service
- API Gateway
- Background Workers
- Outbox Dispatcher
- Scheduled Jobs

The platform must support:

- horizontal scalability
- high availability
- rolling deployments
- automated recovery
- self-healing
- service discovery
- centralized configuration
- secret management
- observability
- workload isolation
- cloud portability

Managing these services manually or through virtual machines would significantly increase operational complexity.

---

# 2. Problem Statement

The platform requires an orchestration solution that:

- supports containerized workloads
- automates deployments
- enables horizontal scaling
- provides service discovery
- supports rolling updates
- supports health monitoring
- enables self-healing
- integrates with CI/CD
- supports secrets
- supports configuration management
- integrates with OpenTelemetry
- supports autoscaling
- supports zero-downtime deployments
- remains cloud agnostic

---

# 3. Decision Drivers

Primary decision drivers include:

1. scalability
2. resilience
3. cloud portability
4. operational maturity
5. ecosystem adoption
6. declarative infrastructure
7. automation
8. security
9. observability
10. maintainability

---

# 4. Considered Options

## 4.1 Virtual Machines

Advantages:

- mature
- familiar

Disadvantages:

- poor scalability
- manual provisioning
- limited automation
- higher operational cost

---

## 4.2 Docker Compose

Advantages:

- simple
- ideal for local development

Disadvantages:

- not intended for production orchestration
- no self-healing
- weak scaling support
- limited operational tooling

---

## 4.3 Kubernetes

Advantages:

- industry standard
- declarative
- scalable
- cloud portable
- mature ecosystem
- strong automation
- rich observability
- high availability

Disadvantages:

- operational complexity
- learning curve
- cluster administration

---

# 5. Decision

The Enterprise Order Platform adopts Kubernetes as the standard orchestration platform for production workloads.

All production services will execute inside Kubernetes clusters.

---

# 6. Rationale

Kubernetes provides:

- workload scheduling
- automatic recovery
- horizontal scaling
- service discovery
- rolling deployments
- configuration management
- secret management
- ecosystem integration

These capabilities align with enterprise operational requirements.

---

# 7. Architecture

```text
Users

↓

Ingress Controller

↓

API Gateway

↓

Microservices

↓

PostgreSQL
Redis
SQS

↓

Persistent Storage
```

---

# 8. Deployment Model

Each service is deployed independently.

Every deployment owns:

- Deployment
- Service
- ConfigMap
- Secret
- ServiceAccount
- HorizontalPodAutoscaler (when applicable)

---

# 9. Containerization

Every application is packaged as an OCI-compliant container image.

Container images must be:

- immutable
- versioned
- reproducible

---

# 10. Image Registry

Container images must be stored in an approved container registry.

Images must be:

- scanned
- signed where supported
- versioned
- immutable

---

# 11. Deployment Strategy

Preferred deployment strategy:

Rolling Update.

Supported alternatives:

- Blue/Green
- Canary

Deployment strategy depends on business criticality.

---

# 12. Rolling Updates

Rolling deployments should ensure:

- zero downtime
- gradual replacement
- readiness validation
- automatic rollback when configured

---

# 13. Health Probes

Every service must expose:

- liveness probe
- readiness probe

Startup probes should be used for slow-starting applications.

---

# 14. Liveness Probe

Liveness indicates whether the application should be restarted.

A failed liveness probe causes pod restart.

---

# 15. Readiness Probe

Readiness indicates whether the application can receive traffic.

Failed readiness removes the pod from load balancing without restarting it.

---

# 16. Startup Probe

Startup probes protect slow application initialization.

Startup failure should not trigger premature liveness failures.

---

# 17. Horizontal Scaling

Horizontal Pod Autoscaler (HPA) may scale workloads using:

- CPU
- memory
- custom metrics
- external metrics

---

# 18. Vertical Scaling

Vertical Pod Autoscaler may be evaluated separately.

Horizontal scaling remains the preferred default.

---

# 19. Resource Requests

Every workload must define:

- CPU request
- memory request

Requests support scheduling.

---

# 20. Resource Limits

Every workload must define:

- CPU limit
- memory limit

Limits prevent resource exhaustion.

---

# 21. Namespace Strategy

Namespaces separate environments and logical domains.

Example:

```text
production

staging

development

monitoring
```

---

# 22. Configuration

Application configuration uses ConfigMaps.

Configuration must remain externalized.

---

# 23. Secrets

Sensitive configuration uses Kubernetes Secrets or an approved external secret manager.

Examples:

- passwords
- API keys
- certificates
- tokens

Secrets must never be committed to source control.

---

# 24. Service Discovery

Internal communication uses Kubernetes Services.

Applications must not depend on pod IP addresses.

---

# 25. Networking

Ingress controllers expose external APIs.

Internal services communicate through cluster networking.

---

# 26. Security

Pods should execute with least privilege.

Security requirements include:

- non-root containers
- read-only root filesystem where practical
- dropped Linux capabilities
- restricted service accounts

---

# 27. Persistent Storage

Persistent volumes are used only where required.

Stateless services are preferred.

---

# 28. Stateful Workloads

Stateful infrastructure such as PostgreSQL or SQS should preferably use managed cloud services.

When self-managed, StatefulSets are required.

---

# 29. Autoscaling

Autoscaling policies must be based on meaningful metrics.

Examples:

- request rate
- CPU utilization
- queue depth
- SQS queue backlog/oldest-message age

---

# 30. Observability

Every workload integrates with:

- OpenTelemetry
- Prometheus-compatible metrics
- structured logging
- health endpoints

---

# 31. Logging

Applications write logs to stdout/stderr.

Log aggregation is handled by the platform.

Applications must not write rotating log files inside containers.

---

# 32. Scheduling

Scheduled workloads execute as Kubernetes CronJobs where appropriate.

---

# 33. CI/CD

CI pipelines build images.

CD pipelines deploy Kubernetes manifests or Helm charts.

---

# 34. Helm

Helm charts are the preferred packaging mechanism.

Charts should support:

- reusable templates
- environment overrides
- versioning

---

# 35. GitOps

GitOps deployment is recommended.

Infrastructure changes should originate from version-controlled repositories.

---

# 36. Anti-Patterns

The following are prohibited:

- mutable containers
- manual production configuration
- hardcoded secrets
- writing application state inside containers
- fixed pod IP assumptions
- root containers without justification

---

# 37. Positive Consequences

The decision provides:

- scalability
- resilience
- self-healing
- cloud portability
- deployment automation
- operational consistency

---

# 38. Negative Consequences

The decision introduces:

- operational complexity
- cluster administration
- learning curve

These costs are acceptable.

---

# 39. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| Misconfigured resources | Resource requests and limits |
| Cluster outage | High availability |
| Secret exposure | Kubernetes Secrets and RBAC |
| Deployment failures | Rolling updates and rollback |
| Scaling instability | HPA tuning |

---

# 40. Implementation Guidance

Mandatory rules:

1. Production workloads execute on Kubernetes.
2. Containers are immutable.
3. Configuration is externalized.
4. Secrets remain outside source control.
5. Health probes are mandatory.
6. Resource requests and limits are mandatory.
7. Logging uses stdout/stderr.
8. Horizontal scaling is preferred.
9. GitOps is recommended.
10. Applications remain stateless whenever practical.

---

# 41. Validation

Validation includes:

- deployment testing
- rolling-update validation
- autoscaling tests
- health probe validation
- observability verification
- disaster recovery exercises

---

# 42. Success Criteria

The decision is successful when:

- deployments occur without downtime
- services recover automatically
- scaling behaves predictably
- operational effort decreases
- platform portability is preserved

---

# 43. Related Decisions

- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-010: Use Redis for Distributed Caching
- ADR-012: Adopt the Saga Pattern for Distributed Workflows
- ADR-014: Adopt OpenTelemetry for Distributed Observability

---

# 44. References

- Kubernetes Documentation
- CNCF Landscape
- Helm Documentation
- OpenTelemetry Documentation
- Enterprise Order Platform Kubernetes Architecture

---

# 45. Review History

| Date | Reviewer | Result |
|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved |

---

# 46. Decision Summary

The Enterprise Order Platform adopts Kubernetes as its production orchestration platform.

The platform standardizes on:

- OCI container images
- Kubernetes Deployments
- Services
- ConfigMaps
- Secrets
- Horizontal Pod Autoscaler
- Ingress Controllers
- Helm packaging
- GitOps deployments
- OpenTelemetry integration

This decision establishes a resilient, cloud-portable and highly scalable runtime platform capable of supporting enterprise-grade distributed applications.

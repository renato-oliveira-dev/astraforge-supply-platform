# Kubernetes Architecture

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Kubernetes Architecture |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the Kubernetes architecture adopted by the AstraForge Supply Platform.

It establishes:

- cluster organization
- namespaces
- deployments
- services
- ingress
- autoscaling
- configuration management
- secrets
- resource allocation
- operational practices

Kubernetes is the standard platform for running all production workloads.

---

# 2. Goals

The platform must provide:

- High Availability
- Horizontal Scalability
- Fault Tolerance
- Self-Healing
- Rolling Deployments
- Operational Consistency

---

# 3. Cluster Architecture

```
Internet

↓

Ingress Controller

↓

API Gateway

↓

Microservices

↓

Amazon SQS

↓

Redis

↓

PostgreSQL
```

---

# 4. Namespace Strategy

Recommended namespaces

```
platform

infrastructure

orders

inventory

payment

shipment

customer

monitoring

logging
```

Each bounded context is isolated whenever operationally appropriate.

---

# 5. Deployment Model

Each microservice is deployed independently.

```
Deployment

↓

ReplicaSet

↓

Pods
```

Deployments are immutable.

---

# 6. Replica Strategy

Minimum replicas

```
Production

3 replicas

Development

1 replica
```

Critical services may require additional replicas.

---

# 7. Pod Design

Each pod contains:

- one application container
- optional sidecars
- shared network namespace
- ephemeral storage

Business services should avoid unnecessary sidecars.

---

# 8. Service Discovery

Internal communication uses Kubernetes Services.

Example

```
orders-service

inventory-service

payment-service
```

DNS-based discovery is preferred.

---

# 9. Ingress

External traffic enters through an Ingress Controller.

Responsibilities

- TLS termination
- routing
- authentication integration
- rate limiting
- request filtering

---

# 10. Configuration

Application configuration is provided by ConfigMaps.

Examples

- feature flags
- URLs
- timeout values
- retry policies

Configuration remains external to the image.

---

# 11. Secrets

Sensitive configuration is stored in Kubernetes Secrets or an external secret manager.

Examples

- passwords
- OAuth client secrets
- certificates
- API keys

Secrets are mounted or injected at runtime.

---

# 12. Resource Management

Every deployment defines:

- CPU request
- CPU limit
- Memory request
- Memory limit

Example

| Resource | Request | Limit |
|----------|---------|-------|
| CPU | 500m | 1000m |
| Memory | 512Mi | 1024Mi |

---

# 13. Horizontal Pod Autoscaler

Services scale automatically using HPA.

Typical metrics

- CPU utilization
- memory utilization
- custom Prometheus metrics

---

# 14. Rolling Updates

Deployment strategy

```
Rolling Update
```

Configuration

```
maxUnavailable = 0

maxSurge = 1
```

Zero-downtime deployment is the default objective.

---

# 15. Rollback

Every deployment supports rollback.

Rollback is triggered when:

- health checks fail
- startup fails
- excessive errors occur
- manual intervention is required

---

# 16. Health Probes

Every service exposes:

```
/actuator/health

/actuator/health/liveness

/actuator/health/readiness
```

Kubernetes uses these endpoints for lifecycle management.

---

# 17. PodDisruptionBudget

Critical services define PodDisruptionBudgets.

Example

```
minAvailable: 2
```

This prevents excessive simultaneous pod eviction.

---

# 18. Affinity

Recommended

Pod Anti-Affinity

Pods belonging to the same service should be distributed across nodes whenever possible.

---

# 19. Node Selection

Specialized workloads may use:

- node selectors
- taints
- tolerations
- affinity rules

General workloads remain scheduler-managed.

---

# 20. Persistent Storage

PersistentVolumes are used only when required.

Examples

- shared reports
- temporary exports
- file processing

Business data remains in PostgreSQL.

---

# 21. Networking

Internal traffic remains inside the cluster.

Network Policies should restrict communication between namespaces.

Default-deny policies are recommended.

---

# 22. Security

Pods should:

- run as non-root
- drop unnecessary Linux capabilities
- use read-only root filesystem where practical
- avoid privileged mode

Security Contexts should be explicitly defined.

---

# 23. Observability

Expose

- Prometheus metrics
- application logs
- distributed traces
- Kubernetes events

Monitoring is centralized.

---

# 24. Backup Strategy

Backups include

- manifests
- persistent volumes (when applicable)
- cluster configuration
- secrets through approved secret management processes

Application state remains external to Kubernetes.

---

# 25. Disaster Recovery

Recovery objectives

| Metric | Target |
|--------|--------|
| RPO | ≤ 5 minutes |
| RTO | ≤ 30 minutes |

Cluster recovery procedures must be periodically tested.

---

# 26. CI/CD Integration

Deployment pipeline

```
Build

↓

Tests

↓

Image Scan

↓

Image Registry

↓

Deploy

↓

Health Validation

↓

Production
```

Progressive delivery techniques may be adopted when appropriate.

---

# 27. Testing

Platform validation includes:

- deployment
- scaling
- failover
- rolling update
- rollback
- autoscaling
- node failure
- pod eviction

---

# 28. Architecture Rules

The platform:

- deploys immutable containers
- scales horizontally
- self-heals failed pods
- externalizes configuration
- isolates workloads
- supports rolling updates
- enforces health probes

---

# 29. Decision Summary

The platform adopts:

- Kubernetes
- Deployments
- ReplicaSets
- Horizontal Pod Autoscaler
- ConfigMaps
- Secrets
- Ingress
- PodDisruptionBudgets
- Anti-Affinity
- Rolling Updates
- Self-Healing

---

# 30. Next Documentation Step

Next document

```
docs/devops/ci-cd-pipeline.md
```

It will define:

- branching strategy
- pull request workflow
- automated testing
- quality gates
- artifact publication
- deployment pipeline
- environment promotion
- release strategy
- rollback process

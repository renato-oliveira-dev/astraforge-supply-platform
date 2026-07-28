# Observability

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Observability |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the observability strategy adopted by the Enterprise Order Platform.

It establishes:

- logging
- metrics
- distributed tracing
- health monitoring
- alerting
- dashboards
- SLOs
- operational visibility

Observability enables engineers to understand the internal state of the platform using external signals.

---

# 2. Observability Pillars

The platform adopts four pillars:

```
Logs

Metrics

Traces

Events
```

These signals complement one another.

---

# 3. Architecture

```
Application

↓

Logs

↓

Metrics

↓

Traces

↓

Collectors

↓

Prometheus

↓

Grafana

↓

AlertManager

↓

Operations Team
```

---

# 4. Logging Strategy

Application logs must be:

- structured
- machine readable
- JSON formatted
- timestamped
- correlated

Every log entry should contain:

- timestamp
- level
- service
- environment
- traceId
- correlationId
- requestId

---

# 5. Log Levels

| Level | Purpose |
|--------|---------|
| ERROR | Business or technical failure |
| WARN | Recoverable situation |
| INFO | Business milestones |
| DEBUG | Diagnostic information |
| TRACE | Development only |

TRACE must never be enabled in production.

---

# 6. Structured Logging

Example

```json
{
  "timestamp":"2027-01-15T12:35:18Z",
  "service":"orders-service",
  "level":"INFO",
  "traceId":"4fe72...",
  "correlationId":"e21c...",
  "event":"OrderSubmitted",
  "orderId":"..."
}
```

Structured logs simplify searching and analytics.

---

# 7. Sensitive Information

Never log:

- passwords
- tokens
- API keys
- credit card data
- authentication secrets
- personal information not required for diagnostics

Sensitive fields should be masked before logging.

---

# 8. Metrics

Expose metrics for:

- requests
- latency
- failures
- throughput
- JVM
- database
- messaging
- cache
- external integrations

---

# 9. Business Metrics

Examples

```
Orders Created

Orders Submitted

Orders Approved

Orders Cancelled

Inventory Reservations

Payments Authorized
```

Business metrics are as important as technical metrics.

---

# 10. Technical Metrics

Examples

```
HTTP Requests

Response Time

CPU

Memory

GC

Database Connections

SQS Backlog / Oldest Message Age

Redis Hit Ratio
```

---

# 11. Histograms

Latency should be exposed using histograms.

Typical buckets

```
5 ms

10 ms

25 ms

50 ms

100 ms

250 ms

500 ms

1 s

2 s

5 s
```

---

# 12. Distributed Tracing

Every request receives a TraceId.

Example

```
API

↓

Orders

↓

Inventory

↓

Payment

↓

Shipment

↓

Notification
```

The TraceId remains unchanged.

---

# 13. Correlation

Business operations also receive a CorrelationId.

```
Order

↓

CorrelationId

↓

Every Event

↓

Every Service
```

CorrelationId groups the entire business process.

---

# 14. Health Checks

Expose:

```
/actuator/health
```

Include:

- database
- Amazon SQS
- Redis
- external services
- disk
- memory

---

# 15. Readiness Probe

Ready when:

- dependencies available
- migrations complete
- consumers initialized

---

# 16. Liveness Probe

Alive when:

- application running
- JVM responsive

External dependency failures should not automatically fail liveness.

---

# 17. Dashboards

Recommended dashboards

Application

Infrastructure

Business

Messaging

Database

Cache

---

# 18. Alerting

Alerts should exist for:

- high error rate
- high latency
- consumer lag
- DLQ growth
- database unavailable
- Redis unavailable
- low disk space
- excessive retries

---

# 19. SLOs

Example

| Indicator | Target |
|-----------|--------|
| Availability | 99.9% |
| API Latency (P95) | < 300 ms |
| API Latency (P99) | < 1 s |
| Error Rate | < 1% |
| SQS Oldest Message Age | < 1 minute |

---

# 20. Error Budget

Example

Availability

```
99.9%

↓

0.1%

↓

43.2 minutes/month
```

Operational decisions should respect the error budget.

---

# 21. Dashboard Categories

Operational

Business

Infrastructure

Security

Performance

Executive

Each audience requires different visualizations.

---

# 22. Capacity Monitoring

Monitor:

- CPU
- memory
- storage
- connections
- SQS queues/message groups
- Redis memory
- PostgreSQL growth

Trend analysis should support capacity planning.

---

# 23. Incident Investigation

Investigation flow

```
Alert

↓

Dashboard

↓

Trace

↓

Logs

↓

Metrics

↓

Root Cause
```

---

# 24. Testing

Verify:

- metric publication
- log correlation
- trace propagation
- health endpoints
- alert rules
- dashboard accuracy

---

# 25. Architecture Rules

Observability:

- must not change business behavior
- must introduce minimal overhead
- must support distributed systems
- must correlate every request
- must be enabled in every environment

---

# 26. Decision Summary

The platform adopts:

- structured JSON logging
- distributed tracing
- Prometheus metrics
- Grafana dashboards
- health probes
- SLO-driven monitoring
- correlation identifiers
- business and technical metrics

---

# 27. Next Documentation Step

Next document

```
docs/infrastructure/resilience.md
```

It will define:

- Circuit Breaker
- Retry
- Timeout
- Bulkhead
- Fallback
- Rate Limiting
- Graceful Degradation
- Failure Isolation

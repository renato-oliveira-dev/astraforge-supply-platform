# Definition of Ready

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Definition of Ready |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the minimum criteria required before any work item enters implementation.

The Definition of Ready (DoR) ensures that engineers begin development with sufficient business, technical and operational clarity to deliver high-quality software with minimal rework.

It applies to:

- new features
- bug fixes
- technical improvements
- refactoring
- database changes
- API changes
- event contract changes
- infrastructure work
- security initiatives

---

# 2. Objectives

The Definition of Ready aims to ensure that every work item:

- has a clear business purpose
- has measurable acceptance criteria
- has identified technical impacts
- has known dependencies
- has identified risks
- has an agreed implementation scope
- is small enough to be completed safely
- can be tested
- can be reviewed
- can be deployed independently whenever practical

---

# 3. Core Principle

A developer should never need to guess business behavior.

Questions discovered after coding begins are significantly more expensive than questions resolved before implementation.

If critical information is missing, the work item is **Not Ready**.

---

# 4. Ready Versus Planned

A backlog item may exist without being ready.

Ready means:

- prioritized
- understandable
- technically feasible
- estimable
- implementable

Planning alone does not make an item ready.

---

# 5. Work Item Identification

Every work item must include:

- unique identifier
- title
- business objective
- owner
- priority
- affected bounded context
- work item type

Example types:

- Feature
- Bug
- Refactoring
- Technical Debt
- Spike
- Infrastructure
- Security
- Performance

---

# 6. Business Objective

The business objective must answer:

- Why does this change exist?
- Which problem does it solve?
- Who benefits?
- What business capability changes?

Example:

> Dealers must be able to approve multiple orders simultaneously to reduce approval time.

---

# 7. Business Context

The work item must describe:

- current behavior
- desired behavior
- constraints
- assumptions
- business terminology
- workflow location

Without context, engineers risk implementing technically correct but functionally incorrect solutions.

---

# 8. Acceptance Criteria

Acceptance criteria must be:

- explicit
- measurable
- testable
- complete
- unambiguous

Example:

```text
Given a pending order

When an authorized analyst approves it

Then the order status becomes APPROVED
```

---

# 9. Non-Functional Requirements

The work item should identify applicable requirements such as:

- performance
- availability
- latency
- scalability
- security
- auditability
- observability
- accessibility
- compatibility

---

# 10. Scope Definition

The scope must clearly identify:

Included:

- APIs
- services
- workflows
- database objects

Excluded:

- unrelated refactoring
- future improvements
- optional enhancements

Scope boundaries reduce unnecessary implementation growth.

---

# 11. Out of Scope

Explicitly identify work that will **not** be implemented.

Example:

```text
Email notifications are out of scope.

Reporting changes are out of scope.
```

---

# 12. Affected Bounded Context

The work item must identify the owning bounded context.

Examples:

- Orders
- Inventory
- Payments
- Customers
- Notifications

Cross-context changes require additional review.

---

# 13. Affected Services

List affected services.

Example:

```text
order-service

inventory-service

notification-service
```

---

# 14. API Impact

Identify whether APIs are:

- unchanged
- extended
- deprecated
- versioned
- removed

Breaking API changes require explicit approval.

---

# 15. Database Impact

Identify whether database changes include:

- new tables
- new columns
- indexes
- constraints
- data migration
- Flyway migration

Applied migrations must never be modified.

---

# 16. Messaging Impact

Identify:

- new events
- modified events
- new consumers
- new producers
- queue changes
- fan-out/routing changes
- ordering impact
- replay impact

---

# 17. Security Impact

Determine whether the work affects:

- authentication
- authorization
- scopes
- permissions
- tenant isolation
- secrets
- sensitive data
- audit

Security-sensitive work may require dedicated review.

---

# 18. Performance Impact

Identify expected impact on:

- database
- caching
- messaging
- concurrency
- latency
- throughput

Performance-sensitive work should define expected targets.

---

# 19. Observability Impact

Determine whether new:

- logs
- metrics
- traces
- dashboards
- alerts
- audit events

must be created.

---

# 20. Data Ownership

The work item must identify:

- owning aggregate
- owning service
- source of truth
- affected repositories

No feature should introduce ambiguous ownership.

---

# 21. Business Rules

All known business rules must be documented.

Avoid relying on:

- tribal knowledge
- previous conversations
- assumptions
- undocumented behavior

---

# 22. Edge Cases

Known edge cases should already be identified.

Examples:

- duplicate requests
- concurrent updates
- cancelled orders
- expired approvals
- invalid states

---

# 23. Error Scenarios

Known failure scenarios should be described.

Examples:

- customer not found
- inventory unavailable
- timeout
- authorization denied
- invalid transition

---

# 24. Dependencies

Identify:

- external APIs
- other teams
- infrastructure
- feature flags
- configuration
- credentials

Blocked dependencies prevent readiness.

---

# 25. Contract Ownership

Public contracts must identify:

- owner
- consumers
- compatibility requirements
- version strategy

---

# 26. Test Strategy

Before implementation, define how the change will be validated.

Possible tests:

- unit
- integration
- contract
- architecture
- security
- performance

---

# 27. Rollback Strategy

High-risk work should identify rollback expectations before development starts.

Consider:

- database
- messaging
- deployment
- feature flags
- compatibility

---

# 28. Feature Flags

If feature flags are required, identify:

- owner
- rollout plan
- removal plan

---

# 29. Risk Assessment

Identify risks such as:

- breaking changes
- downtime
- data migration
- security
- concurrency
- operational complexity

---

# 30. Estimation

A ready work item should be estimable.

Large unknowns usually indicate insufficient refinement.

---

# 31. Definition of Small

Whenever practical, work items should:

- fit into one iteration
- have one primary objective
- avoid unrelated technical changes
- minimize deployment risk

---

# 32. Ready Checklist

Before implementation begins, verify:

- Business objective is clear
- Acceptance criteria exist
- Scope is defined
- Out-of-scope items are listed
- Bounded context is identified
- APIs are analyzed
- Database impact is known
- Messaging impact is known
- Security impact is known
- Performance impact is known
- Observability impact is known
- Dependencies are identified
- Risks are identified
- Test strategy exists
- Rollback strategy exists (when applicable)
- Documentation impact is known
- Work is estimable
- Product owner questions are resolved
- Architecture questions are resolved

---

# 33. Blocking Conditions

A work item is **Not Ready** if any of the following apply:

- unclear business objective
- missing acceptance criteria
- unknown owner
- unknown affected service
- unknown API impact
- unknown migration strategy
- unresolved architecture decision
- unresolved dependency
- unresolved security requirement
- unknown deployment strategy
- impossible estimation

---

# 34. Roles

## Product Owner

Responsible for:

- business value
- acceptance criteria
- prioritization

## Architect

Responsible for:

- architectural impact
- bounded context
- contracts
- technical risks

## Engineering

Responsible for:

- implementation feasibility
- estimates
- technical questions
- testing approach

---

# 35. Architecture Rules

A work item is Ready only when:

- business purpose is understood
- architecture impact is understood
- ownership is defined
- contracts are identified
- risks are visible
- implementation can begin without critical assumptions

---

# 36. Decision Summary

The Enterprise Order Platform considers a work item **Ready** only when:

- business value is clear
- acceptance criteria are complete
- scope is understood
- architecture impact is identified
- APIs and events are analyzed
- database impact is known
- security impact is known
- observability impact is planned
- dependencies are identified
- testing strategy exists
- rollout considerations are understood
- implementation can proceed without unresolved critical questions

The Definition of Ready is the engineering agreement that enables predictable, high-quality implementation with minimal rework.

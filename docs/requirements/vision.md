# Project Vision

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Project Vision |
| Status | Approved |
| Version | 2.0.0 |
| Author | Renato Oliveira |

## 1. Purpose

AstraForge Supply Platform is an independently created, fictional reference implementation for industrial procurement and supply orchestration.

It demonstrates how complex enterprise processes can be modeled with Java, Spring Boot, Domain-Driven Design, Clean Architecture, event-driven integration, automated testing, and production-oriented engineering practices.

The project does not reproduce proprietary source code, private documentation, production data, confidential terminology, internal endpoint contracts, or employer-specific business rules.

## 2. Fictional Business Context

AstraForge serves a fictional network of industrial facilities that acquire maintenance, safety, tooling, and replacement supplies from multiple suppliers.

Procurement involves more than storing a purchase order. A request may require:

- requesting-organization and facility eligibility checks;
- catalog-item and supplier validation;
- price, tax, freight, and landed-cost calculation;
- budget and approval-policy evaluation;
- stock allocation or supplier sourcing;
- fulfillment and shipment planning;
- invoice reconciliation;
- audit registration;
- notification and asynchronous integration.

## 3. Product Vision

For facility requesters, procurement analysts, approvers, warehouse operators, and finance teams, AstraForge is a supply-orchestration platform that coordinates a controlled path from requisition to purchase order, fulfillment, and reconciliation.

Unlike a CRUD demonstration, it models invariants, policy decisions, concurrency, auditability, failure recovery, and integration boundaries as explicit architectural concerns.

## 4. Objectives

- Model an original industrial procurement domain
- Keep business rules independent from frameworks
- Apply explicit bounded contexts and dependency direction
- Support synchronous and asynchronous integration
- Preserve traceability and operational reliability
- Apply automated testing and architecture fitness functions
- Maintain documented provenance and IP-safe contribution rules
- Provide a reproducible local development environment

## 5. Primary Actors

### Facility Requester
Creates requisitions and tracks their processing.

### Procurement Analyst
Reviews sourcing, commercial conditions, and supplier options.

### Approver
Evaluates requests according to budget, category, risk, and delegation policies.

### Warehouse Operator
Allocates stock and coordinates internal fulfillment.

### Finance Analyst
Reconciles invoices, charges, and purchase-order totals.

### Platform Administrator
Manages access, configuration, audit access, and operational support.

### External Systems
May include fictional supplier gateways, inventory systems, identity providers, shipping services, and notification channels.

## 6. Initial Scope

- Authentication and authorization
- Organization and facility reference management
- Supplier and catalog reference management
- Requisition creation and item management
- Price and landed-cost calculation
- Requisition validation and submission
- Approval-policy evaluation
- Purchase-order creation and lifecycle
- Inventory allocation
- Shipment preparation
- Invoice-reconciliation status
- Audit trail
- Event publishing
- Notification requests

## 7. Out of Scope

- Real payment processing
- Real tax or fiscal-document generation
- Integration with a named commercial ERP
- Production supplier credentials
- Production carrier integration
- Employer-specific rules or data
- Marketplace settlement
- Machine-learning recommendations

## 8. Core Lifecycle

```text
REQUISITION_DRAFT
        |
        v
SUBMITTED
        |
        +--> REJECTED
        |
        v
UNDER_REVIEW
        |
        +--> CHANGES_REQUESTED
        |
        v
APPROVED
        |
        v
PURCHASE_ORDER_CREATED
        |
        v
ALLOCATED_OR_SOURCED
        |
        v
FULFILLED
        |
        v
RECONCILED
```

The exact state model will evolve through implementation and tests without importing rules from any external proprietary system.

## 9. Success Criteria

The project succeeds when it provides:

- an original and coherent domain model;
- enforceable architecture boundaries;
- production-oriented quality and delivery controls;
- traceable ADR-to-code implementation;
- meaningful automated tests;
- clear evidence that all portfolio content is fictional and independently authored.

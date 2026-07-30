# Domain Overview

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Domain Overview |
| Status | Approved |
| Version | 1.0.0 |
| Author | Renato Oliveira |

## 1. Domain Summary

AstraForge coordinates industrial supply procurement across fictional organizations, facilities, warehouses, and suppliers.

The core business flow starts with a **Requisition**. After validation and policy approval, an accepted requisition may create a **Purchase Order**. Stock can be allocated from an internal warehouse or sourced from a supplier. Fulfillment, shipment, and invoice reconciliation complete the lifecycle.

## 2. Core Domain

### Procurement Orchestration

Procurement Orchestration owns the business decisions that move a request from intent to an authorized purchase commitment.

Responsibilities include:

- maintaining requisition consistency;
- evaluating submission readiness;
- coordinating approval requirements;
- converting approved requisitions into purchase orders;
- controlling purchase-order state transitions;
- preserving monetary and catalog snapshots;
- emitting domain events.

The initial aggregate candidates are **Requisition** and **PurchaseOrder**. Their final boundaries will be validated during implementation.

## 3. Supporting Domains

### Approval Policy
Determines approval requirements from amount, category, facility, budget, and risk attributes.

### Sourcing
Chooses between warehouse allocation and supplier purchasing.

### Inventory Allocation
Reserves, confirms, and releases stock without duplicate effects.

### Pricing and Landed Cost
Calculates unit prices, discounts, taxes, freight, fees, and final totals.

### Fulfillment
Coordinates picking, packing, dispatch, delivery status, and operational exceptions.

### Invoice Reconciliation
Compares invoice expectations with purchase-order and receipt information.

## 4. Generic Subdomains

- Identity and access management
- Organization and facility reference data
- Supplier reference data
- Industrial catalog
- Notifications
- Audit trail
- Observability
- Integration-event publication

## 5. Primary Actors

- Facility Requester
- Procurement Analyst
- Approver
- Warehouse Operator
- Finance Analyst
- Platform Administrator
- External Integration

## 6. Initial Bounded Contexts

```text
Procurement
  |-- Requisition
  |-- Purchase Order
  |-- Approval Policy

Supply
  |-- Catalog
  |-- Supplier
  |-- Inventory Allocation
  |-- Fulfillment

Finance
  |-- Pricing
  |-- Landed Cost
  |-- Invoice Reconciliation

Platform
  |-- Identity
  |-- Audit
  |-- Notification
  |-- Integration Events
```

## 7. Domain Independence

The domain vocabulary, examples, rules, identifiers, and datasets are fictional and independently authored for this repository. No real company is represented by AstraForge, Forge & Field, or any other example name used in the documentation.

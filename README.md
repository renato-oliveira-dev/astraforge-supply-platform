# Enterprise Order Platform

Enterprise-grade B2B Order Management Platform built with **Java 21**, **Spring Boot**, **Clean Architecture**, **Domain-Driven Design (DDD)**, and modern software engineering practices.

> A production-oriented backend project designed to demonstrate enterprise architecture, distributed systems, software quality, and modern Java development.

---

## Project Status

**Current Phase:** 🏗️ Architecture & Planning

The project follows an **Architecture First** approach.

Before writing production code, the complete business domain, functional requirements, non-functional requirements, architectural decisions, and domain model are documented.

This mirrors the software engineering process adopted by many enterprise organizations.

---

# Why This Project?

Most portfolio projects demonstrate how to build CRUD applications.

This project demonstrates how to design, document, and implement a production-grade enterprise backend system using modern engineering practices.

The objective is to showcase not only coding skills, but also:

- Software Architecture
- Domain Modeling
- Engineering Decision Making
- Scalability
- Maintainability
- Reliability
- Security
- Observability

---

# Business Overview

Enterprise Order Platform is a fictional B2B platform responsible for managing the complete lifecycle of enterprise orders.

The platform models realistic business scenarios including:

- Customer validation
- Product validation
- Pricing
- Inventory reservation
- Approval workflows
- Order lifecycle
- Event publishing
- Audit history
- Notifications
- External integrations

Although fictional, every business rule is inspired by real enterprise systems.

---

# Main Goals

- Demonstrate enterprise backend engineering
- Apply Java 21 and Spring Boot
- Apply Domain-Driven Design
- Implement Clean Architecture
- Build scalable REST APIs
- Apply Event-Driven Architecture
- Implement production-quality testing
- Document architectural decisions
- Design resilient integrations
- Build CI/CD pipelines
- Demonstrate production-ready engineering practices

---

# Planned Business Capabilities

- Customer Management
- Product Catalog
- Order Management
- Approval Workflow
- Pricing Engine
- Inventory Reservation
- Shipment Management
- Payment Tracking
- Notifications
- Audit Trail
- Event Publishing

---

# Technology Stack

## Backend

- Java 21
- Spring Boot 3
- Gradle

## Persistence

- PostgreSQL
- Redis
- Flyway

## Messaging

- Amazon SQS
- Transactional Outbox Pattern

## Security

- Spring Security
- JWT Authentication
- Role-Based Access Control (RBAC)

## Testing

- JUnit 5
- Mockito
- AssertJ
- Testcontainers
- JaCoCo
- ArchUnit

## DevOps

- Docker
- Docker Compose
- GitHub Actions

## Documentation

- OpenAPI
- ADR (Architecture Decision Records)
- Markdown
- Mermaid Diagrams

## Code Quality

- SonarQube / SonarCloud
- Spotless
- Dependency Scanning

---

# Architectural Direction

The project starts as a **Modular Monolith**.

The architecture is intentionally designed to evolve into independently deployable microservices if business requirements demand it.

```
                        Clients
                           │
                           ▼
                    REST Controllers
                           │
                           ▼
                   Application Layer
                 (Use Cases / Services)
                           │
                           ▼
                     Domain Layer
        (Entities • Aggregates • Value Objects)
                           │
               ┌───────────┴───────────┐
               ▼                       ▼
        Outbound Ports          Domain Events
               │
               ▼
      Infrastructure Adapters
      ├── PostgreSQL
      ├── Redis
      ├── Amazon SQS
      └── External APIs
```

### Architectural Principles

- Dependency Inversion
- SOLID
- Domain-Driven Design
- Clean Architecture
- Hexagonal Architecture concepts
- Explicit Business Rules
- Event-Driven Integration

The Domain Layer remains completely independent of Spring, databases, messaging frameworks, and infrastructure concerns.

---

# Documentation

Project documentation is maintained under the `docs` directory.

```
docs
├── adr
├── architecture
├── domain
└── requirements
```

## Available Documentation

- ✅ Project Vision
- ✅ Functional Requirements
- ✅ Non-Functional Requirements

## Planned Documentation

- Domain Overview
- Ubiquitous Language
- Bounded Contexts
- Context Map
- Domain Model
- Event Catalog
- API Design
- Architecture Decision Records
- Security Architecture
- Testing Strategy
- Deployment Guide
- Local Development Guide

---

# Engineering Principles

This project follows a set of engineering principles inspired by production enterprise systems.

- Business Rules First
- Architecture Before Frameworks
- Explicit Domain Language
- Immutable Database Migrations
- High Testability
- Idempotent Operations
- Eventual Consistency
- Secure by Design
- Observable by Default
- Low Coupling
- High Cohesion
- Continuous Quality

---

# Quality Targets

| Metric | Target |
|---------|---------|
| Overall Coverage | ≥ 85% |
| New Code Coverage | ≥ 90% |
| Critical Domain Rules | 100% |
| Blocker Issues | 0 |
| Critical Issues | 0 |
| Confirmed Vulnerabilities | 0 |
| Code Duplication | < 3% |
| Sonar Maintainability | A |
| Sonar Reliability | A |
| Sonar Security | A |

---

# Planned Repository Structure

```
enterprise-order-platform
│
├── docs
│   ├── adr
│   ├── architecture
│   ├── domain
│   └── requirements
│
├── src
│   ├── main
│   └── test
│
├── .github
│   └── workflows
│
├── compose.yaml
├── Dockerfile
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── LICENSE
└── README.md
```

---

# Development Roadmap

## Phase 1 — Architecture

- [x] Vision
- [x] Functional Requirements
- [x] Non-Functional Requirements
- [ ] Domain Model
- [ ] Bounded Contexts
- [ ] Context Map
- [ ] Architecture Decisions

---

## Phase 2 — Foundation

- [ ] Spring Boot Project
- [ ] PostgreSQL
- [ ] Flyway
- [ ] Docker
- [ ] Docker Compose
- [ ] Gradle
- [ ] CI Pipeline

---

## Phase 3 — Core Domain

- [ ] Customers
- [ ] Products
- [ ] Orders
- [ ] Pricing
- [ ] Inventory
- [ ] Audit

---

## Phase 4 — Enterprise Features

- [ ] Approval Workflow
- [ ] Domain Events
- [ ] Transactional Outbox
- [ ] Amazon SQS Integration
- [ ] Notifications

---

## Phase 5 — Reliability

- [ ] Redis Cache
- [ ] Retry Policies
- [ ] Circuit Breakers
- [ ] Structured Logging
- [ ] Correlation IDs
- [ ] Metrics
- [ ] Distributed Tracing

---

## Phase 6 — Production Readiness

- [ ] Performance Tests
- [ ] Security Hardening
- [ ] Deployment Guide
- [ ] Cloud Architecture
- [ ] Kubernetes Evaluation

---

# Design Philosophy

The goal of this repository is not to demonstrate how to use Spring Boot.

The goal is to demonstrate how experienced software engineers design systems that remain maintainable as complexity grows.

Every architectural decision will be documented.

Every business rule will be explicit.

Every trade-off will be justified.

---

# Disclaimer

This is a fictional project created exclusively for educational, architectural, and professional portfolio purposes.

It contains **no proprietary code, business rules, documentation, credentials, data, or intellectual property** from any current or former employer or client.

---

# License

Licensed under the **Apache License 2.0**.

---

# Author

## Renato Oliveira

Senior Software Engineer

Backend Engineering • Java • Spring Boot • Distributed Systems • Clean Architecture • Domain-Driven Design • Software Modernization

---

⭐ If you're interested in enterprise backend engineering, feel free to follow the project's evolution.

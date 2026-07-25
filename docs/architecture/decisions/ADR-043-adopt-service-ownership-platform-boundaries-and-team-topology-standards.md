# ADR-043: Adopt Service Ownership, Platform Boundaries and Team Topology Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-043 |
| Title | Adopt Service Ownership, Platform Boundaries and Team Topology Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Service Ownership, Team Topologies, Platform Engineering, Domain Boundaries |
| Related Work Items | CODEOWNERS, Service Catalog, Platform Engineering, API Ownership, Kafka Ownership, Data Ownership |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The platform consists of multiple independently deployable services and shared infrastructure capabilities.

Typical topology:

```text
                   DIGITAL PLATFORM
                          |
       +------------------+------------------+
       |                  |                  |
       v                  v                  v
     CART              ORDERS            CUSTOMERS
       |                  |                  |
       +------------------+------------------+
                          |
                 PLATFORM CAPABILITIES
                          |
       +------------------+------------------+
       |                  |                  |
       v                  v                  v
     KAFKA             DATABASE          SECURITY
       |                  |                  |
       +------------------+------------------+
                          |
                         AWS
```

As the number of services grows, unclear ownership creates systemic problems.

Examples:

```text
Who owns this API?

Who approves a breaking contract change?

Who owns this Kafka topic?

Who investigates this incident?

Who owns this database schema?

Who upgrades this dependency?

Who can decommission this service?

Who maintains this shared library?
```

Without explicit answers, "shared ownership" frequently becomes:

```text
Everyone owns it
      |
      v
Nobody owns it
```

The platform therefore requires explicit ownership boundaries.

---

# 2. Problem Statement

The platform requires standards defining:

- service ownership
- domain ownership
- API ownership
- database ownership
- Kafka topic ownership
- event ownership
- shared-library ownership
- infrastructure ownership
- platform-team responsibilities
- product/domain-team responsibilities
- CODEOWNERS
- service catalog
- operational ownership
- incident ownership
- security ownership
- dependency ownership
- decommissioning ownership
- cross-team collaboration
- Team Topologies
- cognitive load
- platform boundaries
- ownership transfer

---

# 3. Decision Drivers

Primary drivers are:

1. accountability
2. clear decision authority
3. faster incident response
4. domain autonomy
5. reduced coordination overhead
6. controlled service boundaries
7. scalable engineering organization
8. clear operational responsibility
9. safe contract evolution
10. reduced orphaned systems
11. sustainable cognitive load
12. platform standardization

---

# 4. Decision

Every production service and significant shared technical asset must have explicit ownership.

The canonical model is:

```text
                 BUSINESS DOMAIN
                       |
                       v
               STREAM-ALIGNED TEAM
                       |
                       v
                  SERVICE(S)
                       |
          +------------+------------+
          |            |            |
          v            v            v
         API          DATA        EVENTS
          |            |            |
          +------------+------------+
                       |
                       v
                 TEAM OWNERSHIP
                       |
                       v
                    OPERATE
```

Platform capabilities are owned separately:

```text
               PLATFORM TEAM
                     |
        +------------+------------+
        |            |            |
        v            v            v
      CI/CD       SECURITY     OBSERVABILITY
        |            |            |
        +------------+------------+
                     |
                     v
                 PAVED ROAD
                     |
                     v
             PRODUCT/DOMAIN TEAMS
```

---

# 5. Fundamental Principle

The primary ownership rule is:

```text
Every production capability
must have an identifiable owner.
```

---

# 6. Ownership Is Accountability

Ownership means responsibility for the capability's lifecycle.

It does not mean the owning team must personally implement every supporting infrastructure component.

---

# 7. Service Ownership

Every independently deployable production service requires one primary owning team.

---

# 8. Primary Owner

The primary owner is accountable for:

- service architecture
- application code
- business behavior
- tests
- API contracts
- events produced
- events consumed
- persistence model
- dependencies
- security integration
- observability
- production behavior
- technical debt
- lifecycle

---

# 9. Secondary Contributors

Other teams may contribute code.

Contribution does not automatically transfer ownership.

---

# 10. Shared Contribution

The model is:

```text
CONTRIBUTORS
     |
     v
SERVICE REPOSITORY
     |
     v
PRIMARY OWNER
```

---

# 11. Multiple Owners

Multiple teams may collaborate on a service, but one accountable primary owner must remain identifiable.

---

# 12. Ownership Matrix

A service catalog should make ownership discoverable.

Example:

| Capability | Primary Owner | Supporting Owner |
|---|---|---|
| Cart Service | Commerce Team | Platform Team |
| Orders Service | Order Management Team | Platform Team |
| Customers Service | Customer Domain Team | Platform Team |
| Kafka Platform | Platform Team | Cloud Team |
| CI/CD Platform | Platform Team | DevSecOps |
| Identity Platform | Security/Platform | Domain Teams |

Exact team names depend on organizational structure.

---

# 13. Service Catalog

Production services should be registered in a discoverable service catalog or equivalent inventory.

---

# 14. Service Catalog Metadata

The catalog should contain applicable:

```text
Service Name

Description

Business Domain

Repository

Owning Team

Technical Contact

Operational Contact

Production URL / Endpoint

Documentation

Runbook

Dashboard

SLO

Criticality

Dependencies

Data Stores

Kafka Topics
```

---

# 15. Repository Ownership

Every active repository requires an owner.

---

# 16. CODEOWNERS

Repositories should use `CODEOWNERS` or an equivalent mechanism where supported.

---

# 17. CODEOWNERS Purpose

`CODEOWNERS` provides review routing and ownership visibility.

It does not replace team responsibility.

---

# 18. Example

Conceptually:

```text
*                              @orders-team

/src/main/java/.../security/   @orders-team @security-team

/db/migration/                 @orders-team @database-reviewers

/.github/workflows/            @orders-team @platform-team
```

Exact syntax depends on repository hosting.

---

# 19. Sensitive Areas

Higher-risk areas may require additional review ownership.

Examples:

```text
Security

Database migrations

CI/CD

Infrastructure

Public API contracts

Authentication/Authorization
```

---

# 20. CODEOWNERS Is Not Architecture

Review routing alone does not define architectural boundaries.

It complements ADRs and fitness functions.

---

# 21. Domain Ownership

Services should align with business capabilities rather than arbitrary technical layers.

---

# 22. Preferred Boundary

Prefer:

```text
ORDER MANAGEMENT TEAM
        |
        +--> Orders Service
        +--> Order Workflow
        +--> Order Events
```

over organizational structures such as:

```text
Controller Team

Database Team

Java Team
```

for end-to-end product ownership.

---

# 23. Stream-Aligned Team

A stream-aligned team owns a meaningful flow of business value.

---

# 24. End-to-End Responsibility

Where organizationally feasible, the team should be able to:

```text
DESIGN
  |
  v
BUILD
  |
  v
TEST
  |
  v
DEPLOY
  |
  v
OPERATE
  |
  v
IMPROVE
```

its service.

---

# 25. "You Build It, You Run It"

The platform adopts the principle in a bounded form.

---

# 26. Meaning

The team producing application behavior remains responsible for understanding and supporting that behavior in production.

---

# 27. What It Does Not Mean

It does not mean every domain team must independently become expert in:

```text
Kubernetes internals

Cloud networking

Kafka cluster administration

Certificate infrastructure

CI/CD platform internals
```

---

# 28. Shared Platform Responsibility

Infrastructure expertise belongs to platform/specialist teams where appropriate.

---

# 29. Responsibility Split

The preferred model is:

```text
DOMAIN TEAM
   |
   +--> Application behavior
   +--> Business correctness
   +--> Service architecture
   +--> Service observability
   +--> Application incidents

PLATFORM TEAM
   |
   +--> Runtime platform
   +--> Deployment platform
   +--> Shared observability platform
   +--> Shared security mechanisms
   +--> Infrastructure abstractions
```

---

# 30. Platform Team

The platform team provides reusable capabilities that reduce domain-team cognitive load.

---

# 31. Platform as Product

Internal platform capabilities should be treated as products.

---

# 32. Platform Consumers

Domain/product teams are customers of the internal platform.

---

# 33. Platform Usability

A technically powerful platform that requires constant platform-team intervention does not provide sufficient self-service.

---

# 34. Self-Service

Preferred platform capabilities are self-service.

Examples:

```text
Service Bootstrap

CI Pipeline

Deployment

Secret Integration

Observability

Standard Dashboards

Security Configuration
```

---

# 35. Paved Road

The platform should provide a recommended implementation path.

---

# 36. Paved Road Objective

The easiest path should normally also be:

```text
Secure

Observable

Testable

Deployable

Supported
```

---

# 37. Paved Road vs Mandatory Road

Not every platform recommendation must be mandatory.

---

# 38. Mandatory Platform Constraint

Constraints may become mandatory when required by:

- security
- compliance
- operational safety
- interoperability
- enterprise governance

---

# 39. Platform Escape Hatch

Legitimate deviations may be allowed through the architecture-exception process.

---

# 40. Platform Team Anti-Pattern

The platform team must not become a ticket queue required for every domain deployment.

---

# 41. Domain Team Anti-Pattern

Domain teams must not bypass platform standards simply to avoid learning supported mechanisms.

---

# 42. Cognitive Load

Team architecture must consider cognitive load.

---

# 43. Excessive Cognitive Load

A team owning too many unrelated technologies/services eventually loses effective ownership.

---

# 44. Cognitive Load Signal

Potential indicators include:

```text
Too many services per team

Too many unrelated domains

Frequent ownership confusion

Slow incident diagnosis

Large technology diversity

High cross-team dependency
```

---

# 45. Service Count Is Not Absolute

There is no universal correct number of services per team.

Complexity matters more than raw service count.

---

# 46. Microservice Boundary

Do not create a microservice merely to create another independently owned repository.

---

# 47. Boundary Criteria

A service boundary should consider:

- business capability
- data ownership
- change cadence
- scalability
- security
- operational independence
- team ownership

---

# 48. Team Boundary and Service Boundary

Organizational and service boundaries influence each other.

---

# 49. Conway's Law

System architecture tends to reflect organizational communication structures.

---

# 50. Inverse Conway Maneuver

Team boundaries may intentionally be designed to encourage the desired architecture.

---

# 51. Avoid Distributed Monolith

A distributed system where every change requires coordinated deployment across many teams has weak service autonomy.

---

# 52. Distributed Monolith Signal

Examples:

```text
Orders cannot deploy unless Cart deploys

Customers schema change requires five services simultaneously

Shared database tables modified by several teams

One release train for independent services
```

---

# 53. Service Autonomy

Services should be independently deployable within contract compatibility constraints.

---

# 54. API Ownership

The service exposing an API owns its provider contract.

---

# 55. API Owner Responsibilities

The provider owns:

- OpenAPI contract
- compatibility
- versioning
- deprecation
- error contract
- availability
- documentation

---

# 56. Consumer Responsibility

Consumers own correct integration with the published contract.

---

# 57. Provider Cannot Ignore Consumers

Provider ownership does not grant unrestricted authority to introduce breaking changes.

---

# 58. Contract Collaboration

Breaking contract evolution requires consumer impact analysis.

---

# 59. Consumer Inventory

Critical APIs should maintain discoverability of known consumers where practical.

---

# 60. Consumer-Driven Contracts

Consumer-driven contract testing may be used where it materially improves integration safety.

---

# 61. Database Ownership

Each service owns its persistence boundary.

---

# 62. Service-Owned Database

Preferred model:

```text
ORDERS SERVICE
      |
      v
 ORDERS DATA
```

---

# 63. Cross-Service Direct SQL

Another service must not directly query or modify private Orders tables as its normal integration mechanism.

---

# 64. Why

Direct cross-service database access creates:

- schema coupling
- security coupling
- deployment coupling
- ownership ambiguity

---

# 65. Shared Database Infrastructure

Multiple services may use the same PostgreSQL cluster/platform while maintaining logical ownership boundaries.

---

# 66. Infrastructure vs Data Ownership

The database platform team may own:

```text
PostgreSQL Cluster

Backup Platform

Replication

Patching
```

while the domain team owns:

```text
Schema

Tables

Indexes

Queries

Migrations

Business Data Semantics
```

---

# 67. Migration Ownership

Flyway migrations belong to the service owning the schema.

---

# 68. Cross-Team Migration

Changes affecting another team's data boundary require explicit collaboration.

---

# 69. Applied Migration Rule

Ownership transfer never authorizes rewriting applied Flyway history.

---

# 70. Data Ownership

A business data concept should have an authoritative source.

---

# 71. Source of Truth

For each critical entity, the platform should identify the system of record.

---

# 72. Example

Conceptually:

```text
Customer Master
      |
      v
Customers Service
```

Other services may hold:

```text
Snapshot

Cache

Read Model
```

but not silently become competing masters.

---

# 73. Data Duplication

Distributed systems may legitimately duplicate data.

---

# 74. Duplication vs Ownership

Duplicated data does not imply duplicated authority.

---

# 75. Local Snapshot

A service may own its local snapshot while another service remains authoritative for the source business concept.

---

# 76. Kafka Topic Ownership

Every production Kafka topic requires ownership.

---

# 77. Topic Owner

The topic/event owner is generally the domain team responsible for the event's business meaning.

---

# 78. Platform Kafka Ownership

The platform team owns the Kafka infrastructure.

It does not automatically own every business event.

---

# 79. Distinction

```text
PLATFORM TEAM
     |
     v
Kafka Cluster

DOMAIN TEAM
     |
     v
OrderCreated Event
```

---

# 80. Event Contract Ownership

The producing domain owns the event contract.

---

# 81. Producer Responsibility

The producer owns:

- event semantics
- schema
- compatibility
- documentation
- publication reliability

---

# 82. Consumer Responsibility

Consumers own:

- idempotency
- retry behavior
- failure handling
- compatibility with supported event versions

---

# 83. Topic Configuration

Kafka topic configuration responsibilities should be explicit between platform and domain teams.

---

# 84. Shared Topic Anti-Pattern

A generic topic carrying unrelated business events from unrelated domains should be avoided.

---

# 85. Topic Naming

Topic names should communicate domain ownership.

---

# 86. Event Discoverability

Important events should be discoverable through schema/event catalogs where available.

---

# 87. Shared Library Ownership

Every shared library requires an owner.

---

# 88. Shared Library Responsibilities

The owner maintains:

- compatibility
- versioning
- security updates
- release process
- documentation

---

# 89. Orphan Library

A shared dependency without an active owner should not remain a critical platform dependency indefinitely.

---

# 90. Shared Domain Library

Shared domain libraries across independently owned services are discouraged.

---

# 91. Platform Library

Stable technical libraries may be platform-owned.

Examples:

```text
Logging conventions

Security integration

Testing support

Architecture tests
```

---

# 92. Version Independence

Domain services must retain controlled adoption of shared-library versions.

---

# 93. Forced Coupled Upgrade

A shared library should not force all services into synchronized releases without strong justification.

---

# 94. Dependency Ownership

Application teams own the dependencies they introduce into their services.

---

# 95. Platform BOM

A platform/BOM may provide approved versions.

---

# 96. BOM Ownership

The team publishing the BOM owns:

- version compatibility
- dependency recommendations
- security updates
- release notes

---

# 97. Application Responsibility

Using a platform BOM does not eliminate the application's responsibility to validate its build and behavior.

---

# 98. Security Ownership

Security follows shared responsibility.

---

# 99. Security Team

Security/platform teams own:

- enterprise policies
- common security controls
- scanning infrastructure
- identity platform
- security guidance

---

# 100. Domain Team

Domain teams own:

- authorization correctness
- secure application behavior
- dependency remediation
- secure coding
- secrets usage
- vulnerability remediation in owned services

---

# 101. Security Cannot Be Outsourced

Application security cannot be delegated entirely to a security team.

---

# 102. Observability Ownership

Platform teams may own observability infrastructure.

Domain teams own useful instrumentation for their services.

---

# 103. Example

```text
PLATFORM
  |
  +--> Log ingestion
  +--> Metrics backend
  +--> Dashboard platform

ORDERS TEAM
  |
  +--> Meaningful Orders logs
  +--> Orders metrics
  +--> Orders alerts
  +--> Orders SLO
```

---

# 104. Alert Ownership

Every production alert must route to an accountable owner.

---

# 105. Orphan Alert

An alert without an owner is operational noise.

---

# 106. Incident Ownership

The service owner is the default application responder for incidents involving that service.

---

# 107. Platform Incident

Platform incidents require platform-team participation.

---

# 108. Cross-Service Incident

Major incidents may require multiple teams.

An Incident Commander coordinates rather than attempting to transfer all responsibility to one team.

---

# 109. Incident Responsibility

Ownership should answer:

```text
Who investigates first?
```

not:

```text
Who can we blame?
```

---

# 110. SLO Ownership

The service-owning team owns implementation and monitoring of the service SLO.

---

# 111. Business SLO Agreement

SLO targets should be agreed with relevant business/product stakeholders where business criticality determines the target.

---

# 112. Production Access

Ownership requires sufficient approved access to diagnose production behavior.

---

# 113. Access Principle

Access must still follow:

- least privilege
- auditability
- security policy

---

# 114. Dependency Relationship

Service ownership requires understanding upstream and downstream dependencies.

---

# 115. Dependency Map

Critical services should maintain a dependency map.

---

# 116. Upstream

An upstream dependency is something the service requires.

---

# 117. Downstream Consumer

A downstream consumer depends on the service's output/API/event.

---

# 118. Change Impact

Material contract changes require impact analysis across these relationships.

---

# 119. Cross-Team Dependency

Cross-team dependencies should use explicit contracts.

---

# 120. Informal Coupling

Avoid integration based on undocumented assumptions between developers on different teams.

---

# 121. Collaboration Modes

Following Team Topologies concepts, collaboration may use:

```text
COLLABORATION

X-AS-A-SERVICE

FACILITATING
```

---

# 122. Collaboration

Two teams may collaborate intensively for a limited period to solve a complex problem.

---

# 123. X-as-a-Service

Stable platform capabilities should generally evolve toward X-as-a-Service/self-service interaction.

---

# 124. Facilitating

A specialist team may temporarily help another team acquire capability.

---

# 125. Permanent Dependency

Facilitating relationships should not become permanent hidden dependencies.

---

# 126. Enabling Team

An enabling team helps stream-aligned teams develop capabilities.

---

# 127. Enabling Team Does Not Own Product Forever

After knowledge transfer, the product/domain team retains its ownership.

---

# 128. Complicated Subsystem Team

A specialized team may own a subsystem requiring deep expertise.

Examples may include:

```text
Optimization Engine

Advanced Pricing Engine

Specialized Cryptography
```

when justified.

---

# 129. Avoid Specialist Team by Technology Alone

Do not create permanent teams such as:

```text
Java Team

REST Team

Unit Test Team
```

that fragment normal product ownership.

---

# 130. Team API

Teams should expose clear ways for other teams to interact with their capabilities.

---

# 131. Team API Includes

Conceptually:

```text
Owned Services

Supported Contracts

Documentation

Support Channel

SLO

Change Policy

Escalation
```

---

# 132. Ownership Transfer

Service ownership may change.

---

# 133. Transfer Must Be Explicit

Ownership transfer requires deliberate handover.

---

# 134. Handover Checklist

Applicable:

```text
[ ] Repository ownership updated

[ ] CODEOWNERS updated

[ ] Service catalog updated

[ ] Runbooks reviewed

[ ] Dashboards transferred

[ ] Alerts transferred

[ ] Production access validated

[ ] SLO ownership transferred

[ ] Dependencies reviewed

[ ] API consumers reviewed

[ ] Kafka topics/events reviewed

[ ] Database ownership reviewed

[ ] Technical debt reviewed

[ ] Open incidents reviewed

[ ] Security findings reviewed

[ ] Documentation transferred
```

---

# 135. Knowledge Transfer

Repository access alone does not constitute successful ownership transfer.

---

# 136. Temporary Ownership

Temporary ownership requires an explicit expiration or reassessment point.

---

# 137. Orphan Detection

Governance should periodically detect services/assets without active owners.

---

# 138. Orphaned Assets

Examples:

```text
Repository

Service

Kafka Topic

Database

Shared Library

Dashboard

Scheduled Job

Cloud Resource
```

---

# 139. Orphan Policy

An orphaned production asset requires:

```text
ASSIGN OWNER
      |
      or
      v
DECOMMISSION
```

---

# 140. Decommission Ownership

The current owner is responsible for coordinating service decommissioning.

---

# 141. Decommission Is a Change

Removing a service is an architectural and operational change.

---

# 142. Consumer Validation

A service must not be removed solely because:

```text
"We think nobody uses it."
```

---

# 143. Evidence

Use available:

- traffic
- logs
- metrics
- consumer inventories
- repository searches
- stakeholder validation

---

# 144. Decommission Sequence

```text
IDENTIFY CONSUMERS
        |
        v
MIGRATE CONSUMERS
        |
        v
STOP NEW USAGE
        |
        v
REMOVE TRAFFIC
        |
        v
OBSERVE
        |
        v
REMOVE SERVICE
        |
        v
REMOVE INFRASTRUCTURE
```

---

# 145. Data Retention

Decommissioning must preserve required retention/audit obligations.

---

# 146. Secret Revocation

Service credentials/secrets must be revoked after decommissioning.

---

# 147. Topic Decommission

Kafka topics require consumer/retention analysis before deletion.

---

# 148. Database Decommission

Database/schema deletion requires:

- retention review
- backup requirements
- consumer validation
- recovery consideration

---

# 149. DNS and Routing

Obsolete routes must be removed after safe migration.

---

# 150. Monitoring Cleanup

Decommissioned services must have obsolete:

```text
Alerts

Dashboards

Synthetic Tests

SLOs
```

removed or archived.

---

# 151. Cost Ownership

Cloud/service costs should be attributable where practical.

---

# 152. Cost Awareness

Domain teams should understand significant cost drivers created by their architecture.

---

# 153. FinOps Responsibility

Platform/FinOps teams may provide tooling and standards.

Domain teams remain responsible for avoiding unnecessary resource consumption in their services.

---

# 154. Cost Does Not Override Reliability Blindly

Cost optimization must preserve required:

- SLO
- capacity
- resilience
- security

---

# 155. Ownership and Technical Debt

Technical debt belongs to the team owning the affected capability.

---

# 156. Debt Transfer

Ownership transfer must include known technical debt.

---

# 157. Hidden Debt Transfer

Transferring a service while withholding known risks undermines ownership.

---

# 158. Architecture Decision Ownership

Cross-platform ADRs belong to the architecture/platform governance function.

Service-specific ADRs belong primarily to the service-owning team.

---

# 159. ADR Contributors

Affected teams should participate in cross-service decisions.

---

# 160. Decision Authority

Decision authority should be located as close as practical to the team owning the consequences.

---

# 161. Central Architecture

Central architecture should focus on:

```text
Enterprise Constraints

Cross-Service Standards

Strategic Technology

Systemic Risks
```

rather than micromanaging implementation.

---

# 162. Local Architecture

Domain teams should own local design decisions that comply with platform standards.

---

# 163. Escalation

Escalate when a local decision:

- affects multiple domains
- creates a new enterprise technology
- violates an ADR
- changes platform security
- creates significant shared cost
- introduces cross-team coupling

---

# 164. Ownership Conflict

When two teams believe the other owns a capability, the ambiguity itself is a governance defect.

---

# 165. Conflict Resolution

Resolve ownership based on:

- business capability
- authoritative data
- operational responsibility
- change authority
- domain expertise

---

# 166. Ownership Should Follow Change

A team frequently required to modify another team's service may indicate a boundary problem.

---

# 167. Boundary Smell

Repeated cross-team PRs for normal domain changes should trigger architecture/team-boundary review.

---

# 168. Shared Service

A genuinely shared service requires explicit ownership.

---

# 169. Shared Service Consumer

Consumers should interact through stable contracts rather than ownership ambiguity.

---

# 170. Shared Database Anti-Pattern

A database used as an informal integration bus between independently owned services is prohibited.

---

# 171. Shared Table

Multiple services should not independently mutate the same business table.

---

# 172. Data Integration

Cross-service data exchange should use:

```text
API

Event

Approved Data Product / Integration
```

according to architecture.

---

# 173. Ownership Metadata as Code

Where practical, ownership metadata should be version controlled.

Examples:

```text
CODEOWNERS

catalog-info.yaml

service.yaml
```

depending on platform tooling.

---

# 174. Metadata Validation

CI may verify required ownership metadata exists.

---

# 175. Owner Validity

Automation should detect invalid/deleted teams in ownership metadata where platform tooling supports it.

---

# 176. Production Deployment Ownership

Production deployment must remain attributable to:

- service
- version
- owning team

---

# 177. Release Responsibility

The owning team is accountable for application release correctness.

---

# 178. Platform Deployment Responsibility

The platform team owns correctness of the deployment platform itself.

---

# 179. Separation Example

```text
Orders deployment fails because application
configuration is invalid
        |
        v
Orders Team

Deployment system cannot deploy any service
        |
        v
Platform Team
```

---

# 180. Joint Failure

Some incidents require joint diagnosis.

Ownership rules must support collaboration rather than ticket bouncing.

---

# 181. Ticket Ping-Pong

Repeated reassignment between teams during incidents is an ownership/governance failure.

---

# 182. First Responder

The identified service owner should begin application-level triage even when the root cause may ultimately be platform-related.

---

# 183. Platform Escalation

Platform escalation should include useful diagnostic evidence.

---

# 184. Platform SLO

Platform services should themselves have reliability objectives appropriate to their criticality.

---

# 185. Platform Dependency

A platform capability used by most services may have higher systemic criticality than an individual application.

---

# 186. Blast Radius

Platform architecture must explicitly consider blast radius.

---

# 187. Shared Platform Failure

A failure in a shared component can affect many services simultaneously.

Therefore shared capabilities require strong:

- resilience
- change governance
- observability
- rollback

---

# 188. Platform Versioning

Platform capabilities must evolve compatibly.

---

# 189. Forced Migration

Mandatory migrations should provide:

- reason
- timeline
- migration guidance
- support
- compatibility window

where feasible.

---

# 190. Unsupported Version

Platform versions cannot be supported indefinitely.

---

# 191. Support Lifecycle

Platform components should define:

```text
SUPPORTED

DEPRECATED

END OF SUPPORT
```

where applicable.

---

# 192. Consumer Responsibility

Domain teams must migrate before an announced end-of-support deadline.

---

# 193. Ownership Review

Ownership should be reviewed periodically.

---

# 194. Review Triggers

Review is particularly important after:

```text
Team Reorganization

Service Split

Service Merge

Major Modernization

Acquisition/Migration

Domain Redesign
```

---

# 195. Ownership Fitness Functions

Automated governance should validate ownership where practical.

---

# 196. Example Fitness Functions

```text
Every production repository has CODEOWNERS

Every service catalog entry has owner

Every production alert has routing owner

Every Kafka topic has ownership metadata

Every shared library has owner
```

---

# 197. Ownership Does Not Mean Exclusive Modification

Cross-team contribution is encouraged when appropriate.

---

# 198. Contribution Model

Preferred:

```text
Contributor Team
      |
      v
Pull Request
      |
      v
Owning Team Review
      |
      v
Merge
```

---

# 199. Emergency Contribution

Incident response may require expedited cross-team changes under emergency procedures.

---

# 200. Post-Incident Ownership

Emergency changes do not permanently alter ownership unless explicitly transferred.

---

# 201. Documentation Ownership

Service documentation belongs to the owning team.

---

# 202. Runbook Ownership

Runbooks belong to the teams responsible for responding to the relevant service/alert.

---

# 203. Stale Ownership Documentation

Incorrect ownership metadata is operationally dangerous.

---

# 204. Organizational Change

Team reorganization must include ownership metadata updates.

---

# 205. Ownership Governance Gate

A production service is not considered properly governed until:

```text
[ ] Primary owning team identified

[ ] Repository owner identified

[ ] CODEOWNERS configured where supported

[ ] Service catalog entry exists

[ ] Business domain identified

[ ] Service criticality identified

[ ] API ownership identified

[ ] API consumers discoverable where critical

[ ] Database/schema ownership identified

[ ] System of record identified where applicable

[ ] Kafka topics identified

[ ] Event ownership identified

[ ] Shared libraries identified

[ ] Platform dependencies identified

[ ] Upstream dependencies documented

[ ] Critical downstream consumers understood

[ ] Alert ownership configured

[ ] SLO ownership configured

[ ] Runbook ownership configured

[ ] Production access validated

[ ] Technical debt ownership established

[ ] Security responsibility understood

[ ] Decommission owner established

[ ] Ownership-transfer process understood
```

---

# 206. Anti-Patterns

The following are prohibited or strongly discouraged:

- production services without owners
- "everyone owns it"
- repositories without accountable teams
- shared libraries without owners
- Kafka topics without business ownership
- database tables independently modified by multiple services
- cross-service direct SQL as normal integration
- platform team owning every business event
- domain teams owning Kafka cluster administration
- platform teams required for every normal deployment
- domain teams bypassing platform standards
- CODEOWNERS treated as complete ownership governance
- ownership based solely on who originally wrote the code
- permanent temporary ownership
- service transfer without operational handover
- service decommission without consumer analysis
- orphaned production resources
- technology-silo teams fragmenting product delivery
- uncontrolled shared-domain libraries
- synchronized deployment of independent services without necessity
- incident ticket ping-pong
- transferring technical debt without disclosure
- platform upgrades without migration policy
- ownership metadata left stale after reorganization

---

# 207. Positive Consequences

The decision provides:

- explicit accountability
- faster incident routing
- clearer service boundaries
- stronger domain autonomy
- safer API evolution
- clear data ownership
- clear Kafka/event ownership
- better platform/domain collaboration
- reduced orphaned assets
- improved service decommissioning
- controlled shared libraries
- reduced coordination overhead
- more sustainable cognitive load

---

# 208. Negative Consequences

The decision introduces:

- service-catalog maintenance
- CODEOWNERS maintenance
- ownership reviews
- handover procedures
- cross-team contract coordination
- platform product-management responsibilities

These costs are accepted because ambiguous ownership creates larger operational and architectural costs.

---

# 209. Neutral Consequences

The decision also means:

- ownership does not prevent cross-team contribution
- platform teams do not own application behavior
- domain teams do not need deep expertise in every infrastructure subsystem
- some capabilities require joint ownership responsibilities
- service boundaries may evolve as team/domain boundaries evolve

---

# 210. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Ownership ambiguity | Critical | Medium | Service catalog |
| Orphaned service | High | Medium | Ownership fitness functions |
| Platform bottleneck | High | Medium | Self-service paved road |
| Domain cognitive overload | High | Medium | Platform abstractions |
| Cross-team coupling | High | Medium | Explicit contracts |
| Shared DB coupling | Critical | Medium | Data ownership |
| Event ambiguity | High | Medium | Event ownership |
| Failed ownership transfer | High | Medium | Handover checklist |
| Stale CODEOWNERS | Medium | Medium | Periodic validation |
| Incident ticket ping-pong | High | Medium | First-responder ownership |
| Shared-library coupling | High | Medium | Versioning |
| Distributed monolith | Critical | Medium | Independent deployment |

---

# 211. Implementation Guidance

The following rules are mandatory:

1. Every production service requires one identifiable primary owning team.
2. Every active repository requires ownership.
3. `CODEOWNERS` should be used where supported.
4. Production services should exist in a discoverable service catalog.
5. Ownership includes application behavior through production operation.
6. Domain teams own business/application correctness.
7. Platform teams own shared platform capabilities.
8. Platform capabilities should favor self-service.
9. Platform teams must not become mandatory ticket queues for routine delivery.
10. Domain teams must follow mandatory platform standards.
11. Service boundaries should align with meaningful business capabilities.
12. Service APIs are owned by provider teams.
13. Provider ownership does not permit uncontrolled breaking changes.
14. Service persistence boundaries require explicit ownership.
15. Cross-service direct database access is prohibited as a normal integration pattern.
16. Applied Flyway migrations remain immutable regardless of ownership changes.
17. Critical business data requires an identifiable authoritative source.
18. Kafka infrastructure and business-event ownership must remain distinct.
19. Every production business event/topic requires ownership.
20. Shared libraries require explicit owners and versioning.
21. Security follows a shared-responsibility model.
22. Domain teams own application instrumentation; platform teams may own observability infrastructure.
23. Production alerts require accountable routing.
24. Ownership transfer requires explicit technical and operational handover.
25. Orphaned production assets must receive an owner or be decommissioned.
26. Service decommissioning requires consumer validation.
27. Technical debt transfers with service ownership.
28. Decision authority should remain as close as practical to teams owning consequences.
29. Cross-team contribution should use the owning team's review process.
30. Ownership metadata must be updated after organizational changes.
31. Repeated ownership ambiguity should trigger boundary review.
32. Platform capabilities require lifecycle and compatibility policies.
33. Ownership should be validated through automated fitness functions where practical.

---

# 212. Validation

This ADR will be validated through:

- repository ownership review
- CODEOWNERS validation
- service-catalog validation
- architecture reviews
- incident reviews
- Kafka ownership inventory
- database ownership inventory
- API ownership inventory
- shared-library inventory
- SLO ownership
- alert routing
- technical-debt review
- decommission reviews
- organizational-change reviews

---

# 213. Success Criteria

The decision is successful when:

- every production service has a known owner
- engineers can rapidly identify the correct responder
- APIs have identifiable provider teams
- databases have clear schema ownership
- Kafka events have clear domain ownership
- platform capabilities are largely self-service
- domain teams can deploy independently
- ownership transfer does not lose operational knowledge
- orphaned services/resources decrease
- cross-service database coupling is absent
- shared libraries have controlled lifecycles
- service boundaries support rather than obstruct team autonomy
- incidents do not routinely become ownership disputes

---

# 214. Alternatives Rejected

## 214.1 Shared Ownership Without Primary Owner

Rejected because accountability becomes ambiguous.

---

## 214.2 Platform Team Owns All Production

Rejected because application/domain expertise remains with product teams and the model does not scale.

---

## 214.3 Domain Teams Own All Infrastructure

Rejected because this unnecessarily duplicates specialist expertise and increases cognitive load.

---

## 214.4 Database Team Owns Business Schemas

Rejected as a universal model because business data semantics belong to the owning domain.

---

## 214.5 Kafka Team Owns All Events

Rejected because infrastructure ownership does not imply business-semantic ownership.

---

## 214.6 Ownership Based on Original Author

Rejected because organizational ownership must survive personnel and team changes.

---

## 214.7 CODEOWNERS Alone

Rejected because review routing does not define operational, domain and lifecycle responsibilities.

---

# 215. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Migrations
- ADR-009: Use Apache Kafka for Integration Events
- ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-035: Adopt Engineering Quality and Testing Standards
- ADR-036: Adopt API Design, REST Contract and Compatibility Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-038: Adopt Dependency and Software Supply Chain Security Standards
- ADR-039: Adopt CI/CD, Release and Deployment Governance Standards
- ADR-040: Adopt Production Reliability, Incident Response and Operational Readiness Standards
- ADR-041: Adopt Architecture Governance and Technical Debt Management Standards
- ADR-042: Adopt Architecture Fitness Functions and Automated Governance Standards
- ADR-044: Adopt FinOps, Capacity Efficiency and Cloud Cost Governance Standards

---

# 216. References

- Team Topologies
- Conway's Law
- Inverse Conway Maneuver
- Domain-Driven Design
- Accelerate
- DORA
- Google Site Reliability Engineering
- AWS Well-Architected Framework
- FinOps Foundation
- Backstage Software Catalog
- GitHub CODEOWNERS

---

# 217. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial service ownership and team topology baseline |

---

# 218. Decision Summary

The definitive ownership model is:

```text
                    BUSINESS DOMAIN
                           |
                           v
                  STREAM-ALIGNED TEAM
                           |
                           v
                        SERVICE
                           |
             +-------------+-------------+
             |             |             |
             v             v             v
            API           DATA         EVENTS
             |             |             |
             +-------------+-------------+
                           |
                           v
                       OPERATIONS
```

Platform ownership is complementary:

```text
                    PLATFORM TEAM
                         |
        +----------------+----------------+
        |                |                |
        v                v                v
      CI/CD          OBSERVABILITY     SECURITY
        |                |                |
        +----------------+----------------+
                         |
                         v
                    PAVED ROAD
                         |
                         v
                  DOMAIN TEAMS
```

The key distinction is:

```text
Kafka Cluster
     |
     v
PLATFORM OWNERSHIP

OrderCreated Event
     |
     v
ORDERS DOMAIN OWNERSHIP
```

Likewise:

```text
PostgreSQL Platform
       |
       v
PLATFORM / DATABASE OPERATIONS

Orders Schema
       |
       v
ORDERS DOMAIN TEAM
```

For application ownership:

```text
YOU BUILD IT
     |
     v
YOU UNDERSTAND IT
     |
     v
YOU OBSERVE IT
     |
     v
YOU SUPPORT ITS BEHAVIOR
     |
     v
YOU IMPROVE IT
```

but not:

```text
YOU BUILD IT
     |
     v
YOU MUST PERSONALLY OPERATE
EVERY PIECE OF CLOUD INFRASTRUCTURE
```

The intended responsibility model is:

```text
DOMAIN EXPERTISE
       +
PLATFORM EXPERTISE
       +
CLEAR CONTRACT
       +
SHARED RESPONSIBILITY
       =
SUSTAINABLE AUTONOMY
```

For ownership:

```text
"EVERYONE OWNS IT"
        |
        v
AMBIGUITY
        |
        v
NO ACCOUNTABILITY
```

is replaced by:

```text
PRIMARY OWNER
     +
CONTRIBUTORS
     +
SUPPORTING TEAMS
     =
CLEAR ACCOUNTABILITY
```

For data:

```text
               CUSTOMER
                  |
                  v
          AUTHORITATIVE OWNER
                  |
                  v
          CUSTOMERS SERVICE
                  |
        +---------+---------+
        |                   |
        v                   v
      EVENT               API
        |                   |
        v                   v
  OTHER SERVICES      OTHER SERVICES
        |
        v
LOCAL SNAPSHOT
```

A snapshot does not become a competing source of truth.

For service lifecycle:

```text
CREATE
  |
  v
ASSIGN OWNER
  |
  v
BUILD
  |
  v
OPERATE
  |
  v
EVOLVE
  |
  v
DEPRECATE
  |
  v
MIGRATE CONSUMERS
  |
  v
DECOMMISSION
```

Ownership exists throughout the entire lifecycle.

The final organizational principle is:

```text
Architecture boundaries
and
team boundaries

must reinforce each other.
```

The desired platform state is therefore:

```text
CLEAR DOMAIN OWNERSHIP
        +
SELF-SERVICE PLATFORM
        +
EXPLICIT CONTRACTS
        +
INDEPENDENT DELIVERY
        +
SHARED RESPONSIBILITY
        +
CONTROLLED COGNITIVE LOAD
        =
SCALABLE ENGINEERING ORGANIZATION
```

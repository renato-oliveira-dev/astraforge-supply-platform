# ADR-048: Adopt Engineering Productivity, Developer Experience and InnerSource Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-048 |
| Title | Adopt Engineering Productivity, Developer Experience and InnerSource Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Developer Experience, Platform Engineering, InnerSource, Engineering Productivity |
| Related Work Items | Golden Paths, Service Templates, Gradle, Testcontainers, Self-Service, DORA |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

As the AstraForge Supply Platform grows, engineering productivity depends on more than individual developer capability.

Teams repeatedly solve similar problems:

```text
Create Service

Configure Build

Configure Security

Configure Database

Configure Flyway

Configure SQS

Configure Redis

Configure Observability

Configure CI/CD

Configure Tests

Configure Sonar

Configure SAST

Configure Deployment
```

If every team independently implements these capabilities, the organization accumulates:

- inconsistent architecture
- duplicated code
- security differences
- configuration drift
- incompatible conventions
- slow onboarding
- difficult upgrades
- duplicated operational effort

Developer Experience must therefore be treated as an architectural concern.

---

# 2. Problem Statement

The platform requires standards defining:

- Developer Experience
- Platform Engineering
- Golden Paths
- service templates
- scaffolding
- project structure
- Java 21
- Spring Boot
- Gradle
- local development
- Testcontainers
- developer feedback loops
- CI/CD integration
- reusable libraries
- InnerSource
- documentation
- onboarding
- self-service
- service catalog
- ownership
- platform APIs
- standardization
- exceptions
- productivity metrics
- DORA metrics

---

# 3. Decision Drivers

Primary drivers are:

1. engineering productivity
2. architectural consistency
3. fast developer feedback
4. reduced cognitive load
5. secure defaults
6. faster onboarding
7. reusable engineering capabilities
8. platform standardization
9. autonomous teams
10. controlled technology evolution
11. measurable delivery performance
12. sustainable software development

---

# 4. Decision

The organization adopts Platform Engineering and Developer Experience practices centered on paved roads and Golden Paths.

The canonical model is:

```text
                   PRODUCT TEAM
                        |
                        v
                 GOLDEN PATH
                        |
        +---------------+---------------+
        |               |               |
        v               v               v
    TEMPLATE         PLATFORM        DOCUMENTATION
        |               |               |
        +---------------+---------------+
                        |
                        v
                   CI/CD PIPELINE
                        |
                        v
                    PRODUCTION
```

---

# 5. Fundamental Principle

The primary rule is:

```text
The easiest engineering path
should normally also be
the secure, observable,
testable and supportable path.
```

---

# 6. Developer Experience

Developer Experience represents the quality of the environment in which engineers design, build, test, deploy and operate software.

---

# 7. DevEx Is Not Cosmetic

Developer Experience directly affects:

```text
Lead Time

Change Failure Rate

Deployment Frequency

Security

Quality

Operational Reliability
```

---

# 8. Cognitive Load

Platform capabilities should reduce unnecessary cognitive load.

---

# 9. Essential Complexity

Domain teams must understand their business domain.

---

# 10. Accidental Complexity

Domain teams should not repeatedly solve commodity platform concerns.

Examples:

```text
How do I configure Sonar?

How do I configure SAST?

How do I create a health endpoint?

How do I configure correlation IDs?

How do I configure CI?

How do I configure container builds?
```

These concerns should increasingly be standardized.

---

# 11. Platform Engineering

A platform team provides reusable capabilities enabling product/domain teams to deliver software safely and efficiently.

---

# 12. Platform as Product

The internal platform must be treated as a product.

It therefore requires:

- users
- roadmap
- ownership
- documentation
- support
- feedback
- versioning
- usability

---

# 13. Platform Customers

Primary customers are engineering teams.

---

# 14. Platform Success

Platform success is not measured by the number of tools created.

It is measured by improved engineering outcomes.

---

# 15. Golden Path

A Golden Path is the recommended, supported way to accomplish a common engineering task.

---

# 16. Golden Path Example

For a new backend service:

```text
CREATE FROM TEMPLATE
        |
        v
CONFIGURE DOMAIN
        |
        v
IMPLEMENT BUSINESS LOGIC
        |
        v
TEST
        |
        v
PIPELINE
        |
        v
DEPLOY
```

---

# 17. Golden Path Scope

A backend Golden Path should provide sensible defaults for:

```text
Java 21

Spring Boot

Gradle

Testing

Security

Logging

Observability

Database

Flyway

SQS

Redis

Resilience

Docker

CI/CD

Sonar

SAST
```

when applicable.

---

# 18. Golden Path Is Recommended, Not Prison

Teams may deviate when justified by business or technical requirements.

---

# 19. Exception Governance

Deviation requires:

```text
Reason

Owner

Impact

Support Model
```

for material architectural differences.

---

# 20. Paved Road

The platform should make the recommended path easier than custom implementation.

---

# 21. Do Not Standardize Through Pain

Teams should not be forced into standards solely through documentation and manual review when automation can provide the standard automatically.

---

# 22. Service Template

New services should preferably begin from an approved service template.

---

# 23. Template Baseline

The template should include applicable:

```text
src/main/java

src/test/java

build.gradle

settings.gradle

gradlew

gradlew.bat

gradle/wrapper

Dockerfile

application.yml

application-local.yml

Flyway structure

CI pipeline

README

package-info.java
```

---

# 24. Template Architecture

Recommended package structure should reflect architectural responsibilities.

Example:

```text
controller

domain

repository

service

integration

messaging

config

error

security
```

Exact structure may evolve with architecture standards.

---

# 25. Template Must Remain Small

A service template must not contain every possible platform capability enabled by default.

---

# 26. Optional Capabilities

Capabilities such as:

```text
SQS

Redis

Database

S3

Scheduled Jobs
```

should be included only when required or through composable scaffolding.

---

# 27. Template Version

Templates require versioning.

---

# 28. Template Drift

Existing services should not be expected to remain automatically identical to the newest template.

---

# 29. Upgrade Automation

Reusable automated migrations should be preferred for large cross-service upgrades.

---

# 30. Repository Creation

Creating a service should ideally be self-service.

---

# 31. Scaffolding

A scaffolding workflow may request:

```text
Service Name

Domain

Owning Team

Database Required?

SQS Required?

Redis Required?

External API Required?
```

and generate the appropriate baseline.

---

# 32. Secure Defaults

Generated services should be secure by default.

---

# 33. Quality Defaults

Generated services should include:

- unit-test support
- integration-test support
- JaCoCo
- Sonar configuration
- SAST integration

---

# 34. Java Baseline

Modern services use the approved Java baseline.

Current baseline:

```text
Java 21
```

---

# 35. Java Version Consistency

Local development, CI and production should use compatible Java versions.

---

# 36. Gradle

Gradle Wrapper is mandatory for Gradle-based projects.

---

# 37. Wrapper Command

Developers and CI should use:

```text
./gradlew
```

or on Windows:

```text
gradlew.bat
```

rather than relying on an arbitrary globally installed Gradle version.

---

# 38. Wrapper Version

The Gradle Wrapper version belongs to the repository and must be version controlled.

---

# 39. Reproducible Build

The target is:

```text
SAME SOURCE
    +
SAME DEPENDENCIES
    +
SAME BUILD CONFIGURATION
    =
SAME BUILD BEHAVIOR
```

---

# 40. Dependency Repositories

Dependency repositories should be centrally governed.

---

# 41. Dynamic Versions

Avoid uncontrolled dependency versions such as:

```text
1.+

latest.release
```

---

# 42. Dependency Locking

Dependency locking or equivalent controls should be considered where reproducibility requires it.

---

# 43. Build Performance

Build performance is a Developer Experience concern.

---

# 44. Build Measurement

Measure:

```text
Clean Build Time

Incremental Build Time

Test Time

CI Time
```

---

# 45. Build Cache

Gradle build caching may be adopted when it provides reliable benefit.

---

# 46. Parallel Gradle

Gradle parallel execution may be used where projects/tasks safely support it.

---

# 47. Faster Is Not Always Better

Build optimizations must preserve deterministic behavior.

---

# 48. Local Development

A developer should be able to understand how to run a service locally from its repository documentation.

---

# 49. Local Startup

The repository should document:

```text
Prerequisites

Environment Variables

Dependencies

Startup Command

Ports

Health Check
```

---

# 50. One-Command Goal

Where practical, common local setup should approach:

```text
./gradlew bootRun
```

or another documented minimal command sequence.

---

# 51. External Dependencies

Developers should not require access to production infrastructure to run ordinary local tests.

---

# 52. Testcontainers

Testcontainers is preferred for integration tests requiring representative infrastructure where appropriate.

---

# 53. Representative Technology

Examples:

```text
PostgreSQL -> PostgreSQL container

Redis      -> Redis container

SQS      -> SQS-compatible test infrastructure
```

---

# 54. H2

H2 must not be treated as proof of PostgreSQL-specific behavior.

---

# 55. Integration Test Fidelity

Tests covering:

- SQL
- Flyway
- PostgreSQL types
- indexes
- constraints
- transaction behavior

should use representative database technology.

---

# 56. Container Reuse

Local container reuse may be used to improve developer feedback when it does not compromise test isolation.

---

# 57. CI Isolation

CI tests must remain reproducible and isolated.

---

# 58. Developer Feedback Loop

The engineering feedback loop should be intentionally optimized.

---

# 59. Ideal Loop

```text
CODE
 |
 v
COMPILE
 |
 v
TEST
 |
 v
STATIC ANALYSIS
 |
 v
FEEDBACK
```

should complete quickly enough to support iterative development.

---

# 60. Fast Tests

Unit tests should normally remain fast.

---

# 61. Slow Tests

Slower integration/end-to-end tests should be separated when appropriate.

---

# 62. Test Classification

Projects may classify:

```text
Unit

Integration

Contract

End-to-End
```

tests.

---

# 63. Local Test Command

Developers should have a documented command to execute the relevant pre-commit test suite.

---

# 64. CI Test Command

CI should use the same build system as developers.

---

# 65. "Works Only in CI"

A build that cannot reasonably be reproduced locally creates unnecessary diagnostic friction.

---

# 66. "Works Only on My Machine"

Local environments must not rely on undocumented developer-specific configuration.

---

# 67. Static Analysis Feedback

Developers should receive Sonar/SAST feedback as early as practical.

---

# 68. Sonar

Sonar rules should be incorporated into normal engineering practices rather than treated only as a final release gate.

---

# 69. Test Quality

Tests must themselves satisfy applicable maintainability rules.

---

# 70. Assertion Description

Assertions should provide useful diagnostic descriptions where project standards require them.

Example:

```java
assertThat(result)
        .as("result should contain the expected customer")
        .isEqualTo(expected);
```

---

# 71. Test Naming

Test names should communicate behavior.

Example:

```text
testCreateOrderShouldReturnCreatedOrder
```

---

# 72. Deterministic Tests

Tests should avoid unnecessary randomness.

---

# 73. Fixed Test Data

Stable test constants should be preferred where deterministic identity is useful.

---

# 74. Sleeping Tests

Tests should avoid:

```java
Thread.sleep(...)
```

as synchronization strategy.

---

# 75. Concurrent Tests

Concurrency tests should use deterministic synchronization primitives.

---

# 76. Test Fixtures

Reusable fixtures/builders may reduce repetitive setup.

---

# 77. Fixture Clarity

Test abstractions must not hide the behavior being tested.

---

# 78. Reusable Libraries

Common capabilities may be extracted into reusable libraries when they represent genuinely stable cross-service behavior.

---

# 79. Good Library Candidates

Examples:

```text
Correlation Headers

Common Error Contracts

Security Integration

Testing Utilities

Platform Client Infrastructure
```

---

# 80. Bad Library Candidates

Avoid central libraries containing arbitrary business logic from multiple domains.

---

# 81. Shared Library Coupling

A shared library creates coupling.

---

# 82. Library Extraction Rule

Do not extract code solely because two services currently contain similar lines.

---

# 83. Stable Abstraction

Extract when the shared concept has:

- stable semantics
- multiple consumers
- clear ownership
- independent versioning value

---

# 84. Library Versioning

Reusable libraries require semantic/version governance.

---

# 85. Backward Compatibility

Library upgrades should avoid unnecessary breaking changes.

---

# 86. Dependency Blast Radius

A widely used library has a large blast radius.

Changes require stronger validation.

---

# 87. Platform Starter

Spring Boot starters may package stable platform capabilities where appropriate.

---

# 88. Starter Scope

A starter may provide:

```text
Auto-Configuration

Default Properties

Common Filters

Security Baseline

Error Handling Infrastructure
```

---

# 89. Starter Override

Applications must retain controlled ability to override defaults when legitimate.

---

# 90. Avoid Magic

Platform libraries must not create behavior that is difficult for service teams to discover or debug.

---

# 91. Documentation

Reusable platform capabilities require documentation.

---

# 92. Documentation Structure

Documentation should answer:

```text
What does this do?

When should I use it?

How do I configure it?

What are the defaults?

How do I test it?

How do I troubleshoot it?

Who owns it?
```

---

# 93. Documentation as Code

Technical documentation should be version controlled near the relevant code where practical.

---

# 94. Executable Documentation

Examples and commands should be executable where feasible.

---

# 95. Stale Documentation

Incorrect documentation is a Developer Experience defect.

---

# 96. Documentation Ownership

Documentation must have an accountable owner through the associated component/team.

---

# 97. README

Every service repository should have a useful README.

---

# 98. README Minimum

Include:

```text
Purpose

Owner

Architecture Summary

Prerequisites

Build

Test

Run Locally

Configuration

Dependencies

Deployment

Troubleshooting
```

---

# 99. Architecture Documentation

Repositories should link to applicable ADRs and architecture documentation.

---

# 100. API Documentation

REST APIs should expose maintained OpenAPI documentation where applicable.

---

# 101. API Documentation Accuracy

Generated API documentation should reflect the deployed contract.

---

# 102. Service Catalog

Production services should be discoverable through a service catalog or equivalent inventory.

---

# 103. Catalog Metadata

Useful metadata includes:

```text
Service Name

Domain

Owner

Repository

Documentation

API

Environment

Criticality

Runbook

Dashboard

SLO
```

---

# 104. Ownership

Service ownership must align with ADR-043.

---

# 105. Discoverability

An engineer encountering an alert should be able to determine who owns the service quickly.

---

# 106. Self-Service

Routine platform operations should become self-service where safe.

---

# 107. Self-Service Examples

```text
Create Service

Create Database

Create SQS Topic

Create Redis Instance

Create Environment

Request Secret

Create Dashboard
```

subject to governance.

---

# 108. Self-Service Does Not Mean Uncontrolled

Self-service operations must apply:

- policy
- security
- naming
- tagging
- cost controls

automatically.

---

# 109. Platform API

Platform capabilities should expose stable automation interfaces.

---

# 110. Portal

A developer portal may provide a convenient user interface over platform APIs.

---

# 111. Portal Is Not the Platform

A portal without reliable automation behind it is only another interface.

---

# 112. Automation First

Prefer:

```text
PLATFORM CAPABILITY
        |
        v
      API/IaC
        |
        v
      PORTAL
```

rather than portal-only manual workflows.

---

# 113. InnerSource

Reusable internal software should follow InnerSource practices where organizationally appropriate.

---

# 114. InnerSource Objective

InnerSource enables teams outside the owning team to contribute improvements safely.

---

# 115. Ownership Remains

InnerSource does not mean:

```text
Nobody owns the repository.
```

---

# 116. Maintainer

Every shared repository requires maintainers.

---

# 117. Contribution Guide

Shared repositories should define:

```text
How to contribute

Coding standards

Testing requirements

Review process

Release process
```

---

# 118. Pull Requests

Changes to shared components require review by appropriate maintainers.

---

# 119. Contribution Autonomy

Teams should be able to propose fixes rather than waiting indefinitely for another team.

---

# 120. Maintainer Responsibility

Maintainers remain responsible for:

- architectural coherence
- compatibility
- release quality
- security

---

# 121. CODEOWNERS

Repository ownership mechanisms such as CODEOWNERS may support review routing.

---

# 122. InnerSource Documentation

Shared projects require sufficient documentation for engineers outside the owning team.

---

# 123. Issue Templates

Issue/PR templates may standardize useful contribution information.

---

# 124. Contribution Feedback

InnerSource only works when contributions receive timely review.

---

# 125. Platform Support

The platform requires a clear support model.

---

# 126. Support Channels

Support should distinguish:

```text
Documentation

Known Issues

Questions

Incidents

Feature Requests
```

---

# 127. Avoid Tribal Knowledge

Critical platform knowledge must not exist only in private conversations.

---

# 128. Onboarding

A new engineer should be able to become productive without reconstructing the platform from tribal knowledge.

---

# 129. Onboarding Path

Recommended sequence:

```text
ACCESS
  |
  v
WORKSTATION
  |
  v
CLONE
  |
  v
BUILD
  |
  v
TEST
  |
  v
RUN LOCALLY
  |
  v
FIRST CHANGE
  |
  v
FIRST DEPLOYMENT
```

---

# 130. Time to First Build

The organization should measure friction in obtaining a successful local build.

---

# 131. Time to First Contribution

Time from onboarding to a meaningful first contribution is a useful DevEx signal.

---

# 132. Environment Setup

Developer environment setup should increasingly be automated.

---

# 133. Required Tooling

Required tooling and supported versions should be explicit.

---

# 134. IDE Independence

Core build/test operations should not require a specific IDE.

---

# 135. IDE Support

IDE configuration/templates may still improve productivity.

---

# 136. Local Secrets

Local development must not require production credentials.

---

# 137. Developer Credentials

Development credentials should follow least privilege.

---

# 138. Environment Parity

Development environments should preserve relevant production semantics without requiring identical scale.

---

# 139. Local vs Production

The target is:

```text
SEMANTIC PARITY
```

not:

```text
INFRASTRUCTURE SIZE PARITY
```

---

# 140. Configuration Profiles

Environment-specific configuration should not alter core business semantics unexpectedly.

---

# 141. Local Mocks

Mocks/stubs may be used for external systems when representative integration environments are impractical.

---

# 142. Mock Limitation

Mocks do not replace contract/integration testing against real protocols.

---

# 143. Contract Testing

Consumer/provider contract tests should be considered for important service integrations.

---

# 144. API Compatibility

Contract tests support but do not replace API governance.

---

# 145. CI/CD Golden Path

Standard services should receive a standard pipeline baseline.

---

# 146. Pipeline Stages

Applicable:

```text
CHECKOUT

COMPILE

UNIT TEST

INTEGRATION TEST

JACOCO

SONAR

SAST

DEPENDENCY SCAN

PACKAGE

CONTAINER BUILD

DEPLOY
```

---

# 147. Pipeline Reuse

Common pipeline logic should be reusable rather than copied across every repository.

---

# 148. Pipeline Versioning

Shared pipeline templates require controlled versioning.

---

# 149. Pipeline Escape Hatch

Exceptional workloads may require customized pipelines.

Material deviations should remain visible.

---

# 150. CI Feedback Time

CI duration is an engineering productivity metric.

---

# 151. Queue Time

Pipeline queue time should be distinguished from execution time.

---

# 152. Flaky Tests

Flaky tests are Developer Experience defects.

---

# 153. Flaky Test Policy

A flaky test should be:

```text
IDENTIFIED

OWNED

FIXED
```

rather than retried indefinitely until green.

---

# 154. Retry Is Not Fix

Automatic retry may reduce transient noise but must not hide systemic flakiness.

---

# 155. Test Failure Diagnostics

CI failures should provide enough information to diagnose the problem efficiently.

---

# 156. Build Logs

Build logs should be:

- searchable
- appropriately retained
- free from secrets
- sufficiently diagnostic

---

# 157. Quality Gate

Quality gates should be predictable.

---

# 158. Local Reproduction

Developers should be able to reproduce most quality-gate failures locally where practical.

---

# 159. Dependency Management

Common dependency versions should be governed consistently.

---

# 160. Version Catalog

Gradle Version Catalogs or equivalent mechanisms may standardize versions where appropriate.

---

# 161. BOM

BOM/platform dependency management may be used to maintain compatible dependency sets.

---

# 162. Centralization Balance

Central dependency governance must not prevent necessary service evolution.

---

# 163. Upgrade Campaign

Major upgrades affecting many services should use coordinated automation.

---

# 164. Example

For:

```text
Java 21 -> future approved Java version
```

the platform should provide:

- migration guide
- automated transformations where possible
- compatibility matrix
- validation pipeline

---

# 165. Automated Refactoring

Tools such as OpenRewrite or equivalent may be used for repeatable large-scale Java migrations.

---

# 166. Automation Validation

Automated transformations still require tests and review.

---

# 167. Platform Deprecation

Platform capabilities require explicit deprecation policy.

---

# 168. Deprecation Notice

A deprecated capability should identify:

```text
Replacement

Migration Path

Support End Date
```

---

# 169. Breaking Platform Change

Platform teams must not silently break consuming services.

---

# 170. Adoption Measurement

Platform capabilities should measure adoption.

---

# 171. Adoption Is Not Success Alone

100% adoption of a poor platform is not a successful outcome.

---

# 172. User Feedback

Developer feedback should influence platform priorities.

---

# 173. DevEx Survey

Periodic qualitative surveys may measure:

- cognitive load
- build friction
- deployment friction
- documentation quality
- platform satisfaction

---

# 174. DORA Metrics

The organization should use DORA metrics where meaningful.

---

# 175. Deployment Frequency

Measure how frequently production changes are successfully deployed.

---

# 176. Lead Time for Changes

Measure time from code change to successful production availability.

---

# 177. Change Failure Rate

Measure the proportion of production changes causing significant failure/remediation.

---

# 178. Time to Restore

Measure recovery from production failures.

---

# 179. Metrics Must Not Become Individual Performance Scores

DORA/DevEx metrics must not be used mechanically to rank individual developers.

---

# 180. Why

Doing so creates incentives to manipulate metrics rather than improve system performance.

---

# 181. Team/System Metrics

These metrics are primarily useful for understanding delivery systems and teams.

---

# 182. Context

Metric interpretation requires:

- service criticality
- deployment model
- team scope
- workload

context.

---

# 183. Developer Productivity

Developer productivity must not be measured primarily through:

```text
Lines of Code

Commit Count

PR Count

Hours Online
```

---

# 184. Outcome Orientation

Prefer measuring:

```text
Delivery Flow

Quality

Reliability

Developer Friction

Business Outcomes
```

---

# 185. SPACE Framework

The SPACE framework may complement DORA for broader Developer Experience analysis.

---

# 186. Productivity Dimensions

Relevant dimensions include:

```text
Satisfaction

Performance

Activity

Communication

Efficiency
```

without reducing productivity to a single metric.

---

# 187. Platform SLO

Internal platform capabilities may define SLOs.

---

# 188. Example Platform SLOs

Examples:

```text
CI availability

Artifact repository availability

Developer portal availability

Service provisioning latency
```

---

# 189. Platform Reliability

A platform outage can block many teams simultaneously.

Platform components therefore require reliability proportional to their blast radius.

---

# 190. Platform Blast Radius

Centralization increases leverage but also increases failure impact.

---

# 191. Safe Platform Evolution

Platform changes require:

- backward compatibility
- staged rollout
- observability
- rollback

where appropriate.

---

# 192. Dogfooding

Platform teams should use their own Golden Paths where practical.

---

# 193. Exception Feedback

Repeated exceptions indicate the Golden Path may not satisfy real workloads.

---

# 194. Exception Analysis

If many teams request the same exception:

```text
EXCEPTION
    |
    v
PATTERN
    |
    v
PLATFORM IMPROVEMENT
```

should be considered.

---

# 195. Golden Path Evolution

Golden Paths must evolve with architecture.

---

# 196. Golden Path Versioning

Major changes should have migration guidance.

---

# 197. Service Creation Is Not Enough

The platform must support the full lifecycle:

```text
CREATE
  |
  v
DEVELOP
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
UPGRADE
  |
  v
DECOMMISSION
```

---

# 198. Decommission

Platform tooling should eventually help remove obsolete services and associated infrastructure.

---

# 199. Service Lifecycle

Service catalog state may include:

```text
Experimental

Active

Deprecated

Retiring

Retired
```

---

# 200. Documentation Lifecycle

Documentation should follow service lifecycle.

---

# 201. Developer Portal

A developer portal may aggregate:

```text
Service Catalog

Documentation

APIs

Golden Paths

Scaffolding

Ownership

Dashboards

Runbooks
```

---

# 202. Portal Authentication

Portal access must follow enterprise identity/security standards.

---

# 203. Portal Authorization

Sensitive operations require appropriate authorization.

---

# 204. Auditability

Platform self-service actions affecting infrastructure should be auditable.

---

# 205. Cost Visibility

Self-service infrastructure should expose cost ownership according to ADR-044.

---

# 206. Resource Ownership

Generated infrastructure must automatically inherit:

```text
Service

Team

Domain

Environment

Cost Center
```

metadata where applicable.

---

# 207. Security by Construction

Golden Paths should embed applicable security standards.

---

# 208. Examples

Generated services should avoid:

```text
Hardcoded Secrets

Open Actuator Endpoints

Unrestricted CORS

Uncontrolled Logging

Unpinned Tool Versions
```

---

# 209. Observability by Construction

Services should receive standard:

```text
Logging

Health

Metrics

Correlation
```

capabilities.

---

# 210. Resilience by Construction

Where integrations require resilience, standard platform mechanisms should make correct configuration straightforward.

---

# 211. Database by Construction

Database-enabled services should receive:

- PostgreSQL configuration
- Flyway
- migration structure
- Testcontainers support

where appropriate.

---

# 212. Migration Immutability

Generated database standards must reinforce:

```text
Applied Flyway migration
        |
        v
      IMMUTABLE
```

Corrections require a new migration.

---

# 213. Messaging by Construction

SQS-enabled services should receive approved:

- producer configuration
- consumer configuration
- serialization
- error handling
- observability

patterns.

---

# 214. Testability by Construction

Generated components should be structured for testability.

---

# 215. Avoid Excessive Static State

Platform templates should avoid patterns that unnecessarily complicate unit testing.

---

# 216. Dependency Injection

Dependencies should be explicit.

---

# 217. Constructor Injection

Constructor injection is preferred for mandatory application dependencies.

---

# 218. Clean Code

Golden Paths should embody maintainability standards.

---

# 219. Complexity

Templates must not introduce unnecessary abstractions simply to demonstrate architecture patterns.

---

# 220. Boilerplate Reduction

Boilerplate should be reduced through stable abstractions and code generation where appropriate.

---

# 221. Generated Code

Generated code should be clearly distinguishable when developers are not expected to edit it manually.

---

# 222. Generated Code Ownership

The generator/template becomes responsible for generated patterns.

---

# 223. Regeneration

Generators should not overwrite user business code unexpectedly.

---

# 224. InnerSource Reuse Hierarchy

Before creating a new reusable component:

```text
SEARCH
  |
  v
EXISTING PLATFORM CAPABILITY?
  |
  +--> YES -> REUSE / CONTRIBUTE
  |
  +--> NO  -> EVALUATE NEW COMPONENT
```

---

# 225. Duplicate Platform Components

Multiple libraries solving the same cross-cutting concern should be consolidated where practical.

---

# 226. Domain Independence

Platform components must remain domain-neutral unless intentionally owned by a domain.

---

# 227. Platform Team Boundary

Platform Engineering should enable teams rather than becoming a mandatory ticket queue.

---

# 228. Ticket-Driven Platform

This model should be avoided:

```text
DEVELOPER
    |
    v
OPEN TICKET
    |
    v
WAIT
    |
    v
PLATFORM TEAM
    |
    v
MANUAL ACTION
```

for routine operations.

---

# 229. Target Model

Prefer:

```text
DEVELOPER
    |
    v
SELF-SERVICE PLATFORM
    |
    v
POLICY + AUTOMATION
    |
    v
RESOURCE READY
```

---

# 230. Platform Guardrails

Self-service must operate inside automated guardrails.

---

# 231. Guardrail Examples

```text
Naming

Security

Network

Tagging

Cost

Resource Limits

Approved Versions
```

---

# 232. Platform Escape Hatch

Exceptional workloads require a documented escape path.

---

# 233. Escape Hatch Ownership

The team deviating from the platform baseline assumes appropriate ownership for the additional operational complexity.

---

# 234. Developer Experience Governance Gate

A standard production service is not considered fully integrated with the engineering platform until:

```text
[ ] Service owner identified

[ ] Domain identified

[ ] Repository discoverable

[ ] README exists

[ ] Build uses repository-controlled tooling

[ ] Java baseline compliant

[ ] Local build documented

[ ] Local startup documented

[ ] Unit tests available

[ ] Integration tests available where required

[ ] Representative infrastructure used for integration tests

[ ] JaCoCo configured

[ ] Sonar configured

[ ] SAST configured

[ ] Dependency scanning configured

[ ] CI/CD pipeline configured

[ ] Build reproducible

[ ] Secrets externalized

[ ] Logging baseline configured

[ ] Health checks configured

[ ] API documentation available where applicable

[ ] Service catalog entry exists

[ ] Runbook linked where applicable

[ ] Dashboard linked where applicable

[ ] SLO linked where applicable

[ ] Platform deviations documented

[ ] Shared-library versions supported

[ ] Ownership metadata propagated to infrastructure

[ ] Cost metadata configured

[ ] Developer onboarding path documented
```

---

# 235. Platform Capability Gate

A shared platform capability is not considered mature until:

```text
[ ] Owner identified

[ ] Users identified

[ ] Documentation exists

[ ] Supported versions documented

[ ] Automated tests exist

[ ] Security reviewed

[ ] Upgrade strategy exists

[ ] Deprecation strategy exists

[ ] Support channel exists

[ ] Usage observable

[ ] Feedback mechanism exists

[ ] Failure blast radius understood

[ ] Rollback strategy exists
```

---

# 236. Anti-Patterns

The following are prohibited or strongly discouraged:

- every team creating its own service baseline
- copy/paste platform configuration across repositories
- Golden Paths that cannot be deviated from when justified
- service templates containing every possible dependency
- globally installed Gradle required to build repositories
- dynamic uncontrolled dependency versions
- integration tests using H2 to claim PostgreSQL compatibility
- production credentials required for local development
- CI behavior impossible to reproduce locally
- Thread.sleep used as normal test synchronization
- flaky tests retried indefinitely instead of fixed
- Sonar/SAST considered only immediately before production
- shared libraries containing unrelated domain business logic
- platform libraries with hidden magical behavior
- undocumented reusable components
- service repositories without ownership
- documentation maintained only in tribal knowledge
- manual ticket queues for routine platform operations
- self-service without policy guardrails
- platform adoption used as the sole measure of platform success
- DORA metrics used to rank individual developers
- developer productivity measured by LOC or commit count
- feature-rich developer portals without automation behind them
- platform changes silently breaking consumers
- obsolete platform capabilities without deprecation plans
- generated code overwriting business implementation
- permanent platform exceptions without ownership

---

# 237. Positive Consequences

The decision provides:

- faster service creation
- lower cognitive load
- improved onboarding
- consistent Java/Spring/Gradle baselines
- stronger security defaults
- faster testing
- representative integration testing
- reusable engineering capabilities
- improved self-service
- better documentation
- stronger InnerSource
- more consistent CI/CD
- measurable engineering productivity
- easier enterprise-wide upgrades

---

# 238. Negative Consequences

The decision introduces:

- platform-team investment
- template maintenance
- shared-library governance
- developer portal/tooling maintenance
- documentation maintenance
- upgrade automation effort
- service catalog maintenance

These costs are accepted because repeated manual engineering work across many teams creates substantially greater long-term cost.

---

# 239. Neutral Consequences

The decision also means:

- not every service will use every platform capability
- some teams will legitimately deviate from Golden Paths
- templates will evolve
- existing services may lag behind the latest baseline temporarily
- centralization must be balanced against team autonomy
- Developer Experience cannot be represented by one metric

---

# 240. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Platform becomes bottleneck | High | Medium | Self-service |
| Golden Path too restrictive | High | Medium | Escape hatch |
| Template becomes bloated | Medium | High | Composable capabilities |
| Shared library blast radius | High | Medium | Versioning/testing |
| Documentation becomes stale | Medium | High | Ownership/docs-as-code |
| CI becomes slow | High | Medium | Feedback-loop metrics |
| Flaky tests reduce trust | High | Medium | Flaky-test ownership |
| Platform adoption remains low | Medium | Medium | Developer feedback |
| Metrics become performance surveillance | High | Medium | Team/system-level metrics |
| Central platform outage blocks teams | High | Medium | Platform SLO/resilience |
| Upgrade fragmentation | High | Medium | Automated migrations |
| Tribal knowledge persists | High | Medium | Discoverable documentation |

---

# 241. Implementation Guidance

The following rules are mandatory:

1. Platform Engineering must treat internal developers as platform customers.
2. Common engineering workflows should have documented Golden Paths.
3. The recommended path should provide secure and observable defaults.
4. Teams may deviate when justified, but material deviations require ownership.
5. New standard services should preferably originate from approved templates/scaffolding.
6. Templates must remain composable and avoid unnecessary dependencies.
7. Java services must use the approved Java baseline.
8. Gradle-based repositories must use the Gradle Wrapper.
9. Builds should be reproducible locally and in CI.
10. Local development must not depend on production credentials.
11. Representative integration infrastructure should be used where technology-specific behavior matters.
12. PostgreSQL-specific tests must not rely solely on H2.
13. Developer feedback-loop duration should be measured and improved.
14. Flaky tests must be treated as defects.
15. Sonar, SAST and dependency security should provide early feedback.
16. Test code must follow applicable maintainability standards.
17. Shared libraries require stable semantics and explicit ownership.
18. Shared platform components require versioning and compatibility governance.
19. Platform libraries must avoid hidden behavior.
20. Service documentation must include build, test, run and ownership information.
21. Production services should be discoverable through a service catalog or equivalent inventory.
22. Routine platform operations should become self-service where safe.
23. Self-service must automatically apply security, cost and governance policies.
24. InnerSource shared repositories require maintainers and contribution guidance.
25. Platform knowledge must not depend solely on tribal knowledge.
26. Developer onboarding friction should be measured.
27. Common CI/CD logic should be reusable.
28. DORA metrics should be used to improve delivery systems, not rank individuals.
29. Developer productivity must not be reduced to LOC, commits or PR counts.
30. Platform changes require controlled compatibility and deprecation strategies.
31. Golden Paths must evolve based on developer feedback and exception patterns.
32. Service lifecycle must include creation through decommission.
33. Platform deviations must not silently become permanent unsupported architecture.

---

# 242. Validation

This ADR will be validated through:

- developer surveys
- onboarding measurements
- build-time metrics
- CI-duration metrics
- flaky-test reports
- service-template adoption
- platform-capability adoption
- DORA metrics
- service-catalog completeness
- documentation reviews
- InnerSource contribution metrics
- Sonar/SAST results
- platform SLOs
- architecture reviews
- developer feedback

---

# 243. Success Criteria

The decision is successful when:

- new services can be created quickly with compliant defaults
- developers spend less time configuring commodity infrastructure
- local builds are predictable
- CI feedback becomes faster
- production-specific behavior is tested with representative infrastructure
- onboarding time decreases
- platform capabilities are discoverable
- routine infrastructure provisioning is self-service
- shared components have clear ownership
- organization-wide upgrades require less manual work
- DORA indicators improve without sacrificing reliability
- platform standards reduce rather than increase developer friction

---

# 244. Alternatives Rejected

## 244.1 Every Team Builds Its Own Platform

Rejected because it duplicates effort and creates architectural inconsistency.

---

## 244.2 Mandatory Central Team for Every Infrastructure Change

Rejected because ticket-driven operations reduce team autonomy and throughput.

---

## 244.3 One Massive Service Template

Rejected because services should not inherit unused infrastructure and dependencies.

---

## 244.4 Standardization Without Escape Hatches

Rejected because some workloads legitimately require different technical approaches.

---

## 244.5 Developer Portal as the Entire Platform

Rejected because reliable APIs, automation and infrastructure capabilities must exist underneath the portal.

---

## 244.6 LOC as Productivity Metric

Rejected because code volume does not represent engineering value.

---

## 244.7 Individual Developer Rankings Using DORA

Rejected because DORA metrics characterize software-delivery systems rather than individual productivity.

---

# 245. Related Decisions

This ADR is related to:

- ADR-004: Use Spring Boot
- ADR-006: Use Flyway for Database Migrations
- ADR-014: Adopt Distributed Observability
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-034: Adopt Java 21 Concurrency and Parallelism Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-038: Adopt Dependency and Software Supply Chain Security Standards
- ADR-039: Adopt CI/CD, Release and Deployment Governance Standards
- ADR-040: Adopt Production Reliability, Incident Response and Operational Readiness Standards
- ADR-041: Adopt Architecture Governance and Technical Debt Management Standards
- ADR-042: Adopt Architecture Fitness Functions and Automated Governance Standards
- ADR-043: Adopt Service Ownership, Platform Boundaries and Team Topology Standards
- ADR-044: Adopt FinOps, Capacity Efficiency and Cloud Cost Governance Standards
- ADR-045: Adopt Business Continuity, Disaster Recovery and Regional Resilience Standards
- ADR-046: Adopt Data Governance, Privacy, Retention and Lifecycle Standards
- ADR-047: Adopt Legacy Modernization, Strangler Migration and Technical Evolution Standards
- ADR-049: Adopt AI-Assisted Software Engineering and Responsible AI Development Standards

---

# 246. References

- Team Topologies
- Platform Engineering
- Backstage
- DORA
- SPACE Framework
- InnerSource Commons
- Java 21 Documentation
- Spring Boot Documentation
- Gradle Documentation
- Testcontainers
- OpenRewrite
- SonarQube
- OWASP
- Google Site Reliability Engineering

---

# 247. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial Developer Experience, Platform Engineering and InnerSource baseline |

---

# 248. Decision Summary

The definitive Developer Experience model is:

```text
                     DEVELOPER
                         |
                         v
                    GOLDEN PATH
                         |
          +--------------+--------------+
          |              |              |
          v              v              v
       TEMPLATE       PLATFORM       DOCUMENTATION
          |              |              |
          +--------------+--------------+
                         |
                         v
                     DEVELOP
                         |
                         v
                       TEST
                         |
                         v
                       CI/CD
                         |
                         v
                    PRODUCTION
```

The objective is to transform this:

```text
NEW SERVICE
    |
    +--> Configure Java
    +--> Configure Gradle
    +--> Configure Spring
    +--> Configure Security
    +--> Configure Database
    +--> Configure Flyway
    +--> Configure Tests
    +--> Configure Sonar
    +--> Configure SAST
    +--> Configure Docker
    +--> Configure Pipeline
    +--> Configure Observability
```

into:

```text
NEW SERVICE
    |
    v
SELECT GOLDEN PATH
    |
    v
GENERATE BASELINE
    |
    v
IMPLEMENT DOMAIN
```

For platform engineering:

```text
DOMAIN TEAM
    |
    v
SELF-SERVICE
    |
    v
PLATFORM API
    |
    v
AUTOMATION
    |
    v
POLICY / GUARDRAILS
    |
    v
RESOURCE READY
```

instead of:

```text
DOMAIN TEAM
    |
    v
TICKET
    |
    v
WAIT
    |
    v
MANUAL PLATFORM WORK
```

For local development:

```text
CLONE
  |
  v
BUILD
  |
  v
TEST
  |
  v
RUN
```

must not require undocumented knowledge.

For integration testing:

```text
POSTGRESQL PRODUCTION
        ^
        |
TESTCONTAINERS POSTGRESQL
```

provides substantially stronger fidelity than:

```text
POSTGRESQL PRODUCTION
        ^
        |
       H2
```

for PostgreSQL-specific behavior.

For reusable code:

```text
DUPLICATION
    |
    v
IS THERE A STABLE
SHARED CONCEPT?
   / \
 NO   YES
 |     |
 v     v
KEEP  EXTRACT
LOCAL LIBRARY/PLATFORM
```

For InnerSource:

```text
CONSUMER TEAM
      |
      v
FINDS PLATFORM ISSUE
      |
      v
PROPOSES CONTRIBUTION
      |
      v
MAINTAINER REVIEW
      |
      v
SHARED IMPROVEMENT
```

Ownership remains explicit.

For engineering metrics:

```text
DO NOT OPTIMIZE FOR

LOC
Commits
PR Count
Hours Online
```

Instead:

```text
DELIVERY FLOW
      +
QUALITY
      +
RELIABILITY
      +
DEVELOPER EXPERIENCE
      +
BUSINESS OUTCOME
```

The platform lifecycle is:

```text
CREATE
  |
  v
DEVELOP
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
UPGRADE
  |
  v
DEPRECATE
  |
  v
DECOMMISSION
```

The complete engineering-productivity equation is:

```text
GOLDEN PATHS
      +
SELF-SERVICE
      +
SECURE DEFAULTS
      +
FAST FEEDBACK
      +
REPRESENTATIVE TESTING
      +
REUSABLE PLATFORM CAPABILITIES
      +
DISCOVERABLE DOCUMENTATION
      +
INNERSOURCE
      +
MEASURABLE DELIVERY FLOW
      =
SUSTAINABLE DEVELOPER PRODUCTIVITY
```

The governing principle is:

```text
A platform should not make
developers ask how to comply
with every engineering standard.

The platform should make
compliance the natural result
of using the standard path.

Domain teams should spend
their cognitive capacity primarily
on business problems, while
the engineering platform makes
secure, reliable and maintainable
software easier to build.
```

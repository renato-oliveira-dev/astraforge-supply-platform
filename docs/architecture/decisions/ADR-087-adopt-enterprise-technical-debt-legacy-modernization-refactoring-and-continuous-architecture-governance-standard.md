# ADR-087: Adopt Enterprise Technical Debt, Legacy Modernization, Refactoring and Continuous Architecture Governance Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-087 |
| Title | Adopt Enterprise Technical Debt, Legacy Modernization, Refactoring and Continuous Architecture Governance Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Architecture, Modernization, Refactoring, Technical Debt, Java |
| Related Work Items | Java 21, Spring Boot, SonarQube, SAST, JaCoCo, Microservices, Legacy Modernization |
| Supersedes | ADR-047, ADR-069 |
| Superseded By | None |

---

> **Consolidation:** The technical-debt, refactoring and legacy-modernization portions of ADR-069 are superseded by this ADR. Code-review governance formerly covered by ADR-069 is consolidated by ADR-086.

---

# 1. Context

Enterprise systems evolve over years or decades.

During this evolution, applications accumulate:

```text
LEGACY CODE

OBSOLETE FRAMEWORKS

DUPLICATED LOGIC

OUTDATED DEPENDENCIES

LARGE CLASSES

HIGH COUPLING

DEAD CODE

WORKAROUNDS

TEMPORARY SOLUTIONS

UNDOCUMENTED BUSINESS RULES

UNSAFE DATABASE COUPLING

INSUFFICIENT TESTS
```

Not all technical debt is inherently bad.

Some debt is an intentional trade-off:

```text
BUSINESS URGENCY
      |
      v
TEMPORARY TECHNICAL COMPROMISE
      |
      v
KNOWN FOLLOW-UP
```

The problem occurs when:

```text
TEMPORARY
```

silently becomes:

```text
PERMANENT
```

and nobody knows:

```text
WHY IT EXISTS

WHO OWNS IT

WHAT IT COSTS

WHAT BREAKS IF IT CHANGES
```

---

# 2. Problem Statement

The organization requires standards covering:

- technical debt
- legacy systems
- modernization
- Java modernization
- Java 3/legacy Java to Java 21
- Spring Boot modernization
- refactoring
- rewrite decisions
- replacement decisions
- strangler pattern
- incremental migration
- characterization tests
- dead-code removal
- duplication
- God Classes
- package coupling
- circular dependencies
- cyclomatic complexity
- cognitive complexity
- Sonar technical debt
- architecture fitness functions
- ADR compliance
- modernization roadmaps
- risk management
- continuous architecture

---

# 3. Decision Drivers

Primary drivers are:

1. maintainability
2. business continuity
3. modernization safety
4. delivery velocity
5. operational reliability
6. security
7. testability
8. architecture sustainability
9. cost control
10. knowledge preservation
11. incremental delivery
12. reduced rewrite risk

---

# 4. Decision

Technical debt SHALL be managed as an explicit engineering portfolio rather than an informal collection of complaints.

Modernization SHALL prefer:

```text
INCREMENTAL

MEASURABLE

TESTABLE

REVERSIBLE
```

change over uncontrolled large-scale rewrites.

---

# 5. Fundamental Principle

```text
Do not modernize
because code is old.

Modernize because
the current architecture
creates measurable
business,
security,
reliability,
maintenance,
or delivery risk.
```

---

# 6. Technical Debt Definition

Technical debt is a design or implementation condition that increases the future cost or risk of changing, operating or securing a system.

---

# 7. Debt Is Not Every Imperfection

Not every:

```text
CODE SMELL

OLD CLASS

LONG METHOD

OLD TECHNOLOGY
```

automatically justifies remediation.

---

# 8. Debt Must Have Consequence

Technical debt SHOULD be associated with an identifiable consequence.

Examples:

```text
SLOWER DELIVERY

HIGH DEFECT RATE

SECURITY EXPOSURE

PRODUCTION INCIDENTS

DIFFICULT TESTING

HIGH CHANGE FAILURE RATE

UNSUPPORTED PLATFORM

SCALING LIMIT

KNOWLEDGE RISK
```

---

# 9. Debt Classification

Technical debt SHOULD be classified.

Recommended categories:

```text
ARCHITECTURAL

CODE

TEST

SECURITY

DEPENDENCY

DATABASE

INFRASTRUCTURE

OBSERVABILITY

DOCUMENTATION

OPERATIONAL
```

---

# 10. Architectural Debt

Examples:

```text
CIRCULAR SERVICE DEPENDENCIES

SHARED DATABASES

GOD SERVICES

TIGHT SYNCHRONOUS COUPLING

UNDEFINED DOMAIN BOUNDARIES
```

---

# 11. Code Debt

Examples:

```text
DUPLICATION

GOD CLASSES

HIGH COMPLEXITY

DEAD CODE

INCONSISTENT ABSTRACTIONS
```

---

# 12. Test Debt

Examples:

```text
LOW COVERAGE

FLAKY TESTS

NO INTEGRATION TESTS

TESTS WITHOUT ASSERTIONS

EXCESSIVE MOCKING
```

---

# 13. Security Debt

Examples:

```text
UNSUPPORTED FRAMEWORK

WEAK AUTHORIZATION

HARDCODED CREDENTIALS

UNRESOLVED CVEs

OUTDATED TLS
```

---

# 14. Database Debt

Examples:

```text
SHARED SCHEMAS

MISSING CONSTRAINTS

UNINDEXED CRITICAL QUERIES

MANUAL SCHEMA CHANGES

LARGE UNCONTROLLED TABLES
```

---

# 15. Dependency Debt

Examples:

```text
EOL JAVA

EOL SPRING BOOT

ABANDONED LIBRARIES

MULTIPLE LIBRARIES FOR SAME CONCERN
```

---

# 16. Operational Debt

Examples:

```text
NO RUNBOOK

NO HEALTH CHECK

MANUAL DEPLOYMENT

NO ROLLBACK PROCESS

NO CAPACITY BASELINE
```

---

# 17. Debt Register

Material technical debt SHOULD be recorded.

---

# 18. Debt Record

A debt record SHOULD include:

```text
TITLE

CATEGORY

SYSTEM

DESCRIPTION

IMPACT

RISK

OWNER

PROPOSED ACTION

PRIORITY
```

---

# 19. Optional Metadata

Material debt MAY additionally include:

```text
ESTIMATED EFFORT

DEPENDENCIES

TARGET RELEASE

CREATION DATE

REVIEW DATE
```

---

# 20. Invisible Debt

Significant debt SHOULD NOT exist only as tribal knowledge.

---

# 21. TODO Is Not a Debt Register

A source-code TODO is insufficient governance for significant architectural debt.

---

# 22. Debt Ownership

Material debt SHOULD have an accountable owner/team.

---

# 23. Unowned Debt

Unowned technical debt tends to become permanent.

---

# 24. Debt Priority

Debt SHOULD be prioritized using risk and business impact rather than developer preference.

---

# 25. Priority Model

A useful model is:

```text
PRIORITY
   =
IMPACT
   x
PROBABILITY
   x
CHANGE FREQUENCY
```

with appropriate organizational normalization.

---

# 26. Change Frequency

Debt in code changed every week generally deserves greater attention than equivalent debt in stable code rarely touched.

---

# 27. Hotspot

Code hotspots SHOULD combine:

```text
COMPLEXITY

CHANGE FREQUENCY

DEFECT HISTORY
```

---

# 28. Hotspot Principle

The most complex code is not necessarily the highest priority.

The highest-risk code is often:

```text
COMPLEX
+
FREQUENTLY CHANGED
+
BUSINESS CRITICAL
```

---

# 29. Sonar Technical Debt

SonarQube remediation estimates MAY provide one debt signal.

---

# 30. Sonar Limitation

Sonar technical-debt minutes/hours MUST NOT be treated as the complete architecture debt inventory.

---

# 31. Security Priority

Critical security debt MAY override normal business prioritization.

---

# 32. EOL Priority

Unsupported runtime/framework dependencies SHOULD receive elevated priority.

---

# 33. Incident-Driven Debt

Repeated production incidents caused by the same architectural weakness MUST trigger remediation review.

---

# 34. Debt Budget

Teams SHOULD allocate recurring engineering capacity to debt reduction.

---

# 35. Debt Sprint

Debt SHOULD NOT necessarily be deferred to rare dedicated "cleanup sprints."

---

# 36. Continuous Improvement

Debt remediation SHOULD occur continuously alongside product development.

---

# 37. Boy Scout Rule

When modifying an area, engineers SHOULD leave it modestly better when safe and within scope.

---

# 38. Scope Discipline

The Boy Scout Rule MUST NOT become justification for uncontrolled refactoring inside an urgent production fix.

---

# 39. Opportunistic Refactoring

Small adjacent improvements MAY accompany functional changes when:

```text
RISK IS LOW

TESTS EXIST

REVIEW REMAINS CLEAR
```

---

# 40. Large Refactoring

Large refactoring SHOULD normally be separated from unrelated feature work.

---

# 41. Refactoring Definition

Refactoring changes internal structure while preserving externally observable behavior.

---

# 42. Behavior Preservation

A refactoring SHOULD NOT intentionally change business behavior.

---

# 43. Behavior Change

If behavior changes, it MUST be explicitly treated and tested as a functional change.

---

# 44. Refactoring Safety

Safe refactoring requires sufficient behavioral confidence.

---

# 45. Characterization Test

Legacy code without adequate tests SHOULD first receive characterization tests before major refactoring.

---

# 46. Characterization Test Purpose

A characterization test answers:

```text
WHAT DOES
THE SYSTEM
ACTUALLY DO TODAY?
```

not necessarily:

```text
WHAT SHOULD
THE SYSTEM DO?
```

---

# 47. Legacy Behavior

Characterization tests can preserve unexpected behavior intentionally until business owners decide whether it is correct.

---

# 48. Golden Master

Golden-master testing MAY be useful for complex deterministic legacy outputs.

---

# 49. Golden Master Limitation

Golden-master tests SHOULD NOT blindly freeze defects forever.

---

# 50. Refactoring Sequence

Preferred sequence:

```text
UNDERSTAND

CHARACTERIZE

TEST

REFACTOR

VERIFY

CLEAN UP
```

---

# 51. Small Steps

Refactoring SHOULD proceed through small verifiable steps.

---

# 52. Compile Frequently

During structural refactoring:

```text
COMPILE

TEST

COMMIT
```

frequently.

---

# 53. Large Bang Refactoring

Avoid:

```text
CHANGE 200 CLASSES
      |
      v
RUN TESTS
AT THE END
```

---

# 54. Dead Code

Confirmed dead code SHOULD be removed.

---

# 55. Git Is History

Source control is the historical archive.

Dead code does not need to remain commented out "in case we need it later."

---

# 56. Dead Method

Unused private methods SHOULD be removed.

---

# 57. Dead Class

Unused classes SHOULD be removed after confirming:

```text
REFLECTION

FRAMEWORK DISCOVERY

SERIALIZATION

CONFIGURATION

EXTERNAL CALLERS
```

do not require them.

---

# 58. Reflection Risk

Static "unused" analysis can be incorrect for reflection-driven frameworks.

---

# 59. Deprecated Code

Deprecated code SHOULD have:

```text
REPLACEMENT

MIGRATION PLAN

REMOVAL CONDITION
```

---

# 60. Permanent Deprecation

Deprecation MUST NOT become indefinite storage for obsolete APIs.

---

# 61. Duplication

Meaningful business-rule duplication SHOULD be reduced.

---

# 62. Superficial Duplication

Code that merely looks similar SHOULD NOT automatically be abstracted.

---

# 63. Rule of Three

Repeated patterns SHOULD generally be understood before creating a shared abstraction.

---

# 64. Wrong Abstraction

A wrong shared abstraction can be more expensive than controlled duplication.

---

# 65. Shared Business Rule

If multiple code paths implement the same business invariant, a single authoritative rule SHOULD be preferred.

---

# 66. Copy-Paste Fix

A defect fix duplicated across many copies indicates architectural debt.

---

# 67. God Class

Classes with excessive responsibilities SHOULD be decomposed.

---

# 68. God Class Signals

Signals include:

```text
MANY DEPENDENCIES

MANY UNRELATED METHODS

MANY REASONS TO CHANGE

HIGH COMPLEXITY

LARGE TEST SETUP

DIFFICULT CONSTRUCTOR
```

---

# 69. Dependency Count

A class requiring a very large number of injected dependencies SHOULD trigger responsibility review.

---

# 70. Constructor Smell

A constructor with 15–20+ collaborators usually indicates architectural concentration.

It is a signal, not an automatic rule.

---

# 71. Split by Responsibility

Large services SHOULD be decomposed according to cohesive responsibilities.

Example:

```text
OrderService
    |
    +--> OrderQueryService
    |
    +--> OrderCommandService
    |
    +--> OrderValidationService
    |
    +--> OrderIntegrationService
```

where domain boundaries justify the split.

---

# 72. Artificial Fragmentation

Decomposition MUST NOT create dozens of one-method classes without meaningful responsibility boundaries.

---

# 73. Cohesion

Classes SHOULD have high internal cohesion.

---

# 74. Coupling

Modules SHOULD minimize unnecessary coupling.

---

# 75. Package Coupling

Package dependencies SHOULD generally flow in an intentional direction.

---

# 76. Circular Package Dependency

Circular package dependencies SHOULD be eliminated.

---

# 77. Example

Avoid:

```text
controller
    |
    v
service
    |
    v
mapper
    |
    v
controller
```

---

# 78. Layer Direction

A typical layered architecture may use:

```text
CONTROLLER
    |
    v
APPLICATION / SERVICE
    |
    v
DOMAIN
    |
    v
INFRASTRUCTURE ADAPTER
```

with exact dependencies determined by the chosen architecture.

---

# 79. Domain Independence

Domain/business rules SHOULD avoid unnecessary dependency on infrastructure frameworks.

---

# 80. Framework Leakage

Framework types SHOULD remain near framework boundaries where practical.

---

# 81. Circular Service Dependency

Synchronous circular dependencies between microservices SHOULD be eliminated.

---

# 82. Service Cycle Example

Avoid:

```text
ORDERS
   |
   v
CUSTOMERS
   |
   v
ORDERS
```

---

# 83. Complexity

Cyclomatic and cognitive complexity SHOULD be monitored.

---

# 84. Complexity Threshold

A static-analysis threshold is a review signal, not an invitation to mechanically split code until the number turns green.

---

# 85. Cognitive Complexity

High cognitive complexity often indicates:

```text
NESTING

MULTIPLE CONDITIONS

MULTIPLE RESPONSIBILITIES

HIDDEN STATE
```

---

# 86. Early Return

Early returns MAY reduce nesting and improve readability.

---

# 87. Extraction

Complex conditions SHOULD be extracted into named predicates/business rules when this improves meaning.

---

# 88. Boolean Expression

Prefer:

```java
if (isEligibleForApproval(order, user)) {
    ...
}
```

over repeating a long condition across multiple methods.

---

# 89. Business Rule Object

Complex reusable business rules MAY be represented through:

```text
POLICY

SPECIFICATION

VALIDATOR

RULE
```

depending on semantics.

---

# 90. Strategy Explosion

Patterns MUST NOT be introduced merely to demonstrate pattern usage.

---

# 91. Design Pattern

Use a pattern when it reduces actual complexity.

---

# 92. Abstraction Cost

Every abstraction has:

```text
DISCOVERY COST

INDIRECTION COST

MAINTENANCE COST
```

---

# 93. Clean Code

Clean code prioritizes understandability and change safety rather than maximum abstraction.

---

# 94. Method Size

Long methods SHOULD be reviewed for multiple responsibilities.

---

# 95. Tiny Method Explosion

Breaking every few lines into a method can reduce readability and SHOULD be avoided.

---

# 96. Naming

Names SHOULD communicate domain intent.

---

# 97. Generic Name

Avoid excessive:

```text
Manager

Helper

Utils

Processor
```

when a more precise responsibility exists.

---

# 98. Utility Class

Utility classes SHOULD contain genuinely cohesive stateless functionality.

---

# 99. `CommonUtils`

A growing generic `CommonUtils` class SHOULD be treated as a design smell.

---

# 100. Static State

Mutable global/static state SHOULD be avoided.

---

# 101. Hidden Dependency

Dependencies SHOULD be explicit.

---

# 102. Service Locator

Service-locator patterns SHOULD generally be avoided in application code.

---

# 103. Dependency Injection

Constructor injection SHOULD be preferred for required collaborators.

---

# 104. Optional Dependency

Optional collaborators SHOULD be rare and semantically explicit.

---

# 105. Legacy Modernization

Legacy modernization MUST begin with system understanding.

---

# 106. Modernization Inventory

Before modernization, inventory:

```text
BUSINESS CAPABILITIES

INTEGRATIONS

DATABASES

BATCH JOBS

REPORTS

USERS

DEPENDENCIES

DEPLOYMENT MODEL

SECURITY

INCIDENT HISTORY
```

---

# 107. Hidden Integration

Legacy systems frequently contain undocumented:

```text
FILES

DATABASE LINKS

SCHEDULED JOBS

REPORTS

FTP/SFTP

EMAIL

DIRECT SQL

MANUAL OPERATIONS
```

---

# 108. Discovery

Modernization MUST account for these dependencies before retirement.

---

# 109. Business Rule Discovery

Legacy code often serves as executable documentation for business rules.

---

# 110. Old Code Is Evidence

Do not discard legacy implementation before understanding the rules encoded within it.

---

# 111. Rewrite Risk

A full rewrite discards years of accumulated behavioral knowledge unless deliberately recovered.

---

# 112. Rewrite Default

Full rewrite MUST NOT be the default modernization strategy.

---

# 113. Rewrite Justification

Rewrite MAY be justified when:

```text
CURRENT ARCHITECTURE BLOCKS REQUIRED CHANGE

TECHNOLOGY IS UNSUPPORTABLE

SECURITY RISK IS UNACCEPTABLE

INCREMENTAL MIGRATION COST EXCEEDS REPLACEMENT

BUSINESS DOMAIN IS SUFFICIENTLY UNDERSTOOD
```

---

# 114. Rewrite Evidence

A rewrite decision SHOULD have explicit architectural/business analysis.

---

# 115. Second-System Effect

Rewrite projects MUST account for the risk of attempting to solve every historical problem simultaneously.

---

# 116. Feature Parity Trap

Blind 100% feature parity SHOULD NOT automatically be required.

---

# 117. Feature Inventory

Before migration, classify legacy features:

```text
REQUIRED

USED BUT REPLACEABLE

RARELY USED

OBSOLETE

UNKNOWN
```

---

# 118. Usage Evidence

Telemetry/business evidence SHOULD inform whether legacy functionality needs migration.

---

# 119. Strangler Pattern

The Strangler Fig pattern SHOULD be preferred for many large legacy modernization efforts.

---

# 120. Strangler Flow

```text
LEGACY SYSTEM
      |
      +--> CAPABILITY A
      |
      +--> CAPABILITY B
      |
      +--> CAPABILITY C

MIGRATE A
      |
      v
NEW SERVICE A

MIGRATE B
      |
      v
NEW SERVICE B

EVENTUALLY
      |
      v
RETIRE LEGACY
```

---

# 121. Incremental Cutover

Capabilities SHOULD be migrated incrementally where feasible.

---

# 122. Routing

A gateway/router/facade MAY route requests between legacy and modern implementations during transition.

---

# 123. Anti-Corruption Layer

An Anti-Corruption Layer SHOULD protect the new domain model from legacy semantics where appropriate.

---

# 124. Legacy DTO Leakage

New services SHOULD NOT simply reproduce legacy database structures as their domain model without analysis.

---

# 125. Data Ownership Migration

Modernization MUST explicitly determine future data ownership.

---

# 126. Shared Database Trap

Creating microservices while retaining uncontrolled shared-table ownership does not produce service autonomy.

---

# 127. Database Decomposition

Database decomposition SHOULD follow business ownership boundaries.

---

# 128. Dual Database Period

Temporary synchronization between legacy and new persistence MAY be necessary.

---

# 129. Data Synchronization

Temporary synchronization MUST define:

```text
SOURCE OF TRUTH

DIRECTION

CONSISTENCY MODEL

FAILURE RECOVERY

END DATE
```

---

# 130. Permanent Dual Write

Permanent unmanaged dual writes are prohibited.

---

# 131. Java Modernization

Java modernization SHOULD separate:

```text
RUNTIME UPGRADE

FRAMEWORK UPGRADE

CODE MODERNIZATION

ARCHITECTURE CHANGE
```

where practical.

---

# 132. Avoid Simultaneous Variables

Changing all four simultaneously makes failures difficult to isolate.

---

# 133. Upgrade Ladder

Very old Java applications MAY require staged upgrades.

Conceptually:

```text
LEGACY JAVA
     |
     v
COMPILABLE SUPPORTED INTERMEDIATE STATE
     |
     v
MODERN FRAMEWORK
     |
     v
JAVA 21
```

Exact steps depend on framework compatibility.

---

# 134. Java 21

Modernized Java services SHOULD target the approved Java 21 enterprise baseline where applicable.

---

# 135. Java Language Features

Modern Java features SHOULD be adopted when they improve clarity.

Examples:

```text
RECORDS

SWITCH EXPRESSIONS

PATTERN MATCHING

TEXT BLOCKS

VIRTUAL THREADS
```

---

# 136. Feature Fashion

New Java features SHOULD NOT be used merely because they are new.

---

# 137. Record

Records SHOULD be considered for immutable data carriers.

---

# 138. Record Domain Model

Records SHOULD NOT automatically replace mutable persistence/domain entities.

---

# 139. Virtual Threads

Virtual Threads MAY simplify I/O-bound concurrency but do not eliminate capacity limits.

---

# 140. Legacy Thread Pool

Existing thread-pool assumptions MUST be reviewed before adopting Virtual Threads.

---

# 141. Spring Boot Modernization

Spring Boot upgrades SHOULD follow supported release paths.

---

# 142. Framework Migration

Framework migrations SHOULD first establish behavioral compatibility before opportunistic redesign.

---

# 143. `javax` to `jakarta`

Migration to modern Spring Boot generations requires deliberate handling of:

```text
javax.*
```

to:

```text
jakarta.*
```

ecosystem changes where applicable.

---

# 144. Hibernate Upgrade

ORM upgrades require validation of:

```text
QUERY GENERATION

FETCHING

PAGINATION

TYPE MAPPING

TRANSACTIONS
```

---

# 145. Security Upgrade

Spring Security modernization MUST validate authorization semantics, not merely compilation.

---

# 146. Deprecated API

Deprecated framework APIs SHOULD be eliminated during modernization when safe.

---

# 147. Compiler Warnings

Modernization SHOULD reduce meaningful compiler warnings.

---

# 148. Build Modernization

Legacy builds SHOULD migrate toward the approved Gradle baseline where appropriate.

---

# 149. Dependency Cleanup

Modernization SHOULD remove obsolete libraries no longer needed by modern JDK/framework capabilities.

---

# 150. Test Modernization

Legacy tests SHOULD migrate toward current:

```text
JUnit 5

AssertJ

Mockito

Testcontainers
```

standards where applicable.

---

# 151. Test Preservation

Tests SHOULD be migrated before or alongside production code rather than discarded wholesale.

---

# 152. Test Rewrite Risk

Rewriting implementation and tests simultaneously can eliminate independent behavioral evidence.

---

# 153. Baseline First

Before large modernization, establish a baseline:

```text
BUILD STATUS

TEST RESULTS

COVERAGE

SONAR

SAST

SCA

PERFORMANCE

PRODUCTION BEHAVIOR
```

---

# 154. Improvement Measurement

Modernization SHOULD demonstrate measurable improvement against the baseline.

---

# 155. Modernization Metrics

Useful measures include:

```text
BUILD TIME

DEPLOYMENT FREQUENCY

LEAD TIME

CHANGE FAILURE RATE

MTTR

DEFECT RATE

COVERAGE

COMPLEXITY

VULNERABILITIES

DEPENDENCY AGE

INCIDENT COUNT
```

---

# 156. Lines of Code

Reducing lines of code MAY be useful but MUST NOT be a primary modernization success metric.

---

# 157. Number of Microservices

Increasing microservice count is NOT a modernization success metric.

---

# 158. Architecture Outcome

Modernization success SHOULD be measured through improved business and engineering outcomes.

---

# 159. Refactor vs Rewrite vs Replace

Every major modernization initiative SHOULD explicitly consider:

```text
REFACTOR

REWRITE

REPLACE
```

---

# 160. Refactor

Prefer refactoring when:

```text
CORE BEHAVIOR IS VALUABLE

ARCHITECTURE CAN EVOLVE

TESTABILITY CAN BE IMPROVED

INCREMENTAL CHANGE IS FEASIBLE
```

---

# 161. Rewrite

Consider rewriting when:

```text
ARCHITECTURE IS FUNDAMENTALLY INCOMPATIBLE

IMPLEMENTATION IS NOT ECONOMICALLY RECOVERABLE

DOMAIN IS WELL UNDERSTOOD

MIGRATION CAN BE CONTROLLED
```

---

# 162. Replace

Consider replacing with a product/platform when:

```text
CAPABILITY IS COMMODITY

BUILDING IT PROVIDES LITTLE DIFFERENTIATION

A SUPPORTED SOLUTION SATISFIES REQUIREMENTS
```

---

# 163. Buy vs Build

Replacement decisions MUST consider:

```text
LICENSING

INTEGRATION

VENDOR LOCK-IN

DATA PORTABILITY

SECURITY

CUSTOMIZATION

TOTAL COST
```

---

# 164. Sunk Cost

Historical investment alone MUST NOT determine future architecture.

---

# 165. Rewrite Excitement

Developer preference for newer technology alone MUST NOT justify a rewrite.

---

# 166. Architecture Governance

Architecture SHALL be continuously validated rather than reviewed only at project inception.

---

# 167. Continuous Architecture

Architecture decisions SHOULD evolve with:

```text
BUSINESS

SCALE

TECHNOLOGY

SECURITY

OPERATIONS
```

while maintaining deliberate governance.

---

# 168. ADR

Material architecture decisions MUST be documented through ADRs.

---

# 169. ADR Compliance

Implementation SHOULD remain consistent with accepted ADRs.

---

# 170. Architecture Change

When an ADR is no longer appropriate:

```text
DO NOT SILENTLY IGNORE IT
```

Create a superseding decision.

---

# 171. ADR Is Not Eternal

An ADR records the best decision under known constraints at a point in time.

---

# 172. Superseded ADR

Superseded ADRs SHOULD remain in history.

---

# 173. Architecture Fitness Function

Architecture rules that can be automated SHOULD become fitness functions.

---

# 174. Examples

Fitness functions MAY verify:

```text
NO CIRCULAR PACKAGE DEPENDENCIES

NO CONTROLLER -> REPOSITORY ACCESS

NO CROSS-SERVICE DATABASE ACCESS

NO FORBIDDEN DEPENDENCIES

NO APPLIED FLYWAY MODIFICATION

COVERAGE >= THRESHOLD

NO CRITICAL SONAR ISSUES
```

---

# 175. ArchUnit

ArchUnit SHOULD be considered for Java architecture rules.

---

# 176. Package Rule

Example concept:

```text
controllers
    may depend on
services

but not directly on
repositories
```

---

# 177. Architecture Test

Architecture tests SHOULD execute in CI.

---

# 178. Architecture Test Value

Automated architecture tests prevent gradual erosion after the architecture review is finished.

---

# 179. Dependency Rule

Module/package dependency directions SHOULD be machine-enforced where practical.

---

# 180. Naming Rule

Critical naming/package conventions MAY be enforced where they materially improve architecture.

---

# 181. Over-Governance

Fitness functions MUST NOT enforce cosmetic architecture preferences with no material value.

---

# 182. Governance Goal

Governance exists to:

```text
REDUCE RISK

PRESERVE INTENT

ENABLE CHANGE
```

not to maximize rules.

---

# 183. Architecture Drift

Architecture drift SHOULD be monitored.

---

# 184. Drift Examples

```text
CONTROLLERS ACCESSING REPOSITORIES

DOMAIN IMPORTING WEB TYPES

NEW SHARED DATABASE QUERIES

UNAPPROVED FRAMEWORKS

NEW CIRCULAR DEPENDENCIES
```

---

# 185. Drift Remediation

Small drift SHOULD be corrected early.

---

# 186. Broken Windows

Repeatedly accepting small architectural violations makes later modernization substantially more expensive.

---

# 187. Exception

Architecture exceptions MAY be approved.

---

# 188. Exception Metadata

Exceptions SHOULD contain:

```text
REASON

OWNER

SCOPE

EXPIRATION / REVIEW
```

---

# 189. Permanent Exception

Permanent architecture exceptions SHOULD be rare.

---

# 190. Refactoring Test Gate

A behavior-preserving refactor MUST pass the existing behavioral test suite before and after the change.

---

# 191. Test Failure

If an existing test changes during refactoring, the reason MUST be understood.

---

# 192. Delete Failing Test

Deleting a failing test merely to complete a refactor is prohibited.

---

# 193. Coverage

Refactoring SHOULD NOT materially reduce useful coverage.

---

# 194. Coverage Improvement

High-risk legacy code SHOULD receive targeted coverage improvement before structural change.

---

# 195. Sonar

Refactoring SHOULD reduce rather than relocate Sonar debt.

---

# 196. Complexity Gaming

Moving a complex method into another class solely to make a metric pass does not resolve the underlying debt.

---

# 197. SAST

Modernization MUST not weaken security controls to simplify legacy migration.

---

# 198. Security Modernization

Legacy security mechanisms SHOULD be migrated toward approved standards.

---

# 199. Logging

Legacy modernization SHOULD remove unsafe:

```text
System.out

printStackTrace

TOKEN LOGGING

PASSWORD LOGGING
```

---

# 200. Exception Handling

Legacy broad exception handling SHOULD be reviewed.

Example smell:

```java
catch (Exception ex) {
    // ignore
}
```

---

# 201. Swallowed Exception

Exceptions MUST NOT disappear silently when failure matters.

---

# 202. Log and Rethrow

Exception handling MUST avoid unnecessary duplicate logging across layers.

---

# 203. Sonar Exception Rule

Where an exception is caught, handling SHOULD satisfy both semantic correctness and applicable static-analysis requirements.

---

# 204. Transaction Modernization

Legacy transaction boundaries SHOULD be reviewed before decomposing services/classes.

---

# 205. Hidden Transaction

Splitting one local transaction into multiple service calls changes consistency semantics.

---

# 206. Microservice Extraction

Extracting a microservice is not merely moving classes to another repository.

---

# 207. Extraction Requires

A real extraction requires analysis of:

```text
DOMAIN BOUNDARY

DATA OWNERSHIP

TRANSACTION

API

FAILURE

SECURITY

OBSERVABILITY

DEPLOYMENT
```

---

# 208. Distributed Monolith

A system of microservices with:

```text
SHARED DATABASE

SYNCHRONOUS CHAINS

LOCKSTEP DEPLOYMENTS

SHARED INTERNAL MODELS
```

is effectively a distributed monolith.

---

# 209. Distributed Monolith Risk

Modernization MUST avoid replacing an in-process monolith with a network-distributed monolith.

---

# 210. Monolith

A well-structured modular monolith MAY be preferable to premature microservices.

---

# 211. Modular Monolith

Legacy modernization MAY first establish modular boundaries inside the existing application.

---

# 212. Extraction Later

Modules can later become extraction candidates when operational independence provides value.

---

# 213. Service Extraction Criterion

A module SHOULD become a separate service only when independent:

```text
OWNERSHIP

SCALING

DEPLOYMENT

SECURITY

RELIABILITY
```

provides material value.

---

# 214. Performance Baseline

Refactoring performance-critical code requires a baseline.

---

# 215. Optimization

Do not combine speculative performance optimization with unrelated refactoring.

---

# 216. Benchmark

Critical algorithmic changes SHOULD use measurement rather than intuition.

---

# 217. Database Query Refactoring

Repository/query refactoring SHOULD compare:

```text
QUERY COUNT

EXECUTION PLAN

LATENCY

ROWS SCANNED
```

where relevant.

---

# 218. Remote Call Refactoring

Integration refactoring SHOULD verify:

```text
CALL COUNT

TIMEOUT

CONCURRENCY

ERROR MAPPING
```

---

# 219. N+1

Modernization SHOULD eliminate N+1 patterns rather than reproduce them in new abstractions.

---

# 220. Cache

Caching MUST NOT be introduced merely to hide an inefficient architecture without understanding consistency requirements.

---

# 221. Documentation Modernization

Documentation SHOULD be updated as legacy assumptions are discovered.

---

# 222. Tribal Knowledge

Critical modernization knowledge MUST not remain only with one senior developer.

---

# 223. Knowledge Transfer

Modernization SHOULD create:

```text
ADRs

DIAGRAMS

RUNBOOKS

CONTRACTS

TESTS
```

as durable knowledge.

---

# 224. Bus Factor

High bus-factor risk SHOULD influence modernization priority.

---

# 225. Legacy Specialist

Legacy specialists SHOULD participate in modernization rather than being consulted only after migration failures.

---

# 226. Business Participation

Business/domain specialists SHOULD validate discovered legacy rules.

---

# 227. Unknown Rule

When behavior is not understood:

```text
DO NOT GUESS
```

instrument, test and investigate.

---

# 228. Parallel Run

Critical migrations MAY use parallel run.

---

# 229. Parallel Run Concept

```text
SAME INPUT
   |
   +--> LEGACY
   |
   +--> NEW
   |
   v
COMPARE OUTPUT
```

---

# 230. Shadow Traffic

Shadow traffic MAY validate new implementations without making them authoritative.

---

# 231. Side Effects

Shadow execution MUST prevent duplicate external side effects.

---

# 232. Comparison

Output comparison SHOULD account for legitimate nondeterministic fields.

---

# 233. Reconciliation

Differences between legacy and new implementations MUST be categorized.

---

# 234. Cutover Criteria

Modernization cutover SHOULD have explicit criteria.

---

# 235. Example Cutover Criteria

```text
FUNCTIONAL PARITY ACCEPTED

ERROR RATE ACCEPTABLE

LATENCY ACCEPTABLE

DATA RECONCILED

SECURITY VALIDATED

ROLLBACK READY

OBSERVABILITY READY
```

---

# 236. Cutover

Cutover SHOULD be reversible where practical.

---

# 237. Canary Migration

A subset of traffic/customers MAY migrate first where business architecture permits.

---

# 238. Big-Bang Cutover

Big-bang replacement SHOULD require explicit justification.

---

# 239. Legacy Retirement

Legacy retirement is part of modernization.

---

# 240. Modernization Not Complete

Modernization is not complete while obsolete systems remain indefinitely running without purpose.

---

# 241. Retirement Checklist

Before retirement verify:

```text
NO ACTIVE TRAFFIC

NO BATCH JOB

NO FILE INTEGRATION

NO REPORT DEPENDENCY

NO DATABASE DEPENDENCY

NO SUPPORT PROCESS

DATA RETENTION HANDLED
```

---

# 242. Decommission

Decommission SHOULD remove unnecessary:

```text
INFRASTRUCTURE

CREDENTIALS

NETWORK RULES

DATABASE USERS

SECRETS

MONITORING

LICENSES
```

---

# 243. Zombie System

A legacy system receiving no known traffic but still running indefinitely is a security and operational liability.

---

# 244. Cost Realization

Modernization benefits SHOULD include actual retirement of obsolete operational cost.

---

# 245. Modernization Roadmap

Large modernization initiatives MUST have an incremental roadmap.

---

# 246. Roadmap Structure

A roadmap SHOULD contain:

```text
CURRENT STATE

TARGET STATE

TRANSITION STATES

DEPENDENCIES

RISKS

MILESTONES

EXIT CRITERIA
```

---

# 247. Target Architecture

Target architecture SHOULD describe capabilities and constraints, not merely desired technologies.

---

# 248. Transition Architecture

Intermediate states MUST be designed explicitly.

---

# 249. Temporary Architecture

Temporary migration components SHOULD have removal conditions.

---

# 250. Modernization Slice

A modernization slice SHOULD deliver independently verifiable value.

---

# 251. Horizontal Rewrite

Avoid spending months replacing infrastructure layers without producing a usable business slice where vertical slicing is feasible.

---

# 252. Vertical Slice

Prefer migration such as:

```text
ONE BUSINESS CAPABILITY
        |
        +--> API
        +--> RULES
        +--> DATA
        +--> TESTS
        +--> OBSERVABILITY
        +--> DEPLOYMENT
```

---

# 253. Progress

Modernization progress SHOULD be demonstrated through working migrated capabilities.

---

# 254. Percent Complete

"80% of code rewritten" is a weak modernization progress measure.

---

# 255. Better Progress

Prefer:

```text
CAPABILITIES MIGRATED

TRAFFIC MIGRATED

LEGACY DEPENDENCIES REMOVED

INCIDENTS REDUCED

DEPLOYMENT TIME REDUCED
```

---

# 256. Technical Debt Review Checklist

```text
[ ] What concrete problem does this debt cause?

[ ] Is the debt architectural, code, test, security or operational?

[ ] How frequently is the affected code changed?

[ ] Is it business critical?

[ ] Has it caused incidents?

[ ] Does it block delivery?

[ ] Is the technology unsupported?

[ ] Is there a security exposure?

[ ] Who owns the debt?

[ ] What happens if we do nothing?

[ ] Can it be remediated incrementally?

[ ] Are characterization tests required first?

[ ] Is refactoring sufficient?

[ ] Is rewrite actually justified?

[ ] Would replacement be cheaper?

[ ] Are hidden integrations understood?

[ ] Are business rules understood?

[ ] Is data ownership understood?

[ ] Is there a measurable baseline?

[ ] What is the target architecture?

[ ] What are the transition states?

[ ] How will progress be measured?

[ ] How will the legacy component be retired?
```

---

# 257. Refactoring Review Checklist

```text
[ ] Is observable behavior intended to remain unchanged?

[ ] Do adequate tests exist?

[ ] Are characterization tests required?

[ ] Is the refactoring scope focused?

[ ] Is unrelated feature work excluded?

[ ] Is duplication actually the same business concept?

[ ] Does the new abstraction improve understanding?

[ ] Are responsibilities more cohesive?

[ ] Is coupling reduced?

[ ] Are circular dependencies reduced?

[ ] Is complexity genuinely reduced?

[ ] Is dead code removed?

[ ] Are tests still meaningful?

[ ] Has coverage remained adequate?

[ ] Does Sonar improve?

[ ] Does SAST remain clean?

[ ] Is performance preserved?

[ ] Are database queries preserved/improved?

[ ] Are remote-call counts preserved/improved?
```

---

# 258. Modernization Fitness Functions

Stable controls SHOULD be automated where practical.

Examples:

```text
[ ] No forbidden package dependencies

[ ] No circular module dependencies

[ ] Controllers do not access repositories directly

[ ] Domain does not depend on web infrastructure

[ ] No new critical Sonar issues

[ ] No new critical SAST issues

[ ] Coverage does not regress below policy

[ ] No unsupported Java baseline

[ ] No unsupported Spring Boot baseline

[ ] No applied Flyway migration modification

[ ] No new cross-service database access

[ ] No new synchronous service cycle

[ ] No prohibited dependency

[ ] No production System.out/printStackTrace
```

---

# 259. Enterprise Modernization Gate

A modernization initiative is not considered compliant when applicable conditions include:

```text
[ ] Rewrite was chosen because developers prefer newer technology

[ ] Legacy business rules were not investigated

[ ] Existing behavior has no characterization coverage

[ ] New and old implementations cannot be compared

[ ] Data ownership remains undefined

[ ] Microservices continue sharing uncontrolled tables

[ ] Refactoring intentionally changes behavior without declaring it

[ ] Failing tests were deleted to complete modernization

[ ] Sonar debt was merely moved between classes

[ ] Large God Class was replaced by dozens of meaningless tiny classes

[ ] Circular dependency remains hidden

[ ] Architecture fitness functions are absent for critical rules

[ ] Migration has no measurable baseline

[ ] Roadmap has no transition states

[ ] Big-bang cutover has no justification

[ ] Legacy retirement is not planned

[ ] Temporary compatibility components have no removal condition

[ ] Modernization success is measured only by lines of code or microservice count
```

---

# 260. Anti-Patterns

The following are prohibited or strongly discouraged:

- rewrite because "the code is old"
- rewrite because "microservices are modern"
- rewrite without business-rule discovery
- giant refactor without characterization tests
- deleting failing tests
- abstraction for abstraction's sake
- God Classes
- meaningless class fragmentation
- generic `CommonUtils`
- circular dependencies
- permanent deprecated code
- commented-out dead code
- copying legacy database structure directly into new domain models
- permanent dual writes
- microservices sharing the same database ownership
- changing Java, framework, architecture and behavior simultaneously without need
- measuring modernization by microservice count
- measuring modernization by rewritten lines
- modernization without legacy retirement
- temporary architecture without expiration
- ignoring ADRs without superseding them

---

# 261. Positive Consequences

The decision provides:

- explicit technical-debt visibility
- better modernization prioritization
- safer legacy migration
- reduced rewrite risk
- improved testability
- lower architecture drift
- reduced complexity
- improved code ownership
- stronger architectural enforcement
- better knowledge preservation
- incremental business delivery
- measurable modernization outcomes

---

# 262. Negative Consequences

The decision introduces:

- debt inventory maintenance
- characterization-test effort
- incremental migration complexity
- temporary compatibility layers
- architecture fitness-test maintenance
- longer transition periods
- deliberate legacy-retirement work

These costs are accepted because uncontrolled rewrites and unmanaged technical debt produce significantly greater long-term risk.

---

# 263. Neutral Consequences

The decision also means:

- some legacy code may remain unchanged because its risk is low
- some duplication may be preferable to a wrong abstraction
- some modernization efforts should remain monolithic/modular rather than become microservices
- not every Sonar smell deserves immediate refactoring
- not every modernization requires a rewrite
- architecture decisions can legitimately change over time
- transitional architectures may temporarily appear more complex than either the legacy or target state

---

# 264. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Rewrite failure | Critical | Medium | Incremental strangler migration |
| Unknown business rule | Critical | High | Characterization tests |
| Architecture drift | High | Medium | Fitness functions |
| Modernization regression | High | Medium | Baseline + automated tests |
| Data inconsistency | Critical | Medium | Explicit ownership/reconciliation |
| Permanent transition layer | High | Medium | Removal criteria |
| Over-engineering | Medium | Medium | Value-based architecture |
| Knowledge loss | High | Medium | ADRs/tests/documentation |
| Legacy never retired | High | Medium | Retirement milestones |
| Technical debt accumulation | High | High | Continuous debt governance |

---

# 265. Implementation Guidance

The following rules are mandatory:

1. Material technical debt must have identifiable impact and ownership.
2. Debt must be prioritized by risk, business impact and change frequency.
3. Security and unsupported-platform debt receive elevated priority.
4. Debt reduction should occur continuously.
5. Large refactoring must be separated from unrelated functional changes where practical.
6. Legacy code without adequate tests must receive characterization coverage before high-risk restructuring.
7. Refactoring must preserve intended observable behavior.
8. Dead code must be removed after dependency verification.
9. Business-rule duplication should have a single authoritative implementation where appropriate.
10. God Classes and excessive dependency concentration must trigger responsibility review.
11. Decomposition must improve cohesion rather than merely increase class count.
12. Circular package/module/service dependencies must be eliminated.
13. Complexity metrics must guide review rather than encourage metric gaming.
14. Modernization must inventory business capabilities, integrations, data and operational dependencies.
15. Full rewrite must require explicit justification.
16. Incremental strangler migration should be preferred for suitable large legacy systems.
17. Modernized services must establish clear data ownership.
18. Permanent unmanaged dual writes are prohibited.
19. Java/framework/architecture changes should be separated into diagnosable stages where practical.
20. Modern Java features should be used when they improve clarity or operational characteristics.
21. Modernization must establish measurable current-state baselines.
22. Refactor/rewrite/replace alternatives must be evaluated for major modernization.
23. Architecture decisions must remain documented through ADRs.
24. Obsolete ADRs must be superseded rather than silently ignored.
25. Critical architecture rules should become automated fitness functions.
26. Architecture exceptions must be explicit and reviewable.
27. Parallel/shadow execution may be used to validate critical migrations while preventing duplicate side effects.
28. Cutover must have explicit acceptance and rollback criteria.
29. Legacy retirement must be part of the modernization roadmap.
30. Temporary migration infrastructure must have explicit removal criteria.
31. Modernization progress must be measured through capabilities, traffic, reliability and legacy retirement rather than rewritten lines of code.
32. Quality, Sonar, SAST, coverage, Flyway and CI/CD standards remain applicable throughout modernization.

---

# 266. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Gradle
- JUnit 5
- AssertJ
- Mockito
- Testcontainers
- JaCoCo
- SonarQube
- SAST
- SCA
- ArchUnit
- Flyway
- PostgreSQL
- OpenAPI
- architecture fitness functions
- dependency analysis
- characterization tests
- integration tests
- performance baselines
- CI/CD quality gates

---

# 267. Success Criteria

The decision is successful when:

- technical debt is visible and prioritized
- high-risk legacy code has behavioral protection
- modernization can progress incrementally
- full rewrites become exceptional rather than default
- complexity decreases in frequently changed areas
- architectural cycles decrease
- unsupported platform dependencies are removed
- new architecture drift is automatically detected
- modernized capabilities can be compared with legacy behavior
- transition components have explicit retirement paths
- obsolete legacy infrastructure is actually decommissioned
- modernization produces measurable improvements in delivery, reliability and maintainability

---

# 268. Alternatives Rejected

## 268.1 Rewrite Every Legacy System

Rejected because full rewrites have high business-rule discovery, migration and delivery risk.

---

## 268.2 Never Refactor Working Code

Rejected because unmanaged debt progressively increases delivery and operational risk.

---

## 268.3 Fix Every Sonar Smell Immediately

Rejected because remediation must consider risk, change frequency and business value.

---

## 268.4 Convert Everything to Microservices

Rejected because distribution introduces operational and consistency complexity and is not inherently superior to a modular monolith.

---

## 268.5 Big-Bang Modernization

Rejected as the default because it delays feedback and concentrates migration risk.

---

## 268.6 Preserve Legacy Forever

Rejected because unsupported systems accumulate security, knowledge and operational risk.

---

## 268.7 Architecture Review Only at Project Start

Rejected because architecture continuously evolves through everyday code changes.

---

# 269. Related Decisions

This ADR extends and implements:

- ADR-013: Use Testcontainers for Integration Testing
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-064: Enterprise API Design, REST, HTTP and Contract Governance Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-083: Enterprise Service-to-Service Communication, Service Discovery, Internal APIs and Zero-Trust Networking Standard
- ADR-084: Enterprise Database Schema Evolution, Flyway, Zero-Downtime Migration and Data Backfill Standard
- ADR-085: Enterprise Dependency Management, Gradle, SBOM, Supply Chain Security and Vulnerability Governance Standard
- ADR-086: Enterprise Code Review, Pull Request, Branching, Commit, CI/CD Quality Gates and Definition of Done Standard

---

# 270. References

- Martin Fowler — Refactoring
- Martin Fowler — Strangler Fig Application
- Michael Feathers — Working Effectively with Legacy Code
- Eric Evans — Domain-Driven Design
- Sam Newman — Monolith to Microservices
- Neal Ford et al. — Building Evolutionary Architectures
- SonarQube Documentation
- ArchUnit Documentation
- Java 21 Documentation
- Spring Boot Documentation
- Gradle Documentation
- Testcontainers Documentation
- OWASP SAMM
- NIST Secure Software Development Framework
- Google Engineering Practices
- DORA Research

---

# 271. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial technical debt, modernization and continuous architecture governance baseline |

---

# 272. Decision Summary

Technical debt becomes:

```text
PROBLEM
   |
   v
IDENTIFY IMPACT
   |
   v
CLASSIFY
   |
   v
MEASURE RISK
   |
   v
ASSIGN OWNER
   |
   v
PRIORITIZE
   |
   v
REMEDIATE
   |
   v
VERIFY IMPROVEMENT
```

Legacy refactoring becomes:

```text
LEGACY CODE
    |
    v
UNDERSTAND
    |
    v
CHARACTERIZATION TESTS
    |
    v
SMALL REFACTOR
    |
    v
TEST
    |
    v
NEXT STEP
```

Modernization becomes:

```text
LEGACY
   |
   +--> CAPABILITY A
   +--> CAPABILITY B
   +--> CAPABILITY C
   |
   v
STRANGLE INCREMENTALLY
   |
   +--> MODERN A
   +--> MODERN B
   +--> MODERN C
   |
   v
RETIRE LEGACY
```

Architecture governance becomes:

```text
ADR
 |
 v
ARCHITECTURE RULE
 |
 v
FITNESS FUNCTION
 |
 v
CI
 |
 +--> PASS --> MERGE
 |
 +--> FAIL --> REVIEW
```

Modernization decision-making becomes:

```text
CURRENT SYSTEM
      |
      v
CAN IT EVOLVE SAFELY?
      |
   +--+--+
   |     |
  YES    NO
   |     |
   v     v
REFACTOR  IS CAPABILITY
          COMMODITY?
             |
          +--+--+
          |     |
         YES    NO
          |     |
          v     v
       REPLACE REWRITE
```

A God Class becomes:

```text
ONE CLASS
 |
 +--> QUERIES
 +--> COMMANDS
 +--> VALIDATION
 +--> INTEGRATION
 +--> MAPPING
 +--> AUDIT
```

and evolves toward cohesive responsibilities:

```text
APPLICATION
 |
 +--> QUERY SERVICE
 |
 +--> COMMAND SERVICE
 |
 +--> VALIDATION
 |
 +--> INTEGRATION
```

only where those boundaries genuinely improve the design.

Modernization measurement becomes:

```text
NOT:

LINES REWRITTEN

NUMBER OF MICROSERVICES

NUMBER OF NEW CLASSES
```

but:

```text
FASTER DELIVERY

FEWER INCIDENTS

LOWER COMPLEXITY

BETTER COVERAGE

FEWER VULNERABILITIES

CLEARER OWNERSHIP

LESS LEGACY TRAFFIC

LESS LEGACY INFRASTRUCTURE

FASTER RECOVERY
```

The complete modernization equation is:

```text
EXPLICIT TECHNICAL DEBT
        +
RISK-BASED PRIORITIZATION
        +
CHARACTERIZATION TESTS
        +
SAFE REFACTORING
        +
HIGH COHESION
        +
LOW COUPLING
        +
CONTROLLED COMPLEXITY
        +
INCREMENTAL MODERNIZATION
        +
STRANGLER PATTERN
        +
CLEAR DATA OWNERSHIP
        +
SUPPORTED JAVA / FRAMEWORK
        +
CONTINUOUS ARCHITECTURE
        +
ADRs
        +
FITNESS FUNCTIONS
        +
MEASURABLE BASELINES
        +
CONTROLLED CUTOVER
        +
LEGACY RETIREMENT
        =
SUSTAINABLE ENTERPRISE MODERNIZATION
```

The governing principle is:

```text
Old code
is not automatically
bad code.

New code
is not automatically
good code.

Do not rewrite
because technology
became unfashionable.

Understand the system first.

Understand the business rules.

Understand the integrations.

Understand the data.

Understand what production
actually depends on.

When tests are missing,
characterize behavior.

Then refactor.

Take small steps.

Run the tests.

Do not delete tests
because they disagree
with the modernization.

Investigate.

Remove dead code.

Git remembers it.

Reduce duplication
when it represents
the same concept.

Do not create
an abstraction
because two methods
happen to look similar.

Split God Classes
by responsibility.

Do not replace
one large class
with twenty meaningless ones.

Reduce coupling.

Remove cycles.

Keep the domain clear.

Use modern Java
where it improves
the system.

Not because
the syntax is newer.

Do not create
microservices
just to say
you have microservices.

A distributed monolith
is still a monolith,

only now
the method calls
can time out.

Modernize incrementally.

Use the strangler pattern.

Move real capabilities.

Move ownership.

Move data deliberately.

Measure before.

Measure after.

Know whether
the modernization
actually improved anything.

Automate architecture rules
that matter.

Keep ADRs alive.

When a decision changes,
supersede it.

Do not silently
abandon architecture.

And finish the migration.

Turn off
the legacy system.

Remove its credentials.

Remove its infrastructure.

Remove its cost.

Because modernization
is not complete
when the new system starts.

It is complete
when the old risk
can safely disappear.
```

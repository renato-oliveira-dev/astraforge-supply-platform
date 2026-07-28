# ADR-041: Adopt Architecture Governance and Technical Debt Management Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-041 |
| Title | Adopt Architecture Governance and Technical Debt Management Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Architecture Governance, Technical Debt, Refactoring, ArchUnit, Ownership |
| Related Work Items | ADR Governance, Technical Debt, Architecture Tests, Code Quality, Refactoring |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The platform has established architectural standards covering areas such as:

- Clean Architecture
- Java 21
- Spring Boot
- REST APIs
- PostgreSQL
- Flyway
- SQS
- Redis
- security
- resilience
- observability
- concurrency
- testing
- software supply chain
- CI/CD
- operational reliability

Defining these standards is necessary but insufficient.

Without continuous governance:

```text
Architecture Decision
        |
        v
Documentation
        |
        v
Time Passes
        |
        v
Implementation Diverges
        |
        v
Architecture Becomes Historical Documentation
```

The desired model is:

```text
Architecture Decision
        |
        v
Engineering Standard
        |
        v
Automated Verification
        |
        v
Code Review
        |
        v
Production Feedback
        |
        v
Architecture Evolution
```

Architecture must therefore be continuously governed.

---

# 2. Problem Statement

The platform requires standards defining:

- ADR lifecycle
- architectural ownership
- architecture reviews
- architectural exceptions
- technical debt
- debt prioritization
- refactoring
- legacy-code management
- deprecation
- dead-code removal
- TODO/FIXME governance
- architecture fitness functions
- ArchUnit
- package boundaries
- dependency rules
- complexity
- duplication
- code ownership
- Definition of Done
- periodic architecture reviews
- architecture drift
- obsolete decisions
- architecture evolution

---

# 3. Decision Drivers

Primary drivers are:

1. long-term maintainability
2. architectural consistency
3. reduced technical debt
4. automated governance
5. clear ownership
6. controlled exceptions
7. continuous refactoring
8. reduced architecture drift
9. simpler codebases
10. auditable decisions
11. sustainable delivery velocity
12. explicit architecture evolution

---

# 4. Decision

Architecture governance will be continuous and integrated into normal software delivery.

The canonical model is:

```text
              ARCHITECTURE
                   |
                   v
                  ADR
                   |
                   v
             ENGINEERING RULE
                   |
          +--------+--------+
          |                 |
          v                 v
     AUTOMATION         CODE REVIEW
          |                 |
          +--------+--------+
                   |
                   v
              IMPLEMENTATION
                   |
                   v
                METRICS
                   |
                   v
           PRODUCTION FEEDBACK
                   |
                   v
          ARCHITECTURE REVIEW
                   |
                   v
               EVOLUTION
```

---

# 5. Fundamental Principle

The primary governance rule is:

```text
Architecture that is never verified
eventually becomes documentation of intent,
not documentation of reality.
```

---

# 6. Architecture Ownership

Every production service must have an identifiable technical owner or owning team.

---

# 7. Ownership Responsibilities

Architecture ownership includes:

- architectural consistency
- ADR compliance
- dependency management
- technical debt
- code quality
- security posture
- operational quality
- architecture evolution

---

# 8. Shared Responsibility

Architecture governance is not exclusively the responsibility of a designated architect.

Developers implementing changes are responsible for preserving architectural constraints.

---

# 9. Architect Role

Architectural leadership should:

- define direction
- resolve systemic tradeoffs
- review significant decisions
- identify cross-service patterns
- prevent unnecessary fragmentation

It should not become a bottleneck requiring centralized approval for every implementation detail.

---

# 10. ADR

Architecturally significant decisions must be documented through Architecture Decision Records.

---

# 11. Significant Decision

An ADR is appropriate when a decision materially affects:

- architecture
- security
- scalability
- persistence
- integration
- deployment
- operations
- multiple teams/services
- long-term maintainability

---

# 12. ADR Is Not Required for Everything

An ADR should not be created for trivial implementation decisions.

---

# 13. ADR Lifecycle

Supported states should include:

```text
Proposed

Accepted

Deprecated

Superseded

Rejected
```

where applicable.

---

# 14. Proposed

A decision under evaluation may be marked:

```text
Proposed
```

---

# 15. Accepted

An approved active architectural decision is:

```text
Accepted
```

---

# 16. Deprecated

A decision still present but no longer recommended for new implementation may be:

```text
Deprecated
```

---

# 17. Superseded

When another ADR replaces a decision:

```text
ADR-N
   |
   v
Superseded by ADR-M
```

The historical ADR remains available.

---

# 18. Rejected

A rejected alternative may remain documented when preserving the reasoning has architectural value.

---

# 19. ADR Immutability

Accepted ADRs represent historical decisions.

Materially changing the meaning of an accepted ADR without recording the evolution should be avoided.

---

# 20. Architecture Evolution

When architecture changes materially, prefer:

```text
Existing ADR
     |
     v
New ADR
     |
     v
Supersedes Existing ADR
```

rather than rewriting history.

---

# 21. Minor Correction

Typographical or non-semantic corrections may be made without creating a new ADR.

---

# 22. Decision Context

An ADR must explain why the decision was necessary.

---

# 23. Alternatives

Architecturally meaningful alternatives should be documented.

---

# 24. Consequences

ADRs must include relevant:

```text
Positive Consequences

Negative Consequences

Neutral Consequences
```

---

# 25. Decision Without Tradeoff

A document that presents only advantages is usually incomplete architectural analysis.

---

# 26. Related ADRs

ADRs should reference related decisions when dependencies exist.

---

# 27. Contradictory ADRs

When two active ADRs conflict, the conflict must be resolved explicitly.

---

# 28. ADR Numbering

ADR numbering must remain unique.

---

# 29. ADR Naming

File names should remain predictable.

Example:

```text
ADR-041-adopt-architecture-governance-and-technical-debt-management-standards.md
```

---

# 30. Architecture Index

The repository should maintain an ADR index or equivalent discoverable structure.

---

# 31. Index Information

The index should identify at least:

```text
ADR

Title

Status
```

---

# 32. Architecture Drift

Architecture drift occurs when implementation gradually violates intended architecture.

---

# 33. Typical Drift

Examples include:

```text
Controller accessing repository directly

Domain depending on infrastructure

Duplicate integration clients

Business rules inside controllers

Circular package dependencies

Shared mutable utility classes

Direct database access across service boundaries
```

---

# 34. Drift Prevention

Architecture drift should be prevented through:

```text
Automation
+
Review
+
Refactoring
```

rather than documentation alone.

---

# 35. Architecture Fitness Function

An architecture fitness function automatically evaluates an architectural property.

---

# 36. Examples

Fitness functions may verify:

```text
Package Dependencies

Layer Boundaries

Naming Conventions

Forbidden Dependencies

Circular Dependencies

API Compatibility

Migration Rules
```

---

# 37. Automated Architecture Tests

Architectural constraints that can be automated should normally be automated.

---

# 38. ArchUnit

Java services should use ArchUnit or equivalent architecture tests where it provides meaningful protection against architecture drift.

---

# 39. ArchUnit Purpose

ArchUnit should verify structural rules.

It should not duplicate ordinary unit tests.

---

# 40. Example Layer Rule

Conceptually:

```text
Controller
    |
    v
Service
    |
    v
Repository
```

and prohibit:

```text
Controller
    |
    +------> Repository
```

when the service architecture requires the service boundary.

---

# 41. Domain Independence

Where Clean Architecture boundaries apply:

```text
Domain
```

must not depend on infrastructure implementation.

---

# 42. Dependency Direction

Allowed dependency direction should be encoded where practical.

---

# 43. Package Rules

Architecture tests may verify that classes in:

```text
controller
```

do not access forbidden packages directly.

---

# 44. Circular Dependencies

Package cycles should be prohibited.

---

# 45. Why Cycles Matter

Cycles increase:

- coupling
- change impact
- testing difficulty
- initialization complexity
- refactoring difficulty

---

# 46. Spring Circular Dependencies

Spring bean circular dependencies are prohibited unless an exceptional case is explicitly justified.

---

# 47. Architecture Test Naming

Architecture tests must clearly describe the architectural rule being enforced.

---

# 48. Architecture Test Failure

An architecture-test failure is a design signal.

Do not simply weaken the test to make the build green.

---

# 49. Architecture Exception

If implementation genuinely requires an exception:

```text
Rule
 |
 v
Exception Analysis
 |
 v
Explicit Approval
```

---

# 50. Exception Documentation

An architectural exception must identify:

- violated rule
- reason
- scope
- risk
- owner
- compensating controls
- expiration/review condition

---

# 51. Narrow Exception

Exceptions should be as narrow as possible.

---

# 52. Blanket Exception

Avoid:

```text
ignore all classes in package legacy
```

unless there is an explicit migration plan.

---

# 53. Temporary Exception

Temporary exceptions require a removal or review target.

---

# 54. Permanent Exception

Permanent exceptions require strong justification and periodic review.

---

# 55. Technical Debt

Technical debt is an engineering tradeoff that increases future cost, risk or complexity.

---

# 56. Technical Debt Is Not Simply Bad Code

Technical debt may be intentionally accepted.

The problem is unmanaged debt.

---

# 57. Debt Examples

Examples include:

```text
Temporary duplication

Legacy integration

Unsupported library

Missing automation

Known architecture exception

Temporary compatibility layer

Missing performance optimization

Incomplete test coverage
```

---

# 58. Intentional Debt

Intentional technical debt must be explicit.

---

# 59. Accidental Debt

Accidental debt discovered during development should be recorded when material.

---

# 60. Debt Ownership

Material technical debt requires an owner.

---

# 61. Debt Without Owner

Unowned technical debt tends to become permanent.

---

# 62. Debt Record

A debt item should identify:

- problem
- impact
- risk
- affected area
- remediation
- owner
- priority

---

# 63. Debt Priority

Technical debt should be prioritized based on engineering/business impact.

---

# 64. Suggested Classification

```text
CRITICAL

HIGH

MEDIUM

LOW
```

---

# 65. Critical Debt

Examples:

```text
Known security exposure

Data-integrity risk

Unsupported critical framework

Severe scalability limitation
```

---

# 66. High Debt

Examples:

```text
Repeated production incidents

Major architecture violation

Large maintenance bottleneck

Critical area without tests
```

---

# 67. Medium Debt

Examples:

```text
Significant duplication

Complex implementation

Outdated non-critical component
```

---

# 68. Low Debt

Examples:

```text
Minor cleanup

Small naming inconsistency

Low-impact simplification
```

---

# 69. Debt Interest

Technical debt accumulates "interest" when it repeatedly increases:

```text
Development Time

Defect Probability

Testing Cost

Incident Risk
```

---

# 70. Debt Escalation

A debt item causing repeated delivery friction should increase in priority.

---

# 71. Debt Budget

Teams should reserve regular engineering capacity for debt reduction.

---

# 72. Debt Sprint

Technical debt should not require waiting indefinitely for a hypothetical dedicated cleanup sprint.

---

# 73. Continuous Refactoring

Refactoring is part of normal development.

---

# 74. Boy Scout Principle

When changing an area, improve it where reasonable without unnecessarily expanding scope.

---

# 75. Scope Discipline

Refactoring must remain proportional to the task.

---

# 76. Opportunistic Refactoring

Safe nearby improvements are encouraged when:

- tests protect behavior
- scope remains understandable
- risk is controlled

---

# 77. Unrelated Refactoring

Large unrelated refactoring should not be hidden inside a business change.

---

# 78. Refactoring Definition

Refactoring changes internal structure without intentionally changing external behavior.

---

# 79. Refactoring Safety

Meaningful refactoring requires automated tests.

---

# 80. Characterization Tests

Legacy behavior lacking tests may first require characterization tests before restructuring.

---

# 81. Legacy Code

Legacy code means code whose safe modification is difficult due to characteristics such as:

- insufficient tests
- obsolete technology
- excessive coupling
- unclear ownership
- poor structure

Age alone does not define legacy code.

---

# 82. Legacy Isolation

Legacy code should be isolated behind clear boundaries where immediate replacement is impractical.

---

# 83. Anti-Corruption Layer

An anti-corruption layer may isolate modern domain code from legacy models/protocols.

---

# 84. Legacy Expansion

New functionality should not unnecessarily expand legacy patterns.

---

# 85. Modernization

Modernization should progressively reduce legacy surface area.

---

# 86. Big-Bang Rewrite

A complete rewrite is not automatically preferable.

---

# 87. Incremental Replacement

Prefer incremental replacement when it reduces migration risk.

Conceptually:

```text
LEGACY
   |
   v
ISOLATE
   |
   v
REPLACE SLICE
   |
   v
VALIDATE
   |
   v
REPEAT
```

---

# 88. Strangler Pattern

The Strangler Fig pattern may be used to progressively replace legacy capabilities.

---

# 89. Dead Code

Confirmed dead production code must be removed.

---

# 90. Commented-Out Code

Commented-out implementation code should not be retained as a versioning mechanism.

Git already provides history.

---

# 91. Unused Classes

Unused classes should be removed when confirmed unnecessary.

---

# 92. Unused Methods

Unused private/internal methods should be removed.

---

# 93. Unused Configuration

Obsolete configuration properties should be removed.

---

# 94. Unused Feature Flags

Completed rollout flags must be removed.

---

# 95. Unused Dependencies

Unused dependencies must be removed according to ADR-038.

---

# 96. Deprecated Code

Deprecation is a transition mechanism, not permanent storage.

---

# 97. Deprecation Lifecycle

```text
ACTIVE
   |
   v
DEPRECATED
   |
   v
MIGRATION WINDOW
   |
   v
REMOVED
```

---

# 98. Deprecation Metadata

Deprecated APIs/components should identify the preferred replacement where practical.

---

# 99. Removal Criteria

Removal should occur when:

- consumers migrated
- compatibility window ended
- rollout validated

---

# 100. Permanent Deprecation

Keeping deprecated code forever defeats the purpose of deprecation.

---

# 101. TODO

TODO comments represent unfinished work.

---

# 102. TODO Governance

Material TODOs should reference a tracked work item where practical.

---

# 103. Example

Prefer:

```java
// TODO CARD-1234: remove compatibility mapping after consumer migration.
```

over:

```java
// TODO fix later
```

---

# 104. FIXME

FIXME indicates a known defect/risk and requires higher urgency than ordinary cleanup.

---

# 105. FIXME Tracking

Material FIXME comments require tracked remediation.

---

# 106. TODO as Architecture

TODO comments must not become the permanent architecture-management system.

---

# 107. TODO Age

Long-lived TODO/FIXME items should be periodically reviewed.

---

# 108. Temporary Workaround

A workaround must identify why it exists.

---

# 109. Workaround Removal

Temporary workarounds require an exit condition.

---

# 110. Code Complexity

Complexity must remain understandable.

---

# 111. Complexity Metrics

Metrics such as:

```text
Cyclomatic Complexity

Cognitive Complexity
```

may be used as signals.

---

# 112. Metric Is Not Architecture

Complexity thresholds support review but do not replace engineering judgment.

---

# 113. Complex Method

A method with excessive branching should be decomposed when decomposition improves readability and responsibility boundaries.

---

# 114. Artificial Decomposition

Do not split methods solely to satisfy a metric if the resulting code becomes harder to understand.

---

# 115. Class Responsibility

Classes should have cohesive responsibilities.

---

# 116. God Class

Classes accumulating unrelated responsibilities should be decomposed.

---

# 117. Dependency Count

A class requiring a very large number of injected collaborators is an architectural smell.

---

# 118. Constructor Size

Large constructors often indicate excessive responsibility.

---

# 119. Facade Abuse

Do not create meaningless facades solely to reduce constructor parameter counts.

---

# 120. Cohesive Decomposition

Dependencies should be reduced by extracting cohesive responsibilities.

---

# 121. Duplication

Material business-rule duplication should be removed.

---

# 122. Duplication vs Abstraction

Two similar lines do not automatically justify abstraction.

---

# 123. Rule of Meaning

Abstract shared behavior when it represents the same concept, not merely similar syntax.

---

# 124. Premature Abstraction

Premature generic abstractions can create more complexity than controlled duplication.

---

# 125. Shared Library

A shared library requires a clear stable cross-service concern.

---

# 126. Shared Business Logic

Microservices should not casually share domain business logic through common libraries.

---

# 127. Why

Shared domain libraries create deployment and domain coupling.

---

# 128. Appropriate Shared Libraries

Potential examples:

```text
Observability conventions

Security infrastructure

Technical testing support

Stable platform primitives
```

---

# 129. Shared Library Versioning

Shared libraries require independent versioning and compatibility governance.

---

# 130. Utility Class

Utility classes must remain cohesive.

---

# 131. `Utils` Package

A generic dumping-ground package such as:

```text
utils
```

should be avoided when more precise domain/technical ownership exists.

---

# 132. Naming

Names should communicate intent.

---

# 133. Misleading Name

A misleading class/method name is technical debt even if implementation is functionally correct.

---

# 134. Comments

Comments should explain:

```text
WHY
```

when code cannot communicate the reasoning sufficiently.

---

# 135. Redundant Comment

Avoid:

```java
// Increment counter
counter++;
```

---

# 136. Architectural Comment

Comments explaining non-obvious compatibility/security constraints can be valuable.

---

# 137. Clean Code and Performance

Clean code does not mean adding abstractions without considering runtime cost.

---

# 138. Performance Optimization

Performance optimization should be evidence-driven.

---

# 139. Premature Optimization

Do not sacrifice maintainability for speculative micro-optimization.

---

# 140. Known Hot Path

Confirmed hot paths may justify specialized implementation.

---

# 141. Performance Evidence

Use:

- profiling
- load tests
- production metrics
- benchmarks

where appropriate.

---

# 142. Benchmark

Microbenchmarks should use appropriate tooling such as JMH when JVM optimization would invalidate naive timing.

---

# 143. Parallelism

Parallel execution must follow ADR-034.

---

# 144. Parallelism Is Not Automatic Optimization

Do not add concurrency merely because operations appear independent.

---

# 145. Complexity Cost of Parallelism

Parallelism introduces:

- ordering complexity
- failure coordination
- resource contention
- observability complexity

and therefore requires measurable benefit.

---

# 146. Test Architecture

Test code is part of the maintained codebase.

---

# 147. Test Duplication

Excessive duplicated test setup should be refactored when it reduces readability/maintenance.

---

# 148. Over-Generalized Test Helpers

Test helpers must not hide important test intent.

---

# 149. Test Constants

Stable reusable test data should be centralized where appropriate.

---

# 150. Random Test Data

Tests should avoid uncontrolled randomness when deterministic values provide clearer diagnostics.

---

# 151. Assertion Description

AssertJ assertions should include meaningful `.as("...")` descriptions before the predicate/assertion operation according to the project's testing standard.

---

# 152. Why Assertion Description Matters

A failed assertion should explain the expected business/technical condition rather than forcing the developer to infer it from a line number.

---

# 153. Example

```java
assertThat(result.status())
        .as("status should remain APPROVED after successful workflow processing")
        .isEqualTo(OrderStatus.APPROVED);
```

---

# 154. Test Naming

Tests should follow the project's established naming convention consistently.

---

# 155. Test Isolation

Tests must remain independent and deterministic.

---

# 156. Sleep-Based Test

Avoid:

```java
Thread.sleep(...)
```

as synchronization when deterministic coordination can be used.

---

# 157. Architecture Definition of Done

Architecture compliance is part of Definition of Done.

---

# 158. Definition of Done

A change is not complete merely because:

```text
Code Compiles
```

---

# 159. Engineering Definition of Done

Applicable checks include:

```text
[ ] Business requirement implemented

[ ] Architecture boundaries respected

[ ] No unnecessary dependency introduced

[ ] Tests added/updated

[ ] AssertJ descriptions follow project convention

[ ] Coverage acceptable

[ ] SonarQube clean

[ ] SAST findings addressed

[ ] SCA findings addressed

[ ] API compatibility reviewed

[ ] Event compatibility reviewed

[ ] Database migration reviewed

[ ] Existing applied Flyway migration not modified

[ ] New database correction uses a new migration

[ ] Logging reviewed

[ ] Sensitive data not exposed

[ ] Performance impact considered

[ ] Concurrency impact considered

[ ] Resilience behavior considered

[ ] Documentation updated

[ ] Obsolete code removed

[ ] Technical debt recorded if intentionally introduced

[ ] Operational impact reviewed
```

---

# 160. Pull Request Architecture Review

Code review should consider more than syntax.

---

# 161. Review Questions

Reviewers should ask:

```text
Is responsibility in the correct layer?

Is this abstraction necessary?

Is there duplication?

Does this introduce coupling?

Does it preserve compatibility?

Can it fail safely?

Can it be tested?

Can it be operated?

Does it create debt?
```

---

# 162. Review Scope

Not every pull request requires formal architecture-board review.

---

# 163. Escalation Criteria

Formal architecture review is appropriate when changes:

- create new service boundaries
- introduce new infrastructure technology
- change persistence strategy
- change messaging architecture
- introduce major security patterns
- create cross-platform standards
- materially contradict an accepted ADR

---

# 164. Architecture Board

An architecture board, where used, should resolve significant cross-team decisions rather than approve routine code.

---

# 165. Architecture Bottleneck

Governance must not require centralized approval for ordinary compliant implementation.

---

# 166. Paved Road

The platform should provide preferred implementation patterns.

---

# 167. Golden Path

A golden path should make the compliant implementation easier than creating a custom alternative.

---

# 168. Examples

Golden-path assets may include:

```text
Service Template

Gradle Conventions

Security Configuration

WebClient Configuration

Logging Standards

Test Templates

CI Pipeline Templates
```

---

# 169. Copy-Paste Architecture

Templates should reduce repeated setup but must not encourage copying obsolete implementations indefinitely.

---

# 170. Template Ownership

Templates require lifecycle ownership.

---

# 171. Architecture Metrics

Architecture governance should track useful signals.

---

# 172. Suggested Metrics

Examples:

```text
Architecture Test Failures

Circular Dependencies

Technical Debt Age

Critical Debt Count

High Debt Count

Deprecated Components

Unsupported Dependencies

TODO/FIXME Age

Duplicated Business Rules

Architecture Exceptions
```

---

# 173. Metric Gaming

Metrics must not encourage meaningless code changes merely to improve scores.

---

# 174. Sonar Metrics

SonarQube is a useful quality signal but does not define the architecture.

---

# 175. Zero Sonar Issue

A project with zero Sonar issues may still have poor architecture.

---

# 176. Coverage Metric

High coverage may coexist with weak assertions.

---

# 177. Quality Model

The desired model is:

```text
AUTOMATED METRICS
       +
ENGINEERING JUDGMENT
       +
ARCHITECTURE RULES
       +
PRODUCTION FEEDBACK
```

---

# 178. Periodic Architecture Review

Services should undergo periodic architecture review proportional to their criticality and rate of change.

---

# 179. Review Scope

Periodic review should consider:

- active ADR compliance
- obsolete ADRs
- architecture drift
- technical debt
- unsupported dependencies
- package boundaries
- performance
- security
- operational incidents
- duplication
- legacy code

---

# 180. Triggered Architecture Review

A review should also be triggered by material events.

---

# 181. Trigger Examples

Examples:

```text
Repeated incidents

Major framework upgrade

Significant scaling problem

Security incident

Service split/merge

Database redesign

Large modernization
```

---

# 182. ADR Review

ADRs should be periodically reviewed for continued applicability.

---

# 183. ADR Does Not Expire Automatically

An older ADR remains valid until explicitly deprecated or superseded.

---

# 184. Obsolete ADR

An obsolete ADR must not remain marked `Accepted` indefinitely if the architecture no longer follows it.

---

# 185. Governance Drift

Architecture governance itself must evolve when rules no longer provide sufficient value.

---

# 186. Rule Removal

An obsolete rule should be removed/superseded deliberately rather than ignored informally.

---

# 187. Architecture Exception Count

Growing exception counts may indicate the architecture rule itself needs reevaluation.

---

# 188. Exception Signal

If many teams require the same exception:

```text
Repeated Exception
       |
       v
Review Architecture Rule
```

---

# 189. Technical Debt Review

Debt should be reviewed periodically.

---

# 190. Debt Aging

Old high-impact debt requires explicit reconsideration.

---

# 191. Debt Closure

A debt item is closed only when its underlying risk/cost is actually removed or consciously accepted under a new decision.

---

# 192. Refactoring PR

Large refactoring may be delivered separately from functional changes to simplify review.

---

# 193. Behavioral Preservation

Refactoring PRs should demonstrate preserved behavior through tests.

---

# 194. Migration Refactoring

Database migration history must not be rewritten as part of refactoring.

---

# 195. Flyway Rule

Even during major cleanup:

```text
Applied Flyway migrations remain immutable.
```

Corrections continue through new versions.

---

# 196. Service Decommissioning

Obsolete services require controlled decommissioning.

---

# 197. Decommission Checklist

Applicable steps include:

```text
[ ] Consumers migrated

[ ] Traffic removed

[ ] Scheduled jobs disabled

[ ] SQS consumers/producers reviewed

[ ] Secrets revoked

[ ] DNS/routes removed

[ ] Infrastructure removed

[ ] Dashboards/alerts removed

[ ] Data retention handled

[ ] Documentation updated

[ ] Ownership records updated
```

---

# 198. Zombie Service

A service with no known owner or consumers must not remain indefinitely deployed.

---

# 199. Dependency Decommissioning

Removing a service requires identifying upstream/downstream dependencies.

---

# 200. Database Decommissioning

Database/schema removal must follow data-retention and recovery requirements.

---

# 201. Architecture Repository Structure

Architecture documentation should remain close enough to source and delivery workflows to evolve with implementation.

---

# 202. Documentation Review

Architecture documentation changes require normal code review.

---

# 203. Documentation Drift

Documentation known to be incorrect should be corrected or explicitly marked obsolete.

---

# 204. Production Feedback

Production incidents may invalidate architectural assumptions.

---

# 205. Incident-to-Architecture Loop

```text
INCIDENT
   |
   v
POSTMORTEM
   |
   v
ARCHITECTURAL CAUSE?
   |
   +-- NO --> Local Improvement
   |
   v YES
ADR / STANDARD UPDATE
   |
   v
AUTOMATED RULE
```

where practical.

---

# 206. Security Feedback

Security findings may similarly require architecture-rule evolution.

---

# 207. Performance Feedback

Production bottlenecks may justify revisiting previous design decisions.

---

# 208. Architecture Is Evolutionary

Architecture is expected to evolve.

Consistency does not mean immutability.

---

# 209. Controlled Evolution

The goal is:

```text
CHANGE
  +
REASONING
  +
TRACEABILITY
  +
MIGRATION
```

rather than preventing change.

---

# 210. Architecture Governance Gate

A service is not architecturally healthy merely because its current build passes.

Periodic governance should verify:

```text
[ ] Service ownership current

[ ] ADRs current

[ ] Superseded ADRs marked

[ ] Architecture tests passing

[ ] No unexplained package cycles

[ ] Layer boundaries preserved

[ ] Architecture exceptions reviewed

[ ] Critical technical debt tracked

[ ] High technical debt reviewed

[ ] Legacy boundaries understood

[ ] Dead code removed

[ ] Deprecated code has removal plan

[ ] TODO/FIXME debt reviewed

[ ] Unsupported dependencies addressed

[ ] Duplication reviewed

[ ] Complexity hotspots reviewed

[ ] Production incidents incorporated

[ ] Security findings incorporated

[ ] Performance findings incorporated

[ ] Documentation reflects reality
```

---

# 211. Anti-Patterns

The following are prohibited or strongly discouraged:

- architecture existing only in diagrams
- ADRs never reviewed
- silently rewriting architectural history
- contradictory active ADRs
- architecture rules enforced only by memory
- weakening architecture tests merely to make CI green
- blanket ArchUnit exclusions
- circular package dependencies
- Spring circular dependencies
- architecture exceptions without owners
- permanent temporary exceptions
- technical debt without tracking
- technical debt without ownership
- waiting indefinitely for a debt-only sprint
- large unrelated refactoring hidden in business PRs
- big-bang rewrite without migration justification
- expanding legacy patterns into new functionality
- retaining confirmed dead code
- commented-out implementation code
- permanent deprecated APIs
- `TODO fix later`
- FIXME without remediation tracking
- abstractions created solely to remove superficial duplication
- generic `Utils` dumping grounds
- meaningless facade layers
- optimizing without evidence
- adding parallelism without measurable benefit
- treating SonarQube score as architecture
- treating coverage percentage as test quality
- manually modifying already applied Flyway migrations
- keeping obsolete services deployed without ownership
- documentation knowingly inconsistent with implementation

---

# 212. Positive Consequences

The decision provides:

- sustainable architectural consistency
- reduced architecture drift
- explicit technical debt
- better refactoring discipline
- automated architecture enforcement
- cleaner package boundaries
- reduced legacy growth
- clearer ownership
- better ADR lifecycle
- improved modernization capability
- stronger Definition of Done
- continuous architecture improvement

---

# 213. Negative Consequences

The decision introduces:

- architecture-test maintenance
- ADR review effort
- debt tracking
- refactoring effort
- exception governance
- periodic architecture reviews
- deprecation cleanup
- documentation maintenance

These costs are accepted because unmanaged architecture complexity compounds over time.

---

# 214. Neutral Consequences

The decision also means:

- technical debt may sometimes be intentionally accepted
- not every duplication requires abstraction
- not every architectural decision requires an ADR
- not every PR requires architecture-board review
- old ADRs may remain valid
- architecture rules themselves may evolve
- metrics support judgment rather than replace it

---

# 215. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Architecture drift | High | High | Fitness functions |
| Excessive governance | High | Medium | Automate routine rules |
| Architecture bottleneck | High | Medium | Decentralized ownership |
| Growing technical debt | High | High | Debt review/capacity |
| Legacy expansion | High | Medium | Isolation + modernization |
| Dead code accumulation | Medium | High | Continuous cleanup |
| ADR obsolescence | Medium | Medium | Periodic review |
| Rule gaming | Medium | Medium | Engineering judgment |
| Over-abstraction | High | Medium | Cohesion-based review |
| Big-bang modernization failure | Critical | Medium | Incremental migration |
| Architecture test bypass | High | Medium | Narrow exceptions |
| Documentation drift | Medium | High | Review with code |

---

# 216. Implementation Guidance

The following rules are mandatory:

1. Every production service requires clear ownership.
2. Architecturally significant decisions require ADRs.
3. ADRs must preserve decision history.
4. Material architecture changes should supersede rather than silently rewrite previous decisions.
5. ADR statuses must reflect actual architecture.
6. Architectural rules should be automated where practical.
7. Java structural rules should use ArchUnit where it provides meaningful protection.
8. Package/layer cycles are prohibited.
9. Architecture-test failures must be treated as design signals.
10. Exceptions require explicit scope, owner and justification.
11. Material technical debt must be tracked.
12. High/critical technical debt requires explicit prioritization.
13. Refactoring is part of normal development.
14. Large unrelated refactoring should be separated from functional changes.
15. Legacy systems should be isolated and progressively modernized.
16. New functionality should not unnecessarily expand legacy patterns.
17. Confirmed dead code must be removed.
18. Commented-out code must not replace version control.
19. Deprecation requires an eventual removal strategy.
20. Material TODO/FIXME items require meaningful tracking.
21. Complexity metrics are signals, not architectural goals.
22. Large classes/dependency counts require responsibility analysis.
23. Business-rule duplication should be removed when it represents the same concept.
24. Shared libraries require stable cross-service justification.
25. Performance optimization must be evidence-driven.
26. Parallelism must have measurable benefit and controlled resource use.
27. Test code follows the same maintainability expectations as production code.
28. AssertJ assertions must follow the project's descriptive `.as("...")` convention.
29. Architecture compliance is part of Definition of Done.
30. Applied Flyway migrations remain immutable during all refactoring/modernization.
31. Obsolete services require controlled decommissioning.
32. Production incidents must feed architecture improvement where systemic causes exist.
33. Periodic architecture reviews must consider both documentation and actual implementation.

---

# 217. Validation

This ADR will be validated through:

- ArchUnit tests
- SonarQube
- SAST
- SCA
- code review
- ADR review
- technical-debt review
- dependency analysis
- package-cycle detection
- coverage reports
- production incidents
- performance analysis
- security findings
- architecture assessments
- service decommission reviews

---

# 218. Success Criteria

The decision is successful when:

- architecture rules remain visible in implementation
- architectural drift is detected automatically
- ADRs reflect current architecture
- significant changes preserve decision history
- technical debt has clear ownership
- critical debt does not remain invisible
- legacy surface area decreases
- dead/deprecated code is removed
- package cycles do not emerge
- service ownership remains clear
- production incidents result in systemic improvements
- modernization can occur incrementally
- architecture governance supports delivery instead of blocking it

---

# 219. Alternatives Rejected

## 219.1 Architecture by Documentation Only

Rejected because documentation cannot prevent implementation drift.

---

## 219.2 Architect Approval for Every Change

Rejected because centralized approval creates a delivery bottleneck.

---

## 219.3 Technical Debt Cleanup Only in Dedicated Sprints

Rejected because debt reduction must be continuous.

---

## 219.4 Rewrite Legacy Systems Completely

Rejected as a universal strategy because large rewrites introduce significant migration and business risk.

---

## 219.5 SonarQube as Architecture Governance

Rejected because static quality metrics cannot represent complete architecture semantics.

---

## 219.6 Ignore Old ADRs

Rejected because outdated active decisions create ambiguity.

---

## 219.7 Allow Permanent Temporary Exceptions

Rejected because exceptions without lifecycle management become undocumented architecture.

---

# 220. Related Decisions

This ADR is related to all architecture decisions in the platform and specifically:

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-030: Adopt SQS Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-034: Adopt Java 21 Concurrency and Parallelism Standards
- ADR-035: Adopt Engineering Quality and Testing Standards
- ADR-036: Adopt API Design, REST Contract and Compatibility Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-038: Adopt Dependency and Software Supply Chain Security Standards
- ADR-039: Adopt CI/CD, Release and Deployment Governance Standards
- ADR-040: Adopt Production Reliability, Incident Response and Operational Readiness Standards
- ADR-042: Adopt Architecture Fitness Functions and Automated Governance Standards

---

# 221. References

- Architecture Decision Records
- Evolutionary Architecture
- Building Evolutionary Architectures
- Clean Architecture
- ArchUnit
- SonarQube
- Martin Fowler — Technical Debt
- Martin Fowler — Refactoring
- Strangler Fig Application
- DORA
- Google SRE
- OWASP
- Gradle
- Spring Boot

---

# 222. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial architecture governance and technical debt baseline |

---

# 223. Decision Summary

The definitive governance model is:

```text
                  ARCHITECTURE
                       |
                       v
                      ADR
                       |
                       v
                ENGINEERING RULE
                       |
          +------------+------------+
          |                         |
          v                         v
      AUTOMATION                REVIEW
          |                         |
          +------------+------------+
                       |
                       v
                  IMPLEMENTATION
                       |
                       v
                    PROD
                       |
                       v
                   FEEDBACK
                       |
                       v
                ARCHITECTURE
                  EVOLUTION
```

Architecture rules should evolve toward:

```text
DOCUMENTED RULE
      |
      v
AUTOMATED FITNESS FUNCTION
      |
      v
CI FAILURE ON VIOLATION
```

where technically appropriate.

Technical debt follows:

```text
                 DEBT IDENTIFIED
                       |
                       v
                    RECORD
                       |
                       v
                   CLASSIFY
                       |
                       v
                     OWNER
                       |
                       v
                  PRIORITIZE
                       |
              +--------+--------+
              |                 |
              v                 v
          REMEDIATE          ACCEPT
              |                 |
              v                 v
           REMOVE          REVIEW LATER
```

not:

```text
DEBT IDENTIFIED
      |
      v
"WE'LL FIX IT LATER"
      |
      v
FORGOTTEN
```

Legacy modernization follows:

```text
                    LEGACY
                       |
                       v
                    ISOLATE
                       |
                       v
                CREATE BOUNDARY
                       |
                       v
                 REPLACE SLICE
                       |
                       v
                    VERIFY
                       |
                       v
                 REMOVE LEGACY
                       |
                       v
                    REPEAT
```

Deprecation follows:

```text
ACTIVE
  |
  v
DEPRECATED
  |
  v
CONSUMER MIGRATION
  |
  v
REMOVAL
```

not:

```text
ACTIVE
  |
  v
DEPRECATED
  |
  v
DEPRECATED FOREVER
```

Architecture exceptions follow:

```text
ARCHITECTURE RULE
       |
       v
    VIOLATION
       |
       v
 IS IT NECESSARY?
    /       \
   NO       YES
   |         |
   v         v
FIX DESIGN  DOCUMENT EXCEPTION
             |
             v
           OWNER
             |
             v
         EXPIRATION /
           REVIEW
```

The quality model is:

```text
SONAR
  +
TEST COVERAGE
  +
ARCHUNIT
  +
SAST/SCA
  +
CODE REVIEW
  +
ENGINEERING JUDGMENT
  +
PRODUCTION FEEDBACK
  =
SUSTAINABLE QUALITY
```

No individual metric is sufficient.

The ADR lifecycle is:

```text
PROPOSED
   |
   v
ACCEPTED
   |
   +--------------------+
   |                    |
   v                    v
STILL VALID         NEW DECISION
   |                    |
   v                    v
REMAIN ACCEPTED     SUPERSEDED
                        |
                        v
                   HISTORY KEPT
```

The Flyway rule remains absolute:

```text
APPLIED MIGRATION
       |
       v
    IMMUTABLE
```

including during:

```text
Refactoring

Modernization

Bug Fixing

Architecture Cleanup

Performance Improvement
```

A correction always moves forward:

```text
V27 -> V28 -> V29 -> V30
```

never backward by rewriting history.

Finally, the architecture lifecycle becomes:

```text
                    BUSINESS
                       |
                       v
                  ARCHITECTURE
                       |
                       v
                 IMPLEMENTATION
                       |
                       v
                    QUALITY
                       |
                       v
                    SECURITY
                       |
                       v
                    CI/CD
                       |
                       v
                  PRODUCTION
                       |
                       v
                   INCIDENTS
                       |
                       v
                   LEARNING
                       |
                       v
               TECHNICAL DEBT
                       |
                       v
                  REFACTORING
                       |
                       v
             ARCHITECTURE REVIEW
                       |
                       +----------------+
                                        |
                                        v
                                  ARCHITECTURE
```

The governing principle is:

```text
Architecture is not a one-time design phase.

Architecture is a continuously verified
property of the running system.
```

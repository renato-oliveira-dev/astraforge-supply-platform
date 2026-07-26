# ADR-069: Adopt Enterprise Code Review, Refactoring, Technical Debt and Legacy Modernization Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-069 |
| Title | Adopt Enterprise Code Review, Refactoring, Technical Debt and Legacy Modernization Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Code Review, Refactoring, Technical Debt, Legacy Modernization |
| Related Work Items | Java 21, Spring Boot 3, SonarQube, SAST, ArchUnit, JaCoCo |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise systems accumulate technical debt continuously.

Typical evolution:

```text
INITIAL IMPLEMENTATION
        |
        v
NEW REQUIREMENT
        |
        v
SMALL PATCH
        |
        v
ANOTHER PATCH
        |
        v
SPECIAL CASE
        |
        v
HOTFIX
        |
        v
LEGACY COMPLEXITY
```

Each individual change can appear reasonable while the accumulated result becomes:

```text
LARGE CLASSES

DUPLICATED RULES

DEAD CODE

MULTIPLE ABSTRACTIONS

EXCESSIVE DEPENDENCIES

INCONSISTENT ERROR HANDLING

OUTDATED FRAMEWORK PATTERNS

DIFFICULT TESTING

FRAGILE INTEGRATIONS
```

Modernization therefore cannot be treated as:

```text
REWRITE EVERYTHING
```

nor as:

```text
NEVER TOUCH WORKING CODE
```

The organization requires a controlled method for improving systems while preserving business behavior.

---

# 2. Problem Statement

The organization requires standards covering:

- code review
- refactoring
- technical debt
- legacy modernization
- Java modernization
- Spring Boot modernization
- class decomposition
- class consolidation
- class removal
- dead code
- duplicate code
- God Classes
- God Services
- dependency count
- cognitive complexity
- SonarQube
- SAST
- JaCoCo
- ArchUnit
- compatibility
- incremental migration
- Strangler Pattern
- Branch by Abstraction
- feature flags
- characterization tests
- migration safety
- rollback
- modernization prioritization
- Definition of Done

---

# 3. Decision Drivers

Primary drivers are:

1. maintainability
2. production safety
3. regression prevention
4. modernization velocity
5. reduced technical debt
6. security
7. testability
8. operational reliability
9. developer productivity
10. architecture consistency
11. controlled migration risk
12. long-term cost reduction

---

# 4. Decision

Refactoring and modernization MUST be incremental, evidence-driven and protected by automated tests.

Canonical modernization cycle:

```text
UNDERSTAND
    |
    v
BASELINE
    |
    v
TEST
    |
    v
REFACTOR
    |
    v
VALIDATE
    |
    v
MEASURE
    |
    v
REMOVE LEGACY
```

---

# 5. Fundamental Principle

```text
Refactoring changes
internal structure
without intentionally
changing external behavior.

Modernization may change
technology and architecture,
but business behavior must
remain controlled and explicit.
```

---

# 6. Code Review

Code review is an engineering control, not a formatting ceremony.

---

# 7. Review Objective

Review SHOULD evaluate:

```text
CORRECTNESS

ARCHITECTURE

SECURITY

PERFORMANCE

TESTABILITY

MAINTAINABILITY

OBSERVABILITY

COMPATIBILITY
```

---

# 8. Formatting

Formatting SHOULD normally be automated.

Human review time SHOULD focus on decisions tools cannot reliably make.

---

# 9. Review Scope

A reviewer SHOULD understand:

```text
WHY THE CHANGE EXISTS

WHAT BEHAVIOR CHANGES

WHAT CAN BREAK

HOW IT IS TESTED
```

---

# 10. Large Pull Request

Large pull requests SHOULD be decomposed where practical.

---

# 11. Reviewability

A change that cannot reasonably be reviewed is itself a delivery risk.

---

# 12. Mechanical Refactoring

Large mechanical changes SHOULD be separated from behavioral changes where practical.

Example:

```text
PR 1:
RENAME / MOVE

PR 2:
CHANGE BEHAVIOR
```

This improves diff clarity.

---

# 13. Refactoring

Refactoring SHOULD preserve externally observable behavior unless a behavior change is explicitly part of the requirement.

---

# 14. Refactoring Safety

Before materially refactoring critical code:

```text
UNDERSTAND CURRENT BEHAVIOR

IDENTIFY CONTRACTS

IDENTIFY SIDE EFFECTS

IDENTIFY DATA IMPACT

ESTABLISH TEST BASELINE
```

---

# 15. Characterization Tests

Legacy behavior lacking adequate tests SHOULD receive characterization tests before major restructuring.

---

# 16. Characterization Test Purpose

Characterization tests answer:

```text
WHAT DOES THE SYSTEM
ACTUALLY DO TODAY?
```

not necessarily:

```text
WHAT SHOULD AN IDEAL
SYSTEM DO?
```

---

# 17. Incorrect Existing Behavior

If existing behavior is known to be incorrect, the defect MUST be documented and corrected intentionally rather than accidentally preserved forever.

---

# 18. Refactor vs Rewrite

Default decision:

```text
CAN THE SYSTEM BE
SAFELY IMPROVED
INCREMENTALLY?
      |
   +--+--+
   |     |
  YES    NO
   |     |
   v     v
REFACTOR  EVALUATE
          REWRITE
```

---

# 19. Rewrite

A rewrite requires explicit justification.

---

# 20. Rewrite Risk

A rewrite risks losing undocumented behavior accumulated over years.

---

# 21. Big Bang Rewrite

Big-bang rewrites SHOULD normally be avoided.

---

# 22. Incremental Modernization

Incremental modernization SHOULD be preferred.

---

# 23. Strangler Pattern

The Strangler Pattern SHOULD be considered when replacing large legacy capabilities gradually.

Conceptually:

```text
CLIENT
  |
  v
ROUTING BOUNDARY
  |
  +--> LEGACY
  |
  +--> MODERN
```

Over time:

```text
LEGACY
  |
  v
DECREASES

MODERN
  |
  v
INCREASES
```

until the legacy capability can be removed.

---

# 24. Branch by Abstraction

Branch by Abstraction MAY be used when replacing internal implementations while preserving callers.

Conceptually:

```text
CALLER
   |
   v
ABSTRACTION
   |
   +--> OLD IMPLEMENTATION
   |
   +--> NEW IMPLEMENTATION
```

---

# 25. Abstraction Removal

Temporary migration abstractions MUST be removed after migration completes unless they retain architectural value.

---

# 26. Feature Flag

Feature flags MAY support controlled modernization rollout.

---

# 27. Feature Flag Is Temporary

Migration flags SHOULD have:

```text
OWNER

PURPOSE

CREATION DATE

REMOVAL CRITERIA
```

---

# 28. Permanent Flags

Obsolete permanent feature flags create technical debt and SHOULD be removed.

---

# 29. Legacy Modernization

Modernization SHOULD separate:

```text
TECHNOLOGY CHANGE

ARCHITECTURAL CHANGE

BUSINESS CHANGE
```

where practical.

---

# 30. Simultaneous Change Risk

Changing all three simultaneously increases diagnostic and rollback complexity.

---

# 31. Java Modernization

Legacy Java modernization SHOULD progress through supported runtime baselines rather than merely changing compiler version.

---

# 32. Java 21

Java 21 modernization SHOULD evaluate adoption of appropriate modern language/runtime capabilities.

Examples:

```text
Records

Pattern Matching

Switch Expressions

Text Blocks

Virtual Threads

Modern Collections APIs
```

where they improve the design.

---

# 33. Modern Syntax

Modern syntax MUST NOT be introduced solely for novelty.

---

# 34. Records

Records SHOULD be considered for immutable DTOs/value-oriented structures.

---

# 35. Record Misuse

Records SHOULD NOT automatically replace mutable persistence entities.

---

# 36. Virtual Threads

Virtual Threads SHOULD be adopted according to the concurrency and performance ADRs.

---

# 37. Virtual Thread Migration

Changing:

```text
PLATFORM THREAD
```

to:

```text
VIRTUAL THREAD
```

does not eliminate:

```text
DATABASE LIMITS

CONNECTION POOL LIMITS

REMOTE SERVICE LIMITS

RATE LIMITS
```

---

# 38. Deprecated Java APIs

Deprecated/obsolete APIs SHOULD be removed during modernization when replacement risk is understood.

---

# 39. Spring Boot Modernization

Spring Boot modernization SHOULD remove obsolete framework patterns rather than merely upgrading dependency versions.

---

# 40. Constructor Injection

Constructor injection SHOULD replace unnecessary field injection.

---

# 41. Field Injection

New production code SHOULD avoid:

```java
@Autowired
private SomeService service;
```

when constructor injection is practical.

---

# 42. Configuration Properties

Structured configuration SHOULD use validated configuration-property models.

---

# 43. RestTemplate

Legacy `RestTemplate` integrations SHOULD be evaluated for migration to approved HTTP-client standards.

---

# 44. WebClient vs RestClient

The chosen client SHOULD reflect the application's execution model and project standard rather than fashion.

---

# 45. javax to jakarta

Spring Boot 3 modernization MUST account for:

```text
javax.*
    ->
jakarta.*
```

migration where applicable.

---

# 46. Hibernate Modernization

Hibernate upgrades MUST be validated for:

```text
Query Behavior

Fetch Behavior

Pagination

Type Mapping

Dialect

Generated SQL
```

---

# 47. Dependency Modernization

Dependency upgrades SHOULD evaluate:

```text
API Changes

Security Fixes

Transitive Dependencies

Runtime Behavior

Configuration Changes
```

---

# 48. Blind Upgrade

Changing versions until the build becomes green is not sufficient modernization validation.

---

# 49. Class Review

Classes SHOULD be reviewed according to responsibility and change drivers.

---

# 50. Class Size Signal

Large class size is a review signal, not an automatic violation.

---

# 51. Dependency Count Signal

Approximately:

```text
15-20 injected dependencies
```

MUST trigger architectural review.

---

# 52. Do Not Hide Dependencies

This:

```text
20 dependencies
    |
    v
DependencyBundle
    |
    v
1 constructor argument
```

does not solve excessive responsibility.

---

# 53. God Class

A God Class commonly combines:

```text
BUSINESS RULES

PERSISTENCE

INTEGRATION

MAPPING

VALIDATION

SECURITY

NOTIFICATION

AUDIT
```

---

# 54. God Class Refactoring

Decomposition SHOULD identify cohesive capabilities.

Example:

```text
OrderService
    |
    +--> OrderApprovalService
    +--> OrderCancellationService
    +--> OrderPricingService
    +--> OrderQueryService
```

only when these represent genuine separate responsibilities.

---

# 55. Artificial Decomposition

Do not replace:

```text
1 understandable class
```

with:

```text
15 tiny classes
```

that merely delegate to one another.

---

# 56. Class Split Criteria

Consider splitting when:

```text
Different methods change
for different reasons

Dependencies form
separate clusters

Business capabilities
are independently testable

Different transaction
boundaries exist

Different security
rules exist
```

---

# 57. Class Merge Criteria

Consider merging when classes:

```text
Represent the same responsibility

Always change together

Only delegate between themselves

Cannot be meaningfully understood separately

Contain duplicated implementations
of the same concept
```

---

# 58. Merge Is Not Line Reduction

Classes MUST NOT be merged merely to reduce file count.

---

# 59. Delete Class Criteria

A class SHOULD be deleted when it is:

```text
Unused

Obsolete

Superseded

Pure accidental indirection

Duplicate without independent semantics
```

---

# 60. Reference Verification

Before deleting code, verify:

```text
Compile-time references

Reflection

Framework discovery

Configuration references

Serialization

Messaging

Scheduled jobs
```

---

# 61. Dead Code

Dead production code SHOULD be removed.

---

# 62. Version Control

Git is the historical archive.

Production code MUST NOT preserve obsolete implementations merely as historical reference.

---

# 63. Commented-Out Code

Large commented code blocks SHOULD be removed.

---

# 64. Deprecated Code

Deprecated code MUST have a migration/removal plan.

---

# 65. Dead Configuration

Unused configuration properties SHOULD be removed.

---

# 66. Dead Database Objects

Database object removal requires independent data/migration governance and MUST NOT be treated like ordinary Java dead-code deletion.

---

# 67. Flyway

Existing applied Flyway migrations MUST NEVER be modified during refactoring.

---

# 68. Database Change

Any database correction MUST use:

```text
NEW MIGRATION
+
NEW VERSION
```

---

# 69. Duplicate Code

Duplicate business behavior SHOULD be investigated.

---

# 70. Semantic Duplication

The key question is:

```text
DO THESE PIECES OF CODE
REPRESENT THE SAME
BUSINESS RULE?
```

---

# 71. Syntactic Duplication

Similar syntax alone does not prove a shared abstraction is appropriate.

---

# 72. Wrong Abstraction

A wrong shared abstraction can create stronger coupling than controlled duplication.

---

# 73. Rule of Three

Small duplication MAY be tolerated until a stable abstraction becomes clear.

---

# 74. Copy-Paste Business Rule

A material business rule copied across multiple services/classes SHOULD trigger consolidation review.

---

# 75. Cross-Microservice Duplication

Cross-service duplication MUST NOT automatically be solved with a shared domain library.

---

# 76. Independent Bounded Context

Two bounded contexts MAY intentionally implement similar rules independently when ownership/evolution differs.

---

# 77. Utility Duplication

Small pure technical utilities MAY be shared when semantics are stable.

---

# 78. Generic Utility Class

Large generic:

```text
CommonUtils

ApplicationHelper

GeneralManager
```

classes SHOULD be decomposed.

---

# 79. Cognitive Complexity

High cognitive complexity SHOULD trigger refactoring analysis.

---

# 80. Complexity Reduction

Preferred techniques include:

```text
Guard Clauses

Extract Method

Explicit Policy

Strategy

State Machine

Smaller Cohesive Use Cases
```

where appropriate.

---

# 81. Guard Clause

Deep nesting SHOULD often be replaced by early validation/guard clauses.

---

# 82. Extract Method

Extracted methods SHOULD communicate intent.

Avoid extracting:

```text
line1()

line2()

line3()
```

without semantic value.

---

# 83. Conditional Extraction

Complex repeated conditionals MAY indicate a missing policy abstraction.

---

# 84. Strategy

Strategy SHOULD be used when meaningful algorithms vary independently.

---

# 85. Strategy Overengineering

A two-line stable conditional does not automatically require Strategy.

---

# 86. State Machine

Complex lifecycle transitions SHOULD consider explicit state-transition modeling.

---

# 87. Boolean Explosion

Multiple booleans controlling behavior MAY indicate missing domain state representation.

---

# 88. Long Parameter List

Long parameter lists SHOULD trigger review.

---

# 89. Parameter Object

A parameter object SHOULD be introduced when the values form a cohesive concept.

---

# 90. Parameter Bundle

Do not create arbitrary parameter objects merely to silence static-analysis thresholds.

---

# 91. Primitive Obsession

Repeated primitives representing meaningful domain concepts SHOULD be evaluated for Value Objects.

---

# 92. Null Complexity

Repeated null handling MAY indicate unclear contracts.

---

# 93. Optional

`Optional` SHOULD be used according to project coding standards and MUST NOT become a substitute for clear domain modeling.

---

# 94. Null-Safety Refactoring

Null-safety changes MUST preserve actual external contract semantics.

---

# 95. SonarQube

SonarQube is a mandatory quality signal.

---

# 96. Sonar Is Not Architecture

Passing Sonar does not prove good architecture.

---

# 97. Sonar Remediation

Sonar findings SHOULD be fixed at the root cause.

---

# 98. Mechanical Sonar Fix

A change that technically removes a warning while making architecture worse SHOULD be rejected.

---

# 99. Exception Rule

For exception handling:

```text
HANDLE MEANINGFULLY
OR
RETHROW
```

must be followed.

---

# 100. Log and Rethrow

Do not mechanically:

```java
catch (Exception ex) {
    log.error(..., ex);
    throw ex;
}
```

when an outer boundary will log the same failure.

---

# 101. Either Log or Rethrow

The code SHOULD satisfy Sonar exception-handling requirements while avoiding duplicate logging.

---

# 102. Nullability Warning

Potential `NullPointerException` warnings MUST be solved through correct contracts/control flow rather than suppression without evidence.

---

# 103. Suppression

Static-analysis suppression MUST be exceptional.

---

# 104. Suppression Justification

A suppression SHOULD include enough context to explain why the finding is false-positive or intentionally accepted.

---

# 105. SAST

SAST findings MUST be evaluated according to actual source-to-sink behavior.

---

# 106. Security Shortcut

Security findings MUST NOT be "fixed" through generic transformations that corrupt legitimate business data.

---

# 107. Data Integrity

Security remediation MUST preserve legitimate values.

Example:

```text
M&M
```

must not become:

```text
M&amp;M
```

because of unrelated generic sanitization.

---

# 108. Sanitization Boundary

Sanitization/escaping MUST occur according to the destination context.

---

# 109. Secret Masking

Logging and exception refactoring MUST preserve masking of:

```text
Authorization

Bearer tokens

Passwords

API keys

Secrets
```

---

# 110. SAST Regression Test

Material security fixes SHOULD receive regression tests.

---

# 111. JaCoCo

Refactoring MUST NOT materially reduce meaningful test coverage.

---

# 112. Coverage Baseline

Before a large refactoring, capture the existing coverage baseline.

---

# 113. Coverage Improvement

Low-coverage critical code SHOULD receive tests before or during refactoring.

---

# 114. 80 Percent

The standard project coverage target remains:

```text
>= 80%
```

unless a stricter project requirement exists.

---

# 115. Coverage Gaming

Do not write meaningless tests solely to restore a percentage after refactoring.

---

# 116. Changed-Code Coverage

Changed code SHOULD receive stronger attention than unchanged legacy code.

---

# 117. Architecture Tests

Refactoring SHOULD add ArchUnit rules when a newly established boundary can be mechanically protected.

---

# 118. Architecture Regression

If refactoring creates:

```text
Controller -> Repository
```

where architecture requires:

```text
Controller -> Application -> Repository Port
```

the architecture test SHOULD fail.

---

# 119. Performance

Refactoring MUST evaluate performance when changing:

```text
Database Access

Remote Calls

Concurrency

Serialization

Caching

Collection Processing
```

---

# 120. Clean Code Is Not Automatically Faster

A structurally cleaner implementation can still create a performance regression.

---

# 121. Query Regression

Refactoring persistence code MUST check for:

```text
N+1

Additional Queries

Changed Fetch Strategy

Changed Pagination

Changed Index Usage
```

---

# 122. Remote Call Regression

Refactoring MUST NOT accidentally change:

```text
1 batch call
```

into:

```text
N individual calls
```

---

# 123. Parallelism

Parallelism SHOULD only be introduced for independent operations after batching and boundary design have been evaluated.

---

# 124. Virtual Threads

Virtual Threads MAY improve I/O orchestration but MUST retain bounded downstream concurrency.

---

# 125. Benchmark

Performance-sensitive refactoring SHOULD compare before/after measurements.

---

# 126. Compatibility

Modernization MUST explicitly evaluate backward compatibility.

---

# 127. API Compatibility

Check:

```text
Path

HTTP Method

Request Fields

Response Fields

Status Codes

Error Codes

Validation Behavior
```

---

# 128. Event Compatibility

Check:

```text
Event Type

Schema

Required Fields

Enum Values

Semantics
```

---

# 129. Database Compatibility

Check:

```text
Schema

Migration

Existing Data

Constraints

Indexes
```

---

# 130. Configuration Compatibility

Check:

```text
Property Names

Defaults

Environment Variables

Secrets

Feature Flags
```

---

# 131. Behavioral Compatibility

Compilation success does not prove behavioral compatibility.

---

# 132. Contract Comparison

Critical modernization SHOULD compare old and new behavior using representative scenarios.

---

# 133. Shadow Execution

Shadow execution MAY compare new implementation results without affecting production behavior.

---

# 134. Dual Read

Temporary dual-read comparison MAY be used when safe and operationally justified.

---

# 135. Dual Write

Dual writes are substantially riskier and require explicit consistency/recovery design.

---

# 136. Canary

Modernized behavior MAY be deployed through canary rollout.

---

# 137. Rollback

Material modernization MUST define rollback capability.

---

# 138. Database Rollback

Application rollback MUST account for forward database migrations.

---

# 139. Expand and Contract

Backward-compatible database modernization SHOULD use:

```text
EXPAND

MIGRATE

SWITCH

CONTRACT
```

---

# 140. Expand

Add new compatible schema without immediately removing old schema.

---

# 141. Migrate

Move/backfill data where required.

---

# 142. Switch

Move application behavior to the new representation.

---

# 143. Contract

Remove obsolete schema only after compatibility is no longer required.

---

# 144. Breaking Migration

A database migration that prevents rollback to the previous application version requires explicit deployment planning.

---

# 145. Technical Debt

Technical debt MUST be treated as managed engineering work.

---

# 146. Debt Register

Material debt SHOULD have:

```text
DESCRIPTION

IMPACT

RISK

AFFECTED AREA

OWNER

PRIORITY
```

---

# 147. Debt Is Not Every Imperfection

Minor stylistic preferences SHOULD NOT automatically become formal technical debt.

---

# 148. Debt Classification

Debt MAY be classified as:

```text
SECURITY

RELIABILITY

ARCHITECTURE

PERFORMANCE

MAINTAINABILITY

TESTING

DEPENDENCY

DATA
```

---

# 149. Debt Priority

Technical debt SHOULD be prioritized using risk rather than age alone.

---

# 150. Risk Model

A practical prioritization model MAY consider:

```text
IMPACT
    ×
PROBABILITY
    ×
CHANGE FREQUENCY
```

---

# 151. High Priority Debt

Examples:

```text
Known security vulnerability

Frequent production incident source

Data corruption risk

Critical unsupported dependency

Severe scalability bottleneck
```

---

# 152. Medium Priority Debt

Examples:

```text
God Service under frequent change

Duplicated critical rule

Poor testability

Complex legacy integration
```

---

# 153. Low Priority Debt

Examples:

```text
Stable cosmetic naming issue

Minor duplication in rarely changed code
```

---

# 154. Hotspot

Code that is both:

```text
COMPLEX
+
FREQUENTLY CHANGED
```

SHOULD receive higher modernization priority.

---

# 155. Stable Legacy

Ugly but stable code with low business risk MAY have lower priority than frequently changing critical code.

---

# 156. Boy Scout Rule

Engineers SHOULD improve nearby code when doing so is:

```text
LOW RISK

RELATED TO THE CHANGE

EASILY REVIEWABLE
```

---

# 157. Opportunistic Refactoring

Unrelated large refactoring SHOULD NOT be hidden inside a small business change.

---

# 158. Dedicated Refactoring

Large structural changes SHOULD receive dedicated work items/PRs.

---

# 159. Modernization Roadmap

Large modernization initiatives SHOULD be decomposed into measurable stages.

Example:

```text
STAGE 1
TEST BASELINE

STAGE 2
REMOVE DEAD CODE

STAGE 3
ISOLATE INTEGRATIONS

STAGE 4
DECOMPOSE GOD SERVICE

STAGE 5
MODERNIZE PERSISTENCE

STAGE 6
REMOVE LEGACY PATH
```

---

# 160. Round-Based Refactoring

Complex services MAY use explicit refactoring rounds.

---

# 161. Round 1

Typical first round:

```text
Dead Code

Obvious Duplication

Missing Tests

Simple Sonar Findings

Package Hygiene
```

---

# 162. Round 2

Typical second round:

```text
Class Responsibility

Dependency Reduction

Integration Boundaries

Mapping Consolidation

Validation Consolidation
```

---

# 163. Round 3

Typical third round:

```text
Performance

Concurrency

Persistence Optimization

Architecture Enforcement

Legacy Removal
```

---

# 164. Round Validation

Each round MUST validate the result before proceeding.

---

# 165. Build Gate

At minimum:

```text
clean build
```

must succeed.

---

# 166. Unit Test Gate

Unit tests MUST pass.

---

# 167. Integration Gate

Applicable integration tests MUST pass.

---

# 168. Coverage Gate

JaCoCo MUST remain compliant.

---

# 169. Sonar Gate

Applicable Sonar Quality Gate MUST pass.

---

# 170. SAST Gate

Applicable SAST gate MUST pass.

---

# 171. Architecture Gate

ArchUnit/architecture tests MUST pass.

---

# 172. Regression Gate

Critical business regression scenarios MUST pass.

---

# 173. Performance Gate

Performance-sensitive changes MUST show no unacceptable regression.

---

# 174. Observability Gate

New critical paths SHOULD retain appropriate logs/metrics/tracing according to platform standards.

---

# 175. Documentation Gate

Material architectural changes MUST update applicable documentation/ADR/package documentation.

---

# 176. `package-info.java`

Package documentation SHOULD be updated when package responsibility changes.

---

# 177. Stale Documentation

A refactoring is incomplete when documentation describes architecture that no longer exists.

---

# 178. Comments

Comments SHOULD explain:

```text
WHY
```

rather than restating obvious:

```text
WHAT
```

---

# 179. Obsolete Comment

Outdated comments are defects and SHOULD be removed or corrected.

---

# 180. TODO

TODOs SHOULD be actionable.

---

# 181. TODO Without Ownership

Long-lived:

```text
// TODO fix later
```

is discouraged.

---

# 182. Temporary Workaround

A workaround SHOULD document:

```text
WHY IT EXISTS

WHEN IT CAN BE REMOVED
```

---

# 183. Dependency Removal

Unused dependencies SHOULD be removed.

---

# 184. Dependency Upgrade

Modernization SHOULD prefer supported dependency versions consistent with the platform baseline.

---

# 185. Dependency Convergence

Duplicate/conflicting dependency versions SHOULD be resolved.

---

# 186. Transitive Dependency

Important security/runtime dependencies SHOULD NOT rely blindly on uncontrolled transitive versions.

---

# 187. API Removal

Obsolete endpoints MUST follow deprecation/compatibility policy before removal.

---

# 188. Event Removal

Obsolete event contracts MUST follow consumer migration policy.

---

# 189. Feature Removal

Removing a feature SHOULD also remove:

```text
Code

Configuration

Tests

Flags

Metrics

Documentation

Database Objects when safe
```

---

# 190. Half-Removed Feature

Leaving inactive infrastructure indefinitely after feature removal creates hidden technical debt.

---

# 191. Refactoring Metrics

Modernization SHOULD use metrics as signals.

Examples:

```text
Coverage

Complexity

Duplication

Class Dependencies

Build Time

Test Time

Incident Rate

Latency
```

---

# 192. Metric Gaming

Metrics MUST NOT become goals detached from engineering outcomes.

---

# 193. Zero Duplication

Zero duplication is not a universal objective.

---

# 194. Zero Sonar Issues

Zero Sonar issues does not guarantee good design.

---

# 195. Small Classes

Maximum number of classes is not a quality metric.

---

# 196. Low LOC

Minimum lines of code is not a quality metric.

---

# 197. Architecture Outcome

The objective is:

```text
CODE THAT IS

UNDERSTANDABLE

TESTABLE

CHANGEABLE

SECURE

OPERABLE

PERFORMANT
```

---

# 198. Review Checklist

Every material refactoring SHOULD evaluate:

```text
[ ] What behavior must remain unchanged?

[ ] Is current behavior protected by tests?

[ ] Are characterization tests required?

[ ] What responsibility does each affected class own?

[ ] Are there God Classes?

[ ] Are dependency counts excessive?

[ ] Are dependencies hiding unrelated responsibilities?

[ ] Can classes be safely merged?

[ ] Can obsolete classes be deleted?

[ ] Is duplicated code semantically identical?

[ ] Is there dead code?

[ ] Are utilities becoming dumping grounds?

[ ] Is cognitive complexity excessive?

[ ] Can guard clauses simplify logic?

[ ] Is a strategy/state model actually justified?

[ ] Are framework-specific dependencies leaking?

[ ] Are applied Flyway migrations untouched?

[ ] Are database changes additive/new migrations?

[ ] Could persistence behavior change?

[ ] Could N+1 be introduced?

[ ] Could remote-call count increase?

[ ] Is concurrency bounded?

[ ] Are APIs backward compatible?

[ ] Are events backward compatible?

[ ] Are configuration properties compatible?

[ ] Is rollback possible?

[ ] Do tests follow project standards?

[ ] Is JaCoCo >= approved threshold?

[ ] Does Sonar pass?

[ ] Does SAST pass?

[ ] Do architecture tests pass?

[ ] Is documentation updated?
```

---

# 199. Definition of Done for Refactoring Round

A refactoring round is complete only when applicable conditions are satisfied:

```text
[ ] Production code compiles

[ ] Test code compiles

[ ] Unit tests pass

[ ] Integration tests pass

[ ] Contract tests pass

[ ] Architecture tests pass

[ ] JaCoCo threshold passes

[ ] Sonar Quality Gate passes

[ ] SAST gate passes

[ ] No new critical/high vulnerability exists

[ ] No known behavioral regression exists

[ ] No unacceptable performance regression exists

[ ] Applied Flyway migrations were not modified

[ ] New database changes use new migrations

[ ] Dead code introduced by the refactoring is removed

[ ] Temporary compatibility code has removal criteria

[ ] package-info.java is updated where applicable

[ ] Architecture documentation is updated

[ ] Rollback/deployment implications are understood
```

---

# 200. Architecture Fitness Functions

Stable modernization rules SHOULD be automated.

Examples:

```text
[ ] No package cycles

[ ] Controllers do not access repositories directly

[ ] Domain does not depend on infrastructure

[ ] Coverage threshold enforced

[ ] Architecture tests pass

[ ] Dependency vulnerability scan passes

[ ] No forbidden deprecated APIs

[ ] No production dependency on test code

[ ] Applied migration checksums remain stable
```

---

# 201. Enterprise Modernization Gate

A modernization change is not considered compliant when applicable conditions include:

```text
[ ] Behavior changed unintentionally

[ ] Critical legacy behavior was refactored without tests

[ ] Big-bang rewrite has no migration strategy

[ ] Existing Flyway migration was edited

[ ] Sonar warning was hidden instead of corrected

[ ] SAST fix corrupts legitimate business data

[ ] God Service dependencies were merely hidden in a wrapper

[ ] Class decomposition produced meaningless one-method classes

[ ] Duplicate logic was centralized despite different domain semantics

[ ] Dead code remains after replacement

[ ] New implementation cannot be rolled back safely

[ ] Remote calls increased without analysis

[ ] Database query count materially increased unnoticed

[ ] Coverage fell below the approved gate

[ ] Tests were weakened only to make the build pass

[ ] Temporary migration flags have no removal plan
```

---

# 202. Anti-Patterns

The following are prohibited or strongly discouraged:

- rewrite because legacy code "looks ugly"
- refactoring without understanding current behavior
- changing business behavior accidentally during cleanup
- giant pull requests mixing formatting, refactoring and features
- hiding excessive dependencies inside a facade/bundle
- splitting every method into a class
- merging classes solely to reduce file count
- keeping dead code "just in case"
- commented-out legacy implementations
- modifying applied Flyway migrations
- abstracting syntactically similar but semantically different rules
- giant `Utils`, `Helper`, `Manager` classes
- suppressing Sonar without justification
- logging and rethrowing the same exception at every layer
- generic SAST sanitization that corrupts business values
- increasing JaCoCo through meaningless tests
- weakening tests because refactoring broke them
- introducing concurrency without capacity analysis
- replacing batch access with N+1 calls
- permanent migration feature flags
- modernization without rollback planning
- deleting legacy path before migration is proven
- assuming successful compilation means successful modernization

---

# 203. Positive Consequences

The decision provides:

- safer modernization
- controlled technical debt
- clearer refactoring criteria
- fewer God Classes
- less dead code
- lower duplication
- better testability
- stronger Java 21 adoption
- safer Spring Boot modernization
- improved Sonar/SAST compliance
- better rollback capability
- more reviewable changes
- reduced legacy risk

---

# 204. Negative Consequences

The decision introduces:

- additional analysis before major refactoring
- characterization-test effort
- incremental migration overhead
- temporary compatibility layers
- technical-debt governance
- stronger CI gates
- documentation requirements

These costs are accepted because uncontrolled modernization can create more risk than the legacy system it replaces.

---

# 205. Neutral Consequences

The decision also means:

- not every large class must be split
- not every small class should remain
- not every duplication must be removed
- not every legacy system should be rewritten
- not every Sonar issue represents architecture debt
- not every technical debt item deserves immediate remediation
- not every Java 21 feature needs adoption
- stable legacy code may intentionally remain unchanged
- modernization is a risk-management activity, not a syntax-upgrade exercise

---

# 206. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Regression during refactoring | Critical | Medium | Characterization tests |
| Big-bang modernization failure | Critical | Medium | Incremental migration |
| Lost legacy behavior | High | Medium | Contract comparison |
| Overengineering | Medium | High | Cohesion-based review |
| Sonar-driven bad design | Medium | Medium | Root-cause remediation |
| SAST data corruption | High | Medium | Contextual security fix |
| DB rollback failure | Critical | Medium | Expand/contract |
| Dead compatibility code | Medium | High | Removal criteria |
| Performance regression | High | Medium | Before/after measurement |
| Technical debt growth | High | High | Risk-based debt governance |

---

# 207. Implementation Guidance

The following rules are mandatory:

1. Material refactoring must begin by understanding current behavior.
2. Critical untested legacy behavior must receive characterization tests before restructuring.
3. Incremental modernization must be preferred over big-bang rewrites.
4. Technology, architecture and business changes should be separated where practical.
5. Java modernization must adopt modern capabilities only when they improve the design.
6. Spring Boot modernization must remove obsolete patterns where practical.
7. Classes must be decomposed based on responsibility rather than line count alone.
8. Classes should be unified when separation is artificial and semantics are identical.
9. Dead and superseded classes must be removed after safe migration.
10. Excessive dependency count must trigger architecture review.
11. Dependencies must not be hidden merely to satisfy constructor thresholds.
12. Duplicate business logic must be analyzed semantically before abstraction.
13. Applied Flyway migrations must never be modified.
14. Database corrections must use new versioned migrations.
15. Sonar findings must be fixed at root cause where practical.
16. SAST fixes must preserve legitimate business semantics.
17. Exception handling must follow handle-or-rethrow principles without duplicate logging.
18. JaCoCo coverage must remain at or above the approved threshold.
19. Tests must not be weakened merely to restore a green build.
20. Performance-sensitive refactoring must compare before/after behavior.
21. Refactoring must not introduce database or remote N+1 patterns.
22. New parallelism must remain bounded.
23. Public API/event/configuration compatibility must be explicitly evaluated.
24. Material modernization must have rollback strategy.
25. Database evolution should use expand-and-contract when backward compatibility is required.
26. Technical debt must be prioritized by risk and change frequency.
27. Complex modernization should use staged/round-based delivery.
28. Every round must pass build, tests, coverage, Sonar, SAST and architecture gates where applicable.
29. Temporary migration abstractions and flags must have removal criteria.
30. Documentation must be updated when architecture changes.

---

# 208. Validation

This ADR will be validated through:

- Java 21
- Spring Boot 3
- Gradle
- JUnit 5
- AssertJ
- Mockito
- Testcontainers
- ArchUnit
- JaCoCo
- SonarQube
- SAST
- dependency scanning
- Flyway validation
- contract tests
- integration tests
- performance tests
- architecture review
- pull-request review

---

# 209. Success Criteria

The decision is successful when:

- modernization occurs incrementally
- legacy behavior is protected before structural changes
- God Services decrease without class fragmentation exploding
- obsolete abstractions are removed
- dependency counts become explainable
- duplicated business rules decrease
- applied migrations remain immutable
- Sonar/SAST fixes improve rather than distort code
- coverage remains meaningful
- Java 21 adoption improves maintainability
- performance regressions are detected before production
- rollback remains possible during migrations
- technical debt is prioritized by business/technical risk
- refactoring rounds have objective completion criteria

---

# 210. Alternatives Rejected

## 210.1 Big-Bang Rewrite by Default

Rejected because undocumented behavior and migration risk are too high.

---

## 210.2 Never Refactor Working Code

Rejected because technical debt compounds and eventually increases change risk.

---

## 210.3 Sonar-Driven Refactoring Only

Rejected because static-analysis metrics cannot determine domain responsibility or architecture quality.

---

## 210.4 Maximum Class Decomposition

Rejected because excessive fragmentation increases cognitive overhead.

---

## 210.5 Zero Duplication at Any Cost

Rejected because incorrect abstractions create harmful coupling.

---

## 210.6 Modify Existing Migration

Rejected because applied Flyway migrations are immutable historical database changes.

---

## 210.7 Rewrite Tests to Match Every Refactoring Failure

Rejected because tests must distinguish intended requirement change from regression.

---

# 211. Related Decisions

This ADR extends and implements:

- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-057: Enterprise Event-Driven Architecture, Kafka Messaging and Transactional Outbox Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-059: Enterprise Redis Caching, Distributed Cache and Data Consistency Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-064: Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard
- ADR-065: Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard
- ADR-066: Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard
- ADR-067: Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard

---

# 212. References

- Java 21 Documentation
- Spring Boot Documentation
- Refactoring — Martin Fowler
- Working Effectively with Legacy Code — Michael Feathers
- Domain-Driven Design — Eric Evans
- Clean Architecture — Robert C. Martin
- Building Microservices — Sam Newman
- SonarQube Documentation
- OWASP Secure Coding Practices
- Flyway Documentation
- ArchUnit Documentation
- JaCoCo Documentation
- Testcontainers Documentation

---

# 213. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise refactoring and legacy-modernization baseline |

---

# 214. Decision Summary

The modernization workflow becomes:

```text
LEGACY CODE
    |
    v
UNDERSTAND
    |
    v
CHARACTERIZE
    |
    v
TEST BASELINE
    |
    v
REFACTOR
    |
    v
VALIDATE
    |
    v
MIGRATE
    |
    v
REMOVE LEGACY
```

The class-refactoring decision becomes:

```text
CLASS
  |
  v
MULTIPLE RESPONSIBILITIES?
  |
+-+----------------+
|                  |
YES                NO
|                  |
v                  v
IDENTIFY         KEEP
COHESIVE
CAPABILITIES
```

but:

```text
LARGE CLASS
   |
   X
   |
DOES NOT
AUTOMATICALLY MEAN
   |
   v
SPLIT INTO
20 CLASSES
```

Class consolidation becomes:

```text
CLASS A
   +
CLASS B
   |
   v
SAME DOMAIN CONCEPT?
   |
   v
SAME CHANGE DRIVER?
   |
   v
ARTIFICIAL SEPARATION?
   |
 +---+---+
 |       |
YES      NO
 |       |
 v       v
MERGE   KEEP
        SEPARATE
```

Class removal becomes:

```text
CLASS
 |
 v
USED?
 |
 +-- YES --> STILL OWNS
 |           UNIQUE VALUE?
 |              |
 |           +--+--+
 |           |     |
 |          YES    NO
 |           |     |
 |           v     v
 |          KEEP  REMOVE/
 |                CONSOLIDATE
 |
 +-- NO --> VERIFY
           REFLECTION /
           FRAMEWORK /
           CONFIG
              |
              v
            REMOVE
```

Sonar remediation becomes:

```text
SONAR FINDING
     |
     v
UNDERSTAND ROOT CAUSE
     |
     +--> CODE DEFECT
     |       |
     |       v
     |      FIX
     |
     +--> ARCHITECTURE SMELL
     |       |
     |       v
     |    REFACTOR
     |
     +--> FALSE POSITIVE
             |
             v
        JUSTIFIED
        SUPPRESSION
```

not:

```text
SONAR FINDING
     |
     v
CHANGE CODE UNTIL
WARNING DISAPPEARS
```

SAST remediation becomes:

```text
SOURCE
  |
  v
DATA FLOW
  |
  v
ACTUAL SINK
  |
  v
CORRECT CONTEXTUAL
PROTECTION
```

rather than:

```text
EVERY STRING
   |
   v
HTML ESCAPE
   |
   v
M&amp;M
```

Database modernization becomes:

```text
OLD SCHEMA
    |
    v
EXPAND
    |
    v
MIGRATE
    |
    v
SWITCH
    |
    v
CONTRACT
```

with the invariant:

```text
APPLIED FLYWAY
MIGRATION
    |
    v
IMMUTABLE
```

and every correction becomes:

```text
NEW CHANGE
    |
    v
NEW MIGRATION
    |
    v
NEW VERSION
```

Technical-debt prioritization becomes:

```text
RISK
 =
IMPACT
   ×
PROBABILITY
   ×
CHANGE FREQUENCY
```

which means:

```text
COMPLEX
+
FREQUENTLY CHANGED
+
BUSINESS CRITICAL
        |
        v
HIGH PRIORITY
```

while:

```text
UGLY
+
STABLE
+
LOW RISK
        |
        v
LOWER PRIORITY
```

A modernization round becomes:

```text
ROUND N
   |
   +--> REFACTOR
   |
   +--> TEST
   |
   +--> JACOCO
   |
   +--> SONAR
   |
   +--> SAST
   |
   +--> ARCHUNIT
   |
   +--> PERFORMANCE
   |
   v
VALIDATED BASELINE
   |
   v
ROUND N + 1
```

The complete modernization equation is:

```text
CURRENT-BEHAVIOR UNDERSTANDING
        +
CHARACTERIZATION TESTS
        +
INCREMENTAL REFACTORING
        +
COHESIVE CLASS RESPONSIBILITIES
        +
CONTROLLED CONSOLIDATION
        +
DEAD-CODE REMOVAL
        +
SEMANTIC DUPLICATION ANALYSIS
        +
JAVA 21 MODERNIZATION
        +
SPRING BOOT MODERNIZATION
        +
IMMUTABLE FLYWAY HISTORY
        +
SONAR ROOT-CAUSE REMEDIATION
        +
CONTEXTUAL SAST REMEDIATION
        +
MEANINGFUL COVERAGE
        +
PERFORMANCE VALIDATION
        +
COMPATIBILITY ANALYSIS
        +
ROLLBACK CAPABILITY
        +
RISK-BASED TECHNICAL DEBT
        =
SAFE ENTERPRISE MODERNIZATION
```

The governing principle is:

```text
Understand before changing.

Protect behavior before
restructuring it.

Refactor incrementally.

Do not rewrite merely because
legacy code looks old.

Do not preserve bad structure
merely because it works today.

Split classes because
responsibilities differ.

Merge classes because
their separation is artificial.

Delete code that no longer
provides value.

Do not hide dependencies
to satisfy metrics.

Do not create abstractions
solely to eliminate
superficial duplication.

Fix Sonar findings
at their root cause.

Fix SAST findings
at the correct security boundary.

Never corrupt legitimate
business data to silence
a security scanner.

Never modify an applied
Flyway migration.

Add a new migration.

Keep tests meaningful.

Keep JaCoCo above
the approved threshold.

Do not weaken valid tests
to make the build green.

Measure performance
before and after
sensitive refactoring.

Preserve compatibility
deliberately.

Plan rollback before deployment.

Remove temporary migration
code when migration ends.

Prioritize technical debt
by risk and change frequency.

And modernize one validated
step at a time.

The goal is not
modern-looking code.

The goal is a system
that becomes safer,
simpler and cheaper
to change after every
modernization round.
```

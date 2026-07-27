# ADR-076: Adopt Enterprise Database Migration, Zero-Downtime Schema Evolution and Expand/Contract Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-076 |
| Title | Adopt Enterprise Database Migration, Zero-Downtime Schema Evolution and Expand/Contract Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Database Migration, Flyway, PostgreSQL, Zero-Downtime Deployment |
| Related Work Items | PostgreSQL, Flyway, Kubernetes, Rolling Deployment, CI/CD |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Database schema changes are among the highest-risk changes in enterprise systems.

Application code can often be rolled back by redeploying a previous version.

Database changes are different.

A migration may:

```text
DROP COLUMN

RENAME COLUMN

CHANGE TYPE

ADD CONSTRAINT

BACKFILL MILLIONS OF ROWS

CREATE INDEX

MOVE DATA

DELETE DATA
```

and may affect running application versions immediately.

In a rolling deployment, multiple application versions can coexist:

```text
V1 POD
V1 POD
V2 POD
V2 POD
```

Both versions may access the same database simultaneously.

Database evolution must therefore preserve compatibility across deployment windows.

---

# 2. Problem Statement

The organization requires standards covering:

- Flyway
- migration immutability
- migration versioning
- schema compatibility
- rolling deployments
- zero-downtime migration
- expand/contract
- additive changes
- column rename
- column removal
- type change
- NOT NULL
- defaults
- indexes
- foreign keys
- large tables
- backfill
- batch migration
- dual-read
- dual-write
- rollback
- checksum validation
- migration observability
- migration testing
- deployment ordering
- mixed application versions
- data correction
- destructive migrations

---

# 3. Decision Drivers

Primary drivers are:

1. production safety
2. rollback capability
3. zero-downtime deployment
4. data integrity
5. deployment predictability
6. migration auditability
7. compatibility
8. performance
9. operational recovery
10. minimal locking
11. deterministic schema history
12. controlled technical debt

---

# 4. Decision

Production schema evolution MUST follow:

```text
EXPAND
   |
   v
MIGRATE / BACKFILL
   |
   v
SWITCH APPLICATION
   |
   v
VERIFY
   |
   v
CONTRACT
```

Destructive schema changes MUST NOT normally occur in the same deployment that introduces the replacement behavior.

---

# 5. Fundamental Principle

```text
Application versions
may coexist.

Therefore the database
must temporarily support
both old and new versions.

Expand first.

Migrate safely.

Switch behavior.

Contract later.
```

---

# 6. Flyway

Flyway is the standard schema migration mechanism.

---

# 7. Migration Immutability

An applied migration MUST NEVER be modified.

---

# 8. Historical Record

A Flyway migration represents immutable database history.

---

# 9. Checksum

Changing an applied migration changes its checksum and invalidates schema history.

---

# 10. Correction

If a previous migration contains an error:

```text
DO NOT EDIT IT
```

Create:

```text
A NEW MIGRATION
WITH A NEW VERSION
```

---

# 11. Example

Incorrect:

```text
V23__create_order_table.sql
```

is already applied.

Do not edit V23.

Create:

```text
V24__correct_order_table.sql
```

---

# 12. Migration Naming

Migration names SHOULD describe the intent.

Examples:

```text
V31__add_order_external_status.sql

V32__create_outbox_event_index.sql

V33__backfill_customer_segment.sql
```

---

# 13. One Migration, One Intent

A migration SHOULD represent one cohesive schema/data intent.

---

# 14. Giant Migration

A single migration containing many unrelated changes SHOULD be avoided.

---

# 15. Version Ordering

Migration versions MUST be strictly ordered according to project convention.

---

# 16. Duplicate Version

Two migrations MUST NOT use the same version.

---

# 17. Version Conflict

Conflicting migration versions across branches MUST be resolved before merge.

---

# 18. Baseline

Flyway baseline features MAY be used when onboarding an existing database, with explicit governance.

---

# 19. Repair

`flyway repair` MUST NOT be used casually to hide unexpected checksum divergence.

---

# 20. Repair Governance

Repair requires understanding why schema history differs from source-controlled migrations.

---

# 21. Validate

Flyway validation SHOULD execute in CI and deployment startup/pre-deployment checks where appropriate.

---

# 22. Expand Phase

The expand phase introduces backward-compatible schema changes.

---

# 23. Additive Migration

Preferred expand changes include:

```text
ADD NULLABLE COLUMN

ADD NEW TABLE

ADD NEW INDEX

ADD NEW COMPATIBLE VIEW

ADD NEW OPTIONAL CONSTRAINT
```

when safely implemented.

---

# 24. Compatibility

After expand:

```text
OLD APPLICATION
      +
NEW APPLICATION
```

must both remain functional during the intended coexistence window.

---

# 25. Column Addition

A new column SHOULD initially be nullable or have a backward-compatible default when old application versions do not populate it.

---

# 26. Immediate NOT NULL

Adding a required column with:

```text
NOT NULL
```

immediately can break old application versions.

---

# 27. Safe Required Column Evolution

Preferred sequence:

```text
1. ADD COLUMN NULLABLE

2. DEPLOY APPLICATION
   THAT WRITES THE COLUMN

3. BACKFILL HISTORICAL DATA

4. VERIFY NO NULLS

5. ADD NOT NULL

6. REMOVE TEMPORARY COMPATIBILITY
```

---

# 28. Default Value

Database defaults MAY help compatibility but MUST be intentionally designed.

---

# 29. Default Semantics

A default MUST represent valid business semantics.

---

# 30. Fake Default

Do not add meaningless defaults such as:

```text
status = 'UNKNOWN'
```

solely to satisfy NOT NULL if `UNKNOWN` is not a valid domain state.

---

# 31. Column Rename

Direct renaming can break running V1 instances.

---

# 32. Unsafe Rename

Avoid:

```sql
ALTER TABLE orders
RENAME COLUMN old_name TO new_name;
```

while old application versions still use `old_name`.

---

# 33. Safe Rename Strategy

Preferred pattern:

```text
EXPAND:
ADD new_name

MIGRATE:
COPY old_name -> new_name

SWITCH:
NEW APP USES new_name

CONTRACT:
DROP old_name
```

---

# 34. Dual Write

Temporary dual-write MAY be required during column replacement.

---

# 35. Dual-Write Risk

Dual-write creates consistency complexity and SHOULD be temporary.

---

# 36. Dual-Write Ownership

One application version SHOULD clearly own compatibility logic.

---

# 37. Database Trigger

Database triggers MAY support compatibility in exceptional cases but introduce hidden behavior and MUST be justified.

---

# 38. Application Dual Write

Application-level dual-write is generally easier to observe and test, but still requires reconciliation.

---

# 39. Dual-Write Duration

Dual-write MUST have explicit removal criteria.

---

# 40. Dual Read

Temporary dual-read MAY support gradual migration.

Example:

```text
IF new_column IS NOT NULL
    USE new_column
ELSE
    USE old_column
```

---

# 41. Dual-Read Debt

Fallback reads MUST be removed after migration is complete.

---

# 42. Column Removal

Dropping a column is a contract-phase operation.

---

# 43. Safe Removal

Before dropping a column verify:

```text
No deployed application reads it

No deployed application writes it

No report depends on it

No batch job depends on it

No trigger depends on it

No external integration depends on it
```

---

# 44. Delayed Drop

Destructive drops SHOULD occur in a later release after compatibility has been proven.

---

# 45. Table Removal

Table removal follows the same contract principle.

---

# 46. Type Change

Changing a column type can be dangerous.

---

# 47. In-Place Type Change

In-place type conversion SHOULD only be used when:

```text
Locking is acceptable

Conversion is safe

Old application remains compatible

Data volume is manageable
```

---

# 48. Large Type Migration

For large or incompatible type changes, prefer:

```text
ADD NEW COLUMN

BACKFILL

DUAL WRITE / SWITCH

DROP OLD COLUMN LATER
```

---

# 49. Enum Evolution

Database enum evolution requires special care.

---

# 50. PostgreSQL Enum

PostgreSQL native enum types SHOULD only be used when their migration limitations are understood.

---

# 51. Application Enum

Application enums MUST remain backward compatible during rolling deployment.

---

# 52. Remove Enum Value

Removing an enum value requires evidence that:

```text
No stored data uses it

No old application sends it

No event contract sends it
```

---

# 53. Backfill

Large historical data updates SHOULD be separated from schema changes where practical.

---

# 54. Small Backfill

A small bounded data correction MAY execute inside a Flyway migration.

---

# 55. Large Backfill

Large backfills SHOULD NOT hold long transactions or lock large tables during application startup.

---

# 56. Batch Backfill

Large migrations SHOULD process data in bounded batches.

Conceptually:

```text
SELECT 1000 ROWS

UPDATE

COMMIT

NEXT 1000
```

where operationally appropriate.

---

# 57. Backfill Job

Very large backfills MAY use a controlled application/batch job rather than a single Flyway transaction.

---

# 58. Backfill Tracking

External backfills MUST be:

```text
Observable

Restartable

Idempotent

Auditable
```

---

# 59. Backfill Completion

Contract-phase changes MUST wait until backfill completion is verified.

---

# 60. Backfill Validation

Validation SHOULD confirm:

```text
Rows Expected

Rows Migrated

Rows Failed

Remaining Old Representation
```

---

# 61. Idempotent Backfill

Backfill operations SHOULD be safe to resume.

---

# 62. Data Correction

Production data corrections MUST use controlled scripts/migrations or governed operational procedures.

---

# 63. Manual SQL

Untracked manual production SQL SHOULD be exceptional.

---

# 64. Emergency SQL

Emergency manual changes MUST be:

```text
Authorized

Audited

Reviewed

Reconciled into source-controlled migration/history
```

---

# 65. Index Creation

Index creation on large tables can affect production availability.

---

# 66. PostgreSQL Concurrent Index

For large production tables, consider:

```sql
CREATE INDEX CONCURRENTLY
```

when appropriate.

---

# 67. Transaction Limitation

`CREATE INDEX CONCURRENTLY` cannot run inside a normal transaction block.

Flyway migration configuration MUST account for this.

---

# 68. Index Failure

A failed concurrent index creation may leave an invalid index requiring cleanup.

---

# 69. Index Validation

After index creation verify:

```text
Index Valid

Expected Query Uses It

Write Cost Acceptable
```

---

# 70. Index Is Not Free

Indexes add:

```text
Storage

Write Cost

Vacuum Work

Maintenance
```

---

# 71. Duplicate Index

Migrations MUST avoid redundant indexes.

---

# 72. Foreign Key

Foreign keys protect relational integrity but may create locking/validation cost when added to large existing tables.

---

# 73. New FK

Adding a foreign key to large existing data SHOULD consider staged validation.

---

# 74. PostgreSQL NOT VALID

Where appropriate, PostgreSQL may use:

```sql
ADD CONSTRAINT ... NOT VALID
```

followed later by:

```sql
VALIDATE CONSTRAINT
```

to reduce immediate migration impact.

---

# 75. Constraint Validation

Constraint validation MUST eventually complete.

---

# 76. Permanent NOT VALID

Leaving required constraints permanently unvalidated is prohibited.

---

# 77. Check Constraint

Check constraints MAY help enforce domain invariants at the database boundary where appropriate.

---

# 78. Constraint Before Cleanup

A new constraint MUST not be applied before existing data is made compliant.

---

# 79. NOT NULL

NOT NULL should be added only after validation confirms no null values remain.

---

# 80. Large Table Locking

Large-table DDL MUST be evaluated for locking behavior.

---

# 81. Lock Analysis

Before production migration, determine:

```text
Lock Type

Expected Duration

Blocking Impact

Concurrent Write Impact
```

---

# 82. Lock Timeout

Migration sessions SHOULD use appropriate lock timeouts for risky operations.

---

# 83. Fail Rather Than Freeze

A migration SHOULD prefer failing within a bounded lock wait rather than blocking production indefinitely.

---

# 84. Statement Timeout

Long-running migration SQL SHOULD have deliberate statement timeout behavior.

---

# 85. Timeout Governance

Timeouts MUST reflect the specific migration operation and maintenance window.

---

# 86. Online Migration

Zero-downtime migration does not necessarily mean zero database work.

It means schema/data evolution occurs without unacceptable service interruption.

---

# 87. Rolling Deployment Compatibility

Every migration MUST consider the mixed-version window:

```text
OLD APP
   +
NEW APP
   +
SAME DB
```

---

# 88. Deployment Ordering

Typical safe ordering:

```text
1. EXPAND DATABASE

2. DEPLOY COMPATIBLE APPLICATION

3. BACKFILL / VERIFY

4. SWITCH BEHAVIOR

5. CONTRACT DATABASE LATER
```

---

# 89. Application Before Migration

Deploying application code that requires schema not yet present is unsafe.

---

# 90. Destructive Migration Before Application

Dropping old schema before old pods terminate is unsafe.

---

# 91. Migration Location

Flyway MAY execute:

```text
At application startup

As a dedicated deployment job

Through CI/CD migration stage
```

depending on platform architecture.

---

# 92. Startup Migration

Startup migration is acceptable for small safe migrations and controlled single-run semantics.

---

# 93. Multi-Pod Startup

Multiple replicas starting simultaneously MUST NOT cause unsafe concurrent migration execution.

---

# 94. Flyway Lock

Flyway schema-history locking provides coordination but long migrations can still delay application startup.

---

# 95. Dedicated Migration Job

Critical environments SHOULD consider a dedicated migration job when:

```text
Migrations are operationally sensitive

Rollout ordering matters

Application replicas should not perform DDL

Long-running migration exists
```

---

# 96. Migration Job Success

Application rollout MUST NOT proceed when required migration job fails.

---

# 97. Migration Permissions

Migration credentials MAY require broader privileges than runtime application credentials.

---

# 98. Separate Credentials

Production SHOULD consider separating:

```text
MIGRATION USER

APPLICATION USER
```

where operationally practical.

---

# 99. Least Privilege Runtime

The application runtime user SHOULD NOT retain unnecessary DDL privileges.

---

# 100. Migration Secrets

Migration credentials MUST follow secret-management standards.

---

# 101. Transactional Migration

Flyway migrations SHOULD run transactionally where supported and appropriate.

---

# 102. Non-Transactional Migration

Non-transactional migrations require explicit review because partial completion may occur.

---

# 103. Partial Migration

A failed non-transactional migration MUST have a documented recovery procedure.

---

# 104. Rollback

Database rollback is not equivalent to application rollback.

---

# 105. Forward Fix

Production schema incidents SHOULD generally prefer a new forward migration.

---

# 106. Down Migration

Automatic destructive down migrations are NOT the standard production rollback mechanism.

---

# 107. Reason

Rollback may be impossible after:

```text
Data Transformation

Data Deletion

New Writes

Type Conversion
```

---

# 108. Application Rollback

Schema expansion SHOULD preserve compatibility with the previous application version during the rollback window.

---

# 109. Rollback Window

Every risky release SHOULD define how long previous application versions remain compatible.

---

# 110. Contract After Rollback Window

Destructive contract migrations SHOULD occur only after rollback compatibility is no longer required.

---

# 111. Irreversible Migration

An irreversible migration requires explicit deployment risk review.

---

# 112. Data Deletion

Data deletion MUST follow data-retention/privacy governance.

---

# 113. Backup Is Not Migration Rollback

Restoring a database backup is a disaster-recovery action, not the ordinary response to a failed schema migration.

---

# 114. Restore Cost

Backup restoration can lose newer transactions depending on recovery strategy.

---

# 115. Migration Observability

Migration execution MUST be observable.

---

# 116. Required Signals

Track:

```text
Migration Version

Start Time

End Time

Duration

Success / Failure

Environment

Database
```

---

# 117. Migration SQL Logging

Sensitive migration SQL/data MUST not be indiscriminately exposed in logs.

---

# 118. Long Migration Alert

Unexpectedly long migrations SHOULD become operationally visible.

---

# 119. Lock Wait Alert

Migration blocking/lock waits SHOULD be diagnosable.

---

# 120. Failure Alert

Production migration failure MUST trigger immediate deployment failure/alert.

---

# 121. Migration Dashboard

Critical production environments MAY maintain migration history dashboards or deployment evidence.

---

# 122. Schema Version

Application diagnostics SHOULD expose safe schema/migration version metadata where useful.

---

# 123. Startup Log

Application startup SHOULD log the effective migration state at an appropriate safe level.

---

# 124. Checksum Validation

CI/deployment MUST detect modified applied migrations.

---

# 125. Migration Drift

Environment schema drift MUST be detected.

---

# 126. Manual Drift

Manual schema changes outside source-controlled migrations are prohibited except emergency procedures.

---

# 127. Drift Reconciliation

Emergency schema changes MUST later be represented in controlled migration history.

---

# 128. Database Clone Validation

Important migrations SHOULD be tested against representative production-like data volumes.

---

# 129. Empty Database Test

Testing only migration from an empty database is insufficient.

---

# 130. Upgrade Path Test

CI/integration testing SHOULD validate upgrade from a representative previous schema state.

---

# 131. Full Migration Test

The complete migration chain SHOULD also be tested from baseline/empty state where practical.

---

# 132. Both Paths Matter

Validate:

```text
NEW DATABASE
   ->
LATEST SCHEMA
```

and:

```text
PREVIOUS PRODUCTION SCHEMA
   ->
LATEST SCHEMA
```

---

# 133. Testcontainers

PostgreSQL Testcontainers SHOULD validate Flyway migration behavior.

---

# 134. PostgreSQL Fidelity

Migration tests SHOULD use the same major PostgreSQL behavior as production where practical.

---

# 135. H2

H2 MUST NOT be the sole validator for PostgreSQL migration behavior.

---

# 136. Migration Test

Tests SHOULD verify:

```text
Schema Exists

Constraints Exist

Indexes Exist

Backfill Correct

Application Can Read/Write
```

---

# 137. Compatibility Test

Where V1/V2 coexistence matters, tests SHOULD validate both application expectations against the expanded schema.

---

# 138. Large Migration Performance Test

Large-table migrations SHOULD be tested using representative data volume where practical.

---

# 139. Query Plan After Migration

Changes to:

```text
Indexes

Types

Columns

Constraints
```

SHOULD trigger relevant query-plan/performance validation.

---

# 140. Migration Review Checklist

Every migration SHOULD evaluate:

```text
[ ] Has this migration version ever been applied?

[ ] Is it additive or destructive?

[ ] Can V1 and V2 coexist?

[ ] Can old application still read/write?

[ ] Can new application run before contract phase?

[ ] Does this operation lock the table?

[ ] How long can the lock last?

[ ] Is the table large?

[ ] Is backfill required?

[ ] Can backfill be batched?

[ ] Is backfill idempotent?

[ ] Is CREATE INDEX CONCURRENTLY appropriate?

[ ] Is FK validation staged?

[ ] Is NOT NULL being introduced safely?

[ ] Is dual-read required?

[ ] Is dual-write required?

[ ] How will compatibility code be removed?

[ ] Can application rollback after this migration?

[ ] Is data loss possible?

[ ] Is a backup/restore plan relevant?

[ ] Is migration observable?

[ ] Has PostgreSQL Testcontainers validation passed?
```

---

# 141. Expand/Contract Checklist

## Expand

```text
[ ] New schema is additive

[ ] Old application remains compatible

[ ] New application can begin using new schema

[ ] No destructive removal occurs
```

## Migrate

```text
[ ] Historical data backfilled

[ ] Progress observable

[ ] Operation restartable

[ ] Validation confirms completeness
```

## Switch

```text
[ ] New application uses new representation

[ ] Old fallback no longer required operationally

[ ] Metrics confirm usage
```

## Contract

```text
[ ] Old versions no longer deployed

[ ] Rollback window closed

[ ] Old schema no longer used

[ ] Old compatibility code removed

[ ] Destructive migration reviewed
```

---

# 142. Migration Fitness Functions

Stable rules SHOULD be automated where practical.

Examples:

```text
[ ] Applied Flyway checksums unchanged

[ ] Migration versions unique

[ ] Flyway validate passes

[ ] PostgreSQL migration integration tests pass

[ ] No destructive migration in expand phase

[ ] Migration naming follows convention

[ ] Runtime DB user lacks unnecessary DDL rights

[ ] New schema changes use new migration files
```

---

# 143. Enterprise Migration Gate

A database change is not considered compliant when applicable conditions include:

```text
[ ] Existing applied Flyway migration was modified

[ ] Migration version is duplicated

[ ] New required column breaks old application

[ ] Column renamed while old pods still use old name

[ ] Column dropped in same deployment as replacement introduction

[ ] Large backfill runs as one uncontrolled transaction

[ ] Large table index creation causes avoidable blocking

[ ] New NOT NULL is added before data is populated

[ ] Foreign key validation can block production without analysis

[ ] Contract migration occurs before rollback window closes

[ ] Runtime app requires DDL privileges unnecessarily

[ ] Migration cannot be reproduced from source control

[ ] Manual production schema drift remains undocumented

[ ] Migration is tested only on empty H2 database

[ ] Production migration has no failure/recovery procedure
```

---

# 144. Anti-Patterns

The following are prohibited or strongly discouraged:

- editing applied Flyway migrations
- using `flyway repair` to hide uncontrolled drift
- destructive schema change in same release as replacement introduction
- direct column rename during mixed-version rollout
- immediate NOT NULL on newly introduced required data
- fake defaults to satisfy constraints
- giant backfill inside one startup transaction
- blocking index creation on huge tables without analysis
- adding foreign keys without existing-data validation
- permanent compatibility dual-write
- database triggers used as hidden long-term application logic
- dropping schema before rollback window closes
- application runtime with superuser/DDL privileges
- manual untracked production DDL
- assuming application rollback reverses database changes
- using database restore as ordinary migration rollback
- validating PostgreSQL migration only with H2
- schema changes with no production-size performance analysis

---

# 145. Positive Consequences

The decision provides:

- safer rolling deployments
- immutable database history
- stronger rollback compatibility
- lower migration-lock risk
- controlled large-table evolution
- predictable required-column introduction
- safer column renames
- better migration diagnostics
- reduced schema drift
- stronger CI validation
- improved zero-downtime capability
- clearer migration ownership

---

# 146. Negative Consequences

The decision introduces:

- multi-release schema evolution
- temporary compatibility code
- backfill jobs
- additional migration testing
- operational migration monitoring
- delayed cleanup of old schema

These costs are accepted because destructive single-step migrations create significantly greater production risk.

---

# 147. Neutral Consequences

The decision also means:

- schema cleanup may intentionally lag feature delivery
- some migrations require multiple releases
- dual-read/write may temporarily increase complexity
- not every migration requires a dedicated deployment job
- not every index requires concurrent creation
- forward fixes are preferred over automatic down migrations
- zero downtime requires application and database design together

---

# 148. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Old/new version incompatibility | Critical | Medium | Expand/contract |
| Table lock outage | Critical | Medium | Lock analysis |
| Long backfill | High | High | Batch processing |
| Migration drift | High | Medium | Flyway validate |
| Rollback incompatibility | Critical | Medium | Additive expansion |
| Invalid index | High | Low/Medium | Post-validation |
| Constraint failure | High | Medium | Staged validation |
| Partial non-transactional migration | High | Low/Medium | Recovery procedure |
| Startup delay | High | Medium | Dedicated migration job |
| Data loss | Critical | Low | Delayed destructive phase |

---

# 149. Implementation Guidance

The following rules are mandatory:

1. Applied Flyway migrations must never be modified.
2. Every database correction must use a new migration version.
3. Migration versions must be unique.
4. Schema changes must support rolling deployment compatibility where applicable.
5. Additive expansion must precede destructive contraction.
6. New required columns should be introduced nullable, populated, validated and only then constrained.
7. Direct renames should use expand/migrate/switch/contract for mixed-version systems.
8. Column/table removal must occur only after all consumers stop using them.
9. Large backfills must be bounded and restartable.
10. Large data migrations should be separated from startup where operational risk warrants it.
11. Large-table index creation must consider PostgreSQL concurrent indexing.
12. Foreign-key and constraint validation must consider lock/volume impact.
13. Database lock and statement timeout behavior must be explicit for risky migrations.
14. Migration execution strategy must be defined: startup, deployment job or pipeline stage.
15. Runtime application users should use least privilege and avoid unnecessary DDL rights.
16. Non-transactional migrations require explicit recovery plans.
17. Application rollback compatibility must be preserved during the defined rollback window.
18. Destructive contract migrations must wait until rollback compatibility is no longer required.
19. Production schema changes must be observable.
20. Flyway checksum validation must run automatically.
21. Manual schema drift must not remain outside source-controlled history.
22. Migrations must be tested against PostgreSQL.
23. Important migrations must validate both clean-install and upgrade paths.
24. Large migrations should be tested with representative data volume.
25. Query performance must be reevaluated when schema/index/type changes affect critical access paths.

---

# 150. Validation

This ADR will be validated through:

- PostgreSQL
- Flyway
- Java 21
- Spring Boot
- Spring Data JPA
- Kubernetes rolling deployments
- dedicated migration Jobs where applicable
- Testcontainers PostgreSQL
- Flyway validation
- schema drift checks
- integration tests
- performance tests
- query-plan analysis
- CI/CD migration gates
- deployment smoke tests

---

# 151. Success Criteria

The decision is successful when:

- applied migration checksums remain immutable
- rolling deployments do not fail because of schema incompatibility
- old application versions remain rollback-compatible during the planned window
- large migrations do not cause unexpected long production blocking
- required columns are introduced without downtime
- column renames/removals use staged evolution
- backfills can resume safely
- schema drift is detected
- migration failures stop deployment predictably
- runtime applications operate with reduced database privileges
- production migration duration and outcome are observable
- schema cleanup occurs only after compatibility is proven

---

# 152. Alternatives Rejected

## 152.1 Modify Existing Flyway Migration

Rejected because applied migrations are immutable historical records.

---

## 152.2 Destructive Migration in One Release

Rejected because mixed application versions and rollback become unsafe.

---

## 152.3 Direct Column Rename

Rejected as the standard for rolling systems because old versions still require the original name.

---

## 152.4 Immediate Required Column

Rejected because existing rows and old applications may not populate the value.

---

## 152.5 One Giant Backfill Transaction

Rejected because lock duration, WAL growth and recovery risk become excessive.

---

## 152.6 Database Restore as Routine Rollback

Rejected because restore affects unrelated newer data and is a disaster-recovery mechanism.

---

## 152.7 H2 as Migration Authority

Rejected because PostgreSQL-specific DDL and locking semantics differ.

---

# 153. Related Decisions

This ADR extends and implements:

- ADR-005: Use PostgreSQL as Primary Database
- ADR-006: Use Flyway for Database Migrations
- ADR-013: Use Testcontainers for Integration Testing
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-069: Enterprise Code Review, Refactoring, Technical Debt and Legacy Modernization Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard

---

# 154. References

- Flyway Documentation
- PostgreSQL Documentation
- PostgreSQL CREATE INDEX CONCURRENTLY
- PostgreSQL Constraint Validation
- Spring Boot Database Initialization Documentation
- Testcontainers PostgreSQL Documentation
- Kubernetes Deployment Documentation
- Expand and Contract Pattern
- Evolutionary Database Design — Martin Fowler
- Refactoring Databases — Scott Ambler and Pramod Sadalage
- Database Reliability Engineering

---

# 155. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise zero-downtime database migration baseline |

---

# 156. Decision Summary

Database evolution becomes:

```text
CURRENT SCHEMA
      |
      v
EXPAND
      |
      v
OLD + NEW APP
COMPATIBLE
      |
      v
BACKFILL
      |
      v
SWITCH APPLICATION
      |
      v
VERIFY
      |
      v
CONTRACT
```

Required-column introduction becomes:

```text
ADD NULLABLE COLUMN
       |
       v
DEPLOY WRITER
       |
       v
BACKFILL
       |
       v
VERIFY 0 NULLS
       |
       v
ADD NOT NULL
```

Column rename becomes:

```text
old_name
   |
   +--> ADD new_name
   |
   +--> COPY / DUAL WRITE
   |
   +--> SWITCH READERS
   |
   +--> STOP USING old_name
   |
   +--> DROP old_name
```

Migration history becomes:

```text
V1
 |
V2
 |
V3
 |
V4
```

and once applied:

```text
V3
 |
 X
 |
DO NOT EDIT
```

Correction becomes:

```text
V3 HAS A PROBLEM
      |
      v
CREATE V5 FIX
```

Large backfill becomes:

```text
MILLIONS OF ROWS
       |
       X
       |
DO NOT PROCESS
AS ONE GIANT TX
       |
       v
BOUNDED BATCHES
       |
       v
CHECKPOINT / RETRY
```

Rolling deployment compatibility becomes:

```text
DATABASE EXPANDED
      |
      +--> V1 WORKS
      |
      +--> V2 WORKS
      |
      v
V1 REMOVED
      |
      v
CONTRACT LATER
```

Rollback becomes:

```text
V2 PROBLEM
   |
   v
DEPLOY V1
   |
   v
EXPANDED SCHEMA
STILL SUPPORTS V1
```

instead of expecting:

```text
DOWN MIGRATION
   |
   v
MAGICALLY RESTORE
ALL PREVIOUS DATA
```

The complete database-evolution equation is:

```text
IMMUTABLE MIGRATION HISTORY
        +
ADDITIVE EXPANSION
        +
MIXED-VERSION COMPATIBILITY
        +
BOUNDED BACKFILL
        +
SAFE CONSTRAINT INTRODUCTION
        +
LOCK-AWARE DDL
        +
DELAYED DESTRUCTIVE CHANGES
        +
ROLLBACK WINDOW
        +
POSTGRESQL-SPECIFIC TESTING
        +
MIGRATION OBSERVABILITY
        =
ZERO-DOWNTIME-READY SCHEMA EVOLUTION
```

The governing principle is:

```text
Never edit an applied migration.

Fix history by adding
a new migration,
not rewriting the past.

Assume old and new
application versions
will coexist.

Expand first.

Do not remove old schema
while old code still exists.

Add required data gradually.

Backfill before constraining.

Rename by adding,
migrating and later removing.

Treat dual-read and dual-write
as temporary compatibility tools.

Do not lock giant tables
without understanding the lock.

Do not run enormous backfills
inside one startup transaction.

Use PostgreSQL features
with PostgreSQL-aware tests.

Keep runtime database privileges
smaller than migration privileges.

Prefer forward fixes.

Preserve rollback compatibility
before contracting the schema.

Observe migration duration,
locks and failure.

Test both:

a clean installation

and

an upgrade from the
previous production schema.

And remember:

application deployments
are reversible much more easily
than destructive data changes.

Design database evolution
accordingly.
```

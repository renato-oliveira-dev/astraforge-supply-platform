# ADR-084: Adopt Enterprise Database Schema Evolution, Flyway, Zero-Downtime Migration and Data Backfill Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-084 |
| Title | Adopt Enterprise Database Schema Evolution, Flyway, Zero-Downtime Migration and Data Backfill Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | PostgreSQL, Flyway, Schema Evolution, Database Deployment |
| Related Work Items | Flyway, PostgreSQL, Kubernetes, CI/CD, Zero-Downtime Deployment |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Database schema evolution is one of the highest-risk activities in independently deployed enterprise applications.

A seemingly simple change such as:

```sql
ALTER TABLE orders
ADD COLUMN external_status VARCHAR(30);
```

can interact with:

```text
OLD APPLICATION VERSION

NEW APPLICATION VERSION

ROLLING DEPLOYMENT

DATABASE LOCKS

LARGE TABLES

BACKFILL

INDEX CREATION

CONSTRAINT VALIDATION

REPLICATION

TRANSACTION DURATION

ROLLBACK
```

During a Kubernetes rolling deployment, multiple application versions may coexist:

```text
          DATABASE
          /      \
         /        \
        v          v
OLD PODS        NEW PODS
```

Therefore, database migration design MUST support application-version overlap.

---

# 2. Problem Statement

The organization requires standards covering:

- Flyway
- immutable migrations
- migration naming
- checksums
- migration ordering
- schema history
- baseline
- repair
- expand/contract
- rolling deployments
- backward compatibility
- zero-downtime schema evolution
- adding columns
- renaming columns
- removing columns
- type changes
- nullable to NOT NULL
- defaults
- foreign keys
- indexes
- unique constraints
- backfills
- large tables
- locks
- migration transactions
- migration timeout
- data correction
- rollback
- roll-forward
- DEV/HML/PRD consistency
- observability
- migration testing

---

# 3. Decision Drivers

Primary drivers are:

1. production safety
2. zero/minimal downtime
3. migration immutability
4. deterministic deployment
5. backward compatibility
6. data integrity
7. rollback safety
8. operational visibility
9. reproducibility
10. scalability
11. auditability
12. developer discipline

---

# 4. Decision

Flyway SHALL be the standard database schema migration mechanism for applicable services.

Once a versioned migration has been:

```text
APPLIED
```

to any shared/persistent environment, that migration becomes:

```text
IMMUTABLE
```

Any subsequent correction MUST be implemented through:

```text
A NEW MIGRATION
WITH A NEW VERSION
```

---

# 5. Fundamental Principle

```text
Never rewrite
database history.

Move the schema
forward.

If V17 is wrong
after deployment,

do not edit V17.

Create V18.
```

---

# 6. Migration Immutability

Applied migrations MUST NOT be modified.

This applies even when the required correction appears trivial.

---

# 7. Example

Incorrect:

```text
V17__add_status.sql
       |
       v
DEPLOYED
       |
       v
EDIT V17
```

Correct:

```text
V17__add_status.sql
       |
       v
DEPLOYED
       |
       v
V18__correct_status.sql
```

---

# 8. Why Immutability Matters

Flyway stores migration history and checksums.

Changing an already-applied migration can create:

```text
CHECKSUM MISMATCH

ENVIRONMENT DRIFT

NON-REPRODUCIBLE DATABASES

UNSAFE REPAIR

DEPLOYMENT FAILURE
```

---

# 9. Migration History

The Flyway schema-history table MUST be treated as operational deployment history.

---

# 10. Manual History Manipulation

Manual modification of Flyway history records is prohibited except under an explicitly approved recovery procedure.

---

# 11. Flyway Repair

`flyway repair` MUST NOT be used merely to hide an accidental modification of an applied migration.

---

# 12. Repair Purpose

Repair MAY be used only after the database state and migration history have been deliberately investigated and reconciled.

---

# 13. Checksum Mismatch

A checksum mismatch MUST initially be treated as evidence of migration-history divergence.

---

# 14. No Automatic Repair

CI/CD MUST NOT automatically execute repair after validation failure.

---

# 15. Versioned Migration

Permanent schema evolution SHOULD use versioned migrations.

Example:

```text
V31__add_external_status_to_orders.sql
```

---

# 16. Migration Naming

Migration descriptions SHOULD clearly communicate intent.

Prefer:

```text
V32__create_order_status_index.sql
```

over:

```text
V32__changes.sql
```

---

# 17. Version Collision

Two developers MUST NOT independently introduce the same migration version into a shared release branch.

---

# 18. Collision Resolution

If migration versions collide before deployment, one migration SHOULD receive a new unused version.

---

# 19. Applied Collision

If one conflicting version has already been applied, it MUST NOT be rewritten.

A new migration version MUST resolve the situation.

---

# 20. Migration Ordering

Migration execution order MUST be deterministic.

---

# 21. Schema Ownership

Each microservice SHOULD own migrations for its own database/schema.

---

# 22. Cross-Service Migration

One service SHOULD NOT normally modify another service's schema.

---

# 23. Shared Database

Where legacy architecture requires shared schemas, migration ownership MUST be explicitly governed.

---

# 24. Production Schema Change

Production schema changes MUST be represented by controlled migrations rather than manual SQL whenever practical.

---

# 25. Emergency SQL

Emergency manual database changes require:

```text
INCIDENT / CHANGE RECORD

REVIEW

AUDIT

FOLLOW-UP MIGRATION
```

so declarative migration history catches up with actual database state.

---

# 26. Environment Consistency

The same migration artifacts SHOULD progress through:

```text
DEV
  |
  v
HML / QA
  |
  v
PRD
```

---

# 27. Environment-Specific DDL

Different DDL scripts for the same logical migration across environments SHOULD be avoided.

---

# 28. Configuration vs Schema

Environment-specific application configuration SHOULD NOT normally be embedded into structural migrations.

---

# 29. Migration Validation

Flyway validation MUST occur before production migration execution.

---

# 30. Empty Database Test

The complete migration chain SHOULD be capable of constructing a valid database from an appropriate baseline.

---

# 31. Upgrade Test

Migration testing MUST also cover upgrading an existing representative schema.

---

# 32. Zero-Downtime Principle

Database changes MUST assume old and new application versions can coexist.

---

# 33. Rolling Deployment

During deployment:

```text
OLD VERSION
    |
    +--------+
             |
             v
          DATABASE
             ^
             |
    +--------+
    |
NEW VERSION
```

both versions may execute concurrently.

---

# 34. Expand/Contract

Breaking schema evolution SHOULD use:

```text
EXPAND
  |
  v
MIGRATE
  |
  v
CONTRACT
```

---

# 35. Expand Phase

The expand phase adds the new structure while preserving compatibility with the old application.

---

# 36. Migrate Phase

Applications/data are transitioned to the new representation.

---

# 37. Contract Phase

Old schema structures are removed only after no supported application version depends on them.

---

# 38. Example: Column Rename

Do NOT perform an immediate production rename:

```sql
ALTER TABLE customer
RENAME COLUMN old_name TO new_name;
```

when old pods still use:

```text
old_name
```

---

# 39. Safe Rename Phase 1

Add the new column:

```text
old_name

new_name
```

---

# 40. Safe Rename Phase 2

Deploy application compatibility.

Depending on the scenario:

```text
READ NEW / FALLBACK OLD

DUAL WRITE

DATABASE SYNCHRONIZATION
```

may temporarily be required.

---

# 41. Safe Rename Phase 3

Backfill:

```text
old_name
   |
   v
new_name
```

---

# 42. Safe Rename Phase 4

Move all reads/writes to:

```text
new_name
```

---

# 43. Safe Rename Phase 5

After old application versions are gone:

```text
DROP old_name
```

using a later migration.

---

# 44. Destructive Change

Destructive schema changes MUST NOT be combined casually with application rollout.

---

# 45. Column Removal

A column MUST only be removed after verifying:

```text
No Current Application Reads It

No Current Application Writes It

No Supported Old Version Uses It

No Batch Job Uses It

No Report Uses It

No Integration Uses It
```

---

# 46. Dependency Discovery

Database dependencies SHOULD be investigated before destructive changes.

---

# 47. Adding Nullable Column

Adding a nullable column is generally safer than immediately adding a mandatory populated column.

Example:

```sql
ALTER TABLE orders
ADD COLUMN company_code VARCHAR(30);
```

---

# 48. NOT NULL Evolution

For populated large tables, transition toward `NOT NULL` SHOULD normally be staged.

---

# 49. NOT NULL Phase 1

Add nullable column.

---

# 50. NOT NULL Phase 2

Deploy application writing the new field.

---

# 51. NOT NULL Phase 3

Backfill historical rows.

---

# 52. NOT NULL Phase 4

Validate no nulls remain.

---

# 53. NOT NULL Phase 5

Add/enforce the constraint using the safest supported database technique.

---

# 54. Mandatory Field Compatibility

The new application MUST NOT assume historical rows already contain a value before the backfill is complete.

---

# 55. Default Values

Database defaults MUST be deliberate.

---

# 56. Semantic Default

A default MUST represent a valid business meaning.

---

# 57. Fake Default

This is prohibited unless business-valid:

```text
missing monetary value
        |
        v
       0
```

or:

```text
missing status
        |
        v
    "UNKNOWN"
```

merely to satisfy a constraint.

---

# 58. Default and Existing Rows

The effect of adding a default to a populated table MUST be evaluated for the target PostgreSQL version and table size.

---

# 59. Application Default

Application defaults and database defaults MUST not silently contradict each other.

---

# 60. Type Change

Column-type changes MUST be analyzed for:

```text
LOCK

TABLE REWRITE

DATA LOSS

CAST FAILURE

INDEX IMPACT

APPLICATION COMPATIBILITY
```

---

# 61. Dangerous Type Change

A type alteration that rewrites a very large table SHOULD NOT be casually executed during normal application startup.

---

# 62. Replacement Column

High-risk type changes SHOULD consider:

```text
NEW COLUMN

BACKFILL

DUAL COMPATIBILITY

CUTOVER

OLD COLUMN REMOVAL
```

---

# 63. Data Truncation

Narrowing a field MUST validate all existing data before the constraint/type change.

---

# 64. Example

Before:

```text
VARCHAR(500)
```

to:

```text
VARCHAR(100)
```

verify:

```sql
SELECT MAX(LENGTH(column_name))
FROM table_name;
```

or an equivalent controlled validation.

---

# 65. Foreign Key

Foreign keys SHOULD be used when relational integrity requirements justify them.

---

# 66. FK Creation

Adding a foreign key to a large populated table MUST account for validation and locking cost.

---

# 67. Invalid Existing Data

Constraint creation MUST not assume historical data is valid.

---

# 68. Pre-Validation

Existing violations SHOULD be identified before constraint enforcement.

---

# 69. Constraint Validation

Where PostgreSQL supports safer staged validation, it SHOULD be considered for large production tables.

---

# 70. Unique Constraint

Unique constraints require explicit duplicate-data validation before enforcement.

---

# 71. Duplicate Discovery

Before introducing uniqueness:

```text
FIND DUPLICATES
      |
      v
DEFINE BUSINESS RESOLUTION
      |
      v
CLEAN DATA
      |
      v
ADD CONSTRAINT
```

---

# 72. Arbitrary Duplicate Deletion

Migration scripts MUST NOT arbitrarily delete duplicate business records without a defined resolution rule.

---

# 73. Index

Indexes MUST be justified by query/access patterns.

---

# 74. Index Cost

Every index increases:

```text
STORAGE

INSERT COST

UPDATE COST

DELETE COST

VACUUM WORK

MAINTENANCE
```

---

# 75. Large Table Index

Creating an index on a large production table can affect availability.

---

# 76. Concurrent Index

PostgreSQL `CREATE INDEX CONCURRENTLY` SHOULD be considered when avoiding prolonged write blocking is required.

---

# 77. Transaction Restriction

Operations such as:

```sql
CREATE INDEX CONCURRENTLY
```

have transaction restrictions that MUST be respected by Flyway configuration/migration design.

---

# 78. Migration Transaction

Do not assume every migration operation can execute safely inside the same transaction model.

---

# 79. Concurrent Index Failure

A failed concurrent index creation may leave an invalid index requiring controlled cleanup.

---

# 80. Index Verification

Index existence alone is insufficient; validity and expected definition SHOULD be verified where necessary.

---

# 81. Duplicate Index

Migrations SHOULD avoid creating semantically redundant indexes.

---

# 82. Composite Index

Composite index column order MUST reflect actual query predicates and sorting.

---

# 83. Index Review

Performance-related migrations SHOULD be supported by query-plan evidence where practical.

---

# 84. `EXPLAIN`

`EXPLAIN` / `EXPLAIN ANALYZE` SHOULD be used carefully to validate material index/query changes.

---

# 85. Production Explain

`EXPLAIN ANALYZE` on expensive modifying or heavy queries MUST be used with production-safety awareness.

---

# 86. Data Migration

Schema migration and large data migration are different operational concerns.

---

# 87. Small Deterministic Data Change

Small bounded reference-data changes MAY execute directly in Flyway.

---

# 88. Large Backfill

Large data backfills SHOULD NOT execute as one enormous transaction.

---

# 89. Example

Avoid:

```sql
UPDATE orders
SET company_code = ...
WHERE company_code IS NULL;
```

over tens of millions of rows during application startup without impact analysis.

---

# 90. Backfill Strategy

Large backfills SHOULD use:

```text
BOUNDED BATCHES

CHECKPOINTING

OBSERVABILITY

RESTARTABILITY

RATE CONTROL
```

---

# 91. Batch Size

Backfill batch size MUST be tuned according to:

```text
LOCK DURATION

WAL

REPLICATION

CPU

I/O

TRANSACTION SIZE
```

---

# 92. Restartability

A backfill MUST be safe to resume after partial completion.

---

# 93. Idempotent Backfill

Backfill logic SHOULD be idempotent where practical.

---

# 94. Checkpoint

Long-running backfills SHOULD record progress.

---

# 95. Backfill Ordering

Stable ordering SHOULD be used.

Example:

```text
PRIMARY KEY ASC
```

---

# 96. Offset Pagination

Large backfills SHOULD avoid inefficient high-offset scanning.

---

# 97. Keyset Progress

Prefer progress such as:

```text
WHERE id > :lastId
ORDER BY id
LIMIT :batchSize
```

where compatible with the identifier/access pattern.

---

# 98. Concurrent Writes

Backfill design MUST account for application writes occurring simultaneously.

---

# 99. Race Condition

Example:

```text
BACKFILL READS OLD VALUE

APPLICATION WRITES NEW VALUE

BACKFILL OVERWRITES NEW VALUE
```

MUST be prevented.

---

# 100. Conditional Update

Backfills SHOULD update only rows still requiring migration.

---

# 101. Example

Conceptually:

```sql
UPDATE ...
SET new_column = ...
WHERE id = ...
  AND new_column IS NULL;
```

when semantically appropriate.

---

# 102. Backfill Resource Limit

Backfills MUST NOT consume unrestricted database capacity.

---

# 103. Throttling

Large backfills SHOULD support throttling/pacing.

---

# 104. Pause

Operationally significant backfills SHOULD be pausable.

---

# 105. Cancel

Operators SHOULD have a safe cancellation mechanism.

---

# 106. Progress Metrics

Large backfills SHOULD expose:

```text
ROWS PROCESSED

ROWS REMAINING

BATCH DURATION

FAILURES

CURRENT CHECKPOINT
```

---

# 107. ETA

ETA MAY be exposed but SHOULD be treated as approximate.

---

# 108. Backfill Failure

A failed batch MUST NOT require restarting the entire data migration.

---

# 109. Poison Row

Rows that repeatedly fail SHOULD become diagnostically visible.

---

# 110. Data Correction

Data corrections MUST be deterministic and auditable.

---

# 111. Destructive Correction

Destructive production corrections SHOULD preserve enough evidence for investigation/audit where required.

---

# 112. Migration Startup

Running Flyway automatically at application startup MAY be acceptable for small, safe migrations.

---

# 113. Heavy Migration Startup

Long-running or operationally risky migrations SHOULD NOT depend on every application pod independently attempting them during startup.

---

# 114. Migration Job

High-risk production migrations SHOULD use a controlled deployment/migration job where platform practices support it.

---

# 115. Single Migrator

Only one logical migration execution SHOULD control a schema at a time.

---

# 116. Flyway Locking

Flyway's migration coordination MUST not be circumvented.

---

# 117. Startup Dependency

If application startup requires migration completion, deployment ordering MUST be explicit.

---

# 118. Migration Timeout

Migration execution MUST have an operationally appropriate maximum duration.

---

# 119. Lock Timeout

Potentially blocking DDL SHOULD consider a bounded lock timeout.

---

# 120. Fail Rather Than Block

For many production changes, it is safer to:

```text
FAIL MIGRATION
```

than wait indefinitely for a lock while impacting production.

---

# 121. Statement Timeout

Long-running migration statements SHOULD use intentional timeout policy where appropriate.

---

# 122. Timeout Selection

Timeouts MUST reflect the operation rather than using one arbitrary global value for every migration.

---

# 123. Lock Inspection

Before high-risk DDL, operational procedures SHOULD allow inspection of blocking sessions/locks.

---

# 124. Long Transaction

Long-running application transactions can prevent schema changes.

---

# 125. Deployment Coordination

High-risk migrations MAY require coordination with traffic/load windows.

---

# 126. Maintenance Window

A maintenance window MAY still be appropriate when a truly incompatible operation cannot safely be performed online.

---

# 127. Zero Downtime Is Not Dogma

Zero downtime SHOULD be pursued, but correctness and recoverability take precedence.

---

# 128. Explicit Downtime

If downtime is required, it MUST be intentional, communicated and tested.

---

# 129. Rollback

Database rollback is fundamentally different from application-binary rollback.

---

# 130. Application Rollback

A previous application version can often be redeployed quickly.

---

# 131. Schema Rollback

Destructive database rollback may be:

```text
IMPOSSIBLE

DANGEROUS

DATA-LOSING

SLOW
```

---

# 132. Roll-Forward

Production database recovery SHOULD generally prefer:

```text
ROLL FORWARD
```

through a corrective migration.

---

# 133. Down Migration

Automatic down migrations SHOULD NOT be assumed to be safe.

---

# 134. Destructive Rollback

Dropping newly created data to restore an old schema is prohibited unless the data-loss consequences are explicitly accepted.

---

# 135. Deployment Compatibility

Database expand migrations SHOULD usually be compatible with rollback to the previous application version.

---

# 136. Example

Good:

```text
ADD NULLABLE COLUMN
```

Old application ignores it.

New application uses it.

Application rollback remains possible.

---

# 137. Bad Migration

Risky:

```text
DROP COLUMN old_field
```

before the previous application version is retired.

---

# 138. Backup

High-risk migrations MUST consider backup/recovery posture.

---

# 139. Backup Is Not Rollback

Having a backup does not make a risky migration safe.

---

# 140. Restore Time

Recovery planning MUST consider:

```text
RTO

RPO

RESTORE DURATION

DATA WRITTEN AFTER BACKUP
```

---

# 141. Point-in-Time Recovery

PITR MAY be part of disaster recovery but is not a substitute for safe schema evolution.

---

# 142. Production Data Volume

Migration testing SHOULD use representative production-scale characteristics for high-risk operations.

---

# 143. Works on Empty DB

A migration completing instantly on an empty local database says little about behavior on a 500-million-row production table.

---

# 144. Table Size

Migration review SHOULD consider:

```text
ROW COUNT

TABLE SIZE

INDEX SIZE

WRITE RATE

READ RATE

DEAD TUPLES

REPLICATION
```

---

# 145. WAL

Large updates/index operations can generate significant WAL.

---

# 146. Replica Lag

Migration/backfill impact on replication lag MUST be considered.

---

# 147. Disk

Schema/data migration can temporarily require substantial extra disk.

---

# 148. Index Disk

Creating a replacement index requires space for both old and new structures during transition.

---

# 149. Disk Safety Margin

High-risk migrations MUST verify adequate disk headroom.

---

# 150. Vacuum

Large updates can create significant dead tuples and vacuum work.

---

# 151. Autovacuum Impact

Backfill rate SHOULD account for autovacuum and database maintenance capacity.

---

# 152. Statistics

Significant data/schema changes MAY require statistics refresh/analysis.

---

# 153. Query Plan Regression

Migration success does not guarantee application query performance remains acceptable.

---

# 154. Post-Migration Validation

Material migrations SHOULD validate:

```text
SCHEMA

DATA

CONSTRAINTS

INDEXES

QUERY PERFORMANCE

APPLICATION HEALTH
```

---

# 155. Migration Verification

A migration SHOULD define success criteria before execution.

---

# 156. Data Count

Counts MAY be useful but MUST not be the sole integrity check.

---

# 157. Reconciliation

Critical data migrations SHOULD reconcile source and target representations.

---

# 158. Null Validation

Before NOT NULL:

```sql
SELECT COUNT(*)
FROM table_name
WHERE new_column IS NULL;
```

should produce the expected result.

---

# 159. Duplicate Validation

Before uniqueness:

```text
duplicate count = 0
```

according to business semantics.

---

# 160. Data Sampling

Representative samples SHOULD supplement aggregate validation.

---

# 161. Migration Logging

Migration execution SHOULD produce structured operational logs.

---

# 162. Log Fields

Useful fields include:

```text
migration

version

result

duration

environment
```

---

# 163. SQL Logging

Sensitive data values MUST NOT be exposed through migration logging.

---

# 164. Metrics

Useful migration/backfill metrics include:

```text
migration_duration

migration_failure

backfill_processed

backfill_failed

backfill_remaining

backfill_batch_duration
```

---

# 165. Metric Cardinality

Migration version is bounded and MAY be used as a metric dimension.

---

# 166. Alerting

Production migration failures MUST become immediately visible.

---

# 167. Startup Failure

If Flyway validation/migration prevents service startup, the failure MUST be distinguishable from generic application failure.

---

# 168. Backfill Alert

Stalled backfills SHOULD trigger operational visibility.

---

# 169. Audit

Production migration execution SHOULD be attributable to:

```text
DEPLOYMENT

PIPELINE

CHANGE

VERSION
```

---

# 170. Migration Security

Migration credentials SHOULD follow least privilege while retaining required DDL capability.

---

# 171. Runtime Credentials

Application runtime credentials SHOULD NOT automatically receive broad DDL privileges if separate migration credentials are practical.

---

# 172. Separation

Production environments SHOULD consider:

```text
MIGRATION USER
        |
        +--> DDL

APPLICATION USER
        |
        +--> REQUIRED DML
```

---

# 173. Superuser

Applications MUST NOT use PostgreSQL superuser credentials for ordinary runtime operation.

---

# 174. Secret Management

Database credentials MUST use approved secret management.

---

# 175. SQL Injection

Dynamic migration/backfill SQL MUST NOT construct unsafe SQL from untrusted input.

---

# 176. Schema Qualification

Migration SQL SHOULD be explicit about schema where ambiguity exists.

---

# 177. Search Path

Unexpected `search_path` differences MUST NOT cause objects to be created in the wrong schema.

---

# 178. Object Ownership

Database object ownership MUST be intentional.

---

# 179. Extension

PostgreSQL extensions MUST require explicit architectural/security approval where applicable.

---

# 180. Extension Availability

A migration MUST NOT assume an extension exists in all environments without validation/provisioning.

---

# 181. Reference Data

Small controlled reference data MAY be maintained through Flyway when it is genuinely part of application deployment state.

---

# 182. Mutable Business Data

Frequently changing business configuration SHOULD NOT generally be managed as versioned schema migration data.

---

# 183. Seed Data

Development/test seed data SHOULD be separated from production-required migration data.

---

# 184. Production Test Data

Production migrations MUST NOT insert development/sample data.

---

# 185. PII

Migration scripts MUST NOT contain real personal production data.

---

# 186. Data Masking

Non-production migration testing using production-derived datasets MUST follow approved masking/anonymization standards.

---

# 187. Repeatable Migration

Flyway repeatable migrations MAY be used for appropriate database objects such as controlled views/functions where their replacement semantics are understood.

---

# 188. Repeatable Migration Risk

Repeatable migrations MUST NOT become a mechanism for uncontrolled mutable schema history.

---

# 189. Versioned Structural Change

Critical structural schema evolution SHOULD remain explicit through versioned migrations.

---

# 190. Stored Procedure

Changes to stored procedures/functions MUST consider callers running old and new versions concurrently.

---

# 191. Function Signature

Removing/changing a database function signature can break existing application pods.

---

# 192. Compatible Function Evolution

New signatures SHOULD coexist during migration windows where required.

---

# 193. View

Views used as compatibility layers MAY support staged migration.

---

# 194. Compatibility View

Compatibility views MUST have a defined removal plan.

---

# 195. Trigger

Temporary migration triggers MAY be used for dual-write compatibility only with strong justification.

---

# 196. Trigger Risk

Triggers can create hidden:

```text
WRITE COST

LOCKING

RECURSION

DEBUGGING COMPLEXITY
```

---

# 197. Trigger Removal

Temporary compatibility triggers MUST have an explicit removal migration.

---

# 198. Dual Write

Application-level dual write between old/new columns MAY be used temporarily.

---

# 199. Dual-Write Window

Dual-write periods MUST be bounded.

---

# 200. Dual-Write Divergence

If old/new fields can diverge, reconciliation MUST be defined.

---

# 201. Feature Flag

Feature flags MAY control transition between old/new schema behavior.

---

# 202. Flag Does Not Replace Schema Compatibility

A feature flag cannot make an already dropped column available to old pods.

---

# 203. Deployment Sequence

A typical safe schema rollout is:

```text
1. EXPAND DATABASE

2. DEPLOY COMPATIBLE APPLICATION

3. BACKFILL DATA

4. SWITCH READ PATH

5. OBSERVE

6. RETIRE OLD APPLICATION

7. CONTRACT DATABASE
```

---

# 204. Multi-Release Change

A safe breaking schema change MAY intentionally span multiple application releases.

---

# 205. Release Pressure

Release speed MUST NOT justify collapsing expand/contract into one unsafe deployment.

---

# 206. Migration Review Classification

Migrations SHOULD be classified as:

```text
LOW RISK

MEDIUM RISK

HIGH RISK
```

---

# 207. Low-Risk Example

Potential example:

```text
ADD NULLABLE COLUMN
```

to a small/moderate table after lock impact review.

---

# 208. Medium-Risk Example

Potential examples:

```text
CREATE INDEX

ADD VALIDATED CONSTRAINT

BOUNDED DATA UPDATE
```

depending on table size.

---

# 209. High-Risk Example

Examples:

```text
LARGE TABLE REWRITE

DROP COLUMN

LARGE BACKFILL

TYPE CONVERSION

UNIQUE CONSTRAINT ON DIRTY DATA

MASSIVE INDEX BUILD
```

---

# 210. Risk Depends on Scale

No DDL operation is automatically safe merely because the SQL is short.

---

# 211. Migration Pull Request

Migration PRs SHOULD describe:

```text
WHY

TABLE SIZE

LOCK RISK

BACKWARD COMPATIBILITY

ROLLBACK / ROLL-FORWARD

BACKFILL

VALIDATION
```

for material changes.

---

# 212. Migration Comments

Complex migrations SHOULD contain concise comments explaining non-obvious operational choices.

---

# 213. SQL Formatting

Migration SQL SHOULD remain readable and reviewable.

---

# 214. Destructive Marker

Destructive migrations SHOULD be clearly identifiable during review.

---

# 215. CI Validation

CI SHOULD detect duplicate migration versions.

---

# 216. CI Checksum

CI SHOULD detect unexpected modifications to established migrations where repository history allows.

---

# 217. Static SQL Review

Automated checks MAY detect dangerous patterns such as:

```text
DROP TABLE

DROP COLUMN

TRUNCATE

UNBOUNDED UPDATE

UNBOUNDED DELETE
```

for mandatory human review.

---

# 218. False Positive

Automated migration gates SHOULD support explicit reviewed exceptions rather than silent bypass.

---

# 219. Testcontainers

Migration integration tests SHOULD use a real PostgreSQL version compatible with production.

---

# 220. H2

H2 MUST NOT be considered sufficient validation for PostgreSQL-specific migration behavior.

---

# 221. Migration Test

Tests SHOULD verify Flyway can migrate:

```text
EMPTY / BASELINE DATABASE

SUPPORTED PREVIOUS VERSION
```

---

# 222. Schema Assertion

Critical objects SHOULD be verified after migration.

---

# 223. Constraint Assertion

Tests SHOULD verify expected:

```text
PK

FK

UNIQUE

NOT NULL
```

constraints.

---

# 224. Index Assertion

Critical indexes SHOULD be verified.

---

# 225. Data Migration Test

Data transformations MUST verify representative edge cases.

---

# 226. Idempotency Test

Restartable backfills SHOULD be executed repeatedly in tests where practical.

---

# 227. Partial Completion Test

Backfill testing SHOULD simulate interruption and restart.

---

# 228. Concurrent Write Test

Critical backfills SHOULD test concurrent application writes where race conditions are possible.

---

# 229. Old Application Compatibility

Expand-phase migrations SHOULD be validated against the previous supported application version where practical.

---

# 230. New Application Compatibility

New application behavior MUST be validated against the expanded schema before contract migration.

---

# 231. Contract Migration Test

Contract-phase removal MUST verify no supported application still depends on removed objects.

---

# 232. Performance Test

High-risk migration performance SHOULD be tested against representative data volume.

---

# 233. Lock Test

Potentially blocking DDL SHOULD have lock behavior understood before production.

---

# 234. Backfill Load Test

Large backfill batch size SHOULD be tested against database resource consumption.

---

# 235. Replica Test

Where replicas are critical, migration impact on replication SHOULD be evaluated.

---

# 236. Failure Test

Migration failure MUST leave a known recoverable state.

---

# 237. Transactional DDL

Where DDL is transactional, failure behavior SHOULD leverage transaction safety.

---

# 238. Non-Transactional Operation

Non-transactional operations require explicit partial-failure recovery procedures.

---

# 239. Documentation

Material multi-release migrations SHOULD have an operational runbook.

---

# 240. Runbook

Runbook SHOULD include:

```text
PRECHECK

EXECUTION

MONITORING

VALIDATION

ABORT CONDITIONS

ROLL-FORWARD

CLEANUP
```

---

# 241. Abort Condition

High-risk migrations MUST define when operators should stop.

Examples:

```text
REPLICA LAG > LIMIT

LOCK WAIT > LIMIT

DATABASE CPU > LIMIT

ERROR RATE > LIMIT

BACKFILL FAILURE RATE > LIMIT
```

---

# 242. Migration Review Checklist

```text
[ ] Is this a new migration version?

[ ] Has any existing applied migration been modified?

[ ] Is the migration description meaningful?

[ ] Could the version collide with another branch?

[ ] Is the change backward compatible?

[ ] Can old and new application pods coexist?

[ ] Does this require expand/contract?

[ ] Is any column/table being dropped?

[ ] Is any column being renamed?

[ ] Is any column type changing?

[ ] Is NOT NULL being introduced?

[ ] Is a default business-valid?

[ ] Is a foreign key being added?

[ ] Is uniqueness being introduced?

[ ] Has historical data been validated?

[ ] Is an index being created?

[ ] How large is the table?

[ ] Could the operation rewrite the table?

[ ] Could the operation block writes?

[ ] Is CREATE INDEX CONCURRENTLY appropriate?

[ ] Does the operation require non-transactional Flyway handling?

[ ] Is there a large data backfill?

[ ] Is the backfill batched?

[ ] Is the backfill restartable?

[ ] Can concurrent writes race with the backfill?

[ ] Is progress observable?

[ ] Is database capacity bounded during execution?

[ ] Is replication impact acceptable?

[ ] Is disk headroom sufficient?

[ ] Is roll-forward defined?

[ ] Can the previous application version still run?

[ ] Are validation queries defined?

[ ] Has migration been tested on real PostgreSQL?

[ ] Has representative data volume been tested?
```

---

# 243. Database Migration Fitness Functions

Stable controls SHOULD be automated where practical.

Examples:

```text
[ ] Applied migration files are immutable

[ ] New schema change uses a new Flyway version

[ ] Duplicate migration versions fail CI

[ ] Flyway validate executes in CI

[ ] Migrations execute against PostgreSQL Testcontainers

[ ] Destructive SQL receives explicit review

[ ] Backfills have bounded batch size

[ ] Backfills are restartable

[ ] Production migrations contain no sample data

[ ] Migration SQL contains no credentials

[ ] Old application compatibility is tested for expand changes

[ ] Contract changes are separated from expand changes

[ ] Migration failures are observable
```

---

# 244. Enterprise Database Migration Gate

A migration is not considered compliant when applicable conditions include:

```text
[ ] Previously applied migration was edited

[ ] flyway repair is used to hide checksum drift

[ ] Production schema is changed manually without reconciliation

[ ] Old and new application compatibility was ignored

[ ] Column is renamed while old pods still require old name

[ ] Column is dropped in the expand deployment

[ ] NOT NULL is introduced before historical data is ready

[ ] Fake default is used only to satisfy constraint

[ ] Large table is rewritten without impact analysis

[ ] Large index is built with no locking/capacity analysis

[ ] Huge data backfill executes as one transaction

[ ] Backfill cannot resume

[ ] Backfill can overwrite newer application data

[ ] Migration can wait indefinitely for database lock

[ ] Database rollback assumes destructive down migration is safe

[ ] Production-scale characteristics were ignored

[ ] Migration can exhaust disk/WAL/replication capacity

[ ] DDL runs using unrestricted superuser runtime credentials

[ ] Development/sample data is inserted into production

[ ] PostgreSQL-specific migration was validated only against H2

[ ] Destructive change has no validation or recovery plan
```

---

# 245. Anti-Patterns

The following are prohibited or strongly discouraged:

- editing an applied Flyway migration
- using repair to normalize accidental history changes
- manual production DDL without migration reconciliation
- direct rename during rolling deployment
- drop column in same release that stops using it
- immediate NOT NULL on unprepared historical data
- fake business defaults
- massive single-transaction backfill
- unbounded UPDATE/DELETE
- large blocking index creation without analysis
- ignoring `CREATE INDEX CONCURRENTLY` transaction semantics
- schema changes tied to pod startup regardless of cost
- indefinite lock waits
- automatic destructive down migrations
- superuser runtime credentials
- environment-specific migration history
- production sample/test data
- H2-only migration testing
- assuming local empty-database speed represents production
- collapsing expand and contract because of release pressure

---

# 246. Positive Consequences

The decision provides:

- deterministic migration history
- reproducible environments
- safer rolling deployments
- reduced production lock risk
- explicit backward compatibility
- controlled large-data backfills
- safer schema cleanup
- improved auditability
- better disaster recovery posture
- improved database deployment observability
- reduced Flyway checksum incidents
- stronger CI/CD migration quality gates

---

# 247. Negative Consequences

The decision introduces:

- multi-step schema changes
- temporary duplicate columns
- temporary compatibility logic
- additional migrations
- longer deprecation periods
- backfill infrastructure
- operational review for high-risk DDL
- more integration/performance testing

These costs are accepted because database corruption, deployment failure and production downtime have substantially greater cost.

---

# 248. Neutral Consequences

The decision also means:

- some changes span multiple releases
- roll-forward is often preferable to rollback
- not all migrations should run automatically at pod startup
- a backup is not equivalent to a rollback strategy
- short SQL can still be operationally dangerous
- zero downtime is a design constraint rather than a property provided automatically by Flyway

---

# 249. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Migration history drift | Critical | Medium | Immutable migrations |
| Table lock | Critical | Medium | Lock analysis/timeouts |
| Long table rewrite | Critical | Medium | Expand/contract |
| Backfill overload | High | Medium | Batched throttling |
| Replica lag | High | Medium | Rate control |
| Disk exhaustion | Critical | Low/Medium | Capacity precheck |
| Application incompatibility | Critical | Medium | Rolling compatibility |
| Data loss | Critical | Low/Medium | Roll-forward + validation |
| Duplicate data blocks constraint | High | Medium | Pre-validation |
| Migration startup outage | Critical | Medium | Controlled migration execution |

---

# 250. Implementation Guidance

The following rules are mandatory:

1. Flyway versioned migrations are immutable after application to a persistent/shared environment.
2. Every correction to an applied migration must use a new migration version.
3. Flyway repair must not hide uncontrolled migration modifications.
4. Schema history must not be manually manipulated outside approved recovery procedures.
5. Migration versions must be unique and CI-validated.
6. The same migration chain should progress through DEV, HML/QA and PRD.
7. Schema changes must support rolling-deployment compatibility.
8. Breaking changes must use expand/migrate/contract.
9. Destructive contract migrations must occur only after old consumers are retired.
10. Mandatory fields on populated tables should be introduced through staged evolution.
11. Defaults must represent valid business semantics.
12. Large type changes/table rewrites require explicit operational analysis.
13. Historical data must be validated before introducing new constraints.
14. Large index creation must consider locking and concurrent-index options.
15. Flyway transaction behavior must respect PostgreSQL operation restrictions.
16. Large data backfills must use bounded, restartable batches.
17. Backfills must not overwrite newer concurrent application data.
18. Long-running backfills must expose progress and failure metrics.
19. Production migrations must use bounded lock/statement behavior where appropriate.
20. High-risk migrations should use controlled migration jobs rather than incidental pod startup.
21. Database recovery should generally prefer corrective roll-forward migrations.
22. Migration planning must account for WAL, replicas, disk, vacuum and query-plan effects.
23. High-risk changes must define prechecks, validation and abort criteria.
24. Runtime application users should not receive unnecessary DDL/superuser privileges.
25. Production migration scripts must not contain sample data, credentials or real PII.
26. PostgreSQL migrations must be tested against real PostgreSQL-compatible infrastructure.
27. Migration tests must cover both clean creation and upgrade paths.
28. Expand migrations should be compatible with the previous supported application version.
29. Contract migrations must verify that removed structures are no longer consumed.
30. Material migrations must be observable and auditable through the deployment pipeline.

---

# 251. Validation

This ADR will be validated through:

- PostgreSQL
- Flyway
- Java 21
- Spring Boot
- Kubernetes
- CI/CD
- Testcontainers PostgreSQL
- JUnit 5
- AssertJ
- database metadata assertions
- migration upgrade tests
- compatibility tests
- representative-volume performance tests
- lock analysis
- backfill restart tests
- failure injection
- operational runbooks

---

# 252. Success Criteria

The decision is successful when:

- applied Flyway migrations never require rewriting
- checksum drift is exceptional and investigated
- old and new application versions can coexist during schema rollout
- breaking schema changes can be performed through controlled phases
- large backfills can stop and resume safely
- migrations cannot wait indefinitely on production locks
- database changes do not unexpectedly saturate disk, WAL or replicas
- previous application versions can remain viable during expand phases
- destructive contract phases occur only after dependency retirement
- migration failures have deterministic recovery procedures
- production database history remains reproducible and auditable

---

# 253. Alternatives Rejected

## 253.1 Modify Existing Flyway Migration

Rejected because it rewrites deployment history and causes checksum/environment divergence.

---

## 253.2 Automatically Run Flyway Repair

Rejected because it can conceal actual schema-history inconsistencies.

---

## 253.3 Rename/Drop Columns in One Deployment

Rejected because rolling deployments may still contain old application instances.

---

## 253.4 One Huge Backfill Transaction

Rejected because of lock duration, WAL, rollback, replication and restartability risks.

---

## 253.5 Automatic Down Migration

Rejected because destructive schema rollback can lose production data.

---

## 253.6 H2 as Migration Validation

Rejected because PostgreSQL-specific DDL, locking, indexes, types and transaction semantics are not adequately represented.

---

## 253.7 Manual Production DDL as Normal Process

Rejected because it destroys reproducibility, auditability and environment consistency.

---

# 254. Related Decisions

This ADR extends and implements:

- ADR-013: Use Testcontainers for Integration Testing
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-071: Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-077: Enterprise Scheduled Jobs, Batch Processing, Distributed Scheduling and Workload Coordination Standard
- ADR-083: Enterprise Service-to-Service Communication, Service Discovery, Internal APIs and Zero-Trust Networking Standard

---

# 255. References

- Flyway Documentation
- PostgreSQL Documentation
- PostgreSQL Explicit Locking Documentation
- PostgreSQL Index Documentation
- PostgreSQL `CREATE INDEX CONCURRENTLY`
- PostgreSQL Constraints Documentation
- PostgreSQL MVCC Documentation
- Spring Boot Flyway Documentation
- Testcontainers PostgreSQL Documentation
- Kubernetes Deployment Documentation
- Martin Fowler — Evolutionary Database Design
- Expand and Contract Pattern
- Google Site Reliability Engineering

---

# 256. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise Flyway and zero-downtime database migration baseline |

---

# 257. Decision Summary

Flyway history becomes:

```text
V16
 |
 v
APPLIED

V17
 |
 v
APPLIED
 |
 X
NEVER EDIT

CORRECTION
 |
 v
V18
```

Schema evolution becomes:

```text
EXPAND
   |
   v
OLD + NEW STRUCTURE
   |
   v
DEPLOY COMPATIBLE APP
   |
   v
BACKFILL
   |
   v
SWITCH
   |
   v
OBSERVE
   |
   v
CONTRACT
```

Column rename becomes:

```text
old_name
   |
   +--> ADD new_name
   |
   +--> COMPATIBLE APP
   |
   +--> BACKFILL
   |
   +--> SWITCH READ/WRITE
   |
   +--> RETIRE OLD APP
   |
   v
DROP old_name
```

NOT NULL evolution becomes:

```text
ADD NULLABLE
      |
      v
WRITE NEW VALUES
      |
      v
BACKFILL OLD ROWS
      |
      v
VERIFY NULL COUNT = 0
      |
      v
ENFORCE NOT NULL
```

Large data migration becomes:

```text
MILLIONS OF ROWS
       |
       v
BOUNDED BATCH
       |
       v
COMMIT
       |
       v
CHECKPOINT
       |
       v
THROTTLE
       |
       v
NEXT BATCH
```

instead of:

```text
BEGIN
 |
 v
UPDATE 100,000,000 ROWS
 |
 v
WAIT
 |
 v
WAL / LOCK / REPLICA PRESSURE
 |
 v
FAIL AFTER HOURS
 |
 v
ROLLBACK EVERYTHING
```

Production recovery becomes:

```text
BAD MIGRATION RESULT
       |
       v
INVESTIGATE
       |
       v
NEW CORRECTIVE MIGRATION
       |
       v
ROLL FORWARD
```

rather than rewriting migration history.

The complete database-evolution equation is:

```text
IMMUTABLE FLYWAY HISTORY
        +
NEW VERSION FOR EVERY CORRECTION
        +
VALIDATION
        +
EXPAND / CONTRACT
        +
ROLLING COMPATIBILITY
        +
SAFE CONSTRAINT EVOLUTION
        +
LOCK ANALYSIS
        +
BOUNDED DDL
        +
BATCHED BACKFILL
        +
RESTARTABILITY
        +
CONCURRENT-WRITE SAFETY
        +
CAPACITY ANALYSIS
        +
ROLL-FORWARD RECOVERY
        +
REAL POSTGRESQL TESTING
        +
OBSERVABILITY
        =
SAFE ENTERPRISE DATABASE EVOLUTION
```

The governing principle is:

```text
The database remembers
every deployment.

Do not rewrite
that memory.

Once a Flyway migration
has been applied,

it is immutable.

If it is wrong,
create the next migration.

Do not repair history
to make the checksum green.

Understand why
the checksum changed.

Assume old and new pods
run at the same time.

Expand first.

Migrate second.

Contract later.

Add before removing.

Backfill before constraining.

Validate before enforcing.

Do not invent fake defaults
to make DDL convenient.

Do not rename a column
while old code still needs it.

Do not drop a column
because the newest code
stopped using it yesterday.

Do not run
a hundred-million-row UPDATE
as an incidental startup task.

Batch it.

Checkpoint it.

Throttle it.

Make it restartable.

Protect concurrent writes.

Watch WAL.

Watch replicas.

Watch disk.

Watch locks.

Bound lock waits.

Sometimes failing quickly
is safer than waiting
for production indefinitely.

Test against PostgreSQL.

Not an approximation
of PostgreSQL.

Test the upgrade path,
not only an empty database.

Test with realistic volume
when the migration is risky.

Remember that
application rollback
and database rollback
are not the same thing.

Prefer compatible expansion
and corrective roll-forward.

And above all:

never modify
an already-applied
Flyway migration.

Create a new version.

Always.
```

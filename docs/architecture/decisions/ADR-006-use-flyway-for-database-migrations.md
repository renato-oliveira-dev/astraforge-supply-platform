# ADR-006: Use Flyway for Database Migrations

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-006 |
| Title | Use Flyway for Database Migrations |
| Status | Accepted |
| Date | 2026-07-23 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Database Evolution |
| Related Work Items | Initial platform architecture |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform requires a reliable, repeatable and auditable process for evolving PostgreSQL database schemas across all environments.

The platform stores business-critical data related to:

- orders
- order items
- approvals
- workflow state
- audit information
- transactional outbox records
- integration metadata
- operational configuration

Database changes must remain synchronized with application releases.

Examples of database changes include:

- table creation
- column additions
- column type changes
- constraints
- indexes
- sequences
- views
- data corrections
- reference-data insertion
- backfills
- performance improvements
- removal of obsolete structures

Manual database changes create significant risk.

Without controlled migrations, environments may diverge and application deployments may fail unpredictably.

---

# 2. Problem Statement

The platform requires a database migration mechanism that:

- versions every schema change
- executes changes in a deterministic order
- tracks which changes were applied
- prevents untracked manual evolution
- integrates with Spring Boot
- supports PostgreSQL
- supports automated testing
- works in local, CI and production environments
- provides failure visibility
- prevents modification of previously applied migrations
- supports gradual schema evolution
- supports rollback through forward corrective migrations

The decision must also define the governance rules for creating, reviewing, applying and correcting migrations.

---

# 3. Decision Drivers

The primary decision drivers are:

1. schema consistency
2. repeatability
3. auditability
4. deployment safety
5. environment parity
6. automation
7. PostgreSQL compatibility
8. Spring Boot integration
9. Testcontainers compatibility
10. immutable history
11. failure transparency
12. controlled data migration
13. CI/CD integration
14. operational simplicity
15. long-term maintainability

---

# 4. Constraints

The decision must consider:

- PostgreSQL is the primary transactional database
- Spring Boot is the application framework
- Gradle is used for builds
- workloads run in containers
- production deployments may run on Kubernetes
- integration tests use PostgreSQL through Testcontainers
- the application may run multiple replicas
- schema evolution must be automated
- database changes may include both DDL and DML
- applied migrations must remain immutable
- production access must follow least privilege
- deployment pipelines must detect migration failures

---

# 5. Considered Options

## 5.1 Option A: Flyway

Flyway manages versioned database migrations using ordered migration files and a schema history table.

### Advantages

- simple version-based model
- strong PostgreSQL support
- mature Spring Boot integration
- straightforward SQL migration support
- schema history tracking
- checksum validation
- deterministic ordering
- Testcontainers compatibility
- broad enterprise adoption
- low operational complexity
- supports repeatable migrations where justified
- integrates well with CI/CD

### Disadvantages

- rollback is not automatic by default
- migration discipline is required
- applied migration edits cause validation failures
- complex data migrations require careful design
- large migrations may require operational coordination
- team members must understand version ordering

---

## 5.2 Option B: Liquibase

Liquibase supports migrations through XML, YAML, JSON or SQL changelogs.

### Advantages

- rich change abstraction
- rollback support
- database-agnostic change definitions
- extensive metadata
- mature ecosystem
- flexible changelog composition

### Disadvantages

- greater configuration complexity
- abstract change syntax may obscure generated SQL
- larger conceptual surface
- more verbose for PostgreSQL-specific changes
- no current requirement justifies the additional complexity
- rollback definitions may provide false confidence for unsafe production changes

---

## 5.3 Option C: Manual Database Scripts

Database changes could be executed manually by administrators.

### Advantages

- full execution control
- no migration framework dependency
- convenient for emergency changes

### Disadvantages

- environment drift
- weak traceability
- inconsistent execution order
- high human-error risk
- poor CI/CD integration
- difficult local setup
- difficult automated testing
- undocumented production state
- no reliable checksum validation

---

## 5.4 Option D: Hibernate Automatic Schema Management

Hibernate could automatically create or update the schema.

Examples:

```text
create
update
create-drop
```

### Advantages

- low initial effort
- convenient for prototypes
- schema generated from entity mappings

### Disadvantages

- unsuitable for controlled production evolution
- generated changes may be unsafe
- poor auditability
- unclear execution plans
- difficult data migration
- limited index and constraint control
- application model becomes the schema authority
- may cause destructive or unexpected changes

---

## 5.5 Option E: Custom Migration Framework

The platform could build its own migration runner.

### Advantages

- complete customization
- no third-party migration dependency

### Disadvantages

- unnecessary engineering cost
- increased operational risk
- duplicate implementation of mature capabilities
- checksum and locking complexity
- weak ecosystem support
- long-term maintenance burden

---

# 6. Decision

The Enterprise Order Platform will use Flyway as the exclusive database migration mechanism.

Flyway will manage:

- schema creation
- schema modification
- constraints
- indexes
- reference data
- controlled data backfills
- views
- functions where approved
- migration history
- migration validation

The authoritative database history will reside in versioned migration files committed to the application repository.

Manual production schema changes are prohibited except under an approved emergency process.

---

# 7. Rationale

Flyway was selected because it provides a simple, SQL-oriented and deterministic migration model that aligns with PostgreSQL and Spring Boot.

The platform requires database evolution to be:

- visible in source control
- reviewable
- testable
- repeatable
- auditable
- compatible with automated deployment

Flyway's version history and checksum validation provide strong protection against accidental modification of applied migrations.

The SQL-first model also preserves visibility into the exact PostgreSQL operations being executed.

---

# 8. Fundamental Immutability Rule

The following rule is mandatory:

> A migration that already exists and may have been applied must never be modified.

This includes changes to:

- SQL statements
- whitespace
- comments
- file encoding
- filenames
- version identifiers
- line endings
- object names
- data values
- checksums

Any correction must be implemented through a new migration with a new version.

This rule applies even when the previous migration contains:

- an incorrect column size
- an incomplete index
- a missing constraint
- incorrect reference data
- a naming error
- a performance issue
- an implementation defect

The database history is append-only.

---

# 9. Why Applied Migrations Are Immutable

Editing a previously applied migration may cause:

- Flyway checksum validation failure
- inconsistent schemas across environments
- unreproducible production state
- misleading version-control history
- failed deployments
- manual repair pressure
- inability to recreate historical environments
- data loss risk
- audit inconsistencies

A new migration makes the correction explicit and preserves the real evolution of the schema.

---

# 10. Migration Types

Flyway supports:

- versioned migrations
- repeatable migrations

Versioned migrations are the default and mandatory mechanism for schema and data evolution.

Repeatable migrations may be used only for suitable objects such as:

- views
- selected database functions
- selected stored procedures
- deterministic derived objects

Repeatable migrations must not be used to bypass versioning discipline.

---

# 11. Versioned Migration Naming

Migration files must follow:

```text
V<version>__<description>.sql
```

Examples:

```text
V1__create_order_schema.sql

V2__create_orders_table.sql

V3__create_order_items_table.sql

V4__create_outbox_events_table.sql

V5__add_order_version_column.sql
```

Rules:

- use uppercase `V`
- separate version and description with two underscores
- use lowercase descriptions
- use underscores between words
- use concise business-oriented descriptions
- do not rename migrations after they are shared or applied

---

# 12. Version Strategy

The default version strategy is sequential integer numbering.

Example:

```text
V1

V2

V3

V4
```

For repositories with concurrent migration development, a higher-granularity convention may be approved, such as:

```text
V202607231430
```

or:

```text
V42_1
```

The repository must use one documented convention consistently.

Version collisions must be resolved before merging.

---

# 13. Migration Location

Default migration location:

```text
src/main/resources/db/migration
```

Example:

```text
src/main/resources/db/migration
├── V1__create_order_schema.sql
├── V2__create_orders_table.sql
├── V3__create_order_items_table.sql
└── V4__create_outbox_events_table.sql
```

Migration files must be included in the deployable application artifact unless migration execution is delegated to a dedicated approved deployment component.

---

# 14. Schema History Table

Flyway records migration state in:

```text
flyway_schema_history
```

This table contains information such as:

- installed rank
- version
- description
- migration type
- script name
- checksum
- installed by
- execution time
- success status

The schema history table must not be manually edited under normal operations.

---

# 15. Baseline Strategy

New databases should start from the full ordered migration history.

Flyway baseline should be used only when onboarding an existing database whose prior schema history was not managed by Flyway.

Baseline use requires:

- architecture approval
- schema verification
- documented baseline version
- environment inventory
- controlled rollout
- backup confirmation

Baseline must not be used to hide missing migrations.

---

# 16. Automatic Migration Execution

Spring Boot may run Flyway during application startup.

This approach is appropriate when:

- exactly one process performs migration safely
- Flyway locking behavior is understood
- database credentials permit migrations
- deployment sequencing is controlled
- migration duration is bounded

In production, migration execution may alternatively occur through:

- a dedicated Kubernetes Job
- a deployment pipeline step
- a dedicated migration container
- an approved administrative process

The selected production model must be documented.

---

# 17. Multiple Application Replicas

When multiple replicas start concurrently, only one must apply migrations.

Flyway's metadata locking provides coordination, but deployment architecture must still consider:

- startup timeout
- migration duration
- replica readiness
- database lock contention
- failed migration handling
- application-version compatibility

For significant migrations, a dedicated migration job is preferred before application rollout.

---

# 18. Startup Behavior

The application must fail startup when:

- a required migration fails
- checksum validation fails
- migration order is invalid
- the schema version is incompatible
- a pending migration cannot be applied
- Flyway cannot access the configured schema

Starting the application against an unknown or partially migrated schema is prohibited.

---

# 19. Production Execution Model

The recommended production sequence is:

```text
Build artifact

↓

Run migration validation

↓

Apply pending migrations

↓

Verify schema state

↓

Deploy application instances

↓

Run smoke checks
```

For backward-compatible migrations, limited overlap between application versions may be allowed.

For incompatible changes, deployment sequencing must be explicitly planned.

---

# 20. Expand-and-Contract Strategy

Breaking schema changes must use expand-and-contract migration.

Example:

```text
Step 1: Add new nullable column

Step 2: Deploy application writing old and new columns

Step 3: Backfill existing data

Step 4: Deploy application reading new column

Step 5: Stop using old column

Step 6: Remove old column in a later release
```

This strategy reduces deployment coupling and supports rolling updates.

---

# 21. Backward Compatibility

Database changes should remain compatible with:

- the currently deployed application version
- the new application version
- rolling deployment overlap where applicable

Examples of safer changes:

- adding nullable columns
- adding columns with safe defaults
- adding indexes
- adding new tables
- adding optional constraints after data validation

Examples of potentially breaking changes:

- renaming columns
- dropping columns
- changing column types
- tightening nullability
- modifying enum-like check constraints
- changing primary keys
- changing uniqueness semantics

Breaking changes require staged migration.

---

# 22. Column Renaming

Direct column renaming should be avoided during rolling deployments.

Preferred process:

1. add the new column
2. update application writes
3. backfill data
4. update application reads
5. remove the old column later

Direct renaming may be acceptable only when:

- downtime is explicitly approved
- no old application version remains active
- dependent reports and integrations are validated
- rollback implications are understood

---

# 23. Column Removal

Columns must not be removed in the same release that stops using them.

Recommended sequence:

1. stop reading the column
2. stop writing the column
3. monitor for unexpected use
4. remove application mapping
5. remove the column in a later migration

This provides a recovery window.

---

# 24. Nullability Changes

Changing a column from nullable to non-null requires:

1. identify existing null values
2. define a valid backfill rule
3. backfill existing records
4. add validation in the application
5. add the database constraint
6. validate production behavior

The constraint must not be introduced before data is valid.

---

# 25. Data Type Changes

Column type changes require analysis of:

- conversion safety
- table size
- index rebuild
- lock duration
- application compatibility
- precision loss
- data truncation
- rollback strategy
- replication impact

Large or risky conversions should use a new column and staged migration.

---

# 26. Data Migrations

Data migrations must be:

- deterministic
- idempotent where practical
- bounded
- reviewed
- tested with representative data
- observable
- safe under production volume

Data migrations must not depend on:

- external HTTP services
- non-deterministic current state outside the database
- local files unavailable in production
- unbounded application loops
- arbitrary execution order

---

# 27. Reference Data

Reference data may be inserted through versioned migrations when it is:

- required by the application
- stable
- version-controlled
- environment-independent
- part of the domain or platform baseline

Environment-specific operational data should not be embedded in general migrations.

---

# 28. Data Correction Migrations

Production data corrections must use a new versioned migration when they are deterministic and appropriate for all target environments.

The migration must define:

- affected records
- selection criteria
- expected row count
- correction rule
- safety checks
- validation query

Where the correction is environment-specific, a controlled operational procedure may be more appropriate than a general migration.

---

# 29. Large Data Backfills

Large backfills must be planned separately from simple schema migrations.

Consider:

- batch size
- transaction size
- lock duration
- write amplification
- replication lag
- vacuum impact
- execution time
- retry behavior
- application load
- deployment window

Large backfills may require:

- a dedicated job
- incremental processing
- resumable checkpoints
- operational throttling
- post-deployment execution

A large backfill must not block application startup indefinitely.

---

# 30. Transactional Behavior

PostgreSQL supports transactional DDL for many operations.

Migrations should use transactions where safe.

However, some operations may require special handling.

Examples include:

```text
CREATE INDEX CONCURRENTLY
```

Such migrations may not be compatible with a normal transactional migration.

The migration must document this explicitly and use appropriate Flyway configuration or migration separation.

---

# 31. Index Creation

Index migrations must consider:

- table size
- write activity
- lock duration
- index build time
- disk usage
- query benefit
- deployment window

For large production tables, concurrent index creation may be required.

Example:

```sql
CREATE INDEX CONCURRENTLY idx_orders_status_created_at
    ON orders (status, created_at);
```

The operational constraints of concurrent index creation must be tested and documented.

---

# 32. Index Removal

Before removing an index:

- confirm it is unused
- review query plans
- review production metrics
- consider foreign key implications
- validate no reporting workload depends on it

Index removal is a versioned migration.

---

# 33. Constraints

Constraints should be introduced carefully.

Examples:

- foreign keys
- unique constraints
- check constraints
- not-null constraints

For large existing tables, constraints may need staged validation.

The application must not assume a constraint exists before the migration is applied.

---

# 34. Foreign Keys

Foreign keys may be used within a bounded context.

Cross-context foreign keys should be avoided.

Adding a foreign key to existing data requires:

- orphan analysis
- cleanup
- index review
- lock analysis
- deployment planning

---

# 35. Unique Constraints

Unique constraints should represent stable data invariants.

Before adding one:

- detect duplicates
- define duplicate resolution
- validate null semantics
- consider case sensitivity
- consider normalization
- review concurrent write behavior

Application validation alone is insufficient for uniqueness.

---

# 36. Check Constraints

Check constraints may provide defense in depth for stable invariants such as:

- positive quantity
- non-negative totals
- known status values
- valid date relationships

Rapidly changing business policy should not be encoded exclusively as a rigid database constraint.

---

# 37. Database Functions and Procedures

Database functions and procedures should be used only when they provide clear value.

Possible use cases:

- data-intensive operations
- database-local consistency
- performance-critical transformations
- operational maintenance

Business logic should remain in the Domain layer by default.

Changes to functions and procedures must be version-controlled through Flyway.

---

# 38. Views

Views may be created through migrations for:

- reporting
- simplified read models
- compatibility layers
- operational queries

View definitions must be reviewed for:

- performance
- dependency chains
- security
- stable column contracts
- migration impact

Repeatable migrations may be considered for deterministic view definitions.

---

# 39. Repeatable Migrations

Repeatable migrations follow:

```text
R__<description>.sql
```

Example:

```text
R__order_reporting_view.sql
```

They execute when their checksum changes.

Repeatable migrations must not be used for:

- incremental data changes
- destructive schema changes
- one-time backfills
- sequence-sensitive evolution
- corrections that require historical ordering

---

# 40. Placeholders

Flyway placeholders should be minimized.

Migration behavior should remain consistent across environments.

Environment-specific placeholders may introduce schema drift.

They may be used only when:

- the value is operationally required
- behavior remains equivalent
- the variation is documented
- automated tests cover the variation

---

# 41. Multiple Schemas

If a bounded context uses a dedicated PostgreSQL schema, Flyway configuration must explicitly define:

- managed schema
- default schema
- schema history table location
- permissions
- creation ownership

Cross-schema dependencies must be documented and minimized.

---

# 42. Database User Separation

Production environments should separate:

- migration credentials
- application runtime credentials
- administrative credentials

The migration user may have DDL privileges.

The application runtime user should have only required DML privileges.

The application must not run permanently with schema-owner permissions.

---

# 43. Least Privilege

Migration and runtime users must follow least privilege.

Runtime access should be limited to required operations such as:

- select
- insert
- update
- delete
- sequence usage
- execution of approved functions

Administrative capabilities must not be granted to application users.

---

# 44. Secrets

Database credentials must not be stored in migration files or source-controlled configuration.

Credentials must come from:

- environment variables
- Kubernetes Secrets
- approved secret managers
- deployment platform configuration

Logs must not expose credentials or connection strings containing secrets.

---

# 45. Encoding and Line Endings

Migration files must use:

```text
UTF-8
```

Repositories should normalize line endings to avoid checksum drift.

Recommended `.gitattributes` entry:

```text
*.sql text eol=lf
```

Encoding and line-ending rules must be consistent before migrations are shared.

---

# 46. Comments

Comments inside migrations are permitted and encouraged when they explain:

- non-obvious operations
- safety decisions
- compatibility strategy
- data assumptions
- operational requirements

However, comments in applied migrations must not later be edited because Flyway checksum validation includes file content.

---

# 47. Formatting

SQL should follow a consistent style.

Recommended practices:

- uppercase SQL keywords
- lowercase object names
- one major clause per line
- explicit column lists
- descriptive constraint names
- descriptive index names
- semicolon termination
- avoid unnecessary vendor ambiguity

Formatting changes must not be applied retroactively to an applied migration.

---

# 48. Object Naming

Database object names should follow persistence guidelines.

Examples:

```text
pk_orders

fk_order_items_order

uk_orders_order_number

idx_orders_status_created_at

ck_order_items_quantity_positive
```

Names should remain deterministic and understandable in production diagnostics.

---

# 49. Migration Review

Every migration must undergo code review.

Review must include:

- correctness
- backward compatibility
- object naming
- index impact
- lock impact
- transaction behavior
- data safety
- expected row counts
- performance
- rollback strategy
- deployment sequencing
- observability
- security

A migration is production code.

---

# 50. Pull Request Requirements

A pull request containing a migration should include:

- migration purpose
- expected schema change
- backward-compatibility analysis
- deployment order
- data impact
- estimated execution time
- locking considerations
- validation queries
- recovery plan
- associated application changes

For significant migrations, attach execution-plan or staging evidence.

---

# 51. Local Development

Local development must initialize the database through Flyway.

Developers must not rely on:

- manual SQL setup
- IDE-generated schema
- Hibernate schema update
- undocumented database dumps

A new developer should be able to create the complete database by running the application or the approved migration command.

---

# 52. Testcontainers

Integration tests must execute Flyway against PostgreSQL Testcontainers.

Tests should validate:

- complete migration history
- schema initialization
- constraints
- indexes where relevant
- JPA mapping compatibility
- repository behavior
- migration ordering
- application startup

The test database must not be pre-created through Hibernate.

---

# 53. Migration Validation Test

The build should include a test or task that:

1. starts a clean PostgreSQL container
2. executes all migrations
3. starts the application context or persistence layer
4. validates the final schema
5. executes relevant integration tests

This detects migration defects before merge.

---

# 54. Upgrade Testing

Migration testing should include:

- clean database migration
- upgrade from the current production version
- upgrade from supported previous releases where applicable
- realistic data volume for significant changes
- rolling-deployment compatibility
- application startup after migration

Testing only a clean database is insufficient for high-risk changes.

---

# 55. CI Validation

CI must fail when:

- migrations have duplicate versions
- migrations are out of order according to policy
- Flyway validation fails
- a clean migration fails
- application startup fails after migration
- integration tests fail
- formatting or static SQL rules fail where enforced

Migration validation must occur before deployment.

---

# 56. Out-of-Order Migrations

Out-of-order execution should be disabled by default.

Allowing out-of-order migrations can hide branch-merging problems and produce environment differences.

Version collisions must be resolved before merge by assigning a new migration version.

---

# 57. Version Collision

When two branches create the same migration version:

- do not modify a migration already merged or applied
- rename only the migration that has not been shared or applied
- assign a new higher version
- re-run the full migration suite
- update the pull request

If both versions were applied in different environments, architecture and database operations must define a reconciliation plan.

---

# 58. Flyway Repair

`flyway repair` must not be used as a routine solution to checksum mismatches.

Repair may be used only under controlled circumstances such as:

- failed migration metadata cleanup after root-cause analysis
- approved correction of schema history
- environment recovery with verified schema state

Repair requires:

- backup
- documented reason
- schema comparison
- approval
- post-repair validation

Repair must never be used to legitimize editing an applied migration.

---

# 59. Failed Migration

When a migration fails:

1. stop deployment
2. determine whether the transaction rolled back
3. inspect schema state
4. inspect Flyway history
5. restore or clean partial artifacts if required
6. create a new corrective migration when history was already applied
7. re-run validation
8. document the incident

Do not blindly rerun a failed migration without verifying database state.

---

# 60. Correcting a Defective Migration

If a migration has been applied and a defect is discovered:

```text
Do not edit the original file.
```

Create a new migration.

Example:

```text
V12__add_order_reference_column.sql
```

contained an incorrect length.

Correction:

```text
V13__increase_order_reference_length.sql
```

This preserves history and guarantees consistent evolution.

---

# 61. Rollback Strategy

Flyway Community migrations are primarily forward-moving.

The default rollback strategy is:

- restore from backup for catastrophic failure
- deploy a new corrective migration
- roll back application code only when schema remains backward compatible
- use expand-and-contract for risky changes

Every significant migration must define its recovery strategy.

Rollback must not be assumed to mean automatically reversing SQL.

---

# 62. Down Migrations

Separate down-migration scripts are not required as the default platform standard.

Reasons include:

- many data changes are irreversible
- destructive reversal may cause data loss
- rollback scripts are often less tested
- production recovery frequently requires forward correction

A reversible operation may include a documented manual recovery script, but it must not replace backup and forward-migration planning.

---

# 63. Backup Requirements

Before high-risk production migrations, verify:

- recent successful backup
- restore capability
- point-in-time recovery availability where applicable
- estimated recovery time
- responsible operators
- rollback decision criteria

A backup that has never been restore-tested is not sufficient evidence of recoverability.

---

# 64. Migration Classification

Migrations should be classified by risk.

## Low Risk

Examples:

- create new table
- add nullable column
- add small reference data
- create index on a small table

## Medium Risk

Examples:

- add constraint to existing data
- create index on a large table
- backfill moderate data volume
- change defaults

## High Risk

Examples:

- large table rewrite
- column type conversion
- destructive change
- large backfill
- primary-key modification
- long blocking lock
- cross-version incompatibility

High-risk migrations require explicit operational approval.

---

# 65. Execution-Time Estimation

Significant migrations must be tested with representative data to estimate:

- total duration
- lock duration
- storage requirements
- CPU usage
- replication impact
- transaction-log growth
- application impact

Development-size databases do not provide reliable production estimates.

---

# 66. Lock Analysis

Migration review must identify likely PostgreSQL locks.

Questions include:

- does the operation require an exclusive table lock?
- how long may the lock be held?
- can active transactions block the migration?
- can the migration block application traffic?
- is a concurrent alternative available?
- is a maintenance window required?

Lock-sensitive migrations must be rehearsed.

---

# 67. Statement Timeout

Production migration execution may use controlled statement and lock timeouts.

Example concepts:

```sql
SET lock_timeout = '5s';
SET statement_timeout = '15min';
```

Timeout values must be selected per migration and environment.

A timeout failure should stop the migration safely rather than wait indefinitely.

---

# 68. Observability

Migration execution should expose:

- migration version
- migration description
- start time
- completion time
- duration
- success or failure
- environment
- application release
- responsible pipeline or job

Sensitive SQL data must not be logged unnecessarily.

---

# 69. Auditability

Migration history must be traceable to:

- source-control commit
- pull request
- application release
- deployment record
- work item
- reviewer approval

Database evolution is part of the software delivery audit trail.

---

# 70. Schema Drift

Schema drift occurs when a database differs from the expected migration state.

Possible causes include:

- manual DDL
- edited migrations
- failed deployments
- environment-specific scripts
- incomplete restoration
- incorrect baseline

Schema drift must be detected through:

- Flyway validation
- schema comparison
- integration tests
- controlled permissions
- deployment verification

Manual drift must be corrected through an approved migration or recovery procedure.

---

# 71. Hibernate Schema Management

Production configuration must not use:

```text
ddl-auto=update

ddl-auto=create

ddl-auto=create-drop
```

Recommended production behavior:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

or an equivalent controlled setting.

Hibernate may validate mappings against the Flyway-managed schema.

---

# 72. Application Compatibility

The application must not assume a new schema exists before migration completion.

Deployment orchestration must ensure:

- migrations complete successfully
- application instances start against the intended schema
- old instances remain compatible during overlap
- readiness fails when schema compatibility is not satisfied

---

# 73. Transactional Outbox Migrations

Outbox schema changes require special care because they affect message publication.

Review must include:

- dispatcher compatibility
- status values
- retry columns
- indexing
- polling queries
- payload format
- retention behavior
- old and new application versions

Outbox changes should remain backward compatible during rolling deployment.

---

# 74. Partitioning

Table partitioning may be introduced through Flyway when justified by:

- data volume
- retention
- query patterns
- operational maintenance
- archival requirements

Partitioning requires a dedicated design review because it affects:

- indexes
- constraints
- routing
- vacuum behavior
- migration complexity
- query plans

---

# 75. Archival and Retention

Schema migrations may create structures supporting archival or retention.

Actual data deletion should generally be executed through:

- controlled jobs
- retention services
- scheduled maintenance
- partition removal

Large data purges should not be embedded casually in startup migrations.

---

# 76. Multiple Services

Each service or bounded context must own its migration history.

One service must not apply migrations to another service's database without explicit architecture approval.

Migration ownership must match data ownership.

---

# 77. Shared Database Exceptions

Shared database schemas are discouraged.

Where legacy constraints require shared structures:

- ownership must be explicit
- migration responsibility must be assigned
- cross-service compatibility must be documented
- deployment order must be coordinated
- eventual separation should be planned

---

# 78. Migration Documentation

Complex migrations should contain documentation near the migration or in associated architecture records.

Document:

- purpose
- assumptions
- affected tables
- deployment strategy
- compatibility window
- operational steps
- validation queries
- recovery steps

---

# 79. Security Review

Migration review must consider:

- privileges
- sensitive data
- encryption
- row-level security where applicable
- audit requirements
- accidental exposure
- insecure defaults
- public grants
- function execution rights

New database objects must not unintentionally broaden access.

---

# 80. Personal and Sensitive Data

Data migrations involving personal or sensitive information must:

- minimize exposure
- avoid logging values
- follow retention rules
- preserve encryption requirements
- use controlled access
- avoid copying sensitive data unnecessarily

Test migrations must not require production-sensitive datasets.

---

# 81. Migration Performance Standards

Migrations must avoid:

- unbounded row-by-row processing
- unnecessary table rewrites
- redundant indexes
- full-table updates without planning
- long transactions without justification
- functions applied to indexed columns without analysis
- implicit type conversion on large datasets
- lock-heavy operations during peak traffic

Set-based SQL is preferred.

---

# 82. Migration Anti-Patterns

The following are prohibited:

- modifying an applied migration
- deleting an applied migration
- renaming an applied migration
- using Hibernate auto-update in production
- manual untracked DDL
- hardcoded credentials
- environment-specific schema behavior without approval
- unbounded data backfills during startup
- external HTTP calls from migration scripts
- using `flyway repair` to hide history modification
- merging duplicate migration versions
- destructive changes without recovery planning
- disabling validation to force deployment

---

# 83. Code Review Checklist

Reviewers must verify:

- Is the version unique?
- Is the filename valid?
- Has this migration already been applied anywhere?
- Is the SQL deterministic?
- Is it backward compatible?
- Does it cause a table rewrite?
- Does it acquire a blocking lock?
- Is the index strategy correct?
- Is existing data valid for new constraints?
- Is the data migration bounded?
- Is execution time acceptable?
- Is the migration transactional?
- Is a special non-transactional operation required?
- Is rollback or recovery documented?
- Are application changes sequenced correctly?
- Is the migration tested against PostgreSQL?
- Are permissions appropriate?
- Does it preserve bounded-context ownership?

---

# 84. Testing Checklist

Migration testing should verify:

- clean installation
- upgrade from previous version
- schema validation
- JPA validation
- repository integration
- constraints
- indexes
- data conversion
- null handling
- duplicate handling
- rollback or recovery procedure
- representative volume
- rolling compatibility
- application startup
- outbox operation where affected

---

# 85. Positive Consequences

The decision provides:

- deterministic schema evolution
- auditable history
- environment consistency
- automated deployment
- checksum validation
- PostgreSQL-native SQL control
- strong Spring Boot integration
- reproducible local setup
- reliable integration testing
- clear correction history
- reduced manual error
- easier disaster investigation
- safer CI/CD
- explicit database ownership
- compatibility with immutable infrastructure practices

---

# 86. Negative Consequences

The decision introduces:

- mandatory migration discipline
- additional review effort
- need for operational planning
- inability to edit historical migrations
- forward-only correction requirements
- possible startup delays
- complexity for large data migrations
- need for production-volume testing
- need for lock and execution analysis
- migration version coordination across branches

These costs are accepted because database consistency is critical to platform reliability.

---

# 87. Neutral Consequences

The decision also means:

- database changes become source-controlled software changes
- schema history becomes append-only
- mistakes remain visible and are corrected explicitly
- some deployments require staged application releases
- production migration credentials may differ from runtime credentials
- rollback often means forward correction
- large backfills may require dedicated operational jobs
- database design changes require the same rigor as application code

---

# 88. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Applied migration is edited | High | Medium | Enforce checksum validation and immutable-history policy |
| Migration causes long lock | High | Medium | Perform lock analysis and staging rehearsal |
| Large backfill blocks startup | High | Medium | Use dedicated resumable job |
| Duplicate migration versions | Medium | Medium | Validate in CI and resolve before merge |
| Environment schema drift | High | Low | Restrict manual DDL and run Flyway validation |
| Failed migration leaves partial state | High | Low | Use transactions and inspect schema before retry |
| Breaking change affects rolling deployment | High | Medium | Use expand-and-contract |
| Application user has excessive privileges | High | Medium | Separate migration and runtime users |
| Flyway repair is misused | High | Low | Require operational approval |
| Migration execution time is underestimated | Medium | Medium | Test with representative data |
| Constraint fails on existing data | High | Medium | Validate and backfill before adding constraint |
| Recovery plan is ineffective | High | Low | Test backups and corrective procedures |
| Repeatable migration is misused | Medium | Low | Restrict repeatables to deterministic derived objects |
| Manual hotfix creates drift | High | Low | Require post-incident versioned reconciliation |

---

# 89. Implementation Guidance

The following rules are mandatory:

1. Flyway is the exclusive schema migration mechanism.
2. Every database change requires a migration.
3. Applied migrations are immutable.
4. Corrections require a new migration version.
5. Migration files must follow the naming convention.
6. Version collisions must be resolved before merge.
7. Out-of-order execution is disabled by default.
8. Flyway validation must run in CI.
9. Integration tests must execute all migrations against PostgreSQL.
10. Hibernate schema auto-update is prohibited in production.
11. Breaking changes must use expand-and-contract.
12. Large backfills must not block startup without explicit approval.
13. High-risk migrations require operational review.
14. Migration and runtime credentials should be separated.
15. Manual DDL is prohibited outside approved emergency procedures.
16. Flyway repair requires explicit approval.
17. Rollback and recovery must be documented for significant changes.
18. SQL files must use UTF-8 and normalized line endings.
19. Database changes must preserve bounded-context ownership.
20. Schema history must remain auditable and append-only.

---

# 90. Validation

The decision will be validated through:

- Flyway checksum validation
- clean-database migration tests
- upgrade-path tests
- PostgreSQL Testcontainers
- application startup validation
- Hibernate mapping validation
- CI duplicate-version detection
- production migration logs
- schema drift detection
- code review
- operational review
- backup and recovery tests
- deployment smoke tests
- security review
- performance rehearsal for high-risk migrations

---

# 91. Success Criteria

The decision is successful when:

- every environment is created from the same migration history
- no applied migration is modified
- Flyway validation passes consistently
- new developers can initialize the database automatically
- CI detects migration defects before merge
- production deployments fail safely on migration error
- schema drift is rare and detectable
- corrections are represented by new migrations
- rolling deployments use backward-compatible schema evolution
- large changes have operational plans
- runtime users do not require DDL privileges
- migration history can be traced to releases and pull requests
- PostgreSQL integration tests reproduce production-relevant behavior

---

# 92. Alternatives Rejected

## 92.1 Liquibase

Rejected because Flyway's SQL-first model and lower configuration complexity better fit the platform's PostgreSQL-oriented migration strategy.

---

## 92.2 Manual Scripts

Rejected because they do not provide sufficient environment consistency, auditability or CI/CD automation.

---

## 92.3 Hibernate Automatic Schema Management

Rejected because ORM-generated schema changes are unsuitable for controlled production evolution and data migration.

---

## 92.4 Custom Migration Framework

Rejected because building and maintaining migration infrastructure provides no strategic value compared with Flyway.

---

# 93. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-002: Adopt Domain-Driven Design
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-007: Adopt Transactional Outbox
- ADR-013: Use Testcontainers for Integration Testing
- ADR-015: Deploy Workloads on Kubernetes
- ADR-017: Use Optimistic Locking for Aggregate Concurrency

---

# 94. References

- Flyway Documentation
- Flyway Versioned Migrations Documentation
- Flyway Repeatable Migrations Documentation
- Flyway Validation Documentation
- PostgreSQL Documentation
- PostgreSQL Locking Documentation
- Spring Boot Flyway Integration Documentation
- Testcontainers PostgreSQL Documentation
- Enterprise Order Platform Flyway Migration Strategy
- Enterprise Order Platform Persistence Guidelines
- ADR-005: Use PostgreSQL as the Primary Database

---

# 95. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-23 | Enterprise Order Platform Architecture Team | Approved | Initial database migration baseline |

---

# 96. Decision Summary

The Enterprise Order Platform adopts Flyway as its exclusive database migration mechanism.

All database changes must be:

```text
Versioned

Source controlled

Reviewed

Tested

Auditable

Applied in deterministic order
```

The central rule is:

> Never modify a migration that already exists or may have been applied.

Any correction must be implemented through a new migration with a new version.

Flyway validation, PostgreSQL integration tests and controlled deployment procedures will protect database consistency across local, CI, test, staging and production environments.

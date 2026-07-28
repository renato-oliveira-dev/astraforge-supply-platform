# ADR-046: Adopt Data Governance, Privacy, Retention and Lifecycle Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-046 |
| Title | Adopt Data Governance, Privacy, Retention and Lifecycle Standards |
| Status | Superseded |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Data Governance, Privacy, LGPD, PII, Retention, Data Lifecycle |
| Related Work Items | LGPD, Data Classification, Retention, Masking, Data Quality, Data Lineage |
| Supersedes | ADR-029 |
| Superseded By | ADR-071 |

---

# 1. Context

The Enterprise Order Platform processes data across multiple services:

```text
                   BUSINESS DATA
                        |
        +---------------+---------------+
        |               |               |
        v               v               v
    CUSTOMERS          CART           ORDERS
        |               |               |
        +---------------+---------------+
                        |
        +---------------+---------------+
        |               |               |
        v               v               v
    POSTGRESQL         KAFKA           REDIS
        |               |               |
        +---------------+---------------+
                        |
                        v
                 OBSERVABILITY
```

Data may consequently exist in:

- primary databases
- caches
- integration events
- API payloads
- logs
- traces
- audit records
- backups
- object storage
- reports
- non-production environments

Distributed architecture therefore creates multiple copies and representations of the same business information.

Without explicit governance, this can create:

- excessive retention
- inconsistent deletion
- uncontrolled PII propagation
- sensitive data in logs
- stale replicated data
- unclear source of truth
- non-production privacy exposure
- unknown lineage
- inconsistent masking
- regulatory risk

---

# 2. Problem Statement

The platform requires standards defining:

- data ownership
- system of record
- data classification
- personal data
- sensitive personal data
- LGPD
- PII
- data minimization
- purpose limitation
- access control
- encryption
- masking
- tokenization
- logging restrictions
- retention
- deletion
- anonymization
- pseudonymization
- non-production data
- backups
- Kafka events
- Redis
- replicated data
- data lineage
- data quality
- auditability
- lifecycle management

---

# 3. Decision Drivers

Primary drivers are:

1. regulatory compliance
2. privacy protection
3. data confidentiality
4. clear ownership
5. controlled retention
6. secure distributed architecture
7. auditable data handling
8. data quality
9. reduced data exposure
10. controlled replication
11. operational simplicity
12. lifecycle consistency

---

# 4. Decision

The platform adopts explicit data governance throughout the complete data lifecycle.

The canonical lifecycle is:

```text
COLLECT
   |
   v
VALIDATE
   |
   v
PROCESS
   |
   v
STORE
   |
   v
SHARE / REPLICATE
   |
   v
RETAIN
   |
   v
ARCHIVE
   |
   v
DELETE / ANONYMIZE
```

Governance applies at every stage.

---

# 5. Fundamental Principle

The primary rule is:

```text
Collect only what is required,
use it only for an authorized purpose,
protect it appropriately,
retain it only as long as necessary,
and dispose of it safely.
```

---

# 6. Data Ownership

Every material business-data domain requires an identifiable owner.

---

# 7. Domain Ownership

Data ownership should align with domain ownership defined by ADR-043.

Example:

```text
CUSTOMER DOMAIN
      |
      v
CUSTOMERS SERVICE
      |
      v
AUTHORITATIVE CUSTOMER DATA
```

---

# 8. System of Record

Critical business concepts require an identifiable authoritative system.

---

# 9. Source of Truth

For each important entity, the architecture should answer:

```text
Which system has authority
over this business fact?
```

---

# 10. Local Copies

Other services may maintain:

- snapshots
- projections
- caches
- read models
- integration copies

without becoming the authoritative source.

---

# 11. Distributed Ownership

Example:

```text
             CUSTOMERS SERVICE
                    |
                    v
             CUSTOMER MASTER
                    |
          +---------+---------+
          |                   |
          v                   v
        EVENT                API
          |                   |
          v                   v
     ORDERS COPY          CART COPY
```

The copies do not redefine master ownership.

---

# 12. Data Classification

Data must be classified according to sensitivity.

---

# 13. Suggested Classification

The enterprise classification taxonomy should be used where one exists.

Otherwise, a baseline may use:

```text
PUBLIC

INTERNAL

CONFIDENTIAL

RESTRICTED
```

---

# 14. Public

Information explicitly approved for public disclosure.

---

# 15. Internal

Information intended for internal organizational use.

---

# 16. Confidential

Information requiring controlled access.

Examples may include:

- customer information
- commercial information
- internal business records

---

# 17. Restricted

Information requiring the strongest controls.

Examples may include:

- credentials
- authentication secrets
- highly sensitive personal information
- cryptographic keys

---

# 18. Classification Propagation

Classification follows the data.

Moving data from:

```text
PostgreSQL
```

to:

```text
Kafka
```

does not reduce its sensitivity.

---

# 19. Derived Data

Derived information may remain sensitive.

---

# 20. Aggregation

Aggregation does not automatically make data anonymous.

---

# 21. Personal Data

Personal data must be identified according to applicable privacy legislation, including LGPD requirements.

---

# 22. PII

Personally identifiable information may include, depending on context:

```text
Name

Email

Telephone

Document Identifier

Address

Account Identifier

Device/User Identifiers
```

---

# 23. Sensitive Personal Data

Sensitive personal data requires stronger controls according to applicable legislation and enterprise policy.

---

# 24. Secrets Are Not PII

Credentials and secrets require strong protection even when they are not personal data.

---

# 25. Data Inventory

Critical services should maintain awareness of the sensitive data they process.

---

# 26. Data Inventory Questions

For each important dataset:

```text
What is it?

Why do we collect it?

Where is it stored?

Who owns it?

Who can access it?

Where is it replicated?

How long is it retained?

How is it deleted?
```

---

# 27. Data Minimization

Services must collect only data reasonably necessary for their business purpose.

---

# 28. Convenience Is Not Necessity

Data should not be collected merely because:

```text
"We may need it someday."
```

---

# 29. API Minimization

API contracts should not expose fields consumers do not require.

---

# 30. Event Minimization

Integration events should not become dumps of entire domain entities.

---

# 31. Database Minimization

Persistent storage should not accumulate obsolete attributes indefinitely.

---

# 32. Logging Minimization

Logs should contain the minimum data required for diagnosis.

---

# 33. Purpose Limitation

Data collected for one purpose must not silently be reused for unrelated purposes.

---

# 34. Purpose Documentation

Material personal-data processing should have an identifiable business purpose.

---

# 35. Privacy by Design

Privacy must be considered during architecture and implementation, not added only after release.

---

# 36. Privacy by Default

Default behavior should minimize unnecessary exposure.

---

# 37. Access Control

Data access must follow least privilege.

---

# 38. Authorization

Application authorization must restrict data access according to business rules.

---

# 39. Database Access

Direct database access should be restricted to approved identities and operational purposes.

---

# 40. Production Access

Human access to production data must be controlled and auditable.

---

# 41. Shared Credentials

Shared human credentials for production data access are prohibited.

---

# 42. Service Identity

Service-to-service access should use identifiable workload/service identities.

---

# 43. Encryption in Transit

Sensitive data must be protected in transit using approved transport security.

---

# 44. Encryption at Rest

Sensitive persistent data must use approved encryption-at-rest mechanisms.

---

# 45. Encryption Does Not Replace Authorization

Encrypted storage does not permit unrestricted application access.

---

# 46. Key Management

Encryption keys require controlled lifecycle management.

---

# 47. Key Separation

Application data and encryption-key management should remain appropriately separated.

---

# 48. Secrets Management

Secrets must follow ADR-037 and platform secret-management standards.

---

# 49. Masking

Sensitive data displayed outside its required business context should be masked where appropriate.

---

# 50. Masking Example

Instead of:

```text
123.456.789-00
```

a diagnostic/admin interface may display:

```text
***.***.***-00
```

when full value is unnecessary.

---

# 51. Masking Is Contextual

Masking requirements depend on:

- user role
- business need
- data sensitivity
- interface

---

# 52. Tokenization

Tokenization may be used when systems require a stable reference without the original sensitive value.

---

# 53. Pseudonymization

Pseudonymization reduces direct identification but does not necessarily make data anonymous.

---

# 54. Anonymization

Anonymized data must no longer reasonably permit identification according to applicable standards and context.

---

# 55. Anonymization vs Masking

These are not equivalent:

```text
MASKED
   !=
PSEUDONYMIZED
   !=
ANONYMIZED
```

---

# 56. Logs

Application logs must not become uncontrolled repositories of sensitive data.

---

# 57. Prohibited Logging

Do not log:

```text
Passwords

Access Tokens

Refresh Tokens

Authorization Headers

Private Keys

Secret Values
```

---

# 58. Personal Data in Logs

Personal data should not be logged unless genuinely necessary and explicitly justified.

---

# 59. Request Logging

Do not blindly log complete request bodies.

---

# 60. Response Logging

Do not blindly log complete response bodies.

---

# 61. Error Logging

Exception handling must not expose sensitive request values merely to provide diagnostic context.

---

# 62. Sanitization

Diagnostic messages crossing trust boundaries require appropriate sanitization.

---

# 63. Sanitization Must Not Corrupt Business Data

Security sanitization for logs/errors must not silently modify legitimate persisted business values.

---

# 64. Example

A valid business value such as:

```text
M&M
```

must remain:

```text
M&M
```

in business persistence and API contracts unless the contract explicitly requires another representation.

---

# 65. Security Boundary

Escaping required for a presentation context belongs at the appropriate output/rendering boundary.

---

# 66. Structured Logging

Structured logging should favor identifiers useful for correlation rather than full business objects.

---

# 67. Correlation IDs

Correlation identifiers may be logged where they do not themselves expose sensitive business information.

---

# 68. Business Identifiers

Business identifiers should be logged only when justified by diagnostic requirements and classification.

---

# 69. Metrics

Sensitive values must not be metric labels.

---

# 70. High Cardinality

Identifiers such as:

```text
customerId

orderId

email
```

must not normally become metric dimensions.

---

# 71. Tracing

Trace attributes must follow the same privacy restrictions as logs.

---

# 72. Audit Records

Audit data may legitimately contain information that ordinary application logs should not.

---

# 73. Audit Purpose

Audit records exist to answer questions such as:

```text
Who performed the action?

What action occurred?

When?

Against which business object?

What was the outcome?
```

---

# 74. Audit Data Protection

Audit data itself requires:

- access control
- integrity
- retention
- confidentiality

---

# 75. Audit Immutability

Where regulatory/business requirements demand it, audit records should resist unauthorized modification.

---

# 76. Retention Policy

Every material persistent-data category should have a defined retention policy.

---

# 77. Retention Drivers

Retention may be driven by:

- business need
- contractual requirements
- legal requirements
- regulatory requirements
- audit requirements

---

# 78. Retention Is Not Universal

Different data categories may require different retention periods.

---

# 79. Retention Metadata

A retention policy should define:

```text
Data Category

Owner

Retention Period

Retention Reason

Deletion/Archive Mechanism

Exceptions
```

---

# 80. Indefinite Retention

"Keep forever" is not an acceptable default.

---

# 81. Active Data

Data required for active business operations remains in active storage according to operational needs.

---

# 82. Archive

Data no longer required for frequent operational access may move to an archive when retention is still required.

---

# 83. Archive Security

Archived data retains its classification and security requirements.

---

# 84. Deletion

When data no longer has a legitimate retention requirement, it should be securely deleted or irreversibly anonymized as appropriate.

---

# 85. Logical Delete

A database flag such as:

```text
deleted = true
```

does not necessarily satisfy actual data-erasure requirements.

---

# 86. Soft Delete

Soft deletion may support business workflows but requires eventual lifecycle treatment.

---

# 87. Physical Deletion

Physical deletion removes the underlying record from active persistence.

---

# 88. Anonymization Alternative

Where business/statistical records must remain, irreversible anonymization may replace deletion when legally and technically appropriate.

---

# 89. Referential Integrity

Deletion/anonymization must account for relational dependencies.

---

# 90. Distributed Deletion

In microservices, deletion may need propagation.

---

# 91. Example

```text
CUSTOMERS
    |
    v
DELETE / ANONYMIZE
    |
    +------> ORDERS SNAPSHOT
    |
    +------> CART SNAPSHOT
    |
    +------> SEARCH INDEX
    |
    +------> CACHE
```

---

# 92. Distributed Data Inventory

Deletion cannot be reliable when nobody knows where copies exist.

---

# 93. Deletion Event

A domain event may be used to propagate deletion/anonymization when appropriate.

---

# 94. Event Reliability

Privacy-related deletion events require reliable delivery and idempotent consumption.

---

# 95. Deletion Idempotency

Repeated deletion/anonymization requests must be safely handled.

---

# 96. Partial Deletion Failure

A failure in one downstream service must be observable and retryable.

---

# 97. Deletion Tracking

Material distributed deletion workflows should expose completion status.

---

# 98. Right to Deletion

Where applicable under LGPD or other requirements, requests for deletion must follow approved legal/business rules.

---

# 99. Deletion Is Not Always Permitted

A privacy request does not automatically override mandatory legal retention.

---

# 100. Legal Hold

Data subject to legal hold must not be deleted by ordinary retention automation.

---

# 101. Retention Conflict

When privacy deletion and legal retention conflict, approved legal/compliance policy determines the required treatment.

---

# 102. Anonymization Under Retention

Where permitted, anonymization may satisfy privacy objectives while preserving non-identifying records required for legitimate purposes.

---

# 103. Backup Retention

Backups require explicit retention policies.

---

# 104. Immediate Backup Mutation

Historical backups should generally not be modified record-by-record solely to remove an individual active record unless enterprise/legal policy explicitly requires such capability.

---

# 105. Backup Expiration

Privacy deletion can rely on controlled backup expiration when legally appropriate.

---

# 106. Restore After Deletion

A restore from an older backup can reintroduce data previously deleted from active systems.

---

# 107. Post-Restore Reconciliation

Recovery procedures must therefore reapply applicable:

```text
Deletion

Anonymization

Legal Hold

Retention
```

state after restore.

---

# 108. DR Integration

Data lifecycle governance must integrate with ADR-045.

---

# 109. Kafka Data

Kafka events inherit the classification of the data they contain.

---

# 110. PII in Events

Avoid including personal data in events unless consumers require it.

---

# 111. Event Reference

Prefer identifiers/references instead of complete sensitive entities when architecture permits.

---

# 112. Kafka Retention

Topic retention must reflect:

- replay requirements
- privacy
- storage
- business needs

---

# 113. Kafka Deletion Complexity

Immutable event logs complicate individual-record deletion.

This must be considered before placing unnecessary PII into events.

---

# 114. Event Design

Data minimization at event-design time is preferable to attempting selective deletion from distributed logs later.

---

# 115. Event Schema

Schema documentation should identify sensitive fields where tooling permits.

---

# 116. Schema Evolution

Removing a sensitive field from new events does not remove it from historical retained events.

---

# 117. Historical Retention

Historical event retention must therefore remain part of privacy analysis.

---

# 118. Redis Data

Redis inherits the classification of cached data.

---

# 119. Cache Minimization

Do not cache sensitive fields that are not required for the cached use case.

---

# 120. TTL

Sensitive cache data should have bounded TTL where semantics permit.

---

# 121. Cache Eviction

Deletion/anonymization workflows should invalidate relevant cache entries.

---

# 122. Cache Is Not Privacy Boundary

Short TTL alone does not replace explicit deletion when immediate removal is required.

---

# 123. Search Indexes

Search indexes are additional data copies.

---

# 124. Index Lifecycle

Deletion/anonymization must propagate to search indexes where applicable.

---

# 125. Object Storage

Files and exported reports may contain sensitive information.

---

# 126. Export Governance

Generated exports require:

- access control
- owner
- retention
- expiration where appropriate

---

# 127. Temporary Export

Temporary exports should have automatic expiration.

---

# 128. Public Bucket

Sensitive data must never be placed in publicly accessible object storage.

---

# 129. Pre-Signed URLs

Temporary access URLs require appropriately limited lifetime and scope.

---

# 130. Non-Production Environments

Production personal data must not be copied casually into DEV or TEST.

---

# 131. Preferred Non-Production Data

Prefer:

```text
Synthetic Data

Generated Test Data

Anonymized Data
```

---

# 132. Production Clone

Production database cloning into non-production requires explicit controls and justification.

---

# 133. Masked Clone

When representative production-like data is necessary, approved anonymization/masking must occur before broad non-production access.

---

# 134. Referential Consistency

Anonymized test data should preserve required relational consistency.

---

# 135. Reversible Masking

Reversible masking must not be described as anonymization.

---

# 136. Test Fixtures

Automated test fixtures must not contain copied real customer personal data.

---

# 137. Source Code

Sensitive production data must not be committed to Git repositories.

---

# 138. CI Logs

CI/CD pipelines must not expose secrets or sensitive production data in logs.

---

# 139. Test Reports

Test reports must avoid embedding sensitive production values.

---

# 140. Developer Workstations

Production-data downloads to developer machines should be prohibited or strictly controlled according to enterprise policy.

---

# 141. Data Lineage

Critical data should have understandable lineage.

---

# 142. Lineage Questions

The architecture should be able to answer:

```text
Where did this data originate?

Which service transformed it?

Where was it sent?

Which systems store copies?
```

---

# 143. Lineage Importance

Lineage supports:

- privacy
- incident investigation
- data quality
- deletion
- auditing

---

# 144. Technical Lineage

Technical lineage tracks system-level movement.

Example:

```text
CUSTOMERS DB
     |
     v
CustomerUpdated
     |
     v
KAFKA
     |
     v
ORDERS SNAPSHOT
```

---

# 145. Business Lineage

Business lineage describes how business meaning changes through transformations.

---

# 146. Data Provenance

Critical derived data should preserve enough provenance to understand its origin where required.

---

# 147. Data Quality

Data governance includes correctness, not only privacy.

---

# 148. Quality Dimensions

Relevant dimensions include:

```text
Accuracy

Completeness

Consistency

Timeliness

Uniqueness

Validity
```

---

# 149. Validation Ownership

The authoritative domain should define business validation rules.

---

# 150. Consumer Validation

Consumers may validate integration assumptions but must not silently redefine authoritative business semantics.

---

# 151. Invalid Data

Invalid data should fail as close as practical to its authoritative write boundary.

---

# 152. Downstream Sanitization

Consumers should not routinely "repair" invalid authoritative data silently.

---

# 153. Example

Preferred:

```text
PRODUCER
   |
   v
VALIDATE
   |
   v
CORRECT DATA
```

instead of:

```text
BAD DATA
   |
   v
CONSUMER 1 -> PATCH
CONSUMER 2 -> PATCH DIFFERENTLY
CONSUMER 3 -> PATCH AGAIN
```

---

# 154. Contract Validation

API and event contracts should enforce structural validation.

---

# 155. Semantic Validation

Domain services remain responsible for semantic business validation.

---

# 156. Data Quality Monitoring

Critical data-quality rules may be monitored.

---

# 157. Quality Alert

A sudden increase in invalid/incomplete data should be treated as an operational signal.

---

# 158. Duplicate Data

Duplicate business records should be prevented where uniqueness is a domain requirement.

---

# 159. Database Constraints

Database constraints should enforce invariants appropriately enforceable at persistence level.

---

# 160. Application Validation

Application validation complements rather than replaces appropriate database constraints.

---

# 161. Data Consistency

Distributed systems may be eventually consistent.

---

# 162. Eventual Consistency

Eventual consistency must be intentional and understood.

---

# 163. Staleness

Consumers should understand acceptable staleness of replicated data.

---

# 164. Snapshot Timestamp

Where useful, snapshots should preserve:

```text
sourceUpdatedAt
```

or equivalent provenance/version information.

---

# 165. Versioning

Replicated data may include a source version when needed to prevent stale updates.

---

# 166. Out-of-Order Events

Consumers must consider out-of-order event delivery.

---

# 167. Stale Update Protection

Where required:

```text
Incoming Version < Current Version
           |
           v
        REJECT
```

or equivalent logic should protect the local projection.

---

# 168. Event Idempotency

Repeated events must not corrupt replicated state.

---

# 169. Reconciliation

Critical distributed copies may require periodic reconciliation with the authoritative source.

---

# 170. Reconciliation Purpose

Reconciliation detects:

- missed events
- stale snapshots
- inconsistent state
- partial processing failures

---

# 171. Reconciliation Must Not Hide Defects

Frequent reconciliation should not become a substitute for reliable integration.

---

# 172. Data Contract Ownership

The producer owns the meaning of data it publishes.

---

# 173. Consumer Contract

Consumers own their dependency on documented fields and semantics.

---

# 174. Breaking Data Change

Changes in data semantics require compatibility analysis even if the field type remains unchanged.

---

# 175. Semantic Breaking Change

Example:

```text
status = "ACTIVE"
```

changing meaning without changing schema can still break consumers.

---

# 176. Data Documentation

Important data elements should have clear business definitions.

---

# 177. Naming

Names should reflect business semantics rather than implementation accidents.

---

# 178. Ambiguous Field

Fields such as:

```text
value1

flag2

statusX
```

should be avoided in modernized contracts.

---

# 179. Legacy Data

Legacy schemas may contain ambiguous structures that cannot immediately be changed.

---

# 180. Legacy Mapping

Modern services should isolate legacy representation behind explicit mapping/anti-corruption layers where practical.

---

# 181. Migration

Data migration requires governance.

---

# 182. Migration Plan

Material migrations should define:

```text
Source

Target

Mapping

Validation

Reconciliation

Rollback

Retention of Legacy Data
```

---

# 183. Migration Validation

Record counts alone are insufficient for critical migrations.

---

# 184. Semantic Validation

Migration validation should include representative business invariants.

---

# 185. Dual Write

Dual-write migration strategies require explicit consistency controls.

---

# 186. Dual Write Risk

Writing independently to two systems can create divergence.

---

# 187. CDC

Change Data Capture may be used when justified, but it does not eliminate ownership and schema-governance requirements.

---

# 188. Legacy Decommission

After migration, legacy data must follow explicit retention/decommission policy.

---

# 189. Data Export

Bulk data exports require explicit authorization.

---

# 190. Export Audit

Sensitive bulk exports should be auditable where required.

---

# 191. Export Scope

Exports should contain only required fields.

---

# 192. Data Sharing

Sharing data with another team/system requires:

- legitimate purpose
- appropriate contract
- security controls
- classification awareness

---

# 193. Third Parties

Third-party data sharing requires applicable enterprise/legal/vendor controls.

---

# 194. Data Residency

Where legal/business requirements impose data-location constraints, architecture must respect approved residency requirements.

---

# 195. Cross-Region Replication

Regional replication under ADR-045 must also satisfy privacy/data-residency requirements.

---

# 196. Data Breach

Suspected unauthorized exposure of sensitive data requires security incident procedures.

---

# 197. Breach Evidence

Logs and audit records should support determining:

```text
What data?

Which users?

Which systems?

Which period?

Which access path?
```

where technically possible and appropriate.

---

# 198. Privacy Incident

Privacy incidents require coordination with security, privacy/legal and business stakeholders according to enterprise procedure.

---

# 199. Retention Automation

Retention should be automated where practical.

---

# 200. Scheduled Lifecycle Job

Typical lifecycle:

```text
IDENTIFY EXPIRED DATA
        |
        v
CHECK EXCEPTIONS
        |
        v
ARCHIVE / DELETE / ANONYMIZE
        |
        v
AUDIT RESULT
```

---

# 201. Batch Safety

Large deletion operations require bounded processing to avoid production impact.

---

# 202. Database Impact

Retention jobs should account for:

- locks
- transaction size
- WAL generation
- replication
- vacuum
- indexes

---

# 203. Chunked Deletion

Large cleanup may require controlled batches.

---

# 204. Deletion Transaction

Avoid unnecessarily massive deletion transactions that threaten database availability.

---

# 205. Observability

Lifecycle jobs require metrics and failure reporting.

---

# 206. Lifecycle Failure

A failed retention job must not silently leave expired sensitive data indefinitely.

---

# 207. Retry

Lifecycle processing should support controlled retry.

---

# 208. Audit Trail

Material deletion/anonymization workflows should preserve evidence that the action occurred without retaining the sensitive value unnecessarily.

---

# 209. Privacy Fitness Functions

Governance should automate privacy controls where practical.

---

# 210. Potential Fitness Functions

Examples:

```text
Secret scanning

Sensitive logging rules

Mandatory data classification

Retention policy presence

Kafka topic retention checks

Non-production production-data checks

Export expiration

Database encryption validation

Storage public-access checks
```

---

# 211. Static Analysis

SAST/Sonar/security tooling should detect applicable insecure data-handling patterns.

---

# 212. Dynamic Validation

Some privacy controls require integration/runtime tests.

---

# 213. Contract Review

New API/event fields containing sensitive data should trigger privacy/security review where appropriate.

---

# 214. Schema Review

Database schema changes introducing sensitive fields require classification and retention analysis.

---

# 215. Flyway

New sensitive persistence fields introduced through Flyway require normal migration governance.

---

# 216. Applied Migration Immutability

Privacy changes do not justify modifying already applied Flyway migrations.

---

# 217. Corrective Migration

Schema corrections require a new migration version.

---

# 218. Data Governance Gate

A production capability processing material data is not considered fully governed until:

```text
[ ] Data owner identified

[ ] Authoritative source identified

[ ] Data classification defined

[ ] Personal data identified

[ ] Sensitive personal data identified

[ ] Business purpose documented where required

[ ] Data minimization reviewed

[ ] API exposure reviewed

[ ] Event exposure reviewed

[ ] Database storage reviewed

[ ] Encryption in transit configured

[ ] Encryption at rest configured

[ ] Access control defined

[ ] Production human access controlled

[ ] Logging reviewed for sensitive data

[ ] Metrics reviewed for sensitive labels

[ ] Trace attributes reviewed

[ ] Audit requirements identified

[ ] Retention policy defined

[ ] Archive policy defined where applicable

[ ] Deletion/anonymization mechanism defined

[ ] Legal-hold behavior understood

[ ] Distributed copies inventoried

[ ] Cache invalidation understood

[ ] Kafka retention reviewed

[ ] Backup retention reviewed

[ ] Post-restore privacy reconciliation defined

[ ] Non-production data strategy defined

[ ] Data lineage understood

[ ] Data-quality ownership defined

[ ] Reconciliation defined where necessary

[ ] Export controls defined

[ ] Third-party sharing reviewed

[ ] Data-residency requirements reviewed

[ ] Lifecycle jobs observable

[ ] Privacy/security incident path understood
```

---

# 219. Anti-Patterns

The following are prohibited or strongly discouraged:

- collecting data without a defined purpose
- keeping data indefinitely "just in case"
- exposing complete entities through APIs unnecessarily
- publishing entire domain objects into Kafka by default
- storing secrets in ordinary business tables without approved protection
- logging passwords or tokens
- logging complete request/response payloads indiscriminately
- placing customer identifiers into metric labels
- treating masking as anonymization
- treating pseudonymization as guaranteed anonymization
- copying production databases casually into DEV
- real customer PII in automated test fixtures
- production data committed to source control
- sensitive exports without expiration
- public object storage containing sensitive data
- soft delete treated automatically as legal erasure
- deletion implemented only in the authoritative database while replicas remain indefinitely
- Kafka topics retaining unnecessary PII indefinitely
- Redis caches without TTL/lifecycle where applicable
- data copies without identifiable source of truth
- consumers silently repairing invalid producer data
- data migrations validated only by row count
- dual writes without divergence controls
- restore procedures that reintroduce deleted data without reconciliation
- applied Flyway migrations modified to implement privacy changes
- privacy treated solely as a legal-team responsibility

---

# 220. Positive Consequences

The decision provides:

- clearer data ownership
- stronger LGPD alignment
- reduced PII exposure
- controlled data retention
- safer logging
- better non-production protection
- controlled Kafka data propagation
- stronger deletion workflows
- improved lineage
- improved data quality
- safer backup recovery
- auditable lifecycle management

---

# 221. Negative Consequences

The decision introduces:

- data inventories
- classification work
- retention automation
- deletion propagation
- lineage maintenance
- privacy reviews
- non-production data preparation
- additional testing

These costs are accepted because uncontrolled data proliferation creates significant security, regulatory and operational risk.

---

# 222. Neutral Consequences

The decision also means:

- not every personal-data request results in immediate deletion
- legal retention may override ordinary deletion
- backups may retain deleted data until controlled expiration
- pseudonymized data may still require privacy controls
- distributed systems may legitimately duplicate data
- replicated data does not become authoritative automatically

---

# 223. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| PII leakage | Critical | Medium | Minimization + access controls |
| Sensitive logs | High | Medium | Logging standards |
| Excessive retention | High | High | Retention automation |
| Incomplete deletion | High | Medium | Distributed lifecycle |
| Production data in DEV | Critical | Medium | Synthetic/anonymized data |
| Stale replicas | High | Medium | Versioning/reconciliation |
| Unknown lineage | High | Medium | Data inventory |
| Backup reintroduces deleted data | High | Medium | Post-restore reconciliation |
| Kafka retains PII | High | Medium | Event minimization |
| Cache retains deleted data | Medium | Medium | Invalidation/TTL |
| Data-quality drift | High | Medium | Quality monitoring |
| Legal-hold violation | Critical | Low | Exception controls |
| Unauthorized export | Critical | Low/Medium | Access + audit |

---

# 224. Implementation Guidance

The following rules are mandatory:

1. Material business data requires identifiable ownership.
2. Critical business concepts require an authoritative source.
3. Replicated copies must not silently become competing masters.
4. Data classification must follow enterprise policy.
5. Personal and sensitive data must be identified.
6. Services must practice data minimization.
7. API and event contracts must avoid unnecessary sensitive fields.
8. Data must only be processed for authorized purposes.
9. Sensitive data requires appropriate encryption in transit and at rest.
10. Access must follow least privilege.
11. Human production-data access must be controlled and auditable.
12. Secrets, tokens and credentials must never be written to application logs.
13. Personal data should not be logged without explicit need.
14. Security sanitization must not corrupt legitimate business data.
15. Sensitive identifiers must not become ordinary metric dimensions.
16. Material persistent data requires an explicit retention policy.
17. Indefinite retention must not be the default.
18. Soft deletion must not automatically be considered final erasure.
19. Distributed deletion/anonymization must account for all relevant copies.
20. Privacy deletion workflows must respect legal-retention obligations.
21. Backups require privacy-aware retention and post-restore reconciliation.
22. Kafka events inherit the classification of their payload.
23. PII in events must be minimized.
24. Redis caches containing sensitive data require lifecycle controls.
25. Production data must not be casually copied to non-production environments.
26. Automated tests must not contain real customer PII.
27. Critical data requires understandable lineage.
28. Authoritative domains own business data-quality semantics.
29. Distributed copies must handle idempotency and stale/out-of-order updates where applicable.
30. Critical replicated data should support reconciliation where necessary.
31. Bulk exports require authorization and lifecycle controls.
32. Cross-region data movement must respect data-residency/privacy requirements.
33. Large retention operations must protect database availability.
34. Lifecycle automation must be observable and auditable.
35. Schema changes introducing sensitive fields require privacy/retention review.
36. Applied Flyway migrations remain immutable.
37. Privacy controls should become automated fitness functions where practical.

---

# 225. Validation

This ADR will be validated through:

- architecture reviews
- privacy reviews
- data classification
- data inventories
- SAST
- secret scanning
- logging reviews
- database schema reviews
- Kafka schema reviews
- retention reports
- deletion/anonymization tests
- non-production data reviews
- access audits
- lifecycle-job monitoring
- data-quality monitoring
- reconciliation reports
- DR exercises
- incident reviews

---

# 226. Success Criteria

The decision is successful when:

- critical data has identifiable owners
- authoritative sources are clear
- unnecessary PII propagation decreases
- application logs do not expose sensitive values
- production data is not casually replicated into DEV
- retention is intentional and automated
- distributed deletion is traceable
- Kafka/Redis copies follow lifecycle requirements
- restored environments respect previous privacy actions
- lineage of critical data is understandable
- stale distributed copies are detectable
- data-quality failures become observable
- privacy requirements are incorporated during design rather than after release

---

# 227. Alternatives Rejected

## 227.1 Privacy Only at the Database Layer

Rejected because data also exists in APIs, Kafka, Redis, logs, traces, backups and exports.

---

## 227.2 Keep Everything Forever

Rejected because indefinite retention increases privacy, security and financial risk.

---

## 227.3 Delete Only From the Master Database

Rejected because distributed copies may continue exposing the data.

---

## 227.4 Mask Everything

Rejected because masking is context-specific and does not replace access control or anonymization.

---

## 227.5 Copy Production Data to DEV for Realism

Rejected as the default because synthetic/anonymized data provides safer testing.

---

## 227.6 Let Every Consumer Correct Bad Data

Rejected because it creates inconsistent business semantics.

---

## 227.7 Encrypt Everything and Ignore Other Controls

Rejected because encryption does not replace minimization, authorization, retention or lifecycle governance.

---

# 228. Related Decisions

This ADR is related to:

- ADR-002: Adopt Domain-Driven Design
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Migrations
- ADR-009: Use Apache Kafka for Integration Events
- ADR-010: Use Redis for Distributed Caching
- ADR-014: Adopt Distributed Observability
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-036: Adopt API Design, REST Contract and Compatibility Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-038: Adopt Dependency and Software Supply Chain Security Standards
- ADR-040: Adopt Production Reliability, Incident Response and Operational Readiness Standards
- ADR-042: Adopt Architecture Fitness Functions and Automated Governance Standards
- ADR-043: Adopt Service Ownership, Platform Boundaries and Team Topology Standards
- ADR-045: Adopt Business Continuity, Disaster Recovery and Regional Resilience Standards
- ADR-047: Adopt Legacy Modernization, Strangler Migration and Technical Evolution Standards

---

# 229. References

- Lei Geral de Proteção de Dados Pessoais — LGPD
- ANPD guidance
- ISO/IEC 27001
- ISO/IEC 27701
- NIST Privacy Framework
- OWASP
- Domain-Driven Design
- PostgreSQL Documentation
- Apache Kafka Documentation
- Redis Documentation
- AWS Well-Architected Framework

---

# 230. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial data governance and privacy baseline |

---

# 231. Decision Summary

The definitive data-governance model is:

```text
                    BUSINESS PURPOSE
                           |
                           v
                       COLLECT
                           |
                           v
                       MINIMIZE
                           |
                           v
                       CLASSIFY
                           |
                           v
                       PROTECT
                           |
                           v
                       PROCESS
                           |
                           v
                       REPLICATE
                           |
                           v
                        RETAIN
                           |
                           v
                 DELETE / ANONYMIZE
```

Data ownership remains explicit:

```text
                 AUTHORITATIVE DOMAIN
                         |
                         v
                    SOURCE OF TRUTH
                         |
             +-----------+-----------+
             |                       |
             v                       v
            API                    EVENT
             |                       |
             v                       v
         CONSUMERS              CONSUMERS
             |                       |
             +-----------+-----------+
                         |
                         v
                  LOCAL SNAPSHOTS
```

A local copy is not automatically authoritative.

For privacy:

```text
DO WE NEED THE DATA?
        |
    +---+---+
    |       |
   NO      YES
    |       |
    v       v
DON'T     COLLECT
COLLECT     |
            v
       MINIMUM REQUIRED
            |
            v
          PROTECT
```

For observability:

```text
BUSINESS DATA
     |
     +----> DATABASE
     |
     +----> API
     |
     +----> EVENT

but not automatically:

     +----> LOG
     +----> METRIC LABEL
     +----> TRACE ATTRIBUTE
```

For legitimate business values:

```text
M&M
 |
 v
BUSINESS VALUE
 |
 v
PERSIST AS M&M
```

not:

```text
M&M
 |
 v
GENERIC HTML ESCAPE
 |
 v
M&amp;M
 |
 v
CORRUPTED BUSINESS DATA
```

Presentation escaping belongs at the presentation/security boundary where it is actually required.

For distributed deletion:

```text
                  PRIVACY ACTION
                        |
                        v
               AUTHORITATIVE DOMAIN
                        |
          +-------------+-------------+
          |             |             |
          v             v             v
       DATABASE        EVENT         CACHE
          |             |             |
          +-------------+-------------+
                        |
                        v
                   CONSUMERS
                        |
                        v
               DELETE / ANONYMIZE
                        |
                        v
                VERIFY COMPLETION
```

For backup recovery:

```text
BACKUP FROM T1
      |
      v
RESTORE AT T2
      |
      v
REAPPLY LIFECYCLE STATE
      |
      +--> Deletions
      +--> Anonymizations
      +--> Legal Holds
      +--> Retention
      |
      v
PRIVACY-CONSISTENT RECOVERY
```

For data quality:

```text
BAD DATA
   |
   v
FIX AT AUTHORITATIVE BOUNDARY
```

rather than:

```text
BAD DATA
   |
   +--> Consumer A workaround
   +--> Consumer B workaround
   +--> Consumer C workaround
```

The complete governance equation is:

```text
OWNERSHIP
    +
CLASSIFICATION
    +
MINIMIZATION
    +
ACCESS CONTROL
    +
ENCRYPTION
    +
RETENTION
    +
LIFECYCLE
    +
LINEAGE
    +
DATA QUALITY
    +
AUDITABILITY
    =
TRUSTWORTHY DATA GOVERNANCE
```

The governing principle is:

```text
Data must never become
"somebody else's problem"
simply because it crossed
a microservice boundary.

Its ownership, classification,
purpose and lifecycle must remain
understood throughout the
distributed architecture.
```

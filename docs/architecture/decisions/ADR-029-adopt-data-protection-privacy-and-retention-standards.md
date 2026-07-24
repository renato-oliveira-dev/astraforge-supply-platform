# ADR-029: Adopt Data Protection, Privacy and Retention Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-029 |
| Title | Adopt Data Protection, Privacy and Retention Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Data Protection, Privacy, LGPD, Security, Retention, Data Lifecycle |
| Related Work Items | LGPD, PII, Data Classification, Encryption, Masking, Retention, Deletion, Audit |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform processes information associated with:

- customers
- users
- dealerships
- orders
- quotations
- contacts
- addresses
- commercial relationships
- authentication identities
- audit records
- integration events
- operational telemetry

Some of this information may constitute personal data under applicable privacy legislation, including Brazil's Lei Geral de Proteção de Dados Pessoais — LGPD.

Data can exist across multiple technical boundaries:

```text
API Requests

↓

Application Memory

↓

PostgreSQL

↓

Transactional Outbox

↓

Kafka

↓

Consumers

↓

Caches

↓

Logs / Traces / Metrics

↓

Backups

↓

Disaster Recovery Copies
```

Protecting only the primary database is therefore insufficient.

Data protection must apply throughout the complete information lifecycle.

---

# 2. Problem Statement

The platform requires standards defining:

- data classification
- personal data identification
- sensitive personal data handling
- privacy by design
- privacy by default
- data minimization
- purpose limitation
- API data exposure
- event data exposure
- logging restrictions
- tracing restrictions
- metrics restrictions
- encryption in transit
- encryption at rest
- field-level protection
- tokenization
- pseudonymization
- masking
- access control
- non-production data usage
- retention
- deletion
- anonymization
- backup retention
- Kafka retention
- audit retention
- data lineage
- data export
- incident response
- privacy testing
- lifecycle governance

---

# 3. Decision Drivers

Primary decision drivers are:

1. LGPD compliance
2. confidentiality
3. data minimization
4. customer trust
5. least privilege
6. auditability
7. security
8. operational safety
9. breach impact reduction
10. retention governance
11. deletion capability
12. environment isolation
13. traceability
14. maintainability
15. privacy by design

---

# 4. Decision

The Enterprise Order Platform adopts a data-protection model based on:

```text
CLASSIFY

↓

MINIMIZE

↓

CONTROL ACCESS

↓

PROTECT

↓

OBSERVE SAFELY

↓

RETAIN INTENTIONALLY

↓

DELETE / ANONYMIZE

↓

AUDIT
```

Privacy and data protection are architectural concerns and must not be deferred solely to infrastructure or legal processes.

---

# 5. Fundamental Principle

The platform adopts:

```text
Collect only what is required.

Expose only what is required.

Store only what is required.

Retain only while required.

Protect everywhere.

Delete when no longer required.
```

---

# 6. Privacy by Design

Privacy requirements must be considered during architecture and implementation.

They must not be added only after a system enters production.

---

# 7. Privacy by Default

The default system behavior should expose and retain the minimum data necessary for the intended business function.

---

# 8. Data Classification

Data must be classified according to sensitivity.

The platform adopts the conceptual levels:

```text
PUBLIC

INTERNAL

CONFIDENTIAL

RESTRICTED
```

---

# 9. Public Data

Public data is intentionally approved for public disclosure.

---

# 10. Internal Data

Internal data is intended for authorized organizational use but does not normally create major impact if exposed internally.

---

# 11. Confidential Data

Confidential data requires controlled access.

Examples may include:

- internal commercial information
- business-sensitive order information
- customer-related information
- operational information

---

# 12. Restricted Data

Restricted data requires the strongest controls.

Examples may include:

- authentication secrets
- private keys
- highly sensitive personal information
- security credentials
- legally restricted information

---

# 13. Personal Data

Personal data must be identified during domain and API design.

Examples may include:

- name
- email
- phone number
- document identifier
- address
- account identifier
- user identifier

depending on context and applicable law.

---

# 14. Sensitive Personal Data

Sensitive personal data requires stronger controls according to applicable legislation and enterprise privacy policy.

---

# 15. Context Matters

A field must not be considered harmless solely because its name appears generic.

For example:

```text
userId
```

may still allow an individual to be identified when combined with other information.

---

# 16. Indirect Identification

Privacy analysis must consider both:

```text
Direct Identification
```

and:

```text
Indirect Identification
```

---

# 17. Data Inventory

Critical systems must maintain an inventory of personal and sensitive information they process.

---

# 18. Inventory Information

The inventory should identify:

- data category
- source
- purpose
- storage
- consumers
- retention
- protection
- owner

---

# 19. Data Owner

Important datasets require an accountable business/data owner.

---

# 20. Technical Owner

Technical ownership does not replace business ownership of data.

---

# 21. Purpose Limitation

Data must be processed for defined legitimate business purposes.

---

# 22. New Use

Using existing personal information for a materially new purpose requires appropriate privacy/business assessment.

---

# 23. Convenience Is Not Purpose

Data must not be collected merely because:

```text
It may be useful someday.
```

---

# 24. Data Minimization

Applications must request and persist only fields required for their responsibility.

---

# 25. API Minimization

An API must not return complete entities when consumers require only a small subset.

---

# 26. Example

Instead of returning:

```json
{
  "id": "...",
  "name": "...",
  "email": "...",
  "phone": "...",
  "address": "...",
  "document": "...",
  "commercialData": "...",
  "internalMetadata": "..."
}
```

when the consumer needs only:

```json
{
  "id": "...",
  "name": "..."
}
```

the smaller contract is preferred.

---

# 27. DTO Boundary

Persistence entities must not be exposed directly as public API contracts.

---

# 28. Response DTO

Response DTOs must intentionally define exposed data.

---

# 29. Request DTO

Request DTOs must accept only required input.

---

# 30. Mass Assignment

Binding arbitrary request fields directly to persistence entities is prohibited.

---

# 31. Internal Fields

Internal fields must remain excluded from external contracts unless explicitly required.

---

# 32. Authorization

Authentication does not imply authorization to every field.

---

# 33. Object-Level Authorization

Access to a resource must validate whether the caller may access that specific resource.

---

# 34. Field-Level Authorization

Where business requirements demand it, individual sensitive fields may require additional authorization.

---

# 35. API Search

Search endpoints must not become unrestricted mechanisms for extracting large volumes of personal data.

---

# 36. Pagination

Large personal-data queries must be paginated.

---

# 37. Export

Bulk exports require explicit authorization and auditability.

---

# 38. API Errors

Error responses must not expose personal or restricted information unnecessarily.

---

# 39. Validation Errors

Validation messages should identify invalid fields without echoing sensitive values.

---

# 40. Integration Events

Kafka events must follow the same minimization principle as APIs.

---

# 41. Event Is Data Replication

Publishing a field into Kafka effectively creates additional copies across:

- brokers
- consumers
- logs
- replay infrastructure
- downstream storage

Therefore event contracts require privacy review.

---

# 42. Event Payload

An event should contain only information required by consumers.

---

# 43. Entity Dump Event

Publishing complete database entities as integration events is prohibited.

---

# 44. Identifier Preference

Where possible, events should use identifiers instead of unnecessary replicated personal details.

---

# 45. Lookup Trade-Off

Using identifiers may require downstream lookup.

The architecture must balance:

```text
Privacy Minimization

vs

Runtime Coupling
```

deliberately.

---

# 46. Event Immutability

Once an event has been published, removing sensitive data from every historical copy may be difficult.

This increases the importance of minimization before publication.

---

# 47. Kafka Headers

Sensitive data must not be placed in Kafka headers without explicit justification.

---

# 48. Topic Naming

Topic names must not contain personal data.

---

# 49. Partition Keys

Personal data should not be used directly as partition keys where an opaque identifier can satisfy the requirement.

---

# 50. Logs

Application logs are not business-data storage.

---

# 51. Logging Personal Data

Personal data must not be logged by default.

---

# 52. Restricted Data Logging

Restricted data must never be intentionally logged.

---

# 53. Prohibited Log Content

Logs must not contain:

- passwords
- access tokens
- refresh tokens
- private keys
- authorization headers
- API secrets
- session credentials
- full payment credentials

---

# 54. Request Logging

Full request/response body logging is prohibited by default in production.

---

# 55. Debug Logging

DEBUG logging does not authorize exposure of personal or restricted information.

---

# 56. Exception Logging

Exceptions must not dump sensitive payloads.

---

# 57. Structured Logging

Structured logging from ADR-019 must use intentionally selected fields.

---

# 58. Correlation ID

Correlation identifiers should be opaque and must not encode personal information.

---

# 59. MDC

MDC/context fields must not become a mechanism for propagating personal data into every log line.

---

# 60. Log Masking

Where limited sensitive data must appear for an approved operational reason, masking/redaction must be applied.

---

# 61. Redaction Is Defense in Depth

Redaction does not justify indiscriminate logging.

Preferred:

```text
Do not log sensitive data
```

before:

```text
Log everything and attempt to sanitize afterward
```

---

# 62. Masking

Masking should preserve only the minimum information necessary for operational identification.

---

# 63. Example

Instead of:

```text
email=customer@example.com
```

a controlled representation may be:

```text
email=c***@example.com
```

only when operationally justified.

---

# 64. Sanitization Failure

A sanitizer must fail safely.

---

# 65. Tracing

Distributed traces must not capture sensitive payloads by default.

---

# 66. Span Attributes

Span attributes must use bounded, non-sensitive metadata.

---

# 67. URL Query Parameters

Sensitive values must not be placed in URL query strings because URLs frequently appear in:

- logs
- proxies
- traces
- browser history
- monitoring systems

---

# 68. HTTP Headers

Sensitive HTTP headers must be excluded or redacted from observability.

---

# 69. Metrics

Metrics must not contain personal data.

---

# 70. Metric Labels

Never use:

```text
email

customerName

documentNumber

orderPayload
```

as metric labels.

---

# 71. Cardinality

Personal identifiers also create dangerous metric cardinality.

---

# 72. Business Metrics

Business metrics should aggregate data.

Example:

```text
orders_created_total{segment="M&M"}
```

rather than customer-specific metrics.

---

# 73. Observability Systems

Logs, metrics and traces are independent data stores from a privacy perspective.

---

# 74. Observability Retention

Each observability system requires an approved retention period.

---

# 75. Observability Access

Access to production telemetry must follow least privilege.

---

# 76. Encryption in Transit

Sensitive production communication must use encryption in transit.

---

# 77. TLS

HTTPS/TLS is mandatory for external production APIs.

---

# 78. Internal Traffic

Internal service communication should use encrypted transport according to platform security architecture.

---

# 79. Database TLS

Database connections should use TLS where supported and required by enterprise policy.

---

# 80. Kafka TLS

Kafka communication must use secure transport according to platform standards.

---

# 81. Redis TLS

Redis communication containing sensitive data should use secure transport.

---

# 82. Certificate Validation

TLS clients must validate server certificates.

---

# 83. Trust-All

Production code must never use:

```text
Trust All Certificates
```

---

# 84. Hostname Verification

Hostname verification must remain enabled.

---

# 85. Encryption at Rest

Persistent production data must use encryption at rest according to infrastructure standards.

---

# 86. Database Encryption

Database storage and backups must be encrypted.

---

# 87. Kafka Encryption

Kafka storage containing confidential information should be encrypted at rest through approved infrastructure controls.

---

# 88. Backup Encryption

ADR-028 backups must remain encrypted.

---

# 89. Object Storage

Object storage containing protected data must use approved encryption.

---

# 90. Encryption Key Management

Encryption keys must be managed separately from encrypted data.

---

# 91. KMS

Approved KMS/HSM mechanisms should be used where appropriate.

---

# 92. Key Rotation

Encryption-key rotation must be supported according to enterprise policy.

---

# 93. Field-Level Encryption

Infrastructure-level encryption does not protect data from every authorized database reader.

---

# 94. Stronger Protection

Highly sensitive fields may require application/field-level encryption.

---

# 95. Field Encryption Decision

Use field-level encryption when threat modeling demonstrates that storage-level encryption is insufficient.

---

# 96. Search Limitation

Encrypted fields may complicate:

- indexing
- searching
- sorting
- uniqueness
- analytics

The trade-off must be explicit.

---

# 97. Deterministic Encryption

Deterministic encryption may enable equality searches but leaks equality patterns.

It requires security review.

---

# 98. Tokenization

Tokenization may replace sensitive values with opaque references.

---

# 99. Token Vault

Tokenization requires secure mapping storage when reversibility is required.

---

# 100. Pseudonymization

Pseudonymization reduces direct identifiability but does not necessarily make data anonymous.

---

# 101. Pseudonymized Data

Pseudonymized data must still receive privacy protection when re-identification remains possible.

---

# 102. Anonymization

Properly anonymized data should not reasonably permit re-identification under the applicable standard.

---

# 103. Hashing

Hashing a value does not automatically anonymize it.

---

# 104. Predictable Values

Low-entropy values such as:

```text
email
phone
document number
```

may be vulnerable to dictionary/re-identification attacks even when hashed.

---

# 105. Salt / Keyed Hash

Where pseudonymous matching is required, a keyed construction may be more appropriate than a raw hash.

---

# 106. Access Control

Data access follows least privilege.

---

# 107. Service Identity

Services should access data using dedicated identities.

---

# 108. Shared Database User

Shared database credentials across unrelated services are discouraged.

---

# 109. Database Privileges

Runtime identities should receive only required privileges.

---

# 110. Administrative Access

Administrative database access must be restricted and audited.

---

# 111. Production Data Access

Human access to production personal data requires legitimate operational/business purpose.

---

# 112. Read Access Is Sensitive

Read-only access can still cause a data breach.

---

# 113. Break Glass

Emergency data access must use governed break-glass procedures where applicable.

---

# 114. Audit

Sensitive administrative access should be auditable.

---

# 115. Data Segregation

DEV, HML and PROD data must remain logically separated.

---

# 116. Production Data in DEV

Raw production personal data must not be copied into DEV by default.

---

# 117. Production Data in HML

Raw production personal data must not be copied into HML by default.

---

# 118. Test Data

Lower environments should use:

- synthetic data
- generated data
- anonymized data
- properly masked datasets

---

# 119. Synthetic Data

Synthetic data is preferred where it adequately represents required test scenarios.

---

# 120. Masked Production Data

If production-derived data is genuinely required, it must undergo an approved irreversible or appropriately controlled masking process.

---

# 121. Referential Integrity

Masked datasets may need to preserve referential integrity without preserving real identities.

---

# 122. Stable Pseudonyms

Stable pseudonymous identifiers may be used when cross-table relationships must remain testable.

---

# 123. Lower Environment Security

Lower environments must not be assumed safe merely because they are non-production.

---

# 124. Developer Laptop

Production personal datasets must not be downloaded to developer laptops without explicit authorization.

---

# 125. Data Retention

Every significant data category must have an explicit retention policy.

---

# 126. Retention Principle

Data must not be retained indefinitely by default.

---

# 127. Retention Drivers

Retention may depend on:

- business requirement
- legal obligation
- contractual obligation
- audit requirement
- security requirement
- operational requirement

---

# 128. Retention Matrix

Maintain a matrix conceptually containing:

| Data Category | Owner | Purpose | Retention | Deletion Method |
|---|---|---|---|---|
| Orders | Business Owner | Order processing | Defined policy | Archive/Delete |
| Audit | Security/Compliance | Traceability | Defined policy | Controlled purge |
| Logs | Platform | Operations | Defined policy | Automatic expiration |
| Kafka Events | Integration Owner | Integration/replay | Defined policy | Topic retention |
| Cache | Service Owner | Performance | Short-lived | TTL/Eviction |

Exact periods require business/legal approval.

---

# 129. Database Retention

Database tables containing personal data require defined lifecycle handling.

---

# 130. Indefinite Rows

Rows must not remain forever solely because no deletion job exists.

---

# 131. Soft Delete

Soft deletion does not satisfy physical deletion requirements by itself.

---

# 132. Soft Delete Purpose

Soft deletion may support business recovery/workflow requirements during an approved retention period.

---

# 133. Physical Deletion

After required retention expires, physical deletion or approved anonymization may be required.

---

# 134. Anonymization Instead of Deletion

Where business statistics must be retained, irreversible anonymization may allow retention of non-identifying information.

---

# 135. Referential Constraints

Deletion design must account for referential integrity.

---

# 136. Cascade Delete

Automatic cascade deletion must be used carefully because it can remove required business/audit records.

---

# 137. Deletion Workflow

Complex deletion should use a controlled workflow rather than ad hoc SQL.

---

# 138. Idempotent Deletion

Deletion jobs should be idempotent where practical.

---

# 139. Batch Deletion

Large data deletion should use bounded batches to avoid:

- long transactions
- lock contention
- excessive WAL
- database saturation

---

# 140. Deletion Observability

Deletion processes should expose:

- processed records
- failures
- duration
- backlog

without exposing personal values.

---

# 141. Deletion Failure

Persistent deletion failures require operational attention.

---

# 142. Legal Hold

Data subject to legal hold must not be deleted while the hold is valid.

---

# 143. Legal Hold Governance

Legal holds require explicit authorization and auditability.

---

# 144. Kafka Retention

Kafka topics require explicit retention configuration.

---

# 145. Infinite Kafka Retention

Indefinite retention is prohibited by default for topics containing personal data.

---

# 146. Topic-Specific Retention

Retention must reflect:

- replay requirements
- DR requirements
- privacy requirements
- storage constraints

---

# 147. Compacted Topics

Compaction does not automatically satisfy privacy deletion requirements.

---

# 148. Tombstones

Deletion semantics in compacted topics require explicit design and validation.

---

# 149. Kafka Historical Copies

Privacy architecture must consider replicas, segments and downstream consumers.

---

# 150. Outbox Retention

Successfully dispatched outbox records require a retention/purge policy.

---

# 151. Pending Outbox

Pending events must never be deleted merely because they are old.

---

# 152. Failed Outbox

Failed events require controlled resolution before deletion.

---

# 153. Audit Data

Audit data requires a specific retention policy.

---

# 154. Audit Integrity

Audit records must not be casually modified to satisfy ordinary application workflows.

---

# 155. Privacy vs Audit

Privacy deletion and audit obligations may conflict.

The resolution must follow approved legal/privacy requirements rather than ad hoc engineering decisions.

---

# 156. Audit Minimization

Audit records should contain sufficient evidence without copying entire sensitive payloads.

---

# 157. Audit Reference

Prefer recording:

```text
Actor

Action

Resource Identifier

Timestamp

Outcome
```

rather than full before/after payloads when unnecessary.

---

# 158. Log Retention

Log retention must be bounded.

---

# 159. Trace Retention

Trace retention must be bounded.

---

# 160. Metrics Retention

Metrics retention must be appropriate to operational requirements.

---

# 161. Backups

Backup retention from ADR-028 must align with privacy and data-retention policies.

---

# 162. Immediate Backup Mutation

Deleting one record from every immutable backup immediately may be technically incompatible with secure backup architecture.

---

# 163. Backup Expiration

Expired backup sets should be destroyed according to retention policy.

---

# 164. Restored Data

If an older backup is restored, deletion/anonymization obligations that occurred after that backup may need to be replayed.

---

# 165. Deletion Ledger

Critical systems should maintain sufficient evidence to reapply required deletion/anonymization after restoration.

---

# 166. DR Privacy

Disaster recovery must not resurrect personal data that should no longer exist in active systems without subsequent remediation.

---

# 167. Recovery Reconciliation

Post-restore reconciliation must include privacy lifecycle state where applicable.

---

# 168. Data Subject Requests

Systems processing personal information must support applicable data-subject processes according to enterprise privacy/legal requirements.

---

# 169. Access Request

Where applicable, relevant personal information must be discoverable across systems.

---

# 170. Correction

Correction workflows must propagate appropriately to authoritative systems.

---

# 171. Deletion / Anonymization

Applicable deletion or anonymization requests require controlled execution across relevant data stores.

---

# 172. Distributed Deletion

A deletion request may affect:

```text
Primary Database

↓

Outbox

↓

Kafka

↓

Downstream Databases

↓

Caches

↓

Search Indexes

↓

Analytics
```

---

# 173. Deletion Event

Where appropriate, a dedicated privacy/deletion event may propagate lifecycle changes to downstream systems.

---

# 174. Deletion Event Minimization

The deletion event itself must contain only identifiers required to execute the deletion.

---

# 175. Cache Eviction

Deletion workflows must evict relevant caches.

---

# 176. Search Index

Search/indexing systems must participate in deletion where applicable.

---

# 177. Analytics

Analytics copies require explicit lifecycle governance.

---

# 178. Data Warehouse

Sending data to a warehouse does not remove privacy obligations.

---

# 179. Data Lake

Data lakes must not become indefinite uncontrolled repositories of personal information.

---

# 180. Data Lineage

Critical personal-data flows should have traceable lineage.

---

# 181. Lineage Question

The organization should be able to answer:

```text
Where did this data come from?

Where is it stored?

Where was it sent?

Which systems consume it?
```

---

# 182. Schema Registry

Kafka schema governance should help identify fields propagated across event contracts.

---

# 183. Contract Review

Adding a personal field to an API/event is an architectural privacy change, not merely a DTO change.

---

# 184. API Versioning

Removing an exposed sensitive field may require contract migration according to ADR-022.

---

# 185. Consumer Inventory

Sensitive event topics should have identifiable consumers.

---

# 186. Unknown Consumer

Uncontrolled topic consumption increases privacy risk.

---

# 187. Data Export

Exports containing personal information require controlled handling.

---

# 188. Export Format

Exports must contain only required fields.

---

# 189. Export Storage

Generated export files require:

- access control
- encryption
- expiration
- secure deletion

---

# 190. Temporary Export

Temporary export artifacts should have short retention.

---

# 191. Signed URL

If signed URLs are used, expiration must be bounded.

---

# 192. Email Attachment

Sensitive bulk data should not be distributed through ordinary email attachments unless explicitly approved.

---

# 193. Browser Download

Download endpoints require authorization and auditability.

---

# 194. CSV Injection

CSV exports must defend against spreadsheet formula injection where applicable.

---

# 195. Object Storage

Export buckets/containers must not be public.

---

# 196. Data Breach

Suspected unauthorized personal-data exposure must trigger security/privacy incident procedures.

---

# 197. Breach Containment

Immediate priorities include:

```text
Contain

Preserve Evidence

Revoke Access

Assess Scope

Protect Affected Systems
```

---

# 198. Breach Assessment

The organization must determine:

- data categories
- affected subjects
- exposure period
- systems
- access path
- potential impact

---

# 199. Notification

Legal/regulatory/customer notification decisions belong to authorized privacy/legal/security processes.

---

# 200. Developer Notification

Developers must not independently communicate suspected breaches externally.

---

# 201. Evidence

Relevant evidence must be preserved according to incident-response requirements.

---

# 202. Secret vs Personal Data Incident

Credential compromise and personal-data exposure may occur together and require coordinated response.

---

# 203. Privacy Threat Modeling

Critical features processing personal data should include privacy considerations during threat modeling.

---

# 204. Threat Questions

Ask:

```text
Can we avoid collecting this field?

Can an unauthorized caller access it?

Can it leak through logs?

Can it leak through events?

Can it remain indefinitely?

Can it be deleted?

Can it be restored from backup incorrectly?
```

---

# 205. Code Review

Code review must consider data exposure changes.

---

# 206. Pull Request

A PR adding sensitive fields should make the privacy impact visible to reviewers.

---

# 207. Automated Tests

Tests should verify sensitive data is not exposed unintentionally.

---

# 208. Serialization Test

DTO serialization tests should validate which fields are present.

---

# 209. Error Test

Exception tests should verify sensitive values are absent.

---

# 210. Logging Test

Logging/redaction components require tests.

---

# 211. API Authorization Test

Object-level authorization must be tested.

---

# 212. Export Test

Export authorization and minimization must be tested.

---

# 213. Retention Test

Retention/purge jobs require tests.

---

# 214. Deletion Test

Deletion workflows must verify all relevant active stores.

---

# 215. Restore Test

DR restore exercises should verify previously deleted data is appropriately reconciled.

---

# 216. Static Analysis

SAST and secret scanning from ADR-024 remain part of the data-protection controls.

---

# 217. DLP

Data Loss Prevention tooling may be used where enterprise infrastructure supports it.

---

# 218. Database Scanning

Data-discovery tooling may help identify unclassified personal fields.

---

# 219. Log Scanning

Telemetry systems may be periodically scanned for accidental sensitive-data leakage.

---

# 220. API Schema Review

OpenAPI schemas should help identify exposed sensitive fields.

---

# 221. Kafka Schema Review

Event schemas should be reviewed for unnecessary personal information.

---

# 222. Documentation

Sensitive examples in documentation must use fictitious data.

---

# 223. Screenshots

Production screenshots containing personal data must be handled as protected data.

---

# 224. Support Tickets

Copying personal information into support systems creates additional data copies and should be minimized.

---

# 225. Chat Tools

Production sensitive data must not be pasted into unapproved collaboration or AI systems.

---

# 226. Test Fixtures

Test fixtures committed to Git must not contain real production personal data.

---

# 227. Seed Data

Development seed data must be synthetic.

---

# 228. UUID

Replacing a person's name with a UUID does not necessarily anonymize the record if the UUID can be mapped back to the person.

---

# 229. Data Combination

Multiple non-sensitive-looking fields may become identifying when combined.

---

# 230. Least Data

The safest unnecessary sensitive field is the field that was never collected.

---

# 231. Data Lifecycle

Every significant data category should conceptually follow:

```text
CREATE

↓

USE

↓

SHARE

↓

STORE

↓

ARCHIVE

↓

DELETE / ANONYMIZE
```

---

# 232. Lifecycle Ownership

Responsibility must exist across the entire lifecycle, not only creation.

---

# 233. Schema Evolution

Removing a personal field requires considering:

- API
- database
- events
- caches
- logs
- analytics
- backups

---

# 234. Database Column Removal

Removing the Java field does not remove historical database data automatically.

---

# 235. Event Removal

Removing a field from new events does not remove historical Kafka records.

---

# 236. Log Removal

Fixing logging today does not automatically erase historical log exposure.

---

# 237. Historical Remediation

Known historical exposure requires explicit remediation/risk assessment.

---

# 238. Data Copy Awareness

Every new copy increases:

- attack surface
- deletion complexity
- retention complexity
- breach scope

---

# 239. Microservice Boundary

Microservices must not replicate customer data merely for convenience.

---

# 240. Local Copy

A local data copy requires explicit justification such as:

- availability
- latency
- autonomy
- historical snapshot semantics

---

# 241. Snapshot

A business snapshot may intentionally preserve historical values required for transaction integrity.

---

# 242. Snapshot Privacy

Historical snapshot requirements still require defined retention.

---

# 243. Order History

Data necessary to preserve historical order correctness may legitimately differ from mutable customer master data.

---

# 244. Domain Semantics

Deletion/anonymization must preserve legally/business-required transaction integrity.

---

# 245. Auditability

Privacy operations themselves must be auditable.

---

# 246. Privacy Audit Event

Audit should record:

- request identifier
- operation
- target data category
- actor/system
- timestamp
- outcome

without unnecessarily duplicating personal data.

---

# 247. Consent

Where consent is the applicable lawful basis, evidence and lifecycle must follow enterprise legal/privacy requirements.

---

# 248. Consent Is Contextual

Not all processing depends on consent.

The legal basis must be determined by authorized business/privacy/legal stakeholders.

---

# 249. Consent Withdrawal

Where applicable, withdrawal must affect future processing according to approved rules.

---

# 250. Technical Team Role

Engineering implements approved privacy requirements.

Engineering must not independently invent legal interpretations.

---

# 251. Privacy Configuration

Retention periods and similar legally significant parameters must be controlled and auditable.

---

# 252. Hardcoded Retention

Legally/business-sensitive retention periods should not be scattered as magic numbers throughout source code.

---

# 253. Configuration Validation

Retention configuration must validate reasonable bounds.

---

# 254. Production Override

Changing retention policy in production requires controlled approval.

---

# 255. Deletion Scheduler

Automated retention jobs should be:

- restartable
- observable
- bounded
- idempotent

---

# 256. Concurrent Execution

Distributed schedulers must prevent unintended duplicate destructive execution where required.

---

# 257. Dry Run

High-impact purge processes should support a safe dry-run/report mode where practical.

---

# 258. Deletion Report

A purge process should report aggregate results without exposing deleted personal values.

---

# 259. Performance

Retention jobs must not degrade critical transactional workloads.

---

# 260. Maintenance Window

Large purge/archive operations may use controlled maintenance windows or throttling.

---

# 261. Archive

Archiving is not deletion.

---

# 262. Archived Data

Archived personal data remains protected and subject to retention requirements.

---

# 263. Archive Access

Archived data should normally have more restrictive access than active transactional data.

---

# 264. Retention Expiration

At final retention expiration:

```text
Delete

or

Irreversibly Anonymize
```

according to approved policy.

---

# 265. Data Protection Metrics

Recommended aggregate metrics include:

- retention-job failures
- deletion backlog
- deletion failures
- export operations
- unauthorized-access denials
- redaction failures

without personal identifiers.

---

# 266. Alerts

Actionable alerts should exist for critical lifecycle failures such as persistent retention/deletion failure.

---

# 267. Privacy SLO

Where appropriate, critical privacy workflows may have operational objectives.

Example:

```text
Approved deletion requests completed
within the defined operational target.
```

---

# 268. Anti-Patterns

The following are prohibited or strongly discouraged:

- collecting fields without a defined purpose
- returning entire entities through APIs
- exposing persistence entities directly
- publishing entire entities into Kafka
- logging full request/response bodies in production
- logging passwords or tokens
- storing personal data in metric labels
- storing personal data in correlation IDs
- placing sensitive information in URLs
- disabling TLS certificate validation
- trust-all HTTP clients
- using production data in DEV/HML without approved protection
- committing real personal data as test fixtures
- copying production datasets to developer machines
- retaining database records indefinitely without policy
- assuming soft delete equals deletion
- deleting large datasets in one uncontrolled transaction
- indefinite Kafka retention for personal data by default
- treating Kafka compaction as automatic privacy deletion
- retaining dispatched outbox records forever
- copying complete payloads into audit records
- indefinite log retention
- ignoring privacy obligations in backups
- restoring deleted data without reconciliation
- assuming hashing automatically anonymizes data
- assuming UUID replacement automatically anonymizes data
- treating pseudonymized data as public data
- unrestricted bulk exports
- public object-storage exports
- long-lived signed download URLs
- using email as the default bulk-sensitive-data transport
- introducing personal fields without contract/privacy review
- retaining obsolete data because storage is inexpensive
- creating uncontrolled microservice copies of customer data
- using legal/privacy requirements as undocumented magic numbers
- manually deleting production data through ad hoc SQL as the normal process

---

# 269. Positive Consequences

The decision provides:

- stronger LGPD alignment
- reduced unnecessary data collection
- reduced API exposure
- reduced event exposure
- safer observability
- improved encryption posture
- controlled non-production data
- explicit retention
- deletion capability
- safer backup recovery
- stronger auditability
- reduced breach impact
- improved data lineage
- clearer ownership
- better privacy engineering

---

# 270. Negative Consequences

The decision introduces:

- data classification effort
- retention implementation
- deletion workflows
- masking requirements
- additional authorization checks
- export controls
- privacy review
- reconciliation complexity
- additional tests
- potential query complexity for encrypted data

These costs are accepted because data protection is a fundamental production requirement.

---

# 271. Neutral Consequences

The decision also means:

- some historical data cannot be retained indefinitely
- some debugging becomes harder because payload logging is restricted
- some consumers receive smaller event contracts
- lower environments may have less realistic data unless synthetic generation is mature
- some deletion requirements require asynchronous processing
- some legally required records may remain while identifying fields are minimized or anonymized

---

# 272. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Personal data exposed by API | Critical | Medium | DTO minimization and authorization |
| Personal data exposed in logs | Critical | Medium | Logging standards and redaction |
| Sensitive data published to Kafka | Critical | Medium | Event contract review |
| Production data copied to DEV | Critical | Medium | Synthetic/masked data policy |
| Unauthorized bulk export | Critical | Low | Explicit authorization and audit |
| Data retained indefinitely | High | Medium | Retention matrix and automated purge |
| Deletion breaks referential integrity | High | Medium | Controlled deletion workflow |
| Restore resurrects deleted data | High | Medium | Post-restore deletion reconciliation |
| Encryption key lost | Critical | Low | KMS recovery strategy |
| Raw hash re-identification | High | Medium | Proper pseudonymization design |
| Metric cardinality leaks identifiers | High | Medium | No PII labels |
| Sensitive field added unnoticed | High | Medium | API/event contract review |
| Kafka history conflicts with deletion | High | Medium | Retention/lifecycle design |
| Backup retention violates privacy policy | High | Low | Coordinated backup retention |
| Support tooling creates uncontrolled copies | High | Medium | Data handling standards |

---

# 273. Implementation Guidance

The following rules are mandatory:

1. Personal and sensitive data must be identified during design.
2. Significant datasets must have accountable ownership.
3. Applications must collect only required data.
4. APIs must expose only required fields.
5. Persistence entities must not be public API contracts.
6. Object-level authorization must protect personal resources.
7. Bulk exports require explicit authorization.
8. Kafka events must contain only required information.
9. Complete persistence entities must not be published as events.
10. Sensitive data must not be intentionally logged.
11. Full production request/response body logging is prohibited by default.
12. Correlation identifiers must not contain personal data.
13. Metric labels must not contain personal identifiers.
14. Trace attributes must not contain sensitive payloads.
15. Sensitive data must not be placed in URLs.
16. Production transport must use approved encryption.
17. TLS certificate validation must never be disabled in production.
18. Persistent protected data must use encryption at rest.
19. Highly sensitive fields must receive stronger protection where threat modeling requires it.
20. Production data must not be copied raw into DEV/HML by default.
21. Synthetic data is preferred for lower environments.
22. Test fixtures committed to source control must not contain real personal data.
23. Significant data categories require explicit retention periods.
24. Data must not be retained indefinitely by default.
25. Soft deletion alone must not be treated as final deletion.
26. Large deletion processes must be bounded and observable.
27. Kafka topics containing personal information require explicit retention.
28. Outbox data requires retention after successful dispatch.
29. Audit data must be minimized.
30. Logs, traces and metrics require explicit retention.
31. Backup retention must align with privacy requirements.
32. Restored backups must reconcile deletion/anonymization obligations.
33. Applicable data-subject requests must propagate to relevant active stores.
34. Cache and search-index copies must participate in deletion where applicable.
35. Analytics/data-lake copies require lifecycle governance.
36. Critical personal-data flows should maintain lineage.
37. Adding sensitive API/event fields requires review.
38. Export artifacts must be encrypted, access-controlled and expiring.
39. Privacy incidents must integrate with security incident response.
40. Privacy workflows require automated tests.
41. Historical data copies must be considered when removing fields.
42. Microservices must not replicate personal data without justification.
43. Privacy operations must be auditable.
44. Legally significant retention configuration must be controlled.
45. Purge jobs should be idempotent and restartable.
46. Archive data remains subject to privacy controls.
47. Pseudonymization must not be treated automatically as anonymization.
48. DR recovery must include privacy lifecycle reconciliation.
49. Sensitive production-data access must follow least privilege.
50. Privacy protection must cover the complete data lifecycle.

---

# 274. Data Protection Readiness Gate

A service processing personal data is not considered fully production ready until:

```text
[ ] Personal data identified

[ ] Sensitive data identified

[ ] Data owner identified

[ ] Processing purpose documented

[ ] API exposure reviewed

[ ] Request DTO minimized

[ ] Response DTO minimized

[ ] Authorization reviewed

[ ] Kafka payloads reviewed

[ ] Logs reviewed

[ ] Traces reviewed

[ ] Metrics reviewed

[ ] Correlation IDs reviewed

[ ] Encryption in transit validated

[ ] Encryption at rest validated

[ ] Key management reviewed

[ ] DEV/HML data strategy defined

[ ] Retention defined

[ ] Database deletion strategy defined

[ ] Kafka retention defined

[ ] Outbox retention defined

[ ] Audit retention defined

[ ] Observability retention defined

[ ] Backup retention aligned

[ ] Restore/deletion reconciliation defined

[ ] Export authorization reviewed

[ ] Data lineage documented where critical

[ ] Deletion/anonymization workflow tested

[ ] Privacy incident path documented
```

---

# 275. Validation

This ADR will be validated through:

- architecture review
- privacy review
- OpenAPI contract review
- Kafka schema review
- authorization tests
- serialization tests
- logging tests
- redaction tests
- trace inspection
- metric-cardinality inspection
- SAST
- secret scanning
- DLP where available
- database access review
- encryption validation
- synthetic-data validation
- retention-job tests
- deletion tests
- Kafka retention validation
- outbox purge tests
- export-security tests
- backup-retention review
- DR privacy reconciliation tests
- periodic data-discovery review

---

# 276. Success Criteria

The decision is successful when:

- services know which personal data they process
- APIs expose only necessary fields
- integration events avoid unnecessary personal information
- logs and traces do not routinely contain personal data
- metrics contain no personal identifiers
- lower environments do not depend on raw production data
- protected data is encrypted appropriately
- data categories have explicit retention
- retention jobs operate automatically
- deletion/anonymization can propagate across relevant systems
- backup restoration does not permanently resurrect deleted information
- exports are controlled and auditable
- data lineage is available for critical flows
- privacy-impacting contract changes are visible during review
- personal-data exposure can be investigated effectively

---

# 277. Alternatives Rejected

## 277.1 Protect Only the Database

Rejected because data exists across APIs, Kafka, logs, caches, exports and backups.

---

## 277.2 Log Everything and Mask Later

Rejected because sanitization is not a reliable substitute for minimization.

---

## 277.3 Copy Production Data to Test Environments

Rejected as the default because it unnecessarily expands exposure.

---

## 277.4 Retain Everything Indefinitely

Rejected because storage convenience is not a valid lifecycle policy.

---

## 277.5 Soft Delete as Universal Privacy Deletion

Rejected because the data remains present and accessible.

---

## 277.6 Hash Everything

Rejected because hashing alone does not necessarily anonymize predictable personal data.

---

## 277.7 Encrypt Every Field at Application Level

Rejected as the universal solution because it introduces substantial operational/query complexity without proportional benefit for every field.

---

## 277.8 Rely Exclusively on Infrastructure Security

Rejected because application contracts and business logic determine much of the data exposure.

---

# 278. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-009: Use Apache Kafka for Integration Events
- ADR-010: Adopt Transactional Outbox Pattern
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-019: Adopt Structured Logging
- ADR-022: Adopt API Contract Governance
- ADR-023: Adopt API Security Standards
- ADR-024: Adopt Software Supply Chain Security
- ADR-025: Adopt Kubernetes Runtime Security Standards
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-027: Adopt Production Incident Management and Operational Readiness Standards
- ADR-028: Adopt Disaster Recovery and Business Continuity Standards
- ADR-030: Adopt Kafka Event Governance and Schema Evolution Standards

---

# 279. References

- Lei nº 13.709/2018 — Lei Geral de Proteção de Dados Pessoais (LGPD)
- Autoridade Nacional de Proteção de Dados — ANPD
- OWASP ASVS
- OWASP API Security Top 10
- OWASP Logging Cheat Sheet
- OWASP Cryptographic Storage Cheat Sheet
- OWASP Transport Layer Security Cheat Sheet
- NIST Privacy Framework
- NIST Cybersecurity Framework
- PostgreSQL Documentation
- Apache Kafka Documentation
- OpenTelemetry Documentation
- Kubernetes Documentation
- ADR-019: Adopt Structured Logging
- ADR-022: Adopt API Contract Governance
- ADR-023: Adopt API Security Standards
- ADR-028: Adopt Disaster Recovery and Business Continuity Standards

---

# 280. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial Data Protection, Privacy and Retention baseline |

---

# 281. Decision Summary

The Enterprise Order Platform adopts the following data lifecycle:

```text
BUSINESS REQUIREMENT

↓

IS THIS DATA REQUIRED?

        |
       NO
        |
        v
DO NOT COLLECT

        |
       YES
        |
        v
CLASSIFY

↓

MINIMIZE

↓

AUTHORIZE

↓

PROTECT

↓

PROCESS

↓

SHARE ONLY WHEN REQUIRED

↓

RETAIN FOR DEFINED PERIOD

↓

DELETE / ANONYMIZE
```

Protection applies to the complete data path:

```text
CLIENT
  |
  v
API
  |
  v
APPLICATION
  |
  +-------------> LOGS
  |
  +-------------> TRACES
  |
  +-------------> METRICS
  |
  v
POSTGRESQL
  |
  v
OUTBOX
  |
  v
KAFKA
  |
  v
CONSUMERS
  |
  +-------------> CACHE
  |
  +-------------> ANALYTICS
  |
  v
BACKUPS
```

Every boundary must answer:

```text
Is this field necessary?

Who can access it?

How is it protected?

How long does it remain?

How is it removed?
```

The observability principle becomes:

```text
OBSERVABILITY

≠

COPY ALL PRODUCTION DATA
```

Instead:

```text
OPERATIONAL CONTEXT
+
OPAQUE IDENTIFIERS
+
AGGREGATED METRICS
+
CONTROLLED REDACTION
```

The event-governance principle becomes:

```text
DATABASE ENTITY
       X
       |
       | DO NOT DUMP
       v

BUSINESS EVENT

Only the fields required
to communicate the event
```

The environment principle becomes:

```text
PROD
Real protected data
Strict access

DEV / HML
Synthetic
or
Approved anonymized/masked data
```

Retention follows:

```text
CREATE

↓

ACTIVE USE

↓

RETENTION WINDOW

↓

LEGAL / BUSINESS CHECK

       +----------------+
       |                |
       v                v
   DELETE          ANONYMIZE
```

Disaster recovery and privacy remain connected:

```text
BACKUP RESTORED

↓

DATA STATE RECOVERED

↓

DELETION / ANONYMIZATION
OBLIGATIONS RECONCILED

↓

ACTIVE DATA CORRECT
```

Therefore:

```text
RESTORE SUCCESS
```

does not automatically mean:

```text
PRIVACY STATE CORRECT
```

The complete security/data chain established by the previous ADRs becomes:

```text
API SECURITY
ADR-023

↓

SUPPLY CHAIN
ADR-024

↓

KUBERNETES RUNTIME
ADR-025

↓

CONFIGURATION & SECRETS
ADR-026

↓

PRODUCTION OPERATIONS
ADR-027

↓

DISASTER RECOVERY
ADR-028

↓

DATA PROTECTION & PRIVACY
ADR-029
```

The final principle is:

```text
Data that does not need to exist
cannot be leaked.

Data that must exist
must be protected.

Data that no longer needs to exist
must not remain forever.
```

# ADR-071: Adopt Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-071 |
| Title | Adopt Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Data Privacy, PII, LGPD, Auditability, Retention, Secure Data Handling |
| Related Work Items | Java 21, Spring Boot 3, PostgreSQL, Redis, SQS, SQS, AWS, Observability |
| Supersedes | ADR-046 |
| Superseded By | None |

---

# 1. Context

Enterprise applications process information with different sensitivity levels.

Examples include:

```text
PUBLIC DATA

INTERNAL DATA

BUSINESS-CONFIDENTIAL DATA

PERSONAL DATA

SENSITIVE PERSONAL DATA

AUTHENTICATION DATA

FINANCIAL DATA

SECURITY SECRETS
```

The same information may travel through:

```text
HTTP REQUEST
      |
      v
CONTROLLER
      |
      v
APPLICATION
      |
      v
DATABASE
      |
      +--> CACHE
      |
      +--> EVENT
      |
      +--> LOG
      |
      +--> AUDIT
      |
      +--> BACKUP
      |
      +--> DATA EXPORT
```

Protecting only the database is therefore insufficient.

A personal-data leak can occur through:

```text
Application Logs

Stack Traces

SQS Events

SQS Messages

Redis

HTTP Error Responses

Monitoring Tools

Tracing Attributes

Database Dumps

Development Environments

Support Exports

Temporary Files
```

Privacy must consequently be treated as a system-wide architectural property.

---

# 2. Regulatory Context

Applicable systems MUST comply with organizational legal and privacy requirements.

For Brazilian operations, this includes applicable requirements derived from:

```text
LGPD
Lei nº 13.709/2018
```

International systems MAY additionally be subject to:

```text
GDPR

CCPA/CPRA

Contractual Privacy Requirements

Industry-Specific Regulations
```

This ADR establishes engineering controls.

It does not replace formal legal interpretation.

---

# 3. Problem Statement

The organization requires standards covering:

- data classification
- personal data
- sensitive personal data
- PII
- data minimization
- purpose limitation
- access control
- encryption
- masking
- tokenization
- pseudonymization
- anonymization
- logging
- tracing
- metrics
- PostgreSQL
- Redis
- SQS
- SQS
- REST APIs
- audit trails
- data retention
- deletion
- backups
- exports
- development/test environments
- privileged access
- data-subject operations
- privacy incident investigation
- secure disposal

---

# 4. Decision

Data protection MUST be designed across the complete data lifecycle.

Canonical lifecycle:

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
USE
   |
   v
SHARE
   |
   v
ARCHIVE
   |
   v
DELETE / ANONYMIZE
```

Every stage requires explicit protection.

---

# 5. Fundamental Principle

```text
Collect only
what is needed.

Expose only
what is required.

Store only
as long as justified.

Log only
what is safe.

Grant access only
to authorized actors.

Delete or anonymize
when retention ends.
```

---

# 6. Data Classification

Data MUST be classified according to organizational policy.

A practical engineering model is:

```text
PUBLIC

INTERNAL

CONFIDENTIAL

RESTRICTED
```

---

# 7. Public Data

Public data may be intentionally exposed externally.

Example:

```text
Published Product Documentation
```

Public classification MUST still be intentional.

---

# 8. Internal Data

Internal data is not intended for unrestricted public distribution.

---

# 9. Confidential Data

Confidential data includes information where unauthorized disclosure could materially affect:

```text
Customers

Employees

Partners

Business Operations
```

---

# 10. Restricted Data

Restricted data requires the strongest controls.

Examples MAY include:

```text
Credentials

Authentication Tokens

Private Keys

Sensitive Personal Data

Highly Sensitive Financial Information
```

---

# 11. Personal Data

Personal data is information relating to an identified or identifiable natural person according to applicable privacy requirements.

Examples MAY include:

```text
Name

Email

Telephone

Document Number

Address

User Identifier
```

depending on context.

---

# 12. Sensitive Personal Data

Sensitive personal data MUST receive stronger controls according to applicable regulation and organizational policy.

---

# 13. Context Matters

A field is not safe merely because its name appears harmless.

Example:

```text
customerId
```

may be pseudonymous but still allow an individual to be identified when combined with other systems.

---

# 14. Data Inventory

Systems processing personal/confidential information SHOULD maintain an inventory describing:

```text
Data Element

Classification

Purpose

Source

Storage

Consumers

Retention

Protection
```

---

# 15. Data Ownership

Material datasets SHOULD have an identifiable business/data owner.

---

# 16. Data Minimization

Applications MUST collect only information required for legitimate system functionality.

---

# 17. Future Use

Fields MUST NOT be collected merely because:

```text
"we may need it someday"
```

without a justified purpose.

---

# 18. DTO Minimization

API DTOs SHOULD contain only fields needed by the contract.

---

# 19. Entity Exposure

JPA entities MUST NOT be directly exposed as public API contracts.

---

# 20. Overfetching Sensitive Data

Queries SHOULD avoid retrieving sensitive fields that the use case does not require.

---

# 21. Integration Minimization

Service-to-service integrations SHOULD transmit only required data.

---

# 22. Event Minimization

Events MUST NOT become copies of entire database entities merely for convenience.

---

# 23. Event Contract

A SQS event SHOULD contain the minimum information required by legitimate consumers.

---

# 24. Purpose Limitation

Personal data MUST be used according to its approved purpose and applicable governance.

---

# 25. Secondary Use

Using existing data for a materially different purpose requires appropriate governance review.

---

# 26. API Security

Sensitive API responses MUST require appropriate authentication and authorization.

---

# 27. Object-Level Authorization

Authorization MUST verify access to the requested resource, not merely endpoint access.

Conceptually:

```text
AUTHENTICATED?
     |
     v
AUTHORIZED ROLE?
     |
     v
AUTHORIZED FOR
THIS RESOURCE?
     |
     v
RETURN DATA
```

---

# 28. Excessive Data Exposure

APIs MUST NOT expose internal/sensitive fields merely because they exist in the underlying entity.

---

# 29. Response DTO

Dedicated response DTOs SHOULD control externally visible data.

---

# 30. Request DTO

Dedicated request DTOs SHOULD control accepted input.

---

# 31. Mass Assignment

Request payloads MUST NOT automatically bind unrestricted fields into persistence entities.

---

# 32. Error Response

Error responses MUST NOT disclose sensitive internal data.

---

# 33. Stack Trace

Stack traces MUST NOT be returned to ordinary API consumers.

---

# 34. SQL Error

Raw database errors SHOULD NOT be exposed externally.

---

# 35. Internal Identifier

Internal identifiers MAY be returned when contractually required but SHOULD NOT be assumed to provide authorization protection.

---

# 36. UUID

Using UUID instead of sequential ID does not replace authorization.

---

# 37. Logs

Logs MUST be treated as a data store.

---

# 38. Logging Personal Data

Personal data SHOULD NOT be logged unless operationally necessary and explicitly permitted.

---

# 39. Restricted Logging

Restricted data MUST NOT be logged in plaintext.

---

# 40. Authorization Header

Never log:

```text
Authorization
```

headers.

---

# 41. Bearer Token

Never log complete:

```text
Bearer tokens
```

---

# 42. Password

Passwords MUST NEVER appear in application logs.

---

# 43. Secret

Secrets, private keys and API credentials MUST NEVER appear in logs.

---

# 44. Request Logging

HTTP request logging MUST apply field/header filtering before payloads are recorded.

---

# 45. Response Logging

HTTP response bodies MUST NOT be logged indiscriminately.

---

# 46. Payload Logging

Full payload logging SHOULD be disabled by default for sensitive business APIs.

---

# 47. Debug Logging

Debug mode does not remove privacy obligations.

---

# 48. Production Debug

Production debug logging SHOULD be temporary, controlled and reviewed.

---

# 49. Log Masking

Where sensitive values must be represented operationally, masking SHOULD be applied.

Example:

```text
12345678901
```

could become:

```text
*******8901
```

according to data-specific policy.

---

# 50. Masking vs Escaping

Masking and output escaping solve different problems.

```text
MASKING
    |
    v
PREVENT DATA DISCLOSURE

ESCAPING
    |
    v
SAFE OUTPUT CONTEXT
```

They MUST NOT be confused.

---

# 51. Business Data Integrity

Security transformations MUST NOT corrupt legitimate business data.

For example:

```text
M&M
```

MUST NOT be persistently transformed into:

```text
M&amp;M
```

merely to satisfy generic sanitization logic.

---

# 52. Contextual Encoding

Output encoding MUST occur at the appropriate rendering boundary.

---

# 53. Log Sanitization

Log sanitization MAY normalize dangerous control characters to prevent log injection while preserving legitimate business values.

---

# 54. CRLF

Untrusted log values SHOULD prevent uncontrolled:

```text
\r

\n
```

injection.

---

# 55. Sanitizer Scope

A log sanitizer MUST NOT become a generic mutation layer for domain data.

---

# 56. Trace

Distributed tracing MUST follow the same privacy controls as logs.

---

# 57. Trace Attribute

Sensitive payload fields MUST NOT be added indiscriminately as span attributes.

---

# 58. Correlation ID

Correlation identifiers SHOULD be preferred over personal data for request tracing.

---

# 59. Trace ID

Trace IDs SHOULD allow operational correlation without requiring sensitive identifiers.

---

# 60. Metrics

Metrics SHOULD use low-cardinality, non-sensitive labels.

---

# 61. PII Metric Label

Personal data MUST NOT be used as metric labels.

Avoid:

```text
email="person@example..."
```

---

# 62. High Cardinality

Identifiers such as:

```text
customerId

orderId

userId
```

SHOULD generally not be metric labels.

---

# 63. PostgreSQL

Sensitive data stored in PostgreSQL MUST follow access, encryption, retention and backup policies.

---

# 64. Encryption at Rest

Production databases MUST use approved encryption-at-rest controls.

---

# 65. Encryption in Transit

Database connections MUST use approved encrypted transport where required by infrastructure policy.

---

# 66. Database Credentials

Database credentials MUST be managed as secrets.

---

# 67. Least Privilege

Application database users MUST receive only required privileges.

---

# 68. Shared DBA Credential

Applications MUST NOT execute using broad DBA/superuser credentials.

---

# 69. Schema Access

Database access SHOULD be constrained to required schemas and operations.

---

# 70. Sensitive Column

Highly sensitive columns MAY require additional application/database-level protection based on threat model.

---

# 71. Application-Level Encryption

Application-level field encryption MAY be used when infrastructure encryption at rest does not sufficiently address the threat model.

---

# 72. Encryption Key Separation

Encryption keys MUST be managed separately from encrypted data.

---

# 73. Hardcoded Encryption Key

Encryption keys MUST NOT be hardcoded in application source.

---

# 74. Searchable Encryption

Encryption can affect:

```text
Search

Indexing

Sorting

Uniqueness
```

and MUST be designed accordingly.

---

# 75. Hashing

One-way hashing SHOULD be used when the original value does not need to be recovered.

---

# 76. Password Hashing

Passwords MUST use an approved adaptive password hashing mechanism where the application owns password storage.

---

# 77. Encryption Is Not Hashing

```text
ENCRYPTION
```

is reversible with a key.

```text
HASHING
```

is intended to be one-way.

The mechanisms MUST NOT be interchanged blindly.

---

# 78. Tokenization

Tokenization MAY replace sensitive identifiers with controlled surrogate values.

---

# 79. Pseudonymization

Pseudonymization reduces direct identifiability but does not automatically make information anonymous.

---

# 80. Anonymization

Anonymization MUST be designed so re-identification is not reasonably possible according to applicable governance.

---

# 81. Fake Anonymization

Replacing a name while retaining:

```text
Email

Telephone

Document Number

Exact Address
```

is not meaningful anonymization.

---

# 82. Redis

Redis MUST be treated as a data store, not merely an ephemeral technical component.

---

# 83. Cache Minimization

Sensitive information SHOULD NOT be cached unless caching is necessary.

---

# 84. Cache TTL

Sensitive cached data MUST have an appropriate TTL.

---

# 85. Infinite Cache

Indefinite retention in Redis is prohibited for personal data unless explicitly justified.

---

# 86. Redis Encryption

Redis transport/storage protections MUST follow infrastructure standards.

---

# 87. Redis Access

Redis access MUST follow least privilege and network isolation requirements.

---

# 88. Cache Key

Sensitive personal values SHOULD NOT appear directly in cache keys.

Avoid:

```text
customer:email:person@example...
```

Prefer a non-sensitive internal identifier where possible.

---

# 89. Cache Eviction

Deletion/anonymization workflows MUST consider cached copies.

---

# 90. SQS

SQS queues MUST be classified according to the sensitivity of their payloads.

---

# 91. SQS PII

Personal data SHOULD be minimized in event payloads.

---

# 92. SQS Retention

Topic retention MUST align with data-retention requirements.

---

# 93. Infinite SQS Retention

Infinite event retention MUST NOT be assumed safe for personal data.

---

# 94. SQS Access

Topic ACLs MUST follow least privilege.

---

# 95. SQS Encryption

Broker transport/storage controls MUST comply with infrastructure security standards.

---

# 96. Event Replay

Replay of historical events MUST consider whether the historical data remains legally and operationally valid.

---

# 97. Compact Topic

Log compaction does not automatically satisfy privacy deletion requirements.

---

# 98. SQS

SQS messages MUST follow the same minimization and classification principles as other integration channels.

---

# 99. SQS Payload

Sensitive data SHOULD NOT be included when an opaque identifier can safely provide equivalent functionality.

---

# 100. Dead-Letter Queue

DLQs MUST be included in retention and privacy governance.

---

# 101. DLQ Risk

A message removed from the main queue may remain in:

```text
DLQ

Logs

Monitoring

Replay Storage
```

and therefore is not necessarily deleted.

---

# 102. Audit Trail

Security/business audit trails MUST be distinguished from diagnostic logs.

---

# 103. Audit Purpose

Audit records SHOULD answer:

```text
WHO

DID WHAT

TO WHICH RESOURCE

WHEN

WITH WHAT RESULT
```

---

# 104. Audit Content

Audit trails SHOULD contain sufficient information for accountability without unnecessarily copying sensitive payloads.

---

# 105. Audit Actor

Audit events SHOULD identify the authenticated actor or service identity.

---

# 106. Audit Timestamp

Audit timestamps MUST be reliable and normalized according to platform time standards.

---

# 107. Audit Action

Actions SHOULD use stable semantic identifiers.

Example:

```text
ORDER_APPROVED
```

rather than arbitrary free-text descriptions.

---

# 108. Audit Before/After

Before/after values MAY be captured when required, but sensitive fields MUST be filtered or masked.

---

# 109. Audit Integrity

Audit records SHOULD be protected against unauthorized modification.

---

# 110. Audit Access

Audit information MUST have restricted access.

---

# 111. Audit Retention

Audit retention MAY differ from operational-log retention and MUST be explicitly defined.

---

# 112. Audit Failure

Failure to persist mandatory audit information MUST have an explicit business/technical policy.

---

# 113. Audit Is Not Log

This is insufficient:

```java
log.info("user approved order");
```

when formal auditability is required.

---

# 114. Data Retention

Every material data category SHOULD have a defined retention period or retention rule.

---

# 115. Retention Definition

Retention SHOULD specify:

```text
WHAT DATA

WHY RETAINED

HOW LONG

WHERE STORED

WHAT HAPPENS AFTERWARD
```

---

# 116. Forever

```text
Keep forever
```

is not an acceptable default retention policy.

---

# 117. Business Retention

Business/legal requirements MAY require long-term retention.

Such retention MUST be documented.

---

# 118. Technical Retention

Technical convenience alone SHOULD NOT justify indefinite retention.

---

# 119. Log Retention

Log retention SHOULD be appropriate to operational/security requirements and data sensitivity.

---

# 120. Trace Retention

Trace retention SHOULD generally be shorter than core transactional-data retention unless requirements justify otherwise.

---

# 121. Cache Retention

Cache retention SHOULD normally be significantly shorter than source-of-truth retention.

---

# 122. Backup Retention

Backups MUST have explicit retention policies.

---

# 123. Backup Privacy

A deleted production record may still exist in retained backups.

Privacy processes MUST account for backup lifecycle.

---

# 124. Backup Expiration

Expired backups MUST be securely removed according to infrastructure policy.

---

# 125. Backup Access

Backup access MUST be more restrictive than ordinary application access where practical.

---

# 126. Backup Encryption

Backups containing confidential/restricted information MUST be encrypted according to approved infrastructure controls.

---

# 127. Data Deletion

Deletion workflows MUST consider all relevant copies.

Conceptually:

```text
PRIMARY DATABASE
       |
       +--> CACHE
       |
       +--> SEARCH INDEX
       |
       +--> EVENTS
       |
       +--> EXPORTS
       |
       +--> BACKUPS
```

---

# 128. Hard Delete

Hard deletion MAY be required when retention/legal requirements permit or demand it.

---

# 129. Soft Delete

Soft delete is NOT equivalent to privacy deletion.

---

# 130. Soft-Deleted Personal Data

A row with:

```text
deleted = true
```

still contains personal data.

---

# 131. Anonymization Alternative

Where records must remain for legitimate historical/business reasons, irreversible anonymization MAY be more appropriate than deletion.

---

# 132. Referential Integrity

Deletion/anonymization MUST preserve required referential integrity without retaining unnecessary identifiable data.

---

# 133. Cascading Delete

Database cascade deletion MUST be understood before using it for privacy workflows.

---

# 134. Asynchronous Deletion

Large deletion workflows MAY execute asynchronously but MUST be:

```text
Trackable

Retryable

Auditable

Idempotent
```

---

# 135. Deletion Failure

Partial deletion MUST be detectable.

---

# 136. Deletion Idempotency

Repeated deletion requests SHOULD safely converge to the same final state.

---

# 137. Data Subject Request

Systems subject to applicable privacy rights SHOULD support required operational processes such as:

```text
Access

Correction

Deletion

Anonymization

Portability
```

according to legal/business governance.

---

# 138. Identity Verification

Sensitive data-subject operations MUST verify requester identity according to organizational policy.

---

# 139. Data Export

Data exports MUST be treated as high-risk operations.

---

# 140. Export Authorization

Exports require explicit authorization.

---

# 141. Export Minimization

Exports MUST contain only required fields.

---

# 142. Export Format

CSV, XLSX, JSON and PDF exports can all contain sensitive data and require equivalent governance.

---

# 143. Temporary Export

Temporary export files MUST have controlled lifetime.

---

# 144. Export Storage

Sensitive exports MUST NOT remain indefinitely on:

```text
Local Disk

Shared Folder

Object Storage

Temporary Bucket
```

---

# 145. Signed URL

Temporary signed URLs SHOULD have appropriately short expiration.

---

# 146. Export Logging

Audit that an export occurred without logging the entire exported dataset.

---

# 147. Non-Production Environment

Production personal data SHOULD NOT be copied directly into development or test environments.

---

# 148. Production Dump

Raw production database dumps MUST NOT be distributed to developer workstations.

---

# 149. Synthetic Data

Synthetic test data SHOULD be preferred.

---

# 150. Masked Dataset

When realistic production-derived datasets are necessary, approved masking/anonymization MUST occur before use.

---

# 151. Reversible Masking

Reversible pseudonymization may still constitute personal data and requires appropriate controls.

---

# 152. Developer Laptop

Developer workstations MUST NOT become uncontrolled repositories of production personal data.

---

# 153. Test Fixtures

Automated test fixtures SHOULD use fictional data.

---

# 154. Real Customer Data in Tests

Real customer names, emails, telephone numbers and documents MUST NOT be embedded in source-controlled tests.

---

# 155. Screenshots

Screenshots used for support/documentation MUST be reviewed for sensitive information.

---

# 156. Support Ticket

Support tickets SHOULD avoid unnecessary sensitive payloads.

---

# 157. Chat and Collaboration Tools

Sensitive production data MUST NOT be casually copied into:

```text
Chat

Email

Wiki

Issue Tracker

Pull Request
```

---

# 158. Source Control

Personal data and secrets MUST NOT be committed to Git.

---

# 159. Git Deletion

Deleting a sensitive file in a later commit does not necessarily remove it from Git history.

---

# 160. Secret Exposure

A committed secret MUST be considered exposed and rotated according to incident procedures.

---

# 161. PII Exposure

Accidental committed personal data requires incident/remediation evaluation according to organizational policy.

---

# 162. Object Storage

Buckets containing sensitive data MUST follow:

```text
Encryption

Access Control

Retention

Lifecycle

Audit
```

requirements.

---

# 163. Public Bucket

Sensitive application data MUST NOT reside in publicly accessible buckets.

---

# 164. Presigned Access

Presigned access MUST be:

```text
Purpose-Bound

Time-Bounded

Authorized
```

---

# 165. Encryption in Transit

Sensitive information MUST use approved encrypted transport across untrusted or controlled network boundaries as required.

---

# 166. TLS

Production APIs MUST use TLS according to enterprise security standards.

---

# 167. Internal Traffic

Internal network location MUST NOT automatically be considered a sufficient reason to send restricted data unencrypted.

---

# 168. Encryption at Rest

Sensitive persistent stores MUST use approved encryption-at-rest controls.

---

# 169. Key Management

Cryptographic keys SHOULD use approved centralized key-management infrastructure.

---

# 170. Key Rotation

Key rotation MUST be supported according to enterprise cryptographic policy.

---

# 171. Key Access

Access to encryption keys MUST follow least privilege.

---

# 172. Key and Data Separation

Application data and its cryptographic master keys SHOULD NOT share identical access boundaries.

---

# 173. Secrets Manager

Secrets MUST use approved secret-management mechanisms rather than application configuration files committed to source.

---

# 174. Privileged Access

Administrative access to sensitive data MUST be restricted.

---

# 175. Least Privilege

Users/services MUST receive only the minimum access necessary.

---

# 176. Production Database Access

Direct human access to production databases SHOULD be exceptional.

---

# 177. Break Glass

Emergency privileged access SHOULD be:

```text
Controlled

Authenticated

Time-Bounded

Audited
```

---

# 178. Shared Account

Shared privileged accounts SHOULD be avoided.

---

# 179. Service Account

Service identities SHOULD be uniquely attributable to a workload or controlled workload group.

---

# 180. Access Review

Privileged access SHOULD be periodically reviewed.

---

# 181. Employee Departure

Access revocation processes MUST remove access no longer required.

---

# 182. Audit of Access

Sensitive administrative data access SHOULD itself be auditable.

---

# 183. Database Query Audit

Highly sensitive datasets MAY require audit of privileged query access according to infrastructure policy.

---

# 184. Search Endpoint

Search APIs over personal data MUST enforce:

```text
Authorization

Input Constraints

Result Limits

Data Minimization
```

---

# 185. Enumeration

APIs SHOULD prevent unauthorized bulk enumeration of personal records.

---

# 186. Pagination

Pagination is a performance feature, not an authorization control.

---

# 187. Rate Limiting

Sensitive enumeration-capable endpoints SHOULD consider appropriate rate controls.

---

# 188. Bulk API

Bulk retrieval APIs require stronger review because they increase exfiltration potential.

---

# 189. Report Generation

Reports containing personal/confidential data MUST follow the same authorization and retention requirements as APIs.

---

# 190. Notification

Email/SMS notifications SHOULD contain only necessary personal information.

---

# 191. Email

Email SHOULD NOT be treated as a secure general-purpose transport for restricted datasets.

---

# 192. Notification Logs

Notification systems MUST NOT log full sensitive message bodies unnecessarily.

---

# 193. Third-Party Processor

External processors receiving personal data MUST be explicitly approved according to organizational governance.

---

# 194. Third-Party Minimization

Only required data SHOULD be transmitted to external processors.

---

# 195. Third-Party Logging

Applications SHOULD understand whether external processors retain request/response payloads.

---

# 196. Cross-Border Processing

Cross-border data processing MUST follow applicable legal and enterprise requirements.

---

# 197. AI/ML Usage

Personal/confidential production data MUST NOT be submitted to unapproved AI/ML services.

---

# 198. AI Prompt

Prompts MUST follow the same classification controls as other data transfers.

---

# 199. Incident Detection

Potential privacy leakage SHOULD generate appropriate security incident handling.

---

# 200. Incident Evidence

Incident investigation MUST preserve sufficient evidence without unnecessarily replicating sensitive data.

---

# 201. Log Leak

Sensitive data discovered in logs requires evaluation of:

```text
Log Storage

Retention

Replicas

Exports

Access

Downstream Indexes
```

---

# 202. Credential Leak

Credentials discovered in logs MUST be rotated according to security policy.

---

# 203. Privacy by Design

New features MUST evaluate privacy during design, not only after implementation.

---

# 204. Privacy Review Trigger

Review SHOULD be triggered when a feature introduces:

```text
New Personal Data

New External Processor

New Data Export

New Long-Term Storage

New Cross-Border Transfer

New Sensitive Logging

New Analytics Use
```

---

# 205. Privacy Impact Assessment

High-risk processing MAY require a formal privacy impact assessment according to organizational/legal governance.

---

# 206. Schema Review

Database schema review SHOULD identify newly introduced personal/sensitive fields.

---

# 207. API Review

API review SHOULD identify newly exposed personal/sensitive fields.

---

# 208. Event Review

Event-contract review SHOULD identify newly propagated personal/sensitive fields.

---

# 209. Observability Review

Observability review SHOULD ensure new fields do not leak into:

```text
Logs

Metrics

Traces
```

---

# 210. DTO Review

DTO review SHOULD ask:

```text
Does this consumer
actually need this field?
```

---

# 211. Data Lineage

Critical sensitive-data flows SHOULD be traceable across systems.

Conceptually:

```text
CUSTOMER API
     |
     v
ORDER SERVICE
     |
     +--> POSTGRESQL
     |
     +--> SQS
     |
     +--> NOTIFICATION
     |
     +--> AUDIT
```

---

# 212. Data Lineage Purpose

Data lineage supports:

```text
Impact Analysis

Deletion

Incident Response

Retention

Compliance
```

---

# 213. Schema Metadata

Classification metadata MAY be maintained alongside API/data schemas where tooling supports it.

---

# 214. Automated Detection

Automated secret/PII detection MAY complement, but MUST NOT replace, architectural review.

---

# 215. False Positive

Automated privacy/security findings require contextual analysis.

---

# 216. False Negative

Absence of scanner findings does not prove absence of sensitive-data leakage.

---

# 217. SAST

SAST rules SHOULD detect relevant insecure data handling.

---

# 218. Secret Scanning

Repositories MUST use secret scanning where available.

---

# 219. Dependency Security

Libraries handling:

```text
Cryptography

Authentication

Serialization

File Processing
```

require appropriate supply-chain governance.

---

# 220. Test Coverage

Critical privacy transformations SHOULD have automated tests.

---

# 221. Masking Test

Masking tests SHOULD verify that sensitive values cannot be reconstructed from unintended output where the policy requires masking.

---

# 222. Logging Test

Critical sanitizers/logging boundaries SHOULD test that:

```text
Bearer tokens

Passwords

Secrets
```

are not emitted.

---

# 223. Legitimate Data Regression

Tests SHOULD ensure legitimate business data is preserved.

Example:

```java
assertThat(sanitized)
        .as("legitimate business ampersands must remain unchanged")
        .isEqualTo("M&M");
```

---

# 224. Deletion Test

Deletion/anonymization workflows SHOULD have integration tests.

---

# 225. Cache Deletion Test

Where applicable, tests SHOULD verify that deletion invalidates cached copies.

---

# 226. Audit Test

Critical business/security operations SHOULD verify that expected audit records are produced.

---

# 227. Audit Secret Test

Audit tests SHOULD verify that restricted values are not included.

---

# 228. Architecture Fitness Functions

Privacy controls SHOULD be automated where practical.

Examples:

```text
[ ] No JPA entity exposed by controller

[ ] No Authorization header logging

[ ] No known secret committed

[ ] Sensitive DTOs excluded from generic toString logging

[ ] Production data fixtures absent from tests

[ ] Audit integration exists for required operations

[ ] Retention configuration exists for applicable stores

[ ] Sensitive endpoints require authorization
```

---

# 229. CI Privacy Gate

CI SHOULD include applicable:

```text
SAST

Secret Scanning

Dependency Scanning

Architecture Tests

Security Tests
```

---

# 230. Generic `toString`

Objects containing sensitive fields SHOULD NOT rely on unrestricted generated `toString()` output when they may be logged.

---

# 231. Lombok `@Data`

Lombok-generated `toString()` MUST be reviewed for classes containing sensitive fields.

---

# 232. Record `toString`

Java records automatically expose component values through generated `toString()`.

Sensitive records MUST NOT be logged indiscriminately.

---

# 233. Exception Context

Exception messages SHOULD include diagnostic identifiers rather than sensitive payloads.

Prefer:

```text
Failed to process orderId=<id>
```

over dumping the complete request.

---

# 234. Remote Error

Errors received from external services MUST be filtered before logging or returning upstream.

---

# 235. Error Propagation

A downstream service may return sensitive content.

Upstream services MUST NOT assume downstream error messages are safe to expose.

---

# 236. Correlation

Operational diagnostics SHOULD use:

```text
traceId

correlationId

requestId
```

rather than full customer data.

---

# 237. Retention Configuration

Retention SHOULD be configuration-driven where appropriate but constrained by approved policy.

---

# 238. Runtime Extension

Operators MUST NOT arbitrarily extend sensitive-data retention beyond approved limits without governance.

---

# 239. Scheduled Cleanup

Retention policies requiring deletion SHOULD use automated cleanup processes.

---

# 240. Cleanup Job

Cleanup jobs MUST be:

```text
Observable

Retryable

Idempotent

Auditable
```

---

# 241. Cleanup Metrics

Cleanup SHOULD expose safe metrics such as:

```text
records_processed

records_deleted

records_failed
```

without exposing record identities as metric labels.

---

# 242. Cleanup Failure

Persistent cleanup failure MUST generate operational alerting.

---

# 243. Legal Hold

Legal/regulatory hold MAY override ordinary deletion policies when formally authorized.

---

# 244. Legal Hold Scope

Legal hold SHOULD apply only to required data and duration.

---

# 245. Legal Hold Audit

Activation/removal of legal holds MUST be auditable.

---

# 246. Data Restoration

Backup restoration procedures MUST preserve privacy/access controls.

---

# 247. Restored Expired Data

Restoration MUST consider data that would otherwise have expired under retention policy.

---

# 248. Restore Reconciliation

Post-restore processes MAY need to reapply deletion/anonymization state.

---

# 249. Disaster Recovery

DR replicas and backups are part of the data lifecycle and MUST follow equivalent privacy controls.

---

# 250. Environment Teardown

Temporary environments containing sensitive information MUST securely remove their data when destroyed.

---

# 251. Local Files

Applications SHOULD avoid writing sensitive temporary files unless required.

---

# 252. Temporary Directory

Sensitive temporary files MUST have:

```text
Restricted Permissions

Controlled Lifetime

Secure Cleanup
```

---

# 253. Heap Dump

Heap dumps MAY contain credentials and personal data.

---

# 254. Heap Dump Access

Production heap dumps MUST be treated as restricted artifacts.

---

# 255. Thread Dump

Thread dumps may contain request-related information and require controlled access.

---

# 256. Crash Dump

Crash diagnostics MUST follow secure artifact handling.

---

# 257. Support Bundle

Support bundles MUST filter secrets and sensitive payloads.

---

# 258. APM

APM/observability agents MUST be configured to prevent unintended capture of sensitive headers and payloads.

---

# 259. Automatic Instrumentation

Automatic instrumentation MUST be reviewed because it may capture:

```text
SQL

HTTP URLs

Headers

Parameters

Exceptions
```

---

# 260. URL Query Parameter

Sensitive values SHOULD NOT be placed in URL query strings when avoidable.

---

# 261. URL Logging

URLs are commonly captured by:

```text
Proxies

Access Logs

Browsers

APM
```

and therefore query parameters require careful design.

---

# 262. GET Sensitive Data

Highly sensitive request data SHOULD generally not be transported through GET query parameters.

---

# 263. Database Query Logging

SQL parameter logging MUST be controlled in production.

---

# 264. Hibernate Bind Logging

Verbose Hibernate bind-parameter logging SHOULD NOT be enabled indiscriminately in production.

---

# 265. ORM Entity Logging

Entities containing personal data MUST NOT be automatically logged.

---

# 266. Cache Debug Logging

Cache keys/values MUST follow privacy logging rules.

---

# 267. SQS Debug Logging

SQS producer/consumer debug logging MUST NOT expose full sensitive event payloads.

---

# 268. SQS Debug Logging

Queue debugging MUST NOT expose sensitive message bodies unnecessarily.

---

# 269. Data Privacy Review Checklist

Every material feature SHOULD evaluate:

```text
[ ] Does the feature introduce personal data?

[ ] What is the data classification?

[ ] Why is each field required?

[ ] Can any field be removed?

[ ] Which APIs expose the data?

[ ] Which services receive it?

[ ] Is it placed in SQS?

[ ] Is it cached?

[ ] Is it logged?

[ ] Is it added to traces?

[ ] Is it used as a metric label?

[ ] Where is it stored?

[ ] Is encryption required?

[ ] Who can access it?

[ ] What is the retention rule?

[ ] How is it deleted/anonymized?

[ ] Are backups considered?

[ ] Are DLQs considered?

[ ] Are exports considered?

[ ] Is non-production usage safe?

[ ] Are external processors involved?

[ ] Are tests using synthetic data?

[ ] Is audit required?

[ ] Are privileged operations audited?

[ ] Are new observability fields safe?
```

---

# 270. Secure Logging Checklist

```text
[ ] Authorization header excluded

[ ] Bearer token excluded/masked

[ ] Password excluded

[ ] API key excluded

[ ] Secret excluded

[ ] Personal data minimized

[ ] CR/LF injection controlled

[ ] Payload logging disabled where unnecessary

[ ] Error responses filtered

[ ] Downstream errors filtered

[ ] Legitimate business values preserved

[ ] Trace attributes reviewed

[ ] SQL parameter logging controlled
```

---

# 271. Data Deletion Checklist

```text
[ ] Primary database handled

[ ] Cache invalidated

[ ] Search/index handled

[ ] Derived tables handled

[ ] Event retention considered

[ ] Queue/DLQ considered

[ ] Object storage considered

[ ] Exports considered

[ ] Temporary files considered

[ ] Backups considered

[ ] Audit retention policy considered

[ ] Operation is idempotent

[ ] Partial failures are retryable

[ ] Completion is auditable
```

---

# 272. Enterprise Privacy Gate

A service is not considered compliant when applicable conditions include:

```text
[ ] Sensitive data classification is unknown

[ ] API exposes fields not required by consumer

[ ] JPA entities are returned directly

[ ] Authorization header can be logged

[ ] Bearer tokens can appear in logs

[ ] Passwords/secrets can appear in logs

[ ] Personal data is used as metric labels

[ ] Sensitive payloads are indiscriminately traced

[ ] Production data is copied directly into test/dev

[ ] Real customer data exists in source-controlled tests

[ ] SQS payload contains unnecessary personal data

[ ] Redis stores personal data indefinitely without justification

[ ] DLQ retention is ignored

[ ] Soft delete is treated as privacy deletion

[ ] Data deletion ignores caches or derived stores

[ ] Backups have no retention policy

[ ] Sensitive exports remain indefinitely

[ ] Shared privileged database accounts are uncontrolled

[ ] Generic sanitization corrupts legitimate business data

[ ] Security/audit logs expose restricted values

[ ] Retention defaults to forever without justification
```

---

# 273. Anti-Patterns

The following are prohibited or strongly discouraged:

- logging entire request/response payloads by default
- logging Authorization headers
- logging JWT/Bearer tokens
- logging passwords
- logging secrets
- using personal data as metric labels
- placing sensitive values in URLs unnecessarily
- returning stack traces to API consumers
- exposing JPA entities directly
- copying complete entities into events
- caching sensitive data indefinitely
- storing PII directly in cache keys
- assuming Redis is not a data store
- assuming SQS history is harmless
- ignoring DLQs during privacy analysis
- treating soft delete as real deletion
- retaining data forever by default
- copying production dumps to developer machines
- using real customer data in automated tests
- storing sensitive exports indefinitely
- generic HTML escaping as data-at-rest sanitization
- masking that destroys legitimate domain values
- assuming UUID prevents unauthorized access
- unrestricted production database access
- shared privileged accounts
- secrets committed to Git
- unreviewed automatic APM payload capture

---

# 274. Positive Consequences

The decision provides:

- stronger LGPD-oriented engineering controls
- reduced PII exposure
- safer APIs
- safer logs and observability
- controlled data retention
- stronger auditability
- safer Redis/SQS usage
- better deletion workflows
- reduced non-production exposure
- improved incident investigation
- clearer data ownership
- reduced accidental data propagation

---

# 275. Negative Consequences

The decision introduces:

- data classification work
- retention management
- deletion/anonymization workflows
- additional audit controls
- observability filtering
- export governance
- privacy-focused testing
- additional architecture review

These costs are accepted because data leakage and improper retention can create substantial legal, security and reputational risk.

---

# 276. Neutral Consequences

The decision also means:

- not every identifier is anonymous
- not every field requires encryption at application level
- not every log containing an identifier is automatically prohibited
- not every audit record should contain before/after payloads
- not every privacy requirement requires hard deletion
- pseudonymization is not the same as anonymization
- soft deletion is not actual deletion
- encryption does not eliminate authorization requirements
- compliance cannot be established solely through static analysis

---

# 277. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Personal-data leak in logs | Critical | Medium | Logging policy + masking |
| Unauthorized API access | Critical | Medium | Object-level authorization |
| Excessive event data | High | Medium | Event minimization |
| Cache retention leak | High | Medium | TTL + deletion workflow |
| Non-production exposure | Critical | Medium | Synthetic/masked data |
| Backup over-retention | High | Medium | Lifecycle policy |
| Sensitive export leakage | Critical | Medium | Authorization + expiration |
| Audit data leakage | High | Medium | Audit minimization |
| Incomplete deletion | High | Medium | Data lineage + idempotent workflow |
| Observability leakage | High | Medium | Logs/traces/metrics review |

---

# 278. Implementation Guidance

The following rules are mandatory:

1. Material data must have an understood classification.
2. Personal data must be minimized at collection and integration boundaries.
3. JPA entities must not be exposed directly as external API contracts.
4. APIs must enforce both authentication and appropriate authorization.
5. Restricted values must never be logged in plaintext.
6. Authorization headers, Bearer tokens, passwords and secrets must never be logged.
7. Logs, traces and metrics must be treated as data stores.
8. Personal data must not be used as metric labels.
9. Correlation identifiers should be preferred for diagnostics.
10. Security transformations must not corrupt legitimate business values.
11. Masking and output escaping must remain separate concerns.
12. PostgreSQL access must follow least privilege.
13. Sensitive persistent stores must use approved encryption controls.
14. Redis data must have appropriate retention/TTL.
15. Sensitive values should not appear directly in Redis keys.
16. SQS payloads must contain only necessary information.
17. SQS, SQS and DLQ retention must be included in privacy governance.
18. Audit records must be distinct from ordinary diagnostic logs.
19. Audit records must avoid unnecessary sensitive payload duplication.
20. Data categories must have explicit retention policies.
21. Soft delete must not be treated as privacy deletion.
22. Deletion/anonymization workflows must account for derived copies and caches.
23. Backups must have controlled retention and access.
24. Production data should not be directly copied into non-production environments.
25. Automated tests must use fictional/synthetic personal data.
26. Sensitive exports must be authorized, minimized and time-bounded.
27. Privileged production-data access must be controlled and auditable.
28. External processors must receive only necessary information.
29. Secret and PII scanning should complement architecture/security review.
30. Critical privacy controls must have automated regression tests.
31. Data lineage should be maintained for critical sensitive-data flows.
32. Cleanup/deletion jobs must be observable, retryable and idempotent.
33. Temporary files, heap dumps and support artifacts must follow secure-data handling.
34. APM/automatic instrumentation must be reviewed for unintended sensitive-data capture.
35. Privacy requirements must be evaluated during feature design.

---

# 279. Validation

This ADR will be validated through:

- architecture review
- API review
- data-schema review
- threat modeling
- privacy review
- Java 21
- Spring Boot Security
- PostgreSQL
- Redis
- SQS
- AWS SQS
- AWS KMS
- approved secret-management systems
- SAST
- secret scanning
- dependency scanning
- ArchUnit
- JUnit 5
- AssertJ
- integration tests
- audit tests
- deletion/anonymization tests
- CI/CD security gates

---

# 280. Success Criteria

The decision is successful when:

- data classification is known for material fields
- APIs expose only required information
- sensitive values no longer appear in logs
- observability uses correlation rather than PII
- legitimate business values remain unchanged by security controls
- SQS events contain minimal personal data
- Redis retention is bounded
- DLQs are included in retention policies
- non-production environments use synthetic or approved masked data
- audit trails provide accountability without unnecessary disclosure
- retention is explicitly defined
- deletion workflows account for distributed copies
- privileged access is controlled
- sensitive exports expire
- privacy-related regressions are detected automatically

---

# 281. Alternatives Rejected

## 281.1 Protect Only the Database

Rejected because sensitive information exists throughout APIs, caches, events, logs, traces, backups and exports.

---

## 281.2 Log Everything for Troubleshooting

Rejected because operational convenience does not justify uncontrolled data exposure.

---

## 281.3 Encrypt Everything at Application Level

Rejected as a universal strategy because encryption introduces search, indexing, key-management and operational complexity and must follow threat-model requirements.

---

## 281.4 Soft Delete as Privacy Deletion

Rejected because the identifiable data remains stored.

---

## 281.5 Production Data in Test Environments

Rejected because it unnecessarily expands the exposure surface.

---

## 281.6 Generic Sanitization of Every String

Rejected because context-insensitive transformations can corrupt valid business data without providing correct security.

---

# 282. Related Decisions

This ADR extends and implements:

- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-059: Enterprise Redis Caching, Distributed Cache and Data Consistency Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-064: Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard
- ADR-065: Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard
- ADR-067: Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-069: Enterprise Code Review, Refactoring, Technical Debt and Legacy Modernization Standard
- ADR-070: Enterprise Dependency Management, Gradle, Version Catalog, BOM, Library Governance and Java Supply Chain Standard

---

# 283. References

- Lei nº 13.709/2018 — Lei Geral de Proteção de Dados Pessoais (LGPD)
- ANPD guidance and applicable regulations
- GDPR — Regulation (EU) 2016/679
- OWASP Application Security Verification Standard
- OWASP Logging Cheat Sheet
- OWASP Cryptographic Storage Cheat Sheet
- OWASP REST Security Cheat Sheet
- NIST Privacy Framework
- NIST Cybersecurity Framework
- ISO/IEC 27001
- ISO/IEC 27701
- PostgreSQL Documentation
- Redis Security Documentation
- Amazon SQS Security Documentation
- AWS Security Documentation

---

# 284. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise privacy, PII and secure-data-handling baseline |

---

# 285. Decision Summary

The data lifecycle becomes:

```text
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
RETENTION
   |
   v
DELETE / ANONYMIZE
```

Sensitive-data propagation becomes:

```text
PERSONAL DATA
     |
     v
IS IT REQUIRED?
     |
  +--+--+
  |     |
 NO    YES
  |     |
  v     v
REMOVE  CLASSIFY
          |
          v
       PROTECT
          |
          v
       MINIMIZE
```

Observability becomes:

```text
REQUEST
   |
   v
TRACE ID
   |
   +--> LOG
   |
   +--> TRACE
   |
   +--> ERROR
```

rather than:

```text
REQUEST
   |
   v
FULL CUSTOMER PAYLOAD
   |
   +--> LOG
   +--> TRACE
   +--> APM
   +--> SUPPORT TOOL
```

Logging becomes:

```text
DIAGNOSTIC NEED
      |
      v
CAN CORRELATION ID
SOLVE IT?
      |
   +--+--+
   |     |
  YES    NO
   |     |
   v     v
USE ID   USE MINIMUM
         SAFE CONTEXT
```

Security transformation becomes:

```text
DATA
 |
 +--> LOG?
 |      |
 |      v
 |    MASK / SANITIZE
 |
 +--> HTML?
 |      |
 |      v
 |    OUTPUT ENCODE
 |
 +--> DATABASE?
        |
        v
      PRESERVE
      DOMAIN VALUE
```

Therefore:

```text
M&M
```

remains:

```text
M&M
```

in the domain/database rather than becoming:

```text
M&amp;M
```

Cache governance becomes:

```text
SOURCE OF TRUTH
      |
      v
    REDIS
      |
      +--> MINIMUM DATA
      +--> SAFE KEY
      +--> TTL
      +--> ACCESS CONTROL
      +--> EVICTION
```

Messaging governance becomes:

```text
DATABASE ENTITY
      |
      X
      |
DO NOT COPY
EVERY FIELD
      |
      v
DOMAIN EVENT
      |
      +--> REQUIRED FIELDS
      +--> MINIMUM PII
      +--> RETENTION
      +--> ACL
```

Deletion becomes:

```text
DELETE REQUEST
      |
      +--> DATABASE
      |
      +--> CACHE
      |
      +--> DERIVED DATA
      |
      +--> SEARCH
      |
      +--> OBJECT STORAGE
      |
      +--> EXPORTS
      |
      +--> QUEUES / DLQ
      |
      +--> BACKUP LIFECYCLE
      |
      v
COMPLETION AUDIT
```

and not merely:

```text
UPDATE CUSTOMER
SET DELETED = TRUE
```

Auditability becomes:

```text
WHO
 +
WHAT
 +
RESOURCE
 +
WHEN
 +
RESULT
 =
AUDIT EVENT
```

without:

```text
FULL SENSITIVE PAYLOAD
```

Non-production handling becomes:

```text
TEST / DEV
    |
    v
SYNTHETIC DATA
```

or, only when genuinely required:

```text
PRODUCTION-DERIVED DATA
        |
        v
APPROVED MASKING /
ANONYMIZATION
        |
        v
NON-PRODUCTION
```

The complete privacy equation is:

```text
DATA CLASSIFICATION
        +
MINIMIZATION
        +
PURPOSE LIMITATION
        +
LEAST PRIVILEGE
        +
AUTHORIZATION
        +
ENCRYPTION
        +
SAFE LOGGING
        +
SAFE OBSERVABILITY
        +
CACHE GOVERNANCE
        +
EVENT GOVERNANCE
        +
AUDITABILITY
        +
RETENTION
        +
DELETION / ANONYMIZATION
        +
BACKUP GOVERNANCE
        +
NON-PRODUCTION PROTECTION
        +
PRIVILEGED ACCESS CONTROL
        +
AUTOMATED SECURITY TESTING
        =
PRIVACY BY DESIGN
```

The governing principle is:

```text
Know what data you have.

Know why you have it.

Know where it goes.

Collect only what is needed.

Return only what is needed.

Send only what is needed.

Cache only what is justified.

Retain only as long
as required.

Do not log secrets.

Do not log tokens.

Do not use PII
as metric labels.

Do not assume traces
are private.

Do not assume Redis
is temporary and harmless.

Do not assume SQS
will forget.

Do not ignore DLQs.

Do not copy production
data casually into DEV.

Do not confuse masking
with escaping.

Do not mutate legitimate
business values in the name
of generic sanitization.

Do not confuse soft deletion
with actual deletion.

Design deletion across
every relevant copy.

Protect backups.

Control exports.

Audit privileged access.

Prefer correlation identifiers
over customer information
for diagnostics.

And treat personal data
as something the system
temporarily has responsibility
to protect,

not something it owns forever.
```

# ADR-079: Adopt Enterprise Notification, Email, SMS, Push and External Communication Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-079 |
| Title | Adopt Enterprise Notification, Email, SMS, Push and External Communication Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Notifications, Email, SMS, Push, Messaging, External Communication |
| Related Work Items | SQS, Kafka, Transactional Outbox, Templates, i18n, Notifications Service |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise applications frequently need to communicate with users and external stakeholders.

Typical channels include:

```text
EMAIL

SMS

PUSH NOTIFICATION

IN-APPLICATION NOTIFICATION

WEBHOOK

OPERATIONAL MESSAGE
```

Examples include:

```text
ORDER CREATED

ORDER APPROVED

ORDER REJECTED

PASSWORD / SECURITY NOTIFICATION

BILLING GENERATED

REPORT READY

FILE IMPORT COMPLETED

PROCESS FAILED

WORKFLOW ACTION REQUIRED
```

Sending a notification appears simple:

```text
BUSINESS OPERATION
       |
       v
SEND EMAIL
```

but this tightly couples business processing to an external communication provider.

Real providers can experience:

```text
TIMEOUT

RATE LIMIT

TEMPORARY OUTAGE

INVALID RECIPIENT

BOUNCE

DUPLICATE DELIVERY

DELAYED DELIVERY

PARTIAL DELIVERY

AUTHENTICATION FAILURE
```

Notification architecture must therefore separate:

```text
BUSINESS SUCCESS
```

from:

```text
COMMUNICATION DELIVERY
```

unless delivery itself is explicitly part of the business transaction.

---

# 2. Problem Statement

The organization requires standards covering:

- notification architecture
- email
- SMS
- push notifications
- in-app notifications
- templates
- internationalization
- recipients
- CC/BCC
- asynchronous delivery
- SQS
- Kafka
- Transactional Outbox
- retry
- idempotency
- deduplication
- provider integration
- provider failover
- rate limiting
- delivery status
- bounce handling
- complaints
- invalid recipients
- attachments
- PII
- audit
- notification preferences
- opt-out
- transactional messages
- bulk notifications
- correlation
- observability
- security

---

# 3. Decision Drivers

Primary drivers are:

1. business transaction isolation
2. delivery reliability
3. duplicate prevention
4. provider resilience
5. scalability
6. auditability
7. privacy
8. user preferences
9. operational visibility
10. localization
11. maintainability
12. provider independence

---

# 4. Decision

Business services SHOULD NOT synchronously depend on external notification providers for ordinary notifications.

The preferred architecture is:

```text
BUSINESS SERVICE
       |
       v
BUSINESS TRANSACTION
       |
       v
OUTBOX / EVENT
       |
       v
MESSAGE BROKER
       |
       v
NOTIFICATION SERVICE
       |
       v
PROVIDER
```

---

# 5. Fundamental Principle

```text
Business completion
and notification delivery
are different concerns.

Persist business state first.

Publish reliably.

Deliver asynchronously.

Retry safely.

Prevent duplicates.

Observe final delivery state.
```

---

# 6. Notification Classification

Notifications SHOULD be classified.

Suggested categories:

```text
TRANSACTIONAL

SECURITY

WORKFLOW

OPERATIONAL

MARKETING

BULK

SYSTEM-TO-SYSTEM
```

---

# 7. Transactional Notification

Transactional notifications result from a business event.

Examples:

```text
Order approved

Invoice generated

Report ready
```

---

# 8. Marketing Notification

Marketing communication has different consent and opt-out requirements.

It MUST NOT be treated identically to mandatory transactional communication.

---

# 9. Security Notification

Security-sensitive communication MAY require stronger delivery, privacy and audit controls.

---

# 10. Business Transaction

Ordinary notification failure MUST NOT roll back an already valid business transaction.

---

# 11. Anti-Pattern

Avoid:

```text
BEGIN TRANSACTION

SAVE ORDER

CALL EMAIL PROVIDER

EMAIL TIMEOUT

ROLLBACK ORDER
```

when email delivery is not part of the core consistency boundary.

---

# 12. Preferred Flow

```text
BEGIN TRANSACTION
      |
      +--> SAVE ORDER
      |
      +--> SAVE OUTBOX EVENT
      |
COMMIT
      |
      v
ASYNC DELIVERY
```

---

# 13. Transactional Outbox

When notification creation must be atomically associated with a business transaction, Transactional Outbox SHOULD be used.

---

# 14. Dual Write

This pattern is prohibited:

```text
SAVE DATABASE
      |
      v
PUBLISH MESSAGE
```

as two unrelated operations when losing the notification event is unacceptable.

---

# 15. Failure Window

Without Outbox:

```text
DATABASE COMMIT
      |
      X
PROCESS CRASH
      |
MESSAGE NEVER PUBLISHED
```

---

# 16. Event Ownership

The business service SHOULD publish a business event rather than know provider-specific email mechanics.

---

# 17. Example

Prefer:

```text
ORDER_APPROVED
```

over:

```text
SEND_SES_EMAIL_TEMPLATE_17
```

for domain integration.

---

# 18. Notification Service

A notification service SHOULD centralize cross-cutting delivery concerns when multiple services require external communication.

---

# 19. Notification Responsibilities

The notification component MAY own:

```text
Template Resolution

Recipient Resolution

Channel Selection

Provider Integration

Retry

Delivery Status

Deduplication

Preference Evaluation

Rate Limiting
```

---

# 20. Domain Responsibility

Business services remain responsible for deciding:

```text
WHAT HAPPENED
```

and relevant business context.

Notification infrastructure determines:

```text
HOW TO COMMUNICATE IT
```

---

# 21. Event Contract

Notification-triggering events MUST use explicit versioned contracts.

---

# 22. Event Identity

Every event MUST have a stable unique identifier.

Example:

```text
eventId
```

---

# 23. Correlation

Events SHOULD carry applicable:

```text
eventId

traceId

correlationId

aggregateId

occurredAt
```

---

# 24. Notification Identity

Every logical notification SHOULD receive a unique internal:

```text
notificationId
```

---

# 25. Event ID vs Notification ID

One event MAY generate multiple notifications.

Example:

```text
ORDER_APPROVED EVENT
       |
       +--> EMAIL
       |
       +--> PUSH
```

Therefore:

```text
eventId != notificationId
```

---

# 26. Delivery Identity

A notification MAY have multiple delivery attempts.

Conceptually:

```text
NOTIFICATION
     |
     +--> ATTEMPT 1
     |
     +--> ATTEMPT 2
     |
     +--> ATTEMPT 3
```

---

# 27. Idempotency

Notification consumers MUST be idempotent.

---

# 28. At-Least-Once Delivery

Kafka/SQS delivery MUST be assumed to be at least once unless stronger guarantees are explicitly established.

---

# 29. Duplicate Message

The same event may therefore arrive more than once.

---

# 30. Duplicate Event

Duplicate event consumption MUST NOT automatically create duplicate user communication.

---

# 31. Deduplication Key

A logical deduplication key SHOULD be defined.

Possible dimensions:

```text
eventId

recipient

channel

template

businessAction
```

---

# 32. Example

Conceptually:

```text
eventId
+
recipient
+
channel
```

may identify one logical delivery.

---

# 33. Database Constraint

Critical duplicate prevention SHOULD use durable database uniqueness where appropriate.

---

# 34. In-Memory Deduplication

In-memory deduplication alone is insufficient.

---

# 35. Pod Restart

Pod-local deduplication state disappears after restart.

---

# 36. Notification Status

Suggested logical states:

```text
PENDING

QUEUED

SENDING

SENT

DELIVERED

FAILED

BOUNCED

REJECTED

CANCELLED
```

Not every provider supports every state.

---

# 37. SENT

`SENT` means the provider accepted the message.

It does NOT necessarily mean the recipient received it.

---

# 38. DELIVERED

`DELIVERED` SHOULD only be used when provider feedback confirms delivery according to that channel's semantics.

---

# 39. FAILED

Permanent delivery failure SHOULD eventually transition to a terminal failure state.

---

# 40. Retry

Transient provider failures SHOULD be retried.

---

# 41. Retryable Failure

Examples MAY include:

```text
Timeout

Connection Failure

HTTP 429

HTTP 503

Temporary Provider Error
```

---

# 42. Non-Retryable Failure

Examples MAY include:

```text
Invalid Email Address

Unsupported Recipient

Invalid Template

Malformed Request

Permanent Provider Rejection
```

---

# 43. Retry Classification

Retry MUST be based on failure classification rather than retrying every exception.

---

# 44. Retry Bound

Retries MUST be bounded.

---

# 45. Backoff

Retries SHOULD use exponential backoff with jitter where appropriate.

---

# 46. Immediate Retry Storm

This is prohibited:

```text
PROVIDER DOWN
    |
    v
100,000 MESSAGES
    |
    v
IMMEDIATE RETRY LOOP
```

---

# 47. Retry Scheduling

Delayed retry SHOULD avoid blocking worker threads.

---

# 48. SQS

SQS MAY be used for notification work queues.

---

# 49. SQS Visibility

Visibility timeout MUST account for maximum expected processing time.

---

# 50. SQS Delete

A message MUST NOT be deleted before the required notification state is durably recorded.

---

# 51. SQS Redelivery

Redelivery MUST remain safe through idempotency.

---

# 52. DLQ

Messages exceeding retry policy SHOULD move to a DLQ or equivalent failure mechanism.

---

# 53. DLQ Is Not Disposal

DLQ messages MUST remain operationally visible.

---

# 54. DLQ Reprocessing

A controlled replay process MUST exist for recoverable DLQ messages.

---

# 55. Kafka

Kafka MAY be used when notification triggering is based on reusable domain/integration events.

---

# 56. Kafka Consumer

Consumer offset handling MUST align with durable notification persistence.

---

# 57. Safe Consumer Flow

Prefer:

```text
RECEIVE EVENT
     |
     v
PERSIST / DEDUP NOTIFICATION
     |
     v
COMMIT CONSUMPTION
```

according to the selected messaging semantics.

---

# 58. Provider Abstraction

Provider-specific SDK/API details SHOULD be isolated behind an application abstraction.

---

# 59. Example

Conceptually:

```java
interface EmailProvider {
    DeliveryResult send(EmailMessage message);
}
```

---

# 60. Domain Provider Leakage

Domain services SHOULD NOT contain:

```text
SES API

SMTP DETAILS

TWILIO DETAILS

PUSH PROVIDER DETAILS
```

---

# 61. Provider Independence

Provider abstraction SHOULD allow controlled replacement without rewriting domain workflows.

---

# 62. Provider Failover

Provider failover MAY be implemented for critical communication.

---

# 63. Failover Is Not Automatic Requirement

A second provider adds:

```text
Cost

Complexity

Template Differences

Delivery Semantics

Operational Complexity
```

and SHOULD only be introduced where availability requirements justify it.

---

# 64. Failover Duplicate Risk

Provider failover can cause duplicates.

Example:

```text
PROVIDER A TIMEOUT
       |
       v
UNKNOWN WHETHER SENT
       |
       v
SEND THROUGH PROVIDER B
```

The recipient may receive both.

---

# 65. Ambiguous Result

Provider timeout MUST be treated as potentially ambiguous when the provider may have accepted the request before the timeout.

---

# 66. Provider Idempotency Key

Provider-native idempotency keys SHOULD be used when available.

---

# 67. Provider Message ID

Provider message identifiers SHOULD be persisted when useful for reconciliation.

---

# 68. Rate Limiting

Notification delivery MUST respect provider quotas.

---

# 69. Local Rate Limiting

Local rate limiting MAY protect:

```text
Provider Quota

Application

Recipient

Domain Reputation
```

---

# 70. Burst Control

Large event bursts SHOULD be smoothed through queueing and bounded worker concurrency.

---

# 71. Backpressure

If provider capacity is lower than incoming rate:

```text
QUEUE DEPTH
```

should increase in a controlled manner rather than creating unlimited concurrent calls.

---

# 72. Worker Concurrency

Notification worker concurrency MUST be bounded.

---

# 73. Virtual Threads

Virtual Threads MAY be used for I/O-heavy provider calls.

---

# 74. Virtual Thread Limitation

Virtual Threads MUST NOT bypass provider rate limits or connection limits.

---

# 75. Recipient

Recipient addresses/numbers MUST be validated according to channel requirements.

---

# 76. Email Validation

Email syntax validation SHOULD be reasonable but MUST NOT attempt to prove mailbox existence through unreliable heuristics.

---

# 77. Normalization

Recipient normalization MUST preserve valid semantics.

---

# 78. Case Handling

Email local-part normalization SHOULD be conservative.

---

# 79. SMS Number

Phone numbers SHOULD use a normalized international representation where appropriate.

---

# 80. Recipient Source

Recipient source MUST be explicit.

Possible sources:

```text
Business Event

Customer Record

User Service

Notification Preference

Configuration
```

---

# 81. Recipient Lookup

Avoid unnecessary extra service calls when the required recipient data is already safely available in the business/event contract.

---

# 82. Snapshot Recipient

For asynchronous notification, relevant recipient data MAY be snapshotted into the notification request/event when business semantics require the address effective at event time.

---

# 83. Dynamic Recipient

Alternatively, recipient MAY be resolved at send time when latest contact data is required.

---

# 84. Snapshot vs Dynamic

The choice MUST be explicit.

---

# 85. CC/BCC

CC and BCC MUST follow the same authorization/privacy controls as primary recipients.

---

# 86. Bulk Recipient Leakage

Bulk emails MUST NOT expose unrelated recipients through `To` or `CC`.

---

# 87. BCC

BCC MAY prevent recipient disclosure for small appropriate use cases, but large campaigns SHOULD use provider-native bulk delivery capabilities.

---

# 88. Template

Notification content SHOULD use centrally managed templates.

---

# 89. Template ID

Templates SHOULD have stable identifiers.

Example:

```text
ORDER_APPROVED
```

---

# 90. Template Version

Material template changes MAY use explicit versioning.

---

# 91. Template Logic

Templates SHOULD contain presentation logic, not complex business logic.

---

# 92. Business Rule in Template

This SHOULD be avoided:

```text
IF order total > X
AND segment = Y
AND user profile = Z
THEN ...
```

Business decisions belong upstream.

---

# 93. Template Variables

Template variables MUST have an explicit contract.

---

# 94. Missing Variable

Missing required variables MUST fail deterministically.

---

# 95. Template Injection

Template engines MUST escape or safely handle untrusted values according to output context.

---

# 96. HTML Email

Untrusted data inserted into HTML email MUST be safely encoded for HTML context.

---

# 97. URL

Untrusted values inserted into URLs require URL-context handling.

---

# 98. No Generic Mutation

Security encoding MUST occur at the appropriate output boundary.

Domain values such as:

```text
M&M
```

MUST NOT be permanently mutated to:

```text
M&amp;M
```

inside domain persistence or API data solely for later HTML rendering.

---

# 99. Plain Text

Important emails SHOULD consider a plain-text alternative where appropriate.

---

# 100. Localization

Templates SHOULD support i18n where required.

---

# 101. Locale Resolution

Locale resolution MUST have a defined source.

Examples:

```text
User Preference

Customer Configuration

Business Region

Explicit Event Field
```

---

# 102. Default Locale

A deterministic fallback locale MUST exist.

---

# 103. Time Zone in Template

Dates/times presented to users MUST use the appropriate business/user time zone.

---

# 104. Money Formatting

Monetary values MUST use explicit currency and locale-aware formatting.

---

# 105. Template Rendering Time

Rendering SHOULD occur as close to delivery as practical when template updates/preferences should affect queued notifications.

---

# 106. Immutable Historical Content

Where legal/audit requirements require exact historical content, rendered content or template version/data MUST be retained appropriately.

---

# 107. Subject

Email subjects MUST be bounded.

---

# 108. Header Injection

Recipient names, subject and other header values MUST be protected against CRLF/header injection.

---

# 109. Attachments

Attachments SHOULD be avoided when a secure short-lived download link provides a better architecture.

---

# 110. Large Attachment

Large files MUST NOT normally be embedded directly in email.

---

# 111. Attachment Limit

Attachment size MUST be bounded according to provider and business requirements.

---

# 112. Attachment Security

Attachments MUST follow ADR-078 file-security controls.

---

# 113. Sensitive Attachment

Sensitive reports SHOULD generally use authenticated application download rather than unrestricted email attachment.

---

# 114. Object Storage Link

A short-lived authorized download mechanism SHOULD be preferred for large/sensitive artifacts.

---

# 115. PII

Notification payloads frequently contain PII.

---

# 116. Data Minimization

Only information necessary for communication MUST be included.

---

# 117. Queue Payload

Sensitive data SHOULD NOT be unnecessarily copied into broker messages.

---

# 118. Logs

Email addresses, phone numbers and message bodies MUST follow privacy-safe logging policy.

---

# 119. Message Body Logging

Full notification bodies MUST NOT be logged by default.

---

# 120. Provider Error Logging

Provider errors MUST be sanitized before logging.

---

# 121. Secrets

Provider credentials/API keys MUST use approved secret management.

---

# 122. Credentials in Code

Provider credentials MUST NOT be hardcoded.

---

# 123. Credentials in Event

Provider credentials MUST NEVER appear in notification events.

---

# 124. Encryption

Communication with external providers MUST use secure transport.

---

# 125. Notification Preferences

User/customer notification preferences SHOULD be centrally evaluated where required.

---

# 126. Preference Dimensions

Preferences MAY include:

```text
Channel

Notification Type

Language

Quiet Hours

Opt-In / Opt-Out
```

---

# 127. Transactional Override

Mandatory transactional/security notifications MAY override optional communication preferences where legally and contractually appropriate.

---

# 128. Marketing Consent

Marketing communication MUST respect applicable consent/opt-out requirements.

---

# 129. Unsubscribe

Where required, unsubscribe mechanisms MUST be functional and promptly applied.

---

# 130. Preference Race

Queued optional notifications SHOULD consider whether preferences are evaluated:

```text
AT EVENT TIME
```

or:

```text
AT DELIVERY TIME
```

---

# 131. Delivery-Time Preference

Delivery-time evaluation is generally preferable for optional communications when the latest opt-out must be respected.

---

# 132. Bounce

Email bounce feedback SHOULD be consumed when the provider supports it.

---

# 133. Hard Bounce

Hard-bounced addresses SHOULD be suppressed according to policy.

---

# 134. Soft Bounce

Soft bounce MAY be retried according to bounded policy.

---

# 135. Complaint

Provider complaint/spam feedback MUST be operationally handled.

---

# 136. Suppression List

A suppression mechanism SHOULD prevent repeated delivery to known permanently invalid/rejected recipients where appropriate.

---

# 137. Suppression Scope

Suppression MUST distinguish applicable:

```text
Channel

Address

Notification Category

Reason
```

---

# 138. Provider Webhook

Provider delivery callbacks/webhooks MUST be authenticated/verified.

---

# 139. Webhook Idempotency

Provider callbacks MUST be idempotently processed.

---

# 140. Callback Ordering

Callbacks may arrive out of order.

---

# 141. Status Regression

A late callback MUST NOT incorrectly regress a terminal delivery state.

---

# 142. Example

If:

```text
DELIVERED
```

has already been confirmed, an older delayed:

```text
SENT
```

event MUST NOT downgrade it.

---

# 143. Provider Event Time

Provider event timestamps SHOULD participate in status reconciliation.

---

# 144. Delivery Reconciliation

Critical notification channels SHOULD periodically reconcile unresolved/ambiguous deliveries where provider APIs support it.

---

# 145. Bulk Notification

Bulk notifications require explicit architecture.

---

# 146. One Million Recipients

A request for one million recipients MUST NOT create one synchronous HTTP operation.

---

# 147. Bulk Flow

Preferred:

```text
CREATE CAMPAIGN/JOB
       |
       v
PARTITION RECIPIENTS
       |
       v
QUEUE
       |
       v
BOUNDED WORKERS
       |
       v
PROVIDER
```

---

# 148. Bulk Partitioning

Large recipient sets SHOULD be partitioned into bounded work units.

---

# 149. Bulk Idempotency

Each recipient/channel delivery MUST remain independently idempotent.

---

# 150. Bulk Cancellation

Long-running campaigns MAY support cancellation.

---

# 151. Cancellation Semantics

Cancellation stops unsent work but cannot necessarily recall messages already accepted by providers.

---

# 152. Scheduling

Future notifications MAY be scheduled.

---

# 153. Scheduled Notification

Scheduled notifications SHOULD use durable scheduling state.

---

# 154. In-Memory Timer

Long-term scheduled notifications MUST NOT rely solely on in-memory timers.

---

# 155. Quiet Hours

Optional communication MAY respect configured quiet hours.

---

# 156. Time Zone

Quiet-hour calculation MUST use the recipient's configured/business time zone.

---

# 157. Notification Expiration

Some notifications become useless after a deadline.

---

# 158. TTL

Notifications SHOULD support expiration when late delivery has no value.

Example:

```text
OTP-like notification
```

or:

```text
temporary workflow reminder
```

according to business semantics.

---

# 159. Expired Notification

Expired notifications MUST NOT be retried indefinitely.

---

# 160. Priority

Priority MAY distinguish urgent transactional work from bulk/low-priority communication.

---

# 161. Priority Starvation

High-priority queues MUST NOT permanently starve normal required communication.

---

# 162. Separate Queue

Distinct workload classes MAY use separate queues when isolation is required.

---

# 163. Bulkhead

Bulk marketing/bulk notifications SHOULD NOT exhaust the same capacity required for critical security/transactional messages.

---

# 164. Circuit Breaker

Provider clients SHOULD use appropriate Circuit Breaker behavior.

---

# 165. Circuit Open

When provider Circuit Breaker opens:

```text
DO NOT DROP MESSAGE
```

The notification should remain retryable according to policy.

---

# 166. Timeout

Provider calls MUST have bounded:

```text
Connect Timeout

Response Timeout

Overall Deadline
```

---

# 167. Long Provider Timeout

Worker throughput can collapse if provider calls wait excessively.

---

# 168. Connection Pool

Provider HTTP connection pools MUST be bounded.

---

# 169. Resilience Layering

Retry configuration MUST avoid multiplication across:

```text
HTTP CLIENT

RESILIENCE4J

QUEUE

APPLICATION WORKER
```

---

# 170. Retry Multiplication

Example:

```text
QUEUE ATTEMPTS = 5

x CLIENT RETRIES = 3

= UP TO 15 PROVIDER CALLS
```

before other retry layers.

This MUST be intentionally controlled.

---

# 171. Observability

Notification systems MUST provide end-to-end observability.

---

# 172. Metrics

Useful metrics include:

```text
notifications_created

notifications_sent

notifications_delivered

notifications_failed

notifications_bounced

notifications_retried

notification_duration

queue_depth

oldest_message_age
```

---

# 173. Metric Dimensions

Bounded dimensions MAY include:

```text
channel

notification_type

provider

result
```

---

# 174. Recipient Metric

Email addresses and phone numbers MUST NOT be metric labels.

---

# 175. Notification ID Metric

`notificationId` MUST NOT be a metric label.

---

# 176. Logs

Logs SHOULD include safe:

```text
notificationId

eventId

channel

templateId

provider

attempt

result

elapsedMs
```

---

# 177. Recipient Logging

Recipient values SHOULD be masked or omitted according to privacy requirements.

---

# 178. Correlation

Notification processing SHOULD preserve trace/correlation context where practical.

---

# 179. Async Trace

Asynchronous notification tracing SHOULD link:

```text
BUSINESS EVENT
     |
     v
BROKER
     |
     v
NOTIFICATION
     |
     v
PROVIDER
```

---

# 180. Provider Trace

External provider calls SHOULD create client spans when distributed tracing is enabled.

---

# 181. Alerting

Critical alerts SHOULD include:

```text
Queue Backlog

Oldest Message Too Old

Failure Rate

Provider Outage

DLQ Growth

Bounce Surge

Complaint Surge

Delivery Latency

Zero Worker Throughput
```

---

# 182. Delivery SLO

Critical notification categories SHOULD define delivery objectives.

Example:

```text
99% of transactional emails
accepted by provider
within 5 minutes.
```

---

# 183. Provider Acceptance vs Delivery SLO

Acceptance and actual delivery MUST be distinguished.

---

# 184. Backlog SLI

Oldest queued-message age is an important notification-system SLI.

---

# 185. Testing Strategy

Notification architecture requires tests beyond verifying an email client was called.

---

# 186. Template Test

Templates SHOULD verify:

```text
Required Variables

Rendering

Escaping

Locale

Subject
```

---

# 187. M&M Test

Values containing characters such as:

```text
M&M
```

SHOULD verify domain data remains unchanged while rendered HTML is safely encoded at the presentation boundary.

---

# 188. Consumer Idempotency Test

Consume the same event twice and verify only one logical notification is created.

---

# 189. Delivery Idempotency Test

Retry the same logical delivery and verify duplicate prevention behavior.

---

# 190. Retry Test

Transient provider failures SHOULD verify bounded retry.

---

# 191. Permanent Failure Test

Permanent failures MUST not enter infinite retry.

---

# 192. Circuit Breaker Test

Provider outage SHOULD verify messages remain recoverable.

---

# 193. DLQ Test

Exhausted delivery attempts SHOULD reach the configured failure path.

---

# 194. Replay Test

DLQ replay SHOULD verify idempotency.

---

# 195. Provider Timeout Test

Timeout behavior MUST be tested.

---

# 196. Rate Limit Test

HTTP 429/provider quota behavior SHOULD be tested.

---

# 197. Concurrency Test

Worker concurrency MUST be verified as bounded.

---

# 198. Preference Test

Optional notifications SHOULD test opt-out/preference behavior.

---

# 199. Locale Test

Templates SHOULD test supported locale resolution and fallback.

---

# 200. Attachment Test

Attachments MUST test size/type/security controls.

---

# 201. Callback Test

Provider callbacks SHOULD test:

```text
Valid Signature

Invalid Signature

Duplicate Callback

Out-of-Order Callback
```

---

# 202. Bounce Test

Hard/soft bounce policies SHOULD have tests.

---

# 203. Bulk Test

Bulk processing SHOULD test bounded partition/concurrency behavior.

---

# 204. Shutdown Test

Notification workers SHOULD stop intake and preserve retryable work during graceful shutdown.

---

# 205. Testcontainers

Messaging/database integration SHOULD use Testcontainers where applicable.

---

# 206. AssertJ

Java tests MUST follow established quality conventions, including meaningful:

```java
.as("...")
```

descriptions before applicable assertions.

---

# 207. Notification Architecture Review Checklist

```text
[ ] Is notification delivery actually required synchronously?

[ ] Is business state committed independently?

[ ] Is Transactional Outbox required?

[ ] Is the event contract explicit?

[ ] Does the event have eventId?

[ ] Is notificationId distinct from eventId?

[ ] Is consumption idempotent?

[ ] Can duplicate events create duplicate emails?

[ ] What is the deduplication key?

[ ] Are retries bounded?

[ ] Which failures are retryable?

[ ] Is DLQ/recovery defined?

[ ] Is provider rate limiting respected?

[ ] Is worker concurrency bounded?

[ ] Are provider timeouts bounded?

[ ] Could retry layers multiply?

[ ] Are recipient semantics explicit?

[ ] Is recipient resolved at event time or delivery time?

[ ] Are templates versioned where needed?

[ ] Are untrusted values escaped at output boundary?

[ ] Is localization supported?

[ ] Are attachments really necessary?

[ ] Are preferences/opt-outs evaluated?

[ ] Are bounce/complaint callbacks processed?

[ ] Are callbacks authenticated and idempotent?

[ ] Is PII minimized?

[ ] Are message bodies excluded from logs?

[ ] Is backlog monitored?

[ ] Is delivery latency measurable?

[ ] Can failed notifications be safely replayed?
```

---

# 208. Notification Fitness Functions

Stable controls SHOULD be automated where practical.

Examples:

```text
[ ] Notification consumers have idempotency tests

[ ] Provider clients have bounded timeout

[ ] Retry attempts are bounded

[ ] DLQ configured for critical queues

[ ] Recipient data excluded from metric labels

[ ] Full notification body excluded from logs

[ ] Templates have rendering tests

[ ] Provider credentials come from secret management

[ ] Optional notifications evaluate preferences

[ ] Callback signatures are validated

[ ] Worker concurrency is configured

[ ] Critical queue age has alerting
```

---

# 209. Enterprise Notification Gate

A notification implementation is not considered compliant when applicable conditions include:

```text
[ ] Business transaction depends synchronously on ordinary email delivery

[ ] Database commit and event publication use unsafe dual write

[ ] Duplicate event can generate duplicate communication

[ ] Deduplication exists only in memory

[ ] Provider retry is unbounded

[ ] Queue retry and HTTP retry multiply uncontrollably

[ ] Provider timeout is unbounded

[ ] Provider quota is ignored

[ ] One event can create unlimited concurrent provider calls

[ ] Provider credentials are hardcoded

[ ] Message body containing PII is logged

[ ] Recipient email/phone appears in metric labels

[ ] HTML encoding mutates domain data

[ ] Bulk email exposes recipients through To/CC

[ ] Sensitive large attachment is sent when secure download is required

[ ] Marketing opt-out is ignored

[ ] Provider callback is unauthenticated

[ ] Duplicate callback corrupts delivery state

[ ] DLQ exists but nobody monitors/reprocesses it

[ ] Notification backlog cannot be measured
```

---

# 210. Anti-Patterns

The following are prohibited or strongly discouraged:

- synchronous provider call inside ordinary business transaction
- database + broker dual write without reliability strategy
- duplicate notification on message redelivery
- in-memory-only deduplication
- retry every exception
- unlimited retry
- provider-specific code throughout domain services
- unbounded worker concurrency
- ignoring provider rate limits
- retry multiplication
- giant bulk-send request
- raw template concatenation
- storing HTML-escaped values in domain data
- logging complete notification content
- PII in metrics
- hardcoded provider credentials
- unrestricted attachments
- using email as secure file storage
- ignoring bounce/complaint feedback
- unauthenticated provider callbacks
- treating `SENT` as guaranteed delivery
- permanent DLQ without operational ownership

---

# 211. Positive Consequences

The decision provides:

- business/provider isolation
- reliable asynchronous communication
- duplicate prevention
- controlled retries
- provider independence
- scalable bulk delivery
- stronger privacy
- centralized template governance
- better localization
- safer attachment handling
- observable delivery lifecycle
- controlled recovery and replay

---

# 212. Negative Consequences

The decision introduces:

- asynchronous architecture
- notification persistence
- queue infrastructure
- template management
- callback processing
- deduplication state
- operational DLQ management
- additional monitoring

These costs are accepted because external communication is inherently failure-prone and must not compromise core business consistency.

---

# 213. Neutral Consequences

The decision also means:

- business success does not imply notification delivery
- provider acceptance does not imply recipient delivery
- some notifications may arrive later than the business transaction
- retries can legitimately produce ambiguous provider outcomes
- not every system requires provider failover
- not every notification requires an attachment
- transactional and marketing communication have different rules
- the latest user preference may affect queued optional communication

---

# 214. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Duplicate communication | High | Medium | Durable idempotency |
| Lost notification | High | Medium | Transactional Outbox |
| Provider outage | High | Medium | Queue + retry + CB |
| Retry storm | High | Medium | Backoff + jitter |
| Rate-limit breach | High | Medium | Bounded concurrency |
| PII exposure | Critical | Medium | Data minimization |
| Invalid recipient reputation damage | High | Medium | Bounce suppression |
| Callback spoofing | Critical | Low/Medium | Signature validation |
| Queue backlog | High | Medium | Capacity + alerts |
| Bulk workload starvation | High | Medium | Workload isolation |

---

# 215. Implementation Guidance

The following rules are mandatory:

1. Ordinary notifications must not control core business transaction success.
2. Business-state/event dual writes must use a reliability strategy such as Transactional Outbox where required.
3. Notification-triggering events must have stable identities.
4. Notification processing must assume at-least-once delivery.
5. Consumers must be idempotent.
6. Critical duplicate prevention should use durable uniqueness.
7. Event, notification and delivery-attempt identities must remain conceptually separate.
8. Provider calls must have bounded timeouts.
9. Retry must distinguish transient and permanent failures.
10. Retry attempts must be bounded and use appropriate backoff.
11. Queue, resilience and HTTP retry layers must not multiply unintentionally.
12. Provider throughput/rate limits must be respected.
13. Worker concurrency must be bounded.
14. Provider-specific code must be isolated.
15. Provider failover must account for ambiguous duplicate delivery.
16. Recipient resolution semantics must be explicit.
17. Templates must have explicit variable contracts.
18. Output-context encoding must occur at rendering boundaries and must not mutate domain data.
19. Localization and time-zone behavior must be deterministic.
20. Sensitive/large attachments should use secure file-download architecture.
21. Notification payloads must minimize PII.
22. Full notification content must not be logged by default.
23. Provider credentials must use approved secret management.
24. Optional communication must respect applicable user/customer preferences.
25. Marketing communication must respect consent and opt-out requirements.
26. Provider callbacks must be authenticated and idempotent.
27. Delivery state transitions must tolerate duplicate/out-of-order callbacks.
28. Bounce/complaint feedback must be operationally processed where supported.
29. Critical queues must have DLQ/recovery procedures.
30. Bulk communication must use bounded partitioning and concurrency.
31. Long-term scheduled notifications must use durable scheduling.
32. Expired notifications must not retry indefinitely.
33. Critical notification classes must have backlog, failure and delivery-latency monitoring.
34. Notification workers must preserve retryable work during graceful shutdown.
35. Notification architecture must have automated duplicate, retry, callback and failure tests.

---

# 216. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Spring Kafka
- AWS SQS
- AWS SES or approved email provider
- Transactional Outbox
- PostgreSQL
- Flyway
- Resilience4j
- Java 21 Virtual Threads where appropriate
- template engine
- i18n
- AWS S3 for secure file delivery
- Testcontainers
- JUnit 5
- AssertJ
- provider sandbox environments
- integration tests
- failure-injection tests
- notification dashboards and alerts

---

# 217. Success Criteria

The decision is successful when:

- notification-provider outages no longer roll back valid business transactions
- notification events are not lost after business commit
- message redelivery does not produce duplicate communication
- provider retry is bounded
- queue backlog is measurable
- critical delivery failures are visible
- templates are centrally controlled and tested
- HTML rendering does not mutate source domain values
- user preferences are respected
- invalid recipients are suppressed appropriately
- provider callbacks update delivery state safely
- sensitive files are delivered through secure file mechanisms
- bulk notifications cannot exhaust transactional notification capacity
- failed notifications can be safely replayed

---

# 218. Alternatives Rejected

## 218.1 Send Email Directly Inside Business Transaction

Rejected because provider failure becomes business failure and increases transaction duration.

---

## 218.2 Fire-and-Forget Thread

Rejected because process termination can silently lose notifications.

---

## 218.3 In-Memory Retry Queue

Rejected because pod restart loses pending work.

---

## 218.4 In-Memory Duplicate Cache

Rejected because restart and multi-pod deployments invalidate guarantees.

---

## 218.5 Retry Every Failure Forever

Rejected because permanent failures and provider outages create retry storms.

---

## 218.6 Put Provider Details in Domain Events

Rejected because it couples business contracts to delivery technology.

---

## 218.7 HTML-Escape Domain Data Before Persistence

Rejected because output encoding belongs to the presentation context and changes legitimate domain values.

---

## 218.8 Email Large Sensitive Attachments

Rejected as the standard because secure authenticated downloads provide better access control and lifecycle management.

---

# 219. Related Decisions

This ADR extends and implements:

- ADR-007: Adopt Transactional Outbox
- ADR-008: Assume At-Least-Once Message Delivery
- ADR-009: Use Kafka for Integration Events
- ADR-013: Use Testcontainers for Integration Testing
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-057: Enterprise Event-Driven Architecture, Kafka Messaging and Transactional Outbox Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-067: Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-071: Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-077: Enterprise Scheduled Jobs, Batch Processing, Distributed Scheduling and Workload Coordination Standard
- ADR-078: Enterprise File Processing, Upload, Download, Streaming and Large File Handling Standard

---

# 220. References

- AWS SQS Documentation
- AWS SES Documentation
- Apache Kafka Documentation
- Spring Kafka Documentation
- Spring Boot Documentation
- Resilience4j Documentation
- OWASP Input Validation Cheat Sheet
- OWASP Cross Site Scripting Prevention Cheat Sheet
- OWASP Logging Cheat Sheet
- RFC 5322
- RFC 8058
- Transactional Outbox Pattern
- Enterprise Integration Patterns
- Google Site Reliability Engineering

---

# 221. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise notification and external communication baseline |

---

# 222. Decision Summary

Notification delivery becomes:

```text
BUSINESS TRANSACTION
        |
        +--> BUSINESS DATA
        |
        +--> OUTBOX EVENT
        |
      COMMIT
        |
        v
      BROKER
        |
        v
NOTIFICATION SERVICE
        |
        v
     PROVIDER
```

instead of:

```text
SAVE ORDER
    |
    v
SEND EMAIL
    |
    X
PROVIDER TIMEOUT
    |
    v
ROLLBACK ORDER
```

Delivery identity becomes:

```text
EVENT
  |
  +--> NOTIFICATION
          |
          +--> ATTEMPT 1
          +--> ATTEMPT 2
          +--> ATTEMPT 3
```

At-least-once consumption becomes:

```text
EVENT
 |
 +--> DELIVERY 1
 |
 +--> DUPLICATE EVENT
          |
          v
      IDEMPOTENCY
          |
          X
      NO DUPLICATE EMAIL
```

Retry becomes:

```text
FAILURE
   |
   v
TRANSIENT?
   |
 +--+--+
 |     |
YES    NO
 |     |
 v     v
RETRY TERMINAL
WITH   FAILURE
BACKOFF
```

Provider protection becomes:

```text
QUEUE
  |
  v
BOUNDED WORKERS
  |
  v
RATE LIMIT
  |
  v
CIRCUIT BREAKER
  |
  v
PROVIDER
```

Template processing becomes:

```text
DOMAIN VALUE
   |
   |  M&M
   v
TEMPLATE RENDERER
   |
   v
OUTPUT-CONTEXT
ENCODING
   |
   v
HTML
```

rather than permanently storing:

```text
M&amp;M
```

as business data.

Delivery status becomes:

```text
PENDING
   |
   v
SENDING
   |
   v
SENT
   |
   +--> DELIVERED
   |
   +--> BOUNCED
   |
   +--> FAILED
```

Provider callback processing becomes:

```text
CALLBACK
   |
   v
VERIFY SIGNATURE
   |
   v
DEDUPLICATE
   |
   v
ORDER / RECONCILE STATE
   |
   v
UPDATE DELIVERY
```

Bulk communication becomes:

```text
1,000,000 RECIPIENTS
        |
        v
PARTITION
        |
        v
QUEUE
        |
        v
BOUNDED WORKERS
        |
        v
RATE-LIMITED PROVIDER
```

The complete notification equation is:

```text
TRANSACTIONAL OUTBOX
        +
AT-LEAST-ONCE ASSUMPTION
        +
DURABLE IDEMPOTENCY
        +
BOUNDED RETRY
        +
BACKOFF + JITTER
        +
RATE LIMITING
        +
PROVIDER ISOLATION
        +
SAFE TEMPLATING
        +
I18N
        +
PII MINIMIZATION
        +
PREFERENCES
        +
BOUNCE / CALLBACK PROCESSING
        +
DLQ RECOVERY
        +
OBSERVABILITY
        =
RELIABLE ENTERPRISE COMMUNICATION
```

The governing principle is:

```text
Do not make
a valid business transaction
depend on an ordinary email.

Commit business state.

Publish reliably.

Deliver asynchronously.

Assume messages duplicate.

Make notification creation
idempotent.

Make delivery retryable.

Bound every retry.

Classify failures.

Do not retry invalid addresses
forever.

Respect provider limits.

Bound worker concurrency.

Virtual Threads do not make
provider quotas infinite.

Keep provider technology
outside the domain.

Keep templates centralized.

Encode untrusted values
at the output boundary.

Do not corrupt domain data
to make HTML safe.

Keep M&M as M&M
in the business model.

Minimize PII.

Do not log message bodies.

Do not expose recipients
through metrics.

Respect preferences
and opt-outs.

Treat SENT and DELIVERED
as different states.

Authenticate callbacks.

Expect duplicate callbacks.

Expect callbacks out of order.

Monitor queue age.

Monitor failures.

Monitor DLQs.

Make replay safe.

And remember:

sending a notification
is not a single API call.

It is a distributed workflow
whose final outcome may remain
unknown for some time after
the business transaction
has already succeeded.
```

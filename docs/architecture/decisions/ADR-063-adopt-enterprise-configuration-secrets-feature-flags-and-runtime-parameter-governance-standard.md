# ADR-063: Adopt Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-063 |
| Title | Adopt Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard |
| Status | Accepted |
| Date | 2026-07-25 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Configuration, Secrets, Feature Flags, Runtime Parameters, Governance |
| Related Work Items | Spring Boot, Kubernetes, AWS Secrets Manager, EKS, Feature Flags |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise applications depend on configuration that varies according to:

```text
Environment

Infrastructure

External Services

Security Credentials

Operational Parameters

Business Rules

Feature Availability
```

A typical Java service may require:

```text
Application
    |
    +--> Database Configuration
    |
    +--> Redis Configuration
    |
    +--> SQS Configuration
    |
    +--> SQS Configuration
    |
    +--> External API URLs
    |
    +--> Timeouts
    |
    +--> Connection Pools
    |
    +--> Circuit Breakers
    |
    +--> Feature Flags
    |
    +--> Business Parameters
    |
    +--> Secrets
```

Incorrect configuration can produce failures even when the application binary is correct.

Configuration is therefore part of runtime architecture.

---

# 2. Problem Statement

The organization requires standards covering:

- Spring Boot configuration
- `@ConfigurationProperties`
- profiles
- environment variables
- configuration precedence
- Kubernetes ConfigMaps
- Kubernetes Secrets
- AWS Secrets Manager
- secret rotation
- fail-fast validation
- immutable configuration
- dynamic configuration
- feature flags
- kill switches
- runtime parameters
- parameter caching
- refresh
- fallback
- auditability
- configuration drift
- security
- testing
- production diagnostics

---

# 3. Decision Drivers

Primary drivers are:

1. security
2. configuration correctness
3. environment portability
4. operational safety
5. auditability
6. runtime predictability
7. secret protection
8. controlled dynamic behavior
9. rollback capability
10. testability
11. deployment reproducibility
12. reduced configuration drift

---

# 4. Decision

Enterprise applications MUST externalize environment-specific runtime configuration from application binaries.

Canonical model:

```text
                    APPLICATION ARTIFACT
                           |
                           v
                   CONFIGURATION MODEL
                           |
          +----------------+----------------+
          |                |                |
          v                v                v
      CONFIGMAP       SECRET STORE      PARAMETERS
          |                |                |
          +----------------+----------------+
                           |
                           v
                   VALIDATED CONFIG
                           |
                           v
                     APPLICATION
```

Configuration sources MUST have explicit ownership, precedence, validation and security semantics.

---

# 5. Fundamental Principle

The governing principle is:

```text
Code defines behavior.

Configuration selects
environment-specific behavior.

Secrets authorize behavior.

Feature flags control
availability of behavior.

Business parameters influence
business decisions.

These concerns must not
be treated as equivalent.
```

---

# 6. Configuration Classification

Configuration MUST be classified before deciding where it belongs.

---

# 7. Static Application Configuration

Examples:

```text
Maximum internal batch size

Supported enum mapping

Algorithm strategy

Protocol behavior
```

If values are intrinsic to application behavior and should change only through software delivery, they SHOULD normally remain in code.

---

# 8. Environment Configuration

Examples:

```text
Database Host

Redis Host

SQS Bootstrap Servers

External API Base URL

Queue Name
```

These MUST be externalized.

---

# 9. Secret Configuration

Examples:

```text
Passwords

API Keys

Client Secrets

Private Keys

Tokens
```

These MUST use approved secret-management mechanisms.

---

# 10. Operational Configuration

Examples:

```text
Timeout

Pool Size

Circuit Breaker Threshold

Batch Size

Concurrency Limit
```

These SHOULD be externally configurable when operational tuning is expected.

---

# 11. Business Parameters

Examples:

```text
Order Approval Threshold

Business Cutoff

Allowed Commercial Limit

Processing Rule
```

Business parameters require explicit governance because changing them can change business outcomes without deploying code.

---

# 12. Feature Flags

Feature flags control whether specific functionality is available.

They MUST NOT be confused with arbitrary business configuration.

---

# 13. Configuration Ownership

Every material configuration item SHOULD have an identifiable owner.

Possible ownership:

```text
Development

Platform

Security

Operations

Business
```

---

# 14. Spring Boot

Spring Boot externalized configuration is the standard application configuration mechanism for applicable Java services.

---

# 15. ConfigurationProperties

Related configuration MUST prefer strongly typed:

```java
@ConfigurationProperties
```

over scattered `@Value` declarations.

---

# 16. Example

Preferred:

```java
@ConfigurationProperties(prefix = "clients.products")
public record ProductsClientProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration responseTimeout,
        int maxConnections
) {
}
```

---

# 17. Why Typed Configuration

Typed configuration provides:

```text
Type Safety

Validation

Discoverability

Testability

IDE Support

Centralized Ownership
```

---

# 18. @Value

`@Value` MAY be used for isolated simple values but SHOULD NOT become the primary configuration architecture.

---

# 19. Configuration Grouping

Configuration SHOULD be grouped by logical capability.

Example:

```text
clients.products.*

clients.customers.*

resilience.circuit-breaker.*

cache.redis.*

messaging.sqs.*
```

---

# 20. Configuration Naming

Configuration keys SHOULD use stable, descriptive naming.

---

# 21. Ambiguous Property

Avoid:

```text
timeout=10
```

Prefer:

```text
clients.products.response-timeout=10s
```

---

# 22. Units

Configuration values representing duration or data size SHOULD use explicit units where supported.

Prefer:

```text
10s

500ms

64MB
```

over undocumented numeric interpretation.

---

# 23. Duration

Spring `Duration` SHOULD be preferred for time-based configuration.

---

# 24. DataSize

Spring `DataSize` SHOULD be preferred for applicable memory/storage configuration.

---

# 25. Validation

Required configuration MUST be validated during application startup.

---

# 26. Fail Fast

Invalid mandatory configuration MUST prevent successful application startup.

---

# 27. Invalid Production State

The application MUST NOT start successfully and fail only when the first request discovers that critical configuration is missing.

---

# 28. Bean Validation

`@ConfigurationProperties` SHOULD use Jakarta Bean Validation where appropriate.

Example:

```java
@Validated
@ConfigurationProperties(prefix = "clients.products")
public record ProductsClientProperties(

        @NotNull
        URI baseUrl,

        @NotNull
        Duration responseTimeout,

        @Min(1)
        int maxConnections
) {
}
```

---

# 29. Semantic Validation

Validation MUST include semantic constraints when type validation is insufficient.

Example:

```text
minimum <= maximum

connectTimeout < responseTimeout

batchSize > 0

maxConnections >= concurrency
```

---

# 30. Default Values

Defaults MAY be provided when they are safe and intentional.

---

# 31. Dangerous Defaults

Security-critical or environment-specific configuration SHOULD NOT silently use dangerous defaults.

---

# 32. Missing Secret

A missing mandatory production secret MUST fail startup or prevent the affected capability from becoming ready according to architecture.

---

# 33. Profiles

Spring profiles MAY distinguish broad environment/runtime behavior.

---

# 34. Profile Explosion

Applications SHOULD NOT create excessive profiles such as:

```text
dev

dev-local

dev-user-a

dev-user-b

qa

qa2

hml

hml2

prod-east

prod-west
```

when external configuration can express the differences.

---

# 35. Environment Profiles

Profiles SHOULD represent meaningful behavioral groups rather than every deployment instance.

---

# 36. Profile-Specific Code

Application business logic SHOULD NOT normally contain branches such as:

```java
if (isProduction()) {
    ...
}
```

---

# 37. Environment Parity

Behavior SHOULD remain as consistent as practical across environments.

---

# 38. Local Development

Local-development configuration MAY use safe local defaults.

---

# 39. Production Credentials Locally

Production credentials MUST NOT be required for normal local development.

---

# 40. Configuration Precedence

Configuration precedence MUST be understood and documented.

---

# 41. Accidental Override

Higher-precedence sources MUST NOT silently override critical configuration without operational visibility.

---

# 42. Duplicate Definition

The same property SHOULD NOT be defined redundantly in many configuration layers without reason.

---

# 43. Environment Variables

Environment variables MAY provide runtime configuration to containers.

---

# 44. Environment Variable Naming

Environment-variable mappings SHOULD remain predictable.

Example:

```text
CLIENTS_PRODUCTS_BASE_URL
```

for:

```text
clients.products.base-url
```

---

# 45. Environment Variable Secret

Secrets MAY be injected as environment variables when approved, but the exposure characteristics MUST be understood.

---

# 46. Secret File

Mounted secret files MAY be preferred when required by the secret-management architecture.

---

# 47. ConfigMap

Kubernetes ConfigMaps SHOULD contain non-sensitive environment configuration.

---

# 48. ConfigMap Secret

ConfigMaps MUST NOT contain production passwords, tokens or private credentials.

---

# 49. Kubernetes Secret

Kubernetes Secrets MAY participate in secret delivery but MUST follow platform security requirements.

---

# 50. Base64 Is Not Encryption

Kubernetes Secret base64 encoding MUST NOT be treated as encryption.

---

# 51. Secret Store

Long-lived production secrets SHOULD originate from an approved secret-management system.

---

# 52. AWS Secrets Manager

AWS Secrets Manager SHOULD be used for applicable AWS-hosted secrets where it matches enterprise architecture.

---

# 53. Secret Retrieval

Applications MAY receive secrets through:

```text
Platform Injection

CSI Driver

External Secrets Integration

Approved SDK Integration
```

depending on platform architecture.

---

# 54. Secret SDK

Applications SHOULD NOT implement custom secret-management infrastructure when the platform already provides a standardized mechanism.

---

# 55. Secret in Source

Secrets MUST NOT exist in:

```text
Java Source

application.yml

application.properties

Dockerfile

Git Repository

Unit Tests
```

---

# 56. Test Credentials

Tests SHOULD use synthetic credentials.

---

# 57. Secret Logging

Secret values MUST never be logged.

---

# 58. Secret Error

Errors retrieving secrets MUST NOT include the secret value.

---

# 59. Secret Rotation

Production credentials SHOULD support rotation.

---

# 60. Rotation Without Rebuild

Secret rotation MUST NOT require rebuilding the application artifact.

---

# 61. Rotation Without Source Change

Credential rotation SHOULD NOT require source-code modification.

---

# 62. Dynamic Rotation

When credentials rotate while the application is running, the integration MUST define how refreshed credentials become effective.

---

# 63. Database Credential Rotation

Database credential rotation MUST account for existing pooled connections.

---

# 64. HTTP Credential Rotation

External API credentials SHOULD be refreshed without requiring uncontrolled application restart where feasible.

---

# 65. Rotation Failure

Failed rotation MUST be observable.

---

# 66. Previous Secret

Systems MUST define whether previous credentials remain valid during a rotation overlap window.

---

# 67. Secret Cache

Applications MAY cache secrets temporarily when required for performance/resilience.

---

# 68. Secret Cache TTL

Secret cache duration MUST not prevent expected credential rotation.

---

# 69. Secret Cache Failure

A stale secret MUST NOT be retained indefinitely without operational visibility.

---

# 70. Secret Least Privilege

A workload MUST receive only secrets required by that workload.

---

# 71. Shared Secret

Sharing one credential across unrelated services SHOULD be avoided.

---

# 72. Feature Flag

A feature flag controls deployment-independent feature activation.

---

# 73. Deployment vs Release

Feature flags separate:

```text
DEPLOYMENT

from

FEATURE RELEASE
```

---

# 74. Feature Flag Flow

```text
CODE DEPLOYED
     |
     v
FEATURE OFF
     |
     v
VALIDATE
     |
     v
ENABLE FOR TARGET
     |
     v
OBSERVE
     |
     v
EXPAND
```

---

# 75. Feature Flag Use Cases

Feature flags MAY support:

```text
Progressive Delivery

Canary Release

Controlled Activation

Emergency Disablement

Temporary Compatibility
```

---

# 76. Kill Switch

A kill switch is a specialized operational feature flag used to rapidly disable risky functionality.

---

# 77. Kill Switch Candidate

Good candidates include:

```text
External Integration

Expensive Background Process

New Workflow

Optional Notification

High-Risk Feature
```

---

# 78. Kill Switch Scope

Kill switches SHOULD disable the smallest useful capability rather than the entire service.

---

# 79. Safe Default

Critical kill switches SHOULD have an explicitly defined safe default.

---

# 80. Flag Default

Every feature flag MUST define behavior when the flag provider is unavailable.

---

# 81. Fail Open

`fail-open` means functionality remains enabled when the flag system fails.

---

# 82. Fail Closed

`fail-closed` means functionality remains disabled when the flag system fails.

---

# 83. Flag Failure Strategy

The choice between fail-open and fail-closed MUST reflect business/security risk.

---

# 84. Security Feature

Security controls SHOULD NOT normally depend on a fail-open remote feature flag.

---

# 85. Feature Flag Provider

Feature flags MAY be supplied through an approved centralized feature-management platform.

---

# 86. Local Feature Flag

Simple deployment-scoped flags MAY use ordinary configuration when advanced targeting is unnecessary.

---

# 87. Targeted Flag

User/customer/segment targeting SHOULD use a feature-management system designed for controlled evaluation.

---

# 88. PII in Flag Context

Feature-flag evaluation context MUST minimize sensitive information.

---

# 89. Flag Evaluation

Flag evaluation MUST have bounded latency.

---

# 90. Remote Flag Failure

Remote feature-flag infrastructure MUST NOT become an uncontrolled synchronous dependency on every request.

---

# 91. Flag Cache

Feature flags SHOULD normally be cached or streamed locally according to provider architecture.

---

# 92. Flag Observability

Important flag changes and evaluation failures SHOULD be observable.

---

# 93. Flag Audit

Production flag changes MUST be auditable.

---

# 94. Flag Authorization

Only authorized users/processes may change production feature flags.

---

# 95. Flag Naming

Flags SHOULD use stable names describing the capability.

Prefer:

```text
orders.bulk-approval.enabled
```

over:

```text
new-feature-2
```

---

# 96. Temporary Flag

Temporary release flags MUST have an owner and removal plan.

---

# 97. Permanent Flag

Permanent operational flags MAY remain when they represent legitimate runtime controls.

---

# 98. Flag Debt

Completed rollout flags SHOULD be removed.

---

# 99. Stale Flag

Stale feature flags increase:

```text
Branch Complexity

Testing Cost

Cognitive Load

Production Risk
```

---

# 100. Flag Lifecycle

Recommended lifecycle:

```text
CREATE

DEPLOY OFF

ENABLE GRADUALLY

VALIDATE

FULL ENABLE

REMOVE FLAG

REMOVE DEAD CODE
```

---

# 101. Flag Testing

Both relevant flag states MUST be tested while both remain supported.

---

# 102. Flag Combinations

The number of interacting flags SHOULD be minimized.

---

# 103. Combinatorial Explosion

If there are:

```text
10 independent boolean flags
```

there are potentially:

```text
2^10 = 1024
```

behavior combinations.

---

# 104. Flag Dependency

Flags SHOULD NOT form undocumented dependency chains.

---

# 105. Business Parameter

Business parameters differ from feature flags because they influence business calculations/rules rather than merely enabling functionality.

---

# 106. Business Parameter Governance

Material business parameters MUST define:

```text
Owner

Allowed Range

Effective Date

Audit History

Validation

Fallback
```

---

# 107. Parameter Service

A centralized parameter service MAY be used when business parameters require runtime administration.

---

# 108. Parameter Client

Parameter clients MUST use:

```text
Bounded Timeout

Bounded Connection Pool

Resilience Policy

Observability
```

---

# 109. Parameter Per Request

Stable parameters SHOULD NOT require unnecessary remote calls for every business request.

---

# 110. Parameter Cache

Parameters MAY be cached when consistency requirements permit.

---

# 111. Cache TTL

Parameter cache TTL MUST reflect how quickly changes must become effective.

---

# 112. Parameter Fallback

Fallback behavior MUST be explicit.

Possible policies:

```text
USE CACHED VALUE

USE SAFE DEFAULT

FAIL OPERATION
```

---

# 113. Arbitrary Default

A missing business parameter MUST NOT silently become:

```text
0

false

empty string
```

unless that value is explicitly the safe business default.

---

# 114. Stale Parameter

Using stale cached configuration requires defined maximum staleness.

---

# 115. Last Known Good

For selected operational parameters, a validated last-known-good value MAY provide resilience.

---

# 116. Last Known Good Validation

Only previously validated values may become last-known-good configuration.

---

# 117. Invalid Refresh

An invalid dynamic refresh MUST NOT replace a valid current value.

---

# 118. Atomic Refresh

Related dynamic parameters SHOULD update atomically where partial updates would create inconsistent state.

---

# 119. Dynamic Configuration

Only configuration that genuinely requires runtime changes SHOULD be dynamic.

---

# 120. Static Configuration

Configuration affecting structural application initialization SHOULD generally require restart/redeployment.

Examples:

```text
Database Driver

Major Bean Topology

Protocol Selection

Core Security Architecture
```

---

# 121. Dynamic Everything

Making every property dynamically mutable is prohibited as a default architecture.

---

# 122. Runtime Mutation Risk

Dynamic configuration increases:

```text
State Complexity

Testing Complexity

Incident Complexity

Audit Requirements
```

---

# 123. Refresh Scope

Dynamic refresh MUST clearly define which components observe the new value.

---

# 124. Partial Refresh

Applications MUST avoid states where some components use the old value and others use the new value unintentionally.

---

# 125. Immutable Snapshot

A configuration snapshot SHOULD be immutable after validation.

---

# 126. Snapshot Replacement

Dynamic refresh SHOULD replace an immutable validated snapshot atomically.

---

# 127. Configuration Version

Dynamic configuration SHOULD expose a version/revision where supported.

---

# 128. Effective Configuration

Operations SHOULD be able to determine which configuration version is active without exposing secrets.

---

# 129. Effective Secret

Operational diagnostics MUST NOT reveal effective secret values.

---

# 130. Configuration Audit

Material production configuration changes MUST be auditable.

---

# 131. Audit Fields

Audit SHOULD include applicable:

```text
Who

What

When

Previous Version

New Version

Environment
```

without storing secret plaintext unnecessarily.

---

# 132. Change Reason

High-risk runtime changes SHOULD capture a change reason or associated work item.

---

# 133. Configuration Drift

Configuration drift occurs when actual runtime configuration differs from declared intended configuration.

---

# 134. Declarative Configuration

Production configuration SHOULD be managed declaratively where practical.

---

# 135. Manual Change

Manual configuration changes SHOULD be exceptional.

---

# 136. Reconciliation

Approved manual emergency changes MUST be reconciled into the source of truth.

---

# 137. Source of Truth

Each configuration category MUST have a defined source of truth.

---

# 138. Multiple Sources of Truth

The same business parameter MUST NOT have multiple independent authoritative stores.

---

# 139. GitOps

GitOps MAY manage non-secret declarative runtime configuration where compatible with platform architecture.

---

# 140. Secret GitOps

Secret values MUST NOT be stored as plaintext merely to fit a GitOps workflow.

---

# 141. Environment Promotion

Configuration promotion SHOULD follow controlled environment progression.

---

# 142. Production Copy

Copying complete DEV configuration into PRD without review is prohibited.

---

# 143. Environment Difference

Environment differences SHOULD be intentional and documented.

---

# 144. Configuration Review

Production configuration changes SHOULD receive review appropriate to their risk.

---

# 145. Critical Parameter

Changing a critical business or resilience parameter MAY be operationally equivalent to deploying code.

---

# 146. Change Control

High-impact runtime configuration MUST follow appropriate change-control policy.

---

# 147. Timeout Governance

Timeouts MUST follow ADR-055 and ADR-060 timeout hierarchy principles.

---

# 148. Connection Pool Governance

Pool-size configuration MUST account for dependency capacity.

---

# 149. Concurrency Configuration

Concurrency limits MUST remain bounded.

---

# 150. Dangerous Runtime Value

A configurable value MUST still have enforced safety bounds.

---

# 151. Example

If configured concurrency is:

```text
1..50
```

the application SHOULD reject:

```text
100000
```

rather than trusting configuration blindly.

---

# 152. Circuit Breaker Configuration

Circuit Breaker configuration SHOULD be externalized when operational tuning is required.

---

# 153. Circuit Breaker Bounds

Circuit Breaker values MUST remain within validated ranges.

---

# 154. Retry Configuration

Retry counts and delays MUST be bounded.

---

# 155. Zero Timeout

A zero/negative timeout MUST be rejected unless explicitly meaningful.

---

# 156. Production Overrides

Production overrides SHOULD be minimal and explicit.

---

# 157. Configuration Documentation

Material configuration properties SHOULD be documented.

---

# 158. Metadata

Spring Boot configuration metadata SHOULD be generated where useful for developer tooling.

---

# 159. Deprecated Property

Deprecated configuration SHOULD provide migration guidance.

---

# 160. Unknown Property

Critical configuration groups SHOULD consider rejecting unknown/misspelled properties where practical.

---

# 161. Configuration Compatibility

Renaming/removing configuration properties MUST consider deployment compatibility.

---

# 162. Rolling Deployment

During rolling deployment:

```text
V1 + V2
```

may coexist.

Configuration MUST support both versions during the rollout window when required.

---

# 163. Property Removal

A property required by V1 MUST NOT be removed before V1 instances are gone.

---

# 164. Property Introduction

New mandatory properties MUST exist before deploying a version requiring them.

---

# 165. Expand/Contract Configuration

Configuration evolution SHOULD use:

```text
ADD NEW CONFIG

DEPLOY COMPATIBLE VERSION

MIGRATE USAGE

REMOVE OLD CONFIG LATER
```

---

# 166. Secret Rotation During Rollout

Secret rotation MUST account for old and new application versions.

---

# 167. Feature Flag During Rollout

Feature flags SHOULD permit old/new versions to coexist safely where used for progressive delivery.

---

# 168. Configuration and Rollback

Rollback requires configuration compatibility with the previous release.

---

# 169. Configuration Removal

Do not immediately delete configuration needed by the rollback version.

---

# 170. Startup Logging

Applications MAY log non-sensitive configuration summaries at startup.

---

# 171. Safe Startup Summary

Safe examples:

```text
Configured dependency names

Timeout values

Pool sizes

Feature state
```

---

# 172. Unsafe Startup Summary

Never log:

```text
Passwords

Tokens

Private Keys

Secret Values
```

---

# 173. Configuration Diagnostics

Diagnostics SHOULD indicate configuration origin/version without exposing secret content.

---

# 174. Observability

Configuration failures MUST be observable.

---

# 175. Metrics

Dynamic configuration systems SHOULD expose applicable:

```text
Refresh Success

Refresh Failure

Configuration Age

Cache Hit

Fallback Usage
```

---

# 176. Feature Flag Metrics

Feature flags MAY expose bounded aggregate evaluation metrics.

---

# 177. High Cardinality Flag Metrics

Flag metrics MUST NOT use unrestricted user/customer IDs as labels.

---

# 178. Secret Metrics

Secret values MUST never become metric labels.

---

# 179. Alerting

Alerts SHOULD exist for critical conditions such as:

```text
Repeated Secret Refresh Failure

Configuration Provider Unavailable

Stale Critical Configuration

Invalid Parameter Refresh
```

---

# 180. Configuration Provider Failure

Remote configuration-provider failure MUST have defined behavior.

---

# 181. Provider as SPOF

A remote configuration provider SHOULD NOT become an unnecessary single point of failure for every request.

---

# 182. Startup Dependency

If configuration is required only at startup, the application MAY fail startup when it cannot obtain it.

---

# 183. Runtime Dependency

If configuration is needed dynamically, caching/fallback semantics MUST be defined.

---

# 184. Security Configuration

Security-critical configuration requires stricter governance.

---

# 185. Disable Security Flag

A runtime flag MUST NOT casually disable:

```text
Authentication

Authorization

TLS Validation

Input Validation
```

---

# 186. TLS Verification

Configuration MUST NOT permit production TLS verification to be silently disabled.

---

# 187. Debug Security

Debug configuration MUST NOT expose sensitive production information.

---

# 188. CORS Configuration

CORS settings MUST be explicitly controlled.

---

# 189. Wildcard CORS

Production wildcard CORS requires explicit security justification.

---

# 190. Allowed Origins

Allowed origins SHOULD be externally configurable where environment-specific.

---

# 191. Authentication Endpoints

Authentication/identity provider endpoints MUST use approved configuration sources.

---

# 192. AWS Region

AWS region SHOULD be externally configurable or supplied by the platform environment.

---

# 193. AWS Endpoint Override

AWS endpoint overrides MAY support local testing.

---

# 194. Production Endpoint Override

Production endpoint overrides MUST be tightly controlled.

---

# 195. LocalStack

Local AWS emulation MAY use endpoint overrides for tests/local development.

---

# 196. Test Strategy

Configuration behavior MUST be tested.

---

# 197. Binding Test

Critical `@ConfigurationProperties` classes SHOULD have binding/validation tests.

---

# 198. Missing Property Test

Tests SHOULD verify missing mandatory properties fail validation.

---

# 199. Invalid Range Test

Tests SHOULD verify invalid operational values are rejected.

---

# 200. Default Test

Safe defaults SHOULD be explicitly tested.

---

# 201. Secret Mask Test

Tests MUST verify secret values are not exposed through configuration diagnostics/logging.

---

# 202. Feature Flag Test

Feature-enabled and feature-disabled behavior SHOULD be tested.

---

# 203. Kill Switch Test

Critical kill switches SHOULD have automated tests verifying the disabled path.

---

# 204. Provider Failure Test

Dynamic configuration clients SHOULD test provider failure.

---

# 205. Cache Fallback Test

Where fallback caching exists, tests SHOULD verify:

```text
Provider Available

Provider Unavailable

Cached Value Available

Cache Empty

Expired Value
```

---

# 206. Rotation Test

Critical credential integrations SHOULD validate credential-rotation behavior where feasible.

---

# 207. Concurrency Test

Dynamic configuration refresh MUST be thread-safe.

---

# 208. Atomicity Test

Tests SHOULD verify consumers never observe partially updated configuration snapshots.

---

# 209. Testcontainers

Infrastructure-sensitive configuration SHOULD be validated using representative infrastructure where appropriate.

---

# 210. Architecture Test

Architecture fitness functions SHOULD enforce stable configuration rules.

---

# 211. Configuration Review Checklist

Material changes SHOULD evaluate:

```text
[ ] Is this code or configuration?

[ ] Is it sensitive?

[ ] Is it environment-specific?

[ ] Does it need runtime mutation?

[ ] Who owns it?

[ ] What is its source of truth?

[ ] Is startup validation present?

[ ] Are safe bounds enforced?

[ ] Is there a safe default?

[ ] Can the provider fail?

[ ] What happens when it fails?

[ ] Is caching required?

[ ] What is maximum staleness?

[ ] Is the change auditable?

[ ] Does it support rolling deployment?

[ ] Does it support rollback?

[ ] Can secrets appear in logs?

[ ] Is rotation supported?

[ ] Does a feature flag need removal?

[ ] Are both flag states tested?
```

---

# 212. Configuration Fitness Functions

Stable rules SHOULD be automated where practical.

Examples:

```text
[ ] No secrets in application.yml

[ ] No credentials in source code

[ ] ConfigurationProperties validated

[ ] Duration properties use typed Duration

[ ] Operational limits are bounded

[ ] Production TLS verification cannot be disabled

[ ] ConfigMaps contain no known secret patterns

[ ] Feature flags have defined defaults

[ ] Temporary flags have owner/removal metadata

[ ] Dynamic configuration refresh is atomic

[ ] Secret values are excluded from logs

[ ] Production configuration changes are auditable
```

---

# 213. Enterprise Configuration Gate

A service is not considered production compliant when applicable conditions include:

```text
[ ] Production credentials committed to Git

[ ] Secret stored in ConfigMap

[ ] Required configuration is not validated

[ ] Application starts with invalid critical configuration

[ ] Arbitrary unbounded concurrency can be configured

[ ] Configuration source of truth is undefined

[ ] Dynamic provider is called synchronously on every request unnecessarily

[ ] Feature flag provider failure has undefined behavior

[ ] Temporary feature flags have become permanent dead code

[ ] Secret rotation requires rebuilding the artifact

[ ] Production configuration cannot be audited

[ ] Runtime configuration breaks rolling deployment

[ ] Configuration removal prevents rollback

[ ] Security controls can be casually disabled through configuration
```

---

# 214. Anti-Patterns

The following are prohibited or strongly discouraged:

- production secrets in Git
- credentials in `application.yml`
- secrets inside Docker images
- production secrets in ConfigMaps
- assuming Kubernetes Secret base64 is encryption
- scattered `@Value` configuration for complex subsystems
- configuration without type information
- numeric timeout properties with undocumented units
- starting successfully with invalid critical configuration
- dangerous implicit defaults
- profile explosion
- production-specific branches inside business logic
- manually editing production configuration without audit
- multiple authoritative stores for the same parameter
- remote parameter lookup on every request when values are stable
- unlimited parameter cache staleness
- replacing valid configuration with an invalid refresh
- partially refreshing related parameters
- making every property dynamically mutable
- unrestricted feature-flag combinations
- permanent stale release flags
- using feature flags to bypass security controls
- logging effective secrets
- feature metrics labeled with customer/user IDs
- removing configuration before rollback compatibility expires
- rotating credentials without considering connection pools
- configurable concurrency without maximum bounds
- runtime configuration that silently disables TLS verification

---

# 215. Positive Consequences

The decision provides:

- stronger secret protection
- predictable startup behavior
- typed configuration
- reduced environment drift
- safer operational tuning
- controlled feature rollout
- faster kill-switch response
- auditable business parameter changes
- safer credential rotation
- improved rolling deployment
- improved rollback compatibility
- better testability

---

# 216. Negative Consequences

The decision introduces:

- configuration governance
- secret-management infrastructure
- feature-flag lifecycle management
- additional validation
- audit requirements
- dynamic refresh complexity
- rotation testing
- configuration compatibility planning

These costs are accepted because uncontrolled configuration can change production behavior as significantly as application code.

---

# 217. Neutral Consequences

The decision also means:

- not every value should be configurable
- not every configuration should be dynamic
- not every feature needs a flag
- not every secret requires application-level retrieval
- some configuration changes require restart
- some provider failures should fail startup
- some dynamic systems should use last-known-good values
- business parameters require stronger governance than ordinary technical configuration

---

# 218. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Secret exposure | Critical | Medium | Secret manager + scanning |
| Invalid configuration | Critical | Medium | Fail-fast validation |
| Configuration drift | High | Medium | Declarative source of truth |
| Stale parameter | High | Medium | TTL + freshness monitoring |
| Feature flag debt | Medium | High | Lifecycle/removal policy |
| Dynamic refresh inconsistency | High | Medium | Atomic immutable snapshots |
| Credential rotation failure | Critical | Low/Medium | Rotation strategy + testing |
| Provider outage | High | Medium | Cache/fallback |
| Unsafe runtime value | Critical | Medium | Validation bounds |
| Rollback incompatibility | High | Medium | Expand/contract configuration |

---

# 219. Implementation Guidance

The following rules are mandatory:

1. Environment-specific configuration must remain outside application binaries.
2. Secrets must use approved secret-management mechanisms.
3. Complex Spring configuration must prefer `@ConfigurationProperties`.
4. Required configuration must be validated at startup.
5. Invalid critical configuration must fail fast.
6. Operational parameters must have safe bounds.
7. Duration/data-size configuration should use typed values.
8. Profiles must not replace proper externalized configuration.
9. Production business logic must not branch directly on environment names.
10. ConfigMaps must not contain production secrets.
11. Kubernetes Secret encoding must not be considered encryption.
12. AWS Secrets Manager or the approved equivalent should store applicable production secrets.
13. Secrets must never be committed or logged.
14. Credential rotation must not require rebuilding the application.
15. Secret caches must respect rotation requirements.
16. Feature flags must have defined defaults.
17. Feature-flag provider failure semantics must be explicit.
18. Security controls must not depend on unsafe fail-open flags.
19. Production feature-flag changes must be auditable.
20. Temporary release flags must have a removal lifecycle.
21. Both supported feature states must be tested.
22. Business parameters must have ownership, validation and auditability.
23. Stable parameters must not require unnecessary remote calls per request.
24. Parameter cache staleness must be bounded.
25. Invalid dynamic refresh must not replace valid configuration.
26. Related dynamic values must refresh atomically where required.
27. Only configuration requiring runtime mutation should be dynamic.
28. Effective configuration version should be diagnosable without revealing secrets.
29. Production configuration must have a defined source of truth.
30. Manual emergency changes must be reconciled.
31. Configuration evolution must support rolling deployment.
32. Configuration required by rollback versions must remain available during the rollback window.
33. Security-sensitive configuration requires stricter controls.
34. Configuration behavior must have automated tests.
35. Dynamic refresh must be thread-safe.
36. Secret rotation should be tested for critical integrations.
37. Configuration failures and stale state must be observable.

---

# 220. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Spring `@ConfigurationProperties`
- Jakarta Bean Validation
- Kubernetes ConfigMaps
- Kubernetes Secrets
- AWS Secrets Manager
- AWS IAM/workload identity
- feature-management platforms
- parameter services
- Redis where parameter caching is appropriate
- JUnit 5
- AssertJ
- Mockito
- Testcontainers
- SonarQube
- SAST
- secret scanning
- architecture fitness functions
- CI/CD configuration validation

---

# 221. Success Criteria

The decision is successful when:

- production secrets no longer exist in source-controlled configuration
- invalid configuration is detected before traffic reaches an instance
- configuration properties are typed and discoverable
- credential rotation occurs without artifact rebuild
- production feature changes are auditable
- stale feature flags are routinely removed
- dynamic parameter outages do not create unnecessary application outages
- invalid parameter refreshes preserve last-known-good state
- configuration drift becomes detectable
- rolling deployments remain configuration compatible
- rollback is not blocked by premature configuration removal
- security controls cannot be casually disabled through configuration

---

# 222. Alternatives Rejected

## 222.1 Configuration Embedded in Application Artifact

Rejected because environment-specific behavior would require rebuilds.

---

## 222.2 Secrets in ConfigMaps

Rejected because ConfigMaps are not a secret-management mechanism.

---

## 222.3 Scattered @Value Configuration

Rejected for complex subsystems because it reduces type safety, validation and maintainability.

---

## 222.4 Remote Parameter Lookup on Every Request

Rejected for stable parameters because it unnecessarily increases latency and coupling.

---

## 222.5 Every Configuration Dynamically Mutable

Rejected because it dramatically increases runtime state complexity.

---

## 222.6 Permanent Release Feature Flags

Rejected because completed rollout branches become technical debt.

---

## 222.7 Rebuild for Credential Rotation

Rejected because secrets and application artifacts have different lifecycles.

---

# 223. Related Decisions

This ADR extends and implements:

- ADR-016: Application Resilience
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-035: Engineering Quality and Testing Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-046: Data Governance, Privacy and Lifecycle Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-056: Enterprise REST API and Integration Contract Standard
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-058: Enterprise PostgreSQL Persistence, Transaction Management and Database Engineering Standard
- ADR-059: Enterprise Redis Caching, Distributed Cache and Data Consistency Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard

---

# 224. References

- Spring Boot Externalized Configuration
- Spring Boot Configuration Metadata
- Jakarta Bean Validation
- Kubernetes ConfigMaps
- Kubernetes Secrets
- AWS Secrets Manager
- AWS IAM
- AWS Well-Architected Framework
- OWASP Secrets Management Cheat Sheet
- CNCF Cloud Native Security Guidance
- OpenFeature Specification
- Twelve-Factor App Configuration Principles
- NIST Secure Software Development Framework

---

# 225. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-25 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise configuration, secrets and feature-management baseline |

---

# 226. Decision Summary

Configuration is classified as:

```text
CONFIGURATION
     |
     +--> APPLICATION CONSTANT
     |
     +--> ENVIRONMENT CONFIG
     |
     +--> OPERATIONAL CONFIG
     |
     +--> BUSINESS PARAMETER
     |
     +--> FEATURE FLAG
     |
     +--> SECRET
```

Spring configuration becomes:

```text
EXTERNAL CONFIG
      |
      v
@ConfigurationProperties
      |
      v
TYPE CONVERSION
      |
      v
VALIDATION
      |
   +--+--+
   |     |
 VALID INVALID
   |     |
   v     X
START  FAIL FAST
```

Secret handling becomes:

```text
SECRET MANAGER
      |
      v
WORKLOAD IDENTITY
      |
      v
APPLICATION
```

instead of:

```text
application.yml
      |
      v
password=production-password
```

Feature delivery becomes:

```text
DEPLOY CODE
    |
    v
FLAG OFF
    |
    v
VALIDATE
    |
    v
ENABLE GRADUALLY
    |
    v
OBSERVE
    |
    v
FULL ENABLE
    |
    v
REMOVE TEMPORARY FLAG
```

Kill switches become:

```text
EXTERNAL INTEGRATION
       |
       v
    FAILURE
       |
       v
KILL SWITCH OFF
       |
       v
DISABLE ONLY
AFFECTED CAPABILITY
```

Dynamic configuration becomes:

```text
CONFIG PROVIDER
      |
      v
NEW VERSION
      |
      v
VALIDATE
      |
   +--+--+
   |     |
 VALID INVALID
   |     |
   v     X
ATOMIC  KEEP
SWAP    LAST-KNOWN-GOOD
```

Parameter resilience becomes:

```text
PARAMETER SERVICE
       |
       v
LOCAL CACHE
       |
    +--+--+
    |     |
AVAILABLE FAILURE
    |     |
    v     v
UPDATE   VALIDATED
CACHE    FALLBACK
```

Rolling deployment requires:

```text
V1 + V2
   |
   v
CONFIGURATION
COMPATIBLE WITH BOTH
```

and rollback requires:

```text
V2
 |
 X
 |
 v
V1
 |
 v
OLD CONFIGURATION
STILL AVAILABLE
```

The complete configuration equation is:

```text
TYPED CONFIGURATION
        +
FAIL-FAST VALIDATION
        +
SAFE BOUNDS
        +
EXTERNALIZED ENVIRONMENT VALUES
        +
SECURE SECRET MANAGEMENT
        +
CREDENTIAL ROTATION
        +
CONTROLLED FEATURE FLAGS
        +
AUDITED BUSINESS PARAMETERS
        +
BOUNDED CACHING
        +
ATOMIC DYNAMIC REFRESH
        +
LAST-KNOWN-GOOD STRATEGY
        +
CONFIGURATION VERSIONING
        +
ROLLING-DEPLOYMENT COMPATIBILITY
        +
ROLLBACK COMPATIBILITY
        +
OBSERVABILITY
        =
ENTERPRISE CONFIGURATION GOVERNANCE
```

The governing principle is:

```text
Do not make everything configurable.

Do not make everything dynamic.

Type configuration.

Validate it before accepting traffic.

Put secrets in a secret store,
not in source control.

Rotate credentials independently
from application releases.

Give every feature flag
a defined failure behavior.

Remove temporary flags
after rollout is complete.

Treat business parameters
as governed business decisions.

Cache remote parameters only
with explicit freshness semantics.

Never replace valid runtime state
with invalid configuration.

Keep old and new versions
configuration-compatible during rollout.

Preserve rollback configuration
for the rollback window.

And remember:

changing production configuration
can be operationally equivalent
to changing production code.
```

# ADR-026: Adopt Platform Configuration and Secret Management Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-026 |
| Title | Adopt Platform Configuration and Secret Management Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Configuration Management, Secret Management, Spring Boot, Kubernetes, GitOps |
| Related Work Items | Externalized Configuration, ConfigMaps, Secrets, Secret Manager, Workload Identity, Credential Rotation, GitOps |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform consists of distributed Java 21 and Spring Boot services deployed to Kubernetes.

Application behavior depends on configuration such as:

- database endpoints
- Kafka brokers
- Redis endpoints
- downstream service URLs
- connection-pool settings
- timeout values
- retry policies
- circuit-breaker thresholds
- feature flags
- observability configuration
- logging levels
- security settings
- credentials
- certificates
- API keys
- environment-specific parameters

Configuration is therefore part of the effective production system.

A correct application artifact with incorrect configuration can still cause:

```text
Security Incident

Availability Failure

Data Corruption

Integration Failure

Performance Degradation

Unexpected Business Behavior
```

The platform already establishes the principle from ADR-024:

```text
Build once

↓

Promote the same artifact
```

Therefore DEV, HML and PROD differences must not require application rebuilds.

The platform requires a standardized configuration and secret-management model.

---

# 2. Problem Statement

The platform requires configuration standards that:

- preserve immutable application artifacts
- externalize environment-specific configuration
- distinguish configuration from secrets
- provide typed Spring Boot configuration
- validate mandatory properties
- fail fast on invalid configuration
- establish property precedence
- prevent accidental secret exposure
- integrate with Kubernetes
- integrate with enterprise secret stores
- support workload identity
- support credential rotation
- minimize restart requirements
- govern dynamic configuration
- support feature flags
- provide configuration auditability
- support rollback
- prevent configuration drift
- integrate with GitOps
- support DEV, HML and PROD safely
- preserve zero-downtime deployment
- remain compatible with supply-chain security

---

# 3. Decision Drivers

Primary decision drivers are:

1. security
2. reproducibility
3. environment portability
4. configuration traceability
5. secret protection
6. credential rotation
7. zero-downtime operation
8. startup safety
9. type safety
10. operational simplicity
11. auditability
12. rollback capability
13. least privilege
14. GitOps compatibility
15. configuration consistency
16. drift prevention
17. maintainability

---

# 4. Decision

The Enterprise Order Platform adopts an externalized, typed, validated and auditable configuration model based on:

```text
Immutable Application Artifact

+

Versioned Non-Secret Configuration

+

External Secret Management

+

Typed Spring Boot Configuration

+

Startup Validation

+

Workload Identity

+

Controlled Runtime Overrides

+

Auditable Deployment

+

GitOps Reconciliation
```

Configuration and secrets must remain separate concerns.

---

# 5. Fundamental Principle

The platform adopts:

```text
Code defines capability.

Configuration selects behavior.

Secrets establish trust.

Environment selects configuration.

The artifact remains unchanged.
```

---

# 6. Build Once

Environment differences must not require application recompilation.

The same artifact should progress:

```text
DEV

↓

HML

↓

PROD
```

with environment-specific external configuration.

---

# 7. Artifact vs Configuration

The platform distinguishes:

```text
Application Artifact
```

from:

```text
Application Configuration
```

and from:

```text
Secrets
```

These have different lifecycle and security requirements.

---

# 8. Application Artifact

The artifact contains:

- application code
- compiled classes
- static resources
- dependency versions
- application defaults where safe
- configuration schemas
- validation rules

It must not contain production secrets.

---

# 9. Configuration

Configuration contains non-secret environment or operational values.

Examples:

```text
HTTP timeout

Kafka topic

service URL

connection pool size

retry count

feature configuration
```

---

# 10. Secret

A secret is information whose disclosure can provide unauthorized access or compromise confidentiality/integrity.

Examples:

- password
- API key
- OAuth client secret
- private key
- database credential
- signing key
- access token

---

# 11. Classification

Every configuration property should conceptually belong to one of:

```text
STATIC APPLICATION DEFAULT

ENVIRONMENT CONFIGURATION

RUNTIME OPERATIONAL CONFIGURATION

SECRET
```

---

# 12. Externalized Configuration

Spring Boot externalized configuration is the standard application configuration mechanism.

---

# 13. Configuration Properties

Related properties must be grouped into typed configuration objects using:

```java
@ConfigurationProperties
```

rather than scattered direct property lookups.

---

# 14. Example

Preferred:

```java
@ConfigurationProperties(prefix = "external-http")
public record ExternalHttpClientProperties(
        Duration connectTimeout,
        Duration responseTimeout,
        int maxConnections
) {
}
```

---

# 15. Avoid Scattered @Value

Large sets of:

```java
@Value("${some.property}")
```

distributed across business classes are discouraged.

Typed configuration provides:

- cohesion
- validation
- discoverability
- testability
- refactoring safety

---

# 16. Immutable Configuration Objects

Configuration objects should preferably be immutable.

Java records are preferred where appropriate.

---

# 17. Configuration Validation

Mandatory configuration must be validated during startup.

---

# 18. Bean Validation

Configuration properties should use Bean Validation where appropriate.

Example:

```java
@ConfigurationProperties(prefix = "external-http")
@Validated
public record ExternalHttpClientProperties(

        @NotNull
        Duration connectTimeout,

        @NotNull
        Duration responseTimeout,

        @Min(1)
        int maxConnections
) {
}
```

---

# 19. Fail Fast

Invalid mandatory configuration must cause application startup failure.

Preferred:

```text
Invalid configuration

↓

Startup fails

↓

Pod remains unready

↓

Existing healthy replicas continue serving
```

rather than:

```text
Invalid configuration

↓

Application starts

↓

Failure appears later in production traffic
```

---

# 20. Missing Property

A mandatory property must not silently receive an unsafe fallback.

---

# 21. Defaults

Defaults may be used when:

- behavior is safe
- value is environment independent
- default is documented
- absence does not hide deployment mistakes

---

# 22. Security Defaults

Security-sensitive settings must default securely.

---

# 23. Boolean Security Flags

Avoid configuration where omission unexpectedly disables security.

Bad:

```text
security.enabled=false by accidental absence
```

---

# 24. Property Naming

Configuration properties must use consistent hierarchical naming.

Example:

```text
external-http.connect-timeout

external-http.response-timeout

external-http.max-connections
```

---

# 25. Duration

Timeouts should use explicit duration types rather than ambiguous integers.

Preferred:

```yaml
response-timeout: 5s
```

instead of:

```yaml
response-timeout: 5000
```

where semantics are unclear.

---

# 26. Data Size

Memory/storage configuration should use explicit data-size types where supported.

Example:

```text
10MB
```

rather than undocumented integer units.

---

# 27. Enum Configuration

Properties with a bounded domain should use enums rather than arbitrary strings where practical.

---

# 28. URL Configuration

External URLs must be validated sufficiently to detect malformed deployment configuration.

---

# 29. Profile Usage

Spring profiles may represent broad environment/application modes.

---

# 30. Profile Scope

Profiles should not become the primary mechanism for embedding complete environment configuration into the application artifact.

---

# 31. application.yml

The application may contain:

```text
application.yml
```

for safe common defaults and property structure.

---

# 32. Environment Files

Avoid packaging production-specific credentials or large production configuration sets inside:

```text
application-prod.yml
```

when those values belong to deployment configuration.

---

# 33. Environment Independence

The application image should not need to know at build time whether it will run in:

```text
DEV

HML

PROD
```

---

# 34. Property Precedence

Configuration precedence must be understood and intentionally governed.

---

# 35. Override Risk

Spring Boot supports multiple property sources.

This flexibility can create unexpected overrides if uncontrolled.

---

# 36. Controlled Sources

Production deployments should use a defined set of configuration sources.

Example:

```text
Application safe defaults

↓

Versioned deployment configuration

↓

Secret-management integration

↓

Explicit controlled emergency override
```

---

# 37. Arbitrary Override

Uncontrolled command-line or environment overrides in production are prohibited.

---

# 38. Environment Variables

Environment variables may be used for deployment configuration where appropriate.

---

# 39. Environment Variable Naming

Names should map predictably to Spring Boot properties.

Example:

```text
EXTERNAL_HTTP_RESPONSE_TIMEOUT
```

maps to:

```text
external-http.response-timeout
```

---

# 40. ConfigMap

Kubernetes ConfigMaps are suitable for non-sensitive configuration.

---

# 41. ConfigMap Is Not Secret Storage

Passwords, private keys, API keys and similar values must not be placed in ConfigMaps.

---

# 42. ConfigMap Versioning

Declarative ConfigMap definitions should be version controlled when compatible with the GitOps model.

---

# 43. Secret

Kubernetes Secret objects may participate in secret delivery, but they are not the authoritative enterprise secret-management system by default.

---

# 44. Base64 Is Not Encryption

A Kubernetes Secret manifest such as:

```yaml
data:
  password: cGFzc3dvcmQ=
```

does not provide confidentiality merely because the value is Base64 encoded.

---

# 45. Secret Manager

Production secrets should originate from an approved secret-management platform.

Examples conceptually include:

```text
Enterprise Vault

Cloud Secret Manager

Managed Key Vault
```

according to infrastructure standards.

---

# 46. Secret Source of Truth

The authoritative secret should reside in the approved secret-management system.

---

# 47. Secret in Git

Plain production secrets must never be committed to Git.

---

# 48. Encrypted Secret in Git

Encrypted-secret approaches may be used only when approved by enterprise security architecture and key-management standards.

---

# 49. Secret Retrieval

Secret delivery may use:

- workload identity
- secret-store CSI integration
- external-secret synchronization
- controlled application integration

according to platform capabilities.

---

# 50. Workload Identity

Workload identity is preferred where supported.

Conceptually:

```text
Pod

↓

Kubernetes ServiceAccount

↓

Federated Workload Identity

↓

Secret Manager / Cloud Resource
```

---

# 51. No Embedded Cloud Credential

Applications should not require long-lived cloud access keys embedded in configuration when workload identity is available.

---

# 52. Identity Separation

Each service should receive only the identity required for its resources.

---

# 53. Secret Scope

A service must not receive secrets belonging to unrelated services.

---

# 54. Database Credentials

Database credentials must be scoped to the application's required database privileges.

---

# 55. Migration Credentials

Flyway migration credentials should be separated from normal runtime credentials where practical.

---

# 56. Runtime Database User

The application runtime database user should not automatically receive schema-administration privileges.

---

# 57. Kafka Credentials

Kafka credentials and ACLs must follow least privilege.

---

# 58. OAuth Credentials

OAuth client secrets must be stored in the approved secret-management mechanism.

---

# 59. Private Keys

Private signing or authentication keys require stronger protection and controlled access.

---

# 60. Secret Exposure

Secrets must not appear in:

- Git
- Dockerfiles
- container image layers
- ConfigMaps
- application logs
- stack traces
- actuator output
- metrics
- tracing attributes
- pod labels
- pod annotations

---

# 61. Configuration Logging

Applications may log safe configuration summaries at startup.

They must not log secret values.

---

# 62. Secret Property Names

Even properties whose names suggest sensitive content should receive defensive treatment.

Examples:

```text
password

secret

token

api-key

authorization

private-key
```

---

# 63. toString Risk

Configuration objects containing secrets must not accidentally expose them through generated `toString()` output.

---

# 64. Record Risk

Java records automatically generate `toString()`.

Therefore records containing secrets require careful handling.

---

# 65. Secret Wrapper

Sensitive values may use dedicated wrappers or custom redaction strategies where useful.

---

# 66. Exception Messages

Exceptions must not include secret values.

---

# 67. HTTP Client Logging

HTTP client logging must not expose:

```text
Authorization

Cookie

API key

OAuth token
```

headers.

---

# 68. Actuator

Spring Boot Actuator configuration endpoints must not expose sensitive values publicly.

---

# 69. env Endpoint

Actuator environment/configuration endpoints must remain restricted or disabled according to security policy.

---

# 70. Heap Dumps

Heap dumps may contain secrets and require security-sensitive handling.

---

# 71. Thread Dumps

Thread dumps can contain request or application state and require controlled access.

---

# 72. Diagnostic Artifacts

Diagnostic artifacts must be handled according to production-data security policy.

---

# 73. Secret Rotation

Secrets must be rotatable without rebuilding the application artifact.

---

# 74. Rotation Principle

Preferred:

```text
Rotate Secret

↓

Update secret source

↓

Workload receives new value

↓

Old credential expires/revokes
```

---

# 75. Zero-Downtime Rotation

Critical credentials should support rotation without application downtime where infrastructure permits.

---

# 76. Overlapping Credentials

Some systems may require temporary overlap:

```text
Credential A valid

↓

Credential B created

↓

Applications transition to B

↓

A revoked
```

This reduces rotation downtime.

---

# 77. Database Credential Rotation

Database credential rotation must account for existing pooled connections.

---

# 78. Connection Pools

Changing a secret file does not automatically replace credentials already held by an active connection pool.

---

# 79. Rotation Strategy

Rotation design must consider:

- secret refresh
- connection recreation
- rolling restart
- credential overlap
- revocation timing

---

# 80. Rolling Restart

A controlled rolling restart is acceptable when dynamic credential reload is not safely supported.

---

# 81. Restart Is Not Downtime

With multiple replicas and correct readiness behavior:

```text
rolling restart
```

does not necessarily imply service downtime.

---

# 82. Secret Refresh

Applications should dynamically reload secrets only when the mechanism is well understood and tested.

---

# 83. Dynamic Configuration

Not every configuration property should be dynamically reloadable.

---

# 84. Static Configuration

Configuration affecting fundamental application structure should normally require restart.

Examples:

- server port
- major bean wiring
- database driver configuration
- thread model
- serialization architecture

---

# 85. Dynamic Operational Configuration

Selected operational parameters may support runtime change.

Examples may include:

- feature flags
- some logging levels
- selected thresholds
- selected routing behavior

---

# 86. Dynamic Change Risk

Dynamic configuration bypasses ordinary startup validation and deployment controls unless explicitly engineered.

---

# 87. Dynamic Configuration Governance

Every dynamically changeable property must define:

- owner
- valid range
- default
- validation
- propagation mechanism
- audit behavior
- rollback behavior

---

# 88. Refresh Scope

Broad automatic refresh of arbitrary Spring configuration is discouraged.

---

# 89. Atomic Configuration

Related dynamic values should be updated atomically where inconsistent intermediate states would be unsafe.

---

# 90. Runtime Validation

Dynamic configuration changes must be validated before activation.

---

# 91. Invalid Runtime Configuration

Invalid dynamic configuration must be rejected while preserving the last known valid value.

---

# 92. Last Known Good

Dynamic configuration systems should preserve a known-good configuration when practical.

---

# 93. Feature Flags

Feature flags are the standard mechanism for controlled runtime activation of selected application behavior.

---

# 94. Feature Flag Purpose

Feature flags may decouple:

```text
Deployment
```

from:

```text
Feature Activation
```

---

# 95. Feature Flag Is Not Authorization

Feature flags must not replace business authorization.

---

# 96. Security Controls

Mandatory security controls must not be bypassable through ordinary feature flags.

---

# 97. Feature Flag Metadata

Important flags should have:

- owner
- purpose
- default state
- creation date
- expected retirement
- affected services

---

# 98. Permanent Flags

Temporary rollout flags must not remain permanently without review.

---

# 99. Flag Debt

Obsolete feature flags should be removed after rollout completion.

---

# 100. Kill Switch

Feature flags may serve as controlled kill switches for risky optional functionality.

---

# 101. Kill Switch Security

A kill switch must fail toward the safer behavior.

---

# 102. Flag Failure

If the feature-flag service becomes unavailable, behavior must be explicitly defined.

Examples:

```text
last known value

safe default

feature disabled
```

depending on risk.

---

# 103. Remote Configuration Service

A centralized configuration service may be adopted when justified.

It is not mandatory for all configuration.

---

# 104. Configuration Service Availability

If runtime behavior depends on a remote configuration service, its failure characteristics must be designed explicitly.

---

# 105. Startup Dependency

Applications should not unnecessarily become unable to start solely because an optional dynamic configuration service is temporarily unavailable.

---

# 106. Critical Configuration

Configuration required to establish safe application operation may legitimately be a startup dependency.

---

# 107. Configuration Repository

Non-secret deployment configuration should be declarative and version controlled.

---

# 108. GitOps

The platform should use GitOps principles where supported by infrastructure.

---

# 109. Desired State

Git represents the reviewed desired state for declarative non-secret deployment configuration.

---

# 110. Reconciliation

A GitOps controller reconciles:

```text
Desired State

↓

Actual Cluster State
```

---

# 111. Configuration Drift

Configuration drift occurs when actual runtime configuration differs from approved declarative state.

---

# 112. Manual kubectl Edit

Routine production configuration changes through:

```text
kubectl edit
```

are prohibited.

---

# 113. Manual Patch

Routine unmanaged:

```text
kubectl patch
```

changes are prohibited.

---

# 114. Drift Correction

GitOps reconciliation should detect or correct unauthorized drift according to platform policy.

---

# 115. Emergency Change

Emergency manual configuration changes may be allowed only through a governed break-glass process.

---

# 116. Emergency Reconciliation

After an emergency change:

```text
Runtime change

↓

Incident stabilization

↓

Change recorded in source of truth

↓

Review

↓

Reconciliation restored
```

---

# 117. Configuration Pull Request

Production configuration changes should normally use pull requests.

---

# 118. Configuration Review

Reviewers must evaluate:

- security
- operational impact
- compatibility
- resource impact
- dependency impact
- rollback strategy

---

# 119. Configuration Ownership

Configuration ownership should align with application/platform responsibility.

---

# 120. CODEOWNERS

Critical configuration paths may use CODEOWNERS or equivalent approval enforcement.

---

# 121. Environment Separation

DEV, HML and PROD configuration must be clearly separated.

---

# 122. Production Values

Production configuration must not be accidentally inherited from development defaults.

---

# 123. Development Convenience

Development convenience settings must not become production defaults.

Examples:

```text
debug=true

authentication disabled

verbose SQL

permissive CORS

mock integration
```

---

# 124. HML

HML should approximate production behavior where practical without using production secrets.

---

# 125. Secret Separation

DEV, HML and PROD must use different secret material.

---

# 126. Credential Reuse

Production credentials must not be reused in development environments.

---

# 127. External Endpoints

Environment configuration must prevent DEV/HML workloads from accidentally calling production systems unless explicitly required and protected.

---

# 128. Database Separation

Environment database endpoints must be explicit and independently protected.

---

# 129. Kafka Separation

Kafka environments/topics must be configured to prevent accidental cross-environment event publication.

---

# 130. Environment Marker

Applications may expose a safe environment identifier for observability.

---

# 131. Environment Marker Is Not Authorization

Environment labels must not be used as security credentials.

---

# 132. Configuration Schema

Configuration should have an explicit discoverable schema through typed properties and metadata.

---

# 133. Configuration Documentation

Every externally required property should document:

- name
- purpose
- type
- required/optional
- default
- valid range
- sensitivity
- restart requirement

---

# 134. Configuration Metadata

Spring Boot configuration metadata should be generated where appropriate.

---

# 135. Deprecated Property

Configuration properties should have a controlled deprecation lifecycle.

---

# 136. Property Rename

Property renaming should support a migration period where necessary.

---

# 137. Unknown Properties

Critical configuration objects may reject or detect unknown/misspelled properties where practical.

---

# 138. Typo Risk

A typo such as:

```text
response-timeot
```

must not silently leave an unsafe or unintended default when the property is mandatory.

---

# 139. Numeric Range

Numeric operational properties must define valid ranges.

---

# 140. Timeout Range

A timeout of:

```text
0
```

or:

```text
999999 minutes
```

should not automatically be considered valid merely because parsing succeeds.

---

# 141. Connection Pool

Connection-pool configuration must be bounded.

---

# 142. Thread Pool

Thread-pool configuration must be bounded where explicit pools are used.

---

# 143. Retry Configuration

Retry counts and delays must be bounded to prevent retry storms.

---

# 144. Circuit Breaker Configuration

Circuit-breaker configuration must remain consistent with ADR-016.

---

# 145. Observability Configuration

Observability configuration must remain consistent with ADR-014 and ADR-019.

---

# 146. Log Level

Production log-level changes should be controlled.

---

# 147. DEBUG

Long-term DEBUG logging in production is discouraged due to:

- data exposure
- log volume
- cost
- performance impact

---

# 148. Temporary Debug

Temporary production debug logging must have:

- explicit scope
- owner
- expiration
- auditability

where tooling permits.

---

# 149. Configuration Audit

Production configuration changes must be auditable.

---

# 150. Audit Questions

The platform should be able to answer:

```text
What changed?

Who changed it?

When?

Why?

Which environment?

Which service?

Which version?

What was the previous value?
```

without exposing secret values.

---

# 151. Secret Audit

Secret-management systems should record access and modification events according to enterprise capability.

---

# 152. Secret Value Audit

Audit logs must not contain the secret itself.

---

# 153. Configuration Version

Deployments should identify the version/revision of configuration applied.

---

# 154. Artifact + Configuration

A running production instance should be identifiable by:

```text
Artifact Digest

+

Configuration Revision

+

Secret References / Versions where safe
```

---

# 155. Secret Version Metadata

Secret version identifiers may be recorded when safe.

Secret values must never be recorded.

---

# 156. Deployment Reproducibility

To reproduce a production deployment, the organization should be able to determine:

```text
Application artifact

Deployment manifests

Non-secret configuration revision

Secret references

Infrastructure context
```

---

# 157. Configuration Rollback

Non-secret configuration must support rollback.

---

# 158. Git Rollback

Preferred configuration rollback:

```text
Revert Git change

↓

Review / approve

↓

GitOps reconciliation

↓

Verify
```

---

# 159. Secret Rollback

Secret rollback requires additional caution.

Reactivating a previously compromised secret is prohibited.

---

# 160. Credential Rollback

When a new credential fails, remediation may require creating another valid credential rather than restoring an old revoked one.

---

# 161. Feature Flag Rollback

Feature flags should support rapid safe rollback of feature activation.

---

# 162. Configuration Compatibility

Application versions and configuration versions must remain compatible during rolling deployments.

---

# 163. Mixed-Version Window

During rolling deployment:

```text
Version N

and

Version N+1
```

may run simultaneously.

Configuration must support both during that window.

---

# 164. Breaking Configuration Change

A property change must not assume all old pods disappear instantaneously.

---

# 165. Expand/Contract Configuration

Breaking configuration evolution should use an expand/contract strategy where needed.

Example:

```text
Add new property with compatible fallback

↓

Deploy compatible application

↓

Switch configuration

↓

Remove old property later
```

---

# 166. Property Removal

Old configuration properties should be removed only after all dependent application versions no longer require them.

---

# 167. Zero-Downtime Integration

Configuration rollout must preserve ADR-021 zero-downtime requirements.

---

# 168. Readiness

An application with invalid mandatory configuration must never report ready.

---

# 169. Configuration Change Rollout

Restart-required configuration changes should use controlled rolling deployment.

---

# 170. Dynamic Change Rollout

Dynamic changes should support progressive activation where risk justifies it.

---

# 171. Canary Configuration

High-risk operational changes may be tested against a subset of instances before global activation where platform tooling permits.

---

# 172. Configuration Blast Radius

Changes should be scoped to the smallest required service/environment.

---

# 173. Global Configuration

Global configuration requires stronger governance because mistakes can affect many services simultaneously.

---

# 174. Shared Configuration

Shared configuration should be minimized.

---

# 175. Service Ownership

Services should own their application-specific configuration.

---

# 176. Central Policy

Central platform/security policies remain centrally governed.

---

# 177. Configuration Coupling

Services should not read another service's private configuration directly.

---

# 178. Secret Coupling

Services must not reuse another service's credentials merely for convenience.

---

# 179. Configuration API

A service's private configuration is not an integration contract.

---

# 180. Business Parameters

Business parameters that require domain ownership, history or user administration may belong in application data rather than infrastructure configuration.

---

# 181. Configuration vs Data

A useful distinction is:

```text
Configuration
→ controls application operation

Business Data
→ represents domain state
```

---

# 182. Frequently Changed Business Values

Frequently changed business rules should not automatically become Kubernetes ConfigMaps.

---

# 183. Database Configuration

Domain-managed parameters may require:

- database persistence
- audit history
- authorization
- effective dates

rather than deployment configuration.

---

# 184. Feature Flag vs Business Rule

Feature flags control deployment/activation behavior.

They should not become an uncontrolled substitute for domain rule modeling.

---

# 185. Secret Lifetime

Secret lifetime should be minimized according to system capability.

---

# 186. Temporary Credentials

Short-lived credentials are preferred over long-lived static credentials.

---

# 187. Token Expiration

Applications must handle token expiration and refresh safely.

---

# 188. Token Cache

Token caches must respect expiration and refresh boundaries.

---

# 189. Token Logging

Tokens must never be logged.

---

# 190. Certificate Rotation

TLS certificates must support rotation before expiration.

---

# 191. Certificate Expiration

Certificate expiration must be monitored.

---

# 192. Signing Keys

Signing-key rotation must preserve verification requirements for previously signed artifacts/messages where applicable.

---

# 193. Encryption Keys

Encryption-key rotation requires explicit data compatibility strategy.

---

# 194. Key Versioning

Encrypted data should identify the key version required for decryption where appropriate.

---

# 195. Secret Deletion

Secrets must not be deleted until dependent workloads have safely transitioned.

---

# 196. Rotation Monitoring

Credential rotation should verify:

- new credential accepted
- workloads healthy
- old credential no longer used
- old credential revoked

---

# 197. Secret Leak Response

If a secret is exposed:

```text
Revoke / Rotate

↓

Assess Exposure

↓

Update Workloads

↓

Verify

↓

Investigate

↓

Prevent Recurrence
```

---

# 198. Source Removal Is Insufficient

Deleting a leaked secret from Git does not make it trustworthy again.

---

# 199. Secret Scan Integration

Secret scanning from ADR-024 remains mandatory.

---

# 200. SAST Integration

SAST should detect hardcoded credential patterns where tooling supports it.

---

# 201. Container Scan

Container inspection should help detect embedded sensitive files where tooling supports it.

---

# 202. Admission Integration

ADR-025 admission policies should reject known unsafe configuration patterns where feasible.

---

# 203. Examples of Admission Rules

Policies may reject:

- privileged secret mounts
- secrets in prohibited namespaces
- unapproved secret mechanisms
- missing required workload identity
- unsafe ConfigMap patterns
- mutable images

according to platform capability.

---

# 204. RBAC

Access to configuration and secrets must follow ADR-025 least-privilege RBAC.

---

# 205. Secret Read Permission

Permission to read Secrets must be treated as sensitive.

---

# 206. ConfigMap Permission

ConfigMap write permission can materially alter application behavior and must also be controlled.

---

# 207. Deployment Permission

A user capable of modifying a Deployment may potentially redirect configuration or images.

Deployment write access is therefore security-sensitive.

---

# 208. GitOps Controller Identity

The GitOps controller requires powerful permissions and must be treated as privileged infrastructure.

---

# 209. GitOps Repository Security

Compromise of the GitOps repository can become production compromise.

Therefore it requires:

- branch protection
- review
- authentication
- audit
- supply-chain controls

---

# 210. GitOps Secret

GitOps deployment credentials must be protected using workload identity or approved secret-management mechanisms.

---

# 211. Reconciliation Scope

GitOps controllers should receive only required cluster/namespace permissions.

---

# 212. Multiple Environments

Production and non-production reconciliation should use appropriately separated permissions.

---

# 213. Promotion

Configuration promotion should be explicit.

Example:

```text
DEV configuration

↓

Validated

↓

HML configuration change

↓

Validated

↓

PROD configuration change
```

---

# 214. Copying Configuration

Blindly copying an entire DEV configuration into PROD is prohibited.

---

# 215. Environment-Specific Review

Production values require production-context review.

---

# 216. Infrastructure Endpoints

Production endpoint changes require careful review because they can redirect sensitive traffic.

---

# 217. SSRF-Like Configuration Risk

An attacker with configuration-write access may redirect application HTTP clients toward malicious infrastructure.

Configuration write access is therefore a security boundary.

---

# 218. Kafka Topic Configuration

Incorrect Kafka topic configuration may cause cross-domain or cross-environment data exposure.

---

# 219. Database URL Configuration

Incorrect database configuration may cause the application to connect to the wrong environment.

---

# 220. Guardrails

Where practical, applications should validate environmental invariants.

Example:

```text
PROD workload

must not connect to

known DEV database domain
```

when such rules can be defined reliably.

---

# 221. Startup Safety Checks

Critical services may perform safe startup validation of:

- required endpoints
- configuration structure
- environment consistency
- credential availability

without performing destructive operations.

---

# 222. Dependency Availability

Startup should not necessarily require every external business dependency to be healthy.

---

# 223. Configuration Validity vs Dependency Availability

These are different:

```text
URL missing
→ configuration error
→ startup failure
```

versus:

```text
URL valid but downstream temporarily unavailable
→ runtime resilience concern
```

---

# 224. Fail Fast Scope

Fail-fast applies to invalid configuration, not indiscriminately to transient network failures.

---

# 225. Sensitive Default

Production must never silently fall back to embedded development credentials.

---

# 226. Local Development

Local development may use:

- environment variables
- local profile
- local containers
- developer secret store
- `.env`-style tooling

provided real production secrets are not committed.

---

# 227. .env

Files containing local secrets must be excluded from Git.

---

# 228. .gitignore

Repositories should explicitly ignore known local secret files.

---

# 229. Example Configuration

Safe example configuration files may be committed.

Example:

```text
.env.example
```

containing placeholders only.

---

# 230. Test Configuration

Automated tests should use fake/test credentials.

---

# 231. Testcontainers

Testcontainers should receive isolated test configuration.

---

# 232. CI Secrets

CI/CD secrets remain governed by ADR-024.

---

# 233. Production Secret in CI

A build job should not receive production application secrets merely because it compiles the application.

---

# 234. Deployment Stage

Production secret access should be limited to the deployment/runtime mechanisms that require it.

---

# 235. Build-Time Configuration

Environment-specific runtime configuration must not influence application compilation unnecessarily.

---

# 236. Build Secret

A build-time secret, when genuinely required, must use secure ephemeral build-secret mechanisms.

---

# 237. Docker ARG

Production secrets must not be passed through ordinary Docker build arguments.

---

# 238. Image Layers

Secret deletion in a later Docker layer does not guarantee removal from earlier layers.

---

# 239. Secret Manager Availability

Secret-manager availability must be considered in workload design.

---

# 240. Startup Secret Retrieval

If startup requires retrieving secrets remotely, failure behavior must be bounded and observable.

---

# 241. Retry

Secret retrieval retries must use bounded retry/backoff behavior.

---

# 242. Secret Cache

Locally cached secret material must receive appropriate protection.

---

# 243. Secret Cache Lifetime

Cached secrets must not remain valid indefinitely after rotation.

---

# 244. Memory

Applications should avoid unnecessary duplication of sensitive values in memory.

---

# 245. String Immutability

Java `String` values cannot be explicitly cleared from memory.

Extremely sensitive cryptographic material may require specialized handling.

---

# 246. Secret Redaction

Common logging/sanitization infrastructure should redact sensitive values defensively.

---

# 247. Sanitization Boundary

Redaction is a defensive control.

It must not justify passing secrets into logs intentionally.

---

# 248. Observability Correlation

Configuration changes should be correlatable with production behavior.

---

# 249. Deployment Annotation

Deployment observability may include safe metadata such as:

```text
application version

configuration revision

deployment identifier
```

---

# 250. Incident Analysis

When an incident begins immediately after a configuration change, operators must be able to identify that change quickly.

---

# 251. Change Events

Configuration deployments should emit or record operational change events where tooling supports it.

---

# 252. Metrics

Recommended configuration-related metrics include bounded counts such as:

- configuration load failures
- secret refresh failures
- credential rotation failures
- feature-flag provider failures
- GitOps reconciliation failures
- configuration drift detections

---

# 253. Cardinality

Do not use secret values or arbitrary configuration values as metric labels.

---

# 254. Alerting

Actionable alerts may include:

- secret nearing expiration
- certificate nearing expiration
- secret refresh failure
- configuration reconciliation failure
- invalid configuration rollout
- persistent configuration drift

---

# 255. Health

Optional configuration services should not incorrectly make liveness fail.

---

# 256. Readiness

Loss of configuration required to safely serve traffic may affect readiness depending on runtime semantics.

---

# 257. Last Known Good

If a dynamic configuration provider temporarily fails, an application may continue with last-known-good values when safe.

---

# 258. Security Revocation

Last-known-good behavior must not override explicit security revocation indefinitely.

---

# 259. Configuration Backup

Version control provides history for declarative non-secret configuration.

---

# 260. Secret Backup

Secret-manager backup/recovery follows enterprise infrastructure policy.

---

# 261. Disaster Recovery

Disaster recovery must include:

- configuration repositories
- secret-management infrastructure
- encryption keys
- identity configuration

as applicable.

---

# 262. Secret Recovery

Secret recovery must not depend on undocumented credentials held by individual developers.

---

# 263. Ownership

Every production secret must have an accountable owner or owning system/team.

---

# 264. Orphaned Secrets

Unused secrets must be identified and removed.

---

# 265. Secret Inventory

The organization should maintain an inventory of production secrets and their consumers where platform capabilities permit.

---

# 266. Rotation Metadata

Secret inventory should include where appropriate:

- owner
- purpose
- consumers
- creation
- expiration
- rotation policy

without exposing secret content.

---

# 267. Configuration Inventory

The organization should be able to identify configuration sources for every production service.

---

# 268. Runtime Traceability

For a running pod, the platform should be able to trace:

```text
Pod

↓

Image Digest

↓

Application Version

↓

Configuration Revision

↓

Secret References

↓

Workload Identity

↓

Deployment Revision
```

---

# 269. Anti-Patterns

The following are prohibited:

- rebuilding the application per environment
- production secrets in source code
- production secrets in `application.yml`
- production secrets in ConfigMaps
- treating Kubernetes Secret Base64 as encryption
- committing plaintext credentials
- logging secrets
- logging authorization tokens
- exposing secrets through Actuator
- printing secret-bearing configuration objects
- using unsafe configuration defaults
- silently ignoring missing mandatory configuration
- scattering large numbers of `@Value` declarations
- ambiguous timeout units
- unbounded pool configuration
- uncontrolled runtime property overrides
- uncontrolled dynamic refresh of arbitrary properties
- using feature flags as authorization
- permanent obsolete feature flags
- manually editing production configuration as normal practice
- unmanaged `kubectl patch`
- allowing GitOps drift to persist silently
- sharing production credentials with DEV/HML
- embedding cloud access keys when workload identity exists
- giving every service access to every secret
- using runtime database credentials for unrestricted schema administration
- rotating credentials without considering active connection pools
- assuming secret-file replacement automatically refreshes application state
- deleting old credentials before consumers transition
- restoring a known-compromised secret during rollback
- using production secrets in tests
- sending runtime secrets to build jobs unnecessarily
- passing secrets through Docker ARG
- storing secrets in image layers
- treating configuration-write access as low risk
- changing production endpoints without review
- using arbitrary environment overrides outside the declarative source of truth
- depending on undocumented developer-held recovery credentials

---

# 270. Positive Consequences

The decision provides:

- immutable environment-independent artifacts
- safer secret management
- typed configuration
- startup validation
- reduced configuration errors
- stronger environment separation
- easier credential rotation
- improved zero-downtime operation
- better GitOps compatibility
- configuration traceability
- configuration rollback
- reduced configuration drift
- stronger workload identity
- reduced long-lived credentials
- improved incident analysis
- safer feature rollout
- clearer ownership
- better compliance evidence

---

# 271. Negative Consequences

The decision introduces:

- additional configuration modeling
- validation code
- secret-manager integration
- workload-identity configuration
- GitOps infrastructure
- rotation testing
- feature-flag governance
- configuration review overhead
- compatibility requirements during rolling deployment
- additional operational procedures

These costs are accepted because configuration and credentials are production security boundaries.

---

# 272. Neutral Consequences

The decision also means:

- some configuration changes require rolling restart
- not every property can be dynamically changed
- secret rotation strategies differ by dependency
- GitOps introduces reconciliation semantics
- feature flags require lifecycle management
- stricter configuration validation may expose previously hidden deployment errors
- local development configuration may differ operationally from production while preserving the same application artifact

---

# 273. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Production secret committed to Git | Critical | Medium | External secret manager and secret scanning |
| Secret exposed in logs | Critical | Medium | Redaction and logging standards |
| Invalid configuration reaches production | High | Medium | Typed validation and fail-fast startup |
| DEV configuration used in PROD | Critical | Low | Environment separation and review |
| Workload receives excessive secret access | Critical | Medium | Least-privilege identity |
| Static cloud credential compromised | Critical | Medium | Workload identity |
| Rotation causes outage | High | Medium | Overlap and rolling transition |
| Connection pool continues old credential | High | Medium | Explicit pool rotation strategy |
| Dynamic configuration introduces invalid state | High | Medium | Runtime validation and last-known-good |
| Feature flag bypasses security | Critical | Low | Prohibit security authorization through flags |
| Manual production change creates drift | High | Medium | GitOps reconciliation |
| GitOps repository compromised | Critical | Low | Branch protection and review |
| Configuration rollback restores unsafe value | High | Medium | Security-aware rollback |
| Secret rollback restores compromised credential | Critical | Low | Never reactivate compromised credentials |
| Production endpoint redirected maliciously | Critical | Low | Controlled configuration write access |
| Certificate expires | High | Medium | Expiration monitoring and rotation |
| Secret manager unavailable | High | Low | Resilient retrieval/last-known-good where safe |

---

# 274. Implementation Guidance

The following rules are mandatory:

1. The same application artifact must be promoted across DEV, HML and PROD.
2. Environment-specific configuration must be externalized.
3. Production secrets must not be embedded in application artifacts.
4. Related Spring configuration must use typed `@ConfigurationProperties`.
5. Mandatory configuration must be validated at startup.
6. Invalid mandatory configuration must fail fast.
7. Defaults must be safe and intentional.
8. Duration and size properties must use explicit units/types.
9. Production property sources and precedence must be controlled.
10. ConfigMaps must contain only non-sensitive configuration.
11. Kubernetes Secret Base64 encoding must not be treated as encryption.
12. Production secrets must originate from approved secret-management infrastructure.
13. Plain production secrets must never be committed to Git.
14. Workload identity should replace long-lived cloud credentials where supported.
15. Services must receive only secrets they require.
16. Runtime and migration database credentials should be separated where practical.
17. Secret values must never appear in logs, metrics or traces.
18. Actuator must not expose sensitive configuration.
19. Secrets must be rotatable without rebuilding application artifacts.
20. Critical credential rotation should support zero-downtime transition where practical.
21. Dynamic configuration must be explicitly designed rather than enabled globally.
22. Invalid dynamic configuration must preserve the last known valid state where safe.
23. Feature flags must not replace authorization.
24. Temporary feature flags must have a removal lifecycle.
25. Declarative non-secret configuration should be version controlled.
26. Production configuration changes should use reviewed pull requests.
27. GitOps should reconcile approved desired state where platform capabilities permit.
28. Routine manual production configuration changes are prohibited.
29. Emergency changes must be reconciled back into the source of truth.
30. DEV, HML and PROD must use separate credentials.
31. Production credentials must never be reused in development.
32. Configuration evolution must remain compatible with rolling deployments.
33. Restart-required changes must use controlled rolling deployment.
34. Configuration changes must be auditable.
35. Configuration rollback must be supported.
36. Compromised secrets must never be restored during rollback.
37. Secret access must follow least privilege.
38. Configuration-write access must be treated as security-sensitive.
39. Production endpoint changes require controlled review.
40. Secret and certificate expiration must be monitored.
41. CI build stages must not receive unnecessary production secrets.
42. Production secrets must not be passed through Docker build arguments.
43. Local secret files must be excluded from Git.
44. Tests must use fake or isolated test credentials.
45. Runtime deployments should identify application and configuration revisions.
46. Secret references/versions should be traceable without recording secret values.
47. Configuration drift should be detectable.
48. Security-sensitive configuration must fail toward safe behavior.
49. Shared global configuration must be minimized.
50. Configuration ownership and secret ownership must be explicit.

---

# 275. Validation

The decision will be validated through:

- Spring Boot configuration binding tests
- Bean Validation tests
- startup failure tests
- missing-property tests
- invalid-range tests
- configuration precedence tests
- secret scanning
- SAST
- container inspection
- Actuator security tests
- logging/redaction tests
- Kubernetes manifest validation
- ConfigMap/Secret policy validation
- workload-identity tests
- secret-manager integration tests
- credential-rotation exercises
- database-pool rotation tests
- certificate-rotation tests
- feature-flag failure tests
- rolling configuration deployment tests
- GitOps reconciliation tests
- drift-detection exercises
- configuration rollback tests
- environment-isolation tests
- production-readiness review

---

# 276. Success Criteria

The decision is successful when:

- one immutable artifact can run across all environments
- production secrets do not exist in source repositories
- configuration errors are detected before traffic is served
- configuration properties are typed and validated
- secrets are delivered through approved mechanisms
- workloads use least-privilege identities
- credentials can be rotated predictably
- rotation does not require rebuilding applications
- critical rotation can occur without downtime
- dynamic configuration changes are controlled
- feature flags have ownership and lifecycle
- production configuration is versioned and reviewed
- configuration drift is detectable
- emergency changes leave an audit trail
- configuration can be rolled back safely
- every production deployment can be correlated with its configuration revision
- secret access and modification are auditable
- environment-specific configuration cannot silently redirect workloads to the wrong environment

---

# 277. Alternatives Rejected

## 277.1 Build Separate Artifact Per Environment

Rejected because it breaks artifact immutability and supply-chain promotion guarantees.

---

## 277.2 Store Secrets in application.yml

Rejected because secrets become part of source code or the application artifact.

---

## 277.3 Use ConfigMaps for Everything

Rejected because ConfigMaps do not provide appropriate secret protection.

---

## 277.4 Kubernetes Secrets as Complete Secret Management

Rejected as the universal model because Secret objects alone do not provide the required enterprise lifecycle, rotation and governance capabilities.

---

## 277.5 Environment Variables Without Governance

Rejected because uncontrolled overrides create configuration drift and weak auditability.

---

## 277.6 Dynamic Refresh for Every Property

Rejected because many application structures cannot be changed safely without restart.

---

## 277.7 Manual Production Configuration

Rejected because it destroys reproducibility and creates configuration drift.

---

## 277.8 Shared Credentials Across Environments

Rejected because compromise of a lower environment could compromise production.

---

## 277.9 Long-Lived Static Cloud Credentials

Rejected where workload identity is available.

---

## 277.10 Feature Flags for Authorization

Rejected because feature deployment state is not an authorization model.

---

# 278. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-009: Use Apache Kafka for Integration Events
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-022: Adopt API Contract Governance
- ADR-023: Adopt API Security Standards
- ADR-024: Adopt Software Supply Chain Security
- ADR-025: Adopt Kubernetes Runtime Security Standards
- ADR-027: Adopt Production Incident Management and Operational Readiness Standards

---

# 279. References

- Spring Boot Externalized Configuration
- Spring Boot Configuration Properties
- Spring Boot Actuator
- Jakarta Bean Validation
- Kubernetes ConfigMaps
- Kubernetes Secrets
- Kubernetes ServiceAccounts
- Kubernetes RBAC
- Kubernetes Security Checklist
- Kubernetes Secrets Store CSI Driver
- GitOps Principles
- OpenGitOps
- OWASP Secrets Management Cheat Sheet
- OWASP Kubernetes Security Cheat Sheet
- NIST Secure Software Development Framework
- Twelve-Factor App — Config
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-023: Adopt API Security Standards
- ADR-024: Adopt Software Supply Chain Security
- ADR-025: Adopt Kubernetes Runtime Security Standards

---

# 280. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial configuration and secret-management baseline |

---

# 281. Decision Summary

The Enterprise Order Platform adopts the following separation:

```text
APPLICATION ARTIFACT
Immutable
Built once
Signed
Promoted across environments

+

NON-SECRET CONFIGURATION
Externalized
Typed
Validated
Versioned
Reviewed
Auditable

+

SECRETS
External
Protected
Least privilege
Rotatable
Never committed
```

The application lifecycle becomes:

```text
SOURCE

↓

BUILD

↓

TEST

↓

SCAN

↓

IMMUTABLE ARTIFACT

↓

DEV CONFIGURATION + DEV SECRETS

↓

HML CONFIGURATION + HML SECRETS

↓

PROD CONFIGURATION + PROD SECRETS
```

while the artifact remains:

```text
THE SAME DIGEST
```

across environments.

Spring Boot configuration follows:

```text
@ConfigurationProperties

↓

Typed Values

↓

Bean Validation

↓

Fail Fast

↓

Application Starts Safely
```

Secret access follows:

```text
Pod

↓

Dedicated ServiceAccount

↓

Workload Identity

↓

Approved Secret Manager

↓

Only Required Secret

↓

Application
```

Production configuration follows:

```text
Git

↓

Pull Request

↓

Review

↓

Approved Desired State

↓

GitOps Reconciliation

↓

Kubernetes

↓

Observed Runtime
```

rather than:

```text
Engineer

↓

kubectl edit

↓

Unknown Production State
```

Credential rotation follows:

```text
Create New Credential

↓

Make New Credential Available

↓

Transition Workloads

↓

Verify

↓

Revoke Old Credential
```

and never:

```text
Edit source code

↓

Rebuild application

↓

Deploy just to change password
```

The complete trust chain established by ADR-024, ADR-025 and ADR-026 therefore becomes:

```text
REVIEWED SOURCE

↓

TRUSTED BUILD

↓

IMMUTABLE SIGNED ARTIFACT

↓

VERIFIED KUBERNETES ADMISSION

↓

RESTRICTED RUNTIME

↓

LEAST-PRIVILEGE WORKLOAD IDENTITY

↓

EXTERNAL SECRET MANAGEMENT

↓

VALIDATED CONFIGURATION

↓

AUDITED DESIRED STATE

↓

RUNNING PRODUCTION WORKLOAD
```

A production deployment is therefore not completely identified only by:

```text
application version
```

but by the combination:

```text
Artifact Digest

+

Configuration Revision

+

Deployment Revision

+

Secret References

+

Workload Identity
```

without ever exposing secret values.

The platform's operational principle is:

```text
Artifacts are immutable.

Configuration is explicit.

Secrets are external.

Identity is least privilege.

Changes are reviewed.

Runtime state is reconciled.

Credentials are rotatable.

Production is reproducible.
```

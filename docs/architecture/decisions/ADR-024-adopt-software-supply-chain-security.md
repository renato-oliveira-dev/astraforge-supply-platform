# ADR-024: Adopt Software Supply Chain Security

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-024 |
| Title | Adopt Software Supply Chain Security |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Software Supply Chain, CI/CD, Dependencies, Containers and Artifact Security |
| Related Work Items | SBOM, SLSA, Provenance, Artifact Signing, Vulnerability Scanning, Secret Scanning, Container Security |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

The Enterprise Order Platform is built and deployed through automated software delivery pipelines.

The platform includes:

- Java 21
- Spring Boot
- Gradle
- Gradle Wrapper
- PostgreSQL
- Flyway
- Amazon SQS
- Docker/OCI containers
- Kubernetes
- CI/CD
- artifact repositories
- container registries
- third-party dependencies
- base container images
- OpenTelemetry
- SAST
- dependency scanning
- container scanning
- secret scanning

A production application is not composed solely of source code written by the development team.

Its effective software supply chain includes:

```text
Source Code

+

Build Scripts

+

Build Tools

+

Dependencies

+

Plugins

+

Base Images

+

CI/CD Infrastructure

+

Artifact Repository

+

Container Registry

+

Deployment Manifests

+

Runtime Platform
```

Compromise of any trusted stage can compromise the final production workload.

The platform therefore requires explicit software supply-chain security controls.

---

# 2. Problem Statement

The platform requires a standardized supply-chain security model that:

- protects source-code integrity
- protects build integrity
- controls third-party dependencies
- prevents dependency confusion
- identifies vulnerable dependencies
- detects committed secrets
- protects CI/CD credentials
- produces immutable artifacts
- generates SBOMs
- records artifact provenance
- supports artifact signing
- validates container images
- controls base images
- prevents mutable production deployments
- supports reproducible builds
- protects artifact repositories
- governs CVE remediation
- supports controlled dependency updates
- separates build from deployment
- supports environment promotion
- prevents rebuilding per environment
- supports rollback
- integrates with Kubernetes
- supports auditability
- supports zero-downtime deployment

---

# 3. Decision Drivers

Primary decision drivers are:

1. artifact integrity
2. dependency security
3. build integrity
4. reproducibility
5. traceability
6. vulnerability management
7. secret protection
8. CI/CD least privilege
9. immutable deployment
10. rollback safety
11. provenance
12. auditability
13. automation
14. zero-downtime deployment
15. supply-chain attack resistance
16. regulatory readiness
17. operational consistency

---

# 4. Decision

The Enterprise Order Platform adopts a defense-in-depth Software Supply Chain Security model based on:

```text
Protected Source

+

Controlled Dependencies

+

Reproducible Build Inputs

+

Automated Security Scanning

+

SBOM

+

Build Provenance

+

Immutable Artifacts

+

Artifact Signing

+

Controlled Promotion

+

Runtime Verification
```

The same immutable application artifact must be promoted across environments.

---

# 5. Fundamental Principle

The platform adopts:

```text
Build once.

Verify once.

Promote the same artifact.
```

Production must not receive a separately rebuilt version of source code that was previously validated in another environment.

---

# 6. Software Supply Chain

The software supply chain begins before compilation.

It includes:

```text
Developer

↓

Source Repository

↓

Pull Request

↓

CI Runner

↓

Build Tool

↓

Dependency Repositories

↓

Compilation

↓

Tests

↓

Security Scans

↓

Artifact Creation

↓

SBOM

↓

Provenance

↓

Artifact Registry

↓

Container Registry

↓

Deployment Pipeline

↓

Kubernetes

↓

Production
```

Each transition represents a trust boundary.

---

# 7. Source Repository

The source repository is a critical security boundary.

Production code must originate from approved version-control repositories.

---

# 8. Protected Branches

Production branches must use branch protection.

Direct uncontrolled pushes to protected production branches are prohibited.

---

# 9. Pull Requests

Production changes must normally enter through reviewed pull requests.

---

# 10. Code Review

At least one qualified reviewer should review production changes according to organizational policy.

Security-sensitive changes may require specialized review.

---

# 11. Self-Approval

Where repository tooling and team size permit, authors should not be the sole approver of their own production changes.

---

# 12. Branch Protection

Protected branches should enforce applicable checks such as:

- successful build
- unit tests
- integration tests
- Sonar quality gate
- SAST
- dependency scanning
- secret scanning
- required reviews

---

# 13. Force Push

Force pushes to protected production branches should be disabled.

---

# 14. Commit Traceability

Production artifacts must be traceable to a source revision.

At minimum:

```text
Artifact

↓

Commit SHA
```

must be discoverable.

---

# 15. Signed Commits

Signed commits may be required according to enterprise source-control policy.

They complement but do not replace artifact provenance.

---

# 16. Build Tool

Gradle is the standard build system for Java services in the platform.

---

# 17. Gradle Wrapper

Projects must use the Gradle Wrapper.

Builds should execute:

```text
./gradlew
```

or:

```text
gradlew.bat
```

rather than depending on an uncontrolled globally installed Gradle version.

---

# 18. Wrapper Version

The Gradle Wrapper version must be version controlled.

Changing it is a build-infrastructure change and requires review.

---

# 19. Wrapper Integrity

Wrapper artifacts and configuration must not be modified from untrusted sources.

Where supported, wrapper integrity validation should be applied.

---

# 20. Build Reproducibility

Build inputs must be sufficiently controlled so that a given source revision produces predictably equivalent artifacts.

Perfect bit-for-bit reproducibility is desirable but not required for every component initially.

---

# 21. Non-Deterministic Builds

Avoid embedding uncontrolled values into artifacts, such as:

- current timestamps
- random identifiers
- machine-specific paths
- local environment state

unless required and intentionally governed.

---

# 22. Local Environment Independence

A production artifact must not depend on undocumented developer-machine state.

---

# 23. Build Environment

Production builds must execute in controlled CI environments.

Developer-built artifacts must not be manually uploaded and promoted as production releases.

---

# 24. Clean Build

CI must execute builds from a clean source checkout or equivalent isolated workspace.

---

# 25. Build Cache

Build caches may be used for performance, but they must not weaken artifact integrity.

Cache keys and trust boundaries must prevent untrusted artifacts from contaminating protected builds.

---

# 26. Dependencies

Third-party dependencies are executable supply-chain inputs.

They must be treated as code entering the application.

---

# 27. Explicit Dependency Versions

Production dependencies must use controlled versions.

Avoid uncontrolled dynamic versions such as:

```gradle
implementation("org.example:library:+")
```

---

# 28. Snapshot Dependencies

Uncontrolled snapshot dependencies are prohibited for production releases.

---

# 29. Dependency Locking

Dependency locking should be used where practical to ensure consistent transitive dependency resolution.

---

# 30. Transitive Dependencies

Transitive dependencies are part of the application's attack surface even when not explicitly declared.

They must be included in vulnerability analysis and SBOM generation.

---

# 31. Dependency Repository

Builds must resolve dependencies only from approved repositories.

---

# 32. Repository Allowlist

Uncontrolled arbitrary Maven repositories are prohibited in production builds.

Preferred:

```text
Approved enterprise artifact repository

↓

Controlled upstream repositories
```

---

# 33. Maven Central

Direct access to public dependency repositories may be restricted through enterprise artifact proxies according to infrastructure policy.

---

# 34. Dependency Confusion

Repository resolution must be designed to mitigate dependency-confusion attacks.

Internal package coordinates must not unexpectedly resolve from public repositories.

---

# 35. Internal Artifacts

Internal libraries must use controlled namespaces and repositories.

---

# 36. Dependency Verification

Gradle dependency verification should be adopted where practical.

Verification may use:

- checksums
- signatures

to detect unexpected dependency replacement.

---

# 37. Dependency Update

Dependencies must be updated intentionally.

---

# 38. Automated Dependency Updates

Automated tools may propose dependency updates through pull requests.

They must not bypass:

- tests
- security scans
- code review
- compatibility analysis

---

# 39. Major Dependency Upgrade

Major framework or library upgrades require explicit compatibility analysis.

Examples:

```text
Spring Boot 3.x → 4.x

Gradle 8.x → 9.x
```

---

# 40. Security Dependency Upgrade

Security patches may require accelerated adoption.

Compatibility risk must be balanced against vulnerability exposure.

---

# 41. Vulnerability Scanning

Dependencies must undergo automated vulnerability scanning.

---

# 42. Vulnerability Sources

Scanning should use trusted vulnerability intelligence such as:

- CVE
- NVD-derived data
- vendor advisories
- ecosystem advisories
- enterprise security feeds

according to available tooling.

---

# 43. Vulnerability Finding

A vulnerability finding must be evaluated using:

```text
Severity

+

Exploitability

+

Reachability

+

Exposure

+

Business Criticality

+

Available Fix
```

---

# 44. CVSS

CVSS is an input to risk analysis.

It is not the sole decision criterion.

---

# 45. Reachability

Where tooling supports it, dependency reachability analysis should distinguish:

```text
Vulnerable package exists
```

from:

```text
Vulnerable code path is reachable
```

The latter generally represents greater practical risk.

---

# 46. Vulnerability SLA

The organization should define remediation expectations by risk classification.

Example policy structure:

```text
Critical
→ immediate triage

High
→ expedited remediation

Medium
→ planned remediation

Low
→ normal backlog / risk review
```

Exact time limits belong to enterprise security policy.

---

# 47. No-Fix Vulnerability

When no patched version exists, the team must evaluate:

- compensating control
- dependency replacement
- feature disablement
- isolation
- formal risk acceptance

---

# 48. False Positive

A vulnerability may be classified as false positive only with technical evidence.

---

# 49. Risk Acceptance

Accepted vulnerabilities require:

- owner
- justification
- scope
- expiration/review date
- compensating controls where applicable

Permanent undocumented exceptions are prohibited.

---

# 50. Secret Scanning

Source repositories must undergo automated secret scanning.

---

# 51. Secret Types

Secret scanning should detect patterns such as:

- passwords
- API keys
- client secrets
- private keys
- cloud credentials
- access tokens
- database credentials

---

# 52. Secret Detection Is Not Prevention

Secret scanning complements secure secret-management practices.

It does not make committing secrets acceptable.

---

# 53. Detected Secret

A committed secret must be considered potentially compromised.

Removing it from the latest commit is insufficient.

---

# 54. Secret Response

If a real secret is committed:

```text
Revoke / rotate secret

↓

Remove from repository

↓

Evaluate history exposure

↓

Investigate usage

↓

Prevent recurrence
```

---

# 55. Git History

Rewriting Git history may reduce future exposure but does not replace credential rotation.

---

# 56. Test Secrets

Tests must use:

- fake values
- local credentials
- ephemeral credentials
- test-environment secrets

rather than production credentials.

---

# 57. CI/CD Credentials

CI/CD credentials are high-value security assets.

---

# 58. Pipeline Least Privilege

Pipeline identities must receive only permissions required for their stage.

---

# 59. Build Identity

A build job does not automatically require production deployment permissions.

---

# 60. Deployment Identity

Deployment permissions should be separated from source-build permissions where infrastructure permits.

---

# 61. Production Access

Only approved deployment workflows should receive production deployment authority.

---

# 62. Long-Lived CI Secrets

Long-lived static cloud credentials in CI/CD should be avoided where workload identity or short-lived federation is available.

---

# 63. Workload Identity

CI/CD should prefer short-lived federated identities where supported.

Conceptually:

```text
CI workload identity

↓

Federation

↓

Short-lived cloud credential
```

---

# 64. Credential Rotation

CI/CD credentials must be rotatable without source-code changes.

---

# 65. Pipeline Logging

Pipeline logs must not expose:

- secrets
- tokens
- passwords
- signing keys
- deployment credentials

---

# 66. Untrusted Pull Requests

Untrusted pull-request code must not automatically receive privileged production secrets.

---

# 67. Pull Request Threat

A malicious or compromised pull request may attempt to:

```text
print environment variables

read credential files

upload secrets externally
```

Pipeline architecture must account for this threat.

---

# 68. Build Artifact

A build artifact must be immutable after publication.

---

# 69. Artifact Identity

Every artifact must have an immutable identifier.

Examples include:

```text
version

digest

checksum
```

---

# 70. Mutable Version

Overwriting an existing released artifact with different content is prohibited.

---

# 71. Artifact Repository

Released artifacts must be stored in an approved artifact repository or container registry.

---

# 72. Artifact Repository Access

Repository write permissions must be restricted.

Most consumers should require read-only access.

---

# 73. Artifact Retention

Artifact retention must support:

- rollback
- audit
- incident investigation
- compliance

according to enterprise policy.

---

# 74. Container Images

Container images are production artifacts and must be immutable after publication.

---

# 75. Container Tags

Human-readable tags may be used for navigation.

Example:

```text
orders-service:1.8.3
```

but deployment identity should rely on immutable artifact identity where supported.

---

# 76. Mutable Latest Tag

Production deployment using only:

```text
:latest
```

is prohibited.

---

# 77. Image Digest

Production Kubernetes deployments should prefer immutable image digests.

Example:

```text
registry.example/orders-service@sha256:...
```

---

# 78. Tag and Digest

A release may retain both:

```text
orders-service:1.8.3
```

and its immutable digest.

The digest identifies exact image content.

---

# 79. Base Images

Container base images are dependencies.

---

# 80. Approved Base Images

Production services must use approved base images.

---

# 81. Minimal Base Image

Base images should contain only required runtime components.

Smaller attack surface generally reduces:

- unnecessary packages
- vulnerabilities
- maintenance burden

---

# 82. JDK vs JRE Runtime

Build images may require a full JDK.

Runtime images should contain only the runtime capabilities required by the service.

---

# 83. Base Image Version

Base image versions must be controlled.

Avoid:

```dockerfile
FROM image:latest
```

for reproducible production builds.

---

# 84. Base Image Digest

Where practical, base images should be pinned by immutable digest.

---

# 85. Base Image Updates

Pinned images do not update themselves.

The organization must actively monitor and refresh base images for security patches.

---

# 86. Container User

Production containers should run as non-root where practical.

---

# 87. Container Filesystem

Read-only root filesystems should be used where application requirements permit.

---

# 88. Container Capabilities

Unnecessary Linux capabilities should be removed.

---

# 89. Container Scanning

Every production container image must undergo vulnerability scanning.

---

# 90. Scan Timing

Container scanning should occur:

- during CI/CD
- before production promotion
- periodically after publication where tooling supports continuous registry scanning

---

# 91. Newly Disclosed Vulnerability

An image considered secure at build time may become vulnerable later because a new CVE is disclosed.

Registry rescanning or equivalent continuous monitoring is therefore required where available.

---

# 92. Container Scan Scope

Scanning must include:

- OS packages
- language dependencies
- base image vulnerabilities

where tooling supports them.

---

# 93. Malware Scanning

Artifact or container malware scanning may be applied according to enterprise platform capabilities and risk.

---

# 94. SBOM

Every production application artifact should have a Software Bill of Materials.

---

# 95. SBOM Purpose

An SBOM provides an inventory of software components included in an artifact.

It supports questions such as:

```text
Which production services contain library X?
```

---

# 96. SBOM Content

An SBOM should include, where applicable:

- component name
- version
- package identifier
- dependency relationship
- supplier information
- hashes

according to the selected SBOM standard.

---

# 97. SBOM Format

The platform should use an industry-standard machine-readable format such as:

```text
CycloneDX
```

or:

```text
SPDX
```

according to enterprise tooling.

---

# 98. SBOM Generation

SBOMs should be generated automatically during CI/CD.

Manual SBOM maintenance is prohibited.

---

# 99. SBOM Artifact Association

An SBOM must be associated with the exact artifact it describes.

---

# 100. SBOM Immutability

The SBOM for an immutable released artifact must not be silently replaced with unrelated content.

---

# 101. SBOM Storage

SBOMs should be retained with or alongside release artifacts.

---

# 102. SBOM Is Not a Vulnerability Scan

An SBOM answers:

```text
What is inside?
```

A vulnerability scanner answers:

```text
What known security issues affect what is inside?
```

Both capabilities are required.

---

# 103. Provenance

Build provenance records how an artifact was produced.

---

# 104. Provenance Questions

Provenance should help answer:

```text
Which source produced this artifact?

Which build system built it?

Which workflow executed?

Which dependencies or build inputs were involved?

When was it built?

Which artifact digest resulted?
```

---

# 105. SLSA

The platform adopts SLSA principles as guidance for improving build provenance and supply-chain integrity.

---

# 106. SLSA Adoption

SLSA adoption may be incremental.

The organization should progressively improve:

- source integrity
- build isolation
- provenance generation
- provenance verification
- tamper resistance

---

# 107. Provenance Generation

Provenance should be generated automatically by trusted CI/CD infrastructure.

---

# 108. Self-Declared Provenance

A text file manually written by application code claiming:

```text
built securely
```

is not trustworthy provenance.

---

# 109. Provenance Binding

Provenance must identify the artifact digest it describes.

---

# 110. Artifact Signing

Production artifacts should support cryptographic signing according to enterprise tooling.

---

# 111. Signing Purpose

Artifact signing provides evidence that:

```text
Trusted identity

approved or produced

this exact artifact.
```

---

# 112. Signing Key

Artifact signing keys are highly sensitive.

They must not be:

- committed to repositories
- stored in application source
- exposed to ordinary build jobs unnecessarily

---

# 113. Keyless Signing

Where supported, keyless signing using short-lived workload identity should be preferred over long-lived private signing keys.

---

# 114. Image Signing

Container images should be signed according to platform supply-chain capabilities.

---

# 115. Signature Binding

Signatures must bind to immutable artifact content, normally through its digest.

---

# 116. Signature Verification

Signing provides limited value if signatures are never verified.

Deployment infrastructure should verify signatures or attestations where supported.

---

# 117. Runtime Admission

Kubernetes admission controls should progressively enforce supply-chain policies.

Examples:

```text
Only approved registries

Only signed images

No :latest

Approved provenance

Allowed base-image policy
```

depending on platform capabilities.

---

# 118. Admission Failure

An artifact that violates mandatory supply-chain policy should be rejected before workload execution.

---

# 119. Artifact Promotion

Environment progression must use artifact promotion.

---

# 120. Build Once

The standard release lifecycle is:

```text
Source Commit

↓

Build Artifact

↓

Test

↓

Scan

↓

Generate SBOM

↓

Generate Provenance

↓

Sign

↓

Publish

↓

Deploy DEV

↓

Promote same artifact to HML

↓

Promote same artifact to PROD
```

---

# 121. Environment Rebuild

The following model is prohibited:

```text
Build DEV artifact

Build HML artifact again

Build PROD artifact again
```

because each build can produce different content.

---

# 122. Configuration

Environment-specific behavior must come from externalized configuration rather than rebuilding application code.

---

# 123. Artifact vs Configuration

The platform distinguishes:

```text
Immutable application artifact
```

from:

```text
Environment-specific configuration
```

---

# 124. Configuration Integrity

Production configuration must also be controlled and auditable.

Artifact immutability alone does not protect against malicious configuration.

---

# 125. Infrastructure as Code

Deployment and infrastructure configuration should be managed as code where practical.

---

# 126. IaC Review

Infrastructure changes require review and automated validation.

---

# 127. IaC Security Scanning

Infrastructure definitions should undergo security scanning where supported.

---

# 128. Kubernetes Manifests

Kubernetes deployment definitions must be version controlled.

---

# 129. Helm

If Helm is used, chart versions and values must be governed.

Production deployment must not depend on undocumented manually edited cluster state.

---

# 130. Deployment Artifact Identity

A deployment record must identify the exact artifact deployed.

Preferred:

```text
image digest
```

rather than only a mutable tag.

---

# 131. Release Metadata

Release metadata should include:

- application version
- commit SHA
- image digest
- build identifier
- SBOM reference
- provenance reference
- deployment timestamp

where tooling supports it.

---

# 132. Versioning

Application versions must be immutable once released.

---

# 133. Semantic Versioning

Semantic Versioning may be used for human-readable application versions where appropriate.

The immutable digest remains the authoritative artifact identity.

---

# 134. Snapshot Release

Snapshot versions must not be promoted to production as final releases.

---

# 135. Release Candidate

Release-candidate artifacts may be promoted only if the exact candidate artifact becomes the approved production artifact.

Rebuilding the same source after approval creates a different supply-chain artifact.

---

# 136. Promotion Approval

Promotion to production should require the approved organizational controls.

This may include:

- automated quality gates
- manual approval
- change-management approval

depending on service criticality.

---

# 137. Promotion Gate

Production promotion should verify:

```text
Artifact exists

Artifact immutable

Required tests passed

Security scans acceptable

SBOM available

Provenance available

Signature valid where enforced

Deployment policy satisfied
```

---

# 138. Promotion Metadata

Promotion must not alter the application artifact.

Only environment deployment metadata and configuration may differ.

---

# 139. Rollback

Rollback must use a previously known immutable artifact.

---

# 140. Rollback Rebuild

Rebuilding an old Git commit during an incident is not the preferred rollback mechanism.

The previously released artifact should already exist.

---

# 141. Rollback Trust

A rollback artifact must remain subject to security policy.

A known critically vulnerable historical artifact may not be safe merely because it previously worked.

---

# 142. Artifact Retention for Rollback

Retention policies must preserve a reasonable number of previous production releases.

---

# 143. Database Compatibility

Artifact rollback must respect database compatibility requirements from ADR-021.

---

# 144. Flyway

Database migrations remain immutable after application.

Existing migrations must never be edited to make a rollback or deployment succeed.

---

# 145. New Migration

Any database correction must use a new Flyway migration with a new version.

---

# 146. Migration Artifact

Database migrations are part of the application supply chain.

They must undergo:

- review
- version control
- testing
- security analysis

---

# 147. Migration Privileges

Migration credentials should be separated from runtime credentials where practical.

---

# 148. Migration Supply-Chain Risk

A malicious migration can compromise:

- data integrity
- confidentiality
- availability

Migration review is therefore security relevant.

---

# 149. Database Script Download

Production pipelines must not download and execute arbitrary unverified database scripts at deployment time.

---

# 150. External Build Scripts

Build pipelines must not download and execute arbitrary remote scripts without integrity and trust controls.

Unsafe pattern:

```text
curl URL | sh
```

---

# 151. Build Plugin

Gradle plugins are executable build dependencies.

They require the same trust considerations as application dependencies.

---

# 152. Plugin Repository

Gradle plugin repositories must be controlled.

---

# 153. Build Script Review

Changes to:

```text
build.gradle

settings.gradle

gradle.properties

Dockerfile

CI pipeline definitions
```

are supply-chain-sensitive changes and require review.

---

# 154. Pipeline Definition

CI/CD pipeline definitions must be version controlled where supported.

---

# 155. Pipeline Modification

A pipeline change can bypass application security checks.

Therefore pipeline changes are security-sensitive.

---

# 156. Security Gate Removal

Removing or weakening:

- SAST
- dependency scan
- secret scan
- tests
- signature verification

requires explicit review.

---

# 157. Build Runner

Build runners must be isolated according to platform security requirements.

---

# 158. Ephemeral Runner

Ephemeral CI runners are preferred for sensitive builds where available because they reduce persistent cross-build state.

---

# 159. Shared Runner

Shared runners require strong isolation between workloads.

---

# 160. Build Workspace

Sensitive files from one build must not remain available to unrelated later builds.

---

# 161. Docker Build Context

Docker build context must exclude unnecessary files.

Use:

```text
.dockerignore
```

to avoid accidentally including:

- `.git`
- local secrets
- reports
- credentials
- development files

---

# 162. Multi-Stage Build

Multi-stage Docker builds should be used where appropriate.

Example:

```text
Build stage

↓

Runtime stage
```

This prevents unnecessary build tools from entering the runtime image.

---

# 163. Build Secrets

Secrets required during container build must use secure build-secret mechanisms where supported.

They must not be persisted into image layers.

---

# 164. Docker ARG

Sensitive secrets should not be passed through ordinary Docker `ARG` if they may remain recoverable in image history or build metadata.

---

# 165. Image Layer Inspection

Security review must assume image layers can be inspected.

Deleting a secret in a later Docker layer does not necessarily remove it from previous layers.

---

# 166. Runtime Secrets

Runtime secrets must be injected through the approved secret mechanism rather than embedded in images.

---

# 167. Image Registry

Production images must reside in approved registries.

---

# 168. Registry Authentication

Registry write access must use authenticated identities.

Anonymous production image publishing is prohibited.

---

# 169. Registry Immutability

Released image tags should be immutable where registry capabilities permit.

---

# 170. Registry Retention

Registry cleanup must not remove artifacts still required for:

- active workloads
- rollback
- audit
- incident response

---

# 171. Registry Scanning

Registry-level continuous vulnerability scanning should complement build-time scanning where available.

---

# 172. Quarantine

Artifacts that fail mandatory security policy should not be promotable to production.

They may be quarantined or marked ineligible according to repository capabilities.

---

# 173. Artifact Metadata

Security findings should be associated with artifact identity.

---

# 174. Post-Release CVE

When a new vulnerability is discovered after deployment:

```text
Identify affected SBOMs

↓

Identify deployed artifacts

↓

Assess exploitability

↓

Patch dependency/base image

↓

Build new artifact

↓

Run validation

↓

Promote replacement

↓

Verify production
```

---

# 175. Do Not Patch Running Container

Production containers must not be manually modified in place.

---

# 176. Immutable Infrastructure

A vulnerability fix requires a new artifact and deployment.

Preferred:

```text
Patch source/build inputs

↓

Build new image

↓

Deploy
```

not:

```text
kubectl exec

↓

install package manually
```

---

# 177. Drift

Runtime workloads should match declared deployment state.

Manual production changes create configuration drift and weaken provenance.

---

# 178. Drift Detection

Infrastructure should detect or minimize unauthorized drift where platform tooling permits.

---

# 179. Emergency Change

Emergency releases must still preserve:

- traceability
- artifact identity
- security scanning
- approval evidence

Controls may be expedited but must not disappear.

---

# 180. Break-Glass Pipeline

If a break-glass deployment mechanism exists, it must be:

- restricted
- strongly authenticated
- audited
- time bounded
- reviewed afterward

---

# 181. Security Exception

Supply-chain security exceptions require formal risk acceptance.

---

# 182. Exception Expiration

Exceptions must have a review or expiration date.

---

# 183. Exception Scope

Exceptions must be narrowly scoped.

Example:

```text
specific CVE

specific artifact version

specific service

specific expiration date
```

rather than:

```text
ignore all dependency findings
```

---

# 184. Quality Gate

Supply-chain security is part of the production quality gate.

---

# 185. Sonar

Sonar quality gates remain required according to project standards.

Sonar complements but does not replace supply-chain security scanning.

---

# 186. SAST

SAST evaluates source-level security weaknesses.

---

# 187. Dependency Scan

Dependency scanning evaluates known vulnerabilities in third-party components.

---

# 188. Container Scan

Container scanning evaluates runtime image contents.

---

# 189. Secret Scan

Secret scanning identifies accidentally committed credentials.

---

# 190. SBOM

SBOM inventories artifact composition.

---

# 191. Provenance

Provenance establishes how an artifact was produced.

---

# 192. Signature

Artifact signing establishes cryptographic trust over immutable artifact identity.

---

# 193. Complementary Controls

These controls are complementary:

```text
SAST
≠
Dependency Scan
≠
Container Scan
≠
Secret Scan
≠
SBOM
≠
Provenance
≠
Signature
```

No one control replaces the others.

---

# 194. Security Policy as Code

Supply-chain policies should be automated as code where platform tooling supports it.

---

# 195. Policy Examples

Policies may enforce:

```text
No critical vulnerabilities

No unsigned production images

No latest tags

Approved registry only

Required SBOM

Required provenance

Non-root container

Approved base image
```

---

# 196. Policy Versioning

Security policies themselves must be versioned and auditable.

---

# 197. Policy Rollout

New restrictive policies should be introduced carefully.

A useful transition may be:

```text
Observe

↓

Warn

↓

Enforce
```

where security urgency permits.

---

# 198. Policy Bypass

Policy bypass requires explicit authorization and audit.

---

# 199. Production Admission

Production Kubernetes clusters should eventually enforce mandatory artifact trust policies at admission time.

---

# 200. Artifact Verification

Before deployment, the platform should be able to answer:

```text
What is this artifact?

Who built it?

From which source?

Was it scanned?

What does it contain?

Was it approved?

Was it modified?

```

---

# 201. Release Traceability

For every running production workload, the organization should be able to trace:

```text
Pod

↓

Image Digest

↓

Artifact

↓

Provenance

↓

Build

↓

Commit

↓

Pull Request
```

---

# 202. Reverse Vulnerability Traceability

For a newly disclosed vulnerability, the organization should be able to trace:

```text
CVE / Component

↓

SBOM Inventory

↓

Affected Artifact

↓

Affected Service

↓

Affected Environment
```

---

# 203. Deployment Inventory

The platform should maintain or derive an inventory of deployed artifact versions.

---

# 204. Environment Promotion

The standard environment flow is:

```text
DEV

↓

HML

↓

PROD
```

or the equivalent approved environment topology.

---

# 205. Promotion Rule

Promotion means:

```text
Same artifact digest
```

moving to the next environment.

---

# 206. Environment-Specific Tag

Environment tags may exist for convenience, but they must not replace immutable artifact identity.

---

# 207. Configuration Promotion

Configuration changes may have their own promotion lifecycle.

They must remain versioned and auditable.

---

# 208. Feature Flags

Feature flags allow behavior to vary without rebuilding the artifact.

They must be governed separately from artifact integrity.

---

# 209. Feature Flag Security

Feature flags must not bypass mandatory authentication or authorization controls.

---

# 210. Production Artifact Modification

After production promotion, the artifact must never be modified.

A correction requires:

```text
new source revision

or controlled build-input change

↓

new artifact

↓

new digest

↓

new release
```

---

# 211. Build Number

Build numbers may identify pipeline executions.

They do not replace semantic version or immutable artifact digest.

---

# 212. Commit SHA

Commit SHA identifies source state.

It does not by itself identify the final binary artifact because build inputs also matter.

---

# 213. Digest

The artifact digest identifies exact artifact content.

This is the strongest deployment identity among ordinary release identifiers.

---

# 214. Version + Commit + Digest

Recommended release metadata includes:

```text
Version

Commit SHA

Artifact Digest
```

Each serves a different purpose.

---

# 215. Provenance and Digest

Provenance must bind:

```text
Source

+

Build process

↓

Artifact digest
```

---

# 216. Release Notes

Release notes should identify meaningful:

- features
- fixes
- security changes
- migrations
- compatibility considerations

without exposing sensitive security details unnecessarily.

---

# 217. Database Migration Release Notes

Releases containing Flyway migrations should make database-change implications visible to operators.

---

# 218. Breaking Deployment Change

Supply-chain controls must integrate with ADR-021 deployment compatibility.

A perfectly signed artifact can still cause downtime if application/database compatibility is incorrect.

---

# 219. Security Does Not Replace Architecture

Supply-chain security establishes artifact trust.

It does not replace:

- application security
- API authorization
- resilience
- observability
- contract compatibility
- database migration discipline

---

# 220. Third-Party Build Actions

Third-party CI/CD actions or plugins are supply-chain dependencies.

---

# 221. CI Action Pinning

Third-party CI actions should be pinned to immutable versions or commit identifiers where platform tooling permits.

---

# 222. Floating CI Actions

Using uncontrolled floating references for privileged pipeline actions is discouraged.

---

# 223. CI Plugin Review

Pipeline plugins receiving secrets or deployment permissions require stronger review.

---

# 224. Artifact Download

Pipelines must download artifacts only from approved trusted repositories.

---

# 225. Checksum Verification

Downloaded external build tools or binaries should undergo checksum/signature verification where supported.

---

# 226. Binary Provenance

Unverified binaries manually copied into repositories are prohibited unless formally governed.

---

# 227. Vendored Dependencies

Vendored dependencies must remain traceable to:

- source
- version
- license
- security status

---

# 228. License Governance

Dependency selection must comply with enterprise open-source license policy.

---

# 229. SBOM and License

SBOM tooling should include license metadata where available.

---

# 230. End-of-Life Dependencies

Dependencies or base images that no longer receive security support should be replaced.

---

# 231. Java Runtime

Production Java runtime versions must remain on supported security-update lines.

---

# 232. Spring Boot

Spring Boot versions must remain within supported security-maintenance windows according to enterprise policy.

---

# 233. PostgreSQL Driver

Database drivers must be included in dependency vulnerability governance.

---

# 234. SQS Client

AWS SQS client libraries must be included in dependency vulnerability governance.

---

# 235. Test Dependencies

Test dependencies are also supply-chain inputs because they execute in CI.

They require security governance even when they do not ship in the runtime artifact.

---

# 236. Build Dependencies

Build plugins can execute arbitrary code during compilation.

They require equal or greater scrutiny than many runtime libraries.

---

# 237. Development Tools

IDE plugins and local developer tooling are outside the primary artifact pipeline but remain part of broader enterprise endpoint security.

---

# 238. Generated Code

Generated code must originate from controlled generators.

---

# 239. Generator Version

Code-generator versions must be pinned or otherwise controlled.

---

# 240. Generated Code Review

Generated code may receive adjusted review treatment, but the generator and generation process must be trusted.

---

# 241. OpenAPI Generated Clients

OpenAPI client generation must use controlled generator versions.

---

# 242. Protobuf / Schema Generation

Schema-based generated artifacts must use versioned schemas and controlled generators.

---

# 243. Build-Time Network Access

Production builds should minimize arbitrary network access.

Where possible, dependencies should resolve through approved repositories only.

---

# 244. Hermetic Build Direction

The platform should progressively move toward more hermetic builds.

A hermetic build uses declared inputs rather than uncontrolled external state.

---

# 245. Time Dependency

Build behavior must not unexpectedly depend on current date/time.

---

# 246. External API During Build

Production builds must not depend on arbitrary live business APIs.

---

# 247. Testcontainers

Integration tests may use Testcontainers according to ADR-013.

Container images used by tests must also be controlled and versioned.

---

# 248. Testcontainer Image Version

Tests should avoid uncontrolled:

```text
image:latest
```

references.

---

# 249. Test Infrastructure Security

Test infrastructure must not receive unnecessary production credentials.

---

# 250. Promotion Evidence

A production deployment should preserve evidence that required gates passed.

---

# 251. Audit Trail

The release audit trail should answer:

- who approved
- what artifact
- which version
- which digest
- which source commit
- which environment
- when deployed
- which security gates passed

---

# 252. Deployment Actor

Automated deployment identity should be distinguishable from the human approver where applicable.

---

# 253. Separation of Duties

Critical production environments may require separation between:

```text
Code Author

Reviewer

Production Approver

Deployment Identity
```

according to enterprise governance.

---

# 254. Release Revocation

The organization must be able to mark an artifact as no longer approved for deployment.

---

# 255. Compromised Artifact

If an artifact is determined to be compromised:

```text
Block future deployment

↓

Identify running instances

↓

Contain

↓

Replace with trusted artifact

↓

Investigate provenance

↓

Rotate affected credentials if required
```

---

# 256. Compromised Build System

If CI/CD itself is suspected compromised, artifacts built during the affected window must not automatically remain trusted.

---

# 257. Build-System Incident

Response may require:

- suspending releases
- rotating CI credentials
- validating pipeline definitions
- rebuilding artifacts in trusted infrastructure
- comparing provenance
- redeploying trusted artifacts

---

# 258. Signing-Key Compromise

If an artifact-signing key is compromised:

- revoke the key
- rotate signing identity
- identify artifacts signed by the compromised key
- determine trust impact
- re-sign only after artifact verification where policy permits

---

# 259. SBOM Incident Use

SBOM inventory should accelerate response to ecosystem vulnerabilities.

Example:

```text
Critical vulnerability in library X

↓

Search SBOM inventory

↓

Identify affected services

↓

Prioritize remediation
```

---

# 260. Log4Shell-Class Event

The platform must be able to answer quickly:

```text
Do we use this component?

Which version?

Which services?

Which deployed environments?

Which artifact digest?
```

without manually opening every repository.

---

# 261. Supply-Chain Metrics

Recommended metrics include bounded counts such as:

- vulnerable artifacts
- critical CVEs
- unsigned production artifacts
- artifacts without SBOM
- failed security gates
- dependency age
- base-image age
- secret-scan findings

---

# 262. Metric Cardinality

Artifact digest and commit SHA should generally not become high-volume metric labels.

They may be available in deployment metadata and controlled logs.

---

# 263. Supply-Chain Dashboard

A platform dashboard should provide visibility into:

- security posture
- artifact compliance
- CVE exposure
- SBOM coverage
- signing coverage
- provenance coverage
- outdated dependencies

---

# 264. Alerting

Alerts should focus on actionable events such as:

- critical CVE affecting production
- failed signature verification
- unauthorized artifact publication
- secret detected in protected branch
- unsigned image attempting production deployment

---

# 265. Compliance Evidence

Automated pipeline evidence should be preferred over manually maintained compliance spreadsheets.

---

# 266. Policy Drift

Supply-chain policies must be applied consistently across services.

A service should not silently omit security gates because its pipeline was copied from an outdated template.

---

# 267. Pipeline Templates

Reusable enterprise pipeline templates are recommended.

---

# 268. Template Versioning

Pipeline templates must themselves be versioned.

---

# 269. Template Upgrade

Services should receive controlled upgrades when security pipeline templates evolve.

---

# 270. Platform Golden Path

The organization should provide a secure development golden path including:

```text
Java 21

Spring Boot

Gradle Wrapper

Sonar

SAST

Dependency Scan

Secret Scan

SBOM

Container Scan

Provenance

Artifact Signing

Kubernetes Deployment
```

---

# 271. Developer Experience

Supply-chain security should be automated enough that the secure path is also the easiest normal development path.

---

# 272. Local Verification

Developers should be able to execute major quality checks locally where practical.

Example:

```text
./gradlew clean build
```

The authoritative production gates remain in CI.

---

# 273. Security Gate Consistency

Local and CI configurations should avoid unnecessary differences.

---

# 274. Build Failure

A mandatory security gate failure must fail the release pipeline.

---

# 275. Warning-Only Mode

New security controls may initially run in warning mode during adoption, but critical mandatory policies must eventually become enforced gates.

---

# 276. Grandfathering

Existing services may require staged adoption.

Permanent exemption solely because a service is old is prohibited.

---

# 277. Legacy Service

Legacy services should receive a documented remediation plan toward the supply-chain baseline.

---

# 278. New Service

New services must adopt the baseline from project creation.

---

# 279. Service Template

New service templates should include supply-chain controls by default.

---

# 280. Anti-Patterns

The following are prohibited:

- production builds from developer machines
- direct uncontrolled pushes to protected branches
- bypassing mandatory review without governed emergency process
- dynamic dependency versions in production
- uncontrolled snapshot dependencies
- arbitrary dependency repositories
- committing secrets
- treating removal of a leaked secret as sufficient without rotation
- long-lived broad CI credentials where safer federation exists
- exposing CI secrets to untrusted pull requests
- overwriting released artifacts
- rebuilding separately for DEV, HML and PROD
- deploying only by mutable `latest` tag
- unpinned uncontrolled base images
- skipping container scanning
- treating SBOM as vulnerability scanning
- generating SBOM manually
- signing artifacts without verifying signatures
- storing signing keys in repositories
- modifying running containers manually
- patching production with `kubectl exec`
- editing already applied Flyway migrations
- downloading and executing arbitrary remote scripts
- uncontrolled CI/CD plugins
- bypassing security gates because a release is urgent
- suppressing all CVEs broadly
- permanent undocumented vulnerability exceptions
- deleting rollback artifacts too aggressively
- allowing pipeline definitions to change without review
- allowing arbitrary production image registries
- assuming a previously safe image remains safe forever
- treating commit SHA as complete binary provenance
- manually changing production configuration without audit
- using secrets in Docker image layers
- assuming test dependencies are irrelevant to security
- using unsupported base images or runtime versions
- promoting an artifact whose identity cannot be proven

---

# 281. Positive Consequences

The decision provides:

- stronger artifact integrity
- dependency traceability
- faster vulnerability response
- secret-leak prevention
- reproducible release inputs
- immutable production artifacts
- reliable rollback identity
- SBOM coverage
- provenance
- artifact-signing capability
- stronger container security
- controlled base images
- reduced dependency-confusion risk
- CI/CD least privilege
- improved auditability
- safer environment promotion
- better incident response
- stronger Kubernetes admission controls
- supply-chain policy automation

---

# 282. Negative Consequences

The decision introduces:

- additional CI/CD stages
- longer pipeline execution
- SBOM storage
- provenance infrastructure
- artifact-signing infrastructure
- vulnerability triage workload
- dependency-update workload
- stricter repository governance
- base-image maintenance
- registry storage requirements
- policy-management complexity
- potential deployment blocking from security gates

These costs are accepted because artifact trust is a prerequisite for production trust.

---

# 283. Neutral Consequences

The decision also means:

- dependency updates become continuous operational work
- pinned dependencies require active maintenance
- historical artifacts may become disallowed after new CVEs
- production rollback options depend on both compatibility and security
- security tooling may produce false positives
- some services may require staged compliance
- artifact identity becomes more important than environment-specific version naming
- CI/CD becomes part of the production security perimeter

---

# 284. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Malicious dependency enters build | Critical | Low | Approved repositories and dependency verification |
| Dependency confusion | Critical | Low | Controlled namespaces and repository precedence |
| Vulnerable dependency reaches production | Critical | Medium | Automated dependency scanning |
| Secret committed to repository | Critical | Medium | Secret scanning and credential rotation |
| CI credential compromised | Critical | Low | Least privilege and short-lived identity |
| Malicious PR steals pipeline secret | Critical | Low | Isolated untrusted PR workflows |
| Artifact modified after testing | Critical | Low | Immutable repository and digest |
| DEV/HML/PROD artifacts differ | High | Medium | Build-once promotion |
| Mutable image tag changes unexpectedly | Critical | Medium | Digest-based deployment |
| Vulnerable base image remains pinned | High | Medium | Continuous base-image monitoring |
| New CVE affects existing production image | Critical | Medium | Registry rescanning and SBOM inventory |
| Signing key compromised | Critical | Low | Protected/keyless signing and revocation |
| Signature not verified | High | Medium | Deployment/admission verification |
| Pipeline security gate removed | Critical | Low | Protected pipeline definitions |
| Build runner contaminated | Critical | Low | Isolation and ephemeral runners |
| Secret stored in image layer | Critical | Low | Build-secret mechanisms and scanning |
| Old rollback artifact has critical CVE | High | Medium | Security-aware rollback policy |
| Build cannot be reproduced | Medium | Medium | Controlled build inputs |
| Applied migration is edited | Critical | Low | Immutable Flyway migration policy |
| Unsupported runtime remains deployed | High | Medium | Lifecycle governance |
| SBOM does not match artifact | High | Low | Digest-bound automated generation |

---

# 285. Implementation Guidance

The following rules are mandatory:

1. Production source must originate from approved repositories.
2. Production branches must use branch protection.
3. Production changes must pass required review and quality gates.
4. Gradle projects must use the Gradle Wrapper.
5. Build-tool versions must be controlled.
6. Production builds must execute in controlled CI infrastructure.
7. Dynamic dependency versions are prohibited for production.
8. Uncontrolled snapshot dependencies are prohibited for production.
9. Dependencies must resolve from approved repositories.
10. Dependency verification/locking should be adopted where practical.
11. Third-party dependencies must undergo vulnerability scanning.
12. Vulnerability exceptions require documented risk acceptance.
13. Secret scanning is mandatory.
14. A committed real secret must be rotated.
15. CI/CD identities must follow least privilege.
16. Untrusted PRs must not receive privileged production secrets.
17. Released artifacts must be immutable.
18. Released versions must never be overwritten with different content.
19. Production container deployments must use immutable artifact identity.
20. `latest` alone is prohibited for production.
21. Production images must use approved base images.
22. Base-image versions must be controlled.
23. Production images must undergo container scanning.
24. Production applications should generate an SBOM automatically.
25. SBOMs must correspond to the exact artifact.
26. Build provenance should be generated by trusted CI/CD infrastructure.
27. Production artifacts should support cryptographic signing.
28. Signing credentials must be strongly protected.
29. Signature verification must be enforced where platform capabilities permit.
30. DEV, HML and PROD must use the same promoted application artifact.
31. Environment-specific behavior must use externalized configuration.
32. Production artifacts must not be manually modified.
33. Rollback must use previously published immutable artifacts.
34. Applied Flyway migrations must never be edited.
35. Database corrections require new migration versions.
36. Pipeline definitions must be version controlled and reviewed.
37. Security-gate removal requires explicit review.
38. Arbitrary remote script execution in builds is prohibited.
39. Runtime secrets must not be embedded in container images.
40. Container builds must exclude unnecessary sensitive files.
41. Production registries must use controlled authenticated publication.
42. Artifact metadata must preserve source-to-deployment traceability.
43. Post-release vulnerability monitoring must be supported.
44. New CVEs must be traceable to affected artifacts through SBOM inventory.
45. Emergency releases must preserve artifact identity and auditability.
46. Security exceptions must be scoped and time bounded.
47. Kubernetes admission policies should progressively verify artifact trust.
48. CI/CD third-party actions and plugins must be controlled.
49. Build/test dependencies are included in supply-chain governance.
50. New services must adopt the supply-chain baseline by default.

---

# 286. Validation

The decision will be validated through:

- protected-branch configuration
- pull-request checks
- Gradle Wrapper validation
- dependency locking/verification checks
- dependency vulnerability scanning
- secret scanning
- SAST
- Sonar quality gate
- unit tests
- integration tests
- container scanning
- base-image validation
- SBOM generation
- SBOM/artifact association validation
- provenance generation
- artifact-signature verification
- image-digest validation
- registry-policy validation
- IaC scanning
- Kubernetes admission controls
- environment-promotion verification
- rollback exercises
- credential-rotation exercises
- post-release CVE exercises
- production-readiness review

---

# 287. Success Criteria

The decision is successful when:

- every production artifact is traceable to source
- released artifacts cannot be silently overwritten
- the same artifact is promoted across environments
- production deployments identify exact image digests
- dependency versions are controlled
- critical dependency vulnerabilities are detected before release
- new post-release CVEs can be mapped rapidly to deployed services
- secrets are prevented from reaching repositories and artifacts
- CI/CD identities use least privilege
- SBOM coverage exists for production artifacts
- provenance identifies trusted build origin
- signed artifacts can be verified
- production images use controlled base images
- security gates cannot be bypassed silently
- rollback uses known immutable artifacts
- database migrations remain immutable
- supply-chain incidents can be investigated using durable evidence

---

# 288. Alternatives Rejected

## 288.1 Build Separately Per Environment

Rejected because DEV, HML and PROD could receive different binaries from the same source revision.

---

## 288.2 Mutable Container Tags Only

Rejected because a tag can point to different content over time.

---

## 288.3 Dependency Scanning Only

Rejected because vulnerability scanning does not provide artifact provenance, secret detection or build integrity.

---

## 288.4 SBOM Only

Rejected because knowing artifact composition does not prove that the artifact was built by trusted infrastructure.

---

## 288.5 Artifact Signing Only

Rejected because a correctly signed artifact may still contain vulnerable or malicious dependencies if earlier controls fail.

---

## 288.6 Manual Production Builds

Rejected because they lack consistent reproducibility, security gates and provenance.

---

## 288.7 Developer-Machine Production Artifacts

Rejected because local workstation state is not an acceptable production trust root.

---

## 288.8 Automatic Dependency Updates Directly to Production

Rejected because dependency changes require testing and compatibility validation.

---

## 288.9 Rebuild Old Commit for Rollback

Rejected as the primary rollback model because the rebuilt artifact may differ from the originally deployed artifact.

---

## 288.10 Editing Existing Flyway Migration

Rejected because it destroys database migration history and deployment reproducibility.

---

# 289. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-003: Use Java 21
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-007: Adopt the Transactional Outbox Pattern
- ADR-090: Enterprise Event-Driven Architecture, SQS, Transactional Outbox, Idempotency, Event Contract and Messaging Governance Standard
- ADR-013: Use Testcontainers for Integration Testing
- ADR-014: Adopt OpenTelemetry for Distributed Observability
- ADR-015: Deploy Workloads on Kubernetes
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-018: Version Integration Event Contracts
- ADR-019: Adopt Structured Logging
- ADR-020: Define Service-Level Objectives
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-022: Adopt API Contract Governance
- ADR-023: Adopt API Security Standards
- ADR-025: Adopt Kubernetes Runtime Security Standards

---

# 290. References

- SLSA — Supply-chain Levels for Software Artifacts
- SPDX
- CycloneDX
- OWASP Software Component Verification Standard
- OWASP Dependency-Check
- OWASP CI/CD Security Guidance
- OpenSSF
- NIST Secure Software Development Framework
- NIST Software Supply Chain Security Guidance
- Gradle Dependency Verification
- Gradle Dependency Locking
- OCI Image Specification
- Kubernetes Security Documentation
- Sigstore
- Cosign
- Software Bill of Materials
- Common Vulnerabilities and Exposures
- Common Vulnerability Scoring System
- ADR-006: Use Flyway for Database Schema Evolution
- ADR-013: Use Testcontainers for Integration Testing
- ADR-021: Adopt Zero-Downtime Deployment Practices
- ADR-023: Adopt API Security Standards

---

# 291. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial software supply-chain security baseline |

---

# 292. Decision Summary

The Enterprise Order Platform adopts a secure software supply-chain lifecycle:

```text
SOURCE

↓

REVIEW

↓

BUILD

↓

TEST

↓

SCAN

↓

GENERATE SBOM

↓

GENERATE PROVENANCE

↓

SIGN

↓

PUBLISH

↓

PROMOTE

↓

VERIFY

↓

DEPLOY

↓

MONITOR
```

The primary release principle is:

```text
BUILD ONCE

↓

VERIFY ONCE

↓

PUBLISH IMMUTABLY

↓

DEV

↓

HML

↓

PROD
```

The application must **not** be rebuilt for each environment.

Production trust is based on the combination of:

```text
Protected Source

+

Controlled Dependencies

+

Trusted Build Infrastructure

+

Automated Security Gates

+

SBOM

+

Provenance

+

Artifact Signature

+

Immutable Digest

+

Controlled Promotion
```

For every production workload, the organization should ultimately be able to establish:

```text
Running Pod

↓

Image Digest

↓

Signed Artifact

↓

SBOM

↓

Build Provenance

↓

CI/CD Execution

↓

Commit SHA

↓

Reviewed Pull Request
```

And when a new vulnerability is disclosed:

```text
CVE

↓

Affected Component

↓

SBOM Search

↓

Affected Artifact

↓

Affected Service

↓

Affected Environment

↓

Remediation Release
```

Supply-chain security therefore establishes that production software is not trusted merely because:

```text
it came from our repository
```

or because:

```text
the build passed.
```

Production software is trusted when its:

```text
Source

Build

Dependencies

Composition

Provenance

Artifact Identity

Promotion History
```

can be independently established and verified.

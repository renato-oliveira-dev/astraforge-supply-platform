# ADR-038: Adopt Dependency and Software Supply Chain Security Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-038 |
| Title | Adopt Dependency and Software Supply Chain Security Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Dependencies, Gradle, SBOM, CVE, Containers, CI/CD, Supply Chain |
| Related Work Items | Dependency Management, Vulnerability Scanning, SBOM, Containers, Build Security |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Modern Java applications are composed primarily from third-party software.

A production service may depend on:

```text
Application Code
      |
      +--> Spring Boot
      +--> Spring Framework
      +--> Jackson
      +--> Hibernate
      +--> PostgreSQL Driver
      +--> AWS SDK
      +--> Kafka
      +--> Resilience4j
      +--> Netty
      +--> Logging Libraries
      +--> Test Libraries
      |
      v
Hundreds of Transitive Dependencies
```

The effective attack surface therefore includes considerably more than source code maintained by the application team.

A vulnerability may enter through:

```text
Direct Dependency

Transitive Dependency

Gradle Plugin

Build Tool

Container Base Image

CI/CD Action

Artifact Repository

Generated Artifact

Compromised Package
```

Software supply-chain security must therefore be treated as an architectural concern.

---

# 2. Problem Statement

The platform requires standards defining:

- Gradle dependency management
- dependency versions
- dependency locking
- transitive dependencies
- repositories
- plugins
- CVE management
- vulnerability scanning
- SBOM
- container images
- base images
- image scanning
- secrets scanning
- build provenance
- artifact integrity
- dependency upgrades
- unsupported libraries
- dependency minimization
- CI/CD security
- release artifacts
- emergency vulnerability remediation

---

# 3. Decision Drivers

Primary drivers are:

1. reproducible builds
2. dependency integrity
3. vulnerability management
4. artifact traceability
5. supply-chain security
6. operational stability
7. deterministic releases
8. maintainability
9. auditability
10. rapid security remediation
11. controlled dependency evolution
12. minimized attack surface

---

# 4. Decision

The Enterprise Order Platform adopts controlled and reproducible dependency management across:

```text
SOURCE
  |
  v
DEPENDENCY RESOLUTION
  |
  v
BUILD
  |
  v
TEST
  |
  v
SECURITY SCAN
  |
  v
SBOM
  |
  v
ARTIFACT
  |
  v
CONTAINER IMAGE
  |
  v
REGISTRY
  |
  v
DEPLOYMENT
```

Every stage must preserve sufficient information to answer:

```text
What source produced this artifact?

Which dependencies were included?

Which versions were resolved?

Which image was deployed?

Which vulnerabilities affect it?
```

---

# 5. Fundamental Principle

The primary rule is:

```text
A dependency is production code
that someone else maintains.
```

Therefore dependencies require the same engineering discipline as internally maintained code.

---

# 6. Dependency Minimization

Every dependency increases:

- attack surface
- upgrade effort
- compatibility risk
- build complexity
- transitive dependency count
- CVE exposure

---

# 7. Dependency Justification

A new production dependency should provide sufficient value to justify its lifecycle cost.

---

# 8. Avoid Utility Dependencies for Trivial Problems

Do not add a large dependency solely to implement trivial functionality already safely available in:

- Java
- Spring
- existing approved libraries

---

# 9. Unused Dependencies

Unused dependencies must be removed.

---

# 10. Dependency Scope

Dependencies must use the narrowest appropriate Gradle configuration.

Examples:

```text
implementation

runtimeOnly

compileOnly

testImplementation

testRuntimeOnly
```

---

# 11. Test Dependency

Test-only libraries must not unnecessarily enter production runtime artifacts.

---

# 12. Runtime Dependency

Dependencies needed only at runtime should not unnecessarily expand compile-time APIs.

---

# 13. API Dependency

Gradle `api` exposure should be used only when consumers genuinely require the dependency as part of the module's public API.

---

# 14. Version Ownership

Dependency versions require explicit ownership.

---

# 15. Spring Boot Dependency Management

Where Spring Boot manages a dependency version, its tested dependency-management baseline should normally be preferred.

---

# 16. Arbitrary Override

Do not override Spring Boot-managed versions casually.

---

# 17. Version Override

An override requires a reason such as:

- security remediation
- compatibility requirement
- confirmed defect
- approved feature requirement

---

# 18. Compatibility Verification

Overriding a managed dependency requires regression testing against the framework version in use.

---

# 19. BOM

Approved BOMs should be used where a dependency family provides coordinated compatible versions.

---

# 20. Multiple BOMs

Multiple BOMs must not silently create conflicting dependency ownership.

---

# 21. Direct Version

Explicit versions may be necessary when no approved platform/BOM owns the dependency.

---

# 22. Dynamic Versions

Dynamic versions are prohibited for production dependencies.

Avoid:

```text
1.+

latest.release

latest.integration
```

---

# 23. Why Dynamic Versions Are Unsafe

The same commit could produce:

```text
Build Monday -> Library 1.4.2

Build Friday -> Library 1.4.7
```

without source changes.

This breaks reproducibility.

---

# 24. Snapshot Dependencies

Snapshot dependencies are prohibited in production releases unless explicitly governed for a temporary exceptional case.

---

# 25. Release Build

A release must resolve stable dependency versions.

---

# 26. Dependency Locking

Dependency locking should be adopted for production dependency graphs where supported by the build strategy.

---

# 27. Purpose of Locking

Locking provides:

```text
Declared Dependency
        |
        v
Dependency Resolution
        |
        v
Exact Resolved Graph
        |
        v
LOCK FILE
```

---

# 28. Reproducibility

Given the same:

- source
- Gradle version
- JDK
- lock state
- repositories

dependency resolution should remain deterministic.

---

# 29. Lock File

Dependency lock files are source-controlled artifacts when dependency locking is adopted.

---

# 30. Lock Update

Lock changes require normal code review.

---

# 31. Unexpected Lock Changes

Unexpected large lock-file changes require investigation.

---

# 32. Transitive Dependencies

Transitive dependencies are part of the application's effective dependency graph.

---

# 33. Direct Declaration

A transitive dependency that application source directly relies upon should generally be declared explicitly.

---

# 34. Why

Depending accidentally on another library to provide a transitive dependency creates fragile coupling.

---

# 35. Dependency Insight

Gradle dependency-insight tooling should be used to diagnose:

- version conflicts
- unexpected transitive dependencies
- dependency origin
- forced versions

---

# 36. Conflict Resolution

Version conflicts must be understood rather than solved blindly with:

```text
force
```

---

# 37. Forced Version

Forced versions require documented technical justification.

---

# 38. Exclusions

Dependency exclusions must be narrow and justified.

---

# 39. Blanket Exclusion

Broad exclusions can create runtime incompatibilities and are discouraged.

---

# 40. Duplicate Libraries

Multiple implementations of the same concern should be avoided unless required.

Examples:

```text
multiple JSON stacks

multiple logging implementations

multiple HTTP stacks
```

without architectural justification.

---

# 41. Logging Dependencies

Logging bridges and implementations must be controlled to prevent:

- duplicate bindings
- recursive bridges
- unexpected runtime logging behavior

---

# 42. Repository Governance

Dependencies must resolve only from approved artifact repositories.

---

# 43. Approved Repository

Typical model:

```text
Gradle
  |
  v
Corporate Artifact Repository
  |
  +--> Approved Internal Artifacts
  |
  +--> Controlled Proxy of External Repositories
```

---

# 44. Random Repository

Adding arbitrary repositories directly to application builds is prohibited.

---

# 45. Example

Avoid introducing:

```gradle
maven {
    url = uri("https://random-example-repository.invalid")
}
```

merely because a dependency is unavailable elsewhere.

---

# 46. Repository Order

Repository ordering should be deterministic and centrally governed where practical.

---

# 47. Repository Credentials

Artifact-repository credentials must come from approved secret-management mechanisms.

---

# 48. Credentials in Build Script

Repository credentials must not be hard-coded in:

```text
build.gradle

settings.gradle

gradle.properties committed to Git
```

---

# 49. Plugin Repositories

Gradle plugin repositories require the same governance as library repositories.

---

# 50. Gradle Plugins

Build plugins execute code during the build.

They are therefore part of the supply chain.

---

# 51. Plugin Minimization

Only necessary plugins should be applied.

---

# 52. Plugin Version

Plugin versions must be controlled.

---

# 53. Dynamic Plugin Version

Dynamic Gradle plugin versions are prohibited.

---

# 54. Gradle Wrapper

Projects must use the Gradle Wrapper.

---

# 55. Wrapper Version

The Gradle Wrapper version must be explicitly controlled and reviewed.

---

# 56. Wrapper Upgrade

Wrapper upgrades require build/test validation.

---

# 57. Wrapper Integrity

Wrapper configuration and associated integrity controls must not be bypassed casually.

---

# 58. JDK

The build JDK version must be controlled.

For Java 21 services:

```text
Java 21
```

is the production baseline unless an ADR explicitly changes it.

---

# 59. Java Toolchain

Gradle Java Toolchains should be used where appropriate to make the required Java version explicit.

---

# 60. Developer JDK

The build should not silently depend on whichever JDK happens to be installed on a developer workstation.

---

# 61. Build Reproducibility

The target is:

```text
Same Source

+

Same Controlled Inputs

=

Equivalent Artifact
```

---

# 62. Environment Leakage

Build output must not unintentionally depend on:

- developer username
- local absolute paths
- local timezone
- uncontrolled timestamps
- machine-specific configuration

where avoidable.

---

# 63. Generated Files

Generated files included in artifacts must be deterministic where practical.

---

# 64. Dependency Vulnerability Scanning

Production dependencies must be scanned for known vulnerabilities.

---

# 65. Scanner

The platform may use approved tooling such as:

```text
OWASP Dependency-Check

SCA Platform

Container Registry Scanner

Enterprise Security Scanner
```

according to corporate tooling standards.

---

# 66. Tool Independence

This ADR defines the control, not one permanent vendor.

---

# 67. CVE

A reported CVE requires assessment.

---

# 68. CVE Does Not Equal Exploitability

A dependency CVE does not automatically mean:

```text
Application exploitable
```

because exploitability depends on:

- affected version
- vulnerable functionality
- application usage
- exposure
- configuration
- attack path

---

# 69. Scanner Green Does Not Equal Secure

Likewise:

```text
No Known CVEs
```

does not prove that a dependency or application is secure.

---

# 70. Finding Model

```text
CVE FINDING
     |
     v
AFFECTED VERSION?
     |
     +-- NO --> Document / Close
     |
     v YES
VULNERABLE CODE USED?
     |
     +-- NO --> Assess Exposure
     |
     v YES
REACHABLE / EXPLOITABLE?
     |
     +-- NO --> Risk Assessment
     |
     v YES
REMEDIATE
```

---

# 71. Severity

Severity should consider:

- CVSS
- exploit availability
- internet/internal exposure
- data sensitivity
- reachable code
- compensating controls

---

# 72. Critical Vulnerability

A confirmed exploitable critical vulnerability requires urgent remediation.

---

# 73. High Vulnerability

High-severity findings require prioritized remediation according to security SLA.

---

# 74. False Positive

False positives require documented assessment rather than arbitrary scanner suppression.

---

# 75. Suppression

Vulnerability suppressions must be:

- specific
- justified
- reviewable
- time-bounded where appropriate

---

# 76. Permanent Blanket Suppression

Broad permanent CVE suppression is prohibited.

---

# 77. Upgrade Preferred

Where practical, upgrading to a fixed supported dependency is preferred over indefinite suppression.

---

# 78. Dependency Upgrade Strategy

Dependency upgrades should be incremental and controlled.

---

# 79. Upgrade Flow

```text
Upgrade Candidate
      |
      v
Release Notes
      |
      v
Compatibility Review
      |
      v
Build
      |
      v
Unit Tests
      |
      v
Integration Tests
      |
      v
SAST / SCA
      |
      v
Deploy
```

---

# 80. Major Upgrade

Major framework/library upgrades require broader compatibility assessment.

---

# 81. Security Patch

Security patches may require accelerated processing but must still receive adequate automated verification.

---

# 82. Do Not Downgrade Blindly

Downgrading to remove one finding may reintroduce older vulnerabilities or incompatibilities.

---

# 83. Dependency Freshness

Dependencies should not remain indefinitely obsolete without reason.

---

# 84. Latest Is Not Always Safest

The platform does not require every dependency to use the newest version immediately.

---

# 85. Stability vs Freshness

The desired state is:

```text
Supported

Secure

Compatible

Maintained
```

not simply:

```text
Newest
```

---

# 86. End-of-Life Dependencies

Unsupported/EOL dependencies require remediation planning.

---

# 87. Framework Support

Framework versions outside supported security-maintenance windows should not remain a permanent production baseline.

---

# 88. SBOM

Production releases must support generation of a Software Bill of Materials.

---

# 89. SBOM Purpose

An SBOM answers:

```text
What software components
exist in this release?
```

---

# 90. SBOM Content

The SBOM should include, where supported:

- component
- version
- package identifier
- dependency relationships
- relevant hashes/licenses

---

# 91. SBOM Format

Use an approved standard such as:

```text
CycloneDX
```

or:

```text
SPDX
```

according to platform tooling.

---

# 92. SBOM Per Release

The SBOM must correspond to the actual released artifact.

---

# 93. Stale SBOM

Generating an SBOM once for the repository is insufficient.

---

# 94. Artifact Relationship

Conceptually:

```text
RELEASE
   |
   +--> application artifact
   |
   +--> container image
   |
   +--> SBOM
   |
   +--> provenance metadata
```

---

# 95. Vulnerability Response Using SBOM

When a new vulnerability appears:

```text
NEW CVE
   |
   v
QUERY SBOM INVENTORY
   |
   v
WHICH RELEASES CONTAIN IT?
   |
   v
PRIORITIZE REMEDIATION
```

---

# 96. License Awareness

Dependency governance should identify unacceptable licensing risks according to corporate legal policy.

---

# 97. License Is Separate From CVE

A dependency may be secure but legally unsuitable.

---

# 98. Container Security

Container images are part of the software supply chain.

---

# 99. Base Image

Production containers must use approved base images.

---

# 100. Base Image Minimization

Prefer minimal runtime images containing only required components.

---

# 101. Build Image vs Runtime Image

Multi-stage builds should separate build tooling from the production runtime where appropriate.

---

# 102. JDK vs JRE Runtime

If compilation tools are unnecessary at runtime, they should not be included merely for convenience.

---

# 103. Image Tag

Mutable tags such as:

```text
latest
```

must not be the sole production artifact identity.

---

# 104. Immutable Identity

Production deployments should resolve an immutable image identity, such as a digest or controlled immutable release tag.

---

# 105. Base Image Pinning

Base-image selection should be reproducible according to the container platform's approved strategy.

---

# 106. Base Image Updates

Base images require regular security maintenance.

---

# 107. OS Packages

Unused OS packages should not be installed in application containers.

---

# 108. Package Manager Cache

Build artifacts/package-manager caches should not unnecessarily remain in runtime images.

---

# 109. Root User

Containers should run as a non-root user where the application does not require root privileges.

---

# 110. Privileged Container

Privileged execution is prohibited unless explicitly justified and approved.

---

# 111. Writable Filesystem

Runtime write access should be minimized to required paths.

---

# 112. Shell

A production image does not require a shell merely for hypothetical debugging if the approved runtime strategy can avoid it.

---

# 113. Container Scan

Production container images must be vulnerability scanned.

---

# 114. Image Scan Scope

Image scanning should include:

- OS packages
- application libraries where supported
- known vulnerable components

---

# 115. Build-Time Scan

Scanning only after production deployment is insufficient.

---

# 116. Registry

Production images must be stored in approved registries.

---

# 117. Public Registry Deployment

Production workloads should not directly depend on arbitrary public image tags at deployment time.

---

# 118. Image Promotion

Prefer:

```text
Build Once
   |
   v
Scan
   |
   v
Promote Same Artifact
   |
   +--> DEV
   +--> QA
   +--> PROD
```

over:

```text
Rebuild for DEV

Rebuild for QA

Rebuild for PROD
```

---

# 119. Why Build Once

Rebuilding can change:

- dependencies
- timestamps
- base image
- resolved packages

and weakens confidence that production runs the tested artifact.

---

# 120. Artifact Immutability

A released artifact must not be modified in place.

---

# 121. New Fix

A changed artifact requires a new version/build identity.

---

# 122. Artifact Repository

Published application artifacts must use controlled artifact repositories.

---

# 123. Overwrite Release

Overwriting an existing released version is prohibited.

---

# 124. Provenance

Build provenance should identify:

- source revision
- build workflow
- artifact identity
- build environment where supported

---

# 125. Goal

The organization should be able to establish:

```text
Git Commit
    |
    v
CI Build
    |
    v
Artifact
    |
    v
Container Digest
    |
    v
Deployment
```

---

# 126. Build Identity

Each release should have a unique traceable build identity.

---

# 127. Git Commit

The source revision associated with a release must be recoverable.

---

# 128. Dirty Build

Official releases should not be built from uncommitted developer workspace modifications.

---

# 129. CI Build

Production artifacts should be produced by controlled CI/CD infrastructure rather than manually on developer machines.

---

# 130. CI/CD Is Production Infrastructure

The CI/CD system can produce deployable software and must therefore be treated as security-sensitive infrastructure.

---

# 131. Pipeline Permissions

Pipeline identities must follow least privilege.

---

# 132. Deployment Credential

Build jobs that do not deploy should not automatically receive production deployment credentials.

---

# 133. Secret Scope

Secrets should be scoped to the smallest required:

- repository
- environment
- job
- operation

---

# 134. Fork/Pull Request Security

Untrusted code must not automatically receive privileged CI secrets.

---

# 135. Pipeline Code

Pipeline definitions require code review.

---

# 136. External CI Actions

Third-party CI/CD actions/plugins are executable supply-chain dependencies.

---

# 137. Action Pinning

External CI actions should be pinned according to approved platform policy rather than floating unpredictably.

---

# 138. Build Scripts

Gradle scripts and buildSrc/convention plugins are executable code and require review.

---

# 139. Init Scripts

Uncontrolled Gradle init scripts must not silently alter official release builds.

---

# 140. Secrets Scanning

Repositories must support automated detection of accidentally committed secrets.

---

# 141. Secret Examples

Scanning should consider:

- passwords
- API keys
- private keys
- cloud credentials
- OAuth client secrets
- tokens

---

# 142. Scanner Detection

Secret detection does not make the exposed credential safe.

---

# 143. Compromised Secret

If a real secret is committed:

```text
Detect
  |
  v
Revoke / Rotate
  |
  v
Remove From Current Source
  |
  v
Assess History / Exposure
```

---

# 144. Delete Is Not Rotation

Deleting a credential from Git does not invalidate copies already exposed.

---

# 145. `.gitignore`

`.gitignore` prevents accidental future tracking.

It is not a security boundary for already committed secrets.

---

# 146. Local Secret Files

Local secret/configuration files should be excluded from version control.

---

# 147. Example Configuration

Repositories may contain safe templates such as:

```text
application-local-example.yml
```

with fake values.

---

# 148. Production Secret Example

Real production secrets must never be placed in examples/documentation.

---

# 149. Dependency Confusion

Dependency resolution must defend against dependency-confusion scenarios.

---

# 150. Internal Package Names

Internal coordinates must be governed so public repositories cannot unexpectedly satisfy internal dependencies.

---

# 151. Repository Precedence

Internal artifact resolution strategy must prevent unintended substitution by external packages.

---

# 152. Namespace Ownership

Internal Maven group IDs require organizational ownership.

---

# 153. Typosquatting

New external dependencies require review of:

- exact coordinates
- publisher
- project legitimacy

---

# 154. Similar Package Name

A package with a familiar-looking name is not sufficient evidence of legitimacy.

---

# 155. Abandoned Library

Libraries with no credible maintenance should be avoided for critical new functionality.

---

# 156. Dependency Evaluation

For important new dependencies, evaluate:

```text
Maintenance

Security history

Release cadence

Community/vendor support

License

Compatibility

Transitive graph
```

---

# 157. Package Integrity

Artifact checksum/signature verification should follow repository/platform capabilities.

---

# 158. Checksum Failure

Integrity-check failures must fail the build rather than be silently ignored.

---

# 159. Build Cache

Remote build caches must be trusted and access-controlled.

---

# 160. Cache Poisoning

Untrusted actors must not be able to inject artifacts into privileged build caches.

---

# 161. Generated Code

Code generators are supply-chain dependencies.

---

# 162. Generator Version

Generator versions must be controlled.

---

# 163. Generated Output Review

Security-sensitive generated output should not be assumed safe solely because it was generated.

---

# 164. Annotation Processors

Annotation processors execute during compilation and require dependency governance.

---

# 165. Test Dependencies

Test libraries can execute arbitrary code during builds and remain supply-chain relevant even if absent from runtime.

---

# 166. Development Tooling

IDE plugins are outside the deployable artifact but may still present developer-workstation supply-chain risk and should follow corporate workstation policy.

---

# 167. Emergency CVE Response

The platform requires a rapid vulnerability-response path.

---

# 168. Emergency Flow

```text
Critical CVE Announced
        |
        v
Identify Affected Component
        |
        v
Search SBOM / Dependency Graph
        |
        v
Determine Reachability
        |
        +---- Not Affected --> Document
        |
        v
Affected
        |
        v
Patch / Upgrade / Mitigate
        |
        v
Automated Regression
        |
        v
Security Scan
        |
        v
Deploy
```

---

# 169. Emergency Does Not Mean Untested

Urgency may shorten approval paths but should not eliminate critical automated tests.

---

# 170. Compensating Control

When immediate upgrade is impossible, temporary compensating controls may be used.

---

# 171. Compensating Control Examples

Depending on the vulnerability:

- disable affected feature
- block vulnerable endpoint
- restrict network access
- modify configuration

---

# 172. Temporary Means Temporary

Compensating controls require a tracked remediation plan.

---

# 173. Vulnerability Inventory

The organization should be able to answer:

```text
Which services use dependency X?

Which version?

Which deployed releases?

Which environments?
```

---

# 174. Dependency Automation

Automated dependency-update tooling may propose updates.

---

# 175. Automated PR

Automated upgrade pull requests are acceptable.

---

# 176. Automatic Production Merge

Dependency updates must not automatically reach production solely because a bot created them.

---

# 177. Review

Automated dependency updates remain subject to:

- tests
- compatibility review
- security gates
- code review

---

# 178. Grouped Updates

Related dependency-family upgrades may be grouped when compatibility requires coordinated versions.

---

# 179. Giant Upgrade PR

Combining unrelated major upgrades into one huge change should be avoided.

---

# 180. Spring Boot Upgrade

A Spring Boot upgrade may affect:

- Spring Framework
- Jackson
- Hibernate
- Security
- logging
- embedded server
- dependency BOM

and therefore requires broad regression testing.

---

# 181. Java Upgrade

JDK upgrades require:

- compile validation
- tests
- runtime validation
- container validation
- performance review where material

---

# 182. Database Driver

Database-driver upgrades require integration testing against the actual database technology.

---

# 183. AWS SDK

AWS SDK upgrades require integration/contract validation for affected AWS services.

---

# 184. Security Library

Authentication/authorization library upgrades require explicit security regression testing.

---

# 185. Serialization Library

Jackson or serialization-library upgrades require API/event compatibility validation.

---

# 186. Kafka Library

Kafka client upgrades require producer/consumer compatibility validation.

---

# 187. Netty / HTTP Client

HTTP stack upgrades require:

- timeout validation
- TLS validation
- connection-pool validation
- integration tests

---

# 188. Dependency Policy Exceptions

Exceptions to this ADR require explicit documented approval.

---

# 189. Exception Contents

An exception should state:

- dependency/control
- reason
- risk
- compensating control
- owner
- expiration/review date

---

# 190. Permanent Exception

Permanent exceptions should be rare and periodically reviewed.

---

# 191. Quality Gate

Supply-chain controls are part of CI/CD.

---

# 192. Recommended Pipeline

```text
SOURCE
   |
   v
SECRET SCAN
   |
   v
DEPENDENCY RESOLUTION
   |
   v
BUILD
   |
   v
UNIT / INTEGRATION TESTS
   |
   v
SAST
   |
   v
SCA / DEPENDENCY SCAN
   |
   v
SBOM
   |
   v
PACKAGE
   |
   v
CONTAINER BUILD
   |
   v
CONTAINER SCAN
   |
   v
SIGN / PROVENANCE
   |
   v
PUBLISH
   |
   v
DEPLOY
```

Exact ordering may be optimized without removing mandatory controls.

---

# 193. Gate Failure

Confirmed security findings above the approved risk threshold must prevent normal promotion.

---

# 194. Override

Security-gate overrides require:

- authorization
- justification
- traceability
- remediation ownership

---

# 195. No Local Bypass

Developers must not disable dependency/security scans locally in committed build configuration merely to make CI green.

---

# 196. SBOM Failure

Required SBOM generation failure must be visible and treated as a release-control failure.

---

# 197. Artifact Scan

The artifact that is promoted should be the artifact that was scanned.

---

# 198. Rebuild After Scan

If an artifact is rebuilt after security scanning, the new artifact requires equivalent verification.

---

# 199. Production Readiness

A service is not production ready solely because:

```text
./gradlew build
```

passes.

---

# 200. Supply-Chain Production Readiness Gate

A release is not production ready until:

```text
[ ] Gradle Wrapper version controlled

[ ] Java toolchain controlled

[ ] No dynamic production dependency versions

[ ] No unauthorized snapshot dependencies

[ ] Dependency graph reviewed for unexpected changes

[ ] Dependency locking validated where adopted

[ ] Approved repositories only

[ ] Plugin versions controlled

[ ] Repository credentials externalized

[ ] No unnecessary production dependencies

[ ] Dependency vulnerability scan passes

[ ] Critical/high findings assessed

[ ] Suppressions reviewed

[ ] Unsupported/EOL dependencies reviewed

[ ] SBOM generated

[ ] SBOM corresponds to release artifact

[ ] Container base image approved

[ ] Container image scanned

[ ] Container does not unnecessarily run as root

[ ] Image identity immutable

[ ] Secrets scan passes

[ ] No real credentials committed

[ ] Build provenance available where supported

[ ] Git revision traceable to artifact

[ ] Release artifact immutable

[ ] CI/CD permissions reviewed

[ ] Production artifact built by controlled CI

[ ] Security gate passes

[ ] Exception approvals documented where applicable
```

---

# 201. Testing

Dependency changes require testing proportional to their impact.

---

# 202. Small Utility Upgrade

A small compatible utility upgrade may require:

```text
Build

Unit Tests

SCA
```

plus normal quality gates.

---

# 203. Framework Upgrade

A framework upgrade requires broader:

```text
Unit

Integration

Contract

Security

Performance
```

validation according to affected behavior.

---

# 204. Dependency Removal

Removing a dependency also requires build/runtime verification.

---

# 205. Transitive Change

A direct dependency version change can modify many transitives.

Review the resolved graph, not only the line changed in `build.gradle`.

---

# 206. Lock Diff Review

Dependency-lock diffs should be part of dependency-change review.

---

# 207. Container Regression

Base-image changes require application startup/runtime verification.

---

# 208. Native Dependency

Dependencies relying on native libraries require architecture/runtime compatibility checks.

---

# 209. Architecture Compatibility

Container images must support the deployment architecture:

```text
amd64

arm64
```

according to platform requirements.

---

# 210. SBOM Verification

CI should verify SBOM generation against the final artifact/image where tooling supports it.

---

# 211. Metrics

Supply-chain governance should track useful signals.

---

# 212. Suggested Metrics

Examples:

```text
Critical Vulnerabilities

High Vulnerabilities

Mean Time to Remediate

Dependencies Past EOL

Services With SBOM

Images Scanned

Secret Findings

Security Gate Overrides
```

---

# 213. Avoid Vanity Metrics

The number of dependencies alone is not a sufficient quality metric.

---

# 214. Risk-Based Interpretation

Metrics must support decisions rather than encourage artificial optimization.

---

# 215. Auditability

Security decisions should be recoverable from:

- pull requests
- scan results
- SBOM
- pipeline logs
- exception records
- release metadata

---

# 216. Retention

Supply-chain evidence should be retained according to corporate audit/security policy.

---

# 217. Anti-Patterns

The following are prohibited or strongly discouraged:

- dynamic production dependency versions
- uncontrolled snapshots
- arbitrary Maven repositories
- hard-coded repository credentials
- uncontrolled Gradle plugins
- floating CI actions
- unnecessary dependencies
- accidental reliance on transitive libraries
- blind dependency forcing
- broad dependency exclusions
- ignoring lock-file changes
- ignoring CVEs because the build succeeds
- treating every CVE as automatically exploitable
- blanket CVE suppressions
- indefinite security suppressions
- unsupported/EOL frameworks without remediation plan
- SBOM unrelated to the actual release
- mutable `latest` as production identity
- rebuilding different artifacts for each environment
- overwriting released artifacts
- arbitrary public production images
- root containers without need
- unnecessary OS packages
- secrets committed to Git
- treating deletion of a secret as rotation
- privileged secrets exposed to untrusted PR builds
- manual developer-machine production builds
- deploying an artifact different from the scanned artifact
- disabling security scans merely to pass CI
- automatically merging dependency upgrades without validation
- giant unrelated dependency-upgrade PRs

---

# 218. Positive Consequences

The decision provides:

- reproducible dependency resolution
- stronger supply-chain security
- improved CVE response
- release traceability
- better SBOM coverage
- safer container images
- reduced secret exposure
- controlled dependency upgrades
- improved artifact integrity
- reduced dependency confusion risk
- improved auditability
- clearer emergency remediation

---

# 219. Negative Consequences

The decision introduces:

- dependency governance overhead
- additional CI scanning
- SBOM generation
- container scanning
- lock-file maintenance
- vulnerability triage
- upgrade effort
- security exception governance

These costs are accepted because third-party software represents a substantial portion of the deployed attack surface.

---

# 220. Neutral Consequences

The decision also means:

- newest is not automatically best
- a CVE does not automatically prove exploitability
- no-CVE status does not prove security
- transitive dependencies remain security-relevant
- test dependencies remain supply-chain relevant
- container base images require lifecycle management
- security patches still require regression testing

---

# 221. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Vulnerable dependency | Critical | High | SCA + remediation |
| Dependency confusion | Critical | Low | Repository governance |
| Compromised plugin | Critical | Low | Controlled plugin versions |
| Non-reproducible build | High | Medium | Version control + locking |
| Secret committed | Critical | Medium | Secret scanning + rotation |
| Vulnerable base image | Critical | Medium | Image scanning |
| Artifact substitution | Critical | Low | Immutable artifacts/provenance |
| EOL framework | High | Medium | Lifecycle management |
| False-positive CVE | Medium | High | Reachability assessment |
| Broken security upgrade | High | Medium | Regression testing |
| CI credential compromise | Critical | Low | Least privilege |
| SBOM drift | High | Medium | Generate per release |

---

# 222. Implementation Guidance

The following rules are mandatory:

1. Dependencies are treated as part of production code.
2. New dependencies require technical justification.
3. Unused dependencies must be removed.
4. The narrowest appropriate Gradle dependency scope must be used.
5. Spring Boot-managed versions should normally remain under Boot dependency management.
6. Managed-version overrides require justification and regression testing.
7. Dynamic production dependency versions are prohibited.
8. Production snapshot dependencies are prohibited except approved temporary exceptions.
9. Dependency locking should be used where aligned with platform build strategy.
10. Lock-file changes require review.
11. Application source must not rely accidentally on undeclared transitive dependencies.
12. Version conflicts must be understood before forcing versions.
13. Only approved artifact repositories may be used.
14. Repository credentials must remain outside source control.
15. Gradle plugins are supply-chain dependencies and require controlled versions.
16. Projects must use the Gradle Wrapper.
17. Java/JDK version must be controlled, preferably through toolchains.
18. Production dependencies require vulnerability scanning.
19. CVEs require risk/reachability assessment.
20. Critical exploitable vulnerabilities require urgent remediation.
21. Security suppressions must be narrow and justified.
22. Unsupported dependencies require remediation planning.
23. Production releases require an SBOM according to platform capability.
24. SBOMs must correspond to actual released artifacts.
25. Production containers must use approved base images.
26. Runtime images should be minimal.
27. Containers should run non-root where possible.
28. Production images require vulnerability scanning.
29. Production image identity must be immutable.
30. The same built artifact should be promoted across environments.
31. Released artifacts must never be overwritten.
32. Production releases must be traceable to source revision.
33. Production artifacts should be built by controlled CI/CD.
34. CI/CD identities must follow least privilege.
35. Untrusted pull requests must not receive privileged secrets.
36. Repository secret scanning is mandatory.
37. Exposed real secrets require revocation/rotation.
38. Internal package namespaces must be protected against dependency confusion.
39. Important new dependencies require provenance/maintenance/license review.
40. Dependency upgrades require impact-proportional regression testing.
41. Security-gate failures must prevent normal promotion unless formally overridden.
42. The scanned artifact must be the promoted artifact.

---

# 223. Validation

This ADR will be validated through:

- Gradle dependency reports
- dependency locking
- SCA scanning
- CVE analysis
- SBOM generation
- container scanning
- secret scanning
- artifact repository controls
- CI/CD reviews
- dependency-update reviews
- release provenance
- image registry controls
- penetration/security assessments where applicable
- audit evidence
- production vulnerability monitoring

---

# 224. Success Criteria

The decision is successful when:

- builds resolve deterministic dependency versions
- arbitrary repositories disappear from services
- dependency upgrades are reviewable
- critical vulnerabilities can be mapped quickly to affected services
- every production release can produce an accurate SBOM
- deployed images correspond to tested/scanned artifacts
- secrets are not stored in repositories
- production artifacts are traceable to Git revisions
- unsupported dependencies are actively managed
- vulnerability exceptions remain explicit and temporary
- supply-chain findings prevent unsafe releases
- emergency CVE remediation can occur rapidly without abandoning quality controls

---

# 225. Alternatives Rejected

## 225.1 Always Use Latest Dependency

Rejected because newest does not guarantee compatibility, stability or security.

---

## 225.2 Never Upgrade Unless Broken

Rejected because security and support lifecycles require proactive maintenance.

---

## 225.3 Depend Only on Vulnerability Scanner Result

Rejected because vulnerability scanners require contextual exploitability assessment.

---

## 225.4 Ignore Transitive Dependencies

Rejected because transitive code executes in the production process.

---

## 225.5 Rebuild Separately Per Environment

Rejected because it weakens artifact equivalence and release traceability.

---

## 225.6 Use `latest` for Production Images

Rejected because mutable identities prevent deterministic deployment.

---

## 225.7 Delete Committed Secret and Continue Using It

Rejected because the secret may already have been copied and must be rotated.

---

## 225.8 Build Production Artifact on Developer Machine

Rejected because production builds require controlled and auditable infrastructure.

---

# 226. Related Decisions

This ADR is related to:

- ADR-001: Adopt Clean Architecture
- ADR-004: Use Spring Boot
- ADR-005: Use PostgreSQL as the Primary Database
- ADR-009: Use Apache Kafka for Integration Events
- ADR-016: Adopt Resilience4j for Application Resilience
- ADR-026: Adopt Platform Configuration and Secret Management Standards
- ADR-029: Adopt Data Protection, Privacy and Retention Standards
- ADR-035: Adopt Engineering Quality and Testing Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-039: Adopt CI/CD, Release and Deployment Governance Standards

---

# 227. References

- NIST Secure Software Development Framework
- OWASP Software Component Verification Standard
- OWASP Dependency-Check
- OWASP Dependency Management Cheat Sheet
- CycloneDX Specification
- SPDX Specification
- SLSA Framework
- Gradle Dependency Management Documentation
- Gradle Dependency Locking Documentation
- Gradle Wrapper Documentation
- Spring Boot Dependency Management
- OCI Image Specification
- CWE
- CVE
- CVSS

---

# 228. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | Enterprise Order Platform Architecture Team | Approved | Initial software supply-chain security baseline |

---

# 229. Decision Summary

The definitive dependency model is:

```text
APPLICATION
     |
     v
DECLARED DEPENDENCIES
     |
     v
CONTROLLED VERSION MANAGEMENT
     |
     v
TRANSITIVE GRAPH
     |
     v
LOCK / REPRODUCIBLE RESOLUTION
     |
     v
SECURITY SCAN
     |
     v
TEST
     |
     v
SBOM
     |
     v
ARTIFACT
```

Not:

```text
implementation "library:latest.release"
                  |
                  v
              INTERNET
                  |
                  v
         WHATEVER EXISTS TODAY
```

The definitive artifact model is:

```text
GIT COMMIT
    |
    v
CONTROLLED CI
    |
    v
BUILD ONCE
    |
    +----------------+
    |                |
    v                v
  TEST              SBOM
    |                |
    +-------+--------+
            |
            v
       SECURITY SCAN
            |
            v
      CONTAINER IMAGE
            |
            v
          DIGEST
            |
       +----+----+
       |    |    |
       v    v    v
      DEV   QA  PROD
```

For a new CVE:

```text
                  NEW CVE
                     |
                     v
               SBOM INVENTORY
                     |
                     v
             AFFECTED SERVICES
                     |
                     v
                REACHABILITY
                     |
             +-------+-------+
             |               |
             v               v
        EXPLOITABLE      NOT REACHABLE
             |               |
             v               v
       URGENT FIX       DOCUMENT RISK
             |
             v
       REGRESSION TEST
             |
             v
        SECURITY SCAN
             |
             v
           RELEASE
```

For dependency upgrades:

```text
Dependency Change
       |
       v
Resolved Graph Diff
       |
       v
Compatibility
       |
       v
Tests
       |
       v
Security
       |
       v
Review
       |
       v
Release
```

And for secrets:

```text
SECRET COMMITTED
       |
       v
SECRET COMPROMISED
       |
       +--> REVOKE
       |
       +--> ROTATE
       |
       +--> REMOVE
       |
       +--> ASSESS EXPOSURE
```

not:

```text
SECRET COMMITTED
       |
       v
DELETE LINE
       |
       v
DONE
```

The core principle is:

```text
We do not deploy source code.

We deploy source code
+
dependencies
+
runtime
+
container
+
configuration
+
build provenance.
```

Therefore all of them are part of the security boundary.

The desired state is:

```text
CONTROLLED
+
REPRODUCIBLE
+
SCANNED
+
TRACEABLE
+
IMMUTABLE
+
SUPPORTED
```

not merely:

```text
./gradlew build = SUCCESS
```

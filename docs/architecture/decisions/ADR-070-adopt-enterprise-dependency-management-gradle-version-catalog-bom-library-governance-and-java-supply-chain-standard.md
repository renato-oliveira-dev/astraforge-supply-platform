# ADR-070: Adopt Enterprise Dependency Management, Gradle, Version Catalog, BOM, Library Governance and Java Supply Chain Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-070 |
| Title | Adopt Enterprise Dependency Management, Gradle, Version Catalog, BOM, Library Governance and Java Supply Chain Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | Gradle, Dependencies, BOMs, Version Catalogs, Library Governance, Supply Chain |
| Related Work Items | Java 21, Spring Boot, Gradle, SCA, SBOM, Dependency Security |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Modern Java applications depend on large dependency graphs.

A typical Spring Boot service may include:

```text
APPLICATION
    |
    +--> Spring Boot
    |
    +--> Spring Security
    |
    +--> Spring Data
    |
    +--> Hibernate
    |
    +--> PostgreSQL Driver
    |
    +--> Kafka
    |
    +--> AWS SDK
    |
    +--> Resilience4j
    |
    +--> Micrometer
    |
    +--> Jackson
    |
    +--> Testcontainers
    |
    +--> JUnit
    |
    +--> Mockito
    |
    +--> AssertJ
```

Each direct dependency can introduce additional transitive dependencies.

The real graph may therefore contain hundreds of artifacts.

Poor dependency governance can create:

```text
Version Conflicts

ClassNotFoundException

NoSuchMethodError

Security Vulnerabilities

Unsupported Libraries

Incompatible Framework Versions

Unreproducible Builds

Dependency Drift

Plugin Compromise

Repository Supply-Chain Risk

Shared-Library Coupling
```

Dependency management is therefore part of architecture and software supply-chain security.

---

# 2. Problem Statement

The organization requires standards covering:

- Gradle
- Gradle Wrapper
- Java Toolchains
- dependency declarations
- dependency scopes
- BOMs
- Spring Boot dependency management
- AWS SDK BOM
- version catalogs
- dependency constraints
- transitive dependencies
- dependency locking
- dependency verification
- checksums
- signatures
- artifact repositories
- plugins
- dynamic versions
- SNAPSHOT versions
- dependency updates
- CVEs
- SCA
- Renovate/Dependabot
- SBOM
- CycloneDX
- reproducible builds
- Gradle configuration cache
- build cache
- shared internal libraries
- library versioning
- backward compatibility
- library deprecation
- supply-chain governance

---

# 3. Decision Drivers

Primary drivers are:

1. build reproducibility
2. dependency compatibility
3. security
4. upgradeability
5. maintainability
6. supply-chain integrity
7. deterministic releases
8. developer productivity
9. framework alignment
10. dependency visibility
11. reduced version drift
12. controlled shared-library coupling

---

# 4. Decision

Gradle is the standard build and dependency-management tool for applicable Java services.

The dependency architecture MUST follow:

```text
SOURCE
  |
  v
GRADLE WRAPPER
  |
  v
CONTROLLED PLUGINS
  |
  v
VERSION CATALOG / BOM
  |
  v
DEPENDENCY RESOLUTION
  |
  +--> APPROVED REPOSITORIES
  |
  +--> VERSION CONSTRAINTS
  |
  +--> LOCKING / VERIFICATION
  |
  v
REPRODUCIBLE DEPENDENCY GRAPH
  |
  v
BUILD
```

---

# 5. Fundamental Principle

```text
A dependency is code
that the application
chooses to trust.

Every dependency expands:

Attack Surface

Compatibility Surface

Upgrade Surface

Operational Surface

Therefore dependencies must be
deliberate, controlled and observable.
```

---

# 6. Gradle Wrapper

Every Gradle project MUST include the Gradle Wrapper.

Required files include:

```text
gradlew

gradlew.bat

gradle/wrapper/gradle-wrapper.properties

gradle/wrapper/gradle-wrapper.jar
```

---

# 7. Wrapper Usage

CI and documented local builds MUST use:

```text
./gradlew
```

or:

```text
gradlew.bat
```

rather than an arbitrary globally installed Gradle.

---

# 8. Gradle Version

The Gradle version MUST be explicitly controlled through the Wrapper.

---

# 9. Wrapper Upgrade

Gradle Wrapper upgrades MUST be deliberate and validated.

---

# 10. Wrapper Integrity

Wrapper binaries and configuration MUST remain under source control according to Gradle security recommendations.

---

# 11. Java Toolchain

Java projects SHOULD use Gradle Java Toolchains.

Example:

```groovy
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

---

# 12. Toolchain Purpose

Toolchains reduce dependency on the developer workstation's default JDK.

---

# 13. Java Baseline

The project baseline MUST explicitly define the supported Java version.

For the Enterprise Order Platform:

```text
Java 21
```

is the standard baseline unless an ADR explicitly defines otherwise.

---

# 14. Source Compatibility

Projects SHOULD NOT rely only on:

```text
sourceCompatibility
```

when Toolchains provide stronger build reproducibility.

---

# 15. Gradle Plugins

Gradle plugins are executable supply-chain dependencies.

They MUST be governed accordingly.

---

# 16. Plugin Version

Plugin versions MUST be explicit.

---

# 17. Dynamic Plugin Version

This is prohibited:

```text
plugin version "+"
```

or equivalent uncontrolled dynamic plugin resolution.

---

# 18. Plugin Repository

Plugins MUST come from approved repositories.

---

# 19. Third-Party Plugin

New third-party plugins SHOULD be reviewed for:

```text
Maintenance

Security

License

Release Activity

Necessity

Transitive Behavior
```

---

# 20. Plugin Minimization

Do not add a plugin merely to automate a trivial build operation that can be implemented safely with standard Gradle functionality.

---

# 21. Dependency Scope

Dependencies MUST use the narrowest appropriate Gradle configuration.

Examples:

```text
implementation

api

runtimeOnly

compileOnly

annotationProcessor

testImplementation

testRuntimeOnly
```

---

# 22. implementation

`implementation` SHOULD be the default for internal implementation dependencies.

---

# 23. api

`api` SHOULD only be used when a library's public API intentionally exposes types from that dependency.

---

# 24. api Overuse

Overuse of:

```text
api
```

in shared libraries increases downstream coupling and MUST be avoided.

---

# 25. runtimeOnly

Runtime-only dependencies SHOULD use:

```text
runtimeOnly
```

when compile-time access is unnecessary.

Example:

```text
PostgreSQL JDBC driver
```

depending on project structure.

---

# 26. testImplementation

Test-only dependencies MUST remain outside production runtime dependencies.

---

# 27. Test Dependency Leakage

Production code MUST NOT compile against test-only libraries.

---

# 28. BOM

A Bill of Materials SHOULD align compatible dependency families.

---

# 29. Spring Boot BOM

Spring Boot dependency management SHOULD control versions of Spring-aligned dependencies.

---

# 30. Spring Boot Managed Version

When Spring Boot already manages a dependency version, projects SHOULD avoid unnecessarily overriding it.

---

# 31. Manual Override

A Spring Boot managed dependency version MAY be overridden only when:

```text
A known defect requires it

A security fix requires it

A compatibility requirement requires it
```

and the override MUST be tested.

---

# 32. Framework Alignment

Spring Framework, Spring Security, Spring Data and related dependencies SHOULD remain aligned with the selected Spring Boot release train.

---

# 33. Partial Spring Upgrade

Manually upgrading one Spring module outside the supported Boot dependency set SHOULD be avoided.

---

# 34. AWS SDK BOM

AWS SDK v2 dependency families SHOULD use the AWS SDK BOM when multiple AWS modules are required.

Conceptually:

```groovy
implementation platform("software.amazon.awssdk:bom:<version>")

implementation "software.amazon.awssdk:sqs"
implementation "software.amazon.awssdk:sts"
```

---

# 35. BOM Consistency

Related AWS SDK modules SHOULD NOT use independently selected versions.

---

# 36. Multiple BOMs

Multiple BOMs MAY coexist when they govern independent dependency families.

---

# 37. BOM Conflict

BOM precedence/conflicts MUST be understood.

---

# 38. Platform Dependency

Gradle `platform(...)` or `enforcedPlatform(...)` SHOULD be chosen deliberately.

---

# 39. enforcedPlatform

`enforcedPlatform` SHOULD be used cautiously because it forces versions downstream and may reduce consumer flexibility in reusable libraries.

---

# 40. Version Catalog

Gradle Version Catalogs SHOULD be used for consistent dependency aliases and version management in sufficiently large projects or multi-module repositories.

Example:

```toml
[versions]
testcontainers = "..."
resilience4j = "..."

[libraries]
testcontainers-postgresql = {
    module = "org.testcontainers:postgresql",
    version.ref = "testcontainers"
}
```

---

# 41. Version Catalog Purpose

Version Catalogs improve:

```text
Discoverability

Consistency

Centralization

Refactoring

Dependency Review
```

---

# 42. Catalog Is Not BOM

A Version Catalog does not provide dependency compatibility semantics equivalent to a BOM.

---

# 43. Catalog + BOM

A project MAY use:

```text
Version Catalog
      +
BOM
```

where the catalog provides naming and the BOM controls compatible versions.

---

# 44. Dependency Version Duplication

Repeated hardcoded versions across build files SHOULD be consolidated.

---

# 45. Version Ownership

Each dependency family SHOULD have one clear version-control strategy.

---

# 46. Dependency Constraint

Gradle dependency constraints MAY define allowed/required versions without directly introducing the dependency.

---

# 47. Constraint Use

Constraints SHOULD be considered for:

```text
Transitive Security Fixes

Known Compatibility Bounds

Library Platform Governance
```

---

# 48. Force

Gradle dependency:

```text
force
```

SHOULD NOT be the default mechanism for solving version conflicts.

---

# 49. Force Risk

A forced version can create runtime incompatibility hidden from dependency resolution.

---

# 50. Conflict Resolution

Version conflicts SHOULD be solved by understanding:

```text
Why both versions exist

Which dependency introduces them

Which version is compatible

Whether the dependency should be excluded/upgraded
```

---

# 51. Transitive Dependency

Transitive dependencies MUST be treated as part of the application supply chain.

---

# 52. Dependency Insight

Gradle dependency diagnostics SHOULD be used when resolving unexpected versions.

Examples:

```text
dependencies

dependencyInsight
```

---

# 53. Dependency Graph Review

Critical upgrade work SHOULD inspect the resolved dependency graph.

---

# 54. Transitive Exclusion

Exclusions MAY be used when the transitive dependency is:

```text
Unnecessary

Conflicting

Vulnerable

Replaced
```

---

# 55. Exclusion Risk

An exclusion MUST not remove a dependency still required at runtime.

---

# 56. Broad Exclusion

Broad global exclusions SHOULD be avoided without evidence.

---

# 57. Dependency Locking

Dependency locking SHOULD be considered for services requiring highly reproducible dependency resolution.

---

# 58. Lock File

Lock files MUST be committed when locking is enabled.

---

# 59. Lock Update

Dependency lock updates MUST be deliberate and reviewable.

---

# 60. Lock Drift

Lockfiles MUST NOT be regenerated casually without reviewing actual dependency changes.

---

# 61. Dynamic Version

Production projects MUST NOT use uncontrolled dynamic versions such as:

```text
1.+

[1.0,2.0)

latest.release
```

for ordinary production dependencies.

---

# 62. Dynamic Resolution Risk

Dynamic versions make two builds from the same commit potentially produce different binaries.

---

# 63. SNAPSHOT

External:

```text
-SNAPSHOT
```

dependencies MUST NOT be used in final production releases unless explicitly approved by architecture/release governance.

---

# 64. Internal SNAPSHOT

Internal SNAPSHOT dependencies MAY be used during active development but MUST NOT become an uncontrolled production release dependency.

---

# 65. Immutable Release

Production must depend on immutable published library versions.

---

# 66. Repository Governance

Artifact repositories MUST be explicitly controlled.

---

# 67. Approved Repository

Examples may include:

```text
Enterprise Artifact Repository

Maven Central

Approved Vendor Repository
```

according to organizational policy.

---

# 68. Arbitrary Repository

Developers MUST NOT add arbitrary public repositories merely because one dependency is unavailable elsewhere.

---

# 69. Repository Order

Repository order SHOULD be deterministic.

---

# 70. mavenLocal

Production CI SHOULD NOT depend on:

```text
mavenLocal()
```

---

# 71. Developer Local Artifact

A developer-local artifact MUST NOT silently satisfy a production dependency.

---

# 72. Repository Credentials

Private repository credentials MUST follow secret-management standards.

---

# 73. Credentials in Build Script

Credentials MUST NOT be hardcoded into:

```text
build.gradle

settings.gradle

gradle.properties committed to Git
```

---

# 74. Dependency Verification

Gradle dependency verification SHOULD be enabled for high-assurance builds.

---

# 75. Verification Metadata

Verification metadata MAY record trusted:

```text
Checksums

Signatures
```

---

# 76. Checksum Validation

Unexpected artifact checksum changes MUST fail or trigger explicit review.

---

# 77. Signature Verification

Artifact signature verification SHOULD be adopted where repository/ecosystem support provides meaningful assurance.

---

# 78. Verification Update

Verification metadata updates MUST be reviewed rather than blindly accepted.

---

# 79. Supply-Chain Principle

The build MUST distinguish:

```text
EXPECTED NEW ARTIFACT
```

from:

```text
EXISTING VERSION
WITH DIFFERENT BYTES
```

---

# 80. Reproducible Build

Given the same:

```text
Source Commit

Toolchain

Dependency Graph

Build Configuration
```

the project SHOULD produce functionally equivalent artifacts.

---

# 81. Build Timestamp

Non-deterministic timestamps SHOULD NOT unnecessarily make artifacts unique when reproducible-build tooling can normalize them.

---

# 82. Generated Content

Generated sources/resources MUST be deterministic where practical.

---

# 83. Dependency Cache

Gradle dependency caching MAY improve performance.

---

# 84. Cache Is Not Trust

Local/CI cache presence MUST NOT bypass dependency verification.

---

# 85. Build Cache

Gradle Build Cache MAY be enabled where task inputs/outputs are correctly declared.

---

# 86. Build Cache Risk

Incorrectly declared task inputs can produce stale build output.

---

# 87. Custom Gradle Task

Custom tasks MUST define appropriate inputs and outputs if they participate in caching/incremental builds.

---

# 88. Configuration Cache

Gradle Configuration Cache SHOULD be adopted where compatible and beneficial.

---

# 89. Configuration Cache Compatibility

Build logic and plugins MUST be validated for Configuration Cache compatibility before enabling it as a mandatory CI feature.

---

# 90. Configuration Cache Is Optimization

Configuration Cache MUST NOT change build semantics.

---

# 91. Build Performance

Build performance SHOULD be measured.

---

# 92. Slow Dependency Resolution

Slow dependency resolution SHOULD trigger investigation into:

```text
Repository latency

Too many repositories

Dynamic versions

Metadata resolution

Plugin behavior
```

---

# 93. Dependency Update

Dependencies MUST be updated continuously rather than through rare massive upgrade events.

---

# 94. Small Upgrade

Smaller frequent updates generally reduce migration complexity.

---

# 95. Major Upgrade

Major framework upgrades require explicit compatibility validation.

---

# 96. Automated Update Tool

Dependabot, Renovate or equivalent MAY be used to automate dependency-update proposals.

---

# 97. Automated PR

Automated update pull requests MUST still pass normal:

```text
Build

Tests

Sonar

SAST

SCA

Integration Tests
```

---

# 98. Auto-Merge

Auto-merge SHOULD be limited to low-risk changes with strong automated validation.

---

# 99. Major Auto-Merge

Major dependency versions SHOULD NOT normally auto-merge.

---

# 100. Security Update

High-risk dependency vulnerabilities SHOULD receive accelerated remediation.

---

# 101. CVE

A CVE MUST be evaluated in context.

---

# 102. CVE Risk

Risk evaluation SHOULD include:

```text
Severity

Exploitability

Reachability

Runtime Usage

Public Exposure

Fix Availability

Compensating Controls
```

---

# 103. CVSS

CVSS is useful but MUST NOT be the sole security decision metric.

---

# 104. Reachability

A vulnerable library function not reachable by the application may represent lower practical risk than a directly exposed path.

---

# 105. SCA

Software Composition Analysis is mandatory for applicable production services.

---

# 106. Direct and Transitive

SCA MUST include:

```text
Direct Dependencies

Transitive Dependencies
```

---

# 107. Vulnerability Exception

Security exceptions MUST be:

```text
Explicit

Owned

Risk Accepted

Time-Bounded

Tracked
```

---

# 108. Dependency Age

A dependency being old is not automatically a vulnerability, but unsupported versions SHOULD trigger review.

---

# 109. End-of-Life Dependency

Unsupported libraries/framework versions SHOULD have migration plans.

---

# 110. SBOM

Production artifacts SHOULD generate an SBOM.

---

# 111. CycloneDX

CycloneDX SHOULD be considered the preferred SBOM format for Java application supply-chain integration where compatible with enterprise tooling.

---

# 112. SPDX

SPDX MAY be used when required by enterprise or compliance tooling.

---

# 113. SBOM Artifact Binding

The SBOM MUST correspond to the exact release artifact.

---

# 114. SBOM Content

SBOM SHOULD include applicable:

```text
Component Name

Version

Package Coordinates

Dependency Relationship

Hashes

Licenses
```

---

# 115. Runtime SBOM

SBOM generation SHOULD represent dependencies actually included in the release/runtime artifact rather than only source declarations.

---

# 116. SBOM Storage

Release SBOMs SHOULD be retained with release evidence.

---

# 117. License Governance

Dependency licenses MUST comply with enterprise legal policy.

---

# 118. Unknown License

Dependencies with unknown or incompatible license terms MUST be reviewed.

---

# 119. Library Necessity

Before adding a dependency, engineers SHOULD ask:

```text
Can the JDK already do this?

Can Spring already do this?

Is the library maintained?

Is the library secure?

What transitive graph does it add?

Will it be used enough to justify it?
```

---

# 120. Tiny Library

A library that replaces a trivial amount of stable standard Java code SHOULD be evaluated critically.

---

# 121. Large Framework

A large framework MUST NOT be added to solve one minor feature when a smaller supported alternative exists.

---

# 122. Dependency Surface

Every new library adds:

```text
Upgrade Work

CVE Surface

License Surface

Compatibility Surface
```

---

# 123. Internal Shared Library

Internal shared libraries MAY be created when they provide a stable reusable technical capability.

---

# 124. Good Shared Library

Suitable examples include:

```text
Standard Error Contract

Security Integration

Correlation Infrastructure

Approved HTTP Client Infrastructure

Testing Utilities

Observability Bootstrap
```

when they are stable and genuinely shared.

---

# 125. Poor Shared Library

A shared library SHOULD NOT contain service-specific business/domain logic.

---

# 126. Shared Domain Model

Sharing domain entities across bounded contexts is strongly discouraged.

---

# 127. Shared JPA Entity

Internal libraries MUST NOT be used to share JPA entities across independently owned microservices.

---

# 128. Shared Repository

Shared persistence repositories across services are prohibited.

---

# 129. Shared DTO

Shared public contract libraries MAY be used selectively, but they create compile-time coupling and MUST be justified.

---

# 130. Generated Contract

OpenAPI/event-schema code generation MAY provide a better contract-sharing mechanism than manually shared DTO libraries in some architectures.

---

# 131. Library Ownership

Every internal shared library MUST have an identifiable owner.

---

# 132. Library Purpose

The library MUST document:

```text
Scope

Supported Use Cases

Non-Goals

Versioning

Compatibility Policy
```

---

# 133. Internal Library Version

Published internal libraries MUST use immutable versions.

---

# 134. Overwrite Internal Version

An internal release version MUST NOT be overwritten with different contents.

---

# 135. Semantic Versioning

Reusable internal libraries SHOULD consider semantic versioning.

---

# 136. MAJOR

A breaking public API change SHOULD require a major-version change where semantic versioning is used.

---

# 137. MINOR

Backward-compatible capability additions SHOULD use a minor version.

---

# 138. PATCH

Backward-compatible fixes SHOULD use a patch version.

---

# 139. Internal Library Compatibility

Consumers MUST NOT be forced to upgrade immediately without architectural justification.

---

# 140. Shared Library Release Cadence

A shared library that requires simultaneous upgrade of all consuming services indicates excessive coupling.

---

# 141. Library Dependency Direction

Internal platform libraries SHOULD depend on stable abstractions and minimize unnecessary framework exposure.

---

# 142. Library API

Public library APIs SHOULD expose the minimum practical surface.

---

# 143. Internal API

Implementation classes SHOULD remain package-private or internal where possible.

---

# 144. Library Transitive Dependency

Reusable libraries SHOULD minimize `api` dependencies.

---

# 145. Consumer Version Conflict

A shared library SHOULD avoid forcing unrelated dependency versions onto consumers.

---

# 146. Spring Boot Starter

A custom Spring Boot Starter MAY be created for stable cross-service infrastructure configuration.

---

# 147. Starter Candidate

Good candidates include:

```text
Observability

Security

HTTP Client Defaults

Standard Error Handling
```

when conventions are broadly shared.

---

# 148. Starter Overreach

A starter MUST NOT silently impose unrelated application behavior.

---

# 149. Auto-Configuration

Custom auto-configuration MUST be:

```text
Conditional

Documented

Overridable

Tested
```

---

# 150. Hidden Bean

Shared libraries MUST NOT unexpectedly introduce broad bean graphs without clear documentation.

---

# 151. Library Configuration

Shared-library configuration properties SHOULD use a stable namespace.

---

# 152. Backward-Compatible Configuration

Library configuration changes MUST follow compatibility governance.

---

# 153. Library Deprecation

Deprecated library APIs MUST provide migration guidance.

---

# 154. Removal

Breaking removal SHOULD occur only after an appropriate deprecation/migration period unless security requires urgent removal.

---

# 155. Internal Library Tests

Internal shared libraries require strong automated tests because defects propagate to multiple services.

---

# 156. Consumer Compatibility Test

Critical libraries SHOULD test compatibility against representative consumers where practical.

---

# 157. BOM for Internal Libraries

An enterprise/platform BOM MAY align versions of approved internal libraries.

---

# 158. Enterprise BOM

A platform BOM SHOULD contain only dependencies whose versions are intentionally governed together.

---

# 159. BOM Bloat

A BOM MUST NOT become a random list of every library used by the company.

---

# 160. Dependency Governance Layers

The preferred hierarchy is:

```text
SPRING BOOT BOM
      |
      +--> Spring Ecosystem

AWS SDK BOM
      |
      +--> AWS SDK Modules

ENTERPRISE BOM
      |
      +--> Approved Internal Libraries

VERSION CATALOG
      |
      +--> Aliases / Remaining Versions
```

---

# 161. Explicit Version Override

Any override of a governed version SHOULD be visible and documented.

---

# 162. Duplicate Dependency

Duplicate declarations SHOULD be removed when they add no semantic value.

---

# 163. Conflicting Logging Stack

Applications MUST avoid incompatible parallel logging implementations.

---

# 164. Logging Binding

Only the intended logging backend/binding SHOULD be present at runtime.

---

# 165. Multiple JSON Libraries

Adding multiple libraries solving the same serialization concern SHOULD require justification.

---

# 166. Dependency Convergence

The runtime graph SHOULD avoid multiple incompatible major versions of the same library family.

---

# 167. Shading

Dependency shading/relocation MAY isolate unavoidable conflicts in specialized libraries, but it increases build complexity and MUST be justified.

---

# 168. Fat JAR

Spring Boot executable JARs SHOULD include only required runtime dependencies.

---

# 169. Runtime Classpath

Unused runtime dependencies SHOULD be removed.

---

# 170. Build Dependency

Build-only tooling MUST NOT leak into runtime artifacts.

---

# 171. Annotation Processor

Annotation processors SHOULD be explicitly declared.

---

# 172. Lombok

Lombok MAY be used where approved but MUST NOT obscure domain behavior or encourage mutable designs contrary to architecture standards.

---

# 173. MapStruct

MapStruct MAY be used where generated mapping materially reduces repetitive mapping code.

---

# 174. MapStruct Scope

MapStruct MUST NOT become a substitute for explicit business transformations.

---

# 175. Code Generator Version

Code generators MUST be version controlled.

---

# 176. Generated Source

Generated output SHOULD be reproducible.

---

# 177. BuildSrc

Gradle `buildSrc` MAY centralize build logic but can increase configuration coupling/rebuild costs.

---

# 178. Convention Plugin

Gradle convention plugins SHOULD be preferred for reusable build conventions in large multi-module repositories.

---

# 179. Convention Plugin Scope

Convention plugins MAY standardize:

```text
Java Toolchain

Compiler Options

Testing

JaCoCo

Sonar

Common Repositories
```

---

# 180. Business Logic in Build Plugin

Build convention plugins MUST NOT contain application business logic.

---

# 181. Multi-Module Build

Multi-module repositories SHOULD centralize shared build configuration while preserving module-specific dependencies.

---

# 182. Root Dependency Dump

All dependencies SHOULD NOT be indiscriminately declared at the root and inherited by every subproject.

---

# 183. Module Dependency

Each module SHOULD declare the dependencies it actually requires.

---

# 184. Dependency Boundary

Module dependency graphs SHOULD reinforce architecture boundaries.

---

# 185. Circular Module Dependency

Circular Gradle module dependencies are prohibited.

---

# 186. Dependency on Implementation Module

Modules SHOULD depend on stable module APIs rather than unrelated implementation details.

---

# 187. Test Fixtures Plugin

Gradle Test Fixtures MAY be used for reusable test-only fixtures between modules.

---

# 188. Test Fixture Scope

Test fixtures MUST NOT become production dependency shortcuts.

---

# 189. Dependency Review Checklist

Every new dependency SHOULD evaluate:

```text
[ ] Is it actually needed?

[ ] Can Java/Spring already solve the problem?

[ ] Is it actively maintained?

[ ] Is the license acceptable?

[ ] Does it have known vulnerabilities?

[ ] How large is its transitive graph?

[ ] Is its version already managed by a BOM?

[ ] Is the version duplicated elsewhere?

[ ] Does it belong in implementation/api/runtimeOnly/testImplementation?

[ ] Does it force another major library version?

[ ] Does it affect runtime artifact size?

[ ] Is it compatible with Java 21?

[ ] Is it compatible with Spring Boot baseline?

[ ] Is the repository approved?

[ ] Does it introduce native/runtime operational requirements?

[ ] How will it be upgraded?

[ ] How will it be removed?
```

---

# 190. Upgrade Review Checklist

A dependency upgrade SHOULD evaluate:

```text
[ ] Release notes reviewed

[ ] Breaking changes reviewed

[ ] CVEs resolved

[ ] Transitive graph compared

[ ] Build passes

[ ] Unit tests pass

[ ] Integration tests pass

[ ] Contract tests pass

[ ] Sonar passes

[ ] SAST/SCA passes

[ ] Runtime smoke test passes

[ ] Performance-sensitive paths reviewed
```

---

# 191. Shared Library Review Checklist

Before creating an internal library:

```text
[ ] Is the capability genuinely shared?

[ ] Is it technical rather than bounded-context business logic?

[ ] Is the contract stable?

[ ] Who owns the library?

[ ] How is it versioned?

[ ] Can consumers upgrade independently?

[ ] Does it expose transitive dependencies unnecessarily?

[ ] Would code generation be better?

[ ] Would duplication be safer than coupling?

[ ] Does this create a distributed-monolith dependency?
```

---

# 192. Dependency Fitness Functions

Stable dependency rules SHOULD be automated where practical.

Examples:

```text
[ ] Gradle Wrapper version approved

[ ] Java Toolchain = 21

[ ] No dynamic production dependency versions

[ ] No production SNAPSHOT dependencies

[ ] No mavenLocal in CI build

[ ] Only approved repositories configured

[ ] Dependency verification enabled where required

[ ] Dependency vulnerability gate passes

[ ] SBOM generated

[ ] Duplicate versions detected

[ ] Prohibited libraries rejected

[ ] Shared libraries do not expose forbidden domain packages
```

---

# 193. Enterprise Dependency Gate

A service is not considered compliant when applicable conditions include:

```text
[ ] Build uses developer-installed Gradle instead of Wrapper

[ ] Java baseline is implicit

[ ] Dynamic dependency version exists

[ ] Production SNAPSHOT dependency exists without approval

[ ] Spring-managed versions are arbitrarily overridden

[ ] AWS SDK modules use inconsistent versions

[ ] Arbitrary public repository added

[ ] Production CI depends on mavenLocal

[ ] Artifact verification is bypassed without reason

[ ] Critical known CVE remains untracked

[ ] Dependency graph contains unsupported incompatible versions

[ ] Existing library version was overwritten

[ ] Shared library contains service-specific domain models

[ ] Shared JPA entities couple microservices

[ ] Internal library requires coordinated release of all consumers

[ ] SBOM cannot be generated for the production artifact
```

---

# 194. Anti-Patterns

The following are prohibited or strongly discouraged:

- system Gradle as release build dependency
- uncontrolled Gradle version drift
- undeclared Java version assumptions
- dynamic dependency versions
- production SNAPSHOT dependencies
- arbitrary Spring dependency overrides
- independent AWS SDK module versions
- dependency version copied across many build files
- forcing versions without understanding conflicts
- broad transitive exclusions
- arbitrary public repositories
- `mavenLocal()` in CI
- credentials committed in Gradle configuration
- blindly accepting checksum changes
- blindly regenerating lock files
- large dependency upgrades performed rarely and all at once
- unresolved high-risk CVEs without ownership
- huge third-party library for trivial functionality
- shared domain-model library across bounded contexts
- shared JPA entity libraries between microservices
- overwritten internal library release versions
- shared library that forces coordinated deployment
- universal internal BOM containing unrelated libraries
- root build applying every dependency to every module
- circular Gradle module dependencies
- build plugins with application business logic

---

# 195. Positive Consequences

The decision provides:

- reproducible builds
- consistent Java baseline
- controlled framework alignment
- reduced dependency conflicts
- safer AWS SDK integration
- clearer version ownership
- stronger supply-chain security
- improved vulnerability visibility
- better SBOM quality
- reduced shared-library coupling
- safer upgrades
- more maintainable multi-module builds

---

# 196. Negative Consequences

The decision introduces:

- dependency governance
- lock/verification metadata maintenance
- upgrade review effort
- BOM/catalog maintenance
- shared-library release management
- SCA remediation work
- stricter repository controls

These costs are accepted because unmanaged dependencies create both operational and security risk.

---

# 197. Neutral Consequences

The decision also means:

- not every dependency needs a manually declared version
- not every project requires dependency locking
- not every project requires an internal BOM
- not every repeated utility belongs in a shared library
- newer is not automatically safer
- older is not automatically vulnerable
- a dependency with many transitive artifacts is not automatically wrong
- a shared library can reduce duplication but increase coupling

---

# 198. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Supply-chain compromise | Critical | Low/Medium | Verification + approved repositories |
| Dependency CVE | Critical | High | SCA + continuous updates |
| Version conflict | High | Medium | BOMs + dependency analysis |
| Unreproducible build | High | Medium | Wrapper + locking + fixed versions |
| Spring incompatibility | Critical | Medium | Boot-managed dependency set |
| AWS SDK mismatch | High | Medium | AWS BOM |
| Shared-library coupling | High | Medium | Library governance |
| Upgrade backlog | High | High | Small continuous updates |
| License violation | High | Low/Medium | License scanning |
| Build performance degradation | Medium | Medium | Configuration/build cache |

---

# 199. Implementation Guidance

The following rules are mandatory:

1. Gradle Wrapper must be used for all applicable builds.
2. Java Toolchains should define the Java 21 baseline.
3. Dependency scopes must be as narrow as practical.
4. Spring Boot managed versions should be used unless an explicit exception exists.
5. AWS SDK modules should use a shared AWS SDK BOM.
6. Version Catalogs should centralize aliases/versions in sufficiently large builds.
7. BOMs and Version Catalogs must not be confused as equivalent mechanisms.
8. Dependency constraints should be used for deliberate transitive version governance.
9. Forced versions should be exceptional.
10. Dynamic dependency versions are prohibited for production.
11. Production SNAPSHOT dependencies are prohibited unless explicitly approved.
12. Artifact repositories must be approved.
13. Production CI must not depend on `mavenLocal()`.
14. Repository credentials must remain outside source control.
15. Dependency locking should be adopted where reproducibility requirements justify it.
16. Dependency verification should be adopted for high-assurance builds.
17. Verification/lock updates must be reviewed.
18. Direct and transitive dependencies must be included in SCA.
19. High-risk CVEs must be remediated or explicitly risk-accepted.
20. Automated update tooling may propose dependency updates but must not bypass quality gates.
21. SBOMs should be generated for production artifacts.
22. Dependency licenses must meet organizational policy.
23. New dependencies must be justified against JDK/Spring/platform alternatives.
24. Unused dependencies must be removed.
25. Internal shared libraries must represent stable reusable capabilities.
26. Shared libraries must not contain service-specific domain models.
27. JPA entities and repositories must not be shared across microservices.
28. Internal library versions must be immutable.
29. Reusable internal libraries should have explicit compatibility/versioning policies.
30. Shared libraries should minimize transitive `api` exposure.
31. Custom Spring Boot starters must be conditional, documented and tested.
32. Multi-module builds should declare dependencies at the module that actually needs them.
33. Circular module dependencies are prohibited.
34. Convention plugins should be used for reusable build conventions where valuable.
35. Configuration Cache and Build Cache should be adopted only when semantics remain correct.
36. Dependency upgrades must run applicable build, test, Sonar, SAST and SCA gates.
37. Dependency governance must be enforced through automated CI fitness functions where practical.

---

# 200. Validation

This ADR will be validated through:

- Gradle Wrapper
- Gradle Java Toolchains
- Gradle Version Catalogs
- Gradle Dependency Constraints
- Gradle Dependency Locking
- Gradle Dependency Verification
- Spring Boot BOM
- AWS SDK BOM
- Java 21
- SCA tooling
- SonarQube
- SAST
- dependency vulnerability scanning
- CycloneDX
- SPDX where required
- SBOM validation
- Dependabot/Renovate where adopted
- license scanning
- CI/CD quality gates
- Testcontainers
- integration tests

---

# 201. Success Criteria

The decision is successful when:

- the same source commit resolves the same intended dependency graph
- Java and Gradle versions are explicit
- Spring framework dependencies remain compatible
- AWS SDK version drift disappears
- dynamic versions are eliminated
- production SNAPSHOT usage is eliminated
- repository sources are controlled
- vulnerable dependencies are identified early
- dependency upgrades become smaller and more frequent
- SBOMs accurately represent production releases
- shared libraries remain technically cohesive
- microservices can upgrade shared libraries independently
- build failures caused by dependency drift decrease
- supply-chain changes become auditable

---

# 202. Alternatives Rejected

## 202.1 Manual Versions Everywhere

Rejected because version duplication increases drift and maintenance cost.

---

## 202.2 Rely Only on Spring Boot BOM

Rejected as a universal strategy because non-Spring dependency families such as AWS SDK require their own governance.

---

## 202.3 Version Catalog as Compatibility Management

Rejected because catalogs centralize coordinates but do not inherently guarantee compatible dependency sets.

---

## 202.4 Dynamic Versions

Rejected because identical source commits could resolve different artifacts over time.

---

## 202.5 Shared Library for All Common Code

Rejected because excessive shared libraries create compile-time coupling and distributed-monolith behavior.

---

## 202.6 Rare Massive Dependency Upgrade

Rejected because large version jumps increase migration and diagnostic risk.

---

## 202.7 Blind Automated Dependency Update

Rejected because dependency changes still require automated compatibility and security validation.

---

# 203. Related Decisions

This ADR extends and implements:

- ADR-035: Engineering Quality and Testing Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-042: Architecture Fitness Functions and Automated Governance Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-051: Architecture Testing and Automated Fitness Functions
- ADR-052: Java 21 / Spring Boot Enterprise Coding Standard
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-064: Enterprise Authentication, Authorization, OAuth2/OIDC, JWT and Service-to-Service Security Standard
- ADR-065: Enterprise Domain-Driven Design, Service Boundaries, Clean Architecture and Modularization Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-069: Enterprise Code Review, Refactoring, Technical Debt and Legacy Modernization Standard

---

# 204. References

- Gradle User Manual
- Gradle Wrapper Documentation
- Gradle Java Toolchains
- Gradle Version Catalogs
- Gradle Dependency Locking
- Gradle Dependency Verification
- Spring Boot Dependency Management
- AWS SDK for Java 2.x BOM
- CycloneDX
- SPDX
- OWASP Dependency-Check
- OWASP Software Component Verification Standard
- SLSA
- NIST Secure Software Development Framework
- Maven Central Repository Guidelines
- Renovate Documentation
- Dependabot Documentation

---

# 205. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise Java dependency and library-governance baseline |

---

# 206. Decision Summary

The dependency model becomes:

```text
APPLICATION
    |
    v
GRADLE WRAPPER
    |
    v
JAVA TOOLCHAIN 21
    |
    v
DEPENDENCY GOVERNANCE
    |
    +--> SPRING BOOT BOM
    |
    +--> AWS SDK BOM
    |
    +--> ENTERPRISE BOM
    |
    +--> VERSION CATALOG
    |
    +--> CONSTRAINTS
    |
    v
RESOLVED GRAPH
    |
    v
LOCK / VERIFY
    |
    v
BUILD
```

Version ownership becomes:

```text
SPRING ECOSYSTEM
      |
      v
SPRING BOOT BOM

AWS SDK FAMILY
      |
      v
AWS SDK BOM

INTERNAL PLATFORM LIBRARIES
      |
      v
ENTERPRISE BOM

OTHER LIBRARIES
      |
      v
VERSION CATALOG
```

Dependency introduction becomes:

```text
NEED CAPABILITY?
      |
      v
CAN JDK / SPRING
ALREADY PROVIDE IT?
      |
   +--+--+
   |     |
  YES    NO
   |     |
   v     v
USE IT   EVALUATE LIBRARY
             |
             +--> SECURITY
             +--> LICENSE
             +--> MAINTENANCE
             +--> TRANSITIVES
             +--> COMPATIBILITY
             +--> NECESSITY
```

Version resolution must not become:

```text
application
   |
   v
library:1.+
   |
   v
WHATEVER EXISTS TODAY
```

Instead:

```text
application
   |
   v
CONTROLLED VERSION
   |
   v
VERIFIED ARTIFACT
```

Transitive dependency analysis becomes:

```text
DIRECT DEPENDENCY
       |
       v
TRANSITIVE GRAPH
       |
       +--> VERSION CONFLICT?
       |
       +--> CVE?
       |
       +--> LICENSE?
       |
       +--> UNUSED?
       |
       v
CONTROLLED RUNTIME
```

Shared-library decisions become:

```text
CODE REPEATED
    |
    v
IS IT A STABLE
TECHNICAL CAPABILITY?
    |
 +--+--+
 |     |
YES    NO
 |     |
 v     v
MAY    KEEP WITH
SHARE  CONTEXT OWNER
```

and:

```text
SHARED LIBRARY
      |
      X
      |
MUST NOT BECOME
      |
      v
SHARED DOMAIN DATABASE
MODEL FOR EVERY SERVICE
```

Dependency updates become:

```text
SMALL UPDATE
    |
    v
AUTOMATED PR
    |
    +--> BUILD
    +--> TEST
    +--> SONAR
    +--> SAST
    +--> SCA
    +--> INTEGRATION
    |
    v
MERGE
```

rather than:

```text
IGNORE UPDATES
FOR 3 YEARS
    |
    v
UPGRADE EVERYTHING
AT ONCE
```

Supply-chain validation becomes:

```text
DEPENDENCY
    |
    v
APPROVED REPOSITORY
    |
    v
CHECKSUM / SIGNATURE
    |
    v
SCA
    |
    v
BUILD
    |
    v
SBOM
    |
    v
IMMUTABLE RELEASE
```

The complete dependency equation is:

```text
GRADLE WRAPPER
        +
JAVA TOOLCHAIN
        +
BOM ALIGNMENT
        +
VERSION CATALOG
        +
CONTROLLED CONSTRAINTS
        +
FIXED VERSIONS
        +
APPROVED REPOSITORIES
        +
DEPENDENCY VERIFICATION
        +
DEPENDENCY LOCKING WHERE NEEDED
        +
CONTINUOUS CVE MANAGEMENT
        +
SBOM
        +
LICENSE GOVERNANCE
        +
SMALL CONTINUOUS UPGRADES
        +
CONTROLLED SHARED LIBRARIES
        =
RELIABLE JAVA SUPPLY CHAIN
```

The governing principle is:

```text
Treat every dependency
as code you chose to run.

Pin the build tool.

Pin the Java baseline.

Use BOMs for dependency
families that must evolve together.

Use Version Catalogs
for discoverability and consistency.

Do not override framework versions
without understanding why.

Do not force dependencies
merely until the build turns green.

Inspect the resolved graph.

Control transitive dependencies.

Do not use dynamic production versions.

Do not ship production SNAPSHOTs.

Use approved repositories.

Verify what was downloaded.

Scan direct and transitive components.

Generate an SBOM for the
artifact actually deployed.

Upgrade dependencies continuously.

Do not wait years and
upgrade everything at once.

Create shared libraries only
for stable reusable capabilities.

Do not share bounded-context
domain models through libraries.

Never share JPA entities
as a microservice integration contract.

Keep internal library releases immutable.

Allow consumers to upgrade independently.

And remember:

the dependency graph
is part of the application.

If it changes,
the application changed.
```

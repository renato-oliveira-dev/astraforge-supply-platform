# ADR-049: Adopt AI-Assisted Software Engineering and Responsible AI Development Standards

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-049 |
| Title | Adopt AI-Assisted Software Engineering and Responsible AI Development Standards |
| Status | Accepted |
| Date | 2026-07-24 |
| Decision Owners | AstraForge Supply Platform Architecture Team |
| Technical Area | Generative AI, AI-Assisted Engineering, AI Agents, Responsible AI |
| Related Work Items | AI Coding Assistants, Secure Coding, AI Agents, Code Review, SDLC Governance |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Generative AI is increasingly capable of assisting software engineering activities such as:

```text
Code Generation

Code Review

Refactoring

Test Generation

Documentation

Debugging

SQL Generation

Migration Generation

Architecture Analysis

Security Analysis

Log Analysis

Incident Investigation

Dependency Analysis
```

More advanced AI agents may also perform multi-step engineering activities involving:

```text
Repository Analysis

File Modification

Build Execution

Test Execution

Static Analysis

Pull Request Preparation
```

These capabilities can substantially improve engineering productivity.

However, AI-generated output can also introduce:

- incorrect code
- hallucinated APIs
- security vulnerabilities
- invalid dependencies
- architectural inconsistencies
- licensing/IP concerns
- confidential-data exposure
- destructive database changes
- insufficient tests
- subtle concurrency defects
- incorrect business rules
- excessive automation authority

AI assistance must therefore operate inside the same or stronger engineering controls applied to human-written software.

---

# 2. Problem Statement

The organization requires standards defining:

- approved AI use
- AI coding assistants
- AI-generated code
- human review
- source-code confidentiality
- prompt data
- secrets
- personal data
- intellectual property
- generated dependencies
- generated SQL
- Flyway migrations
- test generation
- secure coding
- SAST
- SonarQube
- hallucination validation
- autonomous agents
- repository modification
- production access
- deployment authority
- model/provider governance
- traceability
- accountability

---

# 3. Decision Drivers

Primary drivers are:

1. engineering productivity
2. software quality
3. security
4. intellectual-property protection
5. confidentiality
6. regulatory compliance
7. human accountability
8. architectural consistency
9. deterministic validation
10. safe automation
11. responsible adoption
12. enterprise governance

---

# 4. Decision

AI-assisted software engineering is permitted when performed through approved tools, models and workflows and when the resulting artifacts remain subject to normal engineering controls.

The canonical model is:

```text
ENGINEER
    |
    v
AI ASSISTANT
    |
    v
PROPOSED CHANGE
    |
    v
HUMAN REVIEW
    |
    v
AUTOMATED VALIDATION
    |
    +--> BUILD
    +--> TESTS
    +--> SONAR
    +--> SAST
    +--> DEPENDENCY SCAN
    |
    v
NORMAL SDLC
```

AI does not bypass the SDLC.

---

# 5. Fundamental Principle

The governing rule is:

```text
AI may accelerate engineering work.

AI does not reduce
engineering accountability.
```

---

# 6. Accountability

Responsibility for a software change remains with the engineer/team accepting and delivering the change.

---

# 7. AI Is Not the Code Owner

This is not an acceptable explanation for a production defect:

```text
"The AI wrote it."
```

The responsible engineering team owns the resulting artifact.

---

# 8. Human-in-the-Loop

Material AI-generated code requires meaningful human review before production integration.

---

# 9. Meaningful Review

Review means understanding:

```text
What changed?

Why was it changed?

Is the algorithm correct?

Are business rules preserved?

Are failure scenarios handled?

Are security implications understood?

Are tests sufficient?
```

---

# 10. Blind Acceptance

Blindly accepting generated code without understanding it is prohibited for production software.

---

# 11. AI Output Is Untrusted Input

Generated output should initially be treated conceptually as:

```text
UNTRUSTED PROPOSAL
```

rather than:

```text
CORRECT IMPLEMENTATION
```

---

# 12. Deterministic Validation

Whenever AI output can be validated deterministically, deterministic tooling should be used.

Examples:

```text
Compiler

Unit Tests

Integration Tests

Contract Tests

SonarQube

SAST

Dependency Scanner

Flyway Validation

Schema Validation
```

---

# 13. Compiler Authority

If AI claims code compiles but the compiler reports otherwise:

```text
COMPILER WINS
```

---

# 14. Test Authority

If generated reasoning conflicts with verified tests representing approved requirements:

```text
REQUIREMENTS + VERIFIED TESTS WIN
```

---

# 15. Documentation Authority

AI explanations must not override authoritative platform documentation.

---

# 16. Source-Code Confidentiality

Source code must be treated according to its enterprise classification.

---

# 17. Approved AI Tools

Confidential corporate source code may only be provided to AI services approved for the corresponding data classification.

---

# 18. Consumer AI Accounts

Confidential corporate source code must not be copied into unapproved consumer AI accounts.

---

# 19. Provider Terms

Before approving an AI provider, applicable enterprise functions should evaluate:

- data retention
- model-training policy
- data location
- access controls
- contractual protections
- security certifications
- incident handling
- deletion capabilities

---

# 20. Prompt Confidentiality

Prompts may themselves contain confidential information.

Prompt handling must therefore follow data-classification requirements.

---

# 21. Secrets in Prompts

Never intentionally provide:

```text
Passwords

API Keys

Private Keys

Access Tokens

Refresh Tokens

Production Credentials

Database Passwords

Secret Environment Variables
```

to AI prompts unless an explicitly approved secure capability requires and permits that specific use.

---

# 22. Secret Redaction

Diagnostic material should have secrets removed before being supplied to AI systems.

---

# 23. Authorization Headers

Authorization headers must be redacted.

Example:

```text
Authorization: Bearer ***
```

---

# 24. Production Logs

Production logs supplied to AI systems must follow:

- data classification
- privacy
- security
- retention
- provider approval

requirements.

---

# 25. Personal Data

Personal data must not be unnecessarily included in prompts.

---

# 26. Data Minimization

Prefer:

```text
Synthetic Example

Redacted Payload

Minimal Reproduction
```

over complete production records.

---

# 27. LGPD

AI-assisted workflows processing personal data remain subject to ADR-046 and applicable LGPD requirements.

---

# 28. Repository Scope

AI tools should receive only the repository/context required for the task where tooling permits scope control.

---

# 29. Least Context

The AI equivalent of least privilege is:

```text
Provide only the context
required to perform the task.
```

---

# 30. Intellectual Property

AI-generated output requires the same intellectual-property awareness as other externally influenced artifacts.

---

# 31. Verbatim External Code

Generated output suspected of reproducing substantial third-party copyrighted implementation must not be incorporated blindly.

---

# 32. License Validation

Third-party code and dependencies suggested by AI require normal license and dependency governance.

---

# 33. AI Cannot Approve a License

A model claiming:

```text
"This library is safe to use commercially."
```

is not sufficient legal/license validation.

---

# 34. Dependency Hallucination

AI may invent libraries, versions, classes or methods.

---

# 35. Dependency Verification

Before adding a generated dependency, verify:

```text
Does it exist?

Is the artifact coordinate correct?

Is the version real?

Is it maintained?

Is it approved?

Does it contain known vulnerabilities?

Is its license acceptable?
```

---

# 36. Package Hallucination Security

Hallucinated package names create supply-chain risk because an attacker could publish the invented package.

---

# 37. No Blind Dependency Installation

Do not automatically install arbitrary packages solely because an AI assistant suggested them.

---

# 38. Dependency Sources

Dependencies must come from approved repositories according to ADR-038.

---

# 39. Version Governance

Generated dependency changes must respect the project's approved version-management strategy.

---

# 40. AI-Generated Java

Generated Java code must follow established project standards.

---

# 41. Java Baseline

For the current platform:

```text
Java 21
```

is the standard baseline unless superseded by an approved ADR.

---

# 42. Spring Boot

Generated Spring code must respect the project's actual Spring Boot version and APIs.

---

# 43. Framework Hallucinations

The model may combine APIs from incompatible framework versions.

These must be validated against the project's actual dependencies.

---

# 44. Existing Architecture First

AI must not introduce a new architectural pattern merely because it is technically possible.

---

# 45. Repository Conventions

Generated changes should follow existing:

- packages
- naming
- exception handling
- DTO patterns
- mapping
- validation
- testing
- logging
- integration conventions

unless the task explicitly changes them.

---

# 46. Clean Code

AI-generated code must satisfy the same maintainability standards as human-written code.

---

# 47. Complexity

AI must not solve a simple problem through unnecessary abstractions.

---

# 48. Generated Overengineering

Watch for:

```text
Unnecessary Factories

Unnecessary Interfaces

Unnecessary Builders

Unnecessary Reflection

Unnecessary Generic Frameworks
```

---

# 49. Duplication

AI should inspect existing reusable abstractions before introducing new ones.

---

# 50. Similar Is Not Identical

AI must not merge domain concepts solely because their current implementation looks similar.

---

# 51. Exception Handling

Generated exception handling must follow project standards.

---

# 52. Swallowed Exception

This is prohibited:

```java
try {
    execute();
} catch (Exception ex) {
    // ignored
}
```

---

# 53. Log or Rethrow

Where applicable, generated exception handling must satisfy the project's error-handling and Sonar standards.

---

# 54. Logging

AI-generated code must not introduce:

```text
System.out.println

printStackTrace

Sensitive Data Logging

Token Logging
```

into production code.

---

# 55. Structured Logging

Generated logs should follow the platform's logging standards.

---

# 56. Sensitive Data

AI must not "improve diagnostics" by logging complete sensitive objects.

---

# 57. Error Sanitization

Security sanitization must occur at appropriate trust boundaries.

---

# 58. Business Data Integrity

AI must not silently alter valid business values to satisfy generic sanitization rules.

Example:

```text
Forge & Field
```

must not be transformed into:

```text
M&amp;M
```

inside persistence/domain processing merely as a generic security workaround.

---

# 59. AI-Generated Tests

AI may generate and improve tests.

---

# 60. Test Review

Generated tests require review for:

- meaningful assertions
- correct behavior
- edge cases
- failure cases
- false positives
- unnecessary mocking

---

# 61. Coverage Gaming

AI must not generate meaningless tests solely to increase JaCoCo coverage.

---

# 62. Bad Coverage Example

A test that merely executes a method without verifying behavior does not provide sufficient value merely because it increases line coverage.

---

# 63. AssertJ

Where AssertJ is the project standard, generated tests should follow established conventions.

---

# 64. Assertion Descriptions

Assertions should use meaningful `.as("...")` descriptions before the relevant assertion/predicate chain where required by project quality rules.

Example:

```java
assertThat(result)
        .as("result should contain the expected order")
        .isEqualTo(expectedOrder);
```

---

# 65. Test Naming

Generated test methods should follow the established project naming convention.

Example:

```java
testCreateOrderShouldReturnCreatedOrder()
```

---

# 66. Deterministic UUIDs

Generated tests should prefer stable test constants rather than unnecessary:

```java
UUID.randomUUID()
```

when deterministic identifiers are appropriate.

---

# 67. Thread.sleep

AI-generated tests should not use:

```java
Thread.sleep(...)
```

as a normal concurrency synchronization mechanism.

---

# 68. Concurrency Tests

Use deterministic synchronization mechanisms.

---

# 69. Mockito

Generated Mockito tests should avoid excessive mocking that merely reproduces implementation internals.

---

# 70. Test Business Behavior

Tests should primarily validate externally meaningful behavior and important internal invariants.

---

# 71. Mutation Resistance

Where appropriate, tests should be strong enough that incorrect business logic would actually fail them.

---

# 72. AI-Generated SQL

Generated SQL requires particular care.

---

# 73. SQL Validation

Review:

```text
Correctness

Execution Plan

Indexes

Cardinality

Locking

Transaction Scope

Null Semantics

Oracle/PostgreSQL Compatibility
```

according to the target database.

---

# 74. Database Dialect

AI must not assume Oracle and PostgreSQL SQL are interchangeable.

---

# 75. Oracle Version

Generated Oracle SQL/PLSQL must respect the actual Oracle version and execution environment.

---

# 76. Oracle Forms Compatibility

PL/SQL called through legacy Oracle Forms must respect Forms/runtime compatibility constraints.

---

# 77. Database Business Logic

AI must not move database business logic into Java—or vice versa—without understanding transactional and architectural consequences.

---

# 78. Flyway Generation

AI may generate Flyway migrations.

---

# 79. Flyway Immutability

The rule is absolute:

```text
APPLIED MIGRATION
       |
       v
   IMMUTABLE
```

AI must never modify an already applied migration to implement a correction.

---

# 80. Corrective Migration

A correction requires:

```text
Existing Migration

        +

New Migration Version
```

---

# 81. Migration Review

Generated migrations require review for:

- backward compatibility
- locking
- runtime
- data volume
- indexes
- constraints
- rollback/recovery implications

---

# 82. Destructive Migration

AI must not autonomously execute destructive schema changes against production.

---

# 83. Data Migration

Generated data migrations require reconciliation according to ADR-047.

---

# 84. AI-Generated Architecture

AI may assist architecture analysis.

---

# 85. Architecture Authority

AI-generated architecture recommendations remain proposals requiring engineering/architecture validation.

---

# 86. Existing ADRs

AI should inspect applicable ADRs before proposing architectural changes when repository context makes them available.

---

# 87. ADR Conflict

A generated change conflicting with an accepted ADR requires either:

```text
CHANGE IMPLEMENTATION
```

or:

```text
FORMALLY REVISIT ADR
```

not silent architectural divergence.

---

# 88. Security Review

AI-generated security-sensitive code requires elevated review.

Examples:

```text
Authentication

Authorization

Cryptography

Token Handling

Input Validation

Deserialization

File Upload

CORS

SQL Construction
```

---

# 89. Cryptography

AI must not invent custom cryptographic algorithms.

---

# 90. Secure Libraries

Use approved cryptographic libraries and enterprise standards.

---

# 91. Authentication

Generated authentication code must follow the approved identity architecture.

---

# 92. Authorization

Generated authorization checks must preserve least privilege.

---

# 93. SQL Injection

Generated database access must use safe parameterization.

---

# 94. Command Injection

Generated OS/process execution requires strict input controls and explicit justification.

---

# 95. SSRF

Generated HTTP integrations must account for SSRF where destinations can be influenced by external input.

---

# 96. Deserialization

Generated polymorphic/deserialization configuration requires security review.

---

# 97. SAST

AI-generated code must pass the same SAST gates as human-written code.

---

# 98. SonarQube

AI-generated code must pass applicable Sonar quality gates.

---

# 99. AI Does Not Override Sonar

Do not suppress a legitimate Sonar finding merely because the AI claims it is a false positive.

---

# 100. False Positive

A finding may be classified as false positive only through the normal engineering/security process.

---

# 101. Suppression

Generated:

```java
@SuppressWarnings(...)
```

must not be used indiscriminately to silence quality/security tooling.

---

# 102. Security Scanner

Security scanners are controls, not obstacles for the AI to circumvent.

---

# 103. AI Code Review

AI may supplement human code review.

---

# 104. AI Review Strengths

AI review may help identify:

- duplication
- missing null handling
- inconsistent naming
- suspicious exception handling
- potential performance issues
- missing tests

---

# 105. AI Review Limitations

AI may miss:

- domain-specific defects
- undocumented requirements
- distributed-system implications
- organizational constraints
- subtle security issues

---

# 106. Human Review Remains

AI review does not replace required human review for material production changes.

---

# 107. Review Evidence

Normal pull-request review evidence remains required.

---

# 108. AI-Generated Documentation

AI may assist documentation generation.

---

# 109. Documentation Verification

Generated documentation must be checked against actual implementation.

---

# 110. Invented Configuration

AI must not document configuration properties that do not exist.

---

# 111. Executable Examples

Generated commands should be tested where practical.

---

# 112. API Documentation

Generated API examples must reflect actual contracts.

---

# 113. Hallucination Management

Hallucination is an expected failure mode that must be controlled.

---

# 114. Hallucination Categories

Common categories include:

```text
Invented Method

Invented Dependency

Invented Configuration

Invented Requirement

Invented Database Column

Invented API Endpoint

Invented Test Behavior
```

---

# 115. Evidence-Based Engineering

When modifying an existing repository, AI should ground changes in actual repository contents rather than assumptions.

---

# 116. Do Not Guess Missing Code

If required code is unavailable, the AI should request or retrieve the relevant source rather than fabricate its structure.

---

# 117. Repository Search

Before changing cross-cutting behavior, relevant usages should be searched.

---

# 118. Impact Analysis

AI-assisted modifications should identify affected:

```text
Callers

Implementations

Tests

Configuration

Database

Contracts

Documentation
```

where applicable.

---

# 119. Build After Modification

Material code changes should be followed by an appropriate build/test cycle when tooling permits.

---

# 120. Fix Iteratively

If compilation fails:

```text
READ ACTUAL ERROR
      |
      v
IDENTIFY ROOT CAUSE
      |
      v
CORRECT
      |
      v
REBUILD
```

---

# 121. Do Not Guess Compile Errors

Compilation diagnostics should drive the correction.

---

# 122. Autonomous Agents

AI agents capable of executing tools require stronger controls than conversational assistants.

---

# 123. Agent Capability Model

Agent authority should be explicitly bounded across:

```text
READ

WRITE

EXECUTE

NETWORK

DEPLOY

PRODUCTION
```

---

# 124. Least Privilege for Agents

Agents should receive the minimum privileges necessary.

---

# 125. Read-Only Agent

Repository-analysis tasks should preferably use read-only access when modifications are unnecessary.

---

# 126. Write Agent

Repository-write access may be granted when the requested workflow requires modification.

---

# 127. Command Execution

Agents may execute approved development commands in isolated development/CI environments.

---

# 128. Production Execution

AI agents must not receive unrestricted production shell/database access as a normal development capability.

---

# 129. Production Changes

Production changes remain governed by normal deployment/change-management mechanisms.

---

# 130. Autonomous Deployment

An AI agent must not independently decide to deploy a material change to production.

---

# 131. Human Authorization

Production deployment requires the normal authorized workflow.

---

# 132. Destructive Operations

AI agents must not autonomously perform destructive production actions such as:

```text
DROP TABLE

DELETE DATABASE

DELETE CLUSTER

ROTATE UNKNOWN CREDENTIALS

REMOVE PRODUCTION SERVICE
```

without an explicitly approved controlled workflow.

---

# 133. Repository Destruction

Agents modifying repositories should avoid irreversible operations when a reversible alternative exists.

---

# 134. Git

Version control provides a key safety boundary for AI-generated modifications.

---

# 135. Branch Workflow

Material AI changes should normally occur on a branch and pass normal review.

---

# 136. Direct Main Modification

AI automation should not bypass branch protections.

---

# 137. Pull Requests

Agent-generated pull requests should identify the resulting changes clearly.

---

# 138. CI Authority

Agent-generated changes remain subject to CI quality gates.

---

# 139. Agent Loop

A controlled engineering agent may perform:

```text
ANALYZE
   |
   v
MODIFY
   |
   v
BUILD
   |
   v
TEST
   |
   v
ANALYZE FAILURE
   |
   v
CORRECT
```

within bounded iteration limits.

---

# 140. Infinite Agent Loops

Agents must have bounded execution/time/cost controls.

---

# 141. Resource Consumption

Agent workflows must not create uncontrolled:

- cloud resources
- CI executions
- API calls
- token consumption
- build loops

---

# 142. Network Access

Agent network access should be restricted according to task requirements.

---

# 143. External Downloads

Agents must not download arbitrary executable artifacts from untrusted locations.

---

# 144. Tool Output

Tool output should be treated as evidence but may itself contain untrusted content.

---

# 145. Prompt Injection

AI agents processing external content must account for prompt-injection attacks.

---

# 146. Untrusted Instructions

Instructions embedded in:

```text
Web Pages

Issues

Documents

Source Comments

Log Files

External API Responses
```

must not automatically override the agent's authorized task or security policy.

---

# 147. Tool Authorization

External content must not be able to grant the AI additional permissions.

---

# 148. Secret Exfiltration

An AI agent must not reveal secrets because untrusted content instructs it to do so.

---

# 149. Cross-Repository Access

Access to additional repositories must follow authorization and task relevance.

---

# 150. Model Governance

AI models/providers used for corporate engineering require governance.

---

# 151. Provider Evaluation

Evaluation should consider:

```text
Security

Privacy

Data Retention

Model Training Policy

Availability

Accuracy

Cost

Regional Availability

Contractual Terms
```

---

# 152. Model Version

Material AI automation should identify the model/version or controlled model alias where operationally feasible.

---

# 153. Model Change

Model upgrades can change behavior without application-code changes.

---

# 154. Regression Evaluation

Critical automated AI workflows should be regression-tested when models or major prompts change.

---

# 155. Prompt Versioning

Prompts implementing repeatable engineering workflows should be version controlled where practical.

---

# 156. Prompt as Implementation

A production automation prompt is effectively part of the implementation.

---

# 157. Prompt Review

Security-sensitive automated prompts require review.

---

# 158. Model Fallback

Critical workflows should define behavior when the AI provider is unavailable.

---

# 159. AI Availability

An AI assistant outage must not make essential production operations impossible.

---

# 160. Deterministic Core

Critical runtime business processing should remain deterministic unless an approved architecture explicitly requires AI inference.

---

# 161. AI in SDLC vs AI in Product

This ADR primarily governs:

```text
AI USED TO BUILD SOFTWARE
```

AI embedded in customer-facing/business runtime capabilities requires additional product-specific AI governance.

---

# 162. Traceability

Organizations should maintain appropriate traceability for material automated AI engineering workflows.

---

# 163. Traceability May Include

```text
Repository

Change

Agent Workflow

Model

Validation Result

Human Approval
```

depending on risk.

---

# 164. Commit Attribution

Normal human/team accountability must remain clear even when AI substantially assists implementation.

---

# 165. AI Disclosure

AI assistance need not create noisy annotations on every source-code line.

---

# 166. Material Automation

Highly autonomous workflows may require explicit metadata according to enterprise policy.

---

# 167. AI Metrics

Useful AI engineering metrics may include:

```text
Cycle Time Reduction

Review Rework

Build Failure Rate

AI Change Acceptance Rate

Security Findings

Escaped Defects

Developer Satisfaction
```

---

# 168. Lines Generated

Number of AI-generated lines is not a useful primary success metric.

---

# 169. Productivity

The objective is not:

```text
MORE GENERATED CODE
```

The objective is:

```text
FASTER DELIVERY
       +
MAINTAINABLE CODE
       +
LOWER RISK
       +
CORRECT BUSINESS BEHAVIOR
```

---

# 170. AI Quality Evaluation

AI-assisted engineering should be evaluated through resulting software quality.

---

# 171. AI Failure Analysis

When an AI-assisted change causes a defect, the postmortem should analyze the engineering control failure rather than blaming the model abstractly.

---

# 172. Example Questions

```text
Was review insufficient?

Was repository context incomplete?

Were tests missing?

Did CI fail to detect the defect?

Was agent authority excessive?

Was an unsafe suggestion accepted blindly?
```

---

# 173. Learning Loop

AI-related incidents should improve:

- prompts
- tests
- guardrails
- templates
- policies
- training

---

# 174. Developer Training

Engineers using AI tools should understand:

```text
Hallucinations

Data Leakage

Prompt Injection

Supply-Chain Risk

Secure Coding

Human Accountability
```

---

# 175. AI Literacy

Effective AI usage requires the ability to challenge generated output.

---

# 176. Senior Engineering Judgment

AI can accelerate implementation but does not replace architectural/domain judgment.

---

# 177. Junior Engineers

AI can assist learning, but generated code should not prevent engineers from understanding foundational concepts.

---

# 178. Knowledge Retention

Teams must avoid becoming unable to maintain systems without AI assistance.

---

# 179. Architecture Knowledge

Critical architectural decisions must remain documented independently of conversational AI history.

---

# 180. AI Conversation Is Not Documentation

Important decisions belong in:

```text
Code

Tests

ADRs

Documentation

Tickets
```

as appropriate.

---

# 181. AI-Assisted Refactoring

AI may perform broad refactoring when supported by adequate tests and repository analysis.

---

# 182. Refactoring Safety

Large AI refactors should be decomposed into reviewable increments.

---

# 183. Atomic Changes

Prefer commits/PRs with coherent intent.

---

# 184. Avoid Unrelated Changes

AI should not opportunistically modify unrelated code unless explicitly requested.

---

# 185. Formatting Noise

Generated changes should minimize unrelated formatting churn.

---

# 186. Diff Quality

A reviewer should be able to understand the change from the diff.

---

# 187. Generated ZIP/Artifacts

When automated tooling creates delivery artifacts, they should contain only the intended changed/new files unless the requested delivery explicitly requires the complete project.

---

# 188. File Structure

Generated artifacts must preserve required repository directory structure.

---

# 189. Validation Before Delivery

Where tooling permits, generated deliverables should be checked for:

```text
Compilation

Tests

Expected Files

Unexpected Files

Secrets

Binary Artifacts
```

---

# 190. AI and Performance

AI may suggest performance optimizations.

---

# 191. Measure First

Performance changes should be supported by evidence.

---

# 192. Premature Parallelism

AI must not introduce concurrency merely because parallelism appears more sophisticated.

---

# 193. Java Virtual Threads

Virtual Threads may be appropriate for I/O-bound workloads under ADR-034.

---

# 194. Parallelism Review

Before introducing parallel execution, review:

- thread safety
- ordering
- transaction context
- security context
- request context
- external-system capacity

---

# 195. Database Parallelism

Parallel calls against a database can worsen performance through contention and connection-pool exhaustion.

---

# 196. External API Parallelism

Parallel integration calls must respect downstream capacity and rate limits.

---

# 197. Benchmark

Performance-sensitive AI-generated changes should be benchmarked where practical.

---

# 198. AI and Clean Architecture

AI should preserve established domain boundaries.

---

# 199. Layer Violation

Generated convenience must not justify:

```text
Controller -> Repository
```

when the established architecture requires domain/service processing.

---

# 200. Domain Logic

Business rules should remain in their appropriate domain/application layer.

---

# 201. DTO Leakage

Generated integration DTOs must not automatically become domain entities.

---

# 202. Persistence Leakage

Database entities should not automatically define external API contracts.

---

# 203. AI and Microservices

AI must not propose creating a new microservice solely to solve a local code-organization issue.

---

# 204. Distributed Complexity

A new service introduces:

```text
Network Failure

Deployment

Monitoring

Security

Data Ownership

Versioning

Operational Cost
```

and requires architectural justification.

---

# 205. AI Engineering Governance Gate

A material AI-assisted production change is not ready until:

```text
[ ] Approved AI tool/provider used

[ ] Source-code classification respected

[ ] No secrets intentionally exposed

[ ] Personal data minimized

[ ] Generated code reviewed by a human

[ ] Business behavior understood

[ ] Repository conventions followed

[ ] Existing architecture respected

[ ] Dependencies verified

[ ] Licenses governed normally

[ ] Compilation succeeds

[ ] Unit tests pass

[ ] Integration tests pass where applicable

[ ] Relevant edge cases tested

[ ] Sonar quality gate passes

[ ] SAST passes

[ ] Dependency scan passes

[ ] Security-sensitive code receives appropriate review

[ ] Database changes reviewed

[ ] Flyway immutability preserved

[ ] Performance implications considered

[ ] Concurrency implications considered

[ ] Logging contains no sensitive data

[ ] Documentation updated where required

[ ] CI/CD protections preserved

[ ] Human accountability is explicit
```

---

# 206. Autonomous Agent Gate

An AI agent capable of modifying engineering assets must additionally satisfy:

```text
[ ] Tool permissions explicitly bounded

[ ] Repository scope bounded

[ ] Network scope bounded

[ ] Production access prohibited or explicitly controlled

[ ] Destructive actions restricted

[ ] Branch protection preserved

[ ] Execution limits configured

[ ] Cost/resource limits configured

[ ] Prompt-injection risk considered

[ ] Secrets inaccessible unless explicitly required

[ ] Audit trail available where required

[ ] Human approval point defined

[ ] Rollback/reversion mechanism available
```

---

# 207. Anti-Patterns

The following are prohibited or strongly discouraged:

- blindly accepting AI-generated production code
- blaming AI for defects instead of owning the change
- pasting corporate secrets into prompts
- sending unnecessary customer PII to AI systems
- using unapproved consumer AI tools for confidential source code
- trusting invented dependencies
- installing packages solely because AI suggested them
- accepting AI-generated licensing claims as authoritative
- generating meaningless tests to increase coverage
- using Thread.sleep in generated concurrency tests
- modifying applied Flyway migrations
- generating destructive database operations and executing them automatically
- ignoring actual compiler errors in favor of AI explanations
- bypassing Sonar/SAST because AI considers a finding harmless
- suppressing warnings indiscriminately
- introducing architectural patterns inconsistent with existing ADRs
- giving development agents unrestricted production access
- allowing agents to bypass branch protections
- autonomous production deployment without approved controls
- uncontrolled agent execution loops
- allowing external prompt injection to expand agent authority
- exposing secrets to satisfy instructions contained in untrusted content
- measuring AI success by lines of generated code
- replacing engineering understanding with AI dependency
- storing critical architecture decisions only in AI conversations
- large unreviewable AI refactors
- unrelated formatting/code churn in generated changes
- adding parallelism without measuring the workload

---

# 208. Positive Consequences

The decision provides:

- higher engineering productivity
- faster repository analysis
- faster test creation
- faster refactoring
- stronger repetitive-quality checks
- controlled AI adoption
- reduced data-leakage risk
- clearer human accountability
- safer agent automation
- stronger supply-chain controls
- consistent Java/Sonar/SAST practices
- better enterprise AI governance

---

# 209. Negative Consequences

The decision introduces:

- provider governance
- AI tool approval
- additional security controls
- human review requirements
- agent permission management
- prompt/model regression testing
- developer training

These costs are accepted because uncontrolled AI automation can amplify mistakes at substantially greater speed and scale than traditional manual development.

---

# 210. Neutral Consequences

The decision also means:

- AI-generated code is not inherently better or worse than human-generated code
- some tasks can be highly automated
- other tasks require substantial human judgment
- model behavior may change over time
- AI tooling will continue evolving
- not every engineering task benefits from AI

---

# 211. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Confidential-code leakage | Critical | Medium | Approved providers |
| Secret leakage | Critical | Medium | Redaction + scanning |
| Hallucinated dependency | High | Medium | Dependency verification |
| Vulnerable generated code | Critical | Medium | Review + SAST |
| Incorrect business logic | High | Medium | Human review + tests |
| Weak generated tests | High | High | Test-quality review |
| Flyway history corruption | Critical | Low/Medium | Migration immutability |
| IP/license issue | High | Low/Medium | License governance |
| Agent destructive action | Critical | Low | Least privilege |
| Prompt injection | High | Medium | Trust-boundary controls |
| Production modification | Critical | Low | Deployment governance |
| Model behavior regression | Medium/High | Medium | Evaluation/versioning |
| Overreliance on AI | High | Medium | Engineering ownership |
| Excessive generated complexity | Medium | High | Clean-code review |
| Cost explosion | Medium | Medium | Agent execution limits |

---

# 212. Implementation Guidance

The following rules are mandatory:

1. AI-generated output is treated as a proposal until validated.
2. Responsibility remains with the engineering team accepting the change.
3. Material production code requires meaningful human review.
4. Only approved AI tools may process confidential corporate source code.
5. Secrets must not be intentionally placed in prompts.
6. Personal data must be minimized according to ADR-046.
7. Generated dependencies must be independently verified.
8. AI-generated code must follow existing repository architecture and conventions.
9. Generated Java must target the project's actual Java/Spring versions.
10. AI-generated tests must validate meaningful behavior.
11. Established AssertJ/Sonar test conventions must be preserved.
12. Generated tests must avoid unnecessary nondeterminism and Thread.sleep.
13. Generated SQL must be reviewed for the actual database dialect and workload.
14. Applied Flyway migrations must never be modified.
15. Database corrections require new migration versions.
16. Security-sensitive generated code requires elevated review.
17. AI-generated code must pass normal Sonar, SAST and dependency gates.
18. AI must not suppress legitimate findings merely to obtain a green build.
19. Existing ADRs remain authoritative over AI recommendations.
20. Repository modifications should be grounded in actual repository contents.
21. Compilation/test errors must drive corrections.
22. Autonomous agents must operate with least privilege.
23. Production access must not be granted by default to engineering agents.
24. AI agents must not independently authorize production deployment.
25. Destructive operations require explicit controlled authorization.
26. Agent loops require execution and resource limits.
27. Prompt-injection threats must be considered when agents consume untrusted content.
28. Model/provider changes require evaluation for critical automated workflows.
29. Important reusable prompts should be versioned where appropriate.
30. AI availability must not become an uncontrolled dependency for essential production operations.
31. AI productivity must be measured through engineering outcomes rather than generated-code volume.
32. Critical decisions must remain captured in durable engineering artifacts.
33. Large AI-assisted refactors should remain reviewable and incremental.
34. AI-generated performance changes require workload-specific justification.
35. AI-generated parallelism must respect downstream capacity and concurrency semantics.

---

# 213. Validation

This ADR will be validated through:

- architecture reviews
- AI-provider security reviews
- source-code classification controls
- secret scanning
- pull-request reviews
- CI
- unit tests
- integration tests
- SonarQube
- SAST
- dependency scanning
- license governance
- Flyway validation
- agent audit logs
- prompt/model regression evaluations
- security exercises
- AI-related incident reviews

---

# 214. Success Criteria

The decision is successful when:

- AI measurably reduces engineering cycle time
- AI-generated changes maintain or improve quality
- confidential data remains protected
- generated dependencies are verified
- AI-assisted tests detect real defects
- Sonar/SAST quality remains strong
- migration history remains safe
- agents operate within explicit permission boundaries
- production deployment governance remains intact
- engineers understand and own generated implementations
- AI-related incidents feed improvements into guardrails
- AI increases engineering capability without weakening architecture governance

---

# 215. Alternatives Rejected

## 215.1 Prohibit AI-Assisted Development

Rejected because responsible AI usage can materially improve engineering productivity.

---

## 215.2 Allow Any AI Tool

Rejected because source-code confidentiality and provider data policies differ substantially.

---

## 215.3 Trust AI Output by Default

Rejected because generative models can produce plausible but incorrect implementation.

---

## 215.4 AI Review Replaces Human Review

Rejected for material production changes because domain and organizational context still require accountable human judgment.

---

## 215.5 Give Agents Full Production Access

Rejected because the blast radius of incorrect autonomous actions is unacceptable.

---

## 215.6 Measure AI Productivity by Generated LOC

Rejected because code volume does not represent software value.

---

# 216. Related Decisions

This ADR is related to:

- ADR-006: Use Flyway for Database Migrations
- ADR-014: Adopt Distributed Observability
- ADR-031: Adopt Database Performance and Data Access Standards
- ADR-034: Adopt Java 21 Concurrency and Parallelism Standards
- ADR-036: Adopt API Design, REST Contract and Compatibility Standards
- ADR-037: Adopt Application Security and Secure Coding Standards
- ADR-038: Adopt Dependency and Software Supply Chain Security Standards
- ADR-039: Adopt CI/CD, Release and Deployment Governance Standards
- ADR-041: Adopt Architecture Governance and Technical Debt Management Standards
- ADR-042: Adopt Architecture Fitness Functions and Automated Governance Standards
- ADR-043: Adopt Service Ownership, Platform Boundaries and Team Topology Standards
- ADR-046: Adopt Data Governance, Privacy, Retention and Lifecycle Standards
- ADR-047: Adopt Legacy Modernization, Strangler Migration and Technical Evolution Standards
- ADR-048: Adopt Engineering Productivity, Developer Experience and InnerSource Standards
- ADR-050: Adopt Enterprise Architecture Baseline and Architecture Governance Operating Model

---

# 217. References

- NIST AI Risk Management Framework
- OWASP Top 10 for Large Language Model Applications
- OWASP Secure Coding Practices
- SLSA
- OpenSSF
- SonarQube
- Java 21 Documentation
- Spring Boot Documentation
- Flyway Documentation
- DORA
- NIST Secure Software Development Framework

---

# 218. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-24 | AstraForge Supply Platform Architecture Team | Approved | Initial AI-assisted engineering and responsible AI development baseline |

---

# 219. Decision Summary

The definitive AI-assisted engineering model is:

```text
                  ENGINEER
                     |
                     v
                AI ASSISTANT
                     |
                     v
               PROPOSED CODE
                     |
                     v
                HUMAN REVIEW
                     |
                     v
           AUTOMATED VALIDATION
                     |
        +------------+------------+
        |            |            |
        v            v            v
      TESTS        SONAR         SAST
        |            |            |
        +------------+------------+
                     |
                     v
                    CI
                     |
                     v
                 PRODUCTION
```

AI output starts as:

```text
UNTRUSTED
PROPOSAL
```

and becomes acceptable through:

```text
UNDERSTANDING
     +
REVIEW
     +
COMPILATION
     +
TESTING
     +
SECURITY ANALYSIS
     +
ARCHITECTURE VALIDATION
```

For confidential information:

```text
SOURCE CODE / LOG / DATA
          |
          v
     CLASSIFY DATA
          |
          v
     MINIMIZE INPUT
          |
          v
      REDACT SECRET
          |
          v
    APPROVED AI TOOL
```

For dependencies:

```text
AI SUGGESTS LIBRARY
         |
         v
      VERIFY
    /    |     \
   v     v      v
EXISTS VERSION SECURITY
         |
         v
       LICENSE
         |
         v
       APPROVE
```

For Flyway:

```text
V20__existing.sql
       |
       v
ALREADY APPLIED
       |
       v
DO NOT MODIFY
       |
       v
V21__correct_behavior.sql
```

For agents:

```text
                  AI AGENT
                     |
                     v
              PERMISSION BOUNDARY
          /          |          \
         v           v           v
       READ        WRITE       EXECUTE
         |           |           |
         +-----------+-----------+
                     |
                     v
                DEV / CI
                     |
                     v
               HUMAN APPROVAL
                     |
                     v
              NORMAL PIPELINE
                     |
                     v
                PRODUCTION
```

not:

```text
AI AGENT
   |
   v
UNRESTRICTED PRODUCTION
   |
   v
AUTONOMOUS DESTRUCTIVE ACTION
```

For prompt injection:

```text
EXTERNAL CONTENT
       |
       v
   UNTRUSTED
       |
       v
CONTENT MAY INFORM TASK

BUT MUST NOT:

       |
       +--> Change security policy
       +--> Grant permissions
       +--> Reveal secrets
       +--> Expand repository scope
       +--> Authorize production action
```

For quality:

```text
AI GENERATED
     |
     v
DOES IT COMPILE?
     |
     v
DO TESTS PASS?
     |
     v
DO TESTS MEAN SOMETHING?
     |
     v
SONAR?
     |
     v
SAST?
     |
     v
ARCHITECTURE?
     |
     v
BUSINESS RULES?
     |
     v
REVIEWED
```

The complete AI engineering equation is:

```text
AI PRODUCTIVITY
       +
HUMAN JUDGMENT
       +
LEAST PRIVILEGE
       +
DATA PROTECTION
       +
DETERMINISTIC VALIDATION
       +
SECURE SDLC
       +
ARCHITECTURE GOVERNANCE
       +
ACCOUNTABILITY
       =
RESPONSIBLE AI-ASSISTED ENGINEERING
```

The governing principle is:

```text
AI should increase the speed
at which engineers can reach
a correct solution.

It must not increase the speed
at which unchecked mistakes
reach production.

The more autonomous the AI,
the stronger its permission
boundaries, validation,
observability and approval
controls must become.
```

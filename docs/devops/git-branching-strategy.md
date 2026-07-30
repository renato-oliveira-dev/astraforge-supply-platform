# Git Branching Strategy

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Git Branching Strategy |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the Git branching strategy adopted by the AstraForge Supply Platform.

It establishes:

- branch model
- branch naming
- merge strategy
- release process
- hotfix workflow
- repository governance
- version tagging

The strategy aims to support Continuous Integration while keeping the repository simple and maintainable.

---

# 2. Branching Model

The repository adopts a Trunk-Based Development approach.

Main branches

```
main

develop (optional)

feature/*

release/*

hotfix/*
```

For smaller teams, `develop` may be omitted.

---

# 3. Main Branch

The `main` branch always represents production-ready code.

Rules

- protected branch
- no direct commits
- pull requests required
- successful pipeline required
- code review required

---

# 4. Develop Branch

When used, `develop` represents the next planned release.

Features are merged into `develop`.

Production releases originate from `release/*`.

---

# 5. Feature Branches

Naming convention

```
feature/order-approval

feature/customer-search

feature/payment-integration

feature/inventory-reservation
```

Rules

- short-lived
- single purpose
- one business capability
- deleted after merge

---

# 6. Bugfix Branches

Naming convention

```
bugfix/order-validation

bugfix/payment-timeout
```

Bugfix branches target the active development line.

---

# 7. Release Branches

Naming

```
release/1.0.0

release/1.1.0
```

Purpose

- stabilization
- regression testing
- documentation updates
- release preparation

Only release-related fixes are allowed.

---

# 8. Hotfix Branches

Naming

```
hotfix/1.0.1

hotfix/payment-failure
```

Hotfixes originate from `main`.

After release they are merged back into:

- main
- develop (if present)

---

# 9. Branch Lifetime

| Branch | Lifetime |
|----------|----------|
| feature | Days |
| bugfix | Days |
| release | Short |
| hotfix | Very Short |
| main | Permanent |

Long-lived feature branches should be avoided.

---

# 10. Pull Requests

Every change requires a Pull Request.

The Pull Request should include:

- purpose
- business context
- architectural impact
- testing evidence
- screenshots (when applicable)

---

# 11. Code Review

Every Pull Request requires review.

Review checklist

- correctness
- readability
- architecture
- performance
- security
- tests
- documentation

---

# 12. Merge Strategy

Preferred strategy

```
Squash Merge
```

Benefits

- cleaner history
- one commit per feature
- easier rollback
- simpler auditing

---

# 13. Commit Messages

The repository follows Conventional Commits.

Examples

```
feat(order): implement approval workflow

fix(payment): prevent duplicate authorization

docs(domain): update context map

test(order): increase approval service coverage

refactor(application): simplify command handler
```

---

# 14. Version Tags

Production releases receive Git tags.

Examples

```
v1.0.0

v1.1.0

v2.0.0
```

Tags are immutable.

---

# 15. Branch Protection

Protected branches require:

- Pull Request
- successful CI
- approved review
- resolved conversations
- signed commits (recommended)

Force push is prohibited.

---

# 16. Merge Requirements

Before merging:

- CI successful
- code review approved
- no merge conflicts
- quality gates passed
- security validation passed

---

# 17. Quality Gates

The pipeline validates:

- compilation
- unit tests
- integration tests
- architecture tests
- code coverage
- SonarQube Quality Gate
- SAST
- dependency scan

Failure blocks the merge.

---

# 18. Conflict Resolution

Merge conflicts should be resolved in the source branch.

History rewriting on shared branches should be avoided.

---

# 19. Repository Governance

Repository administrators define:

- protected branches
- merge permissions
- release permissions
- branch deletion policies

Governance changes require team approval.

---

# 20. Branch Cleanup

Merged branches should be deleted automatically.

Inactive branches should be reviewed periodically.

---

# 21. Emergency Changes

Emergency production fixes use:

```
hotfix/*
```

Normal features must never bypass the review process.

---

# 22. Release Flow

```
Feature

↓

Pull Request

↓

CI Validation

↓

Merge

↓

Release Branch

↓

Production

↓

Tag

↓

Delete Release Branch
```

---

# 23. Architecture Rules

The repository:

- protects production branches
- enforces code review
- validates every change
- keeps branches short-lived
- uses immutable release tags

---

# 24. Decision Summary

The platform adopts:

- Trunk-Based Development
- protected branches
- Conventional Commits
- Pull Requests
- Squash Merge
- automated quality gates
- immutable release tags

---

# 25. Next Documentation Step

Next document

```
docs/devops/release-management.md
```

It will define:

- semantic versioning
- release cadence
- release approval
- rollback strategy
- release checklist
- release notes
- long-term support policy

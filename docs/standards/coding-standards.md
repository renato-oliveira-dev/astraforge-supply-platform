# Coding Standards

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Coding Standards |
| Status | Draft |
| Version | 1.0.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the coding standards adopted by the AstraForge Supply Platform.

These standards promote:

- consistency
- readability
- maintainability
- simplicity
- scalability
- long-term evolution

All source code must follow these guidelines.

---

# 2. Engineering Philosophy

Code should be written for humans first.

Computers execute code.

Engineers maintain it.

The primary goal is readability.

---

# 3. Core Principles

Every implementation should prioritize:

- simplicity
- explicitness
- cohesion
- low coupling
- immutability
- testability

---

# 4. SOLID

All production code should respect SOLID principles whenever applicable.

- Single Responsibility Principle
- Open/Closed Principle
- Liskov Substitution Principle
- Interface Segregation Principle
- Dependency Inversion Principle

---

# 5. Clean Code

Prefer:

- small methods
- expressive names
- one level of abstraction
- early return
- immutable objects

Avoid:

- nested conditionals
- long methods
- duplicated logic
- magic numbers
- unnecessary comments

---

# 6. Method Size

Recommended maximum

```
30 lines
```

Complex methods should be decomposed into private methods.

---

# 7. Class Size

Classes should have one responsibility.

Warning signs

- hundreds of methods
- dozens of dependencies
- unrelated behaviors

Large classes should be split.

---

# 8. Method Naming

Methods should describe behavior.

Examples

Good

```
calculateTotal()

reserveInventory()

approveOrder()

publishEvent()
```

Avoid

```
process()

execute()

run()

handle()

doStuff()
```

---

# 9. Variable Naming

Variables should explain their purpose.

Good

```
approvedOrders

customerId

retryCount

paymentStatus
```

Avoid

```
a

tmp

obj

list1
```

---

# 10. Boolean Naming

Booleans should read naturally.

Good

```
isApproved

hasInventory

canRetry

shouldPublish
```

Avoid

```
approvedFlag

inventoryIndicator
```

---

# 11. Constants

Replace literals with named constants.

Incorrect

```java
if (timeout > 3000)
```

Correct

```java
private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(3);
```

---

# 12. Immutability

Prefer immutable objects.

Use `final` whenever possible.

State changes should be explicit.

---

# 13. Null Handling

Prefer:

- Optional (return values)
- empty collections
- validation

Avoid returning null.

---

# 14. Defensive Programming

Validate external inputs.

Assume external systems may provide:

- invalid values
- malformed payloads
- unexpected states

---

# 15. Exceptions

Exceptions must be meaningful.

Never ignore exceptions.

Never swallow exceptions.

Every caught exception must be:

- handled
- translated
- logged
- rethrown

---

# 16. Logging

Log:

- business milestones
- unexpected failures
- integration errors

Do not log:

- passwords
- tokens
- secrets
- sensitive personal data

---

# 17. Comments

Prefer self-explanatory code.

Comments should explain:

- why

Not:

- what

Outdated comments must be removed.

---

# 18. Duplication

Avoid duplication.

Extract common behavior into:

- reusable methods
- services
- utility classes (when justified)

Copy-and-paste is prohibited.

---

# 19. Conditionals

Prefer:

```java
if (order.isApproved()) {
    ...
}
```

Instead of

```java
if (order.getStatus().equals("APPROVED")) {
    ...
}
```

Encapsulate business rules.

---

# 20. Collections

Prefer streams only when they improve readability.

Simple loops are acceptable.

Readability is more important than functional style.

---

# 21. Dependencies

Depend on abstractions.

Avoid unnecessary framework coupling.

---

# 22. Public APIs

Public methods require:

- stable contracts
- meaningful names
- documented behavior
- predictable exceptions

---

# 23. Refactoring

Refactoring should:

- preserve behavior
- improve readability
- reduce complexity
- increase cohesion

---

# 24. Code Review Checklist

Before submitting code verify:

- readable names
- no duplication
- tests added or updated
- complexity acceptable
- documentation updated
- logging appropriate
- exceptions handled correctly

---

# 25. Architecture Rules

Every implementation should:

- follow Clean Architecture
- preserve domain isolation
- avoid infrastructure leakage
- respect bounded contexts
- maintain high cohesion

---

# 26. Decision Summary

The project prioritizes:

- readability over cleverness
- explicit code over implicit behavior
- maintainability over premature optimization
- simplicity over unnecessary abstraction
- consistency over individual preference

# Value Objects

## Document Information

| Field | Value |
|---|---|
| Project | Enterprise Order Platform |
| Document | Value Objects |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines every Value Object used by the Enterprise Order Platform.

It establishes:

- Identity-free domain objects
- Equality rules
- Validation rules
- Immutability rules
- Business behavior
- Persistence recommendations
- Serialization recommendations
- Mapping boundaries
- Java implementation guidelines

Value Objects are one of the tactical building blocks of Domain-Driven Design.

Unlike Entities, Value Objects are identified only by their values.

---

# 2. Characteristics

Every Value Object must satisfy the following characteristics.

## Identity-free

A Value Object has no business identity.

Example:

```
Money(100 BRL)
```

Another instance with the same values is considered exactly the same.

---

## Immutable

After creation a Value Object never changes.

Instead of:

```
money.setAmount(...)
```

A new instance is created.

Example:

```
Money total =
    subtotal.add(tax);
```

---

## Equality by Value

Two Value Objects are equal when every meaningful property is equal.

Example

```
Money(100, BRL)
==
Money(100, BRL)
```

but

```
Money(100, USD)
!=
Money(100, BRL)
```

---

## Side-effect Free

Operations must never modify existing instances.

Example

```
Money total =
    value.add(other);
```

Both original instances remain unchanged.

---

# 3. General Rules

All Value Objects:

- must be immutable
- must validate constructor arguments
- must reject invalid state
- must not expose setters
- must be serializable when necessary
- should be implemented using Java Records whenever possible
- should avoid framework annotations in the Domain Layer

---

# 4. Identifier Value Objects

The platform uses strongly typed identifiers instead of raw UUIDs.

Instead of

```java
UUID orderId;
UUID customerId;
UUID productId;
```

prefer

```java
OrderId
CustomerId
ProductId
UserId
ReservationId
PaymentId
ShipmentId
```

Benefits:

- compile-time safety
- ubiquitous language
- fewer programming mistakes
- stronger APIs

---

# 5. OrderId

Represents the identity of an Order.

Possible implementation

```java
public record OrderId(UUID value) {

    public OrderId {

        Objects.requireNonNull(value);

    }

}
```

Rules

- immutable
- never null
- globally unique

---

# 6. CustomerId

Represents one customer.

Rules

- immutable
- never null
- globally unique

---

# 7. ProductId

Represents one product.

Rules

- immutable
- never null

---

# 8. UserId

Represents a business actor.

Never use raw String usernames inside aggregates.

---

# 9. CorrelationId

Represents a business flow identifier.

Used by:

- commands
- events
- logs
- integrations

Should remain constant during one business flow.

---

# 10. Money

Money is one of the most important Value Objects.

Money is composed of

```
Money
 ├── amount
 └── currency
```

---

Rules

- amount >= 0
- currency mandatory
- immutable
- BigDecimal only
- explicit rounding
- explicit scale

Never use

```java
double
float
```

---

Possible implementation

```java
public record Money(
        BigDecimal amount,
        Currency currency
) {
}
```

---

Operations

```
add()

subtract()

multiply()

divide()

isZero()

isNegative()

greaterThan()

lessThan()
```

Money operations require identical currencies.

---

# 11. Currency

Represents an ISO-4217 currency.

Example

```
BRL

USD

EUR
```

Avoid arbitrary Strings.

---

# 12. Quantity

Represents requested quantity.

Rules

- > 0
- immutable
- BigDecimal
- configurable precision

---

Operations

```
add()

subtract()

greaterThan()

isZero()
```

---

# 13. Percentage

Represents percentages.

Must explicitly define

```
10

or

0.10
```

Never allow ambiguity.

---

Operations

```
multiply()

apply()

```

---

# 14. CustomerReference

Represents a reference to Customer Context.

Contains only

```
CustomerId
```

Not Customer Entity.

---

# 15. ProductReference

Represents a Product identifier.

Contains only

```
ProductId
```

---

# 16. CustomerSnapshot

Represents historical customer information.

Example

```
CustomerSnapshot

customerId

legalName

tradeName

document

segment

classification

deliveryAddress
```

Snapshots never change after submission.

---

# 17. ProductSnapshot

Represents immutable product information.

Example

```
ProductSnapshot

productId

sku

description

category

unitOfMeasure
```

---

# 18. PricingSnapshot

Represents accepted pricing.

Example

```
subtotal

discount

tax

fees

freight

grandTotal

currency

pricingPolicyVersion
```

---

# 19. SubmissionDetails

Represents

```
submittedBy

submittedAt

correlationId
```

---

# 20. CancellationDetails

Represents

```
cancelledBy

cancelledAt

reason

correlationId
```

---

# 21. Address

Represents delivery address.

Should be immutable.

Contains

```
street

city

postalCode

country

number

complement
```

---

# 22. EmailAddress

Validation

- RFC compliant
- lowercase normalization optional
- immutable

---

# 23. PhoneNumber

Represents a business phone.

Prefer E.164 internally.

---

# 24. PersonName

Represents a business name.

Avoid passing raw Strings everywhere.

---

# 25. TaxIdentifier

Represents

CPF

CNPJ

or equivalent.

Validation belongs here.

---

# 26. Equality

Every Value Object compares values.

Never object identity.

Example

```java
money1.equals(money2)
```

---

# 27. Persistence

JPA recommendations

Prefer

```
@Embeddable
```

or converters.

Never expose JPA entities inside Value Objects.

---

# 28. Serialization

Do not serialize Domain Value Objects directly.

Map to DTOs.

---

# 29. Java Guidelines

Prefer

```java
record
```

instead of mutable classes.

Example

```java
public record CustomerReference(
        CustomerId customerId
) {
}
```

---

# 30. Testing

Every Value Object should have tests covering

- equality

- immutability

- validation

- null rejection

- arithmetic

- serialization

- edge cases

---

# 31. Decision Summary

The platform adopts:

- immutable Value Objects

- Java Records

- equality by value

- constructor validation

- BigDecimal for financial calculations

- strongly typed identifiers

- snapshot Value Objects

- explicit business terminology

---

# 32. Next Documentation Step

Next document

```
docs/domain/entities.md
```

It will define every Entity inside the domain.

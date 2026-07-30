# Originality and Intellectual Property Policy

## Purpose

This repository is a public portfolio and reference-architecture project. All contributions must be independently authored and suitable for public disclosure.

## Fictional Identity

AstraForge Supply Platform, Forge & Field, its actors, organizations, suppliers, facilities, data, identifiers, rules, and workflows are fictional.

The project is not affiliated with, endorsed by, or derived from any employer, client, manufacturer, dealer network, government body, or commercial software product.

## Prohibited Content

Contributors must not add:

- proprietary or employer-owned source code;
- confidential requirements, diagrams, tickets, logs, or documentation;
- production data or realistic copies of production records;
- internal hostnames, URLs, queue names, database names, credentials, tenant identifiers, or account numbers;
- private API contracts or payloads copied from a commercial system;
- company-specific business terminology, approval matrices, pricing logic, or integration behavior;
- decompiled, reverse-engineered, or license-incompatible material.

## Permitted Knowledge

General engineering knowledge and public patterns may be used, including:

- Java and Spring idioms;
- DDD, Clean Architecture, and hexagonal architecture;
- REST, OpenAPI, SQS, transactional outbox, idempotency, and saga patterns;
- publicly documented security, testing, reliability, and observability practices;
- original examples created specifically for this repository.

## Clean-Room Contribution Rule

A contribution must be expressible from public knowledge and the fictional AstraForge requirements without consulting proprietary source code during implementation.

When a concept resembles prior professional experience, contributors must redesign names, contracts, examples, state models, data, and implementation details from first principles.

## Review Checklist

Before committing, confirm:

- the content was independently written;
- no confidential source was copied or paraphrased too closely;
- identifiers and examples are fictional;
- no private endpoint, credential, production log, or customer data is present;
- dependencies and borrowed snippets have compatible licenses and attribution where required;
- `.\gradlew.bat validateOriginality` passes.

## Incident Handling

Potentially protected content must be removed from the current tree and Git history as appropriate. Credentials require immediate revocation and rotation; deleting the text from a later commit is not sufficient.

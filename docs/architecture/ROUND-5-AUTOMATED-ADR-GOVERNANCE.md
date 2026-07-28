# Round 5 — Automated ADR Governance

## Purpose

This round closes the ADR-001 through ADR-090 audit by converting the most important structural documentation rules into an executable CI check.

## Added Files

- `scripts/validate_adr_governance.py`
- `.github/workflows/adr-governance.yml`
- `docs/architecture/ROUND-5-AUTOMATED-ADR-GOVERNANCE.md`

## Enforced Rules

The validator checks that:

- the baseline contains every ADR from ADR-001 through ADR-090;
- no ADR number is duplicated;
- ADR filenames follow `ADR-NNN-lower-kebab-case.md`;
- mandatory metadata fields are present;
- the ADR number in the filename, metadata and H1 heading is consistent;
- the metadata title matches the H1 title;
- status values belong to the approved status set;
- every `Supersedes` and `Superseded By` target exists;
- supersession relationships are reciprocal;
- a superseded ADR identifies its successor;
- an accepted ADR is not simultaneously marked as superseded;
- supersession chains do not contain cycles;
- textual `ADR-NNN` references anywhere in repository Markdown point to an existing ADR;
- local Markdown links point to existing repository files.

## Local Execution

From the repository root:

```bash
python scripts/validate_adr_governance.py --root . --first 1 --last 90
```

A successful validation exits with code `0` and prints:

```text
RESULT: PASSED
```

Any governance violation exits with a non-zero code and identifies the affected file/rule.

## CI Integration

The GitHub Actions workflow runs on documentation-related pull requests, pushes to `main` or `master`, and manual execution.

The validator intentionally uses only the Python standard library so the governance gate has no third-party runtime dependency.

## Baseline Policy

ADR-090 remains the final ADR in the audited baseline. Adding any ADR after ADR-090 is a deliberate architecture-governance action and requires updating the configured validation range in the same pull request.

This prevents a new ADR from appearing outside the governed sequence without an explicit baseline update.

## Outcome

The original manual audit is now backed by an executable architecture fitness function. Structural ADR drift can therefore be detected during code review instead of during a later documentation audit.

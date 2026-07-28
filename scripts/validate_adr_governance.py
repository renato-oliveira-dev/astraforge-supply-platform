#!/usr/bin/env python3
"""Validate ADR governance for the Enterprise Order Platform.

The validator intentionally uses only the Python standard library so it can run
locally and in CI without installing project-specific dependencies.
"""

from __future__ import annotations

import argparse
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

ADR_FILENAME = re.compile(r"^ADR-(?P<number>\d{3})-(?P<slug>[a-z0-9]+(?:-[a-z0-9]+)*)\.md$")
ADR_REFERENCE = re.compile(r"\bADR-(\d{3})\b")
HEADING = re.compile(r"^#\s+ADR-(\d{3}):\s+(.+?)\s*$", re.MULTILINE)
METADATA_ROW = re.compile(r"^\|\s*([^|]+?)\s*\|\s*([^|]+?)\s*\|\s*$", re.MULTILINE)
MARKDOWN_LINK = re.compile(r"(?<!!)\[[^\]]+\]\(([^)]+)\)")

MANDATORY_FIELDS = (
    "ADR",
    "Title",
    "Status",
    "Date",
    "Decision Owners",
    "Technical Area",
    "Supersedes",
    "Superseded By",
)

ALLOWED_STATUSES = {
    "Proposed",
    "Accepted",
    "Superseded",
    "Deprecated",
    "Rejected",
}

NONE_VALUES = {"", "None", "N/A", "-"}


@dataclass(frozen=True)
class Adr:
    number: int
    code: str
    path: Path
    title: str
    status: str
    supersedes: tuple[int, ...]
    superseded_by: tuple[int, ...]
    text: str


class ValidationResult:
    def __init__(self) -> None:
        self.errors: list[str] = []
        self.warnings: list[str] = []

    def error(self, message: str) -> None:
        self.errors.append(message)

    def warn(self, message: str) -> None:
        self.warnings.append(message)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Validate ADR governance and local documentation references.")
    parser.add_argument("--root", type=Path, default=Path.cwd(), help="Repository root (default: current directory).")
    parser.add_argument("--first", type=int, default=1, help="First required ADR number (default: 1).")
    parser.add_argument("--last", type=int, default=90, help="Last required ADR number (default: 90).")
    parser.add_argument(
        "--skip-markdown-links",
        action="store_true",
        help="Skip validation of local Markdown links outside ADR metadata.",
    )
    return parser.parse_args()


def normalize_field_name(value: str) -> str:
    return " ".join(value.strip().split())


def parse_metadata(text: str) -> dict[str, str]:
    metadata: dict[str, str] = {}
    for key, value in METADATA_ROW.findall(text):
        normalized_key = normalize_field_name(key)
        if normalized_key in {"Field", "---"}:
            continue
        metadata[normalized_key] = value.strip()
    return metadata


def parse_adr_refs(value: str, result: ValidationResult, context: str) -> tuple[int, ...]:
    if value.strip() in NONE_VALUES:
        return ()

    refs = tuple(int(item) for item in ADR_REFERENCE.findall(value))
    residue = ADR_REFERENCE.sub("", value)
    residue = residue.replace(",", "").replace(";", "").replace(" ", "")
    if residue:
        result.error(f"{context}: invalid ADR reference list value '{value}'.")
    if not refs:
        result.error(f"{context}: expected ADR-NNN references or 'None', found '{value}'.")
    return refs


def load_adrs(adr_dir: Path, result: ValidationResult) -> dict[int, Adr]:
    adrs: dict[int, Adr] = {}
    candidates = sorted(p for p in adr_dir.iterdir() if p.is_file() and p.name.startswith("ADR-") and p.suffix == ".md")

    for path in candidates:
        match = ADR_FILENAME.fullmatch(path.name)
        if not match:
            result.error(
                f"{path.relative_to(adr_dir.parent.parent.parent)}: invalid ADR filename; expected "
                "ADR-NNN-lower-kebab-case.md."
            )
            continue

        number = int(match.group("number"))
        code = f"ADR-{number:03d}"
        text = path.read_text(encoding="utf-8")
        metadata = parse_metadata(text)

        for field in MANDATORY_FIELDS:
            if not metadata.get(field, "").strip():
                result.error(f"{path.name}: missing mandatory metadata field '{field}'.")

        metadata_code = metadata.get("ADR", "")
        if metadata_code and metadata_code != code:
            result.error(f"{path.name}: metadata ADR is '{metadata_code}', expected '{code}'.")

        heading = HEADING.search(text)
        if not heading:
            result.error(f"{path.name}: missing canonical '# {code}: <Title>' heading.")
            heading_title = metadata.get("Title", "")
        else:
            heading_number = int(heading.group(1))
            heading_title = heading.group(2).strip()
            if heading_number != number:
                result.error(f"{path.name}: heading ADR-{heading_number:03d} does not match filename {code}.")

        title = metadata.get("Title", "").strip()
        if title and heading_title and title != heading_title:
            result.error(
                f"{path.name}: metadata Title '{title}' does not match heading title '{heading_title}'."
            )

        status = metadata.get("Status", "").strip()
        if status and status not in ALLOWED_STATUSES:
            result.error(
                f"{path.name}: unsupported Status '{status}'. Allowed: {', '.join(sorted(ALLOWED_STATUSES))}."
            )

        supersedes = parse_adr_refs(metadata.get("Supersedes", ""), result, f"{path.name} Supersedes")
        superseded_by = parse_adr_refs(metadata.get("Superseded By", ""), result, f"{path.name} Superseded By")

        if number in adrs:
            result.error(f"Duplicate ADR number {code}: '{adrs[number].path.name}' and '{path.name}'.")
            continue

        adrs[number] = Adr(
            number=number,
            code=code,
            path=path,
            title=title or heading_title,
            status=status,
            supersedes=supersedes,
            superseded_by=superseded_by,
            text=text,
        )

    return adrs


def validate_sequence(adrs: dict[int, Adr], first: int, last: int, result: ValidationResult) -> None:
    expected = set(range(first, last + 1))
    actual = set(adrs)
    missing = sorted(expected - actual)
    unexpected = sorted(actual - expected)

    if missing:
        result.error("Missing ADR numbers: " + ", ".join(f"ADR-{n:03d}" for n in missing) + ".")
    if unexpected:
        result.error(
            "ADR numbers outside configured range: " + ", ".join(f"ADR-{n:03d}" for n in unexpected) + "."
        )


def validate_supersession(adrs: dict[int, Adr], result: ValidationResult) -> None:
    for adr in adrs.values():
        if adr.number in adr.supersedes or adr.number in adr.superseded_by:
            result.error(f"{adr.code}: an ADR cannot supersede or be superseded by itself.")

        if adr.status == "Superseded" and not adr.superseded_by:
            result.error(f"{adr.code}: Status is Superseded but 'Superseded By' is None.")
        if adr.status == "Accepted" and adr.superseded_by:
            result.error(f"{adr.code}: Status is Accepted but 'Superseded By' is populated.")
        if adr.status != "Superseded" and adr.superseded_by:
            result.warn(
                f"{adr.code}: 'Superseded By' is populated while Status is '{adr.status}'. Review status semantics."
            )

        for target_number in adr.supersedes:
            target = adrs.get(target_number)
            if not target:
                result.error(f"{adr.code}: Supersedes references missing ADR-{target_number:03d}.")
                continue
            if adr.number not in target.superseded_by:
                result.error(
                    f"{adr.code}: supersedes {target.code}, but {target.code} does not reciprocally list "
                    f"{adr.code} in 'Superseded By'."
                )

        for target_number in adr.superseded_by:
            target = adrs.get(target_number)
            if not target:
                result.error(f"{adr.code}: Superseded By references missing ADR-{target_number:03d}.")
                continue
            if adr.number not in target.supersedes:
                result.error(
                    f"{adr.code}: is superseded by {target.code}, but {target.code} does not reciprocally list "
                    f"{adr.code} in 'Supersedes'."
                )

    # Detect cycles in the directed relation old -> new.
    graph = {n: set(adr.superseded_by) for n, adr in adrs.items()}
    visiting: set[int] = set()
    visited: set[int] = set()

    def visit(node: int, stack: list[int]) -> None:
        if node in visiting:
            cycle_start = stack.index(node)
            cycle = stack[cycle_start:] + [node]
            result.error("Supersession cycle detected: " + " -> ".join(f"ADR-{n:03d}" for n in cycle) + ".")
            return
        if node in visited:
            return
        visiting.add(node)
        stack.append(node)
        for nxt in graph.get(node, ()):  # referenced nodes were checked above
            if nxt in graph:
                visit(nxt, stack)
        stack.pop()
        visiting.remove(node)
        visited.add(node)

    for number in sorted(graph):
        visit(number, [])


def iter_markdown_files(root: Path) -> Iterable[Path]:
    for path in root.rglob("*.md"):
        if ".git" not in path.parts:
            yield path


def validate_active_messaging_baseline(adrs: dict[int, Adr], result: ValidationResult) -> None:
    """Prevent superseded Kafka-specific guidance from becoming normative again.

    ADR-090 intentionally mentions Kafka to document the superseded baseline. Other
    Accepted ADRs must use the current SQS baseline or technology-neutral messaging
    terminology. Historical Superseded ADRs remain untouched as architectural records.
    """
    canonical_messaging_adr = 90
    for adr in adrs.values():
        if adr.status != "Accepted" or adr.number == canonical_messaging_adr:
            continue
        if re.search(r"\bKafka\b", adr.text, re.IGNORECASE):
            result.error(
                f"{adr.code}: Accepted ADR contains Kafka-specific guidance; "
                "use the ADR-090 SQS baseline or technology-neutral messaging terminology."
            )


def validate_adr_references(root: Path, adrs: dict[int, Adr], result: ValidationResult) -> None:
    known = set(adrs)
    for path in iter_markdown_files(root):
        text = path.read_text(encoding="utf-8")
        missing = sorted({int(ref) for ref in ADR_REFERENCE.findall(text)} - known)
        for number in missing:
            result.error(
                f"{path.relative_to(root)}: references missing ADR-{number:03d}."
            )


def strip_link_destination(destination: str) -> str:
    value = destination.strip()
    if value.startswith("<") and value.endswith(">"):
        value = value[1:-1].strip()
    # Optional Markdown link title: path "title". The repository currently uses simple links,
    # so only strip the common whitespace-title form when it is clearly present.
    if ' "' in value:
        value = value.split(' "', 1)[0]
    return value


def validate_local_markdown_links(root: Path, result: ValidationResult) -> None:
    for path in iter_markdown_files(root):
        text = path.read_text(encoding="utf-8")
        for raw_destination in MARKDOWN_LINK.findall(text):
            destination = strip_link_destination(raw_destination)
            if not destination or destination.startswith(("http://", "https://", "mailto:", "#")):
                continue
            target_text = destination.split("#", 1)[0]
            if not target_text:
                continue
            # Ignore URI schemes and templates that are not local files.
            if re.match(r"^[A-Za-z][A-Za-z0-9+.-]*:", target_text):
                continue
            target = (path.parent / target_text).resolve()
            try:
                target.relative_to(root.resolve())
            except ValueError:
                result.error(
                    f"{path.relative_to(root)}: local Markdown link escapes repository root: '{destination}'."
                )
                continue
            if not target.exists():
                result.error(
                    f"{path.relative_to(root)}: broken local Markdown link '{destination}'."
                )


def print_report(adrs: dict[int, Adr], result: ValidationResult, first: int, last: int) -> int:
    accepted = sum(adr.status == "Accepted" for adr in adrs.values())
    superseded = sum(adr.status == "Superseded" for adr in adrs.values())

    print("ADR Governance Validation")
    print("=========================")
    print(f"Configured ADR range : ADR-{first:03d}..ADR-{last:03d}")
    print(f"ADR files discovered : {len(adrs)}")
    print(f"Accepted             : {accepted}")
    print(f"Superseded           : {superseded}")
    print(f"Warnings             : {len(result.warnings)}")
    print(f"Errors               : {len(result.errors)}")

    if result.warnings:
        print("\nWarnings:")
        for warning in sorted(set(result.warnings)):
            print(f"  WARN: {warning}")

    if result.errors:
        print("\nErrors:")
        for error in sorted(set(result.errors)):
            print(f"  ERROR: {error}")
        print("\nRESULT: FAILED")
        return 1

    print("\nRESULT: PASSED")
    return 0


def main() -> int:
    args = parse_args()
    root = args.root.resolve()
    adr_dir = root / "docs" / "architecture" / "decisions"
    result = ValidationResult()

    if not adr_dir.is_dir():
        print(f"ERROR: ADR directory does not exist: {adr_dir}", file=sys.stderr)
        return 2

    adrs = load_adrs(adr_dir, result)
    validate_sequence(adrs, args.first, args.last, result)
    validate_supersession(adrs, result)
    validate_active_messaging_baseline(adrs, result)
    validate_adr_references(root, adrs, result)
    if not args.skip_markdown_links:
        validate_local_markdown_links(root, result)

    return print_report(adrs, result, args.first, args.last)


if __name__ == "__main__":
    raise SystemExit(main())

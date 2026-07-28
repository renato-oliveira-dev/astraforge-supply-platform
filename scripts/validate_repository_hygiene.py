#!/usr/bin/env python3
"""Validate basic repository hygiene rules."""

from __future__ import annotations

import argparse
from pathlib import Path
import sys

IGNORED_DIRS = {
    ".git", ".gradle", ".idea", ".vscode", "build", "target",
    "out", "node_modules", ".scannerwork", ".sonar",
}

FORBIDDEN_DIRS = {
    ".idea", ".vscode", "node_modules", ".scannerwork", ".sonar",
}

FORBIDDEN_SUFFIXES = {
    ".class", ".tmp", ".bak", ".swp", ".log",
}

FORBIDDEN_NAMES = {
    ".DS_Store", "Thumbs.db",
}

TEXT_SUFFIXES = {
    ".java", ".kt", ".kts", ".gradle", ".groovy", ".xml", ".yml", ".yaml",
    ".json", ".md", ".sql", ".sh", ".properties", ".txt", ".csv",
    ".gitignore", ".gitattributes",
}

REQUIRED_FILES = {
    ".gitignore",
    ".gitattributes",
    "scripts/validate_adr_governance.py",
    "scripts/validate_repository_hygiene.py",
    ".github/workflows/adr-governance.yml",
}


def is_ignored(path: Path, root: Path) -> bool:
    try:
        relative = path.relative_to(root)
    except ValueError:
        return False
    return any(part in IGNORED_DIRS for part in relative.parts[:-1])


def is_text_candidate(path: Path) -> bool:
    return path.suffix.lower() in TEXT_SUFFIXES or path.name in {
        ".gitignore", ".gitattributes"
    }


def validate(root: Path) -> list[str]:
    errors: list[str] = []

    for required in sorted(REQUIRED_FILES):
        if not (root / required).is_file():
            errors.append(f"Required repository file is missing: {required}")

    for path in root.rglob("*"):
        if path == root:
            continue

        relative = path.relative_to(root)

        if any(part in FORBIDDEN_DIRS for part in relative.parts):
            errors.append(f"Forbidden generated/editor directory found: {relative}")
            continue

        if is_ignored(path, root):
            continue

        if path.is_file():
            if path.name in FORBIDDEN_NAMES:
                errors.append(f"Forbidden OS metadata file found: {relative}")

            if path.suffix.lower() in FORBIDDEN_SUFFIXES:
                errors.append(f"Forbidden generated/temporary file found: {relative}")

            if path.name.startswith("ROUND-") and path.suffix.lower() == ".md":
                errors.append(f"Temporary round report must not be versioned: {relative}")

            if is_text_candidate(path):
                data = path.read_bytes()
                if b"\r\n" in data:
                    errors.append(f"CRLF detected in LF-governed text file: {relative}")
                if b"\x00" in data:
                    errors.append(f"NUL byte detected in text file: {relative}")

    return sorted(set(errors))


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate repository hygiene.")
    parser.add_argument("--root", default=".", help="Repository root.")
    args = parser.parse_args()

    root = Path(args.root).resolve()
    errors = validate(root)

    print("Repository Hygiene Validation")
    print("=============================")
    print(f"Repository root : {root}")
    print(f"Errors          : {len(errors)}")

    if errors:
        print()
        for error in errors:
            print(f"ERROR: {error}")
        print()
        print("RESULT: FAILED")
        return 1

    print("RESULT: PASSED")
    return 0


if __name__ == "__main__":
    sys.exit(main())

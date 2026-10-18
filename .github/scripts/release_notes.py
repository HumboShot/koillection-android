"""Writes the Google Play "What's new" text from the titles of the commits in a push.

Usage: python release_notes.py <commit before the push, or empty> <output file>

Google Play allows 500 characters. When the list is longer, it ends with "…and more".
"""
import subprocess
import sys

LIMIT = 500
MORE = "…and more"


def commit_titles(before):
    known = before and subprocess.run(
        ["git", "cat-file", "-e", f"{before}^{{commit}}"], capture_output=True
    ).returncode == 0
    # Without a known start (first push, manual run), use only the newest commit.
    selection = [f"{before}..HEAD"] if known else ["-1"]
    out = subprocess.run(
        ["git", "log", "--no-merges", "--format=%s", *selection],
        capture_output=True, text=True, check=True,
    ).stdout
    return [line.strip() for line in out.splitlines() if line.strip()]


def notes(titles):
    if not titles:
        return "Small improvements."
    lines = []
    for title in titles:
        line = f"• {title}"
        if len("\n".join(lines + [line, MORE])) > LIMIT:
            lines.append(MORE)
            break
        lines.append(line)
    return "\n".join(lines)


def main():
    before, path = sys.argv[1], sys.argv[2]
    text = notes(commit_titles(before))
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)
    print(text)


if __name__ == "__main__":
    main()

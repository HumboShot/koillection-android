"""Writes the Google Play "What's new" text from the titles of the commits in a push.

Usage: python release_notes.py <commit before the push, or empty> <output file>

Commits whose title starts with a tag in square brackets, such as "[ci] ..." or "[docs] ...",
are internal and are left out. Google Play allows 500 characters. When the list is longer,
it ends with "…and more".
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
    titles = [line.strip() for line in out.splitlines() if line.strip()]
    return [title for title in titles if not is_internal(title)]


def is_internal(title):
    return title.startswith("[") and "]" in title


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

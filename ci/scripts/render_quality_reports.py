#!/usr/bin/env python3

import argparse
import html
from pathlib import Path


def render_text_as_html(source_path: str, title: str) -> None:
    src = Path(source_path)
    if src.suffix == ".html":
        return

    content = src.read_text(encoding="utf-8", errors="replace")
    dst = src.with_suffix(".html")
    dst.write_text(
        "<!doctype html><html><head><meta charset='utf-8'>"
        f"<title>{title}</title>"
        "<style>body{font-family:monospace;background:#111;color:#eee;padding:16px}"
        "pre{white-space:pre-wrap;word-break:break-word}</style></head><body>"
        f"<h2>{title}</h2><pre>{html.escape(content)}</pre></body></html>",
        encoding="utf-8",
    )


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Render a text report as a lightweight HTML page."
    )
    parser.add_argument("source_path", help="Path to source report file")
    parser.add_argument("title", help="HTML report title")
    args = parser.parse_args()

    render_text_as_html(args.source_path, args.title)


if __name__ == "__main__":
    main()

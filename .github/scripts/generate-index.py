#!/usr/bin/env python3

import json
import os
import re
from pathlib import Path


def parse_apk_info(apk_path):
    """Extract extension info from APK manifest."""
    info = {
        "name": "Tachiyomi: Dokusho",
        "pkg": "eu.kanade.tachiyomi.extension.all.dokusho",
        "apk": os.path.basename(apk_path),
        "lang": "all",
        "code": 1,
        "version": "1.4.1",
        "nsfw": 0,
        "sources": [
            {
                "name": "Dokusho",
                "lang": "all",
                "id": "8524619729907384860",
                "baseUrl": "",
                "versionId": 1,
            }
        ],
    }

    # Try to extract version from filename (handles -release suffix)
    match = re.search(r"-v(\d+\.\d+\.\d+)(?:-release)?\.apk$", apk_path)
    if match:
        info["version"] = match.group(1)
        # Extract version code from version name
        parts = info["version"].split(".")
        if len(parts) >= 3:
            info["code"] = int(parts[2])

    return info


def main():
    repo_dir = Path("repo")
    apk_dir = repo_dir / "apk"

    extensions = []

    for apk_file in apk_dir.glob("*.apk"):
        info = parse_apk_info(str(apk_file))
        extensions.append(info)

    # Write index.json
    with open(repo_dir / "index.json", "w") as f:
        json.dump(extensions, f, indent=2)

    # Write minified version
    with open(repo_dir / "index.min.json", "w") as f:
        json.dump(extensions, f, separators=(",", ":"))

    # Write repo.json
    repo_meta = {
        "meta": {
            "name": "Dokusho Extensions",
            "website": "https://github.com/dokushohq/extensions",
        }
    }
    with open(repo_dir / "repo.json", "w") as f:
        json.dump(repo_meta, f, indent=2)

    # Generate simple index.html
    html = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dokusho Extensions</title>
    <style>
        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
        h1 { color: #333; }
        .extension { background: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0; }
        .extension h2 { margin-top: 0; }
        code { background: #e0e0e0; padding: 2px 6px; border-radius: 4px; }
        a { color: #0066cc; }
    </style>
</head>
<body>
    <h1>Dokusho Extensions</h1>
    <p>Tachiyomi/Mihon extension for Dokusho - a self-hosted manga server.</p>

    <h2>Installation</h2>
    <p>Add this repository to Tachiyomi/Mihon:</p>
    <code>https://dokushohq.github.io/extensions</code>

    <div class="extension">
        <h2>Dokusho</h2>
        <p>Connect to your Dokusho server to read manga from your personal library.</p>
        <p><strong>Requirements:</strong></p>
        <ul>
            <li>A running Dokusho server</li>
            <li>An API key from your Dokusho dashboard</li>
        </ul>
    </div>

    <h2>Configuration</h2>
    <ol>
        <li>Install the extension from the repository</li>
        <li>Go to extension settings</li>
        <li>Enter your Dokusho server URL</li>
        <li>Enter your API key</li>
    </ol>

    <p><a href="https://github.com/dokushohq/extensions">Source Code</a></p>
</body>
</html>
"""
    with open(repo_dir / "index.html", "w") as f:
        f.write(html)

    print(f"Generated index with {len(extensions)} extension(s)")


if __name__ == "__main__":
    main()

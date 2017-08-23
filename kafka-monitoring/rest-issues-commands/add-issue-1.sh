#!/usr/bin/env bash
curl -XPOST localhost:8901/commands/issues \
     -H "Content-Type: application/json" \
     -d '{"type": "BUG", "title": "Bug 1", "description": "..."}'
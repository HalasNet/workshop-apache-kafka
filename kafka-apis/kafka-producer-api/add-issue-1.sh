#!/usr/bin/env bash
curl -XPOST localhost:8801/issues \
     -H "Content-Type: application/json" \
     -d '{"id": "10", "type": "BUG", "title": "Bug 1", "description": "..."}'
#!/usr/bin/env bash
curl -XPOST localhost:8801/issues \
     -H "Content-Type: application/json" \
     -d '{"id": "1000", "type": "IMPROVEMENT", "title": "Improvement 3", "description": "..."}'
#!/usr/bin/env bash

curl -XPOST localhost:8801/issues/_tx \
     -H "Content-Type: application/json" \
     -d '{"id": 3, "type": "BUG", "title": "Bug 1", "description": "..."}'
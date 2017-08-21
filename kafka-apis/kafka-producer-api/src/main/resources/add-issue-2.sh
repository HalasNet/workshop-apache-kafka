#!/usr/bin/env bash
curl -XPOST localhost:8801/issues \
     -H "Content-Type: application/json" \
     -d '{"id": "2", "type": "TASK", "title": "Task 2", "description": "..."}'
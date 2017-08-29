#!/usr/bin/env bash
HOST=workshop-apache-kafka.westeurope.cloudapp.azure.com
# HOST=localhost

curl -XPOST ${HOST}:8801/issues/_tx_uncommitted \
     -H "Content-Type: application/json" \
     -d '{"id": 3, "type": "BUG", "title": "Bug 1", "description": "..."}'
#!/usr/bin/env bash

PROMETHEUS_HOME=/opt/prometheus/prometheus-1.7.1.linux-amd64

cat <<'EOF' > ${PROMETHEUS_HOME}/prometheus-kafka.yml
global:
 scrape_interval: 10s
 evaluation_interval: 10s
scrape_configs:
 - job_name: 'kafka'
   static_configs:
    - targets:
      - localhost:5555
EOF
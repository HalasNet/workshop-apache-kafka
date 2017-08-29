#!/usr/bin/env bash

PROMETHEUS_VERSION=1.7.1

wget https://github.com/prometheus/prometheus/releases/download/v${PROMETHEUS_VERSION}/prometheus-${PROMETHEUS_VERSION}.linux-amd64.tar.gz

tar -xzf prometheus-*.tar.gz

rm prometheus-*.tar.gz*

cp prometheus.yml prometheus-${PROMETHEUS_VERSION}.linux-amd64/.

wget https://s3-us-west-2.amazonaws.com/grafana-releases/release/grafana-4.4.3.linux-x64.tar.gz

tar -zxvf grafana-4.4.3.linux-x64.tar.gz

rm grafana-4.4.3.linux-x64.tar.gz
#!/usr/bin/env bash

KAFKA_HOME=/data/opt/apache/kafka/kafka_2.11-0.11.0.0
cd ${KAFKA_HOME}
wget https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.10/jmx_prometheus_javaagent-0.10.jar
wget https://raw.githubusercontent.com/prometheus/jmx_exporter/master/example_configs/kafka-0-8-2.yml

echo "KAFKA_OPTS=\"\${KAFKA_OPTS} -javaagent:./jmx_prometheus_javaagent-0.10.jar=5555:./kafka-0-8-2.yml\" ./bin/kafka-server-start.sh config/server.properties" > kafka-start-with-agent.sh

chmod +x kafka-start-with-agent.sh
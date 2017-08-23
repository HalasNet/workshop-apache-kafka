#!/usr/bin/env bash
KAFKA_HOME=/data/opt/apache/kafka/kafka_2.11-0.11.0.0

cp ./kafka-connect-twitter-0.1-jar-with-dependencies.jar ${KAFKA_HOME}/libs/.
${KAFKA_HOME}/bin/connect-standalone.sh connect-standalone.properties twitter-source-transform.properties
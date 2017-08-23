#!/usr/bin/env bash
bin/kafka-topics.sh --zookeeper localhost:2181 \
                    --create \
                    --partitions 3 \
                    --replication-factor 3 \
                    --topic issue-events-03

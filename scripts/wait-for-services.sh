#!/usr/bin/env bash
set -euo pipefail

echo "Waiting for services..."

# Wait for TCP port helper
wait_for_port() {
  local host=$1
  local port=$2
  local retries=60
  local wait=1
  for i in $(seq 1 $retries); do
    if nc -z "$host" "$port" >/dev/null 2>&1; then
      echo "$host:$port is up"
      return 0
    fi
    sleep $wait
  done
  echo "Timed out waiting for $host:$port" >&2
  return 1
}

# Postgres
wait_for_port localhost 5432
# Kafka
wait_for_port localhost 9092
# Neo4j (bolt)
wait_for_port localhost 7687
# Redis
wait_for_port localhost 6379

echo "All services are reachable"

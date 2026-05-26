#!/usr/bin/env bash

set -euo pipefail

COMPOSE_FILE="docker-compose.prod.yml"
HEALTH_URL="http://localhost:8080/actuator/health"
PROJECT_DIR="${PROJECT_DIR:-/home/manu/poketeam-backend}"

cd "$PROJECT_DIR"
echo ":: deploy starting on $(hostname) at $(date -Is)"
echo ":: head = $(git rev-parse --short HEAD) ($(git log -1 --pretty=%s))"

docker compose -f "$COMPOSE_FILE" up -d --build --remove-orphans

echo ":: waiting for app to report healthy (60s limit)..."

for i in {1..20}; do
    if curl -sf "$HEALTH_URL" > /dev/null; then
        echo ":: healthy after ${i} probes"
        echo ":: deploy succeeded"
        exit 0
    fi
    sleep 3
done

echo ":: app failed to report healthy within 60s — dumping last 80 log lines"
docker compose -f "$COMPOSE_FILE" logs --tail=80 app
exit 1

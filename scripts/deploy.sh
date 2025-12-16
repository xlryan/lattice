#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
COMPOSE_FILE="$PROJECT_ROOT/docker-compose.yml"

if ! command -v docker >/dev/null 2>&1; then
  echo "[deploy] docker command not found. Please install Docker before running this script." >&2
  exit 1
fi

if ! command -v docker compose >/dev/null 2>&1; then
  echo "[deploy] docker compose plugin not detected. Please upgrade Docker to a recent version." >&2
  exit 1
fi

pushd "$PROJECT_ROOT" >/dev/null

echo "[deploy] Building images..."
docker compose -f "$COMPOSE_FILE" build

echo "[deploy] Starting stack..."
docker compose -f "$COMPOSE_FILE" up -d

echo "[deploy] Current service status:"
docker compose -f "$COMPOSE_FILE" ps

popd >/dev/null

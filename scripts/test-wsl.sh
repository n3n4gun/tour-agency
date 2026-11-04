#!/usr/bin/env bash
set -euo pipefail

# Запускать из Ubuntu WSL; Java и Maven будут взяты из контейнера.
project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"

if [ "$#" -eq 0 ]; then
    set -- -Ppostgres-tests verify
fi

# При host networking порты тестовых контейнеров доступны Maven через localhost.
docker run --rm --network host \
    -e TESTCONTAINERS_HOST_OVERRIDE=127.0.0.1 \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v "$project_dir:/workspace" \
    -v tour-agency-test-maven-cache:/root/.m2 \
    -w /workspace \
    maven:3.9-eclipse-temurin-25 mvn -B -ntp "$@"

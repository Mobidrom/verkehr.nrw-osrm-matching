#!/bin/bash
set -eu

echo "Building & Pushing ${IMAGE_NAME}..."

./gradlew build \
  -Dquarkus.container-image.build=true \
  -Dquarkus.container-image.push=true \
  -Dquarkus.container-image.tag="${IMAGE_TAG}" \
  -Dquarkus.container-image.labels."org.opencontainers.image.source"="https://github.com/mobidrom/verkehr.nrw-osrm-matching"

# extract code coverage from jacoco.csv
./jacoco-summary.sh build/jacoco-report/jacoco.csv

echo "Building ${IMAGE_NAME} completed!"

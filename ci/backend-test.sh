#!/bin/bash

set -e

echo "Running backend Maven tests..."

docker run --rm \
  -v "$PWD/backend:/app" \
  -w /app \
  maven:3.9.9-eclipse-temurin-21 \
  mvn clean test

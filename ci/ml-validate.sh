#!/bin/bash

set -e

echo "Validating ML service..."

docker run --rm \
  -v "$PWD/ml-service:/app" \
  -w /app \
  python:3.12-slim \
  sh -c "pip install --no-cache-dir -r requirements.txt && python -m compileall app"

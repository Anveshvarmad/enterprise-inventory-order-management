#!/bin/bash

set -e

echo "Building frontend..."

docker run --rm \
  -v "$PWD/frontend:/app" \
  -w /app \
  node:20-alpine \
  sh -c "npm install && npm run build"

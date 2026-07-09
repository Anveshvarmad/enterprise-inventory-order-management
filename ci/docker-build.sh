#!/bin/bash

set -e

IMAGE_TAG=${1:-local}

echo "Building Docker images with tag: $IMAGE_TAG"

docker build -t enterprise-inventory-backend:$IMAGE_TAG ./backend
docker build -t enterprise-inventory-frontend:$IMAGE_TAG ./frontend
docker build -t enterprise-inventory-ml-service:$IMAGE_TAG ./ml-service

echo "Docker images built successfully."

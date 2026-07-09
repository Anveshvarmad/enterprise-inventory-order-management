#!/bin/bash

set -e

echo "Deploying to Kubernetes namespace inventory-system..."

kubectl apply -f k8s/local-deployment.yml

echo "Waiting for deployments..."

kubectl rollout status deployment/postgres -n inventory-system
kubectl rollout status deployment/redis -n inventory-system
kubectl rollout status deployment/ml-service -n inventory-system
kubectl rollout status deployment/backend -n inventory-system
kubectl rollout status deployment/frontend -n inventory-system

echo "Kubernetes deployment completed."

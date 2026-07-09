#!/bin/bash

set -e

NAMESPACE="inventory-system"

kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

kubectl delete secret inventory-secrets -n "$NAMESPACE" --ignore-not-found

kubectl create secret generic inventory-secrets \
  --namespace "$NAMESPACE" \
  --from-literal=POSTGRES_PASSWORD="inventory_password" \
  --from-literal=OPENAI_API_KEY="${OPENAI_API_KEY:-}"

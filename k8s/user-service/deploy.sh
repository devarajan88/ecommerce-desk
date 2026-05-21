#!/usr/bin/env bash
# deploy.sh — Build, push to ECR, and deploy user-service to EKS
#
# Usage:
#   chmod +x deploy.sh
#   ./deploy.sh <AWS_ACCOUNT_ID> <AWS_REGION> <IMAGE_TAG> <EKS_CLUSTER_NAME>
#
# Example:
#   ./deploy.sh 123456789012 ap-southeast-1 1.0.0 saga-eks-cluster

set -euo pipefail

AWS_ACCOUNT_ID="${1:?Usage: $0 <AWS_ACCOUNT_ID> <AWS_REGION> <IMAGE_TAG> <EKS_CLUSTER_NAME>}"
AWS_REGION="${2:?}"
IMAGE_TAG="${3:?}"
EKS_CLUSTER_NAME="${4:?}"

ECR_REPO="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/user-service"
MANIFEST_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "${MANIFEST_DIR}/../.." && pwd)"

echo "=== [1/5] Authenticating Docker with ECR ==="
aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin \
      "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

echo "=== [2/5] Building user-service JAR ==="
cd "${PROJECT_ROOT}"
mvn -pl user-service -am clean package -DskipTests

echo "=== [3/5] Building and pushing Docker image ==="
docker build -t "user-service:${IMAGE_TAG}" "${PROJECT_ROOT}/user-service"
docker tag "user-service:${IMAGE_TAG}" "${ECR_REPO}:${IMAGE_TAG}"
docker push "${ECR_REPO}:${IMAGE_TAG}"

echo "=== [4/5] Updating kubeconfig for EKS cluster ==="
aws eks update-kubeconfig \
  --region "${AWS_REGION}" \
  --name "${EKS_CLUSTER_NAME}"

echo "=== [5/5] Applying Kubernetes manifests ==="
# Patch the deployment image tag before applying
# (replaces the placeholder <IMAGE_TAG> in the YAML)
sed "s|<AWS_ACCOUNT_ID>|${AWS_ACCOUNT_ID}|g; \
     s|<AWS_REGION>|${AWS_REGION}|g; \
     s|<IMAGE_TAG>|${IMAGE_TAG}|g" \
  "${MANIFEST_DIR}/04-deployment.yaml" | kubectl apply -f -

kubectl apply -f "${MANIFEST_DIR}/00-namespace.yaml"
kubectl apply -f "${MANIFEST_DIR}/01-serviceaccount.yaml"
kubectl apply -f "${MANIFEST_DIR}/02-configmap.yaml"
kubectl apply -f "${MANIFEST_DIR}/03-secret.yaml"
kubectl apply -f "${MANIFEST_DIR}/05-service.yaml"
kubectl apply -f "${MANIFEST_DIR}/06-hpa.yaml"
kubectl apply -f "${MANIFEST_DIR}/07-pdb.yaml"

echo "=== Waiting for rollout to complete ==="
kubectl rollout status deployment/user-service -n saga --timeout=120s

echo ""
echo "=== Deployment complete ==="
kubectl get pods -n saga -l app=user-service
kubectl get hpa  -n saga user-service-hpa
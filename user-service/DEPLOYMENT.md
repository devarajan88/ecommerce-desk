# user-service — EKS Deployment Guide

Manifests live in [`k8s/user-service/`](../k8s/user-service/).

---

## Prerequisites

| Tool | Version | Install |
|---|---|---|
| AWS CLI | v2+ | `brew install awscli` |
| kubectl | 1.28+ | `brew install kubectl` |
| Docker | 24+ | [docker.com](https://www.docker.com) |
| Maven | 3.9+ | `brew install maven` |
| eksctl (optional) | latest | `brew install eksctl` |

AWS credentials must be configured (`aws configure`) with permissions for:
- ECR (`ecr:GetAuthorizationToken`, `ecr:BatchCheckLayerAvailability`, `ecr:PutImage`)
- EKS (`eks:DescribeCluster`, `eks:UpdateKubeconfig`)

---

## Step 1 — Fill in placeholders

Open each file and replace the following tokens:

| Placeholder | File | Replace with |
|---|---|---|
| `<AWS_ACCOUNT_ID>` | `01-serviceaccount.yaml`, `04-deployment.yaml`, `deploy.sh` | Your 12-digit AWS account ID |
| `<AWS_REGION>` | `04-deployment.yaml`, `deploy.sh` | e.g. `ap-southeast-1` |
| `<IMAGE_TAG>` | `04-deployment.yaml` | e.g. `1.0.0` or a git SHA |
| `<RDS_ENDPOINT>` | `02-configmap.yaml` | RDS Aurora MySQL cluster endpoint |
| `<MSK_BOOTSTRAP_BROKERS>` | `02-configmap.yaml` | MSK broker list (comma-separated) |
| `<ELASTICACHE_REDIS_HOST>` | `02-configmap.yaml` | ElastiCache Redis primary endpoint |

> **Production note:** Do not commit `03-secret.yaml` with real values.
> Use [External Secrets Operator](https://external-secrets.io/) with AWS Secrets Manager instead (see Step 5).

---

## Step 2 — Create the ECR repository (first time only)

```bash
aws ecr create-repository \
  --repository-name user-service \
  --region <AWS_REGION> \
  --image-scanning-configuration scanOnPush=true
```

---

## Step 3 — Build the JAR

From the project root:

```bash
mvn -pl user-service -am clean package -DskipTests
```

The fat JAR will be at `user-service/target/user-service-1.0.0.jar`.

---

## Step 4 — Build, tag and push the Docker image

```bash
# Authenticate Docker with ECR
aws ecr get-login-password --region <AWS_REGION> \
  | docker login --username AWS --password-stdin \
      <AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com

# Build
docker build -t user-service:<IMAGE_TAG> ./user-service

# Tag
docker tag user-service:<IMAGE_TAG> \
  <AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/user-service:<IMAGE_TAG>

# Push
docker push \
  <AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/user-service:<IMAGE_TAG>
```

---

## Step 5 — Configure kubectl for your EKS cluster

```bash
aws eks update-kubeconfig \
  --region <AWS_REGION> \
  --name <EKS_CLUSTER_NAME>

# Verify connection
kubectl get nodes
```

---

## Step 6 — Apply manifests

Apply in order (numeric prefix ensures correct ordering):

```bash
kubectl apply -f k8s/user-service/00-namespace.yaml
kubectl apply -f k8s/user-service/01-serviceaccount.yaml
kubectl apply -f k8s/user-service/02-configmap.yaml
kubectl apply -f k8s/user-service/03-secret.yaml

# Patch image tag into deployment then apply
sed "s|<AWS_ACCOUNT_ID>|123456789012|g; \
     s|<AWS_REGION>|ap-southeast-1|g; \
     s|<IMAGE_TAG>|1.0.0|g" \
  k8s/user-service/04-deployment.yaml | kubectl apply -f -

kubectl apply -f k8s/user-service/05-service.yaml
kubectl apply -f k8s/user-service/06-hpa.yaml
kubectl apply -f k8s/user-service/07-pdb.yaml
```

Or use the deploy script (handles build + push + apply in one command):

```bash
chmod +x k8s/user-service/deploy.sh
./k8s/user-service/deploy.sh <AWS_ACCOUNT_ID> <AWS_REGION> <IMAGE_TAG> <EKS_CLUSTER_NAME>

# Example
./k8s/user-service/deploy.sh 123456789012 ap-southeast-1 1.0.0 saga-eks-cluster
```

---

## Step 7 — Verify the deployment

```bash
# Watch rollout
kubectl rollout status deployment/user-service -n saga --timeout=120s

# Check pods are running (expect 2 replicas)
kubectl get pods -n saga -l app=user-service

# Check HPA is active (TARGETS should show real CPU% after ~30s)
kubectl get hpa -n saga user-service-hpa

# Check PDB
kubectl get pdb -n saga user-service-pdb

# Tail logs
kubectl logs -n saga -l app=user-service --all-containers -f

# Describe a pod for events (useful when pod is in CrashLoopBackOff or Pending)
kubectl describe pod -n saga -l app=user-service
```

Expected healthy pod output:
```
NAME                            READY   STATUS    RESTARTS   AGE
user-service-7d9f8b5c4-k2x9p   1/1     Running   0          2m
user-service-7d9f8b5c4-n7wlq   1/1     Running   0          2m
```

---

## Step 8 — Smoke test

Port-forward to test without exposing via Ingress:

```bash
kubectl port-forward -n saga svc/user-service 8083:8083

# Health check
curl http://localhost:8083/actuator/health

# Test auth endpoint (no JWT required)
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'
```

---

## Updating to a new version

```bash
# Build and push new image
docker build -t user-service:1.1.0 ./user-service
docker tag user-service:1.1.0 <AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/user-service:1.1.0
docker push <AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/user-service:1.1.0

# Patch the running deployment (zero-downtime rolling update)
kubectl set image deployment/user-service \
  user-service=<AWS_ACCOUNT_ID>.dkr.ecr.<AWS_REGION>.amazonaws.com/user-service:1.1.0 \
  -n saga

# Watch rollout
kubectl rollout status deployment/user-service -n saga
```

To rollback if a deploy goes wrong:

```bash
kubectl rollout undo deployment/user-service -n saga
```

---

## Secrets management (production)

Do not store real secrets in `03-secret.yaml`. Use **External Secrets Operator** backed by AWS Secrets Manager:

1. Store secrets in AWS Secrets Manager:
```bash
aws secretsmanager create-secret \
  --name saga/user-service \
  --secret-string '{
    "CONFIG_SERVER_PASSWORD":"configsecret",
    "SPRING_DATASOURCE_PASSWORD":"strongpassword",
    "KEYCLOAK_CLIENT_SECRET":"K0Jn...",
    "JWT_SECRET":"your-256-bit-key"
  }'
```

2. Replace `03-secret.yaml` with an `ExternalSecret` resource that pulls from Secrets Manager automatically and syncs on rotation.

---

## Manifest reference

| File | Resource | Purpose |
|---|---|---|
| `00-namespace.yaml` | `Namespace` | Isolates all saga services under the `saga` namespace |
| `01-serviceaccount.yaml` | `ServiceAccount` | IRSA binding — pod identity for AWS API access |
| `02-configmap.yaml` | `ConfigMap` | Non-sensitive env vars (URLs, profile, JVM flags) |
| `03-secret.yaml` | `Secret` | Passwords and API keys (base64-encoded at rest in etcd) |
| `04-deployment.yaml` | `Deployment` | Pod spec — image, probes, resources, security context |
| `05-service.yaml` | `Service` (ClusterIP) | Internal DNS name for other services to call |
| `06-hpa.yaml` | `HorizontalPodAutoscaler` | Auto-scales 2→10 pods based on CPU/memory |
| `07-pdb.yaml` | `PodDisruptionBudget` | Ensures at least 1 pod survives node drains |
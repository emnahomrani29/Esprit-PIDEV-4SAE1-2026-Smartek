# SMARTEK Kubernetes Deployment Guide

## Prerequisites

### 1. Install Required Tools
```bash
# kubectl (Kubernetes CLI)
# Windows: Use Chocolatey or download from https://kubernetes.io/docs/tasks/tools/
choco install kubernetes-cli

# Docker (for building images)
# Windows: Download Docker Desktop from https://www.docker.com/products/docker-desktop

# Helm (optional, for package management)
choco install kubernetes-helm
```

### 2. Setup Kubernetes Cluster

#### Option A: Docker Desktop (Recommended for local development)
1. Install Docker Desktop for Windows
2. Go to Settings → Kubernetes → Enable Kubernetes
3. Wait for cluster to be ready (check via `kubectl cluster-info`)

#### Option B: Minikube (Alternative local option)
```bash
# Install Minikube
choco install minikube

# Start Minikube with sufficient resources
minikube start --cpus=4 --memory=8192 --disk-size=30g

# Enable ingress addon (optional)
minikube addons enable ingress
```

#### Option C: Cloud Kubernetes (AWS EKS, Azure AKS, Google GKE)
- Follow provider-specific instructions to create and configure cluster
- Configure `kubectl` to access the cluster

### 3. Build and Push Docker Images

Navigate to project root directory and build images:

```bash
# Build all microservice images
cd Backend

# Build individual services
docker build -t your-registry/smartek/eureka-server:latest ./eureka-server
docker build -t your-registry/smartek/auth-service:latest ./auth-service
docker build -t your-registry/smartek/event-service:latest ./event-service
docker build -t your-registry/smartek/planning-service:latest ./planning-service
docker build -t your-registry/smartek/training-service:latest ./training-service
docker build -t your-registry/smartek/offers-service:latest ./offers-service
docker build -t your-registry/smartek/exam-service:latest ./exam-service
docker build -t your-registry/smartek/course-service:latest ./course-service
docker build -t your-registry/smartek/learning-service:latest ./learning
docker build -t your-registry/smartek/skill-evidence-service:latest ./skiil-evidence-service
docker build -t your-registry/smartek/config-server:latest ./config-server
docker build -t your-registry/smartek/api-gateway:latest ./api-gateway

# Build frontend
cd ../Frontend/angular-app
docker build -t your-registry/smartek/frontend:latest .

# Push to Docker registry (Docker Hub or private registry)
docker push your-registry/smartek/eureka-server:latest
# ... push all other images
```

**Note:** Replace `your-registry` with:
- Your Docker Hub username for public registry (e.g., `myusername`)
- Your private registry URL (e.g., `registry.example.com`)

## Deployment Steps

### Step 1: Create Namespace and Secrets
```bash
cd k8s

# Create namespace
kubectl apply -f 01-namespace/namespace.yaml

# Create secrets and configmaps
kubectl apply -f 02-secrets-configmaps/secrets.yaml
kubectl apply -f 02-secrets-configmaps/configmap.yaml

# List created resources
kubectl get secrets -n smartek
kubectl get configmaps -n smartek
```

### Step 2: Deploy Database
```bash
# Deploy MySQL
kubectl apply -f 03-database/mysql-statefulset.yaml

# Wait for MySQL to be ready
kubectl wait --for=condition=ready pod -l app=mysql -n smartek --timeout=300s

# Verify MySQL running
kubectl get pods -n smartek
kubectl logs -n smartek statefulset/mysql --tail=50
```

### Step 3: Deploy Microservices
```bash
# Deploy Eureka Server (must be first - service discovery)
kubectl apply -f 04-microservices/eureka-server.yaml
kubectl wait --for=condition=Ready pod -l app=eureka-server -n smartek --timeout=300s

# Deploy other microservices
kubectl apply -f 04-microservices/auth-service.yaml
kubectl apply -f 04-microservices/services-part1.yaml
kubectl apply -f 04-microservices/services-part2.yaml
kubectl apply -f 04-microservices/gateway-frontend.yaml

# Wait for all deployments to be ready
kubectl wait --for=condition=available --timeout=600s deployment --all -n smartek

# Verify services
kubectl get pods -n smartek
kubectl get svc -n smartek
```

### Step 4: Setup Ingress Controller (if not already installed)
```bash
# For NGINX Ingress Controller
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.1/deploy/static/provider/cloud/deploy.yaml

# Wait for ingress controller to be ready
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=ingress-nginx -n ingress-nginx --timeout=300s
```

### Step 5: Deploy Ingress
```bash
# Deploy ingress rules
kubectl apply -f 05-ingress/ingress.yaml

# Get ingress info
kubectl get ingress -n smartek
```

### Step 6: Deploy Monitoring Stack (Optional)
```bash
# Deploy Prometheus and Grafana
kubectl apply -f 06-monitoring/prometheus-grafana.yaml

# Verify monitoring pods
kubectl get pods -n smartek | grep -E "prometheus|grafana"
```

## Accessing the Application

### Local Access (Docker Desktop / Minikube)

1. **Update hosts file** (C:\Windows\System32\drivers\etc\hosts):
```
127.0.0.1 smartek.local
```

2. **Access services:**
   - Frontend: http://smartek.local
   - API Gateway: http://smartek.local/api
   - Eureka: http://smartek.local/eureka
   - Prometheus: http://localhost:9090 (port-forward)
   - Grafana: http://localhost:3000 (port-forward)

### Port Forwarding

```bash
# Forward API Gateway
kubectl port-forward svc/api-gateway -n smartek 8090:8090

# Forward Eureka Server
kubectl port-forward svc/eureka-server -n smartek 8761:8761

# Forward Prometheus
kubectl port-forward svc/prometheus -n smartek 9090:9090

# Forward Grafana
kubectl port-forward svc/grafana -n smartek 3000:3000

# Forward MySQL
kubectl port-forward svc/mysql-service -n smartek 3306:3306
```

## Useful Kubernetes Commands

```bash
# View all resources in smartek namespace
kubectl get all -n smartek

# View logs
kubectl logs -n smartek deployment/auth-service
kubectl logs -n smartek -f deployment/api-gateway  # Follow logs

# Describe pod (for debugging)
kubectl describe pod -n smartek <pod-name>

# Execute command in pod
kubectl exec -it -n smartek <pod-name> -- /bin/bash

# SSH into MySQL
kubectl exec -it -n smartek mysql-0 -- mysql -u root -proot

# Scale deployment
kubectl scale deployment -n smartek auth-service --replicas=3

# Update image
kubectl set image deployment/auth-service -n smartek auth-service=your-registry/smartek/auth-service:v2

# Delete specific resource
kubectl delete deployment -n smartek auth-service

# View resource usage
kubectl top nodes
kubectl top pods -n smartek
```

## Environment-Specific Configuration

### For Production Deployment:

1. **Update image tags** in YAML files to use version tags instead of `latest`
2. **Configure proper image pull secrets** for private registries
3. **Update database credentials** in secrets.yaml
4. **Configure TLS certificates** for HTTPS
5. **Set resource requests/limits** appropriately for your cluster
6. **Configure persistent storage** based on your infrastructure
7. **Set up proper monitoring and alerting**
8. **Configure backup strategy for databases**

### Sample Production Changes:

Create `kustomization.yaml` for environment-specific configs:

```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

bases:
- ../../base

patchesStrategicMerge:
- mysql-patch.yaml
- replicas-patch.yaml

configMapGenerator:
- name: smartek-config
  behavior: merge
  literals:
  - SPRING_PROFILES_ACTIVE=production
```

## Troubleshooting

### Pod not starting?
```bash
# Check logs
kubectl logs -n smartek <pod-name>

# Check events
kubectl describe pod -n smartek <pod-name>
```

### Database connection issues?
```bash
# Verify MySQL is accessible
kubectl exec -it -n smartek <any-service-pod> -- curl http://mysql-service:3306

# Check MySQL logs
kubectl logs -n smartek mysql-0
```

### Eureka not discovering services?
```bash
# Check Eureka logs
kubectl logs -n smartek deployment/eureka-server

# Check if services are registered
# Visit http://localhost:8761 (via port-forward)
```

### Image pull errors?
```bash
# Update image pull secret
kubectl create secret docker-registry docker-registry-secret \
  --docker-server=docker.io \
  --docker-username=<username> \
  --docker-password=<password> \
  -n smartek

# Update YAML files to reference the secret
```

## Cleanup

```bash
# Delete all resources in smartek namespace
kubectl delete namespace smartek

# Or delete specific resources
kubectl delete all -n smartek
```

## Next Steps

1. Configure CI/CD pipeline to automate image building and deployment
2. Set up monitoring alerts in Prometheus/Grafana
3. Configure backup and disaster recovery procedures
4. Set up log aggregation (ELK, Loki, etc.)
5. Implement network policies for service-to-service communication
6. Configure resource quotas and limits per namespace

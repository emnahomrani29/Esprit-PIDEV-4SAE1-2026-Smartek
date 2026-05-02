# Kubernetes Deployment Structure

This directory contains all Kubernetes manifests and deployment scripts for the SMARTEK project.

## Directory Structure

```
k8s/
├── 01-namespace/              # Kubernetes namespace definition
│   └── namespace.yaml
├── 02-secrets-configmaps/     # Configuration and secrets
│   ├── configmap.yaml
│   └── secrets.yaml
├── 03-database/               # Database deployment (MySQL)
│   └── mysql-statefulset.yaml
├── 04-microservices/          # Microservices deployments
│   ├── eureka-server.yaml
│   ├── auth-service.yaml
│   ├── services-part1.yaml    # Event, Planning, Training, Offers
│   ├── services-part2.yaml    # Exam, Course, Learning, Skill-Evidence, Config
│   └── gateway-frontend.yaml  # API Gateway and Frontend
├── 05-ingress/                # Ingress configuration
│   └── ingress.yaml
├── 06-monitoring/             # Monitoring stack (Prometheus/Grafana)
│   └── prometheus-grafana.yaml
├── DEPLOYMENT_GUIDE.md        # Comprehensive deployment guide
├── README.md                  # This file
├── deploy.sh                  # Bash deployment script
├── deploy.ps1                 # PowerShell deployment script
└── build-and-push.sh          # Image build and push script
```

## Quick Start

### 1. Prerequisites
- Kubernetes cluster (Docker Desktop, Minikube, EKS, AKS, or GKE)
- Docker for building images
- kubectl CLI tool
- Your repository credentials

### 2. Build and Push Docker Images
```bash
# Linux/Mac
./build-and-push.sh your-docker-registry

# Windows PowerShell
.\build-and-push.ps1 -Registry your-docker-registry
```

### 3. Deploy to Kubernetes
```bash
# Linux/Mac
./deploy.sh -n smartek

# Windows PowerShell
.\deploy.ps1 -Namespace smartek -Registry your-docker-registry
```

### 4. Access the Application
```bash
# Port forward to API Gateway
kubectl port-forward svc/api-gateway -n smartek 8090:8090

# Port forward to Frontend
kubectl port-forward svc/frontend-service -n smartek 4200:80

# Port forward to Eureka
kubectl port-forward svc/eureka-server -n smartek 8761:8761
```

## File Descriptions

### 01-namespace/namespace.yaml
Creates the `smartek` namespace where all resources are deployed.

### 02-secrets-configmaps/
- **configmap.yaml**: Contains non-sensitive configuration (database host, port, Eureka endpoint)
- **secrets.yaml**: Contains sensitive data (database credentials, JWT secrets)

### 03-database/mysql-statefulset.yaml
Deploys MySQL as a StatefulSet with:
- Persistent volume for data
- Database initialization script
- Health checks
- Configurable storage

### 04-microservices/
Deployment manifests for all microservices:
- **eureka-server.yaml**: Service discovery server
- **auth-service.yaml**: Authentication service
- **services-part1.yaml**: Event, Planning, Training, Offers services
- **services-part2.yaml**: Exam, Course, Learning, Skill Evidence, Config services
- **gateway-frontend.yaml**: API Gateway and Angular frontend

Each service includes:
- Kubernetes Service (ClusterIP)
- Deployment with replicas
- Resource requests/limits
- Liveness and readiness probes
- Environment variable configuration

### 05-ingress/ingress.yaml
NGINX Ingress configuration for:
- URL routing
- TLS support
- CORS handling

### 06-monitoring/prometheus-grafana.yaml
Monitoring stack deployment:
- Prometheus for metrics collection
- Grafana for visualization
- Persistent volumes for data storage
- Service configuration for metric scraping

## Important Notes

### Image Registry
All YAML files reference `your-docker-registry` as a placeholder. You must:
1. Build Docker images from source code
2. Push to a registry (Docker Hub, private registry, or cloud provider)
3. Update all YAML files with correct registry URL:
   ```bash
   # Find and replace in all files
   sed -i 's|your-docker-registry|your-actual-registry|g' **/*.yaml
   ```

### Database Credentials
Secrets in `02-secrets-configmaps/secrets.yaml` contain default credentials. For production:
1. Change default credentials in the secret
2. Use proper secret management (HashiCorp Vault, AWS Secrets Manager, etc.)
3. Never commit credentials to version control

### Storage
- MySQL uses StatefulSet with PVC for persistent storage
- Prometheus and Grafana use PVC for metric/dashboard storage
- Default storage class must be available in your cluster

### Network Policy
By default, all pods can communicate. For production:
1. Implement network policies
2. Restrict service-to-service communication
3. Use service mesh (Istio, Linkerd) for advanced features

## Troubleshooting

### Check deployment status
```bash
kubectl get all -n smartek
kubectl describe pod <pod-name> -n smartek
kubectl logs <pod-name> -n smartek
```

### Common issues and solutions
See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for troubleshooting section.

## Environment Customization

### Update Configuration
Edit `02-secrets-configmaps/configmap.yaml`:
```yaml
data:
  MYSQL_HOST: "mysql-service.smartek.svc.cluster.local"  # Change as needed
  EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: "http://eureka-server:8761/eureka/"
```

### Scale Replicas
Edit deployment files to change `replicas: 1` to desired number:
```yaml
spec:
  replicas: 3  # Scale to 3 instances
```

Or use kubectl:
```bash
kubectl scale deployment auth-service -n smartek --replicas=3
```

### Update Images
```bash
kubectl set image deployment/auth-service -n smartek auth-service=new-image:v2
```

## Resource Requirements

### Recommended Cluster Size
- **Development**: 2 CPU, 4GB RAM minimum
- **Production**: 4+ CPU, 8GB+ RAM recommended

### Resource Requests
- MySQL: 256Mi - 512Mi RAM, 250m - 500m CPU
- Microservices: 512Mi RAM, 250m CPU (each)
- Monitoring: 256Mi - 512Mi RAM

Adjust in YAML files based on your application needs.

## Cleanup

```bash
# Delete entire namespace (all resources deleted)
kubectl delete namespace smartek

# Delete specific resource
kubectl delete deployment auth-service -n smartek

# View deletion progress
kubectl get pods -n smartek
```

## Additional Resources

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [SMARTEK Deployment Guide](DEPLOYMENT_GUIDE.md)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)

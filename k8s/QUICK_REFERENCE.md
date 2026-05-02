# SMARTEK Kubernetes Quick Reference

## Before You Start

- [ ] Kubernetes cluster installed and running
- [ ] kubectl configured and connected to cluster
- [ ] Docker installed and running
- [ ] Docker registry configured (Docker Hub or private)
- [ ] Project repository cloned to local machine

## Pre-Deployment Steps

### 1. Build Docker Images (5-10 minutes per image)

**Linux/Mac:**
```bash
cd k8s
chmod +x build-and-push.sh
./build-and-push.sh myusername latest
```

**Windows PowerShell:**
```powershell
cd k8s
.\build-and-push.ps1 -Registry myusername -Tag latest
```

Replace `myusername` with:
- Your Docker Hub username (for public registry)
- Your private registry URL with port (e.g., `registry.example.com:5000`)

### 2. Update YAML Files with Registry

**Linux/Mac:**
```bash
sed -i 's|your-docker-registry|myusername|g' *.yaml 04-microservices/*.yaml
```

**Windows PowerShell:**
```powershell
Get-ChildItem -Recurse -Filter "*.yaml" | ForEach-Object {
    (Get-Content $_.FullName) -replace 'your-docker-registry', 'myusername' | Set-Content $_.FullName
}
```

## Deployment

### Single Command Deployment

**Linux/Mac:**
```bash
cd k8s
./deploy.sh
```

**Windows PowerShell:**
```powershell
cd k8s
.\deploy.ps1 -Namespace smartek
```

### Manual Step-by-Step Deployment

```bash
cd k8s

# 1. Create namespace
kubectl apply -f 01-namespace/namespace.yaml

# 2. Create secrets and configs
kubectl apply -f 02-secrets-configmaps/

# 3. Deploy database
kubectl apply -f 03-database/mysql-statefulset.yaml
kubectl wait --for=condition=ready pod -l app=mysql -n smartek --timeout=300s

# 4. Deploy microservices
kubectl apply -f 04-microservices/eureka-server.yaml
kubectl apply -f 04-microservices/
kubectl wait --for=condition=available deployment --all -n smartek --timeout=600s

# 5. Deploy ingress
kubectl apply -f 05-ingress/

# 6. Deploy monitoring (optional)
kubectl apply -f 06-monitoring/
```

## Verify Deployment

```bash
# Check all resources in smartek namespace
kubectl get all -n smartek

# Check specific deployments
kubectl get deployments -n smartek

# Check services
kubectl get services -n smartek

# Check pods
kubectl get pods -n smartek

# Check persistent volumes
kubectl get pvc -n smartek

# Check recent events
kubectl describe node

# View errors (if any)
kubectl describe pod -n smartek <pod-name>
```

## Access Services

### Option 1: Port Forwarding (Local Access)

```bash
# API Gateway (Port 8090)
kubectl port-forward svc/api-gateway -n smartek 8090:8090
# Access: http://localhost:8090

# Frontend (Port 4200)
kubectl port-forward svc/frontend-service -n smartek 4200:80
# Access: http://localhost:4200

# Eureka Server (Port 8761)
kubectl port-forward svc/eureka-server -n smartek 8761:8761
# Access: http://localhost:8761

# Prometheus (Port 9090)
kubectl port-forward svc/prometheus -n smartek 9090:9090
# Access: http://localhost:9090

# Grafana (Port 3000)
kubectl port-forward svc/grafana -n smartek 3000:3000
# Access: http://localhost:3000
# Username: admin
# Password: admin123
```

### Option 2: Ingress (Production Access)

```bash
# Get Ingress IP/URL
kubectl get ingress -n smartek

# Add to hosts file (Windows: C:\Windows\System32\drivers\etc\hosts)
# Linux: /etc/hosts
# <INGRESS_IP> smartek.local

# Access: http://smartek.local
```

## Common Operations

### View Logs

```bash
# View logs from specific pod
kubectl logs -n smartek <pod-name>

# Follow logs (real-time)
kubectl logs -n smartek <pod-name> -f

# View logs from specific container in pod
kubectl logs -n smartek <pod-name> -c <container-name>

# View logs from previous container instance
kubectl logs -n smartek <pod-name> --previous

# View logs from all pods with label
kubectl logs -n smartek -l app=auth-service
```

### Execute Commands in Pod

```bash
# Shell access to pod
kubectl exec -it -n smartek <pod-name> -- /bin/bash

# Run command in pod
kubectl exec -n smartek <pod-name> -- ls -la

# Access MySQL
kubectl exec -it -n smartek mysql-0 -- mysql -u root -proot
```

### Scale Services

```bash
# Scale to 3 replicas
kubectl scale deployment auth-service -n smartek --replicas=3

# Autoscale (requires metrics-server)
kubectl autoscale deployment auth-service -n smartek --min=1 --max=5
```

### Update Image

```bash
# Update image for specific deployment
kubectl set image deployment/auth-service -n smartek \
  auth-service=myusername/smartek/auth-service:v2

# View rollout status
kubectl rollout status deployment/auth-service -n smartek
```

### Rollback Deployment

```bash
# View rollout history
kubectl rollout history deployment/auth-service -n smartek

# Rollback to previous version
kubectl rollout undo deployment/auth-service -n smartek

# Rollback to specific revision
kubectl rollout undo deployment/auth-service -n smartek --to-revision=2
```

### Delete Resources

```bash
# Delete specific deployment
kubectl delete deployment auth-service -n smartek

# Delete specific pod (will respawn if deployment exists)
kubectl delete pod <pod-name> -n smartek

# Delete all resources in namespace
kubectl delete all -n smartek

# Delete entire namespace and all resources
kubectl delete namespace smartek
```

## Troubleshooting Commands

### Pod Issues

```bash
# Describe pod to see events and errors
kubectl describe pod -n smartek <pod-name>

# Check pod status
kubectl get pod -n smartek <pod-name> -o wide

# Check resource usage
kubectl top pod -n smartek <pod-name>

# Check node status
kubectl get nodes
kubectl describe node <node-name>

# Check resource requests vs actual
kubectl get pod -n smartek <pod-name> -o yaml | grep -A 10 "resources:"
```

### Database Connectivity

```bash
# Test MySQL connection
kubectl exec -it -n smartek <service-pod> -- \
  mysql -h mysql-service -u smartek_user -p smartek_db -e "SELECT 1;"

# Check MySQL logs
kubectl logs -n smartek mysql-0

# Exec into MySQL
kubectl exec -it -n smartek mysql-0 -- mysql -u root -proot
```

### Service Discovery

```bash
# Check if Eureka is running
kubectl exec -it -n smartek <pod> -- curl http://eureka-server:8761

# Check service endpoints
kubectl get endpoints -n smartek

# Check service DNS resolution
kubectl exec -it -n smartek <pod> -- nslookup auth-service.smartek.svc.cluster.local
```

## Performance Check

```bash
# CPU and memory usage
kubectl top nodes
kubectl top pods -n smartek

# Check replica status
kubectl get deployment -n smartek

# Check pod distribution
kubectl get pods -n smartek -o wide

# Check persistent volumes
kubectl get pvc -n smartek
df -h  # On the nodes
```

## Clean Up & Reset

```bash
# Remove all Kubernetes resources (CAREFUL!)
kubectl delete namespace smartek

# Delete persistent data
kubectl delete pvc --all -n smartek
kubectl delete pv --all

# Delete everything including node resources
kubectl delete all --all -n smartek
```

## Backup and Restore

### Backup MySQL

```bash
# Backup database
kubectl exec -n smartek mysql-0 -- \
  mysqldump -u root -proot --all-databases > backup.sql

# Restore database
kubectl exec -i -n smartek mysql-0 -- \
  mysql -u root -proot < backup.sql
```

### Backup ConfigMaps and Secrets

```bash
# Export all resources in namespace
kubectl get all -n smartek -o yaml > smartek-backup.yaml

# Export specific secrets
kubectl get secret -n smartek -o yaml > secrets-backup.yaml

# Restore from backup
kubectl apply -f smartek-backup.yaml
```

## Useful Aliases

Add to your shell profile (.bashrc, .zshrc, or PowerShell profile):

```bash
alias k='kubectl'
alias kgp='kubectl get pods -n smartek'
alias kgs='kubectl get svc -n smartek'
alias kgd='kubectl get deployment -n smartek'
alias kl='kubectl logs -n smartek'
alias kdesc='kubectl describe -n smartek'
```

## Emergency Procedures

### Restart All Pods

```bash
kubectl rollout restart deployment --all -n smartek
```

### Force Delete Stuck Pod

```bash
kubectl delete pod <pod-name> -n smartek --grace-period=0 --force
```

### Reset Namespace

```bash
# Delete and recreate namespace
kubectl delete namespace smartek
kubectl apply -f 01-namespace/namespace.yaml
```

## Useful Links

- Kubernetes Docs: https://kubernetes.io/docs/
- kubectl Cheat Sheet: https://kubernetes.io/docs/reference/kubectl/cheatsheet/
- Deployment Best Practices: https://kubernetes.io/docs/concepts/configuration/overview/

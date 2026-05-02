# Kubernetes & Jenkins Integration Guide for SMARTEK

## Why Do We Need Kubernetes? 🤔

Your SMARTEK project is a **microservices architecture** with 12+ independent services. Here's why Kubernetes is essential:

### The Problem Without Kubernetes

**Before (Local Environment):**
```
Your Laptop/VM
├── MySQL running
├── Eureka Server running
├── Auth Service running
├── Event Service running
├── ... (10+ more services)
└── Single Frontend
```

**Challenges:**
- ❌ Manual deployment of each service
- ❌ Manual scaling (copy-paste and run multiple instances?)
- ❌ No automatic restart if a service crashes
- ❌ Manual load balancing between instances
- ❌ No centralized logging/monitoring
- ❌ Difficult to manage dependencies (startup order)
- ❌ Resource wastage (all services on one machine)
- ❌ No zero-downtime deployments

### The Solution: Kubernetes

**With Kubernetes:**
```
Kubernetes Cluster (VM with proper orchestration)
├── Namespace: smartek
│   ├── MySQL StatefulSet (data persistence)
│   ├── Eureka Server (auto-discovered services)
│   ├── Auth Service (auto-scaled: 1-5 replicas)
│   ├── Event Service (auto-scaled: 1-5 replicas)
│   ├── ... (all 12+ services)
│   ├── API Gateway (load balancer)
│   ├── Frontend (auto-scaled)
│   └── Monitoring (Prometheus + Grafana)
└── Self-healing & Auto-scaling
```

## Key Benefits of Kubernetes for SMARTEK

### 1. **Automatic Deployment & Scaling** 📈
```yaml
# Kubernetes automatically:
# - Creates 3 instances of auth-service
# - Distributes them across nodes
# - Load balances traffic between them
replicas: 3
```

### 2. **Self-Healing** 🏥
```
If a pod crashes:
  1. Kubernetes detects it immediately
  2. Automatically restarts the container
  3. Your service keeps running
  → No manual intervention needed!
```

### 3. **Rolling Updates** ✨
```
Deploy new version without downtime:
  Old Version (3 instances)  →  New Version (3 instances)
  1. Start 1 new instance
  2. Kill 1 old instance
  3. Repeat until all updated
  → Users don't notice any downtime
```

### 4. **Resource Management** 💾
```
Kubernetes allocates resources efficiently:
  - Memory: 512 MB max for each service
  - CPU: 500m (0.5 CPU) limit
  - Prevents one service from monopolizing resources
```

### 5. **Service Discovery** 🔍
```
Services automatically discover each other:
  auth-service:8081
  event-service:8082
  → No need to hardcode IP addresses
  → Services find each other automatically
```

### 6. **Centralized Monitoring** 📊
```
Prometheus Metrics:
  - Prometheus collects metrics from all services
  - Grafana visualizes them
  - You see everything in one dashboard
```

## Architecture: How It Works Together

### Your Current Setup
```
Jenkins (on separate VM)
    ↓
    ├── Build Pipeline (Docker image)
    ├── Push to Registry
    └── ??? (How to deploy?)
```

### With Kubernetes Integration
```
Jenkins Pipeline
    ↓
    ├── Build Docker Image
    │   (learning-service:v1.2.3)
    │
    ├── Push to Docker Registry
    │   (Docker Hub/Private Registry)
    │
    ├── Update Kubernetes YAML
    │   (change image tag in deployment)
    │
    └── Deploy to Kubernetes
        kubectl apply -f learning-deployment.yaml
        ↓
        Kubernetes automatically handles:
        ├── Rolling update
        ├── Health checks
        ├── Self-healing
        ├── Load balancing
        └── Monitoring
```

## Why This Architecture Makes Sense

### Without Kubernetes (Manual Approach)
```
1. Build service in Jenkins
2. SSH into VM
3. Stop old container manually
4. Pull new image
5. Start new container
6. Check if it's running
7. If crashed, restart manually
= TEDIOUS & ERROR-PRONE
```

### With Kubernetes (Automated)
```
1. Build service in Jenkins
2. Jenkins applies new YAML to Kubernetes
3. Kubernetes handles everything automatically
   - Deployment
   - Health checks
   - Restart if needed
   - Load balancing
= SIMPLE & RELIABLE
```

## Jenkins + Kubernetes Integration Flow

```
┌─────────────────────────────────────────────────────────┐
│                   Developer                             │
│              Push code to GitHub                        │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────┐
│                   Jenkins                               │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Pipeline Stage 1: Build                          │   │
│  │ - Checkout code                                  │   │
│  │ - Run tests                                      │   │
│  │ - Build JAR (Maven)                             │   │
│  │ Result: learning-service-1.0.0.jar             │   │
│  └──────────────────────────────────────────────────┘   │
│                   ↓                                      │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Pipeline Stage 2: Containerize                   │   │
│  │ - docker build                                   │   │
│  │ - Create image: learning-service:v1.2.3        │   │
│  │ Result: Docker image                            │   │
│  └──────────────────────────────────────────────────┘   │
│                   ↓                                      │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Pipeline Stage 3: Push to Registry               │   │
│  │ - docker push                                    │   │
│  │ - Push to Docker Hub/Private Registry           │   │
│  │ Result: Image available in registry             │   │
│  └──────────────────────────────────────────────────┘   │
│                   ↓                                      │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Pipeline Stage 4: Deploy to Kubernetes (NEW!)    │   │
│  │ - Update YAML with new image tag                │   │
│  │ - kubectl apply -f deployment.yaml              │   │
│  │ - Result: Service running in Kubernetes!        │   │
│  └──────────────────────────────────────────────────┘   │
│                   ↓                                      │
└───────────────────┼──────────────────────────────────────┘
                   │
                   ↓
┌─────────────────────────────────────────────────────────┐
│              Kubernetes Cluster (Ubuntu VM)             │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Deployment: learning-service                     │   │
│  │ Image: learning-service:v1.2.3                  │   │
│  │ Replicas: 3                                      │   │
│  │ ┌─────────────┬─────────────┬─────────────┐     │   │
│  │ │  Pod 1      │  Pod 2      │  Pod 3      │     │   │
│  │ │ (running)   │ (running)   │ (running)   │     │   │
│  │ └─────────────┴─────────────┴─────────────┘     │   │
│  │                                                  │   │
│  │ Service: learning-service:8092                  │   │
│  │ (Load balancers traffic across 3 pods)         │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## Step-by-Step: Integrate Kubernetes with Your Jenkins Pipelines

### Step 1: Prepare Your Kubernetes Cluster (Already Done ✓)

You already have:
- ✅ Ubuntu VM
- ✅ Kubernetes installed
- ✅ YAML manifests created (in `k8s/` folder)

### Step 2: Configure Jenkins to Access Kubernetes

The Jenkins instance needs to authenticate with the Kubernetes cluster.

#### Option A: Using kubeconfig (Recommended)

**On your Ubuntu VM (where Kubernetes is):**

```bash
# Get kubeconfig file
cat ~/.kube/config
```

This file contains credentials to access Kubernetes. You'll add it to Jenkins.

**In Jenkins UI:**

1. Go to **Manage Jenkins** → **Manage Credentials**
2. Click **System** → **Global credentials**
3. Click **Add Credentials**
4. Choose: **Kubernetes configuration (kubeconfig)**
5. Paste the kubeconfig content
6. Click **Create**

#### Option B: Using Service Account (More Secure for Production)

Better approach for production. Create a service account with limited permissions:

```bash
# Create service account for Jenkins
kubectl create serviceaccount jenkins -n smartek
kubectl create clusterrolebinding jenkins-admin --clusterrole=cluster-admin --serviceaccount=smartek:jenkins

# Get token
kubectl describe secret $(kubectl get secret -n smartek | grep jenkins-token | awk '{print $1}') -n smartek
```

### Step 3: Create Jenkins Deployments for Each Service

For each microservice (learning, skill-evidence, etc.), create a Jenkins pipeline with deployment stage.

#### Example: Learning Service Pipeline

**File: `k8s/learning-service-deployment.yaml`**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: learning-service
  namespace: smartek
spec:
  replicas: 3
  selector:
    matchLabels:
      app: learning-service
  template:
    metadata:
      labels:
        app: learning-service
    spec:
      containers:
      - name: learning-service
        image: your-registry/smartek/learning-service:LATEST_TAG  # Jenkins will replace this
        ports:
        - containerPort: 8092
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:mysql://mysql-service:3306/learning_db?createDatabaseIfNotExist=true"
        # ... rest of configuration
```

### Step 4: Modify Your Jenkins Pipeline

Your current Jenkinsfile probably looks like this:

```groovy
// BEFORE: Local/Manual Deployment
pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t learning-service:${BUILD_NUMBER} .'
            }
        }
        stage('Push to Registry') {
            steps {
                sh 'docker push myregistry/smartek/learning-service:${BUILD_NUMBER}'
            }
        }
        // Stage 4: Deploy to VM manually? 🤷‍♂️
    }
}
```

**AFTER: With Kubernetes**

```groovy
pipeline {
    agent any
    
    environment {
        REGISTRY = 'myregistry'
        IMAGE_NAME = 'learning-service'
        BUILD_TAG = "${BUILD_NUMBER}"
        DOCKER_IMAGE = "${REGISTRY}/smartek/${IMAGE_NAME}:${BUILD_TAG}"
        KUBECONFIG = credentials('kubeconfig-credentials')  // Jenkins credential
    }
    
    stages {
        stage('Build') {
            steps {
                script {
                    sh '''
                        cd Backend/learning
                        mvn clean package -DskipTests
                    '''
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    sh '''
                        cd Backend/learning
                        docker build -t ${DOCKER_IMAGE} .
                    '''
                }
            }
        }
        
        stage('Push to Registry') {
            steps {
                script {
                    sh '''
                        docker push ${DOCKER_IMAGE}
                        echo "Image pushed: ${DOCKER_IMAGE}"
                    '''
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    // NEW STAGE! 🎉
                    sh '''
                        # Update the deployment YAML with new image tag
                        sed -i "s|image:.*learning-service.*|image: ${DOCKER_IMAGE}|g" k8s/04-microservices/services-part2.yaml
                        
                        # Apply to Kubernetes
                        kubectl apply -f k8s/04-microservices/services-part2.yaml -n smartek
                        
                        # Wait for rollout to complete
                        kubectl rollout status deployment/learning-service -n smartek --timeout=5m
                        
                        echo "✓ Learning service deployed successfully!"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}
```

### Step 5: Create Deployment YAML for Each Service

Create separate YAML files for services that have their own pipelines:

**File: `k8s/04-microservices/learning-deployment.yaml`**

```yaml
apiVersion: v1
kind: Service
metadata:
  name: learning-service
  namespace: smartek
spec:
  type: ClusterIP
  ports:
  - port: 8092
    targetPort: 8092
  selector:
    app: learning-service
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: learning-service
  namespace: smartek
spec:
  replicas: 2
  selector:
    matchLabels:
      app: learning-service
  strategy:
    type: RollingUpdate  # Zero-downtime deployment
    rollingUpdate:
      maxSurge: 1        # Create 1 extra pod during update
      maxUnavailable: 0  # Keep all pods running
  template:
    metadata:
      labels:
        app: learning-service
    spec:
      containers:
      - name: learning-service
        image: your-registry/smartek/learning-service:latest
        ports:
        - containerPort: 8092
        env:
        - name: SERVER_PORT
          value: "8092"
        - name: SPRING_APPLICATION_NAME
          value: "learning-service"
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: smartek-db-credentials
              key: DATABASE_URL
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          value: "http://eureka-server:8761/eureka/"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8092
          initialDelaySeconds: 40
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8092
          initialDelaySeconds: 20
          periodSeconds: 5
```

**Similar file: `k8s/04-microservices/skill-evidence-deployment.yaml`**

(Same structure, just change port to 8091 and name to skill-evidence-service)

### Step 6: Update Your Existing Pipelines

Your two working pipelines (learning and skill-evidence) need one change:

**Add this stage to BOTH pipelines:**

```groovy
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh '''
                        # For learning service
                        sed -i "s|image:.*learning-service.*|image: ${DOCKER_IMAGE}|g" k8s/04-microservices/services-part2.yaml
                        kubectl apply -f k8s/04-microservices/services-part2.yaml -n smartek
                        kubectl rollout status deployment/learning-service -n smartek --timeout=5m
                    '''
                }
            }
        }
```

## Complete Deployment Flow

### First Time Setup
```bash
# 1. Setup Kubernetes (Ubuntu VM) - Already Done ✓
kubectl apply -f k8s/01-namespace/namespace.yaml
kubectl apply -f k8s/02-secrets-configmaps/
kubectl apply -f k8s/03-database/mysql-statefulset.yaml
kubectl apply -f k8s/04-microservices/eureka-server.yaml
# ... apply all base services

# 2. Configure Jenkins with kubeconfig
# (Done via Jenkins UI)

# 3. Push your pipelines to Jenkins
# - Learning Pipeline
# - Skill Evidence Pipeline
```

### Every Deployment (After This)
```
Developer pushes code → Jenkins builds → Jenkins pushes image → Jenkins deploys to K8s → Kubernetes handles the rest!
```

## Monitoring: See Your Services in Action

```bash
# Watch pods
kubectl get pods -n smartek -w

# View logs
kubectl logs -n smartek deployment/learning-service -f

# Port forward to access
kubectl port-forward svc/learning-service -n smartek 8092:8092

# Check Eureka (service discovery)
kubectl port-forward svc/eureka-server -n smartek 8761:8761
# Visit http://localhost:8761
```

## Troubleshooting Kubernetes Deployments

### Pod not starting?
```bash
# Check pod status and events
kubectl describe pod -n smartek <pod-name>

# View logs
kubectl logs -n smartek <pod-name>

# Check if image pull is failing
kubectl describe pod -n smartek <pod-name> | grep -A 10 "Events:"
```

### Can't connect to MySQL?
```bash
# Test connectivity from pod
kubectl exec -it -n smartek <pod-name> -- \
  mysql -h mysql-service -u smartek_user -p -e "SELECT 1;"
```

### Service not registered in Eureka?
```bash
# Check if service pods are running
kubectl get pods -n smartek -l app=learning-service

# Check service logs for Eureka registration
kubectl logs -n smartek deployment/learning-service | grep -i eureka
```

## Benefits Recap

| Aspect | Before (Manual) | With Kubernetes |
|--------|-----------------|-----------------|
| **Deployment** | SSH + manual commands | Jenkins → Kubectl → Done |
| **Scaling** | Stop/start containers manually | `kubectl scale --replicas=5` |
| **Crash Recovery** | Manual restart | Automatic |
| **Updates** | Downtime required | Zero-downtime rolling updates |
| **Resource Limits** | Hope they don't hog resources | Enforced limits |
| **Monitoring** | Check each service manually | Centralized dashboard |
| **Load Balancing** | Manual | Automatic |

## Next Steps

1. ✅ You've already created all YAML files
2. 🔄 Integrate Jenkins with Kubernetes (add kubeconfig credential)
3. 🔄 Update learning & skill-evidence pipelines with deployment stage
4. 🔄 Test: push code → Jenkins → Kubernetes → Verify deployment
5. 🔄 Create pipelines for remaining services (auth, event, etc.)
6. 📊 Setup monitoring and alerts

Would you like me to create the specific Jenkins pipeline scripts for your learning and skill-evidence services?

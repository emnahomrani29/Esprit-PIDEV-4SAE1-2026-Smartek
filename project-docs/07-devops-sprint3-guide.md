# SMARTEK — Sprint 3 DevOps Complete Guide

> **Scope:** Deploy the SMARTEK platform (Infra + Auth Service + Offers Service + Frontend) on a **KubeAdm cluster** inside a VM, with a full **CI/CD Jenkins pipeline**, **SonarQube**, **Nexus**, **Vault**, **Prometheus**, and **Grafana**.

---

## ⚠️ IMPORTANT: 8GB RAM / 2 vCPU Setup

This guide is **optimized for 8GB RAM and 2 vCPU**. It is tight but doable for a demo.

### What was optimized:
- **Memory limits** added to all Docker containers (Jenkins, SonarQube, Nexus, Vault)
- **Memory & CPU limits** added to all Kubernetes pods (MySQL, Eureka, services, monitoring)
- **SonarQube** tuned down to use ~1.5GB max (was ~3GB)
- **Nexus** tuned down to use ~1GB max

### Tips to survive 8GB:
1. **Start services one by one** — don't launch everything simultaneously
2. **If a pod gets `OOMKilled`** — increase its memory limit slightly or reduce another service's limit
3. **During build**, Jenkins + Maven uses a lot of RAM. Close any browser tabs on the VM host
4. **If the VM freezes** — increase VM swap:
   ```bash
   sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
   ```
5. **For evaluation day** — if you only need to show the running app, you can temporarily stop SonarQube or Nexus:
   ```bash
   docker stop sonarqube sonar-db nexus
   ```

### Recommended upgrade (if possible):
If you can bump to **12–16GB RAM**, the experience will be much smoother. But **8GB works** — just follow the limits.

---

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [VM Specifications & OS Setup](#2-vm-specifications--os-setup)
3. [Install Docker on the VM](#3-install-docker-on-the-vm)
4. [Set Up the KubeAdm Cluster](#4-set-up-the-kubeadm-cluster)
5. [Deploy DevOps Tools (Jenkins, SonarQube, Nexus, Vault)](#5-deploy-devops-tools-jenkins-sonarqube-nexus-vault)
6. [Configure Jenkins](#6-configure-jenkins)
7. [Configure SonarQube](#7-configure-sonarqube)
8. [Configure Nexus](#8-configure-nexus)
9. [Configure Vault](#9-configure-vault)
10. [Configure GitHub Webhooks](#10-configure-github-webhooks)
11. [CI/CD Pipelines](#11-cicd-pipelines)
12. [Deploy to Kubernetes](#12-deploy-to-kubernetes)
13. [Monitoring — Prometheus & Grafana](#13-monitoring--prometheus--grafana)
14. [Access Cheat Sheet](#14-access-cheat-sheet)
15. [Troubleshooting](#15-troubleshooting)

---

## 1. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              UBUNTU 22.04 VM                                │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │              KUBEADM CLUSTER (inside VM)                            │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐  │   │
│  │  │  MySQL   │ │  Eureka  │ │  Config  │ │ API GW   │ │ Offers   │  │   │
│  │  │          │ │  Server  │ │  Server  │ │          │ │ Service  │  │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘  │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐               │   │
│  │  │ Frontend │ │Prometheus│ │ Grafana  │ │  (Auth)  │               │   │
│  │  │          │ │          │ │          │ │ (future) │               │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘               │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐                       │
│  │ Jenkins  │ │SonarQube │ │  Nexus   │ │  Vault   │  ← Docker containers  │
│  │  :8080   │ │  :9000   │ │  :8081   │ │  :8200   │    on VM host         │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. VM Specifications & OS Setup

### Recommended Specs
| Resource | Minimum | Recommended |
|----------|---------|-------------|
| **RAM** | 8 GB | **16 GB** |
| **vCPU** | 2 | **4** |
| **Disk** | 40 GB | **60 GB SSD** |
| **OS** | — | **Ubuntu 22.04 LTS Server** |

### 2.1 Install Ubuntu 22.04
1. Download Ubuntu 22.04 LTS Server ISO.
2. Create a new VM in VirtualBox / VMware / cloud provider.
3. During setup:
   - Create user: `smartek` (or your name)
   - Enable OpenSSH server.
   - Let it install updates automatically.

### 2.2 Initial VM Configuration
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install essentials
sudo apt install -y curl wget git vim net-tools openssh-server

# Set static IP (optional but recommended)
# Edit /etc/netplan/00-installer-config.yaml
sudo nano /etc/netplan/00-installer-config.yaml
```

Example netplan config for static IP:
```yaml
network:
  version: 2
  ethernets:
    eth0:
      dhcp4: no
      addresses:
        - 192.168.1.100/24
      routes:
        - to: default
          via: 192.168.1.1
      nameservers:
        addresses:
          - 8.8.8.8
          - 1.1.1.1
```

Apply:
```bash
sudo netplan apply
```

> **Note:** Replace `192.168.1.100` with your actual VM IP. Update `VM_IP` in all pipeline files accordingly.

---

## 3. Install Docker on the VM

```bash
# Remove old versions
sudo apt remove docker docker-engine docker.io containerd runc

# Install Docker
sudo apt update
sudo apt install -y ca-certificates gnupg lsb-release
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Add your user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Verify
docker --version
```

---

## 4. Set Up the KubeAdm Cluster

### 4.1 Install kubeadm, kubelet, kubectl

```bash
# Disable swap (required)
sudo swapoff -a
sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab

# Install dependencies
sudo apt install -y apt-transport-https ca-certificates curl gnupg

# Add Kubernetes apt repo
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.29/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg

echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.29/deb/ /' | \
  sudo tee /etc/apt/sources.list.d/kubernetes.list

sudo apt update
sudo apt install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl

# Enable kernel modules
sudo modprobe overlay
sudo modprobe br_netfilter

sudo tee /etc/sysctl.d/kubernetes.conf <<EOF
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward = 1
EOF

sudo sysctl --system
```

### 4.2 Configure containerd

```bash
sudo mkdir -p /etc/containerd
sudo containerd config default | sudo tee /etc/containerd/config.toml

# Set SystemdCgroup = true
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' /etc/containerd/config.toml

sudo systemctl restart containerd
sudo systemctl enable containerd
```

### 4.3 Initialize the Cluster (Single Node)

```bash
# Initialize control plane
sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --apiserver-advertise-address=<VM_IP>

# Example:
# sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --apiserver-advertise-address=192.168.1.100
```

After init completes:
```bash
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

### 4.4 Install CNI (Flannel)

```bash
kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
```

### 4.5 Allow Scheduling on Control Plane (Single Node)

```bash
kubectl taint nodes --all node-role.kubernetes.io/control-plane-
```

### 4.6 Verify Cluster

```bash
kubectl get nodes
kubectl get pods -n kube-system
```

Both should show `Ready` / `Running`.

---

## 5. Deploy DevOps Tools (Jenkins, SonarQube, Nexus, Vault)

All DevOps tools run as **Docker containers on the VM host** (outside K8s). This keeps Jenkins independent of cluster failures and simplifies networking.

### 5.1 Start the Tools Stack

> **Note:** `docker-compose.tools.yml` includes **memory limits** for 8GB RAM. If you have 16GB+, feel free to increase them.

```bash
cd ~/Esprit-PIDEV-4SAE1-2026-Smartek  # or wherever you cloned the repo
docker compose -f docker-compose.tools.yml up -d
```

Wait 3–5 minutes for all services to start (especially Nexus and SonarQube).

### 5.2 Verify Tools Are Running

```bash
docker ps
```

You should see containers: `jenkins`, `sonarqube`, `sonar-db`, `nexus`, `vault`.

**If a container keeps restarting**, check its memory:
```bash
docker stats --no-stream
```
If memory usage is at the limit, increase the VM RAM or adjust the `deploy.resources.limits.memory` values in `docker-compose.tools.yml`.

---

## 6. Configure Jenkins

### 6.1 First-Time Setup

1. Open `http://<VM_IP>:8080`
2. Get initial admin password:
   ```bash
   docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```
3. Install **suggested plugins**.
4. Create admin user: `admin / admin123` (or your choice).

### 6.2 Install Required Plugins

Go to **Manage Jenkins → Plugins → Available Plugins**, install:
- `Docker Pipeline`
- `Docker plugin`
- `Pipeline`
- `Git`
- `GitHub Integration`
- `SonarQube Scanner`
- `HTML Publisher`
- `Kubernetes CLI`
- `Credentials Binding`

Restart Jenkins after installation.

### 6.3 Configure Tools

Go to **Manage Jenkins → Tools**:

| Tool | Configuration |
|------|--------------|
| **JDK** | Name: `JDK17`, Install automatically → Adoptium JDK 17.0.9+9 |
| **Maven** | Name: `Maven-3.9`, Install automatically → Apache Maven 3.9.6 |
| **SonarQube Scanner** | Name: `SonarScanner`, Install automatically → latest |
| **Docker** | Name: `docker`, uncheck "Install automatically" (it uses host Docker) |

### 6.4 Configure Credentials

Go to **Manage Jenkins → Credentials → System → Global credentials**.

Add these credentials (kind = **Username with password** unless noted):

| ID | Type | Purpose |
|----|------|---------|
| `docker-hub-credentials` | Username + Password | Docker Hub login |
| `docker-hub-username` | Secret text | Docker Hub username (used in env vars) |
| `docker-hub-password` | Secret text | Docker Hub password/token |
| `nexus-credentials` | Username + Password | Nexus login |
| `sonar-token` | Secret text | SonarQube user token |
| `kubeconfig-file` | Secret file | Upload `~/.kube/config` |

> **Tip:** For Docker Hub, create an **Access Token** at https://hub.docker.com/settings/security instead of using your real password.

### 6.5 Create Jenkins Jobs

#### CI Jobs (one per service)
| Jenkins Job Name | Script Path | Trigger |
|------------------|-------------|---------|
| `smartek-auth-service-ci` | `jenkins/ci/Jenkinsfile-auth-service` | GitHub webhook (path-filtered) |
| `smartek-offers-service-ci` | `jenkins/ci/Jenkinsfile-offers-service` | GitHub webhook (path-filtered) |
| `smartek-frontend-ci` | `jenkins/ci/Jenkinsfile-frontend` | GitHub webhook (path-filtered) |

For each CI job:
1. **New Item → Pipeline → Name: `smartek-<service>-ci`**
2. Under **Pipeline**, select **Pipeline script from SCM**
3. SCM: **Git**
4. Repository URL: `https://github.com/emnahomrani29/Esprit-PIDEV-4SAE1-2026-Smartek.git`
5. Branch: `*/offers-service` (or `*/main`)
6. Script Path: `jenkins/ci/Jenkinsfile-<service>`
7. Under **Build Triggers**, check **GitHub hook trigger for GITScm polling**
8. Save.

#### CD Jobs (one per service + infra)
| Jenkins Job Name | Script Path | Trigger |
|------------------|-------------|---------|
| `smartek-auth-service-cd` | `jenkins/cd/Jenkinsfile-auth-service` | Triggered by `smartek-auth-service-ci` |
| `smartek-offers-service-cd` | `jenkins/cd/Jenkinsfile-offers-service` | Triggered by `smartek-offers-service-ci` |
| `smartek-frontend-cd` | `jenkins/cd/Jenkinsfile-frontend` | Triggered by `smartek-frontend-ci` |
| `smartek-infra-cd` | `jenkins/cd/Jenkinsfile-infra` | Manual (run once) |

For each CD job:
1. **New Item → Pipeline → Name: `smartek-<service>-cd`**
2. Under **Pipeline**, select **Pipeline script from SCM**
3. SCM: **Git**
4. Same repo and branch
5. Script Path: `jenkins/cd/Jenkinsfile-<service>`
6. Save.

### 6.6 Verify kubectl inside Jenkins Container

The Jenkins container needs `kubectl` to deploy to the cluster. We mounted the host's `kubectl` into the container via `docker-compose.tools.yml`. Verify it works:

```bash
docker exec jenkins kubectl version --client
```

If you see an error like `kubectl: not found`, the host path might differ. Fix it:
```bash
# Find kubectl on your host
which kubectl

# If it's at /usr/bin/kubectl (default from apt), update the mount in docker-compose.tools.yml:
# volumes:
#   - /usr/bin/kubectl:/usr/local/bin/kubectl:ro

# Then restart Jenkins
docker compose -f docker-compose.tools.yml restart jenkins
```

---

## 7. Configure SonarQube

1. Open `http://<VM_IP>:9000`
2. Default login: `admin / admin`
3. Change password when prompted (e.g., `admin123`).

### 7.1 Create Project
1. Click **Projects → Create Project → Manual**
2. Project key: `offers-service`
3. Display name: `Offers Service`
4. Setup: **Locally**
5. Generate token, name it `jenkins-token`
6. Copy the token → save it in Jenkins credentials as `sonar-token`.

### 7.2 Quality Gate (Optional but Recommended)
1. Go to **Quality Gates**
2. Create or edit a gate:
   - Coverage: `> 60%`
   - Duplicated Lines: `< 10%`
   - Critical Issues: `0`

---

## 8. Configure Nexus

1. Open `http://<VM_IP>:8081`
2. Get initial password:
   ```bash
   docker exec nexus cat /nexus-data/admin.password
   ```
3. Login as `admin` with that password, then set a new password.
4. Enable anonymous access (for simplicity) or configure roles.

### 8.1 Create Maven Repository
1. Go to **Settings → Repositories → Create repository**
2. Select **maven2 (hosted)**
3. Name: `smartek-releases`
4. Version policy: **Release**
5. Deployment policy: **Allow redeploy**
6. Save.

### 8.2 Create Docker Registry (Optional)
If you later want to push Docker images to Nexus instead of Docker Hub:
1. Create repository → **docker (hosted)**
2. Name: `smartek-docker`
3. HTTP port: `8082`
4. Enable Docker V1 API: **Yes**
5. Save.
6. Update Jenkins to use `localhost:8082` as registry.

> **For this Sprint 3 guide we use Docker Hub**, so this step is optional.

---

## 9. Configure Vault

1. Open `http://<VM_IP>:8200`
2. Vault is running in **dev mode** with root token: `smartek-root-token`
3. Login with **Token** method, token: `smartek-root-token`

### 9.1 Enable KV Secrets Engine
1. Go to **Secrets Engines → Enable new engine**
2. Select **KV** (version 2)
3. Path: `secret`
4. Save.

### 9.2 Store Secrets

Create the following secrets at `secret/smartek/docker-hub`:
```
username = your-dockerhub-username
password = your-dockerhub-access-token
```

Create at `secret/smartek/mysql`:
```
root-password = root
```

Create at `secret/smartek/nexus`:
```
username = admin
password = your-nexus-password
```

### 9.3 Vault + Jenkins Integration (Optional Enhancement)

If you want Jenkins to pull secrets from Vault instead of storing them in Jenkins credentials:
1. Install **HashiCorp Vault Plugin** in Jenkins.
2. Go to **Manage Jenkins → Configure System → Vault**
3. Vault URL: `http://vault:8200`
4. Authentication: Token, Token: `smartek-root-token`
5. In pipeline, use `withVault` block.

For this Sprint 3, **Jenkins Credentials** is sufficient and more stable for demo purposes. Vault integration demonstrates secrets management architecture.

---

## 10. Configure GitHub Webhooks

### 10.1 Generate Jenkins GitHub Webhook URL

In Jenkins:
For **each CI job**, enable the webhook trigger:
1. Go to **smartek-<service>-ci → Configure**
2. Under **Build Triggers**, check **GitHub hook trigger for GITScm polling**
3. Save.

The webhook URL format is the same for all jobs:
```
http://<VM_IP>:8080/github-webhook/
```

### 10.2 Add Webhook in GitHub

1. Go to your GitHub repo → **Settings → Webhooks → Add webhook**
2. **Payload URL:** `http://<VM_IP>:8080/github-webhook/`
3. **Content type:** `application/json`
4. **Which events?** Just the `push` event
5. Save.

### 10.3 Test the Webhook

Make a small commit and push to any branch. The GitHub webhook will trigger **all CI jobs**, but each job checks if its service folder changed and skips if there are no changes. Only the affected service(s) will actually build and deploy.

> **If your VM is behind NAT** (e.g., VirtualBox on your laptop), GitHub cannot reach `http://<VM_IP>:8080`. Use one of these workarounds:
> - **ngrok:** `ngrok http 8080` → gives a public URL
> - **Tailscale:** Put both your laptop and VM on the same Tailnet
> - **Manual trigger:** Skip webhooks and run Jenkins jobs manually (the evaluator will likely accept manual trigger if you explain the NAT limitation)

---

## 11. CI/CD Pipelines

We use **per-service CI/CD pipelines**. Each service has its own Jenkinsfile. This ensures that when you edit one microservice, only that service rebuilds and redeploys.

| File | Purpose | Trigger |
|------|---------|---------|
| `jenkins/ci/Jenkinsfile-auth-service` | Detect changes, build, test, push auth-service image | Git push webhook (path-filtered) |
| `jenkins/ci/Jenkinsfile-offers-service` | Detect changes, build, test, SonarQube, push offers-service image | Git push webhook (path-filtered) |
| `jenkins/ci/Jenkinsfile-frontend` | Detect changes, build, push frontend image | Git push webhook (path-filtered) |
| `jenkins/cd/Jenkinsfile-auth-service` | Deploy auth-service to K8s | Triggered by auth-service CI |
| `jenkins/cd/Jenkinsfile-offers-service` | Deploy offers-service to K8s | Triggered by offers-service CI |
| `jenkins/cd/Jenkinsfile-frontend` | Deploy frontend to K8s | Triggered by frontend CI |
| `jenkins/cd/Jenkinsfile-infra` | Deploy all infrastructure (MySQL, Eureka, Config, Gateway, Monitoring) | Manual (run once) |

### 11.1 CI Pipeline Steps (example: `Jenkinsfile-offers-service`)

1. **Detect Changes**: Check `git diff` for `Backend/offers-service/`. Skip if no changes.
2. **Build & Test** (`mvn clean verify`)
3. **Publish JaCoCo** coverage report in Jenkins UI
4. **SonarQube Analysis** (bugs, coverage, code smells)
5. **Publish JAR** to Nexus Maven repository
6. **Build & Push Docker Image** to Docker Hub
7. **Update K8s manifest** with the new image tag
8. **Trigger CD** job automatically

> **Auth Service CI** follows the same pattern but skips SonarQube (optional).  
> **Frontend CI** only builds and pushes the Docker image (no Maven).

### 11.2 CD Pipeline Steps (example: `Jenkinsfile-offers-service`)

1. **Checkout** manifests from SCM
2. **Deploy** only the Offers Service manifests to K8s
3. **Wait** for rollout (`kubectl rollout status`)
4. **Smoke Test**: curl health endpoint

> **Infra CD** (`Jenkinsfile-infra`) deploys MySQL, Eureka, Config Server, API Gateway, Prometheus, and Grafana. Run this **once** before any app CD jobs.

### 11.3 Important: Update VM_IP in Pipelines

Before running, edit both Jenkinsfiles and replace `192.168.1.100` with your actual VM IP:

```bash
# In repo root
sed -i 's/192.168.1.100/YOUR_VM_IP/g' jenkins/ci/Jenkinsfile-auth-service
sed -i 's/192.168.1.100/YOUR_VM_IP/g' jenkins/ci/Jenkinsfile-offers-service
```

Also update `k8s/infra/api-gateway/api-gateway-deployment.yaml`:
```bash
sed -i 's/<VM_IP>/YOUR_VM_IP/g' k8s/infra/api-gateway/api-gateway-deployment.yaml
```

### 11.4 Important: Update Docker Hub User in K8s Manifests

Replace `<DOCKER_HUB_USER>` in all app deployments:
```bash
sed -i 's/<DOCKER_HUB_USER>/your-dockerhub-username/g' k8s/apps/auth-service/auth-service-deployment.yaml
sed -i 's/<DOCKER_HUB_USER>/your-dockerhub-username/g' k8s/apps/offers-service/offers-service-deployment.yaml
sed -i 's/<DOCKER_HUB_USER>/your-dockerhub-username/g' k8s/apps/frontend/frontend-deployment.yaml
```

---

## 12. Deploy to Kubernetes

> **All K8s Deployments include memory & CPU limits** optimized for 8GB RAM. If a pod is `OOMKilled`, you can increase its limit in the respective YAML file.

### 12.0 First-Time Deployment Order (IMPORTANT)

**On a fresh cluster, deploy in this exact order:**

1. **Infra first** — run `smartek-infra-cd` in Jenkins (manual, one time)
2. **Then apps** — run each app CD once (or let CI trigger them after first push)
3. **After that**, only edited services redeploy automatically

> **Why this order?** MySQL and Eureka must be running before Auth/Offers services start, or they will crash-loop.

### 12.1 Manual Deploy (First Time or Debug)

```bash
# Ensure kubectl points to your cluster
kubectl get nodes

# Apply everything in order
cd ~/Esprit-PIDEV-4SAE1-2026-Smartek

kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/infra/mysql/
kubectl apply -f k8s/infra/eureka/
kubectl apply -f k8s/infra/config-server/
kubectl apply -f k8s/infra/api-gateway/
kubectl apply -f k8s/apps/offers-service/
kubectl apply -f k8s/apps/frontend/
kubectl apply -f k8s/monitoring/
```

### 12.2 Verify Deployment

```bash
# Watch pods come up
kubectl get pods -n smartek -w

# Check all resources
kubectl get all -n smartek

# Check logs if something fails
kubectl logs -n smartek deployment/offers-service --tail=50
kubectl logs -n smartek deployment/api-gateway --tail=50
```

### 12.3 Access Services from Your Browser (Host Machine)

| Service | URL |
|---------|-----|
| Frontend | `http://<VM_IP>:30420` |
| API Gateway | `http://<VM_IP>:30090` |
| Grafana | `http://<VM_IP>:30092` |
| Prometheus | `http://<VM_IP>:30091` |
| Eureka (via port-forward) | `kubectl port-forward -n smartek svc/eureka-server 8761:8761` → `http://localhost:8761` |

---

## 13. Monitoring — Prometheus & Grafana

### 13.1 Prometheus
- Already configured via `prometheus-configmap.yaml`
- Scrapes: Prometheus itself, Eureka, API Gateway, Offers Service, Config Server
- Access: `http://<VM_IP>:30091`
- Go to **Status → Targets** to verify all endpoints are UP.

### 13.2 Grafana
- Access: `http://<VM_IP>:30092`
- Login: `admin / admin`

#### Add Prometheus Data Source
1. **Configuration → Data Sources → Add data source**
2. Select **Prometheus**
3. URL: `http://prometheus:9090` (inside cluster) or `http://<VM_IP>:30091`
4. Save & Test.

#### Import Dashboards
1. **Create → Import**
2. Use official dashboard ID `4701` (JVM Micrometer) or `1443` (Spring Boot Statistics)
3. Select your Prometheus data source.
4. Import.

You should now see JVM metrics, memory usage, HTTP requests, etc.

---

## 14. Access Cheat Sheet

| Tool / Service | URL / Command | Credentials |
|----------------|---------------|-------------|
| **Jenkins** | `http://<VM_IP>:8080` | admin / admin123 |
| **SonarQube** | `http://<VM_IP>:9000` | admin / admin123 |
| **Nexus** | `http://<VM_IP>:8081` | admin / *your-password* |
| **Vault** | `http://<VM_IP>:8200` | Token: `smartek-root-token` |
| **K8s Dashboard** | `kubectl proxy` → `http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/` | Token auth |
| **Frontend** | `http://<VM_IP>:30420` | — |
| **API Gateway** | `http://<VM_IP>:30090` | — |
| **Grafana** | `http://<VM_IP>:30092` | admin / admin |
| **Prometheus** | `http://<VM_IP>:30091` | — |
| **Eureka** | `kubectl port-forward -n smartek svc/eureka-server 8761:8761` | — |

---

## 15. Troubleshooting

### Jenkins cannot reach K8s cluster
```bash
# Copy kubeconfig into Jenkins container
docker cp ~/.kube/config jenkins:/var/jenkins_home/.kube/config
docker exec -u root jenkins chown jenkins:jenkins /var/jenkins_home/.kube/config
```

### Docker Hub push fails (denied)
- Verify `docker-hub-credentials` in Jenkins is correct.
- Use a Docker Hub **Access Token**, not your account password.

### SonarQube analysis fails
- Check `sonar-token` is saved in Jenkins.
- Verify SonarQube URL (`http://<VM_IP>:9000`) is reachable from Jenkins container:
  ```bash
  docker exec jenkins curl -sf http://<VM_IP>:9000/api/system/status
  ```

### K8s pods stuck in `Pending`
```bash
kubectl describe pod -n smartek <pod-name>
# Usually: PVC not bound, image not found, or resource limits
```

### K8s pods stuck in `ImagePullBackOff`
- The images `<DOCKER_HUB_USER>/smartek-auth-service:latest` or `<DOCKER_HUB_USER>/smartek-offers-service:latest` don't exist on Docker Hub yet.
- Run the CI pipeline first to build and push images.
- Or for local testing, use `imagePullPolicy: IfNotPresent` and pre-load images into the cluster.

### MySQL pod crashes
```bash
kubectl logs -n smartek deployment/mysql
# Check if PVC is bound:
kubectl get pvc -n smartek
```

### Offers Service cannot connect to MySQL
- Verify MySQL is ready: `kubectl get pods -n smartek -l app=mysql`
- Check JDBC URL in ConfigMap: should be `jdbc:mysql://mysql:3306/offers_db?...`
- DNS resolution works because both are in the `smartek` namespace.

### Grafana shows "No Data"
- Verify Prometheus target is UP at `http://<VM_IP>:30091/targets`
- Check if `micrometer-registry-prometheus` dependency is present in offers-service (already added).
- Ensure actuator endpoint `/actuator/prometheus` is accessible inside the cluster.

---

## Quick Start Summary (For Eval Day)

```bash
# 1. Start DevOps tools
docker compose -f docker-compose.tools.yml up -d

# 2. Verify cluster
kubectl get nodes

# 3. Trigger CI pipeline (via GitHub push or manually in Jenkins)
# → Only the affected service CI job runs (path-based detection)
# → Builds, tests, pushes Docker image
# → Automatically triggers the matching CD job

# 4. Verify deployment
kubectl get pods -n smartek

# 5. Access application
# Frontend:     http://<VM_IP>:30420
# API Gateway:  http://<VM_IP>:30090
# Grafana:      http://<VM_IP>:30092
# SonarQube:    http://<VM_IP>:9000
# Jenkins:      http://<VM_IP>:8080
```

---

## Files You Need to Modify Before First Run

| File | What to Change |
|------|----------------|
| `jenkins/ci/Jenkinsfile-auth-service` | Replace `192.168.1.100` with your VM IP |
| `jenkins/ci/Jenkinsfile-offers-service` | Replace `192.168.1.100` with your VM IP |
| `jenkins/cd/Jenkinsfile-auth-service` | Replace `192.168.1.100` with your VM IP |
| `jenkins/cd/Jenkinsfile-offers-service` | Replace `192.168.1.100` with your VM IP |
| `jenkins/cd/Jenkinsfile-infra` | Replace `192.168.1.100` with your VM IP |
| `k8s/infra/api-gateway/api-gateway-deployment.yaml` | Replace `<VM_IP>` with your VM IP |
| `k8s/apps/auth-service/auth-service-deployment.yaml` | Replace `<DOCKER_HUB_USER>` with your Docker Hub username |
| `k8s/apps/offers-service/offers-service-deployment.yaml` | Replace `<DOCKER_HUB_USER>` with your Docker Hub username |
| `k8s/apps/frontend/frontend-deployment.yaml` | Replace `<DOCKER_HUB_USER>` with your Docker Hub username |

---

## Evaluation Grid Mapping

| Grid Criterion | How We Address It |
|----------------|-------------------|
| **Jenkins & Webhooks (Front & Back)** | Per-service Jenkins pipelines with path-based change detection. Webhook triggers CI on Git push; only the affected service(s) rebuild and redeploy. Backend (`auth-service`, `offers-service`) and frontend (`angular-app`) images are built and pushed independently. |
| **Docker Orchestration — KubeAdm** | Full stack (MySQL, Eureka, Config, Gateway, Offers, Frontend, Prometheus, Grafana) deployed on kubeadm cluster. |
| **SonarQube Integration** | SonarQube analysis runs in CI pipeline with JaCoCo coverage report for offers-service. |
| **Grafana Dashboard (Prometheus)** | Prometheus scrapes all services. Grafana dashboard imported and active. |
| **Vault (Secrets)** | Vault container running. Secrets stored and documented. Jenkins (or K8s) consumes them securely. |
| **Nexus (Artifacts)** | Maven JARs published to Nexus `smartek-releases` repository. |

# SMARTEK — Sprint 3 DevOps Complete Guide

> **Scope:** Deploy the SMARTEK platform (Infra + Auth Service + Offers Service + Frontend) on a **KubeAdm cluster** on a cloud server, with a full **CI/CD Jenkins pipeline**, **SonarQube**, **Nexus**, **Vault**, **Prometheus**, and **Grafana**.

---

## ⚠️ IMPORTANT: Cloud Server Setup

This guide is written for a **cloud server** (e.g., Hetzner, AWS, DigitalOcean) with a **public IP address**.

**Why cloud instead of local VM?**
- No VirtualBox/VMware slowness
- No NAT/port-forwarding headaches
- GitHub webhooks work out of the box (public IP)
- 16GB RAM + 4 vCPU = smooth experience
- You can access everything from your PC browser directly

> **Security note:** This server will expose Jenkins, SonarQube, Nexus, and your app to the internet. We will set up a basic firewall, but **shut down the server after your evaluation** — do not leave it running.

---

---

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Server Setup (Hetzner Cloud)](#2-server-setup-hetzner-cloud)
3. [Install Docker on the Server](#3-install-docker-on-the-server)
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
│                           UBUNTU 22.04 CLOUD SERVER                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │              KUBEADM CLUSTER (on server)                            │   │
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
│  │  :8080   │ │  :9000   │ │  :8081   │ │  :8200   │    on server host     │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘                       │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Server Setup (Hetzner Cloud)

### Recommended Specs
| Resource | Spec |
|----------|------|
| **RAM** | **16 GB** |
| **vCPU** | **4** |
| **Disk** | **160 GB** |
| **OS** | **Ubuntu 22.04 LTS** |
| **Location** | Any (closest to you) |

> Example Hetzner server: **CCX23** (4 AMD vCPU, 16GB RAM, 160GB NVMe)

---

### Step 0: Generate an SSH Key (on your PC)

If you don't have an SSH key yet, generate one on your **local PC** (not the server):

```bash
# Windows (Git Bash / WSL / PowerShell)
ssh-keygen -t ed25519 -C "your-email@example.com"

# Press Enter to save to default location (~/.ssh/id_ed25519)
# Press Enter twice for no passphrase (simpler for demos)
```

Copy the **public key** to your clipboard:
```bash
# macOS
pbcopy < ~/.ssh/id_ed255.pub

# Windows (Git Bash)
cat ~/.ssh/id_ed25519.pub | clip

# Linux
xclip -sel clip < ~/.ssh/id_ed25519.pub
```

---

### Step 1: Create the Server on Hetzner

1. Go to [Hetzner Cloud Console](https://console.hetzner.cloud/)
2. **Projects** → Select or create a project
3. Click **Add Server**
4. **Location:** Pick closest to you (e.g., Nuremberg, Falkenstein, Helsinki)
5. **Image:** `Ubuntu 22.04`
6. **Type:** `CCX23` (4 vCPU, 16GB RAM) or equivalent
7. **SSH Key:** Click **Add SSH Key** → paste your public key → name it `my-pc`
8. **Name:** `smartek-devops`
9. Click **Create & Buy Now**

Wait ~1 minute for the server to be ready.

---

### Step 2: Connect to Your Server

Your server now has a **public IP** (e.g., `78.46.123.45`).

From your PC:
```bash
ssh root@YOUR_SERVER_IP
```

Example:
```bash
ssh root@78.46.123.45
```

> **First time?** Type `yes` when asked about host authenticity.

---

### Step 3: Initial Server Configuration

Once logged in as `root`:

```bash
# Update system
apt update && apt upgrade -y

# Install essentials
apt install -y curl wget git vim net-tools ufw

# Create a non-root user (recommended)
adduser smartek
usermod -aG sudo smartek

# Copy SSH key to new user
mkdir -p /home/smartek/.ssh
cp /root/.ssh/authorized_keys /home/smartek/.ssh/
chown -R smartek:smartek /home/smartek/.ssh
chmod 700 /home/smartek/.ssh
chmod 600 /home/smartek/.ssh/authorized_keys

# Switch to new user
su - smartek
```

From now on, use `smartek` user:
```bash
ssh smartek@YOUR_SERVER_IP
```

> **Shortcut:** We provide `scripts/setup-server.sh` in the repo that automates Docker, kubeadm, and config replacement. You can run it after cloning:
> ```bash
> chmod +x scripts/setup-server.sh
> ./scripts/setup-server.sh YOUR_SERVER_IP yourdockerhubuser
> ```

---

### Step 4: Note Your Server IP

Your server IP is the **public IPv4** shown in the Hetzner console.  
Example: `78.46.123.45`

This is your `<SERVER_IP>` / `<SERVER_IP>` for the rest of the guide.  
Replace all `192.168.1.100` placeholders with this IP.

---

---

## 3. Install Docker on the Server

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

## 3.5 Configure Firewall (UFW)

Your server is on the public internet. Open only the ports you need:

```bash
# Allow SSH (so you don't lock yourself out)
sudo ufw allow 22/tcp

# Allow Jenkins
sudo ufw allow 8080/tcp

# Allow SonarQube
sudo ufw allow 9000/tcp

# Allow Nexus
sudo ufw allow 8081/tcp

# Allow Vault
sudo ufw allow 8200/tcp

# Allow Kubernetes NodePorts (frontend, gateway, grafana, prometheus)
sudo ufw allow 30090/tcp
sudo ufw allow 30420/tcp
sudo ufw allow 30091/tcp
sudo ufw allow 30092/tcp

# Enable firewall
sudo ufw enable

# Check status
sudo ufw status
```

> **Hetzner Firewall (optional extra layer):**
> You can also add a firewall in the Hetzner Console → `Firewall` → Create rules for the same ports. This blocks traffic before it even reaches your server.

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
sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --apiserver-advertise-address=<SERVER_IP>

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

All DevOps tools run as **Docker containers on the server host** (outside K8s). This keeps Jenkins independent of cluster failures and simplifies networking.

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
With 16GB RAM you shouldn't hit limits, but if any container restarts, check memory with `docker stats` and adjust `deploy.resources.limits.memory` in `docker-compose.tools.yml`.

---

## 6. Configure Jenkins

### 6.1 First-Time Setup

1. Open `http://<SERVER_IP>:8080`
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

1. Open `http://<SERVER_IP>:9000`
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

1. Open `http://<SERVER_IP>:8081`
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

1. Open `http://<SERVER_IP>:8200`
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
http://<SERVER_IP>:8080/github-webhook/
```

### 10.2 Add Webhook in GitHub

1. Go to your GitHub repo → **Settings → Webhooks → Add webhook**
2. **Payload URL:** `http://<SERVER_IP>:8080/github-webhook/`
3. **Content type:** `application/json`
4. **Which events?** Just the `push` event
5. Save.

### 10.3 Test the Webhook

Make a small commit and push to any branch. The GitHub webhook will trigger **all CI jobs**, but each job checks if its service folder changed and skips if there are no changes. Only the affected service(s) will actually build and deploy.

> ✅ **With a cloud server, webhooks work automatically!** GitHub can reach your public IP directly. No ngrok or Tailscale needed.

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

### 11.3 Important: Update SERVER_IP in Pipelines

Before running, edit the Jenkinsfiles and replace `192.168.1.100` with your actual server public IP:

```bash
# In repo root
sed -i 's/192.168.1.100/YOUR_SERVER_IP/g' jenkins/ci/Jenkinsfile-auth-service
sed -i 's/192.168.1.100/YOUR_SERVER_IP/g' jenkins/ci/Jenkinsfile-offers-service
```

Also update `k8s/infra/api-gateway/api-gateway-deployment.yaml`:
```bash
sed -i 's/<SERVER_IP>/YOUR_SERVER_IP/g' k8s/infra/api-gateway/api-gateway-deployment.yaml
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
| Frontend | `http://<SERVER_IP>:30420` |
| API Gateway | `http://<SERVER_IP>:30090` |
| Grafana | `http://<SERVER_IP>:30092` |
| Prometheus | `http://<SERVER_IP>:30091` |
| Eureka (via port-forward) | `kubectl port-forward -n smartek svc/eureka-server 8761:8761` → `http://localhost:8761` |

---

## 13. Monitoring — Prometheus & Grafana

### 13.1 Prometheus
- Already configured via `prometheus-configmap.yaml`
- Scrapes: Prometheus itself, Eureka, API Gateway, Offers Service, Config Server
- Access: `http://<SERVER_IP>:30091`
- Go to **Status → Targets** to verify all endpoints are UP.

### 13.2 Grafana
- Access: `http://<SERVER_IP>:30092`
- Login: `admin / admin`

#### Add Prometheus Data Source
1. **Configuration → Data Sources → Add data source**
2. Select **Prometheus**
3. URL: `http://prometheus:9090` (inside cluster) or `http://<SERVER_IP>:30091`
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
| **Jenkins** | `http://<SERVER_IP>:8080` | admin / admin123 |
| **SonarQube** | `http://<SERVER_IP>:9000` | admin / admin123 |
| **Nexus** | `http://<SERVER_IP>:8081` | admin / *your-password* |
| **Vault** | `http://<SERVER_IP>:8200` | Token: `smartek-root-token` |
| **K8s Dashboard** | `kubectl proxy` → `http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/` | Token auth |
| **Frontend** | `http://<SERVER_IP>:30420` | — |
| **API Gateway** | `http://<SERVER_IP>:30090` | — |
| **Grafana** | `http://<SERVER_IP>:30092` | admin / admin |
| **Prometheus** | `http://<SERVER_IP>:30091` | — |
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
- Verify SonarQube URL (`http://<SERVER_IP>:9000`) is reachable from Jenkins container:
  ```bash
  docker exec jenkins curl -sf http://<SERVER_IP>:9000/api/system/status
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
- Verify Prometheus target is UP at `http://<SERVER_IP>:30091/targets`
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
# Frontend:     http://<SERVER_IP>:30420
# API Gateway:  http://<SERVER_IP>:30090
# Grafana:      http://<SERVER_IP>:30092
# SonarQube:    http://<SERVER_IP>:9000
# Jenkins:      http://<SERVER_IP>:8080
```

---

## Files You Need to Modify Before First Run

| File | What to Change |
|------|----------------|
| `jenkins/ci/Jenkinsfile-auth-service` | Replace `192.168.1.100` with your server IP |
| `jenkins/ci/Jenkinsfile-offers-service` | Replace `192.168.1.100` with your server IP |
| `jenkins/cd/Jenkinsfile-auth-service` | Replace `192.168.1.100` with your server IP |
| `jenkins/cd/Jenkinsfile-offers-service` | Replace `192.168.1.100` with your server IP |
| `jenkins/cd/Jenkinsfile-infra` | Replace `192.168.1.100` with your server IP |
| `k8s/infra/api-gateway/api-gateway-deployment.yaml` | Replace `<SERVER_IP>` with your server IP |
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

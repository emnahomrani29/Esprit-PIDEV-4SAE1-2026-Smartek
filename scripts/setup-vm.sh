#!/bin/bash
# =============================================================================
# SMARTEK Sprint 3 — VM Setup Helper Script
# =============================================================================
# Run this on your fresh Ubuntu 22.04 VM as the smartek user
# =============================================================================

set -e

VM_IP="${1:-192.168.1.100}"
DOCKER_USER="${2:-<DOCKER_HUB_USER>}"

echo "=========================================="
echo "SMARTEK Sprint 3 — VM Setup"
echo "VM IP: $VM_IP"
echo "Docker Hub User: $DOCKER_USER"
echo "=========================================="

# -----------------------------------------------------------------------------
# 1. System Update
# -----------------------------------------------------------------------------
echo "[1/8] Updating system..."
sudo apt update && sudo apt upgrade -y
sudo apt install -y curl wget git vim net-tools openssh-server apt-transport-https ca-certificates gnupg lsb-release

# -----------------------------------------------------------------------------
# 2. Install Docker
# -----------------------------------------------------------------------------
echo "[2/8] Installing Docker..."
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker "$USER"

# -----------------------------------------------------------------------------
# 3. Install kubeadm, kubelet, kubectl
# -----------------------------------------------------------------------------
echo "[3/8] Installing Kubernetes..."
sudo swapoff -a
sudo sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab

curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.29/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.29/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list

sudo apt update
sudo apt install -y kubelet kubeadm kubectl
sudo apt-mark hold kubelet kubeadm kubectl

sudo modprobe overlay
sudo modprobe br_netfilter
sudo tee /etc/sysctl.d/kubernetes.conf <<EOF
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
net.ipv4.ip_forward = 1
EOF
sudo sysctl --system

# -----------------------------------------------------------------------------
# 4. Configure containerd
# -----------------------------------------------------------------------------
echo "[4/8] Configuring containerd..."
sudo mkdir -p /etc/containerd
sudo containerd config default | sudo tee /etc/containerd/config.toml
sudo sed -i 's/SystemdCgroup = false/SystemdCgroup = true/' /etc/containerd/config.toml
sudo systemctl restart containerd
sudo systemctl enable containerd

# -----------------------------------------------------------------------------
# 5. Init kubeadm cluster (skip if already initialized)
# -----------------------------------------------------------------------------
echo "[5/8] Initializing kubeadm cluster..."
if [ ! -f "$HOME/.kube/config" ]; then
    sudo kubeadm init --pod-network-cidr=10.244.0.0/16 --apiserver-advertise-address="$VM_IP"
else
    echo "Kubeadm already initialized. Skipping."
fi

mkdir -p "$HOME/.kube"
sudo cp -i /etc/kubernetes/admin.conf "$HOME/.kube/config"
sudo chown "$(id -u):$(id -g)" "$HOME/.kube/config"

kubectl apply -f https://github.com/flannel-io/flannel/releases/latest/download/kube-flannel.yml
kubectl taint nodes --all node-role.kubernetes.io/control-plane- || true

# -----------------------------------------------------------------------------
# 6. Create local directories
# -----------------------------------------------------------------------------
echo "[6/8] Creating directories..."
sudo mkdir -p /mnt/data/mysql
sudo chown "$(id -u):$(id -g)" /mnt/data/mysql

# -----------------------------------------------------------------------------
# 7. Clone repo (if not already present)
# -----------------------------------------------------------------------------
echo "[7/8] Cloning repository..."
if [ ! -d "$HOME/Esprit-PIDEV-4SAE1-2026-Smartek" ]; then
    git clone https://github.com/emnahomrani29/Esprit-PIDEV-4SAE1-2026-Smartek.git "$HOME/Esprit-PIDEV-4SAE1-2026-Smartek"
fi

cd "$HOME/Esprit-PIDEV-4SAE1-2026-Smartek"
git fetch origin
git checkout offers-service 2>/dev/null || git checkout -b offers-service origin/offers-service 2>/dev/null || true

# -----------------------------------------------------------------------------
# 8. Replace placeholders
# -----------------------------------------------------------------------------
echo "[8/8] Replacing placeholders in configs..."
sed -i "s/192.168.1.100/$VM_IP/g" jenkins/ci/Jenkinsfile-auth-service
sed -i "s/192.168.1.100/$VM_IP/g" jenkins/ci/Jenkinsfile-offers-service
sed -i "s/<VM_IP>/$VM_IP/g" k8s/infra/api-gateway/api-gateway-deployment.yaml
sed -i "s/<DOCKER_HUB_USER>/$DOCKER_USER/g" k8s/apps/auth-service/auth-service-deployment.yaml
sed -i "s/<DOCKER_HUB_USER>/$DOCKER_USER/g" k8s/apps/offers-service/offers-service-deployment.yaml
sed -i "s/<DOCKER_HUB_USER>/$DOCKER_USER/g" k8s/apps/frontend/frontend-deployment.yaml

echo "=========================================="
echo "Setup complete! Please log out and back in"
echo "for Docker group membership to take effect."
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Log out and SSH back in"
echo "2. cd ~/Esprit-PIDEV-4SAE1-2026-Smartek"
echo "3. docker compose -f docker-compose.tools.yml up -d"
echo "4. Configure Jenkins, SonarQube, Nexus, Vault"
echo "5. Run Jenkins CI/CD pipelines"

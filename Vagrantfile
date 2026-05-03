# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile pour Smartek DevOps Platform
# Ubuntu 22.04 avec Docker, Kubernetes, Jenkins, SonarQube, Prometheus, Grafana

Vagrant.configure("2") do |config|
  # Box Ubuntu 22.04 LTS
  config.vm.box = "bento/ubuntu-22.04"
  config.vm.box_download_options = { "ssl-no-revoke" => true }
  config.vm.hostname = "smartek-devops"

  # Configuration réseau
  config.vm.network "private_network", ip: "192.168.56.10"
  
  # Port forwarding pour accès depuis Windows
  config.vm.network "forwarded_port", guest: 8080, host: 8080, id: "jenkins"
  config.vm.network "forwarded_port", guest: 9000, host: 9000, id: "sonarqube"
  config.vm.network "forwarded_port", guest: 9090, host: 9090, id: "prometheus"
  config.vm.network "forwarded_port", guest: 3000, host: 3000, id: "grafana"
  config.vm.network "forwarded_port", guest: 6443, host: 6443, id: "k8s-api"
  config.vm.network "forwarded_port", guest: 30080, host: 30080, id: "k8s-app"
  config.vm.network "forwarded_port", guest: 30090, host: 30090, id: "prometheus-nodeport"
  config.vm.network "forwarded_port", guest: 30300, host: 30300, id: "grafana-nodeport"

  # Configuration VirtualBox
  config.vm.provider "virtualbox" do |vb|
    vb.name = "smartek-devops-vm"
    vb.memory = "8192"  # 8 GB RAM
    vb.cpus = 4         # 4 CPU cores
    vb.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
    vb.customize ["modifyvm", :id, "--natdnsproxy1", "on"]
    vb.customize ["modifyvm", :id, "--clipboard", "bidirectional"]
    vb.customize ["modifyvm", :id, "--draganddrop", "bidirectional"]
  end

  # Provisioning - Installation automatique
  config.vm.provision "shell", inline: <<-SHELL
    echo "🚀 Début de l'installation DevOps Stack..."
    
    # Mise à jour du système
    apt-get update
    apt-get upgrade -y
    
    # Installation des outils de base
    apt-get install -y \
      curl \
      wget \
      git \
      vim \
      net-tools \
      ca-certificates \
      gnupg \
      lsb-release \
      apt-transport-https \
      software-properties-common \
      build-essential
    
    # Configuration timezone
    timedatectl set-timezone Europe/Paris
    
    echo "✅ Provisioning initial terminé"
    echo "📝 Pour installer la stack DevOps complète, exécutez :"
    echo "   vagrant ssh"
    echo "   cd /vagrant/scripts"
    echo "   chmod +x install-devops-complete.sh"
    echo "   ./install-devops-complete.sh"
  SHELL

  # Synchronisation des dossiers
  config.vm.synced_folder ".", "/vagrant"
end

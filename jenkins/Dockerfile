# ============================================================
# Jenkins custom avec plugins pré-installés + Docker CLI
# ============================================================
FROM jenkins/jenkins:lts-jdk17

# Désactiver le wizard de setup (géré par JCasC)
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV CASC_JENKINS_CONFIG=/var/jenkins_home/casc/jenkins.yaml

USER root

# Installer Docker CLI (pour builder les images depuis Jenkins)
RUN apt-get update && apt-get install -y \
    docker.io \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Installer Trivy (scan sécurité)
RUN curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh \
    | sh -s -- -b /usr/local/bin v0.50.0

USER jenkins

# Installer les plugins depuis la liste
COPY casc/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Copier la config JCasC
COPY casc/jenkins.yaml /var/jenkins_home/casc/jenkins.yaml

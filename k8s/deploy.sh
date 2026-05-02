#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# Script de déploiement Kubernetes — Smartek Platform
# Usage: ./k8s/deploy.sh [all|infra|services|monitoring|delete]
# ─────────────────────────────────────────────────────────────────────────────

set -e

NAMESPACE="smartek"
ACTION=${1:-all}

echo "🚀 Déploiement Smartek sur Kubernetes — action: $ACTION"

deploy_infra() {
  echo ""
  echo "📦 Déploiement de l'infrastructure..."
  kubectl apply -f k8s/namespace.yml
  kubectl apply -f k8s/configmap.yml
  kubectl apply -f k8s/secret.yml
  kubectl apply -f k8s/infrastructure/mysql.yml
  echo "⏳ Attente MySQL ready..."
  kubectl wait --for=condition=ready pod -l app=mysql -n $NAMESPACE --timeout=180s
  kubectl apply -f k8s/infrastructure/eureka-server.yml
  echo "⏳ Attente Eureka ready..."
  kubectl wait --for=condition=ready pod -l app=eureka-server -n $NAMESPACE --timeout=120s
  kubectl apply -f k8s/infrastructure/api-gateway.yml
  echo "✅ Infrastructure déployée"
}

deploy_services() {
  echo ""
  echo "🔧 Déploiement des microservices..."
  kubectl apply -f k8s/services/event-service.yml
  kubectl apply -f k8s/services/planning-service.yml
  kubectl apply -f k8s/services/training-service.yml
  kubectl apply -f k8s/services/course-service.yml
  kubectl apply -f k8s/services/exam-service.yml
  echo "✅ Microservices déployés"
}

deploy_monitoring() {
  echo ""
  echo "📊 Déploiement du monitoring (Prometheus + Grafana)..."
  kubectl apply -f k8s/monitoring/prometheus.yml
  kubectl apply -f k8s/monitoring/grafana.yml
  echo "✅ Monitoring déployé"
}

delete_all() {
  echo ""
  echo "🗑️  Suppression de tous les déploiements Smartek..."
  kubectl delete namespace $NAMESPACE --ignore-not-found=true
  echo "✅ Namespace $NAMESPACE supprimé"
}

show_status() {
  echo ""
  echo "📋 État des pods dans le namespace $NAMESPACE:"
  kubectl get pods -n $NAMESPACE
  echo ""
  echo "🌐 Services exposés:"
  kubectl get services -n $NAMESPACE
  echo ""
  echo "🔗 Accès aux services (NodePort):"
  NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}' 2>/dev/null || echo "localhost")
  echo "  API Gateway  : http://$NODE_IP:30090"
  echo "  Prometheus   : http://$NODE_IP:30090 (port 30090)"
  echo "  Grafana      : http://$NODE_IP:30300  (admin/smartek123)"
}

case $ACTION in
  all)
    deploy_infra
    deploy_services
    deploy_monitoring
    show_status
    ;;
  infra)
    deploy_infra
    ;;
  services)
    deploy_services
    ;;
  monitoring)
    deploy_monitoring
    ;;
  status)
    show_status
    ;;
  delete)
    delete_all
    ;;
  *)
    echo "Usage: $0 [all|infra|services|monitoring|status|delete]"
    exit 1
    ;;
esac

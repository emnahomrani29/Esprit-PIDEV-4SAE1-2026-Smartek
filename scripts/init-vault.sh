#!/bin/bash
# =============================================================================
# SMARTEK Sprint 3 — Vault Initialization Script
# =============================================================================
# Run this AFTER docker-compose.tools.yml is up and Vault is running.
# This script uses 'docker exec' so you do NOT need vault CLI on the host.
# =============================================================================

set -e

DOCKER_USER="${1:-<DOCKER_HUB_USER>}"
DOCKER_PASS="${2:-<DOCKER_HUB_PASS>}"
NEXUS_USER="${3:-admin}"
NEXUS_PASS="${4:-admin123}"

echo "=========================================="
echo "Initializing Vault for SMARTEK"
echo "=========================================="

VAULT_CONTAINER="vault"
VAULT_ADDR="http://localhost:8200"
VAULT_TOKEN="smartek-root-token"

# Wait for Vault to be ready
echo "[0/4] Waiting for Vault to be ready..."
until docker exec "$VAULT_CONTAINER" wget -qO- "$VAULT_ADDR/v1/sys/health" > /dev/null 2>&1; do
    sleep 2
    echo "  ...waiting"
done

# -----------------------------------------------------------------------------
# 1. Enable KV v2 at secret/ (if not already enabled)
# -----------------------------------------------------------------------------
echo "[1/4] Enabling KV v2 secrets engine..."
docker exec -e VAULT_ADDR="$VAULT_ADDR" -e VAULT_TOKEN="$VAULT_TOKEN" \
    "$VAULT_CONTAINER" vault secrets enable -version=2 -path=secret kv 2>/dev/null || echo "KV already enabled"

# -----------------------------------------------------------------------------
# 2. Store Docker Hub credentials
# -----------------------------------------------------------------------------
echo "[2/4] Storing Docker Hub credentials..."
docker exec -e VAULT_ADDR="$VAULT_ADDR" -e VAULT_TOKEN="$VAULT_TOKEN" \
    "$VAULT_CONTAINER" vault kv put secret/smartek/docker-hub \
    username="$DOCKER_USER" \
    password="$DOCKER_PASS"

# -----------------------------------------------------------------------------
# 3. Store MySQL root password
# -----------------------------------------------------------------------------
echo "[3/4] Storing MySQL credentials..."
docker exec -e VAULT_ADDR="$VAULT_ADDR" -e VAULT_TOKEN="$VAULT_TOKEN" \
    "$VAULT_CONTAINER" vault kv put secret/smartek/mysql \
    root-password="root"

# -----------------------------------------------------------------------------
# 4. Store Nexus credentials
# -----------------------------------------------------------------------------
echo "[4/4] Storing Nexus credentials..."
docker exec -e VAULT_ADDR="$VAULT_ADDR" -e VAULT_TOKEN="$VAULT_TOKEN" \
    "$VAULT_CONTAINER" vault kv put secret/smartek/nexus \
    username="$NEXUS_USER" \
    password="$NEXUS_PASS"

# -----------------------------------------------------------------------------
# Verify
# -----------------------------------------------------------------------------
echo ""
echo "=========================================="
echo "Vault secrets initialized successfully!"
echo "=========================================="
echo ""
echo "Stored paths:"
echo "  - secret/smartek/docker-hub"
echo "  - secret/smartek/mysql"
echo "  - secret/smartek/nexus"
echo ""
echo "To verify manually:"
echo "  docker exec -e VAULT_TOKEN=$VAULT_TOKEN vault vault kv get secret/smartek/docker-hub"

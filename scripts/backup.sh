#!/bin/bash
# scripts/backup.sh - Sauvegarde des bases MySQL via XAMPP ou Docker

set -euo pipefail

BACKUP_DIR=${BACKUP_DIR:-./backups}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
MYSQL_USER=${MYSQL_USER:-root}
MYSQL_PASS=${MYSQL_PASS:-root}

mkdir -p "$BACKUP_DIR"

DATABASES=(
  smartek_auth
  smartek_events
  smartek_planning
  smartek_training
  smartek_offers
  smartek_course
  smartek_exam
  smartek_skill_evidence
  smartek_learning
  smartek_sponsor
  smartek_certification
)

# Detecter si MySQL tourne dans Docker ou XAMPP
if docker ps --format '{{.Names}}' 2>/dev/null | grep -q "smartek-mysql"; then
  echo "Using Docker MySQL..."
  MYSQL_CMD="docker exec smartek-mysql mysqldump -u$MYSQL_USER -p$MYSQL_PASS"
elif [ -f "C:/xampp/mysql/bin/mysqldump.exe" ]; then
  echo "Using XAMPP MySQL..."
  MYSQL_CMD="C:/xampp/mysql/bin/mysqldump.exe -u$MYSQL_USER"
else
  MYSQL_CMD="mysqldump -u$MYSQL_USER -p$MYSQL_PASS"
fi

for db in "${DATABASES[@]}"; do
  BACKUP_FILE="$BACKUP_DIR/${db}_${TIMESTAMP}.sql"
  echo "Backing up $db..."
  $MYSQL_CMD "$db" > "$BACKUP_FILE" 2>/dev/null && echo "  [OK] $db -> $BACKUP_FILE" || echo "  [SKIP] $db (not found)"
done

# Nettoyage des sauvegardes de plus de 30 jours
find "$BACKUP_DIR" -name "*.sql" -mtime +30 -delete 2>/dev/null || true
echo ""
echo "Backup completed in $BACKUP_DIR"

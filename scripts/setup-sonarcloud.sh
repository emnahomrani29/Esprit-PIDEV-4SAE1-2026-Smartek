#!/bin/bash
set -e

SONAR_TOKEN="${1}"
SONAR_ORG="${2}"

if [ -z "$SONAR_TOKEN" ] || [ -z "$SONAR_ORG" ]; then
  echo "Usage: $0 <SONAR_TOKEN> <SONAR_ORGANIZATION>"
  exit 1
fi

SONAR_URL="https://sonarcloud.io"

PROJECTS=(
  "smartek-auth-service:Smartek Auth Service"
  "smartek-course-service:Smartek Course Service"
  "smartek-exam-service:Smartek Exam Service"
  "smartek-event-service:Smartek Event Service"
  "smartek-offers-service:Smartek Offers Service"
  "smartek-planning-service:Smartek Planning Service"
  "smartek-training-service:Smartek Training Service"
  "smartek-certification-badge-service:Smartek Certification Badge Service"
  "smartek-skill-evidence-service:Smartek Skill Evidence Service"
  "smartek-learning-service:Smartek Learning Service"
  "smartek-sponsor-service:Smartek Sponsor Service"
  "smartek-frontend:Smartek Frontend Angular"
)

for ENTRY in "${PROJECTS[@]}"; do
  PROJECT_KEY="${ENTRY%%:*}"
  PROJECT_NAME="${ENTRY##*:}"
  FULL_KEY="${SONAR_ORG}_${PROJECT_KEY}"

  echo -n "Creating: $FULL_KEY ... "

  HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" \
    -X POST \
    -u "${SONAR_TOKEN}:" \
    "${SONAR_URL}/api/projects/create" \
    -d "project=${FULL_KEY}" \
    -d "name=${PROJECT_NAME}" \
    -d "organization=${SONAR_ORG}" \
    -d "visibility=public")

  if [ "$HTTP_CODE" -eq 200 ] || [ "$HTTP_CODE" -eq 201 ]; then
    echo "OK"
  elif [ "$HTTP_CODE" -eq 400 ]; then
    echo "already exists"
  else
    echo "FAILED (HTTP $HTTP_CODE)"
  fi
done

echo ""
echo "Dashboard: ${SONAR_URL}/organizations/${SONAR_ORG}/projects"

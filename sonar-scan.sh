#!/usr/bin/env bash
# =============================================================================
# sonar-scan.sh — Build all modules, generate coverage, then push to SonarQube
# =============================================================================
# Usage:
#   ./sonar-scan.sh                        # prompts for token
#   SONAR_TOKEN=sqa_78172af3ca1722a6590e10ad5574e10662eac44c ./sonar-scan.sh    # non-interactive (CI)
#   ./sonar-scan.sh --skip-tests           # skip unit tests (faster, no coverage)
# =============================================================================
set -euo pipefail

SONAR_HOST="${SONAR_HOST:-http://localhost:9000}"
SKIP_TESTS=false

# ---- Parse arguments --------------------------------------------------------
for arg in "$@"; do
  case $arg in
    --skip-tests) SKIP_TESTS=true ;;
    *) echo "Unknown argument: $arg"; exit 1 ;;
  esac
done

# ---- Require a token --------------------------------------------------------
if [ -z "${SONAR_TOKEN:-}" ]; then
  echo ""
  echo "SonarQube token is required."
  echo "  1. Open ${SONAR_HOST} (admin / admin on first launch)"
  echo "  2. My Account → Security → Generate Token"
  echo "  3. Re-run:  SONAR_TOKEN=sqa_78172af3ca1722a6590e10ad5574e10662eac44c ./sonar-scan.sh"
  echo ""
  read -rsp "sqa_78172af3ca1722a6590e10ad5574e10662eac44c" SONAR_TOKEN
  echo ""
fi

# ---- Wait for SonarQube to be ready -----------------------------------------
echo ">>> Waiting for SonarQube at ${SONAR_HOST} ..."
MAX_WAIT=120
WAITED=0
until curl -sf "${SONAR_HOST}/api/system/status" | grep -q '"status":"UP"'; do
  if [ $WAITED -ge $MAX_WAIT ]; then
    echo "ERROR: SonarQube did not become ready within ${MAX_WAIT}s. Is Docker running?"
    exit 1
  fi
  sleep 5
  WAITED=$((WAITED + 5))
  echo "  ... still waiting (${WAITED}s)"
done
echo ">>> SonarQube is UP."

# ---- Maven build + Sonar analysis ------------------------------------------
MAVEN_OPTS_EXTRA=""
if [ "$SKIP_TESTS" = true ]; then
  MAVEN_OPTS_EXTRA="-DskipTests"
  echo ">>> Skipping unit tests (no coverage data will be sent)."
fi

echo ""
echo ">>> Running: mvn clean verify sonar:sonar"
echo "    Host   : ${SONAR_HOST}"
echo ""

mvn clean verify sonar:sonar \
  -Dsonar.host.url="${SONAR_HOST}" \
  -Dsonar.token="${SONAR_TOKEN}" \
  ${MAVEN_OPTS_EXTRA} \
  --batch-mode \
  --no-transfer-progress

echo ""
echo "======================================================"
echo " Analysis complete. Open the dashboard:"
echo " ${SONAR_HOST}/projects"
echo "======================================================"

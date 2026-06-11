#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="${1:-circleguard-dev}"

echo "Starting port-forward to ${NAMESPACE}..."
echo "Press Ctrl+C to stop all forwards."
echo ""

forward() {
  local service="$1"
  local local_port="$2"
  local remote_port="${3:-8080}"
  echo "  ${service}:${remote_port} -> localhost:${local_port}"
  kubectl port-forward "svc/${service}" "${local_port}:${remote_port}" -n "${NAMESPACE}" >/dev/null 2>&1 &
}

forward circleguard-auth-service       8180
forward circleguard-identity-service   8083
forward circleguard-gateway-service    8087
forward circleguard-form-service       8086
forward circleguard-promotion-service  8088
forward circleguard-notification-service 8082

# Not deployed to k8s yet — uncomment when available
# forward circleguard-dashboard-service 8084
# forward circleguard-file-service      8085

echo ""
echo "All forwards started. Verifying..."
sleep 2

failures=0
for port in 8180 8083 8087 8086 8088 8082; do
  if curl -sf "http://localhost:${port}/actuator/health" >/dev/null 2>&1; then
    echo "  [OK] localhost:${port}"
  else
    echo "  [--] localhost:${port} (not ready)"
    failures=$((failures + 1))
  fi
done

if [ "$failures" -gt 0 ]; then
  echo ""
  echo "Some services are not yet reachable. They may still be starting."
fi

echo ""
echo "Port-forwards running in background. Press Ctrl+C to stop."
wait

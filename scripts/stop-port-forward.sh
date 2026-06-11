#!/usr/bin/env bash
set -euo pipefail

pkill -f "kubectl port-forward.*svc/circleguard-" 2>/dev/null && echo "All port-forwards stopped." || echo "No active port-forwards found."

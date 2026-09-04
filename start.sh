#!/usr/bin/env bash

# ==============================================================================
#  MobileDashboard - Go Native Backend Başlatıcı
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/backend-go"

# Binary yoksa veya main.go daha yeniyse derle
if [ ! -f "./mobiledashboard-server" ] || [ "./main.go" -nt "./mobiledashboard-server" ]; then
    echo "⚡ Go sunucusu derleniyor..."
    go build -o mobiledashboard-server main.go
fi

exec ./mobiledashboard-server "$@"

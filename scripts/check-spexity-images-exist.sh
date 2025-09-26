#!/bin/bash
set -e

if [ $# -ne 1 ]; then
    echo "Usage: $0 <tag>"
    echo "Example: $0 1.0.0"
    exit 1
fi

TAG=$1

IMAGES=(
    "ghcr.io/spexity/spexity-web:$TAG"
    "ghcr.io/spexity/spexity-backend:$TAG"
)

./check-docker-images-exist.sh "${IMAGES[@]}"

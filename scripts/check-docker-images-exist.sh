#!/bin/bash
set -e

if ! command -v docker &> /dev/null; then
    echo "Error: Docker command not found. Please install Docker."
    exit 1
fi

if [ $# -eq 0 ]; then
    echo "Usage: $0 <image1> [image2] [image3] ..."
    echo "Example: $0 ghcr.io/spexity/spexity-web:1.0.0 ghcr.io/spexity/spexity-backend:1.0.0"
    exit 1
fi

IMAGES=("$@")

echo "Checking if images exist..."

for IMAGE in "${IMAGES[@]}"; do
    echo "Checking $IMAGE..."
    if docker manifest inspect "$IMAGE" &> /dev/null; then
        echo "✓ $IMAGE exists"
    else
        echo "✗ $IMAGE does not exist"
        exit 1
    fi
done

echo "All images exist"

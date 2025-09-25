#!/bin/bash

set -e

# Check if tag argument is provided
if [ $# -ne 1 ]; then
    echo "Usage: $0 <tag>"
    echo "Example: $0 v1.0.0"
    exit 1
fi

TAG=$1

# Check if docker command exists
if ! command -v docker &> /dev/null; then
    echo "Error: Docker command not found. Please install Docker."
    exit 1
fi

# Define the images to check
IMAGES=(
    "ghcr.io/spexity/spexity-web:$TAG"
    "ghcr.io/spexity/spexity-backend:$TAG"
)

echo "Checking if images exist for tag: $TAG"

# Check each image
for IMAGE in "${IMAGES[@]}"; do
    echo "Checking $IMAGE..."
    if docker manifest inspect "$IMAGE" &> /dev/null; then
        echo "✓ $IMAGE exists"
    else
        echo "✗ $IMAGE does not exist"
        exit 1
    fi
done

echo "All images exist for tag: $TAG"
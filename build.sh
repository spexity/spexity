#!/bin/bash
set -euo pipefail

# Set GIT_SHA if not already set
if [ -z "${GIT_SHA-}" ]; then
  if command -v git >/dev/null 2>&1; then
    export GIT_SHA=$(git rev-parse HEAD)
    echo "Set GIT_SHA to: $GIT_SHA"
  else
    echo "Warning: git command not available, GIT_SHA not set"
  fi
fi

# Backend
pushd backend || exit 1

if command -v mvn >/dev/null 2>&1; then
  echo "Building backend"
  mvn -B clean install -DskipTests=true
else
  echo "Error: Maven is not installed. Please install Maven first."
  exit 1
fi

popd || exit 1

# Web
printf '\n%.0s' {1..10}

pushd web || exit 1

if command -v node >/dev/null 2>&1; then
  echo "Building web"
  if [ -n "${CI-}" ]; then
    npm ci
  else
    npm install
  fi
  npm run build
  npm prune --production
else
  echo "Error: Node.js is not installed. Please install Node.js first."
  exit 1
fi

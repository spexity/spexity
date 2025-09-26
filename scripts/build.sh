#!/bin/bash
set -euo pipefail
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Set GIT_SHA if not already set
if [ -z "${GIT_SHA-}" ]; then
  if command -v git >/dev/null 2>&1; then
    GIT_SHA="$(git rev-parse HEAD)"
    export GIT_SHA
    echo "Set GIT_SHA to: $GIT_SHA"
  else
    echo "Warning: git command not available, GIT_SHA not set"
  fi
fi

# Web
pushd "$SCRIPT_DIR/../web" || exit 1

if command -v node >/dev/null 2>&1; then
  echo "Building web"
  if [ -n "${CI-}" ]; then
    npm ci
  else
    npm install
  fi
  npm run build
  npm prune --omit=dev
else
  echo "Error: Node.js is not installed. Please install Node.js first."
  exit 1
fi

#Done web

popd || exit 1

printf '\n%.0s' {1..10}

# Backend
pushd "$SCRIPT_DIR/../backend" || exit 1

if command -v mvn >/dev/null 2>&1; then
  echo "Building backend"
  mvn -B clean install -DskipTests=true
else
  echo "Error: Maven is not installed. Please install Maven first."
  exit 1
fi

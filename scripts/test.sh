#!/bin/bash
set -euo pipefail
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Backend
pushd "$SCRIPT_DIR/../backend" || exit 1

if command -v mvn >/dev/null 2>&1; then
  echo "Running backend tests"
  mvn -B clean install -Dquarkus.profile=e2e-tests
else
  echo "Error: Maven is not installed. Please install Maven first."
  exit 1
fi

popd || exit 1

# Web
printf '\n%.0s' {1..10}

pushd "$SCRIPT_DIR/../web" || exit 1

if [ -f ".env" ]; then
  echo "web .env file already exists"
else
  cp .env.example .env
  echo "copied .env.example as .env"
fi

if command -v node >/dev/null 2>&1; then
  echo "Running web tests"
  if [ -n "${CI-}" ]; then
    npm ci
  else
    npm install
  fi
  npm run checked-build
else
  echo "Error: Node.js is not installed. Please install Node.js first."
  exit 1
fi

popd || exit 1

pushd "$SCRIPT_DIR/../e2e-tests" || exit 1

printf '\n%.0s' {1..10}
echo "Running e2e-tests"
if [ -n "${CI-}" ]; then
  npm ci
else
  npm install
fi
npx playwright install --with-deps
npx playwright test


printf '\n%.0s' {1..10}
echo "Finished all testing"
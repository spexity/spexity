#!/bin/bash


# Backend setup
pushd backend || exit 1

if command -v mvn >/dev/null 2>&1; then
  echo "running mvn install for server dependencies"
  mvn clean install -DskipTests=true  -pl '!server' -am
else
  echo "Error: Maven is not installed. Please install Maven first."
  exit 1
fi

popd || exit 1

# Web setup
pushd web || exit 1

if [ -f ".env" ]; then
  echo "web .env file already exists"
else
  cp .env.example .env
  echo "copied .env.example as .env"
fi

if command -v node >/dev/null 2>&1; then
  echo "running npm install"
  npm install
else
  echo "Error: Node.js is not installed. Please install Node.js first."
  exit 1
fi

printf '\n%.0s' {1..10}

echo "Setup complete."
echo "To start developing"
echo "For the backend:"
echo "  run 'mvn quarkus:dev' in ./backend/server"
echo "For the web application:"
echo "  run 'npm run dev' in ./web"

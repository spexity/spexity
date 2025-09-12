## Backend

### Build application

Build the quarkus application 
```shell
#cd backend

mvn clean package
```

### Build docker image
```shell
#cd backend/server

docker build -f src/main/docker/Dockerfile.jvm -t spexity-backend .
```

### Run docker image

```shell
docker run --rm -p 48080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://host.docker.internal:45432/spexity" \
  -e QUARKUS_DATASOURCE_USERNAME=test \
  -e QUARKUS_DATASOURCE_PASSWORD=test \
  -e QUARKUS_OIDC_AUTH_SERVER_URL="http://host.docker.internal:48080/realms/spexity" \
  -e QUARKUS_OIDC_CLIENT_ID=spexity \
  -e QUARKUS_HTTP_CORS_ENABLED=true \
  -e QUARKUS_HTTP_CORS_ORIGINS="http://localhost:5173,http://localhost:4173" \
  spexity-backend
```


## Web

### Build application and docker image
```shell
#cd web
export GIT_SHA=$(git rev-parse HEAD)
npm install
npm run build
npm prune --production
docker build -t spexity-web .
```


### Run docker image

```shell
docker run --rm -p 4173:3000 \
  -e API_URL="http://host.docker.internal:8080" \
  -e PUBLIC_API_URL="http://localhost:48080" \
  -e PUBLIC_OIDC_AUTH_SERVER_URL="http://localhost:48080/realms/spexity" \
  -e PUBLIC_OIDC_CLIENT_ID="spexity" \
  spexity-web
```

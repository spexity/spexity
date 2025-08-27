## Backend

### Build application

Make sure all modules have been installed in the local maven repo.
```shell
#cd backend

mvn clean install
```

Build the native quarkus application 
```shell
#cd backend/server

mvn clean package -Dnative -Dquarkus.native.container-build=true -f pom.xml
```

### Build docker image
```shell
#cd backend/server

#for amd64 arch
docker buildx build --platform linux/amd64 -f src/main/docker/Dockerfile.native-micro -t spexity/backend .
#for arm64 arch
docker buildx build --platform linux/arm64 -f src/main/docker/Dockerfile.native-micro -t spexity/backend .
```

### Run docker image

```shell
docker run --rm -p 8080:8080 \
  -e QUARKUS_PROFILE=prod \
  -e QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://host.docker.internal:55432/spexity" \
  -e QUARKUS_DATASOURCE_USERNAME=myuser \
  -e QUARKUS_DATASOURCE_PASSWORD=mypassword \
  spexity/backend
```
modify env variables as needed.

## Web

### Build application and docker image
```shell
#cd web

#for amd64 arch
docker buildx build --platform linux/amd64 -t spexity/web .
#for arm64 arch
docker buildx build --platform linux/arm64 -t spexity/web .
```


### Run docker image

```shell
docker run --rm -p 3000:3000 \
  -e API_URL="http://host.docker.internal:8080" \
  spexity/web
```

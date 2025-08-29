## Backend

### Build application

Make sure all modules have been installed in the local maven repo.
```shell
#cd backend

mvn clean install -pl '!server' -am
```

Build the native quarkus application 
```shell
#cd backend/server

mvn clean package -Dnative -Dquarkus.native.container-build=true -f pom.xml
```

### Build docker image
```shell
#cd backend/server

docker build -f src/main/docker/Dockerfile.native-micro -t spexity-backend .
```

### Run docker image

```shell
docker run --rm -p 8080:8080 \
  -e QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://host.docker.internal:55432/spexity" \
  -e QUARKUS_DATASOURCE_USERNAME=test \
  -e QUARKUS_DATASOURCE_PASSWORD=test \
  spexity-backend
```
modify env variables as needed.

## Web

### Build application and docker image
```shell
#cd web

docker build --build-arg GIT_SHA=$(git rev-parse HEAD) -t spexity-web .
```


### Run docker image

```shell
docker run --rm -p 3000:3000 \
  -e API_URL="http://host.docker.internal:8080" \
  spexity-web
```

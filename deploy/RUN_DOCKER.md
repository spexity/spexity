To run the generated docker image of spexity

```shell
 docker run -i --rm -p 8080:8080 \                                                                         
  -e QUARKUS_PROFILE=prod \
  -e QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://host.docker.internal:55432/spexity \
  -e QUARKUS_DATASOURCE_USERNAME=myuser \
  -e QUARKUS_DATASOURCE_PASSWORD=mypassword \
  quarkus/spexity
```
modify env variables as needed.
# Deploy

## Kubernetes
The spexity application is deployable in a kubernetes cluster.
To deploy the application components, in your kubernetes cluster:
1. Have available a Postgresql database with a username, password and database created for the application.
2. Create a kubernetes namespace for the application.
3. Create a secret inside this namespace for the database connection details with the following keys (case sensitive):
   1. username
   2. password
   3. jdbcUrl
4. Deploy the application using helm

```shell
helm upgrade --install spexity ./path/to/this/directory \
  -n namespace-from-above -f ./path/to/this/directory/values-you-want.yaml
```
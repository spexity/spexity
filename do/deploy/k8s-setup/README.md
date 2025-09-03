# Setup Kubernetes

## Namespace
Set up a new namespace for the deployment.

```shell
kubectl create namespace spexity-test
```

## CI/CD Service Account
To get GitHub to deploy to this Kubernetes, you need to:
1. Create a service account and a cluster role.
2. Bind the service account to the namespace
3. Create a token for GitHub to use

### Create Service Account and Cluster Role
For ease (especially in a k3s deployment) create an independent service account that can manage other namespaces.

We will create a new namespace
```shell
kubectl create namespace spexity-cicd
```

Then create the service account `gh-deployer`. See file [sa-and-clusterrole.yaml](sa-and-clusterrole.yaml)

```shell
kubectl apply -f sa-and-clusterrole.yaml
```

### Bind Service Account
Bind the service account to the role in the destination namespace.
You can duplicate this file to bind to more namespaces.
See file [bind-ci-sa-into-namespaces.yaml](bind-ci-sa-into-namespaces.yaml)

```shell
kubectl apply -f bind-ci-sa-into-namespaces.yaml
```

### Create Token
Let Kubernetes create a token for the gh-deployer to be able to send commands.
See file [sa-token.yaml](sa-token.yaml)
```shell
kubectl apply -f sa-token.yaml
```

To retrieve the token:
```shell
kubectl -n spexity-cicd get secret gh-deployer-token -o jsonpath='{.data.token}' | base64 -d
```

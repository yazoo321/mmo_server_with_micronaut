## Getting Azure credentials
1. register / login to Azure
2. Create a new subscription, where you will get yourself the subscription ID
3. Execute: az ad sp create-for-rbac --name "<service-principal-name>" --role="Contributor" --scopes="/subscriptions/<subscription-id>"
4. the command will return the new client id and secret that you will need to populate main.tf file with (and the subscription ID)
5. You can override them in your env variables, or create terraform.tfvars file specifying the variable values

## Deploying application
In order to deploy the application, we would use a container registry

### Create container registry:
`az acr create --resource-group myGameResourceGroup --name openmmoregistry --sku Basic`

(change myGameResourceGroup to your resource group)
(change openmmoregistry to your desired name)

login:
`az acr login --name openmmoregistry`

Tag your docker image with ACR login server:
`docker tag myapp/mmo-server openmmoregistry.azurecr.io/mmo-server`

### Push image to ACR:
`docker push myContainerRegistry.azurecr.io/mmo-server`

## Building the docker image

docker file contents:
```
FROM openjdk:17-jdk-alpine
COPY mmo_server-0.8.2-all.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

change the `mmo_server-0.8.2-all.jar` -> with the jar name that you have in your build/libs folder

build the docker and push using:
```
docker build -t myapp/mmo-server .
docker tag myapp/mmo-server openmmoregistry/myapp/mmo-server
docker push openmmoregistry/myapp/mmo-server
```

### You may need to re-auth your docker
- `docker logout`
- `az login`
- `az acr login --name openmmoregistry`
- `docker tag myapp/mmo-server openmmoregistry.azurecr.io/myapp/mmo-server`
- `docker push openmmoregistry.azurecr.io/myapp/mmo-server`

## Integrate AKS with ACR
- `az aks update -n <aks-cluster-name> -g <resource-group-name> --attach-acr <acr-name>`
- `az aks update -n myAKSCluster -g myGameResourceGroup --attach-acr openmmoregistry`


## Debugging
a useful way to debug is using kubectl

First you'd need to get credentials:
- `az aks get-credentials --resource-group <resource_group> --name <aks-cluster-name>`

for example
- `az aks get-credentials --resource-group myGameResourceGroup --name myAKSCluster`

1. `kubectl get pods` (can extend to `kubectl get pods --all-namespaces`)
2. alternatively: `kubectl get pods -n micronaut-namespace`
3. `kubectl logs <pod-name> --all-containers=true`

another example:

getting pod: `main          kafka-6d74548c5f-v82xh                0/1     CrashLoopBackOff   3 (30s ago)     82s`
check its logs using: 
`kubectl logs kafka-6d74548c5f-v82xh -n main`

### Debug docker images
`az acr repository list --name openmmoregistry --output table`

### Get services, e.g. mongo, redis, kafka
`kubectl get svc`
you may need to specify namespace:
`kubectl get svc -n main`

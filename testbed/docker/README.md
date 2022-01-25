# Testbed Client

## client

The client for the testbed is a Docker container with kubectl, cmctl and ssh client.
Administration tasks for the test bench can be carried out with the client.

```bash
docker build -t nubes/client:latest .
```

execute `kubectl apply -k $OVERLAY_OR_BASE`
```bash
docker run --rm \
  --mount type=bind,source=$PROJECT_DIR/testbed/src/base,target=/opt/app/base \
  --mount type=bind,source=$PROJECT_DIR/testbed/build/run/kubeconfig,target=/root/.kube/config \
  nubes/client:latest \
 kubectl apply -k base
```

Get Root Certificate:

```bash
kubectl get secret -n cert-manager nubes-ca -o json | jq -r '.data."tls.crt"' | base64 -d | openssl x509
```
bzw:

```bash
docker run --rm -it \
  --mount type=bind,source=$PROJECT_DIR/testbed/src/base,target=/opt/app/base \
  --mount type=bind,source=$PROJECT_DIR/testbed/build/run/kubeconfig,target=/root/.kube/config \
  nubes/client:latest \
  bash -c "kubectl get secret -n cert-manager nubes-ca -o json | jq -r '.data.\"tls.crt\"' | base64 -d | openssl x509 "
```


## pki

Create root CA:

```bash
docker build -t nubes/pki:latest .
docker run --rm --env BUILD_DIR=/tmp --mount type=bind,source=$PROJECT_DIR/testbed/build/run/kubeconfig,target=/root/.kube/config  nubes/pki:latest
```
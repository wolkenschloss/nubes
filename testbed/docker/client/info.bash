#!/usr/bin/env bash

CACHE=$HOME/.cache
mkdir -p "$CACHE"

echo openssl version
openssl version

echo
echo kubectl version
kubectl version --short

echo
echo cmctl version
cmctl version --short

echo
echo registry catalog
curl -s https://registry.wolkenschloss.local/v2/_catalog \
  --cacert /usr/local/share/ca-certificates/ca.crt \
  | jq --raw-output '.repositories[]'

echo
echo list certificates
kubectl get secrets --all-namespaces --field-selector type=kubernetes.io/tls --cache-dir="$CACHE"

# kubectl get secrets nubes-ca -n cert-manager --cache-dir=.cache -o go-template='{{index .data "tls.crt"}}' | base64 -d
# kubectl get secrets --all-namespaces --field-selector type=kubernetes.io/tls --cache-dir="$CACHE" -o go-template='{{range .items}}{{index .data "tls.crt"}}{{"\n"}}{{end}}' | base64 -d | openssl x509 -text
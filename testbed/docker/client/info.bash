#!/usr/bin/env bash

echo openssl version
openssl version

echo
echo kubectl version
kubectl version

echo
echo cmctl version
cmctl version

echo registry catalog
curl https://registry.wolkenschloss.local/v2/_catalog --cacert /usr/local/share/ca-certificates/ca.crt -i

echo list certificates
kubectl get secrets --all-namespaces --field-selector type=kubernetes.io/tls
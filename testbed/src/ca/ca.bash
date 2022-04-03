#! /usr/bin/env bash

# Install cert-manager and root ca

SSL_DIR=$(realpath "${1?"usage: xxx BUILD_DIR"}")
export SSL_DIR
echo "SSL Directory is $1"

cert_manager_version="v1.7.0-beta.0"
root_ca_crt="/opt/app/ca.crt"
root_ca_key="/opt/app/ca.key"

echo "running checks"
kubectl get secret nubes-ca -n cert-manager > /dev/null
secret_exists=$?

set -e
set -ou pipefail

echo "setup script functions"

if [ $secret_exists -ne 0 ]
then
  echo "creating secret"
  kubectl create secret tls nubes-ca --key "$root_ca_key" --cert "$root_ca_crt" -n cert-manager
  kubectl apply -f /opt/app/ca-issuer.yaml
fi


echo "Current working directory: $(pwd)"
ls -lhaR /opt/app
id
# print certificate to stdout
#kubectl get secret -n cert-manager nubes-ca -o json | jq -r '.data."tls.crt"' | base64 -d | openssl x509
# cat "$root_ca_crt"

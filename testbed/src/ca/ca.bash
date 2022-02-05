#! /usr/bin/env bash

# Creates a self signed root CA and installs cred-manager
#
# return ca on stdout

SSL_DIR=$(realpath "${1?"usage: xxx BUILD_DIR"}")
export SSL_DIR
echo "SSL Directory is $1"

cert_manager_version="v1.7.0-beta.0"
root_ca_crt="/opt/app/ca.crt"
root_ca_key="/opt/app/ca.key"

echo "running checks"
kubectl get secret nubes-ca -n cert-manager > /dev/null
secret_exists=$?

kubectl get namespace cert-manager > /dev/null
cert_manager_installed=$?

set -e
set -ou pipefail

echo "setup script functions"

function create_secret() {
#  if [ $secret_exists -ne 0 ]
#  then
    echo "creating secret"
    kubectl create secret tls nubes-ca --key "$root_ca_key" --cert "$root_ca_crt" -n cert-manager
#  fi
}

function install_ca() {
#  if [ $cert_manager_installed -ne 0 ]
#  then
      echo "installing cert manager"

      kubectl apply -f https://github.com/jetstack/cert-manager/releases/download/$cert_manager_version/cert-manager.yaml
      create_secret
      cmctl check api --wait=10m
      kubectl apply -f /opt/app/ca-issuer.yaml
#  fi
}

echo "Current working directory: $(pwd)"
ls -lhaR /opt/app
id

install_ca

# print certificate to stdout
#kubectl get secret -n cert-manager nubes-ca -o json | jq -r '.data."tls.crt"' | base64 -d | openssl x509
# cat "$root_ca_crt"


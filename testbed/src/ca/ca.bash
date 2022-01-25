#! /usr/bin/env bash

# Creates a self signed root CA and installs cred-manager
#
# return ca on stdout

export DOMAIN_SUFFIX="nubes.local"
export ORGANIZATION="Wolkenschloss"
export COUNTRY="DE"
export BUILD_DIR="../build"
SSL_DIR=$(realpath "${1?"usage: xxx BUILD_DIR"}")
export SSL_DIR
echo "SSL Directory is $1"

cert_manager_version="v1.7.0-beta.0"
root_ca_conf="ca.conf"
root_ca_csr="$SSL_DIR/ca.csr"
root_ca_crt="$SSL_DIR/ca.crt"
root_ca_key="$SSL_DIR/private/ca.key"
root_ca_crl="$SSL_DIR/ca.crl"

echo "running checks"
kubectl get secret nubes-ca -n cert-manager > /dev/null
secret_exists=$?

test -f "$root_ca_key"
key_exists=$?

kubectl get namespace cert-manager > /dev/null
cert_manager_installed=$?

set -e
set -ou pipefail

echo "setup script functions"

# Create Root CA Directory Structure
function create_directory() {
  echo "creating target directory $SSL_DIR"
  mkdir -p "$SSL_DIR"/{certs,db,private}
  chmod 700 "$SSL_DIR"/private/
  touch "$SSL_DIR"/db/index
  openssl rand -hex 16 > "$SSL_DIR"/db/serial
  echo 1001 > "$SSL_DIR"/db/crlnumber
}

# Root CA Generation
function create_ca() {
  echo "creating root ca"
  openssl req -new -config $root_ca_conf -out "$root_ca_csr" -keyout "$root_ca_key" -nodes
  openssl ca -selfsign -config $root_ca_conf -in "$root_ca_csr" -out "$root_ca_crt" -extensions ca_ext -batch
  openssl ca -gencrl -config $root_ca_conf -out "$root_ca_crl"
}

function create_secret() {
  echo "creating secret"
  kubectl create secret tls nubes-ca --key "$root_ca_key" --cert "$root_ca_crt" -n cert-manager
}

function delete_secret() {
  echo "deleting secret"
  kubectl delete secret nubes-ca -n cert-manager
}

function install_ca() {
  echo "installing cert manager"

  kubectl apply -f https://github.com/jetstack/cert-manager/releases/download/$cert_manager_version/cert-manager.yaml
  create_secret
  cmctl check api --wait=10m
  kubectl apply -f /opt/app/ca-issuer.yaml
}

echo "make decision"
# 1. Fall: Secret existiert und $SSL_DIR enthält Daten:
if [ $secret_exists -eq 0 ] && [ $key_exists -eq 0 ]
then
  echo "Kubernetes secret exists. CA private key exists. i'll to nothing"
fi

# 2. Fall: Secret existiert nicht und $SSL_DIR enthält Daten:
if [ $secret_exists -ne 0 ] && [ $key_exists -eq 0 ]
then
  echo "CA private key exists. Secret is missing. Creating secret..."
  install_ca
fi

# 3. Fall: Secret existiert und $SSL_DIR enthält keine Daten:
if [ $secret_exists -eq 0 ] && [ $key_exists -ne 0 ]
then
  echo "CA private key is missing. Kubernetes secret exists. Creating new CA and secret..."
  delete_secret
  create_directory
  create_ca
  install_ca
fi

# 4. Fall: Secret existiert nicht und $SSL_DIR enthält keine Daten:
if [ $secret_exists -ne 0 ] && [ $key_exists -ne 0 ]
then
  echo "CA is missing. Kubernetes Secret ist missing. Creating new CA and secret..."
  create_directory
  create_ca
  install_ca
fi

# print certificate to stdout
kubectl get secret -n cert-manager nubes-ca -o json | jq -r '.data."tls.crt"' | base64 -d | openssl x509

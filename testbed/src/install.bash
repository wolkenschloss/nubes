#!/usr/bin/env bash

export BUILD_DIR="../build/ssl"

kubectl apply -f https://github.com/jetstack/cert-manager/releases/download/v1.6.1/cert-manager.yaml
kubectl create secret tls nubes-ca --key $root_ca_key --cert $root_ca_crt -n cert-manager
# Now Import Certificate in the trust store
#sudo cp $SSL_DIR/root-ca.crt /usr/local/share/ca-certificates
#sudo update-ca-certificates
kubectl apply -f ca-issuer.yaml
kubectl apply -f registry-ingress.yaml

kubectl create secret tls nubes-ca --key $BUILD_DIR/private/root-ca.key --cert $BUILD_DIR/root-ca.crt -n cert-manager
kubectl apply -f ca-issuer.yaml
kubectl apply -f registry-ingress.yaml
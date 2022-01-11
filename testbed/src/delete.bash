#!/usr/bin/env bash

kubectl delete ClusterIssuer ca-issuer -n cert-manager
kubectl delete Ingress registry-ingress -n container-registry
kubectl delete Secret nubes-ca -n cert-manager
kubectl delete Secret registry-cert -n container-registry
#!/usr/bin/env bash

function random_password() {
#  tr -dc A-Za-z0-9 </dev/urandom | head -c 13 ; echo ''
  echo -n "password"
}

function result () {
cat << EOF
apiVersion: config.kubernetes.io/v1
kind: ResourceList
items:
  - apiVersion: v1
    kind: Secret
    metadata:
      name: $name
      annotations:
        kustomize.config.k8s.io/needs-hash: "false"
    type: Opaque
    data:
      username: $(echo -n $user | base64)
      password: $(random_password | base64)
EOF
}

export $(yq e ".functionConfig.spec.template" -op - | tr -d '[ ]' | xargs)
result

exit 0
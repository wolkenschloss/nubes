#!/usr/bin/env bash

SCRIPT=$(readlink -f "$0")
SCRIPT_PATH=$(dirname "$SCRIPT")
BUILD_DIR=$(realpath "$SCRIPT_PATH"/../build/run)

docker run --rm -it \
  --mount type=bind,source="$BUILD_DIR/kubeconfig",target="$HOME/.kube/config" \
  --mount type=bind,source="$BUILD_DIR/known_hosts",target="$HOME/.ssh/known_hosts" \
  --mount type=bind,source="$HOME/.ssh/id_rsa",target="$HOME/.ssh/id_rsa" \
  --mount type=bind,source="$BUILD_DIR/hosts",target="/etc/hosts" \
  --mount type=bind,source="$BUILD_DIR/../ca/ca.crt",target="/usr/local/share/ca-certificates/ca.crt" \
  nubes/client:latest

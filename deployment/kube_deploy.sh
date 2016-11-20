#!/usr/bin/env bash

curl -O https://storage.googleapis.com/kubernetes-release/release/v1.4.6/bin/linux/amd64/kubectl
chmod +x kubectl

./kubectl config set-cluster aws-dave --server ${KUBE_API}
./kubectl config set-cluster aws-dave --insecure-skip-tls-verify=true
./kubectl config set-credentials dave-admin --username=${KUBE_USERNAME} --token=${KUBE_PASSWORD}
./kubectl config set-context dave --cluster=aws-dave --user=dave-admin
./kubectl config use-context dave
./kubectl set image deployment/dave-deployment dave=scholzj/dave:${CIRCLE_SHA1}

#!/usr/bin/env bash

curl -O https://storage.googleapis.com/kubernetes-release/release/v1.3.7/bin/linux/amd64/kubectl
chmod +x kubectl

./kubectl config set-cluster aws-kubernetes --server ${K8S_MASTER}
./kubectl config set-cluster aws-kubernetes --insecure-skip-tls-verify=true
./kubectl config set-credentials aws-admin --username=${K8S_USERNAME} --password=${K8S_PASSWORD}
./kubectl config set-context aws --cluster=aws-kubernetes --user=aws-admin
./kubectl config use-context aws
./kubectl set image deployment/dave-deployment dave=scholzj/dave:${CIRCLE_SHA1}
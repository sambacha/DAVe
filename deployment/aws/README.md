# AWS deployment

This directory contains the DAVe deployment into Kubernetes cluster running on Amazon AWS. The deployment is using Terraform to deploy the infrastructure andf Ansible to setup the Kubernetes on top of it. It is heavily inspired by https://opencredo.com/kubernetes-aws-terraform-ansible-1/.

## TODOs

* ~~Integrate with AWS cloud provider~~
* Move into private network to make sure that hosts are not accessible directly from outside
* Move accross two zones to make it more HA
* Bootstrap the nodes so that we can use auto scaling groups easily

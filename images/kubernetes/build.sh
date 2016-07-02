#!/bin/bash

docker build -t llparse/k8s:dev .
docker push llparse/k8s:dev
gsync llparse/k8s:dev

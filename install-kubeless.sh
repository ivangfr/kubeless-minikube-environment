#!/usr/bin/env bash

echo
echo "Create 'kubeless' namespace"
echo "---------------------------"

kubectl create namespace kubeless

export RELEASE=v1.0.5

echo
echo "Create Kubeless version $RELEASE"
echo "--------------------------------"

kubectl create -f https://github.com/kubeless/kubeless/releases/download/$RELEASE/kubeless-$RELEASE.yaml

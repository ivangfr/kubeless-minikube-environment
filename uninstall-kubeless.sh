#!/usr/bin/env bash

export RELEASE=v1.0.5
kubectl delete -f https://github.com/kubeless/kubeless/releases/download/$RELEASE/kubeless-$RELEASE.yaml

kubectl delete namespace kubeless

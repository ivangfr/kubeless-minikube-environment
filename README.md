# `kubeless-minikube-environment`

The goal of this project is to setup [`Kubeless`](https://kubeless.io/) in [`Minikube`](https://github.com/kubernetes/minikube) and then, deploy and run some functions.

## Prerequisites

You must have installed in your machine:
- a recent version of [`Apache Maven`](https://maven.apache.org/);
- [`JDK 1.8`](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or later;
- `Kubectl` (how to install [here](https://kubernetes.io/docs/tasks/tools/install-kubectl/))
- `Minikube` (how to install [here](https://kubernetes.io/docs/tasks/tools/install-minikube/))
- `Kubeless CLI` (can be downloaded from the [release page](https://github.com/kubeless/kubeless/releases))

## Examples

- ### [book-api-java](https://github.com/ivangfr/kubeless-minikube-environment/tree/master/book-api-java)
- ### [greetting-java](https://github.com/ivangfr/kubeless-minikube-environment/tree/master/greeting-java)

## Start Environment

### Start Minikube

Open a terminal and start `Minikube` by running the following command. The properties `--memory`, `--cpus` and `--vm-driver` are optional.
```
minikube start --memory=8192 --cpus=4 --vm-driver='virtualbox'
```

### Install Kubeless

In `kubeless-minikube-environment` directory, run the script below to install `Kubeless` in `Minikube`
```
./install-kubeless.sh
```

Check `Pods` in `kubeless` namespace
```
kubectl get pods --namespace kubeless
```

## Shutdown Environment

### Uninstall Kubeless

In `kubeless-minikube-environment` directory, run the script below to uninstall `Kubeless`
```
./uninstall-kubeless.sh
```

### Shutdown Minikube

The following command shuts down the `Minikube Virtual Machine`, but preserves all cluster state and data. Starting the cluster again will restore it to it’s previous state.
```
minikube stop
```

The command shuts down and deletes the `Minikube Virtual Machine`. No data or state is preserved.
```
minikube delete
```

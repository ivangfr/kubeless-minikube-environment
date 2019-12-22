# `kubeless-minikube-environment`
## `> greeting-java`

The goal of this example is to deploy in the `Kubeless` cluster the `greeting-java` function

## Start Minikube and install Kubeless

First of all, start `Minikube` and install `Kubeless` as explained at [Start Environment](https://github.com/ivangfr/kubeless-minikube-environment#start-environment) in the main README.

## Install greeting-java Function 

1. Open a terminal and navigate to `kubeless-minukube-environment/greeting-java`

1. Install `greeting-java` function by running the following command
   ```
   kubeless function deploy greeting-java -n kubeless \
   --runtime java1.8 --handler Greeting.greet --from-file Greeting.java
   ```

1. Get a description of the function
   ```
   kubeless function describe -n kubeless greeting-java
   ```

1. Check function status
   ```
   kubeless function ls -n kubeless
   ```
   - Continue to the next step when the `STATUS` of the function is `1/1 READY`

   - **Troubleshooting**

     In case the function doesn't get ready, run the following command to get more details
     ```
     kubectl describe pod -n kubeless <greeting-java-running-pod>
     ```

1. Call function
   ```
   kubeless function call -n kubeless greeting-java --data 'Ivan'
   ````

1. Check the function logs
   ```
   kubeless function logs -n kubeless greeting-java
   ```

1. Check the function configmaps (source code can be seeing here)
   ```
   kubectl describe configmaps -n kubeless greeting-java
   ```

1. Delete function
   ```
   kubeless function delete -n kubeless greeting-java
   ```



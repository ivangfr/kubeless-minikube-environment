# `kubeless-minikube-environment`
## `> book-api-java`

The goal of this example is to deploy in the `Kubeless` cluster a couple of Kubeless functions to handle books: `get-books`, `get-book`, `add-book` and `remove-book`.

## Prerequisites

### kubeless-maven-plugin

This example uses a [`Maven Plugin`](https://maven.apache.org/plugins/) called [`kubeless-maven-plugin`](https://github.com/ivangfr/kubeless-maven-plugin). The main goal of this plugin it to read a class in `/src/main/java/io/kubeless` directory and the `pom.xml` of the project and converts them into a ready to use inputs in `kubeless function deploy` command.

In order to install `kubeless-maven-plugin` in your local `Maven` repository, run
```
git clone https://github.com/ivangfr/kubeless-maven-plugin.git
cd kubeless-maven-plugin.
mvn clean install
```

## Start Minikube and install Kubeless

First of all, start `Minikube` and install `Kubeless` as explained at [Start Environment](https://github.com/ivangfr/kubeless-minikube-environment#start-environment) in the main README.

## Install MySQL

Let's install [`MySQL`](https://www.mysql.com/) as `book-api-java` functions require it
```
helm install my-mysql \
--namespace kubeless \
--set imageTag=5.7.28 \
--set mysqlDatabase=bookdb \
--set mysqlRootPassword=secret \
--set mysqlUser=bookuser \
--set mysqlPassword=bookpass \
--set persistence.enabled=false \
stable/mysql
```

## Create book-api-java Kubeless files

In `kubeless-minikube-environment/book-api-java` directory, run the command below to create the files that will be used when calling the command `kubeless function deploy`: `BookResource.java` and `pom.xml`;
```
mvn clean compile
```

## Install book-api-java functions

In `kubeless-minikube-environment/book-api-java` directory, run the following commands

- `get-books`
  ```
  kubeless function deploy get-books -n kubeless --runtime java1.8 \
  --env MYSQL_HOST=my-mysql --env MYSQL_USER=bookuser --env MYSQL_PASSWORD=bookpass \
  --handler BookResource.getBooks --from-file target/generated-sources/kubeless/BookResource.java --dependencies target/generated-sources/kubeless/pom.xml
  ```

- `get-book`
  ```
  kubeless function deploy get-book -n kubeless --runtime java1.8 \
  --env MYSQL_HOST=my-mysql --env MYSQL_USER=bookuser --env MYSQL_PASSWORD=bookpass \
  --handler BookResource.getBook --from-file target/generated-sources/kubeless/BookResource.java --dependencies target/generated-sources/kubeless/pom.xml
  ```

- `add-book`
  ```
  kubeless function deploy add-book -n kubeless --runtime java1.8 \
  --env MYSQL_HOST=my-mysql --env MYSQL_USER=bookuser --env MYSQL_PASSWORD=bookpass \
  --handler BookResource.addBook --from-file target/generated-sources/kubeless/BookResource.java --dependencies target/generated-sources/kubeless/pom.xml
  ```

- `remove-book`
  ```
  kubeless function deploy remove-book -n kubeless --runtime java1.8 \
  --env MYSQL_HOST=my-mysql --env MYSQL_USER=bookuser --env MYSQL_PASSWORD=bookpass \
  --handler BookResource.removeBook --from-file target/generated-sources/kubeless/BookResource.java --dependencies target/generated-sources/kubeless/pom.xml
  ```

> **Troubleshooting**
> In case the function doesn't get ready, run the following command to get more details
> ```
> kubectl describe pod -n kubeless <function-name-running-pod>
> ```

### Useful Commands

- Check function status
  ```
  kubeless function ls -n kubeless
  ```

- Get a description of the function
  ```
  kubeless function describe -n kubeless <function-name>
  ```

- Check the function logs
  ```
  kubeless function logs -n kubeless <function-name>
  ```

- Check the function `configmaps` (source code can be seeing here)
  ```
  kubectl describe configmaps <function-name>
  ```

### Call book-api-java functions

- `get-books`
  ```
  kubeless function call -n kubeless get-books
  ```

- `add-book`
  ```
  kubeless function call -n kubeless add-book --data '{"isbn":"123", "title": "Learn Kubeless"}'
  ```

- `get-book`
  ```
  kubeless function call -n kubeless get-book --data 1
  ```

- `remove-book`
  ```
  kubeless function call -n kubeless remove-book --data 1
  ```

## Shutdown

- Delete `book-api-java` functions
  ```
  kubeless function delete -n kubeless get-books
  kubeless function delete -n kubeless get-book
  kubeless function delete -n kubeless add-book
  kubeless function delete -n kubeless remove-book
  ```

- Delete `MySQL`
  ```
  helm delete --namespace kubeless my-mysql
  ```


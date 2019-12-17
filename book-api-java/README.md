```
docker run -d --rm \
--name mysql \
-p 3306:3306 \
-e MYSQL_DATABASE=bookdb \
-e MYSQL_USER=bookuser \
-e MYSQL_PASSWORD=bookpass \
-e MYSQL_ROOT_PASSWORD=secret \
mysql:5.7.28
```

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

```
kubeless function deploy get-books -n kubeless --runtime java1.8 \
--env MYSQL_HOST=my-mysql --env MYSQL_USER=bookuser --env MYSQL_PASSWORD=bookpass \
--handler BookResource.getBooks --from-file function/BookResource.java --dependencies function/pom.xml

kubeless function deploy get-book -n kubeless --runtime java1.8 \
--env MYSQL_HOST=my-mysql --env MYSQL_USER=bookuser --env MYSQL_PASSWORD=bookpass \
--handler BookResource.getBook --from-file function/BookResource.java --dependencies function/pom.xml

kubeless function deploy add-book -n kubeless --runtime java1.8 \
--env MYSQL_HOST=my-mysql --env MYSQL_USER=bookuser --env MYSQL_PASSWORD=bookpass \
--handler BookResource.addBook --from-file function/BookResource.java --dependencies function/pom.xml

kubeless function deploy remove-book -n kubeless --runtime java1.8 \
--env MYSQL_HOST=my-mysql --env MYSQL_USER=bookuser --env MYSQL_PASSWORD=bookpass \
--handler BookResource.removeBook --from-file function/BookResource.java --dependencies function/pom.xml
```

```
kubeless function call -n kubeless get-books
kubeless function call -n kubeless add-book --data '{"isbn":"123", "title": "Learn Kubeless"}'
kubeless function call -n kubeless get-book --data 1
kubeless function call -n kubeless remove-book --data 1
```

```
kubeless function delete -n kubeless get-books
kubeless function delete -n kubeless get-book
kubeless function delete -n kubeless add-book
kubeless function delete -n kubeless remove-book
```

```
helm delete --namespace kubeless my-mysql
```


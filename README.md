# <img height="25" src="./images/AILLogoSmall.png" width="40"/> audit-service

<a href="https://www.legosoft.com.mx"><img height="150px" src="./images/Icon.png" alt="AI Legorreta" align="left"/></a>
Microservice that acts as the system general auditory repository.

The `audit-service` stores all events that need to be persisted for auditory purpose. All this data is store in a 
Mongo database to be mor efficient taht using a relational database. It listens to all Kafka events that have a
`audit` as a topic and stores it in a MongoDB. The front microservice `sys-ui` that has READ access is the one to
display all data using GraphQL and Querydsl for MongoDB. The access to MongoDB is using the reactive driver for
`audit-rective-service`.

note: this microservice depends on the Kafka to listen notifications with the `audit` topic.


## Introduction

This is a microservice that stores all system auditory registers in MongoDB.

All transaction data that wants to be persisted for auditory purpose needs to send a Kafka message with the `audit`
topic.

## Event Request

The **Event Request** is a **JSON** with the follow properties:

* **username:** The name of the user that generates the event.
* **correlationId:** This id helps to a listener application to follow a track of events. This id is generated by the Zuul API Gateway and keep the same until all micro services involved in the transaction finished.  It is important this trasaction for the Zipkin and Graylog montior servers.
* **eventType:** This event type tell if our event will be stored or not, its a **enum** that accepts these values:
  * DB_STORE: Store only into a data base.
  * FILE_STORE: Store only into a .TXT file.
  * FULL_STORE: Store into a data base and .TXT file.
  * NON_STORE: No store the event.
* **eventName:** This is the name of the event for example: *saveUser.*
* **applicationName**: Is the name of the micro service or front end application that generates the event.
* **coreName**: Is the group which it belongs the application name.  In other words is the topic that the listener will suscribe.
* **body:** The body is the action of the event, if we create a user the body is the result of create a new user.


### Query events from mongoDB

This microservice utilizes mongoDB to store the events, GraphQL as an API to query events in  QueryDsl as repositories.

For more information see example:

spring-graphql-examples or visit link:

https://github.com/hantsy/spring-graphql-sample

Or the official repository (see examples) for Spring for GraphQL

https://github.com/spring-projects/spring-graphql


For QueryDSL mongoDB examples visit:

https://github.com/spring-projects/spring-data-examples/tree/main/mongodb/querydsl


Since we need to generate the 'Q' classes for the QueryDsl framework, sometimes we need to generate the classes first
and therefor execute:

```bash
./gradlew compile
```

### System parameters database


### Create the image manually

```
./gradlew bootBuildImage
```

### Publish the image to GitHub manually

```
./gradlew bootBuildImage \
   --imageName ghcr.io/rlegorreta/audit-service \
   --publishImage \
   -PregistryUrl=ghcr.io \
   -PregistryUsername=rlegorreta \
   -PregistryToken=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
```

### Publish the image to GitHub from the IntelliJ

To publish the image to GitHub from the IDE IntelliJ a file inside the directory `.github/workflows/commit-stage.yml`
was created.

To validate the manifest file for kubernetes run the following command:

```
kubeval --strict -d k8s
```

This file compiles de project, test it (for this project is disabled for some bug), test vulnerabilities running
skype, commits the code, sends a report of vulnerabilities, creates the image and lastly push the container image.

<img height="340" src="./images/commit-stage.png" width="550"/>

For detail information see `.github/workflows/commit-stage.yml` file.


### Run the image inside the Docker desktop

```
docker run \
    --net ailegorretaNet \
    -p 8351:8300 \
    -e SPRING_PROFILES_ACTIVE=local \
    audit-service
```

Or a better method use the `docker-compose` tool. Go to the directory `ailegorreta-deployment/docker-platform` and run
the command:

```
docker-compose up
```

## Run inside Kubernetes

### Manually

If we do not use the `Tilt`tool nd want to do it manually, first we need to create the image:

Fist step:

```
./gradlew bootBuildImage
```

Second step:

Then we have to load the image inside the minikube executing the command:

```
image load ailegorreta/audit-service --profile ailegorreta 
```

To verify that the image has been loaded we can execute the command that lists all minikube images:

```
kubectl get pods --all-namespaces -o jsonpath="{..image}" | tr -s '[[:space:]]' '\n' | sort | uniq -c\n
```

Third step:

Then execute the deployment defined in the file `k8s/deployment.yml` with the command:

```
kubectl apply -f k8s/deployment.yml
```

And after the deployment can be deleted executing:

```
kubectl apply -f k8s/deployment.yml
```

Fourth step:

For service discovery we need to create a service applying with the file: `k8s/service.yml` executing the command:

```
kubectl apply -f k8s/service.yml
```

And after the process we can delete the service executing:

```
kubectl deltete -f k8s/service.yml
```

Fifth step:

If we want to use the project outside kubernetes we have to forward the port as follows:

```
kubectl port-forward service/audit-service 8351:80
```

Appendix:

If we want to see the logs for this `pod` we can execute the following command:

```
kubectl logs deployment/audit-service
```

### Using Tilt tool

To avoid all these boilerplate steps is much better and faster to use the `Tilt` tool as follows: first create see the
file located in the root directory of the proyect called `TiltFile`. This file has the content:

```
# Tilt file for audit-service
# Build
custom_build(
    # Name of the container image
    ref = 'audit-service',
    # Command to build the container image
    command = './gradlew bootBuildImage --imageName $EXPECTED_REF',
    # Files to watch that trigger a new build
    deps = ['build.gradle', 'src']
)

# Deploy
k8s_yaml(['k8s/deployment.yml', 'k8s/service.yml'])

# Manage
k8s_resource('audit-service', port_forwards=['8351'])
```

To execute all five steps manually we just need to execute the command:

```
tilt up
```

In order to see the log of the deployment process please visit the following URL:

```
http://localhost:10350
```

Or execute outside Tilt the command:

```
kubectl logs deployment/audit-service
```

In order to undeploy everything just execute the command:

```
tilt down
```

To run inside a docker desktop the microservice need to use http://cach-service:8300 path


### Reference Documentation
This microservice uses the recent Spring Gateway :

* [Spring Boot Gateway](https://cloud.spring.io/spring-cloud-gateway/reference/html/)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.0.1/maven-plugin/reference/html/)
* [Config Client Quick Start](https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_client_side_usage)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/3.0.1/reference/htmlsingle/#production-ready)

### Links to Springboot 3 Observability

https://tanzu.vmware.com/developer/guides/observability-reactive-spring-boot-3/

Baeldung:

https://www.baeldung.com/spring-boot-3-observability



### Contact AI Legorreta

Feel free to reach out to AI Legorreta on [web page](https://legosoft.com.mx).


Version: 2.0.0
©LegoSoft Soluciones, S.C., 2023

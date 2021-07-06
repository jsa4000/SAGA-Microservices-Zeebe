# Zeebe

![Zeebe](./images/zeebe.jpg)

With Zeebe you can:

* Define workflows graphically in BPMN 2.0
* Choose any gRPC-supported programming language to implement your workers
* Build workflows that react to events from Apache Kafka and other messaging platforms
* Use a SaaS offering or deploy with Docker and Kubernetes (in the cloud or on-premises)
* Scale horizontally to handle very high throughput
* Rely on fault tolerance and high availability for your workflows
* Export workflow data for monitoring and analysis
* Engage with an active community

## Installation

### Docker

This installation requires `docker-compose` and will install the following applications:

* Zeebe Worker
* Zeebe Operate
* ElasticSearch instances.

To perform the installation simply use

`docker-compose -f docker/docker-compose.yaml up`

Zeebe operate can be accessed using this URL (http://localhost:8080) (demo/demo)

In order to connect to Zeebe Workers via Gateway use the `localhost:26500`

### Kubernetes

![Helm Chart](./images/charts.png)

Zeebe is going to be installed in a Kubernetes cluster using Helm chart, since it is a non experimental method.

Requirements for the installation:

* Helm >= 3.x +
* Kubernetes >= 1.8
* Minimum cluster requirements include the following to run this chart with default settings. All of these settings are configurable.
  * Three Kubernetes nodes to respect the default "hard" affinity settings.
  * 1GB of RAM for the JVM heap

Follownig are the main repositories that are going to be used.

* [Zeebe Fulll Helm](https://github.com/zeebe-io/zeebe-full-helm). This is the main helm chart. This chart has some dependencies. Subcharts and other components such as elasticsearch, kibana, Prometheus, etc are needed in order to install Zeebe in a Kubernetes cluster.
* [Zeebe Profiles](https://github.com/zeebe-io/zeebe-helm-profiles). Some default profiles with vanilla configuration depending on the environment to be deployed

1. Clone Profiles repositories to customize some values depending on the environment.

     ```bash
    # Clone repository or directly use the URL with the profile (yaml) installing the HELM chart
    git clone https://github.com/zeebe-io/zeebe-helm-profiles.git
    ```

2. Setup Zeebe Helm repository

    ```bash
    helm3 repo add zeebe https://helm.camunda.io
    helm3 repo update
    ```

3. Install `Zeebe` using the dev profile wiith the minimaal installation (default namespace)

    ```bash
    helm3 install zeebe-dev --namespace zeebe --create-namespace zeebe/zeebe-full-helm --version 0.0.143 -f kubernetes/zeebe-dev-profile.yaml

    # With custom values
    helm3 install zeebe-dev --namespace zeebe --create-namespace zeebe/zeebe-full-helm --version 0.0.143 --set zeeqs.enabled=true -f kubernetes/zeebe-dev-profile.yaml

    # Extract templates
    helm3 template zeebe-dev --namespace zeebe --create-namespace zeebe/zeebe-full-helm --version 0.0.143 -f kubernetes/zeebe-dev-profile.yaml > helm-values.yaml
    ```

4. Check all the pods are running in the cluster.

    `kubectl get pods -n zeebe`

    > Since it is the dev profile it must be just one replica per component installed.

    ```bash
    NAME                                                  READY   STATUS    RESTARTS   AGE
    elasticsearch-master-0                                1/1     Running   0          4m49s
    zeebe-dev-ingress-nginx-controller-69557db77c-fz9ln   1/1     Running   0          4m49s
    zeebe-dev-zeebe-0                                     1/1     Running   0          4m49s
    zeebe-dev-zeebe-gateway-67c9dc4446-dbcqh              1/1     Running   0          4m49s
    zeebe-dev-zeebe-operate-helm-f7c48f96c-km8xs          1/1     Running   0          4m49s
    ```

5. Check the services created

    `kubectl get svc -n zeebe`

    ```bash
    elasticsearch-master                           ClusterIP      10.103.158.218   <none>        9200/TCP,9300/TCP              29m
    elasticsearch-master-headless                  ClusterIP      None             <none>        9200/TCP,9300/TCP              29m
    zeebe-dev-ingress-nginx-controller             LoadBalancer   10.105.72.236    localhost     80:30131/TCP,443:31296/TCP     29m
    zeebe-dev-ingress-nginx-controller-admission   ClusterIP      10.96.216.239    <none>        443/TCP                        29m
    zeebe-dev-zeebe                                ClusterIP      None             <none>        9600/TCP,26502/TCP,26501/TCP   29m
    zeebe-dev-zeebe-gateway                        ClusterIP      10.97.13.10      <none>        9600/TCP,26500/TCP             29m
    zeebe-dev-zeebe-operate-helm                   ClusterIP      10.111.15.0      <none>        80/TCP                         29m
    ```

6. Test `Zeebe Operate` using the loadbalancer created by the ingress controller. (http://localhost/#/login) (demo/demo)

    > If the LoadBalancer it is not available in the kubernetes cluster use `port-forward` or `NodePort` to access.

    ```bash
    # Bind the port from the current service 80 to the local 8080. http://localhost:8080/#/login
    kubectl port-forward -n zeebe svc/zeebe-dev-ingress-nginx-controller 8080:80
    ```

7. Communication inside the Cluster using `Zeebe Gateway`

    > In order to interact with the services inside the cluster you need to use port-forward to route traffic from your environment to the cluster.

    ```bash
    # This allows from dev to connect through the cluster using local java applications or Zeebe CLI
    kubectl port-forward -n zeebe svc/zeebe-dev-zeebe-gateway 26500:26500
    ```

## Workflows

* [Friendly Enough Expression Language](https://zeebe.io/blog/2020/09/feel/)

### Camunda Modeler

In order to create and view Zeebe workflows, it is recommended to use the Tool `Camunda Modeler`

> [Zeebe Modeler](https://github.com/zeebe-io/zeebe-modeler/releases) is no longer maintained

1. Install [Camunda Modeler](https://camunda.com/download/modeler/)

    ```bash
    # Using brew macosx
    brew install --cask camunda-modeler

    # Download and install from the releases github page
    wget https://downloads.camunda.cloud/release/camunda-modeler/4.8.1/camunda-modeler-4.8.1-mac.dmg
    ```

2. Open a BPMN example `emergency-process.bpmn`

    ```bash
    # Clone following repository
    git clone https://github.com/salaboy/zeebe-k8s-getting-started.git

    # Open the file "emergency-process.bpmn" from Camunda Modeler
    # Zeebe curren version has changed the format of the files. i.e 'emergencyType == "Fire"' > '= emergencyType = "Fire"'
    ```

    ![Zeebe Process](images/simple-process.png)

3. Check the example `emergency-process.bpmn` and inspect the properties and types for the tasks

    * General: The `Id` (emergency-process) and the `Name` (Emergecy Process) of the process. General properties appear when no other item is selected
    * Tasks: `Classify Emergency`, `Coordinate With Hospital`, `Coordintate With Firefighters`
    * Types: `classify`, `hospital`, `firefighters`
    * Conditionals: Links between conditional task specify the expressions (`Condition Expression`) to match to decide the Taks to execute next.

    > In this example there are three **Task** or **Types**. Depending on the output `emergencyType` from `Classify Emergency`, the workflow will execute `Coordinate With Hospital` or `Coordintate With Firefighters`. Each Task Type will be executed by a Worker that implements that `Type`. A worker or instance can implement multiple tasks.

### Zeebe CLI

Zeebe CLI is the Command Line Interface. By default is configured to point to localhost:26500 to interact with a cluster, and because we are running port-forward from our environment to the cluster, our 26500 port is redirected to the cluster service.

1. Download CLI from [Zeebe releases page](https://github.com/zeebe-io/zeebe/releases)

    ```bash
    # Macos Version
    wget https://github.com/camunda-cloud/zeebe/releases/download/1.0.1/zbctl.darwin
    chmod +x zbctl.darwin

    # Copy binary file into global so it can bee used from any location
    sudo mv zbctl.darwin /usr/local/bin/zbctl
    ```

2. Connect inside the Cluster using `Zeebe Gateway`

    > Using docker-compose it is not necessary since `localhost:26500` is already exposed

    ```bash
    # This allows from dev to connect through the cluster using local java applications or Zeebe CLI
    kubectl port-forward -n zeebe svc/zeebe-dev-zeebe-gateway 26500:26500
    ```

3. Test and get the current status

    ```bash
    # Since it is local we will use --insecure
    zbctl status --insecure

    # Following must be the output displaying the status of the cluster
    Cluster size: 1
    Partitions count: 1
    Replication factor: 1
    Gateway version: 1.0.0
    Brokers:
    Broker 0 - 172.18.0.3:26501
        Version: 1.0.0
        Partition 1 : Leader, Healthy
    ```

## Simple Demo

![Helm Chart](./images/example.png)

1. Connect to the Zeebe Cluster.

    * Kubernetes::

    In order to interact with the services inside the cluster you need to use `port-forward` to route traffic from your environment to the cluster.

    ```bash
    kubectl port-forward -n zeebe svc/zeebe-dev-zeebe-gateway 26500:26500
    ```

    * docker-compose:

    `docker-compose -f docker/docker-compose.yaml up`

2. Once you have your connection to your cluster you can deploy our process definition by running:

    ```bash
    zbctl deploy workflows/emergency-process.bpmn --insecure

    # This is the output when the workflow has been deployed
    {
        "key": 2251799813685250,
        "workflows": [
            {
            "bpmnProcessId": "emergency-process",
            "version": 1,
            "workflowKey": 2251799813685249,
            "resourceName": "emergency-process.bpmn"
            }
        ]
    }

    # This will deploy another version. THe version is automatically incremented
    zbctl deploy workflows/emergency-process-v2.bpmn --insecure

    zbctl deploy workflows/emergency-process-v3.bpmn --insecure
    ```

3. Check current workflow within the Zeebe Operator at (http://localhost/#/) or (http://localhost:8080).

   > the `emergency-process` must appear in the dashboard with Zero instances yet

    ![Zeebe Operator](images/zeebe-operator.png)

4. Create Workflow Instances.

    > When creating instances it is possible to add variables

    ```bash
    # Create an instance where emergencyReason = "person"
    zbctl create instance emergency-process --variables "{\"emergencyReason\" : \"person\"}" --insecure

    # Create an instance where emergencyReason = "building on fire" 
    zbctl create instance emergency-process --variables "{\"emergencyReason\" : \"building on fire\"}" --insecure

    # This is the output when both instances have been created
    {
        "workflowKey": 2251799813685249,
        "bpmnProcessId": "emergency-process",
        "version": 1,
        "workflowInstanceKey": 2251799813685251
    }

    {
        "workflowKey": 2251799813685249,
        "bpmnProcessId": "emergency-process",
        "version": 1,
        "workflowInstanceKey": 2251799813685258
    }
    ```

5. Check current workflow within the Zeebe Operator at (http://localhost/#/).

    > At this point, you will see that they are both stuck at the `Classify Emergency` task. This is because you don't have workers for such tasks, so the process will wait in that state until we provide one of these workers.

    ![Process Waiting for Workers](images/manual-process-waiting.png)

6. Starting a simple Spring Boot Zeebe Worker

    ```bash
    cd  src/zeebe-worker-spring-boot/
    mvn clean package
    mvn spring-boot:run
    ```

7. Starting a simple Spring Boot Zeebe Worker

    > The worker is configured by default to connect to `localhost:26500` to fetch Jobs. If everything is up and running the worker will start and connect, automatically completing the pending tasks in our Workflow Instances.

8. Check current instances status

    > If you refresh Operate (http://localhost:80) you will find both Instances completed. Check Finished Instances to show the finished instances. In the graph, the terminal nodes show the number and final statuses (succesfull).

    ![Process Finished](images/manual-process-finished.png)

## Synchronous Demo (via REST)

1. Deploy a local Zeebe environment for development using docker-compose

    ```bash
    # run docker compose
    docker-compose -f docker/docker-compose.yml up
    ```

2. Deploy `emergency-process-v2`into local >eebe cluster

    ```bash
    # Deploy bpmn version 2
    zbctl deploy workflows/emergency-process-v2.bpmn --insecure  
    ```

3. Open and run `zeebe-rest-spring-boot`

    ```bash
    cd  src/zeebe-rest-spring-boot/
    mvn clean package
    mvn spring-boot:run
    ```

4. Test Following endpoints

    ```bash
    # Returns an object of type 'Injured'
    curl http://localhost:8081/classify/person

    # Returns an object of type 'Fire'
    curl http://localhost:8081/classify/fire

    # Returns a Server Internal Error due a timeout
    curl http://localhost:8081/classify/retries

    # Returns a HTTP 404 Not Found Error due a the reason does not exists
    curl http://localhost:8081/classify/reason
    ```

5. Verify current calls using Zeebe Operate at (http://localhost:8080).

    ![All Process run](images/rest-process-all.png)

## Asynchronous Demo (events)

1. Deploy a local Zeebe environment for development using docker-compose

    ```bash
    # run docker compose
    docker-compose -f docker/docker-compose.yml up
    ```

2. Deploy `emergency-process-v2` into local Zeebe cluster

    ```bash
    # Deploy bpmn version 2
    zbctl deploy workflows/emergency-process-v3.bpmn --insecure  
    ```

3. Open and run `zeebe-rest-spring-boot`

    ```bash
    cd  src/zeebe-rest-spring-boot/
    mvn clean package
    mvn spring-boot:run
    ```

4. Test Following endpoints

    > Since both request are synchronous both are going to return Internal Server Error because a Timeout

    ```bash
    # Returns an object of type 'Injured'
    curl http://localhost:8081/classify/person

    # Returns an object of type 'Fire'
    curl http://localhost:8081/classify/fire
    ```

    ![Process Waiting for Message](images/rest-process-waiting.png)

5. Publish Message Event to Zeebe so it continues with the workflow

    > Check dashboard to check messages moves from the event node to the final.

    ```bash
    # Returns an object of type 'Injured'
    zbctl publish message "emergency-received" --correlationKey="person" --insecure 

    # Returns an object of type 'Fire'
    zbctl publish message "emergency-received" --correlationKey="fire" --insecure 
    ```

6. Verify current calls using Zeebe Operate.

    ![Process Waiting for Message](images/rest-process-finished.png)

## SAGA Pattern Demo

1. Build and run `zeebe-saga-spring-boot`

    ```bash
    # Build and create Far Jar files for the microservices
    cd src/zeebe-saga-spring-boot
    mvn clean install 

    # Build the images using maven spring boot plugin
    mvn spring-boot:build-image
    ```

2. Verify the images created by the previous build

     ```bash
     # Use docker to list the images created.
     docker images

    REPOSITORY                                          TAG                                                     IMAGE ID       CREATED         SIZE
    ...
    jsa4000/flight-microservice                         1.0.0-SNAPSHOT                                          d058b75bd583   41 years ago    293MB
    jsa4000/hotel-microservice                          1.0.0-SNAPSHOT                                          d74b39bc37dd   41 years ago    293MB
    jsa4000/booking-microservice                        1.0.0-SNAPSHOT                                          99f196bf2652   41 years ago    293MB
    jsa4000/car-microservice                            1.0.0-SNAPSHOT                                          ef2495212f66   41 years ago    293MB
    paketobuildpacks/builder                            base                                                    1f124c766673   41 years ago    661MB
     ```

3. Deploy a local environment for development using docker-compose

    > This method use the docker compose v2 with docker compose integrated into Docker CLI. It shares the same docker network (`zeebe_network`)

    ```bash
    # Clean and prune all the cache and volumes (or execute a bash file '~/docker-clean')
    docker system prune -f
    docker volume prune -f
    docker image prune -f
    docker network prune -f

    # Open a terminal with three horizontal tabs to see the outputs from docker compose runs

    # Deploy Zeebe Cluster
    docker compose --project-name zeebe -f docker/docker-compose.yaml up

    # Deploy MongoDB Cluster
    MONGO_REPLICASET_HOST=mongo docker compose --project-name zeebe  -f src/zeebe-saga-spring-boot/docker/docker-compose-mongo.yaml up

    # Deploy Microservices
    docker compose --project-name zeebe  -f src/zeebe-saga-spring-boot/docker/docker-compose-micros.yaml up
    ```

4. Test Microservices HealthCheck to verify every thing it is working

    ```bash
    # Booking Service
    curl http://localhost:8081/actuator/health | jq .
    ```

5. Deploy `saga-example.bpmn` into local Zeebe cluster

    > It can be deployed directly by using **Camunda Modeler**

    ```bash
    # Deploy bpmn version 2
    zbctl deploy workflows/saga-example.bpmn --insecure  
    ```

    ![Zeebe](./images/saga-example.png)
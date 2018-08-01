# SpringCloud 教程

## 父模块

　　父模块通常用来聚合整个工程，可以用来管理工程中各个模块的依赖，避免大范围依赖冲突。同时该父模块可以继承 SpringBoot ，借此减少 Spring 相关的版本冲突。

　　参考如下的父模块 pom.xml 文件重要片段：

```xml

    <project>
    
        <!-- 继承 SpringBoot，最大限度避免 Spring 依赖冲突 -->
        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>2.0.3.RELEASE</version>
            <relativePath/> <!-- lookup parent from repository -->
        </parent>
        
        <!-- 父模块为 pom 打包模式，起聚合作用 -->
        <packaging>pom</packaging>
    
        <!-- 用于统领工程中各个模块的依赖版本管理 -->
        <properties>
            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
            <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
            <maven.compiler.source>1.8</maven.compiler.source>
            <maven.compiler.target>1.8</maven.compiler.target>
            <java.version>1.8</java.version>
            <spring-cloud.version>Finchley.RELEASE</spring-cloud.version>
            <mysql.version>8.0.11</mysql.version>
            <argLine>-Dfile.encoding=UTF-8</argLine>
            <gson.version>2.8.5</gson.version>
            <springboot.version>2.0.3.RELEASE</springboot.version>
            <springcloud.eureka.version>2.0.0.RELEASE</springcloud.eureka.version>
            <springcloud.config.version>2.0.0.RELEASE</springcloud.config.version>
        </properties>
    
        <dependencies>
        </dependencies>
    
        <dependencyManagement>
            <dependencies>
    
                <!-- Spring Cloud -->
                <dependency>
                    <groupId>org.springframework.cloud</groupId>
                    <artifactId>spring-cloud-dependencies</artifactId>
                    <version>${spring-cloud.version}</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
    
                <dependency>
                    <groupId>me.junbin.commons</groupId>
                    <artifactId>commons-utility</artifactId>
                    <version>1.2.4</version>
                </dependency>
    
                <dependency>
                    <groupId>org.apache.httpcomponents</groupId>
                    <artifactId>httpcomponents-client</artifactId>
                    <version>4.5.5</version>
                </dependency>
    
            </dependencies>
        </dependencyManagement>
        
    </project>

```

## API 模块

　　API 模块通常规划了整个工程中最基础的部分，比如实体、工具类、通用配置的。 API 模块通常是除了父模块以外依赖最少的模块。

## 原始服务提供方（原始模块）

　　对外提供服务的模块称为服务提供方，该模块通常会依赖 API 模块，最原始的服务提供方可以没有 SpringCloud 相关依赖，通常这个阶段是功能开发阶段。

## 原始服务消费方（原始模块）

　　最原始的服务消费方可以通过 RESTful 风格与单一服务提供方做交互。这时候主要是用于验证服务提供方所提供的服务正确性与可用性。

## 单个 Eureka 服务发现模块（commit id --> 6b3379c）

### Eureka 服务端

　　Eureka 服务端用于作为服务发现的注册点，提供服务注册功能：其他服务可以向 Eureka 注册自身信息成为 Eureka 客户端。提供服务发现功能：其他服务可以向 Eureka 查询已注册的客户端信息。

　　Eureka 服务端开发流程：

　　一、添加 Eureka 服务端依赖：

```xml

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
            <version>${springcloud.eureka.version}</version>
        </dependency>
    </dependencies>

```

　　二、配置 Eureka 服务端信息：

```yaml
    
    # 指定 Eureka 服务端访问端口
    server:
      port: 7001
    
    eureka:
      instance:
        # 配置 Eureka 实例主机名
        hostname: localhost
      client:
        # 是否需要向 eureka 服务端注册自身信息（以成为 eureka 客户端）。这里设置为 false，因为这里是 eureka 的服务端
        register-with-eureka: false
        # 是否需要向 eureka 服务端获取抓取 eureka 客户端信息。这里设置为 false
        fetch-registry: false
        # eureka 服务端交互地址（eureka 客户端服务注册方调用这个地址进行注册，同时其他服务可以通过这个地址查询服务提供方信息），默认为 http://localhost:8761/eureka/
        # key 为 defaultZone，value 为 交互地址
        service-url:
          defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/

```

　　三、开启 Eureka 服务端配置，直接在 SpringBoot 上添加 @org.springframework.cloud.netflix.eureka.server.EnableEurekaServer 注解即可：

```java

    // 启动 Eureka Server，接受其他微服务注册进来，启动后访问 http://${host}:${server.port} 如果看到 Eureka Server 页面则表示配置启动成功
    @EnableEurekaServer
    @SpringBootApplication
    public class Eureka7001Application {
    
        public static void main(String[] args) throws Exception {
            SpringApplication.run(Eureka7001Application.class, args);
        }
    
    }

```

　　四、启动 Eureka 服务端

　　直接运行 main 方法，然后访问 http://localhost:${server.port}，看到[Eureka服务端页面][EurekaServerPage]即表示 Eureka 配置正确。

### Eureka 客户端（服务注册）

　　当我们有了 Eureka 服务端之后，就可以将之前编写的服务提供方作为服务注册到 Eureka 服务端。

　　一、添加 Eureka 客户端依赖：

```xml

        <!-- eureka 客户端依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
            <version>${springcloud.config.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>${springcloud.eureka.version}</version>
        </dependency>
        <!-- eureka 客户端依赖 -->

```

　　二、配置 Eureka 客户端信息：

```yaml

    eureka:
      client:
        # 是否需要向 eureka 服务端注册自身信息（以成为 eureka 客户端）
        register-with-eureka: true
        # 是否需要向 eureka 服务端获取抓取 eureka 客户端信息
        fetch-registry: true
        service-url:
          # eureka 服务端交互地址
          defaultZone: http://localhost:7001/eureka/

```

　　三、开启 Eureka 客户端配置，直接在 SpringBoot 上添加 @org.springframework.cloud.netflix.eureka.EnableEurekaClient 注解即可：

```java

    // 将服务作为 Eureka 客户端，借此向 Eureka 服务端注册自身
    @EnableEurekaClient
    @SpringBootApplication
    public class Provider8001Application {
    
        public static void main(String[] args) throws Exception {
            SpringApplication.run(Provider8001Application.class, args);
        }
    
    }

```

　　四、启动 Eureka 客户端

　　在启动了 Eureka 服务端的前提下，直接运行 main 方法启动 Eureka 客户端，这时候刷新 Eureka 服务端页面，就可以看到 Eureka 客户端注册到服务端的[服务注册表][EurekaRegisteredInfo]

　　五、完善 Eureka 服务信息

　　eureka.client.instance.instance-id 配置项可以修改服务实例名称（默认服务实例名称为：${hostname}:${spring.application.name}:${server.port}）
　　eureka.client.instance.prefer-ip-address 配置项默认为 false ，修改为 true 之后可以访问 eureka 客户端时就不再使用 hostname 了，而是使用 ip 访问
　　除了上面两个比较常用的配置外，还可以使用 info

　　默认 Eureka 服务信息访问地址为 /info 或者 /actuator/info，我们可以直接在 Eureka Server 的服务状态列直接点击访问，通常这时候都是直接出现错误页面，因为我们没有添加相应的访问配置。

　　第一步：添加 spring-boot-actuator 依赖

```xml

        <!-- eureka 服务信息配置依赖 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!-- eureka 服务信息配置依赖 -->

```

　　第二步：添加 info 配置

```yaml

    # 以 info 开头的配置都会默认作为 /actuator/info 接口的 json 数据
    info:
      app.name: microservice
      company.name: joyley
      ## 通过 @variableName@ 可以读取 pom.xml 配置项
      build.version: @project.version@
      build.artifactId: @project.artifactId@
    # 上面的配置将会被转成如下 JSON
    {
        "app": {
            "name": "microservice"
        },
        "company": {
            "name": "joyley"
        },
        "build": {
            "version": 1,
            "artifactId": "provider8001"
        }
    }

```



　　附注：如果先启动 Eureka 客户端，后启动　Eureka 服务端，那么可以看到如下的日志：

```text
    
    # Eureka 客户端日志

    ## 由于尚未开启 Eureka Server，因此这里报错是 ConnectException: Connection refused: connect（无法向 Eureka Server 发起请求）
    2018-07-20 23:27:40.535 INFO  com.netflix.discovery.DiscoveryClient#getAndStoreFullRegistry:1047 Getting all instance registry info from the eureka server
    2018-07-20 23:27:42.727 ERROR com.netflix.discovery.shared.transport.decorator.RedirectingEurekaHttpClient#execute:83 Request execution error
    com.sun.jersey.api.client.ClientHandlerException: java.net.ConnectException: Connection refused: connect
    com.netflix.discovery.shared.transport.decorator.RetryableEurekaHttpClient#execute:130 Request execution failed with message: java.net.ConnectException: Connection refused: connect
    2018-07-20 23:27:42.732 ERROR com.netflix.discovery.DiscoveryClient#fetchRegistry:972 DiscoveryClient_MICROSERVICE-PROVIDER8081/localhost:MicroService-Provider8081:8081 - was unable to refresh its cache! status = Cannot execute request on any known server
    com.netflix.discovery.shared.transport.TransportException: Cannot execute request on any known server

    ## 这里启动了一个服务发现定时器，每隔 30 秒执行一次心跳检测，同时向 Eureka Server 注册的信息有：
    ### 当前服务的名称：MicroService-Provider8081
    ### 当前服务的状态：[timestamp=1532100462780, current=UP, previous=STARTING]
    ### 当前服务的访问地址：DiscoveryClient_MICROSERVICE-PROVIDER8081/localhost:MicroService-Provider8081:8081: registering service...
    2018-07-20 23:27:42.735 WARN  com.netflix.discovery.DiscoveryClient$1#get:290 Using default backup registry implementation which does not do anything.
    2018-07-20 23:27:42.742 INFO  com.netflix.discovery.DiscoveryClient#initScheduledTasks:1264 Starting heartbeat executor: renew interval is: 30
    2018-07-20 23:27:42.753 INFO  com.netflix.discovery.InstanceInfoReplicator#<init>:60 InstanceInfoReplicator onDemand update allowed rate per min is 4
    2018-07-20 23:27:42.767 INFO  com.netflix.discovery.DiscoveryClient#<init>:449 Discovery Client initialized at timestamp 1532100462762 with initial instances count: 0
    2018-07-20 23:27:42.778 INFO  org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry#register:42 Registering application MicroService-Provider8081 with eureka with status UP
    2018-07-20 23:27:42.780 INFO  com.netflix.discovery.DiscoveryClient$3#notify:1299 Saw local status change event StatusChangeEvent [timestamp=1532100462780, current=UP, previous=STARTING]
    2018-07-20 23:27:42.806 INFO  com.netflix.discovery.DiscoveryClient#register:826 DiscoveryClient_MICROSERVICE-PROVIDER8081/localhost:MicroService-Provider8081:8081: registering service...
    2018-07-20 23:27:42.853 INFO  org.apache.juli.logging.DirectJDKLog#log:180 Starting ProtocolHandler ["http-nio-8081"]
    2018-07-20 23:27:42.884 INFO  org.apache.juli.logging.DirectJDKLog#log:180 Using a shared selector for servlet write/read
    2018-07-20 23:27:42.936 INFO  org.springframework.boot.web.embedded.tomcat.TomcatWebServer#start:206 Tomcat started on port(s): 8081 (http) with context path ''
    2018-07-20 23:27:42.962 INFO  org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration#onApplicationEvent:124 Updating port to 8081
    2018-07-20 23:27:42.966 INFO  org.springframework.boot.StartupInfoLogger#logStarted:59 Started Provider8001Application in 22.635 seconds (JVM running for 23.922)

    ## 继续尝试向 Eureka 注册服务
    2018-07-20 23:27:44.845 ERROR com.netflix.discovery.shared.transport.decorator.RedirectingEurekaHttpClient#execute:83 Request execution error
    com.sun.jersey.api.client.ClientHandlerException: java.net.ConnectException: Connection refused: connect

    2018-07-20 23:28:44.810 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:958 Disable delta property : false
    2018-07-20 23:28:44.811 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:959 Single vip registry refresh property : null
    2018-07-20 23:28:44.812 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:960 Force full registry fetch : false
    2018-07-20 23:28:44.813 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:961 Application is null : false
    2018-07-20 23:28:44.813 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:962 Registered Applications size is zero : true
    2018-07-20 23:28:44.814 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:964 Application version is -1: true
    2018-07-20 23:28:44.815 INFO  com.netflix.discovery.DiscoveryClient#getAndStoreFullRegistry:1047 Getting all instance registry info from the eureka server
    2018-07-20 23:28:45.062 INFO  com.netflix.discovery.DiscoveryClient#renew:850 DiscoveryClient_MICROSERVICE-PROVIDER8081/localhost:MicroService-Provider8081:8081 - Re-registering apps/MICROSERVICE-PROVIDER8081
    2018-07-20 23:28:45.063 INFO  com.netflix.discovery.DiscoveryClient#register:826 DiscoveryClient_MICROSERVICE-PROVIDER8081/localhost:MicroService-Provider8081:8081: registering service...
    
    ## 注册服务成功。请求 Eureka Server 成功返回 200，Eureka 注册成功响应体中的 statusCode 返回 204 
    2018-07-20 23:28:45.095 INFO  com.netflix.discovery.DiscoveryClient#getAndStoreFullRegistry:1056 The response status is 200
    2018-07-20 23:28:45.241 INFO  com.netflix.discovery.DiscoveryClient#register:835 DiscoveryClient_MICROSERVICE-PROVIDER8081/localhost:MicroService-Provider8081:8081 - registration status: 204
    2018-07-20 23:29:15.100 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:958 Disable delta property : false
    2018-07-20 23:29:15.100 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:959 Single vip registry refresh property : null
    2018-07-20 23:29:15.101 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:960 Force full registry fetch : false
    2018-07-20 23:29:15.102 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:961 Application is null : false
    2018-07-20 23:29:15.103 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:962 Registered Applications size is zero : true
    2018-07-20 23:29:15.104 INFO  com.netflix.discovery.DiscoveryClient#fetchRegistry:964 Application version is -1: false
    2018-07-20 23:29:15.104 INFO  com.netflix.discovery.DiscoveryClient#getAndStoreFullRegistry:1047 Getting all instance registry info from the eureka server
    2018-07-20 23:29:15.230 INFO  com.netflix.discovery.DiscoveryClient#getAndStoreFullRegistry:1056 The response status is 200
    2018-07-20 23:32:40.538 INFO  com.netflix.discovery.shared.resolver.aws.ConfigClusterResolver#getClusterEndpoints:43 Resolving eureka endpoints via configuration


    # Eureka 服务端日志
    ## 将 MicroService-Provider8081 服务信息添加到服务管理中，由于未发现该服务，因此执行 renew 操作
    ## 开启定时器，每隔 1 分钟检测已注册服务的状态并执行相关的补偿操作
    2018-07-20 23:28:45.031 WARN  com.netflix.eureka.registry.AbstractInstanceRegistry#renew:356 DS: Registry: lease doesn't exist, registering resource: MICROSERVICE-PROVIDER8081 - localhost:MicroService-Provider8081:8081
    2018-07-20 23:28:45.032 WARN  com.netflix.eureka.resources.InstanceResource#renewLease:116 Not Found (Renew): MICROSERVICE-PROVIDER8081 - localhost:MicroService-Provider8081:8081
    2018-07-20 23:28:45.238 INFO  com.netflix.eureka.registry.AbstractInstanceRegistry#register:269 Registered instance MICROSERVICE-PROVIDER8081/localhost:MicroService-Provider8081:8081 with status UP (replication=false)
    2018-07-20 23:29:22.924 INFO  com.netflix.eureka.registry.AbstractInstanceRegistry$EvictionTask#run:1247 Running the evict task with compensationTime 0ms
    2018-07-20 23:30:22.924 INFO  com.netflix.eureka.registry.AbstractInstanceRegistry$EvictionTask#run:1247 Running the evict task with compensationTime 0ms
    2018-07-20 23:31:22.925 INFO  com.netflix.eureka.registry.AbstractInstanceRegistry$EvictionTask#run:1247 Running the evict task with compensationTime 0ms
    2018-07-20 23:32:22.925 INFO  com.netflix.eureka.registry.AbstractInstanceRegistry$EvictionTask#run:1247 Running the evict task with compensationTime 0ms
    2018-07-20 23:33:22.927 INFO  com.netflix.eureka.registry.AbstractInstanceRegistry$EvictionTask#run:1247 Running the evict task with compensationTime 1ms
    2018-07-20 23:34:22.932 INFO  com.netflix.eureka.registry.AbstractInstanceRegistry$EvictionTask#run:1247 Running the evict task with compensationTime 4ms

```

### 常见问题：

　　_EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE._

　　该问题主要是因为 Eureka 客户端 down 掉或者网络不可达等原因造成的。在开启自我保护模式下，Eureka 服务端会将出现问题的客户端保护起来（不从服务注册表中删除该服务）并提示该警告。

　　解决方案是关闭 Eureka Server 的自我保护模式（eureka.server.enable-self-preservation=false），同时可以调整清除不可用服务间隔（eureka.server.eviction-interval-timer-in-ms=10000）以迅速剔除不可用的服务。（线上不建议关闭自我保护模式）

　　如果关闭了 Eureka Server 的自我保护模式，那么 Eureka 会在页面上给出相应的警示语：_THE SELF PRESERVATION MODE IS TURNED OFF.THIS MAY NOT PROTECT INSTANCE EXPIRY IN CASE OF NETWORK/OTHER PROBLEMS._

### 再说 Eureka 自我保护模式

默认情况下，如果EurekaServer在一定时间内没有接收到某个微服务实例的心跳，EurekaServer将会注销该实例（默认90秒）。
但是当网络分区故障发生时，微服务与EurekaServer之间无法正常通信，以上行为可能变得非常危险了一一因为微服务本身其实
是健康的，此时本不应该注销这个微服务。Eureka通过自我保护模式来解决这个问题一一当EurekaServer节点在短时间内丟
失过多客户端时（可能发生了网络分区故障），那么这个节点就会进入自我保护模式。一旦进入该模式，EurekaServer就会保护服
务注册表中的信息，不再删除服务注册表中的数据（也就是不会注销任何微服务）。当网络故障恢复后，该EurekaServer节点会
自动退出自我保护模式。
在自我保护模式中，Eureka Server会保护服务注册表中的信息，不再注销任何服务实例。当它收到的心跳数重新恢复到阈值以上
时，该Eureka Server节点就会自动退出自我保护模式。它的设计哲学就是宁可保留错误的服务注册信息，也不盲目注销任何可能
健康的服务实例。一句话讲解：好死不如赖活着
综上，自我保护模式是一种应对网络异常的安全保护措施。它的架构哲学是宁可同时保留所有微服务（健康的微服务和不健康的微
服务都会保留），也不盲目注销任何健康的微服务。使用自我保护模式，可以让Eureka集群更加的健壮、稳定。
在Spring Cloud中，可以使用eureka.server.enable-self-preservation = false 禁用自我保护模式。

### Eureka 服务发现

　　一：在 SpringBootApplication 上添加 @org.springframework.cloud.client.discovery.EnableDiscoveryClient 注解开启服务发现功能

　　二：在需要使用服务发现的类中注入 org.springframework.cloud.client.discovery.DiscoveryClient ，该服务发现客户端有以下两个常用方法：

```java

    public interface DiscoveryClient {
    
        /**
         * A human readable description of the implementation, used in HealthIndicator
         * @return the description
         */
        String description();
    
        /**
         * 获取指定服务ID的所有服务实例对象
         * Get all ServiceInstances associated with a particular serviceId
         * @param serviceId the serviceId to query
         * @return a List of ServiceInstance
         */
        List<ServiceInstance> getInstances(String serviceId);
    
        /**
         * 获取所有当前已知的服务ID
         * @return all known service ids
         */
        List<String> getServices();
    
    }
```

　　三：获取对应服务实例后，可以通过该实例获取服务的访问地址

```java

    public interface ServiceInstance {
    
        /**
         * 获取服务 ID
         * @return the service id as registered.
         */
        String getServiceId();
    
        /**
         * 获取服务 hostname
         * @return the hostname of the registered ServiceInstance
         */
        String getHost();
    
        /**
         * 获取服务端口
         * @return the port of the registered ServiceInstance
         */
        int getPort();
    
        /**
         * 服务是否需要通过 https 访问
         * @return if the port of the registered ServiceInstance is https or not
         */
        boolean isSecure();
    
        /**
         * 服务的 URI 地址
         * @return the service uri address
         */
        URI getUri();
    
        /**
         * 服务的元数据
         * @return the key value pair metadata associated with the service instance
         */
        Map<String, String> getMetadata();
    
        /**
         * 服务的协议
         * @return the scheme of the instance
         */
        default String getScheme() {
            return null;
        }
    }

```

## Ribbon 客户端负载均衡

　　一、确保有多个 provider 服务（相同服务名称不同实例ID）注册到 eureka 中，然后修改 consumer80 的配置文件，添加 eureka 集群交互地址与 provider 服务交互地址

```yaml

    # 将与 provider 服务的交互地址改成 provider 服务的微服务名称而不再是具体的 IP 地址
    microservice:
      provider:
        # baseHost: http://jd.me:8001
        # 修改地址为向 eureka 注册的服务地址
        baseHost: http://MicroService-Provider8001
    
    ## 添加 eureka 服务发现机制
    eureka:
      client:
        # 不向 eureka 注册服务（因为 consumer 是客户端，是任意的客户，不属于我们的服务，所以不需要注册自身到 eureka 集群中）
        register-with-eureka: false
        # 获取已注册服务（向 eureka 集群获取已注册服务信息，这其中就包括了 provider 服务注册的服务信息）
        fetch-registry: true
        # erueka 集群交互地址
        service-url:
          defaultZone:
            http://eureka7001:7001/eureka/,http://eureka7002:7002/eureka/,http://eureka7003:7003/eureka/


```

　　二、项目启动类添加 @org.springframework.cloud.netflix.eureka.EnableEurekaClient 以使用 eureka 的服务发现功能，同时添加
　　@org.springframework.cloud.netflix.ribbon.RibbonClient(name = "MicroService-Provider8001") 注解开启 Ribbon Client 配置。此时启动类如下：

```java

    @EnableEurekaClient
    @SpringBootApplication
    @RibbonClient(name = "MicroService-Provider8001")
    public class ConsumerApplication {
    
        public static void main(String[] args) throws Exception {
            SpringApplication.run(ConsumerApplication.class, args);
        }
    
    }

```

　　三、修改与 MicroService-Provider8001 微服务进行交互的 RestTemplate Bean 配置，添加 org.springframework.cloud.client.loadbalancer.LoadBalanced 注解（该注解只能够标注在 RestTemplate 上）该 RestTemplate 使用负载均衡客户端进行交互操作。

　　这时候启动 consumer 模块，每次访问 provider 服务都将根据已经发现的 provider 微服务进行轮询交互。
　　这是因为默认采用了 com.netflix.loadbalancer.ZoneAvoidanceRule 负载规则，ZoneAvoidanceRule （父类：com.netflix.loadbalancer.ClientConfigEnabledRoundRobinRule）
　　内部持有一个 com.netflix.loadbalancer.ClientConfigEnabledRoundRobinRule.roundRobinRule 轮询负载规则实例，通过选择可用的 zone 之后再执行轮询负载。

　　如果没有自定义负载规则，那么默认会在首次执行负载时通过如下逻辑动态创建 com.netflix.loadbalancer.ZoneAvoidanceRule 负载规则

```text

    org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient.getLoadBalancer(String serviceId)
        org.springframework.cloud.netflix.ribbon.SpringClientFactory.getLoadBalancer(String name) 此时会刷新 Spring 容器通过
            org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration.ribbonLoadBalancer(IClientConfig config, ServerList<Server> serverList, ServerListFilter<Server> serverListFilter, IRule rule, IPing ping, ServerListUpdater serverListUpdater)
        动态创建 LoadBalancer，LoadBalancer 会通过
            org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration.ribbonRule(IClientConfig config)
        动态创建负载规则

    特别注意：不明确自定义负载规则时，通过 （org.springframework.context.ApplicationContext）org.springframework.beans.factory.ListableBeanFactory.getBeansOfType(ZoneAvoidanceRule.class) 是无法获取到该 Bean 的。

```

　　四、我们可以自定义负载规则，只需要往 Spring 容器中注册一个 IRule 类型的 Bean，那么 Ribbon 就会直接使用该负载规则而不是默认动态创建的 ZoneAvoidanceRule 负载规则。

```java

        // 修改默认的负载规则
        @Bean
        public IRule customRule() {
            return new RoundRobinTimesRule();
        }

```

　　此时第一次使用负载均衡时，将会通过 org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration.ribbonLoadBalancer(IClientConfig config, ServerList<Server> serverList, ServerListFilter<Server> serverListFilter, IRule rule, IPing ping, ServerListUpdater serverListUpdater) 动态创建 LoadBalancer 时将会直接使用我们自定义的 IRule。

## Feign 声明式 HTTP 客户端

　　Feign 是一款声明式的 HTTP 客户端，借助 Feign 可以快速完成 HTTP 交互（比 HttpClient 或者 RestTemplate 更加方便），默认 Feign 仅支持纯文本的 HTTP 交互，所以像图片等一些二进制传输则无法执行（二进制交互可以考虑结合 feign-form 和 feign-form-spring 依赖实现单文件上传，遇到多文件也不行）

### API 模块改动

　　一、添加 feign 依赖

```xml

        <!-- feign 声明式 HTTP 客户端依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
            <version>${springcloud.openfeign.version}</version>
        </dependency>
        <!-- feign 声明式 HTTP 客户端依赖 -->

```

　　二、编写 Feign 的 HTTP 交互类 me.junbin.microservice.service.FeignUserService

```java

    @RequestMapping("/user")
    @FeignClient(name = "${microservice.provider.name}")
    public interface FeignUserService {
    
        @PostMapping
        void append(User user);
    
        @DeleteMapping("/{id:\\d+}")
        void delete(@PathVariable("id") Long id);
    
        @GetMapping("/{id:\\d+}")
        User findById(@PathVariable("id") Long id);
    
        @GetMapping("/list")
        List<User> findAll();
    
    }

```

　　这里的 @FeignClient 注解，主要通过 ${microservice.provider.name} 配置服务名称，这里没有使用字面值而是采用了占位符方式，这样服务名称可以直接由使用方配置管理。

　　需要注意的是：旧版本的 SpringCloud 的 feign 依赖 artifactId 为 spring-cloud-starter-feign，2.0.0.RELEASE 开始切换成 spring-cloud-starter-openfeign。至少在 2.0.0.RELEASE 开始支持 GetMapping，PostMapping 等 RequestMapping 衍生注解（旧版本中则不支持）；但是不支持使用形如 @GetMapping("/{id:\\d+}") 等形式的 SpringMVC 正则校验。 

### 新模块 consumer-feign

　　一、pom 依赖，复制 consumer80 模块的依赖并添加 feign 依赖

```xml

        <!-- feign 声明式 HTTP 客户端依赖 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
            <version>${springcloud.openfeign.version}</version>
        </dependency>
        <!-- feign 声明式 HTTP 客户端依赖 -->

```

　　二、application.yml 配置

```yaml

    server:
      port: 80
    
    microservice:
      provider:
        name: MicroService-Provider8001
    
    # 使用 eureka 的服务发现功能
    eureka:
      client:
        register-with-eureka: false
        fetch-registry: true
        service-url:
          defaultZone: http://eureka7001:7001/eureka/,http://eureka7002:7002/eureka/,http://eureka7003:7003/eureka/
    
```

　　三、SpringBoot 启动类配置

```java

    @EnableEurekaClient
    //@SpringBootApplication(scanBasePackageClasses = {ConsumerFeignApplication.class, FeignUserService.class})
    @SpringBootApplication
    // 通过 basePackageClasses 或者 basePackage 或者 value 指定扫描被
    // org.springframework.cloud.openfeign.FeignClient 标注的类
    @EnableFeignClients(basePackageClasses = FeignUserService.class)
    // 除了通过 EnableFeignClients 指定扫描路径外，还可以直接通过 ComponentScan 注解 或者
    // org.springframework.boot.autoconfigure.SpringBootApplication.scanBasePackages 等方法添加扫描路径
    //@ComponentScan(basePackageClasses = FeignUserService.class)
    public class ConsumerFeignApplication {
    
        public static void main(String[] args) throws Exception {
            SpringApplication.run(ConsumerFeignApplication.class, args);
        }
    
    }

```

　　首先通过 @EnableEurekaClient 启动 Eureka 客户端功能，接着使用 @EnableFeignClients(basePackageClasses = FeignUserService.class) 指定 feign 的包扫描路径，实际上也可以直接使用 SpringBootApplication 注解的 scanBasePackageClasses 方法或者用 @ComponentScan(basePackageClasses = FeignUserService.class) 等方式进行扫描，主要能够保证被 @FeignClient 标注的类能够作为 Spring Bean 存在即可。

　　四、编写 FeignUserController 来使用 FeignUserService 功能

```java

    @RestController
    @RequestMapping("/consumer/user")
    public class FeignUserController {
    
        @Autowired
        private FeignUserService userService;
    
        @GetMapping("/{id:\\d+}")
        public User query(@PathVariable long id) {
            return userService.findById(id);
        }
    
        @DeleteMapping("/{id:\\d+}")
        public User delete(@PathVariable long id) {
            User user = userService.findById(id);
            userService.delete(id);
            return user;
        }
    
        @PostMapping
        public User append(@RequestBody User user) {
            userService.append(user);
            return userService.findAll().stream()
                    .filter(u -> u.getUsername().equals(user.getUsername()))
                    .findFirst().orElse(new User());
        }
    
        @GetMapping("/list")
        public List<User> list() {
            return userService.findAll();
        }
    
    }


```

　　可以看到，我们通过注入 FeignUserService 之后就可以与 eureka 中已注册的服务做交互了。同时，我们看到 FeignUserService 类上标注的 @FeignClient#name 为 ${microservice.provider.name}，这里会读取我们在 application.yml 配置文件中的配置项数据。借此保证交互服务的正确性。

### Feign 执行流程简单解析

　　由于 spring-cloud-starter-openfeign 默认集成 spring-cloud-starter-netflix-ribbon 模块，并且在 feign 模块中的 META-INF/spring.factories 指定了 org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration 自动配置类，该自动配置类会

```java

        // 检测是否存在 RetryTemplate 这个类来动态创建 CachingSpringLoadBalancerFactory 类 
    	@Bean
    	@Primary
    	@ConditionalOnMissingClass("org.springframework.retry.support.RetryTemplate")
    	public CachingSpringLoadBalancerFactory cachingLBClientFactory(
    			SpringClientFactory factory) {
    		return new CachingSpringLoadBalancerFactory(factory);
    	}
    
    	@Bean
    	@Primary
    	@ConditionalOnClass(name = "org.springframework.retry.support.RetryTemplate")
    	public CachingSpringLoadBalancerFactory retryabeCachingLBClientFactory(
    		SpringClientFactory factory,
    		LoadBalancedRetryFactory retryFactory) {
    		return new CachingSpringLoadBalancerFactory(factory, retryFactory);
    	}

```

　　FeignRibbonClientAutoConfiguration 会同时引入 DefaultFeignLoadBalancedConfiguration 配置类，该配置类生成一个 org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient 实例作为 Spring Bean。

```java

    @Configuration
    class DefaultFeignLoadBalancedConfiguration {
    	@Bean
    	@ConditionalOnMissingBean
    	public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory,
    							  SpringClientFactory clientFactory) {
    		return new LoadBalancerFeignClient(new Client.Default(null, null),
    				cachingFactory, clientFactory);
    	}
    }


```
　　
　　调用被 @FeignClient 注解标注的类方法时，将有如下执行过程：

```text
    
    org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient.execute(Request request, Request.Options options)
        org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient.lbClient(String clientName)
            org.springframework.cloud.openfeign.ribbon.CachingSpringLoadBalancerFactory.create(String clientName)
                org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration.ribbonLoadBalancer // 到这里已经是 Ribbon 的逻辑了 

```


## 超链管理区

[EurekaServerPage]: file:///C:/Users/Reka/Desktop/Markdown专辑/SpringCloud/Eureka/Eureka服务端管理页面.jpg "Eureka服务端"
[EurekaRegisteredInfo]: file:///C:/Users/Reka/Desktop/Markdown专辑/SpringCloud/Eureka/Eureka已注册服务信息.jpg "Eureka已注册服务信息"
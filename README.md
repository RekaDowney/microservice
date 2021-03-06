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

## 单个 Eureka 服务发现模块（eureka 分支 commit id --> e87ce73）

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

## EurekaServer 集群（eureka-cluster 分支 commit id --> 416615c）

　　一：编辑 hosts 文件，添加如下映射

```text

    # Eureka 集群测试
    127.0.0.1 eureka7001
    127.0.0.1 eureka7002
    127.0.0.1 eureka7003

```

　　二：修改 eureka-server7001 模块

```yaml

    eureka:
      instance:
        # EurekaServer 实例名称
        # 集群环境下，通过 hosts 文件模拟不同的 hostname
        hostname: eureka7001
      client:
        # 是否需要向 eureka 服务端注册自身信息（以成为 eureka 客户端）。这里设置为 false，因为这里是 eureka 的服务端
        register-with-eureka: false
        # 是否需要向 eureka 服务端获取抓取 eureka 客户端信息。这里设置为 false
        fetch-registry: false
        # eureka 服务端交互地址
        # 集群环境下，交互地址为其他 EurekaServer 交互地址，多个交互地址采用逗号分隔
        service-url:
          defaultZone: http://eureka7002:7002/eureka/,http://eureka7003:7003/eureka/

```

　　三：添加 eureka-server7002 和 eureka-server7003 模块

　　对于 eureka-server7002 模块有如下配置：

```yaml

    server:
      port: 7002
    
    eureka:
      instance:
        # EurekaServer 实例名称
        # 集群环境下，通过 hosts 文件模拟不同的 hostname
        hostname: eureka7002
      client:
        # 是否需要向 eureka 服务端注册自身信息（以成为 eureka 客户端）。这里设置为 false，因为这里是 eureka 的服务端
        register-with-eureka: false
        # 是否需要向 eureka 服务端获取抓取 eureka 客户端信息。这里设置为 false
        fetch-registry: false
        # eureka 服务端交互地址（eureka 客户端服务注册方调用这个地址进行注册，同时其他服务可以通过这个地址查询服务提供方信息），默认为 http://localhost:8761/eureka/
        # key 为 defaultZone，value 为 交互地址
        service-url:
          defaultZone: http://eureka7001:7001/eureka/,http://eureka7003:7003/eureka/

```

　　其启动类如下：

```java

    @EnableEurekaServer
    @SpringBootApplication
    public class Eureka7002Application {
    
        public static void main(String[] args) throws Exception {
            SpringApplication.run(Eureka7002Application.class, args);
        }
    
    }

```

　　对于 eureka-server7003 模块有如下配置：

```yaml

    server:
      port: 7003
    
    eureka:
      instance:
        # EurekaServer 实例名称
        # 集群环境下，通过 hosts 文件模拟不同的 hostname
        hostname: eureka7003
      client:
        # 是否需要向 eureka 服务端注册自身信息（以成为 eureka 客户端）。这里设置为 false，因为这里是 eureka 的服务端
        register-with-eureka: false
        # 是否需要向 eureka 服务端获取抓取 eureka 客户端信息。这里设置为 false
        fetch-registry: false
        # eureka 服务端交互地址（eureka 客户端服务注册方调用这个地址进行注册，同时其他服务可以通过这个地址查询服务提供方信息），默认为 http://localhost:8761/eureka/
        # key 为 defaultZone，value 为 交互地址
        service-url:
          defaultZone: http://eureka7001:7001/eureka/,http://eureka7002:7002/eureka/

```

　　其启动类如下：

```java

    @EnableEurekaServer
    @SpringBootApplication
    public class Eureka7003Application {
    
        public static void main(String[] args) throws Exception {
            SpringApplication.run(Eureka7003Application.class, args);
        }
    
    }

```

　　四：修改 provider8001 的 eureka 交互地址

```yaml

    eureka:
      client:
        # 是否需要向 eureka 服务端注册自身信息（以成为 eureka 客户端）
        register-with-eureka: true
        # 是否需要向 eureka 服务端获取抓取 eureka 客户端信息
        fetch-registry: true
        service-url:
          # eureka 服务端交互地址
          defaultZone: http://eureka7001:7001/eureka/,http://eureka7002:7002/eureka/,http://eureka7003:7003/eureka/

```

　　此时集群即搭建成功，通过启动 Eureka7001Application 、 Eureka7002Application 、 Eureka7003Application 先启动 Eureka 集群，然后启动 Provider8001Application 向 Eureka 集群注册服务。最后通过访问 eureka7001:7001 、 eureka7002:7002 、 eureka7003:7003 验证集群和服务注册效果。

## 超链管理区

[EurekaServerPage]: file:///C:/Users/Reka/Desktop/Markdown专辑/SpringCloud/Eureka/Eureka服务端管理页面.jpg "Eureka服务端"
[EurekaRegisteredInfo]: file:///C:/Users/Reka/Desktop/Markdown专辑/SpringCloud/Eureka/Eureka已注册服务信息.jpg "Eureka已注册服务信息"
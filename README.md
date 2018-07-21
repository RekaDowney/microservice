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

### Eureka 客户端

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



## 超链管理区

[EurekaServerPage]: file:///C:/Users/Reka/Desktop/Markdown专辑/SpringCloud/Eureka/Eureka服务端管理页面.jpg "Eureka服务端"
[EurekaRegisteredInfo]: file:///C:/Users/Reka/Desktop/Markdown专辑/SpringCloud/Eureka/Eureka已注册服务信息.jpg "Eureka已注册服务信息"
package me.junbin.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/20 22:50
 * @description :
 */
// 启动 Eureka Server，接受其他微服务注册进来，启动后访问 http://${host}:${server.port} 如果看到 Eureka Server 页面则表示配置启动成功
@EnableEurekaServer
@SpringBootApplication
public class Eureka7001Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Eureka7001Application.class, args);
    }

}

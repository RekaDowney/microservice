package me.junbin.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/20 9:34
 * @description :
 */
// 将服务作为 Eureka 客户端，借此向 Eureka 服务端注册自身
@EnableEurekaClient
@SpringBootApplication
public class Provider8001Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Provider8001Application.class, args);
    }

}

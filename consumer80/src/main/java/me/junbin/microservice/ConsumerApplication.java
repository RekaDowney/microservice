package me.junbin.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/20 11:43
 * @description :
 */
@EnableEurekaClient
@SpringBootApplication
@RibbonClient(name = "MicroService-Provider8001")
public class ConsumerApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ConsumerApplication.class, args);
    }

}

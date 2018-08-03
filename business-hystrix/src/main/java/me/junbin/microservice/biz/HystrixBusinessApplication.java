package me.junbin.microservice.biz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/8/1 14:48
 * @description :
 */
@EnableEurekaClient
@SpringBootApplication
@EnableHystrix // 等价于 @EnableCircuitBreaker，启动 Hystrix 服务熔断/降级 支持
public class HystrixBusinessApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(HystrixBusinessApplication.class, args);
    }

}

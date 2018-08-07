package me.junbin.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/8/6 11:49
 * @description :
 */
@EnableZuulProxy
@SpringBootApplication
public class ZuulGatewayApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ZuulGatewayApplication.class, args);
    }

}

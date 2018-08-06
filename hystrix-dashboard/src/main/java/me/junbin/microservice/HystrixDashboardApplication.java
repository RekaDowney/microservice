package me.junbin.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/8/6 9:22
 * @description :
 */
@SpringBootApplication
@EnableHystrixDashboard // 启动 Hystrix Dashboard
public class HystrixDashboardApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(HystrixDashboardApplication.class, args);
    }

}

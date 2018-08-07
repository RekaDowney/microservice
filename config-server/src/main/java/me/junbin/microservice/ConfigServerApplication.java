package me.junbin.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/8/7 9:58
 * @description :
 */
@EnableConfigServer // 启动 SpringCloud ConfigServer 自动配置
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}

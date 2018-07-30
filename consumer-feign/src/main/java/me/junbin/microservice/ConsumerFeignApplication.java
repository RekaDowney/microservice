package me.junbin.microservice;

import me.junbin.microservice.service.FeignUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018-07-30 09:55
 * @description :
 */
@EnableEurekaClient
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

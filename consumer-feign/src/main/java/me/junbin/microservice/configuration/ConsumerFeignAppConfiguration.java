package me.junbin.microservice.configuration;

import me.junbin.microservice.service.fallback.FeignUserServiceFallbackFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018-07-30 10:46
 * @description :
 */
@Configuration
public class ConsumerFeignAppConfiguration {

    @Bean
    public FeignUserServiceFallbackFactory feignUserServiceFallbackFactory() {
        return new FeignUserServiceFallbackFactory();
    }

}

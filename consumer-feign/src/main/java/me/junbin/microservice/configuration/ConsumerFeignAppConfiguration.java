package me.junbin.microservice.configuration;

import com.netflix.loadbalancer.IRule;
import me.junbin.microservice.ribbon.rule.RoundRobinTimesRule;
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

    // 由于 FeignUserServiceFallbackFactory 被 FeignUserService 的
    // org.springframework.cloud.openfeign.FeignClient.fallbackFactory() 注解标注
    // 所使用，因此必须将该类注册到 Spring 容器中
    @Bean
    public FeignUserServiceFallbackFactory feignUserServiceFallbackFactory() {
        return new FeignUserServiceFallbackFactory();
    }

    // 通过往容器中添加 Ribbon 负载规则，可以覆盖 Feign 默认的 ZoneAvoidanceRule 轮询负载规则
    @Bean
    public IRule customRule() {
        return new RoundRobinTimesRule();
    }

}

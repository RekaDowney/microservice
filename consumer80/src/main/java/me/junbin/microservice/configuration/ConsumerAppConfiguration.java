package me.junbin.microservice.configuration;

import com.netflix.loadbalancer.IRule;
import me.junbin.microservice.interceptor.LoggingRequestInterceptor;
import me.junbin.microservice.ribbon.rule.RoundRobinTimesRule;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/20 11:30
 * @description :
 */
@Configuration
public class ConsumerAppConfiguration {

    @Bean
    public ClientHttpRequestInterceptor loggingRequestInterceptor() {
        return new LoggingRequestInterceptor(true);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate(ClientHttpRequestInterceptor loggingRequestInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });
        restTemplate.getInterceptors().add(loggingRequestInterceptor);
        return restTemplate;
    }

    // 修改默认的负载规则
    @Bean
    public IRule customRule() {
        // 默认为 轮询
//        return new RoundRobinRule();
        // 随机负载
//        return new RandomRule();
        // 无法采用下面这种自定义负载规则，原因是 com.netflix.client.ClientFactory.instantiateInstanceWithClientConfig()
        // 会根据返回的 IRule 类型直接调用 newInstance 方法而不是直接使用该 Bean
        // 自定义负载规则，轮询，每个服务端点访问 3 次后轮询到下一个服务端点
        return new RoundRobinTimesRule();
    }

}

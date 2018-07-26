package me.junbin.microservice.configuration;

import me.junbin.microservice.interceptor.LoggingRequestInterceptor;
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

}

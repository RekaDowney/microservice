package me.junbin.microservice.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/20 11:35
 * @description :
 */
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingRequestInterceptor.class);

    private final boolean logHeaders;

    public LoggingRequestInterceptor(boolean logHeaders) {
        this.logHeaders = logHeaders;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String method = request.getMethodValue();
        String url = request.getURI().toString();
        if (logHeaders) {
            HttpHeaders headers = request.getHeaders();
            if (body.length != 0) {
                String bodyString = new String(body, StandardCharsets.UTF_8);
                LOGGER.info("执行{}请求，请求URL：{}，请求头：{}，请求体：{}", method, url, headers, bodyString);
            } else {
                LOGGER.info("执行{}请求，请求URL：{}，请求头：{}", method, url, headers);
            }
        } else {
            if (body.length != 0) {
                String bodyString = new String(body, StandardCharsets.UTF_8);
                LOGGER.info("执行{}请求，请求URL：{}，请求体：{}", method, url, bodyString);
            } else {
                LOGGER.info("执行{}请求，请求URL：{}", method, url);
            }
        }
        return execution.execute(request, body);
    }

}

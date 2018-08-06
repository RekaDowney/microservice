package me.junbin.microservice.biz.configuration;

import com.alibaba.druid.support.http.ResourceServlet;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import me.junbin.commons.gson.Gsonor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/20 10:28
 * @description :
 */
@Configuration
public class HystrixMvcConfiguration extends WebMvcConfigurationSupport {

    @Bean
    public ServletRegistrationBean<StatViewServlet> statViewServlet() {
        ServletRegistrationBean<StatViewServlet> bean =
                new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put(ResourceServlet.PARAM_NAME_USERNAME, "reka");
        initParameters.put(ResourceServlet.PARAM_NAME_PASSWORD, "123456");
        // 不配置 ALLOW 或者空串则默认允许所有 IP 访问，相当于 IP 白名单
        // initParameters.put(ResourceServlet.PARAM_NAME_ALLOW, "");
        // 相当于 IP 黑名单
        // initParameters.put(ResourceServlet.PARAM_NAME_DENY, "192.168.0.1");
        bean.setInitParameters(initParameters);
        return bean;
    }

    // Spring Cloud 1.X 不需要我们手动注册，但 Spring Cloud 2.X 开始需要我们手动配置该 Servlet
    // 注册 Hystrix 监控用的 Servlet
    @Bean
    public ServletRegistrationBean<HystrixMetricsStreamServlet> hystrixStreamServlet() {
        return new ServletRegistrationBean<>(new HystrixMetricsStreamServlet(), "/hystrix.stream");
    }

    @Bean
    public FilterRegistrationBean<WebStatFilter> webStatFilter() {
        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>(new WebStatFilter());
        bean.setUrlPatterns(Collections.singletonList("/*"));
        Map<String, String> initParameters = new HashMap<>();
        initParameters.put(WebStatFilter.PARAM_NAME_EXCLUSIONS, ".js,*.css,/druid/*");
        bean.setInitParameters(initParameters);
        return bean;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(Gsonor.SN_SIMPLE.getGson());
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new AllEncompassingFormHttpMessageConverter());
        //converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(gsonHttpMessageConverter);
    }

/*

    // 通过该 ServerFactory 生成 TomcatWebServer
    // （servlet 容器采用 TomcatServletWebServerFactory，reactive 容器采用 TomcatReactiveWebServerFactory）
    @Bean
    public ServletWebServerFactory tomcatServerFactory(TomcatConnectorCustomizer tomcatConnectorCustomizer) {
        TomcatServletWebServerFactory serverFactory = new TomcatServletWebServerFactory();
        serverFactory.addConnectorCustomizers(tomcatConnectorCustomizer);
        return serverFactory;
    }
*/

}
/*
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(Gsonor.SN_SIMPLE.getGson());
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new AllEncompassingFormHttpMessageConverter());
        //converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(gsonHttpMessageConverter);
    }

}
*/

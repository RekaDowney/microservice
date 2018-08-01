package me.junbin.microservice.service;

import me.junbin.microservice.domain.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018-07-30 10:00
 * @description : {@link FeignClient#fallbackFactory()} 方法指定的类必须在 Spring 容器中存在实例，即 {@link ApplicationContext#getBean(Class)} 必须获取得到实例
 */
@RequestMapping("/user")
@FeignClient(name = "${microservice.provider.name}"/*, fallbackFactory = FeignUserServiceFallbackFactory.class*/)
public interface FeignUserService {

    // 默认会通过 Ribbon 负载规则进行服务访问
    // 当前版本（2.0.0.RELEASE）的 spring-cloud-starter-openfeign 支持 GetMapping、DeleteMapping、PostMapping 等 RequestMapping 衍生注解，
    // 而在一些旧版本的 spring-cloud-starter-feign （注意旧版本的 artifictId 不一样）会出现不支持 GetMapping 这类衍生注解。

    @PostMapping
    void append(User user);

    // 这里映射不支持 {id:\\d+} 这种格式（会抛出异常），即无法采用 SpringMVC 的正则校验
    @DeleteMapping("/{id}")
    void delete(@PathVariable("id") Long id);

    @GetMapping("/{id}")
    User findById(@PathVariable("id") Long id);

    @GetMapping("/list")
    List<User> findAll();

}

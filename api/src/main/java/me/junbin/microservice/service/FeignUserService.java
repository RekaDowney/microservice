package me.junbin.microservice.service;

import me.junbin.microservice.domain.User;
import me.junbin.microservice.service.fallback.FeignUserServiceFallbackFactory;
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
@FeignClient(name = "${microservice.provider.name}", fallbackFactory = FeignUserServiceFallbackFactory.class)
public interface FeignUserService {

    // 默认会通过轮询的方式来访问服务

    @PostMapping
    void append(User user);

    @DeleteMapping("/{id:\\d+}")
    void delete(@PathVariable("id") Long id);

    @GetMapping("/{id:\\d+}")
    User findById(@PathVariable("id") Long id);

    @GetMapping("/list")
    List<User> findAll();

}

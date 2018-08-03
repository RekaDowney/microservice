package me.junbin.microservice.biz.web;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import me.junbin.commons.gson.Gsonor;
import me.junbin.microservice.biz.service.RemoteService;
import me.junbin.microservice.biz.service.UserService;
import me.junbin.microservice.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/20 9:49
 * @description :
 */
@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RemoteService remoteService;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public User append(@RequestBody User user) {
        LOGGER.info("请求添加客户：{}", Gsonor.SIMPLE.toJson(user));
        userService.append(user);
        return userService.findById(user.getId());
    }

    @DeleteMapping("/{id:\\d+}")
    public User delete(@PathVariable("id") long id) {
        LOGGER.info("请求删除用户（id：{}）", id);
        User user = userService.findById(id);
        userService.delete(id);
        return user;
    }

    @GetMapping("/{id:\\d+}")
    @HystrixCommand(fallbackMethod = "nullToDefaultUser")
    public User query(@PathVariable("id") long id) {
        LOGGER.info("请求查询用户（id：{}）", id);
        User user = userService.findById(id);
        if (user == null) {
            throw new NullPointerException();
        }
        return user;
    }

    @GetMapping("/list")
    public List<User> list() {
        LOGGER.info("请求查询所有用户");
        return userService.findAll();
    }

    @GetMapping(value = "/remote/{step1:\\d+}/{step2:\\d+}")
    // 参考 com.netflix.hystrix.HystrixCommandProperties 类
    // 特别注意：Hystrix 的 Properties 有两种类型，分别为 commandProperties 和 threadPoolProperties，不同的 properties 有不同的属性，具体参考：
    // commandProperties --> https://github.com/Netflix/Hystrix/wiki/Configuration#command-properties
    // threadPoolProperties --> https://github.com/Netflix/Hystrix/wiki/Configuration#threadpool-properties
    // 如果搞错了属性的类型，那么将会抛出如下异常：
    // Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception
    // [Request processing failed; nested exception is com.netflix.hystrix.contrib.javanica.exception.HystrixPropertyException:
    //      Failed to set Thread Pool properties. groupKey: 'UserController', commandKey: 'remote', threadPoolKey: 'null'] with root cause
    //          java.lang.IllegalArgumentException: unknown thread pool property: execution.isolation.thread.timeoutInMilliseconds
    @HystrixCommand(fallbackMethod = "timeout", commandProperties = {
            // 执行时间超过4s则超时，超时则抛出 com.netflix.hystrix.exception.HystrixTimeoutException: null
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "4000"),
            @HystrixProperty(name = "execution.timeout.enabled", value = "true")
    })
    public String remote(@PathVariable int step1, @PathVariable int step2) {
        try {
            int cost1 = remoteService.doStep1(step1);
            int cost2 = remoteService.doStep2(step2);
            return String.format("%d + %d = %d", cost1, cost2, (cost1 + cost2));
        } catch (InterruptedException e) {
            return "Error Occur! action has interrupted!";
        }
    }

    // 被 @HystrixCommand#fallbackMethod 修饰的方法，参数列表格式为：@HystrixCommand 修饰的方法的参数列表 再加上 可选的 Throwable 参数
    // 比如有如下代码
    // @HystrixCommand(fallbackMethod = "doSomethingFail")
    // public String doSomething(int arg1, String arg2) {}
    // public String doSomethingFail(int arg1, String arg2, Throwable t) {}
    // public String doSomethingFail(int arg1, String arg2) {}

    // 那么这时候如果 doSomething 抛出了异常，那么服务熔断的时候会优先调用 doSomethingFail(int arg1, String arg2, Throwable t)
    // 如果 doSomethingFail(int arg1, String arg2, Throwable t) 方法不存在，那么将会执行 doSomethingFail(int arg1, String arg2)
    // 如果连 doSomethingFail(int arg1, String arg2) 也不存在，则抛出如下异常

    // Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception
    // [Request processing failed; nested exception is com.netflix.hystrix.contrib.javanica.exception.FallbackDefinitionException:
    // fallback method wasn't found: nullToDefaultUser([long])] with root cause
    public User nullToDefaultUser(long id, Throwable e) {
        LOGGER.info("Throwable 异常信息为：{}", e.getMessage());
        return new User("ID为" + id + "的用户不存在").setId(id);
    }

    public String timeout(int step1, int step2) {
        String message = String.format("timeout for action with %d, %d", step1, step2);
        LOGGER.info(message);
        return message;
    }

    // 优先调用 timeout(int step1, int step2, Throwable t)，此时 Throwable 为 com.netflix.hystrix.exception.HystrixTimeoutException: null
    public String timeout(int step1, int step2, Throwable t) {
        String message = String.format("timeout for action with %d, %d", step1, step2);
        LOGGER.info(message, t);
        return message;
    }

}

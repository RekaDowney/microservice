package me.junbin.microservice.web;

import me.junbin.commons.gson.Gsonor;
import me.junbin.microservice.domain.User;
import me.junbin.microservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private DiscoveryClient client;
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
    public User query(@PathVariable("id") long id) {
        LOGGER.info("请求查询用户（id：{}）", id);
        return userService.findById(id);
    }

    @GetMapping("/list")
    public List<User> list() {
        LOGGER.info("请求查询所有用户");
        return userService.findAll();
    }

    // 必须先启动 EurekaServer，服务发现功能才能正常使用
    @GetMapping("/service/discovery")
    public Object serviceDiscovery() {
        Map<String, Object> result = new HashMap<>();
        String description = client.description();
        System.out.println(client.getInstances("MicroService-Provider8081")); // EurekaDiscoveryClient$EurekaServiceInstance
        System.out.println(client.getServices()); // [microservice-provider8081]
        result.put("description", description);
        for (String service : client.getServices()) {
            List<Object> serviceInfo = new ArrayList<>();
            for (ServiceInstance instance : client.getInstances(service)) {
                Map<String, Object> instanceInfo = new HashMap<>();
                instanceInfo.put("serviceId", instance.getServiceId());
                instanceInfo.put("scheme", instance.getScheme());
                instanceInfo.put("host", instance.getHost());
                instanceInfo.put("port", instance.getPort());
                instanceInfo.put("Uri: ", instance.getUri());
                instanceInfo.put("secure: ", instance.isSecure());
                serviceInfo.add(instanceInfo);
                System.out.println("MetaData: " + instance.getMetadata());
            }
            result.put(service, serviceInfo);
        }
        return result;
    }

}

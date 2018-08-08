package me.junbin.microservice.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/8/8 0:18
 * @description :
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Value("${spring.application.name}")
    private String applicationName;

/*
    @Value("${eureka.client.service-url.defaultZone}")
    private String eurekaUrl;
*/

    @Value("${server.port}")
    private int port;

    @GetMapping({"", "/"})
    public Object getConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("applicationName", applicationName);
//        result.put("eurekaUrl", eurekaUrl);
        result.put("port", port);
        return result;
    }

}

package me.junbin.microservice.web;

import com.netflix.loadbalancer.IRule;
import me.junbin.commons.web.WebUtils;
import me.junbin.microservice.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/20 11:30
 * @description :
 */
@RestController
@RequestMapping(value = "/consumer/user")
public class ConsumerUserController {

    @Value("${microservice.provider.baseHost}")
    private String baseHost;
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/{id:\\d+}")
    public User query(@PathVariable long id) {
        return queryUser(id);
    }

    @PostMapping
    public User append(@RequestBody User user) {
        HttpEntity requestEntity = new HttpEntity<>(user, null);
        ResponseEntity<User> responseEntity = restTemplate.exchange(baseHost + "/user", HttpMethod.POST, requestEntity, User.class);
        return queryUser(responseEntity.getBody().getId());
    }

    @GetMapping("/list")
    public List<User> list() {
        return restTemplate.exchange(baseHost + "/user/list", HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
        }).getBody();
    }

    @GetMapping("/rule")
    public Object currentRule() {
        WebApplicationContext webAppCtx = WebUtils.currentWebAppCtx(WebUtils.currentRequest().getServletContext());
        Map<String, IRule> beansOfType = webAppCtx.getBeansOfType(IRule.class);
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, IRule> entry : beansOfType.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getClass().getName());
        }
        return result;
    }

    private User queryUser(@PathVariable long id) {
        ResponseEntity<User> responseEntity = restTemplate.exchange(baseHost + "/user/{0}", HttpMethod.GET, null, User.class, id);
        if (responseEntity.hasBody()) {
            return responseEntity.getBody();
        }
        return null;
    }

}

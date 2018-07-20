package me.junbin.microservice.web;

import me.junbin.microservice.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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

    private User queryUser(@PathVariable long id) {
        ResponseEntity<User> responseEntity = restTemplate.exchange(baseHost + "/user/{0}", HttpMethod.GET, null, User.class, id);
        if (responseEntity.hasBody()) {
            return responseEntity.getBody();
        }
        return null;
    }

}

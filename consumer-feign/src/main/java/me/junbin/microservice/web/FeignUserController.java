package me.junbin.microservice.web;

import me.junbin.microservice.domain.User;
import me.junbin.microservice.service.FeignUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/30 10:47
 * @description :
 */
@RestController
@RequestMapping("/consumer/user")
public class FeignUserController {

    @Autowired
    private FeignUserService userService;

    @GetMapping("/{id:\\d+}")
    public User query(@PathVariable long id) {
        return userService.findById(id);
    }

    @DeleteMapping("/{id:\\d+}")
    public User delete(@PathVariable long id) {
        User user = userService.findById(id);
        userService.delete(id);
        return user;
    }

    @PostMapping
    public User append(@RequestBody User user) {
        userService.append(user);
        return userService.findAll().stream()
                .filter(u -> u.getUsername().equals(user.getUsername()))
                .findFirst().orElse(new User());
    }

    @GetMapping("/list")
    public List<User> list() {
        return userService.findAll();
    }

}

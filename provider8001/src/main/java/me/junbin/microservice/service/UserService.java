package me.junbin.microservice.service;

import me.junbin.microservice.domain.User;

import java.util.List;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/19 23:23
 * @description :
 */
public interface UserService {

    void append(User user);

    void delete(Long id);

    User findById(Long id);

    List<User> findAll();

}

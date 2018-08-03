package me.junbin.microservice.biz.repo;

import me.junbin.microservice.domain.User;

import java.util.List;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/19 23:23
 * @description :
 */
public interface UserRepo {

    int insert(User user);

    int deleteById(Long id);

    User findById(Long id);

    List<User> findAll();

}

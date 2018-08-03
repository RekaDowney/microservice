package me.junbin.microservice.biz.service.impl;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import me.junbin.microservice.biz.repo.UserRepo;
import me.junbin.microservice.biz.service.UserService;
import me.junbin.microservice.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/20 9:36
 * @description :
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    @HystrixCommand(fallbackMethod = "appendFail")
    public void append(User user) {
        userRepo.insert(user);
    }

    @Override
    public void delete(Long id) {
        userRepo.deleteById(id);
    }

    @Override
    public User findById(Long id) {
        return userRepo.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepo.findAll();
    }

    // 添加客户Reka失败。异常堆栈为：
    //org.springframework.dao.DuplicateKeyException:
    //### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'Reka' for key 'UNIQUE_IDX_USERNAME'
    //### The error may involve defaultParameterMap
    //### The error occurred while setting parameters
    //### SQL: INSERT INTO USER (USERNAME, DB_SOURCE) VALUES (?, DATABASE())
    //### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'Reka' for key 'UNIQUE_IDX_USERNAME'
    //; ]; Duplicate entry 'Reka' for key 'UNIQUE_IDX_USERNAME'; nested exception is java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'Reka' for key 'UNIQUE_IDX_USERNAME'
    public void appendFail(User user, Throwable t) {
        LOGGER.error(String.format("添加客户%s失败。异常堆栈为：", user.getUsername()), t);
    }

}

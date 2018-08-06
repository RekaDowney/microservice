package me.junbin.microservice.service.fallback;

import feign.hystrix.FallbackFactory;
import me.junbin.microservice.domain.User;
import me.junbin.microservice.service.FeignUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018-07-30 10:28
 * @description : 特别注意：实现 {@link FallbackFactory<FeignClient>} 接口的类必须注册到 Spring 后才能在被 {@link FeignClient} 标注的类中所使用
 */
//@Component
public class FeignUserServiceFallbackFactory implements FallbackFactory<FeignUserService> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeignUserServiceFallbackFactory.class);

    @Override
    public FeignUserService create(Throwable cause) {
        return new FeignUserService() {
            @Override
            public void append(User user) {
                LOGGER.error(String.format("添加客户 %s 失败", user), cause);
            }

            @Override
            public void delete(Long id) {
                LOGGER.error(String.format("删除客户 [id：%d] 失败", id), cause);
            }

            @Override
            public User findById(Long id) {
                LOGGER.info(String.format("查询客户 [id：%d] 失败", id), cause);
                return new User(String.format("id为%d的客户不存在", id)).setId(id);
            }

            @Override
            public List<User> findAll() {
                LOGGER.info("查询客户列表失败", cause);
                return null;
            }
        };
    }

}

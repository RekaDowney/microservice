package me.junbin.microservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/19 22:50
 * @description :
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    // ID
    private Long id;
    // 用户名
    private String username;
    // 数据库名称
    private String dbSource;

    public User() {
    }

    public User(String username) {
        this.username = username;
    }

}

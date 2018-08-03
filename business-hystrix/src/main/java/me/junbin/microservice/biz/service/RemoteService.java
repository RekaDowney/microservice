package me.junbin.microservice.biz.service;

import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/8/2 10:18
 * @description :
 */
@Service
public class RemoteService {

    public int doStep1(int cost) throws InterruptedException {
        TimeUnit.SECONDS.sleep(cost);
        return new Random().nextInt(10);
    }

    public int doStep2(int cost) throws InterruptedException {
        TimeUnit.SECONDS.sleep(cost);
        return new Random().nextInt(20);
    }

}

package me.junbin.microservice.configuration;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018-07-26 17:47
 * @description : 添加停止 SpringBoot 的优雅处理方案，通过 kill -SEGTERM ${springbootPid} 或者 kill -15 ${springbootPid} 方式请求停止 SpringBoot
 */
public class GracefulShutdownTomcat implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GracefulShutdownTomcat.class);
    private volatile Connector connector;
    private final long waitForShutdownSeconds;

    public GracefulShutdownTomcat(long waitForShutdownSeconds) {
        this.waitForShutdownSeconds = waitForShutdownSeconds;
    }

    @Override
    public void customize(Connector connector) {
        this.connector = connector;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        this.connector.pause();
        Executor executor = this.connector.getProtocolHandler().getExecutor();
        if (executor instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
            threadPoolExecutor.shutdown();
            try {
                if (!threadPoolExecutor.awaitTermination(this.waitForShutdownSeconds, TimeUnit.SECONDS)) {
                    LOGGER.warn("无法在 {} 秒内结束 Tomcat 进程", this.waitForShutdownSeconds);
                }
            } catch (InterruptedException e) { // 捕获到中断异常时对当前线程执行请求中断
                LOGGER.warn("等待 {} 秒内结束 Tomcat 进程时被请求中断 --> {}", this.waitForShutdownSeconds, e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

}

package me.junbin.microservice.configuration;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018-07-26 17:47
 * @description : 添加停止 SpringBoot 的优雅处理方案，通过 kill -SIGTERM ${springbootPid} 或者 kill -15 ${springbootPid} 方式请求停止 SpringBoot
 */
public class GracefulShutdownTomcat implements TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GracefulShutdownTomcat.class);
    private volatile Connector connector;
    private final int waitForShutdownSeconds;

    public GracefulShutdownTomcat(int waitForShutdownSeconds) {
        this.waitForShutdownSeconds = waitForShutdownSeconds;
    }

    @Override
    public void customize(Connector connector) {
        this.connector = connector;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        LOGGER.info("停止 Tomcat Connector...");
        this.connector.pause();

        ApplicationContext applicationContext = event.getApplicationContext();
        // 获取整个容器中过的 TaskExecutor
        Map<String, AsyncTaskExecutor> taskExecutorMap = applicationContext.getBeansOfType(AsyncTaskExecutor.class);
        if (!taskExecutorMap.isEmpty()) {
            LOGGER.info("关闭容器中的线程池（仅当该线程池实例是 ThreadPoolTaskExecutor 实例时才关闭），总共有{}个线程池相关实例", taskExecutorMap.size());
            for (Map.Entry<String, AsyncTaskExecutor> entry : taskExecutorMap.entrySet()) {
                AsyncTaskExecutor taskExecutor = entry.getValue();
                // 如果是 ThreadPoolTaskExecutor 实例则等待关闭
                if (taskExecutor instanceof ThreadPoolTaskExecutor) {
                    LOGGER.info("准备关闭线程池 [BeanName： {}]，有必要的话会等待 {} 秒以关闭该线程池", entry.getKey(), this.waitForShutdownSeconds);
                    ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) taskExecutor;
                    executor.setWaitForTasksToCompleteOnShutdown(true);
                    executor.setAwaitTerminationSeconds(this.waitForShutdownSeconds);
                    executor.shutdown();
                } else {
                    LOGGER.info("BeanName 为 {} 的线程池实例类型为 {}，不执行关闭操作", entry.getKey(), taskExecutor.getClass());
                }
            }
        }

        Executor executor = this.connector.getProtocolHandler().getExecutor();
        if (executor instanceof ThreadPoolExecutor) {
            LOGGER.info("关闭线程池（不再接收新的任务），有必须的话等待{}秒以便线程池完成现有任务...", this.waitForShutdownSeconds);
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

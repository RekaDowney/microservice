package me.junbin.microservice.ribbon.rule;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018-07-27 10:45
 * @description :
 */
public class RoundRobinTimesRule extends AbstractLoadBalancerRule {

    private AtomicInteger nextServerCyclicCounter;
    private AtomicInteger currentServerTimesCounter;
    private final int currentServerMaxTimes; // 指定一次轮询中使用多少次
    private static final Logger LOGGER = LoggerFactory.getLogger(RoundRobinTimesRule.class);

    // 无法将 currentServerMaxTimes 作为构造方法参数，原因是 com.netflix.client.ClientFactory.instantiateInstanceWithClientConfig()
    // 会根据返回的 IRule 类型直接调用 newInstance，如果没有无参构造方法那么将会抛出实例化异常
    public RoundRobinTimesRule() {
        this.currentServerMaxTimes = 3;
        this.nextServerCyclicCounter = new AtomicInteger(0);
        this.currentServerTimesCounter = new AtomicInteger(0);
    }

    public RoundRobinTimesRule(ILoadBalancer loadBalancer) {
        this();
        super.setLoadBalancer(loadBalancer);
    }

/*
    public RoundRobinTimesRule(int currentServerMaxTimes) {
        this.currentServerMaxTimes = currentServerMaxTimes;
        this.nextServerCyclicCounter = new AtomicInteger(0);
        this.currentServerTimesCounter = new AtomicInteger(0);
    }

    public RoundRobinTimesRule(int currentServerMaxTimes, ILoadBalancer loadBalancer) {
        this(currentServerMaxTimes);
        super.setLoadBalancer(loadBalancer);
    }
*/

    private Server choose(ILoadBalancer loadBalancer, Object keyIgnored) {
        if (null == loadBalancer) {
            LOGGER.warn("no load balancer");
            return null;
        }
        Server server;
        int count = 0;
        while (count++ < 10) {
            List<Server> reachableServers = loadBalancer.getReachableServers();
            List<Server> allServers = loadBalancer.getAllServers();
            int upCount = reachableServers.size();
            int serverCount = allServers.size();

            if ((upCount == 0) || (serverCount == 0)) {
                LOGGER.warn("No up servers available from load balancer: {}", loadBalancer);
                return null;
            }

            int nextServerIndex = nextServerIndex(serverCount);
            server = allServers.get(nextServerIndex);

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }
        }

        if (count >= 10) {
            LOGGER.warn("No available alive servers after 10 tries from load balancer: {}", loadBalancer);
        }
        return null;
    }

    private int nextServerIndex(int serverCount) {
        while (true) {
            int current = nextServerCyclicCounter.get();
            int currentTimes = currentServerTimesCounter.get();
            if (currentTimes < currentServerMaxTimes) {
                int curTimesPlus1 = currentTimes + 1;
                if (currentServerTimesCounter.compareAndSet(currentTimes, curTimesPlus1)) {
                    return current;
                }
            } else {
                if (currentServerTimesCounter.compareAndSet(currentTimes, 0)) {
                    int next = (current + 1) % serverCount;
                    if (nextServerCyclicCounter.compareAndSet(current, next)) {
                        return next;
                    }
                }
            }
        }
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }

}

package me.junbin.microservice.biz.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import java.sql.SQLException;
import java.util.Properties;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018-08-01 11:43
 * @description :
 */
@Configuration
public class DataSourceConfiguration implements EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Primary
    @Bean(value = "dataSource", initMethod = "init", destroyMethod = "close")
    public DruidDataSource dataSource() throws SQLException {

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(environment.getProperty("jdbc.driver.class.name"));
        dataSource.setUrl(environment.getProperty("jdbc.url"));
        dataSource.setUsername(environment.getProperty("jdbc.username"));
        dataSource.setPassword(environment.getProperty("jdbc.password"));
        dataSource.setInitialSize(environment.getProperty("jdbc.druid.initialSize", int.class));
        dataSource.setMinIdle(environment.getProperty("jdbc.druid.minIdle", int.class));
        dataSource.setMaxActive(environment.getProperty("jdbc.druid.maxActive", int.class));
        dataSource.setMaxWait(environment.getProperty("jdbc.druid.maxWait", int.class));
        dataSource.setTimeBetweenEvictionRunsMillis(environment.getProperty("jdbc.druid.timeBetweenEvictionRunsMillis", int.class));
        dataSource.setMinEvictableIdleTimeMillis(environment.getProperty("jdbc.druid.minEvictableIdleTimeMillis", int.class));
        dataSource.setValidationQuery(environment.getProperty("jdbc.druid.validationQuery"));
        dataSource.setTestWhileIdle(environment.getProperty("jdbc.druid.testWhileIdle", boolean.class));
        dataSource.setTestOnBorrow(environment.getProperty("jdbc.druid.testOnBorrow", boolean.class));
        dataSource.setTestOnReturn(environment.getProperty("jdbc.druid.testOnReturn", boolean.class));
        dataSource.setPoolPreparedStatements(environment.getProperty("jdbc.druid.poolPreparedStatements", boolean.class));
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(environment.getProperty("jdbc.druid.maxPoolPreparedStatementPerConnectionSize", int.class));
        dataSource.setUseGlobalDataSourceStat(environment.getProperty("jdbc.druid.useGlobalDataSourceStat", boolean.class));

        Properties properties = new Properties();
        String connProperties = environment.getProperty("jdbc.druid.connectionProperties");
        for (String property : connProperties.split(";")) {
            // 直接使用 split("=") 会导致 Base64 编码的 publicKey（结尾可能有一个或者两个 = ）出现问题
            // 解决方案一：直接通过 indexOf('=') 进行切割
            // 解决方案二：依旧使用 split("=")，检测 arr[0] 是否是 config.decrypt.key，是的话检测 arr[1] 的长度 % 4，如果结果为 0，则补上足够的 = 使得长度 % 4 == 0
            int idx = property.indexOf('=');
            properties.setProperty(property.substring(0, idx), property.substring(idx + 1));
        }
        dataSource.setConnectProperties(properties);

        // config 过滤器指向 com.alibaba.druid.filter.config.ConfigFilter
        dataSource.setFilters(environment.getProperty("jdbc.druid.filters"));
        return dataSource;
    }

}
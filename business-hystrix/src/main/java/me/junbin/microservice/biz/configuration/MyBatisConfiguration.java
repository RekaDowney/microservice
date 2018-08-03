package me.junbin.microservice.biz.configuration;

import com.alibaba.druid.pool.DruidDataSource;
import me.junbin.commons.converter.mybatis.LocalDateTimeTypeHandler;
import me.junbin.microservice.biz.repo.UserRepo;
import me.junbin.microservice.domain.User;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.concurrent.TimeUnit;

/**
 * @author : Zhong Junbin
 * @email : <a href="mailto:rekadowney@gmail.com">发送邮件</a>
 * @createDate : 2018/7/20 9:31
 * @description :
 */
@org.springframework.context.annotation.Configuration
@Import(DataSourceConfiguration.class)
public class MyBatisConfiguration {

    @Bean
    public Configuration configuration() {
        Configuration cfg = new Configuration();
        cfg.setMapUnderscoreToCamelCase(true);
        cfg.setDefaultStatementTimeout((int) TimeUnit.SECONDS.toMillis(3));
        return cfg;
    }


    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactory(DruidDataSource dataSource,
                                                   Configuration configuration) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
//        factoryBean.setTypeAliasesPackage("me.junbin.crawler.laosiji.domain");
//        factoryBean.setTypeHandlersPackage("me.junbin.commons.converter.mybatis");
        factoryBean.setTypeAliasesPackage(User.class.getPackage().getName());
        factoryBean.setTypeHandlersPackage(LocalDateTimeTypeHandler.class.getPackage().getName());
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        factoryBean.setMapperLocations(resolver.getResources("classpath:mapper/**/*.xml"));
        factoryBean.setConfiguration(configuration);
        // 避免打包成 SpringBoot ExecutableJar 之后运行时抛出
        // org.apache.ibatis.builder.BuilderException: Error resolving class. Cause: org.apache.ibatis.type.TypeException: Could not resolve type alias 'user'.  Cause: java.lang.ClassNotFoundException: Cannot find class: user
        factoryBean.setVfs(SpringBootVFS.class);
        return factoryBean;
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setBasePackage(UserRepo.class.getPackage().getName());
        configurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return configurer;
    }

}

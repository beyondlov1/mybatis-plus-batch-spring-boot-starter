package com.beyond.mybatisplus.batch.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.beyond.mybatisplus.batch.sqlinject.CustomerSqlInjector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author beyond
 * @date 2020/07/17
 */
@Configuration
public class MybatisPlusBatchAutoConfiguration {
    @Bean
    public ISqlInjector sqlInjector(){
        return new CustomerSqlInjector();
    }
}

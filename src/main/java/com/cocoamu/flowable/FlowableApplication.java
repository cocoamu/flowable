package com.cocoamu.flowable;

import com.cocoamu.flowable.config.AppDispatcherServletConfiguration;
import com.cocoamu.flowable.config.ApplicationConfiguration;
import com.cocoamu.flowable.config.DatabaseAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.Import;


@Import(value={
        // 引入修改的配置
        ApplicationConfiguration.class,
        AppDispatcherServletConfiguration.class,
        // 引入 DatabaseConfiguration 表更新转换
//        DatabaseConfiguration.class,
        // 引入 DatabaseAutoConfiguration 表更新转换
        DatabaseAutoConfiguration.class})
@SpringBootApplication(exclude={SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
public class FlowableApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowableApplication.class, args);
    }

}

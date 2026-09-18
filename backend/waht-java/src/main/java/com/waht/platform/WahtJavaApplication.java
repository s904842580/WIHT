package com.waht.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WAHT Java 后端启动入口，负责加载 Spring Boot 组件和 MyBatis Mapper。
 */
@MapperScan(basePackages = {"com.waht.platform.mapper", "com.waht.platform.agent.mapper", "com.waht.platform.audit"},
        annotationClass = org.apache.ibatis.annotations.Mapper.class)
@SpringBootApplication
public class WahtJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WahtJavaApplication.class, args);
    }
}

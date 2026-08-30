package com.waht.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WAHT Java 后端启动入口，负责加载 Spring Boot 组件和 MyBatis Mapper。
 */
@MapperScan("com.waht.platform.mapper")
@SpringBootApplication
public class WahtJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WahtJavaApplication.class, args);
    }
}

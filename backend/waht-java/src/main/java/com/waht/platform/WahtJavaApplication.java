package com.waht.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.waht.platform.mapper")
@SpringBootApplication
public class WahtJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WahtJavaApplication.class, args);
    }
}

package com.travel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.travel.mapper")
@SpringBootApplication
public class TravelPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelPlatformApplication.class, args);
    }

}

package com.lyentech.bdc.md.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author guolanren
 */
@SpringBootApplication
@EnableCaching
@EnableFeignClients
@MapperScan("com.lyentech.bdc.md.auth.dao")
@EnableAsync
public class MdAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdAuthApplication.class, args);
    }

}

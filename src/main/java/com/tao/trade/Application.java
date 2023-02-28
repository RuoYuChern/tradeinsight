package com.tao.trade;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableFeignClients
@Slf4j
@MapperScan("com.tao.trade.infra.db.mapper")
@EnableTransactionManagement
public class Application {
    public static void main( String[] args ){
        log.info("TaiChi trading started!");
        SpringApplication.run(Application.class, args);
    }
}

package com.tao.trade;

import com.tao.trade.proccess.JobActors;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableFeignClients
@Slf4j
@MapperScan("com.tao.trade.infra.db.mapper")
@EnableTransactionManagement
public class Application implements CommandLineRunner {
    @Autowired
    private JobActors actors;
    public static void main( String[] args ){
        log.info("TaiChi trading started!");
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        actors.loadData();
    }
}

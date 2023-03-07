package com.tao.trade.infra.http;

import feign.Feign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@Slf4j
public class TuFeignConfigure {
    public TuFeignConfigure(){
        log.info("TuFeignConfigure");
    }

    @Bean
    public Feign.Builder custom(){
        return Feign.builder().retryer(new FeignRetry());
    }
}

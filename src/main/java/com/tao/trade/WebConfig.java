package com.tao.trade;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class WebConfig {
    private static String taoSalt;

    public static String getTaoSalt() {
        return taoSalt;
    }

    @Value("${tao.security.salt}")
    public void setTaoSalt(String str){
        log.info("salt:{}", str);
        taoSalt = str;
    }
    @Bean
    public FilterRegistrationBean filterRegistrationBean(){
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(new TaoFilter());
        //过滤所有路径
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("taoFilter");
        return registrationBean;
    }
}

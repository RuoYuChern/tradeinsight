package com.tao.trade;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {
    @Bean
    public FilterRegistrationBean filterRegistrationBean(){
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(new TaoFilter());
        //过滤所有路径
        registrationBean.addUrlPatterns("/*");
        registrationBean.setName("taoFilter");
        return registrationBean;
    }
}

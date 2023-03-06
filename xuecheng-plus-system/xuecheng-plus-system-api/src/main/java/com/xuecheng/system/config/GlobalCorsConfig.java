package com.xuecheng.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-02-28 14:46
 **/
@Configuration
public class GlobalCorsConfig {
    //允许跨域调用的过滤器
    @Bean
    public CorsFilter getCorsFilter(){

        CorsConfiguration config = new CorsConfiguration();
        //允许白名单域名跨域
        config.addAllowedOrigin("*");

        //允许所有请求方法调用
        config.addAllowedMethod("*");

        //允许跨域发送cookie
        config.setAllowCredentials(true);

        //允许全部头信息
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);

        return new CorsFilter(source);
    }

}

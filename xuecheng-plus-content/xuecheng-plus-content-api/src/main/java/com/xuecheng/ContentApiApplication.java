package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableSwagger2Doc
@SpringBootApplication
@EnableFeignClients(basePackages={"com.xuecheng.content.feignclient"})
public class ContentApiApplication {


    public static void main(String[] args) {

        SpringApplication.run(ContentApiApplication.class, args);

    }

}

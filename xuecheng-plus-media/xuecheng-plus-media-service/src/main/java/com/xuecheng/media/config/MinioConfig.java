package com.xuecheng.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-03-07 20:19
 **/
@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    String endpoint;
    @Value("${minio.accessKey}")
    String accessKey;
    @Value("${minio.secretKey}")
    String secretKey;

    @Bean
    public MinioClient minioClient(){
//        System.out.println("==========================================================="+endpoint);
        MinioClient minioClient = MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
        return minioClient;
    }
}

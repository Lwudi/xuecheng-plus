package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-03-17 20:10
 **/
@SpringBootTest
public class FeignUploadTest {
    @Autowired
    MediaServiceClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() throws IOException {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("E:\\develop\\test.html"));

        String upload = mediaServiceClient.uploadFile(multipartFile, "course", "test.html");
        if(upload==null){
            System.out.println("走了降级逻辑");
        }


    }
    }

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.media.service.MediaProcessService;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-03-07 11:20
 **/



public class Test {
    @Autowired
    MediaProcessService mediaProcessService;
    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("https://play.min.io")
                    .endpoint("http://192.168.90.123:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();
    @org.junit.jupiter.api.Test
    public void uploadtest(){
        try {
            UploadObjectArgs testbucket = UploadObjectArgs.builder().bucket("testbucket").object("01-今日课程介绍.mp4").
                    filename("E:\\学习\\1、微服务开发框架SpringCloud+RabbitMQ+Docker+Redis+搜索+分布式微服务全技术栈课程\\实用篇\\视频教程\\day01-SpringCloud01\\01-今日课程介绍.mp4").build();
            minioClient.uploadObject(testbucket);
            System.out.println("上传成功");
        } catch (Exception e) {
            System.out.println("上传失败了");
        }
    }

    @org.junit.jupiter.api.Test
    public void deleteTest(){
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("video").object("video/e/c/ec169eaf7263eb64605ec9fab710485b").build();

        try {
            minioClient.removeObject(removeObjectArgs);
            System.out.println("删除成功");
        }catch (Exception e){
            System.out.println("删除失败了");
        }
    }

    @org.junit.jupiter.api.Test
    public void getTest(){

        GetObjectArgs getObjectArgs =GetObjectArgs.builder().bucket("testbucket").object("01-今日课程介绍.mp4").build();

        try(
            FilterInputStream object = minioClient.getObject(getObjectArgs);
            FileOutputStream fileOutputStream = new FileOutputStream(new File("E:\\学习\\1、微服务开发框架SpringCloud+RabbitMQ+Docker+Redis+搜索+分布式微服务全技术栈课程\\实用篇\\视频教程\\1.mp4"));)
        {
            if (object!=null){
                IOUtils.copy(object,fileOutputStream);
            }
        }catch (Exception e){
            System.out.println("获取失败了");
        }
    }

    @org.junit.jupiter.api.Test
    void bbb(){
        String a ="";
        int length = a.length();
        System.out.println(length);
    }


}

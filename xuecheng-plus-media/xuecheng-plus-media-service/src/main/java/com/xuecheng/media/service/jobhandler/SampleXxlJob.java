package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.exception.XueChengException;
import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import com.xuecheng.media.service.MediaProcessService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * XxlJob开发示例（Bean模式）
 *
 * 开发步骤：
 *      1、任务开发：在Spring Bean实例中，开发Job方法；
 *      2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
@Slf4j
public class SampleXxlJob {
    @Autowired
    MediaProcessService mediaProcessService;

    @Autowired
    MediaFileService mediaFileService;
    @Autowired
    MinioClient minioClient;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpeg_path;

    /**
     * 视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void demoJobHandler2() throws Exception {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();
        log.error("分片序号：{}，分片总数：{}",shardIndex,shardTotal);

        // default success
        int size;
        //线程数
        int processors = Runtime.getRuntime().availableProcessors();
        List<MediaProcess> mediaProcessList = mediaProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        //任务数量
        size=mediaProcessList.size();
        log.debug("取出待处理视频任务{}条", size);
        //创建一个线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        //计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);

        for (MediaProcess mediaProcess : mediaProcessList) {
            executorService.execute(() -> {
                //任务执行逻辑

                try {
                    //开始任务
                    boolean result1 = mediaProcessService.startTask(mediaProcess.getId());
                    if (!result1) {
                        return;
                    }
                    log.debug("开始执行任务:{}", mediaProcess);
                    //视频处理逻辑
                    //桶
                    String bucket = mediaProcess.getBucket();
                    //源文件路径
                    String filePath = mediaProcess.getFilePath();
                    //源文件MD5文件名
                    String fileId = mediaProcess.getFileId();
                    //源文件名称
                    String filename = mediaProcess.getFilename();
                    //将要处理的文件下载到服务器上
                    File original = mediaFileService.downloadFileFromMinIO(bucket, filePath);
                    if (original == null) {
                        log.debug("下载待处理文件失败,originalFile:{}", mediaProcess.getBucket().concat(mediaProcess.getFilePath()));
                        mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "下载待处理文件失败");
                        return;
                    }

                    //创建临时文件，作为转换后的文件
                    File mp4File = null;
                    try {
                        mp4File = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.error("创建临时文件异常：{}", e.getMessage());
                        mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "创建mp4临时文件失败");
                        return;
                    }
                    //视频源avi的路径
                    String video_path = original.getAbsolutePath();
                    //转换为MP4文件的名称
                    String mp4_name = fileId + ".mp4";
                    ////转换为MP4文件的路径
                    String mp4folder_path = mp4File.getAbsolutePath();
                    Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
                    //开始转换
                    String result = "";
                    result = mp4VideoUtil.generateMp4();
                    if (!"success".equals(result)) {
                        log.error("视频转码失败，原因：{},bucket:{},objectname:{}", result, bucket, filePath);
                        mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, result);
                        return;
                    }
                    String objectName = mediaFileService.getFilePath(fileId, ".mp4");
                    //访问url
                    String url = "/" + bucket + "/" + objectName;

                    //mp4上传到minio
                    try {
                        addMediaFilesToMinIO(mp4File.getAbsolutePath(), bucket, objectName);
                        //将url存储至数据，并更新状态为成功，并将待处理视频记录删除存入历史
                        mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "2", fileId, url, "成功上传");

                    } catch (Exception e) {

                        log.error("上传视频失败或入库失败,视频地址:{},错误信息:{}", bucket + objectName, e.getMessage());
                        //最终还是失败了
                        mediaProcessService.saveProcessFinishStatus(mediaProcess.getId(), "3", fileId, null, "处理后视频上传或入库失败");
                    }
                } finally {
                    countDownLatch.countDown();
                }


            });
        }
        //等待,给一个充裕的超时时间,防止无限等待，到达超时时间还没有处理完成则结束任务
        countDownLatch.await(30, TimeUnit.MINUTES);


    }

    private void addMediaFilesToMinIO(String filePath, String bucket, String objectName) {
        String extension = objectName.substring(objectName.lastIndexOf("."));
        String contentType = mediaFileService.getMimeType(extension);
        try {
            minioClient.uploadObject(UploadObjectArgs
                    .builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(filePath)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            XueChengException.err("上传到文件系统出错");
        }
    }

}

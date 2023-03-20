package com.xuecheng.media.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 * @description 媒资文件处理业务方法
 * @author itcast
 * @since 2023-03-07
 */
public interface MediaProcessService extends IService<MediaProcess> {
    /**
     * @description 获取待处理任务
     * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
     * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
     * @author Mr.M
     * @date 2022/9/14 14:49
     */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);
    /**
    * @description 更新任务状态
    * @param taskId 任务ID
     * @param status 当前状态
     * @param fileId 任务文件名称（MD5）
     * @param url
     * @param errorMsg 错误信息
    * @return void
    * @author 31151
    * @date 2023/3/12 16:11
    */
     void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg);

    /**
     *  开启一个任务
     * @param id 任务id
     * @return true开启任务成功，false开启任务失败
     */
    public boolean startTask(long id);




}

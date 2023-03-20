package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class MediaProcessServiceImpl extends ServiceImpl<MediaProcessMapper, MediaProcess> implements MediaProcessService {

    @Autowired
    MediaProcessMapper mediaProcessMapper;
    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count) {
        List<MediaProcess> mediaProcesses = mediaProcessMapper.selectListByShardIndex(shardTotal, shardIndex, count);

        return mediaProcesses;
    }

    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg) {
        // 查询这个任务
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess==null){
            log.error("更新任务状态时，此任务：{}为空",taskId);
            return;
        }
        //任务失败
        if ("3".equals(status)) {
            try {
                LambdaUpdateWrapper<MediaProcess> wrapper = new LambdaUpdateWrapper<>();
                wrapper.set(MediaProcess::getStatus,status)
                        .set(MediaProcess::getFailCount,mediaProcess.getFailCount()+1)
                        .set(MediaProcess::getFinishDate, LocalDateTime.now())
                        .set(MediaProcess::getErrormsg,errorMsg);

                wrapper.eq(MediaProcess::getId,taskId);
                this.update(wrapper);
            } catch (Exception e) {
                log.error("修改任务表失败{}",e.getMessage());
            }finally {
                return;
            }
        }
        //任务成功
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);

        //更新任务表中的url
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);
        //更新MediaProcess表中的状态
        LambdaUpdateWrapper<MediaProcess> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(MediaProcess::getStatus,status)
                .set(MediaProcess::getFinishDate,LocalDateTime.now())
                .set(MediaProcess::getUrl,url);

        wrapper.eq(MediaProcess::getId,taskId);
        this.update(wrapper);
        //将MediaProcess表中数据插入到MediaProcessHistory表
        mediaProcess=mediaProcessMapper.selectById(taskId);
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess,mediaProcessHistory);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        //在MediaProcess表中删除当前任务
        mediaProcessMapper.deleteById(taskId);

    }

    @Override
    public boolean startTask(long id) {
        int i = mediaProcessMapper.startTask(id);
        return (i<=0)?false:true;
    }


}

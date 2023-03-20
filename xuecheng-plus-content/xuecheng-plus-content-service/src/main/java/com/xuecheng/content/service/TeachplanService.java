package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author itcast
 * @since 2023-02-28
 */
public interface TeachplanService extends IService<Teachplan> {

    /**
    * @description 获取课程章节信息
    * @param courseId
    * @return java.util.List<com.xuecheng.content.model.dto.TeachplanDto>
    * @author 31151
    * @date 2023/3/5 14:16
    */
    List<TeachplanDto> findTeachplayTree(long courseId);


    /**
     * @description 保存课程计划（新增/修改）
     * @param teachplanDto  课程计划信息
     * @return void
     * @author Mr.M
     * @date 2022/9/9 13:39
     */
     void saveTeachplan(SaveTeachplanDto teachplanDto);
     /**
     * @description 删除章节（大小章节）
     * @param id
     * @return void
     * @author 31151
     * @date 2023/3/5 15:42
     */
     void removeChapter(Long id);
    /**
    * @description 小节位置上移/下移
    * @param moveType
     * @param teachplanId
    * @return void
    * @author 31151
    * @date 2023/3/5 16:29
    */
    void moveChapter(String moveType, Long teachplanId);

    /**
     * 教学计划绑定媒资信息
     * @param bindTeachplanMediaDto
     */
    void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto);

    /** 解绑教学计划与媒资信息
     * @param teachPlanId       教学计划id
     * @param mediaId           媒资信息id
     */
    void unassociationMedia(Long teachPlanId, String mediaId);

}

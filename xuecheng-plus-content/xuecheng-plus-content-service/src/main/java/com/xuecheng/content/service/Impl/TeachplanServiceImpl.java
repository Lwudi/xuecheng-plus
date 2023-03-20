package com.xuecheng.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exception.XueChengException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
@Transactional
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    /**
    * @description 按课程计划ID查询所有章节信息
    * @param courseId
    * @return java.util.List<com.xuecheng.content.model.dto.TeachplanDto>
    * @author 31151
    * @date 2023/3/5 15:43
    */
    @Override
    public List<TeachplanDto> findTeachplayTree(long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    /**
    * @description 修改课程计划（新增/修改）
    * @param teachplanDto
    * @return void
    * @author 31151
    * @date 2023/3/5 15:44
    */
    @Override
    public void saveTeachplan(SaveTeachplanDto teachplanDto) {

        Long id = teachplanDto.getId();
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan==null){
          //新增
            teachplan=new Teachplan();
            BeanUtils.copyProperties(teachplanDto,teachplan);
            int orderByInt = getTeachplanCount(teachplanDto.getCourseId(), teachplanDto.getParentid());
            teachplan.setOrderby(orderByInt+1);
            teachplanMapper.insert(teachplan);
        }else {
           //修改
            BeanUtils.copyProperties(teachplanDto,teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    /**
    * @description 按id删除章节（大小章节）
    * @param id
    * @return void
    * @author 31151
    * @date 2023/3/5 15:44
    */
    @Override
    public void removeChapter(Long id) {
        if (id == null){XueChengException.err("课程计划id为空");}

        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan.getParentid()==0&&teachplan.getGrade()==1){
            //删除大章节
            //查询章节下存在小节则不能删除
            int i = sectionCount(id);
            if (i>0){ XueChengException.err("课程计划信息还有子信息，无法操作");}

                teachplanMapper.deleteById(id);

        }else {
            //删除小节及对应视频信息
            LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper();
            wrapper.eq(TeachplanMedia::getTeachplanId,id);
            teachplanMapper.deleteById(id);
            teachplanMediaMapper.delete(wrapper);
        }

    }

    @Override
    public void moveChapter(String moveType, Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        // 获取层级和当前orderby，章节移动和小节移动的处理方式不同
        Integer grade = teachplan.getGrade();
        Integer orderby = teachplan.getOrderby();
                LambdaQueryWrapper <Teachplan>wrapper=new LambdaQueryWrapper<>();
                wrapper.eq(Teachplan::getGrade,grade);
                wrapper.eq(Teachplan::getCourseId,teachplan.getCourseId());
                wrapper.eq(Teachplan::getParentid,teachplan.getParentid());
                //上移
                if ("moveup".equals(moveType)) {
                    wrapper.lt(Teachplan::getOrderby,orderby);
                    wrapper.orderByDesc(Teachplan::getOrderby);
                }else if ("movedown".equals(moveType)){
                    //下移
                    wrapper.gt(Teachplan::getOrderby,orderby);
                    wrapper.orderByAsc(Teachplan::getOrderby);
                }
                wrapper.last("limit 1");
        Teachplan changedTeachplan = teachplanMapper.selectOne(wrapper);
            exchangeOrderby(teachplan,changedTeachplan);

    }
@Transactional
    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        String fileName = bindTeachplanMediaDto.getFileName();
        String mediaId = bindTeachplanMediaDto.getMediaId();
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();

        // 先根据请求参数查询出对应的教学计划teachplan
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            XueChengException.err("教学计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if(grade!=2){
            XueChengException.err("只允许第二级教学计划绑定媒资文件");
        }


        LambdaQueryWrapper<TeachplanMedia> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(TeachplanMedia::getTeachplanId,teachplanId);
         teachplanMediaMapper.delete(wrapper);
         TeachplanMedia teachplanMedia = new TeachplanMedia();
         teachplanMedia.setMediaFilename(fileName);
         teachplanMedia.setMediaId(mediaId);
         teachplanMedia.setTeachplanId(teachplanId);
         teachplanMedia.setCourseId(teachplan.getCourseId());
         teachplanMedia.setCreateDate(LocalDateTime.now());
//         teachplanMedia.setChangePeople();
//         teachplanMedia.setCreatePeople();

         teachplanMediaMapper.insert(teachplanMedia);

    }

    @Override
    public void unassociationMedia(Long teachPlanId, String mediaId) {
        LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeachplanMedia::getMediaId,mediaId).eq(TeachplanMedia::getTeachplanId,teachPlanId);
        teachplanMediaMapper.delete(wrapper);
    }

    private void exchangeOrderby(Teachplan teachplan,Teachplan changedTeachplan) {
        if (changedTeachplan==null){
            XueChengException.err("已经到头了不能再移动了。");
        }
        Integer orderby = teachplan.getOrderby();
        Integer orderby1 = changedTeachplan.getOrderby();
        teachplan.setOrderby(orderby1);
        changedTeachplan.setOrderby(orderby);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(changedTeachplan);

    }


    private int sectionCount(Long id) {
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getParentid,id);
        Integer integer = teachplanMapper.selectCount(wrapper);
        return integer;
    }

    //获取当前章节的小章节数
    private int getTeachplanCount(Long courseId, Long parentid) {
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getCourseId,courseId);
        wrapper.eq(Teachplan::getParentid,parentid);
        Integer integer = teachplanMapper.selectCount(wrapper);
        return integer;

    }
}

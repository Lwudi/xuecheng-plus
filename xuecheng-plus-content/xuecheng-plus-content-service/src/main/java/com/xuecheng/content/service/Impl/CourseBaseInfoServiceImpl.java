package com.xuecheng.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import com.xuecheng.base.exception.XueChengException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;

import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.plugin.com.Utils;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;



/**
 * @program: xuecheng-plus-group1
 * @description: 课程基本信息业务实现类
 * @author: lxw
 * @create: 2023-02-28 09:50
 **/
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    TeachplanMapper teachplanMapper;
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Autowired
    CourseMarketServiceImpl courseMarketService;
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams) {
        Page<CourseBase> page = new Page<>();
        page.setSize(pageParams.getPageSize());
        page.setCurrent(pageParams.getPageNo());

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper();
        //根据课程名称查询
        queryWrapper.like(StringUtils.isNotBlank(queryCourseParams.getCourseName()), CourseBase::getName, queryCourseParams.getCourseName());
        //根据课程审核状态查询
        queryWrapper.like(StringUtils.isNotBlank(queryCourseParams.getAuditStatus()), CourseBase::getAuditStatus, queryCourseParams.getAuditStatus());
        //根据课程发布状态查询
        queryWrapper.like(StringUtils.isNotBlank(queryCourseParams.getPublishStatus()), CourseBase::getStatus, queryCourseParams.getPublishStatus());
       Page<CourseBase> pageResult= courseBaseMapper.selectPage(page, queryWrapper);
        List<CourseBase> records = pageResult.getRecords();

        PageResult<CourseBase> basePageResult = new PageResult<CourseBase>(records,pageResult.getTotal(),pageParams.getPageNo(),pageParams.getPageSize());

        return basePageResult;
    }


    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
       /* //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
           XueChengException.err("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
           XueChengException.err("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            XueChengException.err("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {

            XueChengException.err("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {

            XueChengException.err("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {

            XueChengException.err("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {

            XueChengException.err("收费规则为空");
        }*/

        //新增对象
        CourseBase courseBaseNew = new CourseBase();
        //将填写的课程信息赋值给po对象
        BeanUtils.copyProperties(dto,courseBaseNew);
        //添加课程信息po的其他数据
            //设置审核状态
        courseBaseNew.setAuditStatus("202002");
            //设置发布状态
        courseBaseNew.setStatus("203001");
            //机构id
        courseBaseNew.setCompanyId(companyId);
            //添加时间
        courseBaseNew.setCreateDate(LocalDateTime.now());

        // 将基本信息添加到库
        int insert = courseBaseMapper.insert(courseBaseNew);
        Long courseId = courseBaseNew.getId();


        //课程营销信息
        CourseMarket courseMarketNew = new CourseMarket();
        //将dto中的营销收费信息赋值给po对象
        BeanUtils.copyProperties(dto,courseMarketNew);

        //给课程营销po添加其他数据
            //添加机构ID
        courseMarketNew.setId(courseId);
            //校验收费规则:课程收费时（） 价格不能为空

        Integer insert1 = saveCourseMarket(courseMarketNew);

        if (insert<=0||insert1<=0){
            XueChengException.err("新增课程基本信息失败");
        }

        //添加成功，返回课程基本信息
        return getCourseBaseInfo(courseId);

    }

    //根据课程id查询课程基本信息，包括基本信息和营销信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId){
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        if (courseBase==null){
            return null;
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);

        if (courseMarket==null){
            return null;
        }
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);

        //查询分类名称
        CourseCategory courseCategoryBySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategoryBySt.getName());
        CourseCategory courseCategoryByMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryByMt.getName());

        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto) {
        Long id = dto.getId();
        CourseBase courseBaseUpdate = courseBaseMapper.selectById(id);
        if (courseBaseUpdate==null){
            XueChengException.err("课程不能为空");
        }
        if (!companyId.equals(courseBaseUpdate.getCompanyId())){
            XueChengException.err("只允许修改本机构的课程");
        }

        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(dto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        //更新基本信息
        courseBaseMapper.updateById(courseBase);

        //封装营销数据到CourseMarker对象
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        //收费金额校验，选收费金额不能为空或小于等于0
        //更新营销信息
        saveCourseMarket(courseMarket);

        CourseBaseInfoDto courseBaseInfo = this.getCourseBaseInfo(id);
        return courseBaseInfo;

    }
    @Transactional
    @Override
    public void delectCourse(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!companyId.equals(courseBase.getCompanyId()))
            XueChengException.err("只允许删除本机构的课程");
        // 删除课程教师信息
        LambdaQueryWrapper<CourseTeacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(teacherLambdaQueryWrapper);
        // 删除课程计划
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(teachplanLambdaQueryWrapper);
        // 删除营销信息
        courseMarketMapper.deleteById(courseId);
        // 删除课程基本信息
        courseBaseMapper.deleteById(courseId);
    }

    private Integer saveCourseMarket(CourseMarket courseMarket) {
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            XueChengException.err("请设置收费规则");
        }
        if ("201001".equals(charge)){

            if (courseMarket.getPrice().floatValue()<=0||courseMarket.getPrice()==null){

                XueChengException.err("课程设置了收费价格不能为空且必须大于0");
            }
        }
        //没有则新增，有原数据则修改
        boolean b = courseMarketService.saveOrUpdate(courseMarket);
        return b?1:0;
    }
}

package com.xuecheng.content.service.Impl;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto queryCourseParams) {
        Page<CourseBase> page = new Page<>();
        page.setSize(pageParams.getPageSize());
        page.setPages(pageParams.getPageNo());

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
}

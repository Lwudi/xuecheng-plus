package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-02-28 09:35
 **/

public interface CourseBaseInfoService {
    /**
    * @description 课程查询接口
    * @param pageParams 分页参数
     * @param queryCourseParams 查询条件
    * @return com.xuecheng.base.model.PageResult<com.xuecheng.content.model.po.CourseBase>
    * @author 31151
    * @date 2023/2/28 9:49
    */
    PageResult<CourseBase> queryCourseBaseList(PageParams pageParams,QueryCourseParamsDto queryCourseParams);
    /**
    * @description 新增课程接口
    * @param companyId 教学机构ID
     * @param dto 课程基本信息
    * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
    * @author 31151
    * @date 2023/3/1 18:46
    */
    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto);
    /**
    * @description 按ID查询课程基本信息
    * @param courseId
    * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
    * @author 31151
    * @date 2023/3/1 19:09
    */
    CourseBaseInfoDto getCourseBaseInfo(long courseId);
    /**
    * @description 按ID修改课程信息
    * @param companyId
     * @param dto
    * @return com.xuecheng.content.model.dto.CourseBaseInfoDto
    * @author 31151
    * @date 2023/3/3 15:25
    */
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto dto);

    void delectCourse(Long companyId, Long courseId);
}

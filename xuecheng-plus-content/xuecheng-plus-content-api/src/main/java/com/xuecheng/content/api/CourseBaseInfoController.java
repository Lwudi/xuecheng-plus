package com.xuecheng.content.api;

import com.xuecheng.base.exception.ValidationGroups;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.spi.service.contexts.SecurityContext;


/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-02-27 13:12
 **/
@RestController
@Api(value = "课程内容基本数据接口", tags = "课程内容基本数据接口。")
public class CourseBaseInfoController {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody QueryCourseParamsDto queryCourseParams){
        PageResult<CourseBase> courseBasePageResult = courseBaseInfoService.queryCourseBaseList(pageParams, queryCourseParams);
        return courseBasePageResult;
    }

    @ApiOperation("课程新增接口")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody @Validated AddCourseDto addCourseDto){
        Long companyId = 22L;

        return courseBaseInfoService.createCourseBase(companyId,addCourseDto);
    }


    @GetMapping("/course/{courseId}")
    @ApiOperation("课程数据回显（按ID查询数据）")
    public CourseBaseInfoDto getCourseBaseById(@PathVariable long courseId){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("================================================================================================="+principal);
        return courseBaseInfoService.getCourseBaseInfo(courseId);
    }

    @ApiOperation("修改课程基础信息")
    @PutMapping("/course")
    public CourseBaseInfoDto modifyCourseBase(@RequestBody @Validated EditCourseDto editCourseDto){
        Long companyId = 22L;
        return courseBaseInfoService.updateCourseBase(companyId,editCourseDto);
    }
    @ApiOperation("删除课程")
    @DeleteMapping("/course/{courseId}")
    public void deleteCourse(@PathVariable Long courseId) {
        Long companyId = 22L;
        courseBaseInfoService.delectCourse(companyId,courseId);
    }

}

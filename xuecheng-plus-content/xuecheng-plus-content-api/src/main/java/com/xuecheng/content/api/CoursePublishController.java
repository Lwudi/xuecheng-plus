package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.service.CoursePublishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-03-13 20:00
 **/
@Api(value = "课程预览发布接口",tags = "课程预览发布接口")
@RestController
public class CoursePublishController {
    @Autowired
    CoursePublishService coursePublishService;
    @ApiOperation("课程预览")
    @GetMapping("/coursepreview/{courseId}")
    public ModelAndView preview(@PathVariable("courseId") long courseId){

        //获取课程预览信息
        CoursePreviewDto coursePreviewInfo = coursePublishService.getCoursePreviewInfo(courseId);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("model",coursePreviewInfo);
        modelAndView.setViewName("course_template_view");
        return modelAndView;
    }

/**
* @description 课程提交
* @param courseId
* @return void
* @author 31151
* @date 2023/3/16 9:31
*/
    @ApiOperation("课程提交")
    @PostMapping("/courseaudit/commit/{courseId}")
    public void commitAudit(@PathVariable("courseId") Long courseId){
        Long companyId = 22L;
        coursePublishService.commitAudit(companyId,courseId);

    }
    /**
     * @description 课程预览，发布
     * @author Mr.M
     * @date 2022/9/16 14:48
     * @version 1.0
     */
        @ApiOperation("课程发布")
        @PostMapping ("/coursepublish/{courseId}")
        public void coursepublish(@PathVariable("courseId") Long courseId){
            Long companyId = 22L;
            coursePublishService.publish(companyId,courseId);

        }




    }


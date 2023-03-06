package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: xuecheng-plus-group1
 * @description: 数据字典 前端控制器
 * @author: lxw
 * @create: 2023-02-28 16:19
 **/
@Slf4j
@RestController
@Api(value = "课程类别相关接口",tags = "课程类别相关接口")
public class CourseCategoryController {

    @Autowired
    CourseCategoryService courseCategoryService;
    @GetMapping("/course-category/tree-nodes")
    @ApiOperation(value = "课程类别展示接口")
    public List<CourseCategoryTreeDto> queryTreeNode(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        return courseCategoryTreeDtos;
    }
}

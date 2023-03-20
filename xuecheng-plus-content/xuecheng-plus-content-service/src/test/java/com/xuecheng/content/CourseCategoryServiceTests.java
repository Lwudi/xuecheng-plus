package com.xuecheng.content;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;

import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ClassUtils;


import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.AbstractQueuedLongSynchronizer;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-03-01 09:45
 **/
@SpringBootTest
public class CourseCategoryServiceTests {
    @Autowired
    CourseCategoryService courseCategoryService;

    @Test
    void testqueryTreeNodes() {

        List<CourseCategoryTreeDto> categoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(categoryTreeDtos);
    }

    @Test
    void test(){


        boolean assignable = ClassUtils.isAssignable(Float.class, BigDecimal.class);
        System.out.println(assignable);
    }





}

package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
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
}

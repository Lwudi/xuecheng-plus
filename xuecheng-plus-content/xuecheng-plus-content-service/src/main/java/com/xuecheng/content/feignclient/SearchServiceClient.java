package com.xuecheng.content.feignclient;

import com.xuecheng.content.feignclient.po.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @program:  搜索服务远程接口
 * @description:
 * @author: lxw
 * @create: 2023-03-19 11:35
 **/
@FeignClient(value = "search",fallbackFactory = SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient {
    @PostMapping("/search/index/course")
    public Boolean add(@RequestBody CourseIndex courseIndex);

}

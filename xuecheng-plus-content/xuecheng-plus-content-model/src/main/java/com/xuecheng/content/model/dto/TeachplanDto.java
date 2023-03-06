package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-03-04 15:30
 **/
@Data
@ToString
public class TeachplanDto extends Teachplan {
    //课程计划相关的媒资信息
    TeachplanMedia teachplanMedia;

    //子节点
    List<TeachplanDto> teachPlanTreeNodes;

}

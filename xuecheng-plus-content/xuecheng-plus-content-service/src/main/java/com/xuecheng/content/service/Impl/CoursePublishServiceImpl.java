package com.xuecheng.content.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: xuecheng-plus-group1
 * @description:
 * @author: lxw
 * @create: 2023-03-14 20:18
 **/
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService {
    @Autowired
    CourseBaseInfoService courseBaseInfoService;

    @Autowired
    TeachplanService teachplanService;

    @Autowired
    CourseTeacherService courseTeacherService;

    @Autowired
    CourseBaseMapper courseBaseMapper;
    @Autowired
    CourseMarketMapper courseMarketMapper;
    @Autowired
    CourseTeacherMapper courseTeacherMapper;
    @Autowired
    CoursePublishPreMapper coursePublishPreMapper;
    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Autowired
    CoursePublishMapper coursePublishMapper;
    @Autowired
    MqMessageService mqMessageService;
    @Autowired
    MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId) {
        //课程基本信息、营销信息
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        //课程计划信息
        List<TeachplanDto> teachplayTree = teachplanService.findTeachplayTree(courseId);

        //获取师资信息
        List<CourseTeacher> courseTeacherList = courseTeacherService.getCourseTeacherList(courseId);

        CoursePreviewDto coursePreviewDto= new CoursePreviewDto();
        coursePreviewDto.setCourseTeacherList(courseTeacherList);
        coursePreviewDto.setCourseBase(courseBaseInfo);
        coursePreviewDto.setTeachplans(teachplayTree);
        return coursePreviewDto;
    }
@Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId) {
        //约束校验
    CourseBaseInfoDto courseBase = courseBaseInfoService.getCourseBaseInfo(courseId);
    //课程审核状态
        String auditStatus = courseBase.getAuditStatus();
        //当前审核状态为已提交不允许再次提交
        if ("202003".equals(auditStatus)){
            XueChengException.err("当前审核状态为已提交，不可重复提交");
        }
        //本机构只允许提交本机构的课程
        Long companyId1 = courseBase.getCompanyId();
        if (!companyId1.equals(companyId)){
            XueChengException.err("不能提交非本公司的课程。");
        }
        //课程图片是否填写
        if (StringUtils.isBlank(courseBase.getPic())){
            XueChengException.err("提交失败，请上传课程图片");
        }
        //添加课程预发布记录
        CoursePublishPre coursePublishPre = new CoursePublishPre();
        //课程基本信息加部分营销信息
        BeanUtils.copyProperties(courseBase,coursePublishPre);
/*        //添加mtName
        String mtId = courseBase.getMt();
        LambdaQueryWrapper<CourseCategory>mtWrapper = new LambdaQueryWrapper<>();
        mtWrapper.eq(CourseCategory::getId,mtId);
        CourseCategory courseCategory = courseCategoryMapper.selectOne(mtWrapper);
        String mtName = courseCategory.getName();
        coursePublishPre.setMtName(mtName);
        //添加stName
        String stId = courseBase.getSt();
        LambdaQueryWrapper<CourseCategory>stWrapper=new LambdaQueryWrapper<>();
        stWrapper.eq(CourseCategory::getId,stId);
        CourseCategory courseCategory2=courseCategoryMapper.selectOne(stWrapper);
        String stName = courseCategory2.getName();
        coursePublishPre.setStName(stName);*/

        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //转为json
        String courseMarketJson = JSON.toJSONString(courseMarket);
        //将课程营销信息json数据放入课程预发布表
        coursePublishPre.setMarket(courseMarketJson);
        //查询课程计划信息
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplayTree(courseId);
        //转json
        String teachPlanTreeJson = JSON.toJSONString(teachplanTree);
        coursePublishPre.setTeachplan(teachPlanTreeJson);
        //查询师资信息
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getCourseId,courseId);
        List<CourseTeacher> courseTeacherList = courseTeacherService.getCourseTeacherList(courseId);
        String courseTeachersJson = JSON.toJSONString(courseTeacherList);
        coursePublishPre.setTeachers(courseTeachersJson);
        // 设置预发布记录状态,已提交
        coursePublishPre.setStatus("202003");
        //教学机构id
        coursePublishPre.setCompanyId(companyId);
        //提交时间
        coursePublishPre.setCreateDate(LocalDateTime.now());

        CoursePublishPre coursePublishPreUpdate = coursePublishPreMapper.selectById(courseId);
        //添加课程预发布记录
        if (coursePublishPreUpdate==null){
            int insert = coursePublishPreMapper.insert(coursePublishPre);
            if (insert<=0){
                XueChengException.err("新增课程提交失败");
            }
        }else {
            int i = coursePublishPreMapper.updateById(coursePublishPre);
            if(i<=0){
                XueChengException.err("更新课程提交失败");
            }
        }


        //更新课程基本表的审核状态
        courseBase.setAuditStatus("202003");
        courseBaseMapper.updateById(courseBase);

    }
    @Transactional
    @Override
    public void publish(Long companyId, Long courseId) {
        //约束校验
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre==null){
            XueChengException.err("请先提交审核，审核通过才能发布");
        }
        //本机构只允许提交本机构的课程
        if (!coursePublishPre.getCompanyId().equals(companyId)) {
            XueChengException.err("只允许发布本机构的课程");
        }
        //课程审核状态
        String auditStatus = coursePublishPre.getStatus();
        //审核通过才可以发布
        if (!"202004".equals(auditStatus)){
            XueChengException.err("操作失败，课程审核通过方可发布。");
        }

        //保存课程发布信息
        saveCoursePublish(courseId);

        //保存消息表
        saveCoursePublishMessage(courseId);

        //删除课程预发布表对应记录
        coursePublishPreMapper.deleteById(courseId);
        //修改课程基本信息为已发布
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);

    }

    @Override
    public File generateCourseHtml(Long courseId) {

        //静态化文件
        File htmlFile  = null;

        try {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template_view.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);
            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course",".html");
            log.debug("课程静态化，生成静态文件:{}",htmlFile.getAbsolutePath());

            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            log.error("课程静态化异常:{}",e.toString());
            XueChengException.err("课程静态化异常");

        }
        return htmlFile;

    }

    @Override
    public void uploadCourseHtml(Long courseId, File file) {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.uploadFile(multipartFile, "course", courseId+".html");
        if(course == null) {
            XueChengException.err("远程调用媒资服务上传文件失败");
        }


        }

    private void saveCoursePublishMessage(Long courseId) {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage==null){
            XueChengException.err(CommonError.UNKOWN_ERROR);
        }

    }

    /**
    * @description 保存课程发布信息
    * @param courseId  课程id
    * @return void
    * @author 31151
    * @date 2023/3/16 9:50
    */
    private void saveCoursePublish(Long courseId) {

        //整合课程发布信息
        //查询课程预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if(coursePublishPre == null){
            XueChengException.err("课程预发布数据为空");
        }

        CoursePublish coursePublish = new CoursePublish();
        //拷贝到课程发布对象
        BeanUtils.copyProperties(coursePublishPre,coursePublish);
        coursePublish.setStatus("203002");

        CoursePublish coursePublishUpdate = coursePublishMapper.selectById(courseId);
        if (coursePublishUpdate==null){
            //新增
            coursePublishMapper.insert(coursePublish);
        }else {
            //更新
            coursePublishMapper.updateById(coursePublish);
        }

        //更新课程信息基本表的发布状态
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setStatus("203002");
        courseBaseMapper.updateById(courseBase);


    }
}

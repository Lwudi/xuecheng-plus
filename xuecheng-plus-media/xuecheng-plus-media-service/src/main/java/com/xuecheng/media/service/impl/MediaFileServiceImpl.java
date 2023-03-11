package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @description TODO
 * @author Mr.M
 * @date 2022/9/10 8:58
 * @version 1.0
 */
@Slf4j
 @Service
public class MediaFileServiceImpl implements MediaFileService {

  @Autowired
 MediaFilesMapper mediaFilesMapper;
  @Autowired
 MinioClient minioClient;

  @Autowired
  MediaFileService mediaFileService;

 //普通文件桶
 @Value("${minio.bucket.files}")
 private String bucket_Files;
 //视频文件桶
 @Value("${minio.bucket.videofiles}")
 private String bucket_VideoFiles;




 @Override
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

  //构建查询条件对象
  LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
  String filename = queryMediaParamsDto.getFilename();
  String fileType = queryMediaParamsDto.getFileType();
  queryWrapper.like(StringUtils.isNotEmpty(filename),MediaFiles::getFilename,filename);
  queryWrapper.eq(StringUtils.isNotEmpty(fileType),MediaFiles::getFileType,fileType);


  //分页对象
  Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
  // 查询数据内容获得结果
  Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
  // 获取数据列表
  List<MediaFiles> list = pageResult.getRecords();
  // 获取数据总数
  long total = pageResult.getTotal();
  // 构建结果集
  PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
  return mediaListResult;

 }

 @Override
 public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {

  //生成文件id，文件的md5值
   String fileId = DigestUtils.md5Hex(bytes);
  //文件名称
  String filename = uploadFileParamsDto.getFilename();
  //构造objectname
  if (StringUtils.isEmpty(objectName)){
   objectName=fileId+filename.substring(filename.lastIndexOf("."));
  }
  //如果存储路径为空，则默认年/月/日/目录下
  if (StringUtils.isEmpty(folder)){
   folder = getFileFolder(new Date(), true, true, true);
  }else if (folder.indexOf("/") < 0) {
   //路径中没有”/“则在结尾自动添加
   folder = folder + "/";
  }
  //对象名称
  objectName = folder + objectName;





  try {
   addMediaFilesToMinIO(bytes,bucket_Files,objectName);
   MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileId, uploadFileParamsDto, bucket_Files, objectName);

   UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
    BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);
    return uploadFileResultDto;
   }
   catch (Exception e) {
   XueChengException.err("上传过程中出错");
  }
  return null;
 }

 @Transactional
 public MediaFiles addMediaFilesToDb(Long companyId,String fileId,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName) {
  //文件信息存入数据库
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
  if (mediaFiles == null) {
   mediaFiles = new MediaFiles();
   //拷贝基本信息
   BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
   mediaFiles.setId(fileId);
   mediaFiles.setFileId(fileId);
   mediaFiles.setCompanyId(companyId);
   mediaFiles.setUrl("/" + bucket + "/" + objectName);
   mediaFiles.setFilePath(objectName);
   mediaFiles.setBucket(bucket);
   mediaFiles.setCreateDate(LocalDateTime.now());
   mediaFiles.setAuditStatus("002003");
   mediaFiles.setStatus("1");
   //保存文件信息到文件表
   int insert = mediaFilesMapper.insert(mediaFiles);
   if (insert < 0) {
    XueChengException.err("保存文件信息失败");
   }
  }
  return mediaFiles;
 }

 @Override
 public RestResponse<Boolean> checkFile(String fileMd5) {
  MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
  // 数据库中不存在，则直接返回false 表示不存在
  if (mediaFiles==null){
   return RestResponse.success(false);
  }
  // 若数据库中存在，根据数据库中的文件信息，则继续判断bucket中是否存在

  String bucket = null;
  try {
   bucket = mediaFiles.getBucket();
   InputStream inputStream= minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(mediaFiles.getFilePath()).build());
   if (inputStream==null){
    //视频文件不存在
    return RestResponse.success(false);
   }
  } catch (Exception e) {
   return RestResponse.success(false);
  }
//存在
  return RestResponse.success(true);
 }

 @Override
 public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {

  String chunkFileFolderPath= getChunkFileFolderPath(fileMd5);
  String chunkFilePath = chunkFileFolderPath + chunkIndex;

  try{
   InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucket_Files).object(chunkFilePath).build());
   if (inputStream==null){
    //分块不存在
    return RestResponse.success(false);
   }
  }catch (Exception e){
    return RestResponse.success(false);
  }
  //分块已存在
  return RestResponse.success(true);

 }

 @Override
 public RestResponse uploadChunk(String fileMd5, int chunk, byte[] bytes) {
  //得到分块文件的目录路径
  String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
  //分块文件路径
  String chunkFilePath=chunkFileFolderPath+chunk;

  try{
   //将文件存储至minIO
  addMediaFilesToMinIO(bytes,bucket_VideoFiles,chunkFilePath);
  return RestResponse.success(true);
  }catch (Exception e){
    e.printStackTrace();
    log.debug("上传分块文件:{},失败:{}",chunkFilePath,e.getMessage());
  }
  return RestResponse.validfail(false,"上传分块失败");
 }

 @Override
 public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto)  {
  // 分块文件所在目录
  String chunkFileFolderPath=getChunkFileFolderPath(fileMd5);
  // 获得合并后文件地址
  String filename = uploadFileParamsDto.getFilename();
  String fileExt = filename.substring(filename.lastIndexOf("0"));
  String objectName =getFilePath(fileMd5,fileExt);

  //找到所有的分块文件
  List<ComposeSource> sources= Stream.iterate(0,i->++i).limit(chunkTotal).
          map(i->ComposeSource.builder().bucket(bucket_VideoFiles)
                  .object(chunkFileFolderPath+i)
                  .build()).collect(Collectors.toList());

  ComposeObjectArgs build = ComposeObjectArgs.builder().sources(sources).bucket(bucket_VideoFiles).object(objectName).build();
  //1.合并文件
  try {
   minioClient.composeObject(build);
  } catch (Exception e) {
   e.printStackTrace();
   log.error("合并文件出错，bucket:{},objectName:{},错误信息:{}",bucket_VideoFiles,objectName,e.getMessage());
   RestResponse.validfail(false,"合并文件异常");
  }

  //2.校验合并后的文件和源文件是否一致，视频上传才成功
  File file = downloadFileFromMinIO(bucket_VideoFiles, objectName);
  //计算文件MD5并校验

  try( FileInputStream fileInputStream = new FileInputStream(file)) {
   String md5Hex = DigestUtils.md5Hex(fileInputStream);
   if (!fileMd5.equals(md5Hex)){
    log.error("校验MD5值不通过，原始文件：{},合并文件：{}",fileMd5,md5Hex);
    return RestResponse.validfail(false,"文件校验失败");
   }
   uploadFileParamsDto.setFileSize(file.length());
  } catch (Exception e) {
   e.printStackTrace();
   return RestResponse.validfail(false,"文件校验失败");
  }

  //3.将文件信息入库
  MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_VideoFiles, objectName);
  if (mediaFiles==null){
   return RestResponse.validfail(false,"文件入库失败");
  }

  //4.清理分块文件
  clearChunkFiles(chunkFileFolderPath,chunkTotal);
  return RestResponse.success(true);
 }
private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal)  {

 List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i).limit(chunkTotal).map(i -> new DeleteObject(chunkFileFolderPath.concat(Integer.toString(i)))).collect(Collectors.toList());
 RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_VideoFiles).objects(deleteObjects).build();
 Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
 //真正删除
 for (Result<DeleteError> result : results) {
      try {
       result.get();
      } catch (Exception e) {
       e.printStackTrace();
      }
 }
}

 private File downloadFileFromMinIO(String bucket,String objectName){
   //临时文件
  File minIOFile = null;
  FileOutputStream outputStream=null;
  try{
   InputStream object = minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());
  //创建临时文件
   minIOFile = File.createTempFile("minio", ".merge");
   outputStream=new FileOutputStream(minIOFile);
   IOUtils.copy(object,outputStream);
   return minIOFile;

  }catch (Exception e){
e.printStackTrace();
  }finally {
   if (outputStream!=null){
    try{
     outputStream.close();
    }catch (Exception e){
     e.printStackTrace();
    }
   }
  }
 return null;
 }

 private String getFilePath(String fileMd5,String fileExt) {
  return fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+fileExt;
 }


 @NotNull
 private String getChunkFileFolderPath(String fileMd5) {
  return fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunk/";
 }

 private void addMediaFilesToMinIO(byte[] bytes, String bucketFiles,String objectName) throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException, InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
  //转为流
  ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
  String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 默认content-type为未知二进制流

  String extension=null;
  if (objectName.indexOf(".")>=0) {//文件名中包含扩展名
   extension = objectName.substring(objectName.lastIndexOf("."));
  }

  if (StringUtils.isNotBlank(extension)) {
   ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
   if (extensionMatch != null) {//匹配到了content-type类型名
    contentType = extensionMatch.getMimeType();
   }
  }

  PutObjectArgs putObjectArgs = PutObjectArgs.builder().bucket(bucketFiles).object(objectName)
          //-1表示文件分片按5M(不小于5M,不大于5T),分片数量最大10000，
          .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
          .contentType(contentType)
          .build();

  minioClient.putObject(putObjectArgs);
 }

 private String getFileFolder(Date date, boolean year, boolean month, boolean day) {

  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

  //当前日期字符串
  String dateString = simpleDateFormat.format(date);
  //取出年、月、日各个字段
  String[] split = dateString.split("-");
  StringBuffer stringBuffer = new StringBuffer();
  if (year){
   stringBuffer.append(split[0]).append("/");
  }
  if (month){
   stringBuffer.append(split[1]).append("/");
  }
  if (day){
   stringBuffer.append(split[2]).append("/");
  }
  return stringBuffer.toString();
 }
}

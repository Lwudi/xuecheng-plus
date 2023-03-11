package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;

/**
 * @description 媒资文件管理业务类
 * @author Mr.M
 * @date 2022/9/10 8:55
 * @version 1.0
 */
public interface MediaFileService {

 /**
  * @description 媒资文件查询方法
  * @param pageParams 分页参数
  * @param queryMediaParamsDto 查询条件
  * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
  * @author Mr.M
  * @date 2022/9/10 8:57
 */
 public PageResult<MediaFiles> queryMediaFiels(Long companyId,PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);



/**
* @description 上传文件通用方法
* @param companyId 机构ID
 * @param uploadFileParamsDto 上传文件基本信息
 * @param bytes 文件本体字节码
 * @param folder 文件目录，如果不传则默认年/月/日
 * @param objectName 文件名称
* @return com.xuecheng.media.model.dto.UploadFileResultDto 上传文件结果
* @author 31151
* @date 2023/3/7 20:36
*/
        UploadFileResultDto uploadFile(Long companyId,
                                      UploadFileParamsDto uploadFileParamsDto,
                                       byte[] bytes,
                                       String folder,
                                       String objectName);

        /**
        * @description 文件信息添加到数据库
        * @param companyId 机构ID
         * @param fileId 文件ID
         * @param uploadFileParamsDto
         * @param bucket minio 桶名
         * @param objectName 全路径名
        * @return com.xuecheng.media.model.po.MediaFiles
        * @author 31151
        * @date 2023/3/9 17:53
        */
    public MediaFiles addMediaFilesToDb(Long companyId,String fileId,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName);



    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的md5
     * @return
     */
    RestResponse<Boolean> checkFile(String fileMd5);

    /**
     * 检查分块是否存在
     * @param fileMd5       文件的MD5
     * @param chunkIndex    分块序号
     * @return
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);

    /**
     * 上传分块
     * @param fileMd5   文件MD5
     * @param chunk     分块序号
     * @param bytes     文件字节
     * @return
     */
    RestResponse uploadChunk(String fileMd5,int chunk,byte[] bytes);
    /**
     * @description 合并分块
     * @param companyId  机构id
     * @param fileMd5  文件md5
     * @param chunkTotal 分块总和
     * @param uploadFileParamsDto 文件信息
     * @return com.xuecheng.base.model.RestResponse
     */
    public RestResponse mergechunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);

}

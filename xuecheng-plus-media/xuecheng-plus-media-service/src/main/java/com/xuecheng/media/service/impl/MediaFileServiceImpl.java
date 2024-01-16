package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MediaFileService mediaFileService;

    // 存储普通文件
    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;
    // 存储视频
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

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
    public UploadFileResultDto uploadFile(Long companyId,
                          UploadFileParamsDto uploadFileParamsDto, String localFilePath) {
        // 文件名称
        String filename = uploadFileParamsDto.getFilename();
        // 文件扩展名
        String end = filename.substring(filename.lastIndexOf("."));
        // 文件类型
        String mimeType = this.getMimeType(end);
        // 得到子目录
        String defaultFolderPath = this.getDefaultFolderPath();
        // 文件的md5值
        String fileMd5 = this.getFileMd5(new File(localFilePath));
        // 对象名
        String objectName = defaultFolderPath + fileMd5 + end;
        // 将文件上传到minio
        boolean result = this.addMediaFilesToMinIO(localFilePath, mimeType, bucket_mediafiles, objectName);
        if (!result){
            XueChengPlusException.cast("上传文件失败");
        }
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_mediafiles, objectName);

        // 准备返回对象
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
        return uploadFileResultDto;
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String folder = sdf.format(new Date()).replace("-", "/") + "/";
        return folder;
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }finally {
            if (fileInputStream != null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * @param end 文件扩展名
     * @return 媒体类型
     */
    private String getMimeType(String end) {
        if (end == null) {
            end = "";
        }
        // 根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(end);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 通用mimeType，字节流
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }


    private boolean addMediaFilesToMinIO(String localFilePath,
                                         String mimeType, String bucket, String objectName) {
        // 上传文件的参数信息
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket) // 桶
                    .filename(localFilePath) // 指定本地文件路径
                    .object(objectName)   // 对象名
                    .contentType(mimeType)      // 设置文件类型
                    .build();
            // 上传文件
            minioClient.uploadObject(uploadObjectArgs);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @description 将文件信息添加到文件表
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param uploadFileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
     * @return com.xuecheng.media.model.po.MediaFiles
     */
    @Override
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName){
        //从数据库查询文件
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();
            //拷贝基本信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileMd5);
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setAuditStatus("002003");
            mediaFiles.setStatus("1");
            //保存文件信息到文件表
            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert < 0) {
                XueChengPlusException.cast("保存文件信息失败");
            }
        }
        return mediaFiles;
    }

    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        // 查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null){
            // 查询minio
            GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                    .bucket(mediaFiles.getBucket())
                    .object(mediaFiles.getFilePath())
                    .build();
            try {
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                if (inputStream != null){
                    // 文件已存在
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        //得到分块文件目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //得到分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunkIndex;
        // 查询minio
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket_video)
                .object(chunkFilePath)
                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
            if (inputStream != null){
                // 文件已存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath) {
        // 扩展名
        String mimeType = getMimeType(null);
        //分块文件的路径
        String chunkFilePath = this.getChunkFileFolderPath(fileMd5) + chunk;

        boolean b = addMediaFilesToMinIO(localChunkFilePath, mimeType, bucket_video, chunkFilePath);
        if (!b){
            return RestResponse.validfail(false,"上传分块失败");
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5,
                        int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //分块文件的路径
        String chunkFileFolderPath = getChunkFileFolderPath(fileMd5);
        // 拼接所有分块文件
        List<ComposeSource> sourceList = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket(bucket_video)
                    .object(chunkFileFolderPath + i)
                    .build();
            sourceList.add(composeSource);
        }
        // 源文件名
        String filename = uploadFileParamsDto.getFilename();
        // 扩展名
        String fileExt = filename.substring(filename.lastIndexOf("."));
        // 合并后文件的objectName
        String objectName = this.getFilePathByMd5(fileMd5, fileExt);
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video) // 桶
                .sources(sourceList)  //指定源文件
                .object(objectName)
                .build();

        // 合并文件
        try {
            ObjectWriteResponse objectWriteResponse = minioClient.composeObject(composeObjectArgs);
            System.out.println("合并完成！");
        } catch (Exception e) {
            e.printStackTrace();
            return RestResponse.validfail(false,"合并文件出错。");
        }
        // 下载合并后的文件
        File file = this.downloadFileFromMinIO(bucket_video, objectName);
        // 合并后文件的MD5
        String downloadFileMd5 = this.getFileMd5(file);
        if (!fileMd5.equals(downloadFileMd5)){
            return RestResponse.validfail(false,"文件校验失败。");
        }
        uploadFileParamsDto.setFileSize(file.length());
        // 文件信息存入数据库
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, objectName);
        if (mediaFiles == null){
            return RestResponse.validfail(false,"文件入库失败。");
        }
        // 清理分块文件
        this.clearChunkFiles(chunkFileFolderPath,chunkTotal);
        return RestResponse.success(true);
    }

    //得到分块文件的目录
    private String getChunkFileFolderPath(String fileMd5) {
        return fileMd5.substring(0, 1) + "/"
                + fileMd5.substring(1, 2) + "/"
                + fileMd5 + "/" + "chunk" + "/";
    }

    /**
     * 得到合并后的文件的地址
     * @param fileMd5 文件id即md5值
     * @param fileExt 文件扩展名
     * @return
     */
    private String getFilePathByMd5(String fileMd5,String fileExt){
        return   fileMd5.substring(0,1) + "/"
                + fileMd5.substring(1,2) + "/"
                + fileMd5 + "/" +fileMd5 +fileExt;
    }

    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 清除分块文件
     * @param chunkFileFolderPath 分块文件路径
     * @param chunkTotal 分块文件总数
     */
    private void clearChunkFiles(String chunkFileFolderPath,int chunkTotal) {
        List<DeleteObject> deleteObjects = new ArrayList<>();
        for (int i = 0; i < chunkTotal; i++) {
            DeleteObject deleteObject = new DeleteObject(chunkFileFolderPath + i);
            deleteObjects.add(deleteObject);
        }
        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder()
                .bucket(bucket_video)
                .objects(deleteObjects)
                .build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
        // 真正的删除
        for (Result<DeleteError> result : results) {
            try {
                DeleteError deleteError = result.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

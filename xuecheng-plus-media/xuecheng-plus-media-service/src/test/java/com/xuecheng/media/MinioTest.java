package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class MinioTest {

    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.30.100:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();
    @Test
    public void testUpload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }

        // 上传文件的参数信息
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket") // 桶
                .filename("C:\\Users\\ZSH\\Desktop\\biZhi.jpg") // 指定本地文件路径
                .object("biZhi.jpg")   // 对象名
                .contentType(mimeType)      // 设置文件类型
                .build();
        // 上传文件
        minioClient.uploadObject(uploadObjectArgs);
    }
    @Test
    public void testDelete() throws Exception {
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("testbucket")
                .object("biZhi.jpg")
                .build();
        minioClient.removeObject(removeObjectArgs);
    }

    @Test
    public void testGetFile() throws Exception{
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testbucket")
                .object("biZhi.jpg")
                .build();

        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\ZSH\\Desktop\\biZhi1.jpg"));
        IOUtils.copy(inputStream,outputStream);
        //校验文件的完整性对文件的内容进行md5
        String source_md5 = DigestUtils.md5Hex(inputStream);
        String local_md5 = DigestUtils.md5Hex(
                new FileInputStream("C:\\Users\\ZSH\\Desktop\\biZhi1.jpg")
        );
        if(source_md5.equals(local_md5)){
            System.out.println("下载成功");
        }
    }

    @Test
    public void uploadBigFile() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        File file = new File("D:\\Tools\\bing_test");
        File[] files = file.listFiles();

        // 根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }
        for (File file1 : files) {
            // 上传文件的参数信息
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket") // 桶
                    .filename(file1.getAbsolutePath()) // 指定本地文件路径
                    .object("chunk/" + file1.getName())   // 对象名
                    .build();
            // 上传文件
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传分块 " + file1.getName() + "完成。");
        }
    }

    @Test
    public void mergeBig() throws Exception{
        List<ComposeSource> sourceList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket("testbucket")
                    .object("chunk/" + i)
                    .build();
            sourceList.add(composeSource);
        }

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket") // 桶
                .sources(sourceList)  //指定源文件
                .object("mergePic.jpg")
                .build();

        // 合并文件
        minioClient.composeObject(composeObjectArgs);
        System.out.println("合并完成！");
    }
}

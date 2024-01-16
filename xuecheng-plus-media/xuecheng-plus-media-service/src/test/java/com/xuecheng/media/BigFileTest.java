package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BigFileTest {
    // 分块测试
    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("D:\\Tools\\biZhi.jpg");
        String chunkPath = "D:/Tools/bing_test/";
        File file = new File(chunkPath );
        if(!file.exists()){
            file.mkdirs();
        }
        File chunkFolder = new File(chunkPath);
        long chunkSize = 1024 * 1024 * 5; //5M
        //分块数量
        long chunkNum = (long) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        System.out.println("分块总数："+chunkNum);
        //缓冲区大小
        byte[] bytes = new byte[1024];
        //使用RandomAccessFile访问文件
        RandomAccessFile raf_read = new RandomAccessFile(sourceFile, "r");
        for (int i = 0; i < chunkNum; i++) {
            // 创建分块文件
            file = new File(chunkPath + i);
            // 写入文件
            RandomAccessFile raf_write = new RandomAccessFile(file, "rw");
            int len = -1;
            while ((len = raf_read.read(bytes)) != -1) {
                raf_write.write(bytes, 0, len);
                if (file.length() >= chunkSize) {
                    break;
                }
            }
            raf_write.close();
        }
        raf_read.close();
        System.out.println("完成分块");
    }

    //测试文件合并方法
    @Test
    public void testMerge() throws IOException {
        //块文件目录
        File chunkFolder = new File("D:/Tools/bing_test/");
        //原始文件
        File originalFile = new File("D:\\Tools\\biZhi.jpg");
        //合并文件
        File mergeFile = new File("D:\\Tools\\biZhi_1.jpg");

        //用于写文件
        RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
        //缓冲区
        byte[] bytes = new byte[1024];
        //分块列表
        File[] files = chunkFolder.listFiles();
        // 转成集合，便于排序
        List<File> fileList = Arrays.asList(files);
        // 从小到大排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });
        for (File file : fileList) {
            RandomAccessFile raf_read = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = raf_read.read(bytes)) != -1) {
                raf_write.write(bytes, 0, len);
            }
            raf_read.close();
        }
        raf_write.close();

        //校验文件
        try (
            FileInputStream fileInputStream = new FileInputStream(originalFile);
            FileInputStream mergeFileStream = new FileInputStream(mergeFile);
        ) {
            //取出原始文件的md5
            String originalMd5 = DigestUtils.md5Hex(fileInputStream);
            //取出合并文件的md5进行比较
            String mergeFileMd5 = DigestUtils.md5Hex(mergeFileStream);
            if (originalMd5.equals(mergeFileMd5)) {
                System.out.println("合并文件成功");
            } else {
                System.out.println("合并文件失败");
            }

        }
    }
}

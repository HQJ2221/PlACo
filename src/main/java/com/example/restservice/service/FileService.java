package com.example.restservice.service;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.*;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.OSSObject;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    private OSS ossClient;
    private String bucketName;
    private static final String UPLOAD_DIR = "uploads/"; // OSS 中的虚拟目录前缀

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    public FileService() {
    }

    @PostConstruct
    public void init() {
        // 从环境变量获取 OSS 配置
        String endpoint = "https://oss-cn-shenzhen.aliyuncs.com";
        String region = "cn-shenzhen";
        this.bucketName = "teedy-translation-files";

        try {
            // 初始化 OSS 客户端
            DefaultCredentialProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
            ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
            clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
            this.ossClient = OSSClientBuilder.create()
                    .endpoint(endpoint)
                    .credentialsProvider(credentialsProvider)
                    .region(region)
                    .build();
        } catch (Exception e) {
            logger.error("初始化 OSS 客户端失败", e);
            throw new RuntimeException("初始化 OSS 客户端失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件到 OSS
     * @param fileName 文件名（包含虚拟路径，如 uploads/xxx.txt）
     * @param content 文件内容字节数组
     * @throws IOException 如果上传失败
     */
    public void uploadFile(String fileName, byte[] content) throws IOException {
        try {
            String ossKey = UPLOAD_DIR + fileName;
            ossClient.putObject(bucketName, ossKey, new ByteArrayInputStream(content));
            logger.info("文件上传到 OSS 成功: {}", ossKey);
        } catch (OSSException | ClientException e) {
            logger.error("上传文件到 OSS 失败: {}", fileName, e);
            throw new IOException("上传文件到 OSS 失败: " + e.getMessage());
        }
    }

    /**
     * 从 OSS 获取文件字节数组
     * @param fileName 文件名（包含虚拟路径）
     * @return 文件内容的字节数组
     */
    public byte[] getBytesArrayByFileName(String fileName) {
        try {
            String ossKey = UPLOAD_DIR + fileName;
            OSSObject ossObject = ossClient.getObject(bucketName, ossKey);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = ossObject.getObjectContent().read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                return baos.toByteArray();
            } finally {
                ossObject.getObjectContent().close();
            }
        } catch (OSSException | ClientException | IOException e) {
            logger.error("读取 OSS 文件失败: {}", fileName, e);
            return null;
        }
    }

    /**
     * 获取文件的 Base64 编码
     * @param fileName 文件名
     * @return Base64 编码字符串
     */
    public String getBase64ByFileName(String fileName) {
        byte[] fileBytes = getBytesArrayByFileName(fileName);
        return fileBytes != null ? Base64.getEncoder().encodeToString(fileBytes) : null;
    }

    /**
     * 删除 OSS 中的文件
     * @param fileName 文件名（包含虚拟路径）
     * @throws IOException 如果删除失败
     */
    public void deleteFile(String fileName) throws IOException {
        try {
            String ossKey = UPLOAD_DIR + fileName;
            ossClient.deleteObject(bucketName, ossKey);
            logger.info("文件从 OSS 删除成功: {}", ossKey);
        } catch (OSSException | ClientException e) {
            logger.error("删除 OSS 文件失败: {}", fileName, e);
            throw new IOException("删除 OSS 文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取上传目录前缀
     * @return 虚拟目录前缀
     */
    public String getUploadDir() {
        return UPLOAD_DIR;
    }

    /**
     * 释放 OSS 客户端资源
     */
    public void shutdown() {
        if (ossClient != null) {
            ossClient.shutdown();
            logger.info("OSS 客户端已关闭");
        }
    }
}
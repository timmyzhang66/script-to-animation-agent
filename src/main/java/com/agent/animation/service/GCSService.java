package com.agent.animation.service;

import com.agent.animation.config.AppConfig;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Google Cloud Storage 服务类
 * 负责将生成的图片上传到 GCS，以便 Vertex AI API 访问
 */
public class GCSService {
    private static final Logger logger = LoggerFactory.getLogger(GCSService.class);
    
    private final Storage storage;
    private final String bucketName;

    public GCSService() {
        AppConfig config = AppConfig.getInstance();
        this.bucketName = config.getProperty("gcs.bucket.name", "my-animation-assets-123");
        
        // 默认使用 Application Default Credentials (ADC)
        // 在本地运行时，可以通过 `gcloud auth application-default login` 设置
        // 在 Google Cloud 环境中会自动获取权限
        this.storage = StorageOptions.getDefaultInstance().getService();
        
        logger.info("GCSService initialized with bucket: {}", bucketName);
    }

    /**
     * 上传本地文件到 GCS
     * 
     * @param localFilePath 本地文件路径
     * @param objectName GCS 中的对象名称（如果为 null 则自动生成）
     * @return GCS URI (gs://bucket/object)
     * @throws IOException 上传失败时抛出异常
     */
    public String uploadFile(String localFilePath, String objectName) throws IOException {
        if (objectName == null || objectName.isEmpty()) {
            String extension = "";
            int i = localFilePath.lastIndexOf('.');
            if (i > 0) {
                extension = localFilePath.substring(i);
            }
            objectName = "assets/" + UUID.randomUUID().toString() + extension;
        }

        logger.info("Uploading file to GCS: {} -> gs://{}/{}", localFilePath, bucketName, objectName);
        
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        
        storage.create(blobInfo, Files.readAllBytes(Paths.get(localFilePath)));
        
        String gcsUri = String.format("gs://%s/%s", bucketName, objectName);
        logger.info("File uploaded successfully to GCS: {}", gcsUri);
        
        return gcsUri;
    }

    /**
     * 上传字节数组到 GCS
     * 
     * @param data 字节数据
     * @param fileName 文件名
     * @return GCS URI (gs://bucket/object)
     */
    public String uploadBytes(byte[] data, String fileName) {
        String objectName = "assets/" + UUID.randomUUID().toString() + "_" + fileName;
        logger.info("Uploading bytes to GCS: gs://{}/{}", bucketName, objectName);
        
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        
        storage.create(blobInfo, data);
        
        String gcsUri = String.format("gs://%s/%s", bucketName, objectName);
        logger.info("Bytes uploaded successfully to GCS: {}", gcsUri);
        
        return gcsUri;
    }

    /**
     * 删除 GCS 中的文件
     * 
     * @param gcsUri GCS URI
     */
    public void deleteFile(String gcsUri) {
        try {
            if (gcsUri == null || !gcsUri.startsWith("gs://")) {
                return;
            }
            
            String path = gcsUri.substring(("gs://" + bucketName + "/").length());
            BlobId blobId = BlobId.of(bucketName, path);
            boolean deleted = storage.delete(blobId);
            
            if (deleted) {
                logger.info("Deleted GCS object: {}", gcsUri);
            } else {
                logger.warn("GCS object not found for deletion: {}", gcsUri);
            }
        } catch (Exception e) {
            logger.error("Failed to delete GCS object: {}", gcsUri, e);
        }
    }

    public void close() {
        // Storage 接口不需要显式关闭
        logger.info("GCSService closed");
    }
}

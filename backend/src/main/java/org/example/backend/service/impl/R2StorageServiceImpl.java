package org.example.backend.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.enums.UploadTypeEnum;
import org.example.backend.service.IFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

/**
 * Cloudflare R2 对象存储实现类
 */
@Slf4j
@Service
public class R2StorageServiceImpl implements IFileStorageService {

    @Autowired
    private S3Client s3Client;

    @Value("${r2.bucket-name}")
    private String bucketName;

    @Value("${r2.public-url}")
    private String publicUrl;

    @Override
    public String uploadFile(MultipartFile file, String type) {
        // 1. 校验文件非空
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }

        // 2. 根据业务枚举校验并获取存储目录前缀
        UploadTypeEnum uploadType = UploadTypeEnum.getByCode(type);

        // 3. 提取文件原始扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = "jpg";
        if (StrUtil.isNotBlank(originalFilename) && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        // 4. 生成唯一无重复的文件名（避免同名覆盖）
        String uniqueFileName = IdUtil.fastSimpleUUID() + "." + extension;
        String objectKey = uploadType.getPathPrefix() + uniqueFileName;

        // 5. 组装 S3 上传请求并推送到 Cloudflare R2
        try {
            String contentType = file.getContentType();
            if (StrUtil.isBlank(contentType)) {
                contentType = "application/octet-stream";
            }

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 6. 拼接并返回全球 CDN 直链
            String baseDomain = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
            String fileUrl = baseDomain + "/" + objectKey;
            log.info("文件上传至 Cloudflare R2 成功，业务类型: {}，直链: {}", uploadType.getDescription(), fileUrl);
            return fileUrl;
        } catch (IOException e) {
            log.error("读取上传文件流失败", e);
            throw new RuntimeException("读取文件流失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("Cloudflare R2 上传失败", e);
            throw new RuntimeException("云端存储上传失败: " + e.getMessage());
        }
    }
}


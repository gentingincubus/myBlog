package org.example.backend;

import org.example.backend.common.enums.UploadTypeEnum;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Cloudflare R2 对象存储独立连通性测试 (无需启动数据库与Redis，纯粹测试图床连通与直链生成)
 */
class R2StorageTest {

    @Test
    void testDirectUploadToR2() throws Exception {
        // 1. 读取本地保护的 application-local.properties
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application-local.properties")) {
            if (in == null) {
                System.err.println("❌ 未找到 application-local.properties 文件，请确认是否已创建！");
                return;
            }
            props.load(in);
        }

        String ak = props.getProperty("r2.access-key-id");
        String sk = props.getProperty("r2.secret-access-key");
        String endpoint = "https://5923ef4425a92d629e07cbb86b887ba1.r2.cloudflarestorage.com";
        String bucket = "myblog-vr";
        String publicUrl = "https://vr.gentingincubus.com";

        System.out.println("====== [1/3] 初始化 S3 客户端 ======");
        S3Client s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(ak, sk)
                ))
                .region(Region.of("auto"))
                .build();

        // 2. 模拟一张 1 像素的微型透明 PNG 图片数据
        byte[] tinyPngBytes = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D,
                0x49, 0x48, 0x44, 0x52, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x08,
                0x06, 0x00, 0x00, 0x00, 0x1F, 0x15, (byte) 0xC4, (byte) 0x89, 0x00, 0x00, 0x00,
                0x0A, 0x49, 0x44, 0x41, 0x54, 0x78, (byte) 0x9C, 0x63, 0x00, 0x01, 0x00, 0x00,
                0x05, 0x00, 0x01, 0x0D, 0x0A, 0x2D, (byte) 0xB4, 0x00, 0x00, 0x00, 0x00, 0x49,
                0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82
        };

        String objectKey = UploadTypeEnum.VR_PREVIEW.getPathPrefix() + "test_connection.png";
        System.out.println("====== [2/3] 正在上传测试图片到 R2: " + objectKey + " ======");

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType("image/png")
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(tinyPngBytes));

        // 3. 输出公网直链
        String fileUrl = publicUrl + "/" + objectKey;
        System.out.println("====================================================");
        System.out.println("🎉🎉 恭喜！Cloudflare R2 上传测试 100% 成功！");
        System.out.println("👉 请直接复制到浏览器打开验证: " + fileUrl);
        System.out.println("====================================================");
    }
}


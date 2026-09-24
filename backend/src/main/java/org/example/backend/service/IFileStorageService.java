package org.example.backend.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储统一接口
 */
public interface IFileStorageService {

    /**
     * 上传单个文件
     *
     * @param file 前端上传的二进制文件
     * @param type 业务类型（如 vr_panorama, vr_preview, vr_map, avatar 等）
     * @return 可公网直接访问的完整 CDN 图片直链 URL
     */
    String uploadFile(MultipartFile file, String type);
}


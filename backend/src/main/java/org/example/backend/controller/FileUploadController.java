package org.example.backend.controller;

import org.example.backend.dto.BasicResponse;
import org.example.backend.service.IFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 通用文件上传控制器
 */
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Autowired
    private IFileStorageService fileStorageService;

    /**
     * 单图片上传接口
     *
     * @param file 上传的文件
     * @param type 业务类型（支持: vr_panorama, vr_preview, vr_map, avatar, blog_image）
     * @return 文件的公开 CDN 直链 URL
     */
    @PostMapping("/image")
    public BasicResponse<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "blog_image") String type) {
        String url = fileStorageService.uploadFile(file, type);
        return BasicResponse.success(url);
    }
}

package org.example.backend.common.enums;

import lombok.Getter;

/**
 * 文件上传业务类型枚举（统一管理上传目录、格式与路由规范）
 */
@Getter
public enum UploadTypeEnum {

    /** VR 全景 360 巨幕高清大图 */
    VR_PANORAMA("vr_panorama", "vr/panoramas/", "VR全景大图"),

    /** VR 列表缩略预览图 */
    VR_PREVIEW("vr_preview", "vr/previews/", "VR场景缩略图"),

    /** VR 园区平面地图底图 */
    VR_MAP("vr_map", "vr/maps/", "VR地图底图"),

    /** 用户头像 */
    AVATAR("avatar", "user/avatars/", "用户头像"),

    /** 博客文章配图 */
    BLOG_IMAGE("blog_image", "blog/articles/", "博客文章图片");

    /** 前端传递的业务 code */
    private final String code;

    /** 存放在 R2 桶中的目录前缀 */
    private final String pathPrefix;

    /** 业务说明描述 */
    private final String description;

    UploadTypeEnum(String code, String pathPrefix, String description) {
        this.code = code;
        this.pathPrefix = pathPrefix;
        this.description = description;
    }

    /**
     * 根据 code 安全匹配枚举对象（大小写不敏感）
     * 如果传入未定义的类型，抛出友好业务异常
     */
    public static UploadTypeEnum getByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("上传类型 (type) 不能为空");
        }
        for (UploadTypeEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(code.trim())) {
                return item;
            }
        }
        throw new RuntimeException("不支持的文件上传业务类型: " + code);
    }
}


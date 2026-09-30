import request from './request'

/**
 * 通用图片上传 API（直连 Cloudflare R2 对象存储）
 * @param {File|Blob} file 图片文件
 * @param {'carousel_image'|'rich_text_image'|'vr_panorama'|'vr_preview'|'vr_map'|'avatar'|'blog_image'} type 业务类型
 */
export function uploadImageApi(file, type = 'blog_image') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)

  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export const uploadApi = {
  uploadImage: uploadImageApi
}

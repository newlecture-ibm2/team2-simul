import { apiClient } from './client';

/** 이미지 분석을 통한 태그 자동 추출 (Vision API 연동) */
export async function analyzeTags(imageFile: File) {
  const formData = new FormData();
  formData.append('image', imageFile);

  return apiClient('/tags/analyze', {
    method: 'POST',
    body: formData,
  });
}

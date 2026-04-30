import { apiClient } from './client';

export interface AnalyzeTagsResponse {
  recommended_tags: string[];
}

/** 이미지 분석을 통한 태그 자동 추출 (Vision API 연동) */
export async function analyzeTags(imageFile: File): Promise<AnalyzeTagsResponse> {
  const formData = new FormData();
  formData.append('image', imageFile);

  return apiClient<AnalyzeTagsResponse>('/tags/analyze', {
    method: 'POST',
    body: formData,
  });
}

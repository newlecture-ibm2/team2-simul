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

export interface TagResponse {
  tagId: string;
  name: string;
  category: string | null;
  usageCount: number;
}

/** 태그 자동완성 검색 */
export async function searchTags(query: string): Promise<TagResponse[]> {
  return apiClient<TagResponse[]>(`/tags/search?q=${encodeURIComponent(query)}`, {
    method: 'GET',
  });
}

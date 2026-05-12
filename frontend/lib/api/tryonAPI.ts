import { apiClient } from './client';

export type TryonGenerateResponse = {
  job_id: string;
  status: 'processing' | 'completed' | 'failed';
  estimated_seconds: number;
};

export type MyBaseImagesResponse = {
  base_images: Array<{
    base_image_id: string;
    image_url: string;
    created_at: string;
  }>;
};

/** 시착 생성 요청 (기술서/백엔드 스펙: POST /tryon/generate) */
export async function generateTryon(data: {
  base_image_id: string;
  item_id?: string;
  item_ids?: string[];
}): Promise<TryonGenerateResponse> {
  return apiClient('/tryon/generate', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/** 내 베이스 이미지 목록 조회 (GET /users/me/base-images) */
export async function getMyBaseImages(): Promise<MyBaseImagesResponse> {
  return apiClient('/users/me/base-images');
}

/** 시착 결과 조회 */
export async function getTryonResult(id: number) {
  return apiClient(`/tryon/${id}`);
}

/** 시착 이력 목록 */
export async function getTryonHistory() {
  return apiClient('/tryon/history');
}

/** 크레딧 잔액 조회 */
export async function getTryonCredits() {
  return apiClient('/tryon/credits');
}

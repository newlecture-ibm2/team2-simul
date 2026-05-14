import { apiClient } from './client';

export type TryonGenerateResponse = {
  job_id: string;
  status: 'processing' | 'completed' | 'failed';
  estimated_seconds: number;
};

export type TryonJobResponse = {
  job_id: string;
  status: 'processing' | 'completed' | 'failed';
  base_image_url?: string | null;
  result_image_url?: string | null;
};

export type MyBaseImagesResponse = {
  base_images: Array<{
    base_image_id: string;
    image_url: string;
    created_at: string;
  }>;
};

export type BaseImageUploadResponse = {
  base_image_id: string;
  image_url: string;
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

/** 시착 job 조회 (GET /tryon/jobs/{jobId}) */
export async function getTryonJob(jobId: string): Promise<TryonJobResponse> {
  return apiClient(`/tryon/jobs/${jobId}`);
}

/** 베이스 이미지 업로드 (POST /tryon/base-images) */
export async function uploadBaseImage(file: File): Promise<BaseImageUploadResponse> {
  const form = new FormData();
  form.append('image', file);
  return apiClient('/tryon/base-images', {
    method: 'POST',
    body: form,
  });
}

/** 내 베이스 이미지 목록 조회 (GET /users/me/base-images) */
export async function getMyBaseImages(): Promise<MyBaseImagesResponse> {
  return apiClient('/users/me/base-images');
}

/** 베이스 이미지 삭제 (DELETE /tryon/base-images/{baseImageId}) */
export async function deleteBaseImage(baseImageId: string): Promise<void> {
  return apiClient(`/tryon/base-images/${baseImageId}`, { method: 'DELETE' });
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
  return apiClient<{ remaining: number; total_daily: number; reset_at: string }>('/tryon/credits');
}

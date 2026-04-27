import { apiClient } from './client';

/** 시착 생성 요청 */
export async function createTryon(data: {
  baseImageId: number;
  clothImageId: number;
}) {
  return apiClient('/tryon', {
    method: 'POST',
    body: JSON.stringify(data),
  });
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

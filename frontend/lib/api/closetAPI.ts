import { apiClient } from './client';

/** 옷장 아이템 목록 조회 */
export async function getClosetItems(params?: {
  category?: string;
  sort?: string;
}) {
  return apiClient('/closet', { params: params as Record<string, string> });
}

/** 옷장 아이템 상세 조회 */
export async function getClosetItem(id: number) {
  return apiClient(`/closet/${id}`);
}

/** 아이템 추가 */
export async function addClosetItem(data: {
  imageUrl: string;
  category: string;
  memo?: string;
}) {
  return apiClient('/closet', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/** 아이템 수정 */
export async function updateClosetItem(
  id: number,
  data: { category?: string; memo?: string }
) {
  return apiClient(`/closet/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(data),
  });
}

/** 아이템 삭제 */
export async function deleteClosetItem(id: number) {
  return apiClient(`/closet/${id}`, { method: 'DELETE' });
}

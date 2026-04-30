import { apiClient } from './client';

// ==========================================
// Types
// ==========================================

export interface ClosetItemResponse {
  itemId: string;
  imageUrl: string;
  category: string | null;
  memo: string | null;
  tryCount: number;
  createdAt: string;
}

export interface ClosetItemListResponse {
  items: ClosetItemResponse[];
  hasNext: boolean;
  totalCount: number;
}

export interface GetClosetItemsParams {
  category?: string;
  sort?: string;
  page?: number;
  size?: number;
}

// ==========================================
// API Functions
// ==========================================

/** 옷장 아이템 목록 조회 */
export async function getClosetItems(params?: GetClosetItemsParams): Promise<ClosetItemListResponse> {
  const queryParams: Record<string, string> = {};

  if (params?.category) queryParams.category = params.category;
  if (params?.sort) queryParams.sort = params.sort;
  if (params?.page !== undefined) queryParams.page = String(params.page);
  if (params?.size !== undefined) queryParams.size = String(params.size);

  return apiClient<ClosetItemListResponse>('/closet/items', { params: queryParams });
}

/** 옷장 아이템 상세 조회 */
export async function getClosetItem(id: string) {
  return apiClient(`/closet/items/${id}`);
}

/** 아이템 추가 */
export async function addClosetItem(data: {
  imageUrl: string;
  category: string;
  memo?: string;
}) {
  return apiClient('/closet/items', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/** 아이템 수정 */
export async function updateClosetItem(
  id: string,
  data: { category?: string; memo?: string }
) {
  return apiClient(`/closet/items/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(data),
  });
}

/** 아이템 삭제 */
export async function deleteClosetItem(id: string) {
  return apiClient(`/closet/items/${id}`, { method: 'DELETE' });
}

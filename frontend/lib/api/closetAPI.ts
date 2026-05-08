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
export async function getClosetItem(id: string): Promise<ClosetItemResponse> {
  return apiClient<ClosetItemResponse>(`/closet/items/${id}`);
}

/** 아이템 추가 */
export async function addClosetItem(formData: FormData) {
  return apiClient('/closet/items', {
    method: 'POST',
    body: formData,
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

export interface ClosetCollectionResponse {
  collectionId: string;
  name: string;
  coverImageUrl: string | null;
  itemCount: number;
  createdAt: string;
}

export interface ClosetCollectionListResponse {
  collections: ClosetCollectionResponse[];
  hasNext: boolean;
  totalCount: number;
}

/** 컬렉션(폴더) 추가 */
export async function addClosetCollection(formData: FormData): Promise<string> {
  return apiClient<string>('/closet/collections', {
    method: 'POST',
    body: formData,
  });
}

/** 컬렉션(폴더) 목록 조회 */
export async function getClosetCollections(params?: { sort?: string; page?: number; size?: number }): Promise<ClosetCollectionListResponse> {
  const queryParams: Record<string, string> = {};
  if (params?.sort) queryParams.sort = params.sort;
  if (params?.page !== undefined) queryParams.page = String(params.page);
  if (params?.size !== undefined) queryParams.size = String(params.size);

  return apiClient<ClosetCollectionListResponse>('/closet/collections', { params: queryParams });
}

/** 컬렉션(폴더) 수정 */
export async function updateClosetCollection(id: string, formData: FormData) {
  return apiClient(`/closet/collections/${id}`, {
    method: 'PATCH',
    body: formData,
  });
}

/** 컬렉션(폴더) 삭제 */
export async function deleteClosetCollection(id: string) {
  return apiClient(`/closet/collections/${id}`, { method: 'DELETE' });
}

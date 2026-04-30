import { apiClient } from './client';

/** 피드 게시물 응답 타입 */
export interface FeedPost {
  postId: string;
  userId: string;
  nickname: string;
  profileImageUrl: string | null;
  imageUrl: string | null;
  tags: string[];
  caption: string;
  likeCount: number;
  isLiked: boolean;
  createdAt: string;
}

/** Spring Page 응답 타입 */
export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;       // 현재 페이지 (0-indexed)
  size: number;
  last: boolean;
  first: boolean;
}

/** 피드 목록 조회 */
export async function getFeedPosts(params?: {
  tab?: string;
  sort?: string;
  page?: number;
  size?: number;
}) {
  const queryParams: Record<string, string> = {};
  if (params?.tab) queryParams.tab = params.tab;
  if (params?.sort) queryParams.sort = params.sort;
  if (params?.page !== undefined) queryParams.page = String(params.page);
  if (params?.size !== undefined) queryParams.size = String(params.size);
  
  return apiClient<PageResponse<FeedPost>>('/posts', { params: queryParams });
}

/** 게시물 상세 조회 */
export async function getPostDetail(id: number) {
  return apiClient(`/posts/${id}`);
}

/** 게시물 작성 */
export async function createPost(data: FormData) {
  // 파일 업로드를 위해 FormData를 받도록 수정
  return apiClient('/posts', {
    method: 'POST',
    body: data,
  });
}

/** 좋아요 토글 */
export async function toggleLike(postId: number) {
  return apiClient(`/posts/${postId}/likes`, { method: 'POST' });
}

/** 댓글 작성 */
export async function createComment(postId: number, content: string) {
  return apiClient(`/posts/${postId}/comments`, {
    method: 'POST',
    body: JSON.stringify({ content }),
  });
}

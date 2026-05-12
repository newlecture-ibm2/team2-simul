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

export interface Comment {
  commentId: string;
  userId: string;
  nickname: string;
  profileImageUrl: string | null;
  content: string;
  depth: number;
  createdAt: string;
  isDeleted: boolean;
  replies: Comment[];
}

export interface LikeUser {
  userId: string;
  nickname: string;
  profileImageUrl: string | null;
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

/** 특정 유저의 게시물 목록 조회 */
export async function getUserPosts(userId: string, params?: {
  page?: number;
  size?: number;
}) {
  const queryParams: Record<string, string> = {};
  if (params?.page !== undefined) queryParams.page = String(params.page);
  if (params?.size !== undefined) queryParams.size = String(params.size);
  
  return apiClient<PageResponse<FeedPost>>(`/posts/users/${userId}`, { params: queryParams });
}

/** 내가 좋아요한 게시물 목록 조회 */
export async function getLikedPosts(params?: {
  page?: number;
  size?: number;
}) {
  const queryParams: Record<string, string> = {};
  if (params?.page !== undefined) queryParams.page = String(params.page);
  if (params?.size !== undefined) queryParams.size = String(params.size);
  
  return apiClient<PageResponse<FeedPost>>('/posts/liked', { params: queryParams });
}

/** 게시물 상세 조회 */
export async function getPostDetail(id: string) {
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

export async function toggleLike(postId: string) {
  return apiClient(`/posts/${postId}/likes`, { method: 'POST' });
}

/** 게시물 좋아요 누른 사용자 목록 조회 */
export async function getPostLikes(postId: string, page = 0, size = 20) {
  return apiClient<PageResponse<LikeUser>>(`/posts/${postId}/likes`, {
    params: { page: String(page), size: String(size) },
  });
}

/** 게시물 수정 */
export async function updatePost(postId: string, data: FormData) {
  return apiClient(`/posts/${postId}`, {
    method: 'PATCH',
    body: data,
  });
}

/** 게시물 삭제 */
export async function deletePost(postId: string) {
  return apiClient(`/posts/${postId}`, { method: 'DELETE' });
}

/** 댓글 목록 조회 */
export async function getComments(postId: string, page = 0, size = 20) {
  return apiClient<PageResponse<Comment>>(`/posts/${postId}/comments`, {
    params: { page: String(page), size: String(size) },
  });
}

/** 댓글 작성 */
export async function createComment(postId: string, content: string, parentCommentId?: string) {
  return apiClient<Comment>(`/posts/${postId}/comments`, {
    method: 'POST',
    body: JSON.stringify({ content, parentCommentId }),
  });
}

/** 댓글 삭제 */
export async function deleteComment(commentId: string) {
  return apiClient(`/comments/${commentId}`, { method: 'DELETE' });
}

/** 게시물 신고 */
export async function reportPost(postId: string, reason: string) {
  return apiClient(`/posts/${postId}/report`, {
    method: 'POST',
    body: JSON.stringify({ reason }),
  });
}

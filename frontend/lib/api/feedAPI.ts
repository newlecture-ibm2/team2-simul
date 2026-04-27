import { apiClient } from './client';

/** 피드 목록 조회 */
export async function getFeedPosts(params?: {
  tab?: string;
  sort?: string;
  page?: number;
}) {
  return apiClient('/feed', { params: params as Record<string, string> });
}

/** 게시물 상세 조회 */
export async function getPostDetail(id: number) {
  return apiClient(`/feed/${id}`);
}

/** 게시물 작성 */
export async function createPost(data: {
  imageUrl: string;
  caption: string;
  isPublic: boolean;
}) {
  return apiClient('/feed', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

/** 좋아요 토글 */
export async function toggleLike(postId: number) {
  return apiClient(`/feed/${postId}/like`, { method: 'POST' });
}

/** 댓글 작성 */
export async function createComment(postId: number, content: string) {
  return apiClient(`/feed/${postId}/comments`, {
    method: 'POST',
    body: JSON.stringify({ content }),
  });
}

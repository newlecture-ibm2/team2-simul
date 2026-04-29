import { apiClient } from './client';

/** 피드 목록 조회 */
export async function getFeedPosts(params?: {
  tab?: string;
  sort?: string;
  page?: number;
}) {
  return apiClient('/posts', { params: params as Record<string, string> });
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

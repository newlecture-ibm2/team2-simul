import { apiClient } from './client';
import { PageResponse } from './feedAPI';

export interface FeedPostResponse {
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

export type SearchType = 'tag' | 'caption' | 'all';

/**
 * 통합 피드 검색
 * @param query 검색어
 * @param type 검색 유형 (tag, caption, all)
 * @param page 페이지 번호
 * @param size 페이지당 개수
 */
export async function searchPosts(
  query: string,
  type: SearchType = 'all',
  page: number = 0,
  size: number = 20
): Promise<PageResponse<FeedPostResponse>> {
  const params = new URLSearchParams({
    q: query,
    type,
    page: page.toString(),
    size: size.toString(),
  });

  return apiClient<PageResponse<FeedPostResponse>>(`/search?${params.toString()}`, {
    method: 'GET',
  });
}

import { useState, useEffect, useCallback } from 'react';
import { getClosetItems, ClosetItemResponse, GetClosetItemsParams } from '@/lib/api/closetAPI';

interface UseClosetItemsReturn {
  items: ClosetItemResponse[];
  isLoading: boolean;
  error: string | null;
  totalCount: number;
  hasNext: boolean;
  refetch: () => void;
}

/**
 * 옷장 아이템 목록을 가져오는 커스텀 훅
 * - BFF → Spring Boot 백엔드 GET /closet/items 호출
 */
export function useClosetItems(params?: GetClosetItemsParams): UseClosetItemsReturn {
  const [items, setItems] = useState<ClosetItemResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalCount, setTotalCount] = useState(0);
  const [hasNext, setHasNext] = useState(false);

  const fetchItems = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await getClosetItems(params);
      setItems(response.items);
      setTotalCount(response.totalCount);
      setHasNext(response.hasNext);
    } catch (err) {
      console.error('[useClosetItems] API 호출 실패:', err);
      setError(err instanceof Error ? err.message : '아이템을 불러올 수 없습니다.');
      setItems([]);
    } finally {
      setIsLoading(false);
    }
  }, [params?.category, params?.sort, params?.page, params?.size]);

  useEffect(() => {
    fetchItems();
  }, [fetchItems]);

  return { items, isLoading, error, totalCount, hasNext, refetch: fetchItems };
}

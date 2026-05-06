import { useQuery } from '@tanstack/react-query';
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
 * 옷장 아이템 목록을 가져오는 커스텀 훅 (React Query 기반)
 * - queryClient.invalidateQueries({ queryKey: ['closetItems'] }) 호출 시 자동 갱신됨
 */
export function useClosetItems(params?: GetClosetItemsParams): UseClosetItemsReturn {
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['closetItems', params],
    queryFn: () => getClosetItems(params),
  });

  return {
    items: data?.items || [],
    isLoading,
    error: error instanceof Error ? error.message : (error ? '아이템을 불러올 수 없습니다.' : null),
    totalCount: data?.totalCount || 0,
    hasNext: data?.hasNext || false,
    refetch,
  };
}

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminAPI } from '@/lib/api/adminAPI';
import { toast } from '@/lib/utils/toast';

export function useReportManagement() {
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['admin-reports'],
    queryFn: () => adminAPI.getReports(0, 50),
  });

  const blindMutation = useMutation({
    mutationFn: (postId: string) => adminAPI.blindPost(postId),
    onSuccess: () => {
      toast.success('게시물이 블라인드 처리되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['admin-reports'] });
    },
    onError: () => {
      toast.error('처리에 실패했습니다.');
    }
  });

  const unblindMutation = useMutation({
    mutationFn: (postId: string) => adminAPI.unblindPost(postId),
    onSuccess: () => {
      toast.success('게시물 블라인드가 해제되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['admin-reports'] });
    },
    onError: () => {
      toast.error('처리에 실패했습니다.');
    }
  });
  
  const suspendUserMutation = useMutation({
    mutationFn: (userId: string) => adminAPI.suspendUser(userId),
    onSuccess: () => {
      toast.success('유저가 정지 처리되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['admin-reports'] });
    },
    onError: () => {
      toast.error('처리에 실패했습니다.');
    }
  });

  const provideCreditsMutation = useMutation({
    mutationFn: (userId: string) => adminAPI.provideCredits(userId),
    onSuccess: () => {
      toast.success('크레딧이 수동 지급되었습니다.');
    },
    onError: () => {
      toast.error('크레딧 지급에 실패했습니다.');
    }
  });

  return {
    reports: data?.content || [],
    isLoading,
    blindPost: blindMutation.mutate,
    unblindPost: unblindMutation.mutate,
    suspendUser: suspendUserMutation.mutate,
    provideCredits: provideCreditsMutation.mutate,
  };
}

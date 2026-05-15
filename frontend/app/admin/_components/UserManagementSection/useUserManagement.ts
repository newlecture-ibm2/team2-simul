import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminAPI } from '@/lib/api/adminAPI';
import { toast } from '@/lib/utils/toast';

export function useUserManagement() {
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => adminAPI.getUsers(0, 50),
  });

  const suspendUserMutation = useMutation({
    mutationFn: (userId: string) => adminAPI.suspendUser(userId),
    onSuccess: () => {
      toast.success('유저가 정지 처리되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
    },
    onError: () => {
      toast.error('유저 정지에 실패했습니다.');
    }
  });

  const provideCreditsMutation = useMutation({
    mutationFn: ({ userId, amount }: { userId: string; amount: number }) => adminAPI.provideCredits(userId, amount),
    onSuccess: () => {
      toast.success('크레딧이 수동 지급되었습니다.');
    },
    onError: () => {
      toast.error('크레딧 지급에 실패했습니다.');
    }
  });

  return {
    users: data?.content || [],
    isLoading,
    suspendUser: suspendUserMutation.mutate,
    provideCredits: provideCreditsMutation.mutate,
  };
}

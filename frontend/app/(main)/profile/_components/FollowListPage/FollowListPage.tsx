'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getFollowers, getFollowings, followUser, unfollowUser, getUserProfile, FollowUserResponse } from '@/lib/api/userAPI';
import { useAuthStore, User } from '@/lib/stores/useAuthStore';
import { toast } from '@/lib/utils/toast';
import ConfirmModal from '@/components/ConfirmModal/ConfirmModal';
import styles from './FollowListPage.module.css';

interface FollowListPageProps {
  userId: string;
  type: 'followers' | 'followings';
}

export default function FollowListPage({ userId, type }: FollowListPageProps) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { isAuthenticated, user: currentUser } = useAuthStore();
  const [currentType, setCurrentType] = useState<'followers' | 'followings'>(type);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, targetId: '' });

  // Sync state with prop if it changes (e.g. initial load)
  useEffect(() => {
    setCurrentType(type);
  }, [type]);

  const queryKey = [currentType, userId];

  const { data: targetUser } = useQuery<User>({
    queryKey: ['userProfile', userId],
    queryFn: () => getUserProfile(userId),
    enabled: !!userId,
  });

  const { data: users, isLoading } = useQuery<FollowUserResponse[]>({
    queryKey,
    queryFn: () => currentType === 'followers' ? getFollowers(userId) : getFollowings(userId),
    enabled: !!userId,
  });

  const followMutation = useMutation({
    mutationFn: (targetId: string) => followUser(targetId),
    onSuccess: () => {
      toast.success('팔로우했습니다!');
      queryClient.invalidateQueries({ queryKey });
      queryClient.invalidateQueries({ queryKey: ['userProfile', userId] });
      queryClient.invalidateQueries({ queryKey: ['me'] });
    }
  });

  const unfollowMutation = useMutation({
    mutationFn: (targetId: string) => unfollowUser(targetId),
    onSuccess: () => {
      toast.info('언팔로우했습니다.');
      queryClient.invalidateQueries({ queryKey });
      queryClient.invalidateQueries({ queryKey: ['userProfile', userId] });
      queryClient.invalidateQueries({ queryKey: ['me'] });
    }
  });

  const handleToggleFollow = (targetId: string, isFollowing: boolean) => {
    if (!isAuthenticated) {
      toast.error('로그인이 필요합니다.');
      return;
    }
    if (isFollowing) {
      setConfirmModal({ isOpen: true, targetId });
    } else {
      followMutation.mutate(targetId);
    }
  };

  const handleConfirmUnfollow = () => {
    unfollowMutation.mutate(confirmModal.targetId);
    setConfirmModal({ ...confirmModal, isOpen: false });
  };

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <button className={styles.backBtn} onClick={() => router.back()}>
          <img src="/icons/arrow-left.png" alt="Back" className={styles.backIcon} />
        </button>
        <h1 className={styles.title}>{targetUser?.nickname || '사용자'}</h1>
      </header>

      <div className={styles.tabBar}>
        <button 
          className={`${styles.tab} ${currentType === 'followings' ? styles.activeTab : ''}`}
          onClick={() => setCurrentType('followings')}
        >
          팔로잉
        </button>
        <button 
          className={`${styles.tab} ${currentType === 'followers' ? styles.activeTab : ''}`}
          onClick={() => setCurrentType('followers')}
        >
          팔로워
        </button>
      </div>

      {isLoading ? (
        <div className={styles.loading}>목록을 불러오는 중...</div>
      ) : !users || users.length === 0 ? (
        <div className={styles.empty}>
          {currentType === 'followers' ? '팔로워가 없습니다.' : '팔로잉하는 사용자가 없습니다.'}
        </div>
      ) : (
        <div className={styles.list}>
          {users.map((u) => (
            <div key={u.userId} className={styles.userItem}>
              <Link href={`/profile/${u.userId}`}>
                <img src={u.profileImageUrl || '/profile.jpg'} alt={u.nickname} className={styles.avatar} />
              </Link>
              <div className={styles.info}>
                <Link href={`/profile/${u.userId}`} className={styles.nickname}>
                  {u.nickname}
                </Link>
              </div>
              {(!currentUser || String(currentUser.userId) !== String(u.userId)) && (
                <button 
                  className={`${styles.followBtn} ${u.isFollowing ? styles.following : styles.notFollowing}`}
                  onClick={() => handleToggleFollow(u.userId, u.isFollowing)}
                  disabled={followMutation.isPending || unfollowMutation.isPending}
                >
                  {u.isFollowing ? '언팔로우' : '팔로우'}
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title="언팔로우 하시겠습니까?"
        description="더 이상 이 사용자의 소식을 받지 않게 됩니다."
        confirmText="언팔로우"
        isDanger={true}
        onConfirm={handleConfirmUnfollow}
        onCancel={() => setConfirmModal({ ...confirmModal, isOpen: false })}
      />
    </div>
  );
}

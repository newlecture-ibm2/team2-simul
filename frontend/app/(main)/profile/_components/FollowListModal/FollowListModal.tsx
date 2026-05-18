'use client';

import { useEffect } from 'react';
import Link from 'next/link';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getFollowers, getFollowings, followUser, unfollowUser, FollowUserResponse } from '@/lib/api/userAPI';
import { useAuthStore } from '@/lib/stores/useAuthStore';
import styles from './FollowListModal.module.css';

interface FollowListModalProps {
  userId: string;
  type: 'followers' | 'followings';
  onClose: () => void;
}

export default function FollowListModal({ userId, type, onClose }: FollowListModalProps) {
  const queryClient = useQueryClient();
  const { isAuthenticated, user: currentUser } = useAuthStore();

  const queryKey = [type, userId];

  const { data: users, isLoading } = useQuery<FollowUserResponse[]>({
    queryKey,
    queryFn: () => type === 'followers' ? getFollowers(userId) : getFollowings(userId),
    enabled: !!userId,
  });

  const followMutation = useMutation({
    mutationFn: (targetId: string) => followUser(targetId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey });
      queryClient.invalidateQueries({ queryKey: ['userProfile'] });
      queryClient.invalidateQueries({ queryKey: ['me'] });
    }
  });

  const unfollowMutation = useMutation({
    mutationFn: (targetId: string) => unfollowUser(targetId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey });
      queryClient.invalidateQueries({ queryKey: ['userProfile'] });
      queryClient.invalidateQueries({ queryKey: ['me'] });
    }
  });

  const handleToggleFollow = (targetId: string, isFollowing: boolean) => {
    if (!isAuthenticated) {
      alert('로그인이 필요합니다.');
      return;
    }
    if (isFollowing) {
      unfollowMutation.mutate(targetId);
    } else {
      followMutation.mutate(targetId);
    }
  };

  // Close on outside click or ESC key
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [onClose]);

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <div className={styles.title}>{type === 'followers' ? '팔로워' : '팔로잉'}</div>
          <button className={styles.closeBtn} onClick={onClose}>✕</button>
        </div>

        {isLoading ? (
          <div className={styles.loading}>로딩 중...</div>
        ) : !users || users.length === 0 ? (
          <div className={styles.empty}>목록이 없습니다.</div>
        ) : (
          <div className={styles.list}>
            {users.map((u) => (
              <div key={u.userId} className={styles.userItem}>
                <Link href={`/profile/${u.userId}`} onClick={onClose}>
                  <img src={u.profileImageUrl || '/profile.jpg'} alt={u.nickname} className={styles.avatar} />
                </Link>
                <div className={styles.info}>
                  <Link href={`/profile/${u.userId}`} className={styles.nickname} onClick={onClose}>
                    {u.nickname}
                  </Link>
                </div>
                {(!currentUser || String(currentUser.userId) !== String(u.userId)) && (
                  <button 
                    className={`${styles.followBtn} ${u.isFollowing ? styles.following : styles.notFollowing}`}
                    onClick={() => handleToggleFollow(u.userId, u.isFollowing)}
                    disabled={followMutation.isPending || unfollowMutation.isPending}
                  >
                    {u.isFollowing ? '팔로잉 ✓' : '팔로우'}
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

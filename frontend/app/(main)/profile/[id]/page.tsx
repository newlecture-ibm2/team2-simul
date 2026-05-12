'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getUserProfile, followUser, unfollowUser } from '@/lib/api/userAPI';
import { useAuthStore, User } from '@/lib/stores/useAuthStore';
import { toast } from '@/lib/utils/toast';
import ConfirmModal from '@/components/ConfirmModal/ConfirmModal';
import styles from '../page.module.css'; // Use the parent directory's CSS module

export default function UserProfilePage() {
  const [activeTab, setActiveTab] = useState<'게시물' | '옷장'>('게시물');
  const params = useParams();
  const router = useRouter();
  const userId = params.id as string;
  const { isAuthenticated, user: currentUser } = useAuthStore();
  const queryClient = useQueryClient();
  const [isUnfollowModalOpen, setIsUnfollowModalOpen] = useState(false);


  
  // If the user tries to view their own profile via ID, redirect to /profile
  useEffect(() => {
    if (currentUser?.userId === userId) {
      router.replace('/profile');
    }
  }, [currentUser, userId, router]);

  const { data: user, isLoading } = useQuery<User>({
    queryKey: ['userProfile', userId],
    queryFn: () => getUserProfile(userId),
    enabled: !!userId,
  });

  const followMutation = useMutation({
    mutationFn: () => followUser(userId),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ['userProfile', userId] });
      const previousUser = queryClient.getQueryData<User>(['userProfile', userId]);
      if (previousUser) {
        queryClient.setQueryData<User>(['userProfile', userId], {
          ...previousUser,
          isFollowing: true,
          followerCount: (previousUser.followerCount || 0) + 1,
        });
      }
      return { previousUser };
    },
    onError: (err, variables, context) => {
      if (context?.previousUser) {
        queryClient.setQueryData(['userProfile', userId], context.previousUser);
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['userProfile', userId] });
    },
    onSuccess: () => {
      toast.success('팔로우했습니다!');
    },
  });

  const unfollowMutation = useMutation({
    mutationFn: () => unfollowUser(userId),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ['userProfile', userId] });
      const previousUser = queryClient.getQueryData<User>(['userProfile', userId]);
      if (previousUser) {
        queryClient.setQueryData<User>(['userProfile', userId], {
          ...previousUser,
          isFollowing: false,
          followerCount: Math.max((previousUser.followerCount || 0) - 1, 0),
        });
      }
      return { previousUser };
    },
    onError: (err, variables, context) => {
      if (context?.previousUser) {
        queryClient.setQueryData(['userProfile', userId], context.previousUser);
      }
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['userProfile', userId] });
    },
    onSuccess: () => {
      toast.info('언팔로우했습니다.');
    },
  });

  const handleFollowToggle = () => {
    if (!isAuthenticated) {
      toast.error('로그인이 필요합니다.');
      return;
    }
    if (user?.isFollowing) {
      setIsUnfollowModalOpen(true);
    } else {
      followMutation.mutate();
    }
  };

  if (isLoading) return <div className={styles.loading}>로딩 중...</div>;
  if (!user) return <div className={styles.loading}>사용자를 찾을 수 없습니다.</div>;

  return (
    <div className={styles.profilePage}>
      <div className={styles.profileFrame}>
        {/* Immersive Hero Section */}
        <div className={styles.heroSection}>
          <img src={user?.profileImageUrl || "/profile.jpg"} alt="Profile Background" className={styles.heroBg} />
          <div className={styles.heroOverlay}></div>

          <button onClick={() => router.back()} style={{position: 'absolute', top: '16px', left: '16px', zIndex: 10, background: 'none', border: 'none', color: '#fff', fontSize: '24px', cursor: 'pointer'}}>
            ←
          </button>

          {/* Hero Content (Bottom aligned) */}
          <div className={styles.heroContent}>
            <h1 className={styles.heroName}>{user?.nickname || '사용자'}</h1>
            <p className={styles.heroUsername}>{user?.bio || ''}</p>

            <div className={styles.heroActions}>
              <button 
                className={styles.heroBtn} 
                onClick={handleFollowToggle}
                style={{ 
                  backgroundColor: user?.isFollowing ? 'rgba(255,255,255,0.2)' : 'var(--color-primary)',
                  color: '#fff',
                  border: user?.isFollowing ? '1px solid rgba(255,255,255,0.5)' : 'none',
                  flex: 1
                }}
              >
                {user?.isFollowing ? '언팔로우' : '팔로우'}
              </button>
            </div>

            <div className={styles.heroStats}>
              <div 
                className={styles.statItem}
                style={{ cursor: 'pointer' }}
                onClick={() => router.push(`/profile/${userId}/followings`)}
              >
                <span className={styles.statNum}>{user?.followingCount || 0}</span>
                <span className={styles.statText}>팔로잉</span>
              </div>
              <div 
                className={styles.statItem}
                style={{ cursor: 'pointer' }}
                onClick={() => router.push(`/profile/${userId}/followers`)}
              >
                <span className={styles.statNum}>{user?.followerCount || 0}</span>
                <span className={styles.statText}>팔로워</span>
              </div>
              <div className={styles.statItem}>
                <span className={styles.statNum}>0</span>
                <span className={styles.statText}>게시물</span>
              </div>
            </div>
          </div>
        </div>

        <div className={styles.bottomSection}>
          {/* Tabs */}
          <div className={styles.tabBar}>
            <button 
              className={`${styles.tabBtn} ${activeTab === '게시물' ? styles.activeTab : ''}`}
              onClick={() => setActiveTab('게시물')}
            >
              게시물
            </button>
            <button 
              className={`${styles.tabBtn} ${activeTab === '옷장' ? styles.activeTab : ''}`}
              onClick={() => setActiveTab('옷장')}
            >
              옷장
            </button>
          </div>

          {/* Grid Content */}
          <div className={styles.gridContent}>
            {activeTab === '게시물' && (
              <div className={styles.postGrid}>
                {Array.from({ length: 12 }, (_, i) => (
                  <Link key={i} href={`/post/${i + 1}`} className={styles.gridItem}>
                    <img src="/recent.jpg" alt="Post" className={styles.gridImage} />
                  </Link>
                ))}
              </div>
            )}
            
            {activeTab === '옷장' && (
              <div className={styles.postGrid}>
                {Array.from({ length: 12 }, (_, i) => (
                  <Link key={i} href="/closet" className={styles.gridItem}>
                    <img src="/clothes.png" alt="Closet item" className={styles.gridImage} />
                  </Link>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      <ConfirmModal
        isOpen={isUnfollowModalOpen}
        title="언팔로우 하시겠습니까?"
        description={`${user?.nickname}님의 소식을 더 이상 받지 않게 됩니다.`}
        confirmText="언팔로우"
        isDanger={true}
        onConfirm={() => {
          unfollowMutation.mutate();
          setIsUnfollowModalOpen(false);
        }}
        onCancel={() => setIsUnfollowModalOpen(false)}
      />
    </div>
  );
}
